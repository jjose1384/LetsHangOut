package androidapp.social.jj.letshangout.dao;

import android.util.Log;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidapp.social.jj.letshangout.dto.Invitation;
import androidapp.social.jj.letshangout.dto.InvitationResponse;
import androidapp.social.jj.letshangout.dto.Place;
import androidapp.social.jj.letshangout.dto.User;
import androidapp.social.jj.letshangout.utils.Constants;

/**
 * Created by Jason on 4/7/2017.
 */

public class InvitationDAO {

    private static final String TAG = "InvitationDAO";

    public static void deleteInvitation(final String invitationId)
    {

        // Firebase DB reference
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final Map<String, Object> childUpdates = new HashMap<>();


        /*
            delete invitationList for each user
            retrieve list of users invited to the even based on InvitationResponse
            invitationResponse reference
        */
        DatabaseReference invitationResponseReference =
                firebaseDatabase.getReference("/InvitationResponse/"+invitationId+"/");
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                InvitationResponse invitationResponse = dataSnapshot.getValue(InvitationResponse.class);

                /*
                    remove waiting, sent, accepted, declined invitations from InvitationsList for a user
                    if there are any
                */
                childUpdates.put("/InvitationsList/" +
                        invitationResponse.getUserId() +"/" +
                        Constants.invitationListOptions.waitingInvitations.toString()+ "/" +
                        invitationId + "/", null);
                childUpdates.put("/InvitationsList/" +
                        invitationResponse.getUserId() +"/" +
                        Constants.invitationListOptions.sentInvitations.toString()+ "/" +
                        invitationId + "/", null);
                childUpdates.put("/InvitationsList/" +
                        invitationResponse.getUserId() +"/" +
                        Constants.invitationListOptions.acceptedInvitations.toString()+ "/" +
                        invitationId + "/", null);
                childUpdates.put("/InvitationsList/" +
                        invitationResponse.getUserId() +"/" +
                        Constants.invitationListOptions.declinedInvitations.toString()+ "/" +
                        invitationId + "/", null);

                // delete invitation based on invitationId
                childUpdates.put("/Invitation/" + invitationId, null);

                // delete invitationResponse based on invitationId
                childUpdates.put("/InvitationResponse/" + invitationId, null);

                // delete place based on invitationId
                childUpdates.put("/Place/" + invitationId, null);


                /*
                    deletion code: runs every time a child in InvitationResponse is
                    found, representing an invitee. The Invitation, InvitationResponse
                    and Place are added to childUpdates but they are nullified on the
                    first update and future updateChildren calls make no difference
                */
                firebaseDatabase.getReference().updateChildren(childUpdates);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        invitationResponseReference.addChildEventListener(childEventListener);

    }

    public static void createInvitation(String what, Date when,
                                        Map<String, User> inviteesMap, Map<String, AutocompletePrediction> placeMap)
    {

        // Firebase DB reference
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        Map<String, Object> childUpdates = new HashMap<>();
        String loggedInUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // new invitation
        Invitation newInvitation = new Invitation();
        String invitationId = firebaseDatabase.getReference("Invitation/").push().getKey();
        newInvitation.setInvitationId(invitationId);
        newInvitation.setSender(loggedInUserId);
        newInvitation.setWhat(what);
        newInvitation.setWhen(when.getTime());
        newInvitation.setInviteeCount(inviteesMap.keySet().size());

        Map<String, Object> invitationValues = newInvitation.toMap();
        // creating Invitation object
        childUpdates.put("/Invitation/" + invitationId, invitationValues);

        // adding Invitation object into sentInvitations
        // and waitingInvitations under InvitationsList for the sender
        childUpdates.put("/InvitationsList/" + loggedInUserId + "/waitingInvitations/" + invitationId, invitationValues);
        childUpdates.put("/InvitationsList/" + loggedInUserId + "/sentInvitations/" + invitationId, invitationValues);


        // creating InvitationResponse for the sender
        InvitationResponse newInvitationResponse = new InvitationResponse();
        newInvitationResponse.setInvitationId(invitationId);
        newInvitationResponse.setUserId(loggedInUserId);
        newInvitationResponse.setGoing(Constants.goingOptions.NOT_RESPONDED.toString());
        Map<String, Object> invitationResponseValues = newInvitationResponse.toMap();
        childUpdates.put("/InvitationResponse/" + invitationId + "/" + loggedInUserId, invitationResponseValues);


        // add list of friends(InvitationResponse) and add InvitationsList
        // so you can get to the list of invitations easily for each user
        for (String userId: inviteesMap.keySet())
        {
            // creating InvitationResponse for invitees
            User invitee = inviteesMap.get(userId);
            newInvitationResponse.setUserId(userId);

            invitationResponseValues = newInvitationResponse.toMap();
            childUpdates.put("/InvitationResponse/" + invitationId + "/" + userId, invitationResponseValues);

            // creating InvitationsList objects under waiting initations(one for each invitee, including sender)
            childUpdates.put("/InvitationsList/" + userId + "/waitingInvitations/" + invitationId, invitationValues);
        }


        // add list of places
        for (String googlePlaceId: placeMap.keySet())
        {
            AutocompletePrediction googlePlace = placeMap.get(googlePlaceId);

            Place newPlace = new Place();
            String placeId = firebaseDatabase.getReference("Place/"+invitationId + "/").push().getKey();
            newPlace.setInvitationId(invitationId);
            newPlace.setPlaceId(placeId);
            // if it's not an actual google placeId, don't add it
            newPlace.setGooglePlaceId(googlePlaceId.startsWith(Constants.placeIdPlaceHolderPrefix)?null:googlePlaceId);
            newPlace.setName(googlePlace.getPrimaryText(null).toString());

            Map<String, Object> placeValues = newPlace.toMap();
            childUpdates.put("/Place/" + invitationId + "/" + placeId, placeValues);
        }


        // save all objects atomically
        firebaseDatabase.getReference().updateChildren(childUpdates);
    }
}
