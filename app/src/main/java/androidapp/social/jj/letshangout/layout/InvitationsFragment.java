package androidapp.social.jj.letshangout.layout;

/**
 * Created by Jason on 1/12/2017.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import androidapp.social.jj.letshangout.R;
import androidapp.social.jj.letshangout.adapters.InvitationArrayAdapter;
import androidapp.social.jj.letshangout.dto.Invitation;
import androidapp.social.jj.letshangout.utils.Constants;

/**
 * A placeholder fragment containing a simple view.
 * Resource:
 *  http://stackoverflow.com/questions/3040374/runtimeexception-your-content-must-have-a-listview-whose-id-attribute-is-andro
 */
public class InvitationsFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NAME = "section_number";
    private static final String TAG = "InvitationsFragment";

    public InvitationsFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static InvitationsFragment newInstance(String sectionName) {
        InvitationsFragment fragment = new InvitationsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SECTION_NAME, sectionName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_invitations, container, false);

        setSentInvitationsList(rootView);
        setWaitingInvitationsList(rootView);


        return rootView;
    }

    /*
     *   setup sent invitations list
     */
    private void setSentInvitationsList(View rootView)
    {
        setInvitationsList(rootView,
                Constants.invitationListOptions.sentInvitations.toString(),
                R.id.listView_sentInvitations);
    }

    /*
     *   setup waiting invitations list
     */
    private void setWaitingInvitationsList(View rootView)
    {
        setInvitationsList(rootView,
                Constants.invitationListOptions.waitingInvitations.toString(),
                R.id.listView_waitingInvitations);
    }

    /*
     *   setup the adapter to listen for the invitation list changes
     */
    private void setInvitationsList(View rootView, String invitationListType, int listViewId)
    {
        // Firebase database
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        String loggedInUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // invitation reference
        DatabaseReference invitationsReference =
                firebaseDatabase.getReference("InvitationsList/"+loggedInUserId+"/"+invitationListType+"/");

        ArrayList<Invitation> invitationsList = new ArrayList<>();
        if (getActivity() != null) {
            final InvitationArrayAdapter invitationArrayAdapter =
                    new InvitationArrayAdapter(getActivity(), invitationsList);

            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    // A new invitation has been added, add it to the displayed list
                    Invitation invitation = dataSnapshot.getValue(Invitation.class);
                    invitationArrayAdapter.add(invitation);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                    Invitation invitation = dataSnapshot.getValue(Invitation.class);
                    int invitationIndex = invitationArrayAdapter.getPosition(invitation);

                    invitationArrayAdapter.remove(invitation); // remove the invitation with the same id based on the equals method
                    invitationArrayAdapter.insert(invitation, invitationIndex); // insert the invitation in the index location that was deleted

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                    Invitation invitation = dataSnapshot.getValue(Invitation.class);
                    invitationArrayAdapter.remove(invitation);
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                    // ...
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                    Toast.makeText(getActivity(), "Failed to load comments.", Toast.LENGTH_SHORT).show();
                }
            };

            invitationsReference.addChildEventListener(childEventListener);
            ListView listView = (ListView) rootView.findViewById(listViewId);

            // navigate to the send RSVP screen when invitation is clicked
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Invitation invitation = invitationArrayAdapter.getItem(position);

                    Intent intent = new Intent(getActivity(), AddEditRSVPActivity.class);
                    intent.putExtra("invitation", invitation);
                    startActivity(intent);
                }
            });
            listView.setAdapter(invitationArrayAdapter);
        }
    }
}
