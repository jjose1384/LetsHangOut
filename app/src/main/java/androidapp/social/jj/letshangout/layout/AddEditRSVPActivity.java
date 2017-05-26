package androidapp.social.jj.letshangout.layout;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import androidapp.social.jj.letshangout.R;
import androidapp.social.jj.letshangout.dto.Invitation;
import androidapp.social.jj.letshangout.dto.InvitationResponse;
import androidapp.social.jj.letshangout.dto.Place;
import androidapp.social.jj.letshangout.dto.User;
import androidapp.social.jj.letshangout.utils.Constants;
import androidapp.social.jj.letshangout.utils.PlaceAutocompleteAdapter;
import androidapp.social.jj.letshangout.utils.PlacesCompletionView;

import static androidapp.social.jj.letshangout.layout.AddEditInvitationActivity.setBounds;

public class AddEditRSVPActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;

    private static final String TAG = "AddEditRSVP";
    private final Context context = this;

    private PlacesCompletionView placesCompletionView_other;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    // maps the radio button id Integer to the placeId String
    final private HashMap<Integer, String> radioIdToPlaceId = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_rsvp);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*
         * Set listeners before loading data, so that the enabling and disabling
         * works correctly based on the data
         */

        // listeners for going switch and places radio buttons
        switchRadioGroupListeners();

        // listeners for send cancel buttons
        submitCancelButtonListeners();

        // setup where autocomplete with google places, with chips (limit 1)
        setupWhereAutocomplete();

        // load RSVP data
        Invitation invitation = (Invitation)getIntent().getExtras().get("invitation");
        loadData(invitation);
    }

    /*
     *  - Logic to enable places list when going is set to yes, or vice versa
     *  - Logic to enable/disable other text box when the other option is selected
     */
    private void switchRadioGroupListeners()
    {

        // enable disable other text box based on other radio option
        placesCompletionView_other =
                (PlacesCompletionView) findViewById(R.id.placesCompletionView_other);
        RadioGroup radioGroup_where = (RadioGroup) findViewById(R.id.radioGroup_where);
        radioGroup_where.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                if (checkedId == R.id.radioButton_other)
                {
                    // enable other textbox
                    placesCompletionView_other.setEnabled(true);
                    placesCompletionView_other.requestFocus();
                }
                else
                {
                    // disable other textbox
                    placesCompletionView_other.setText("");
                    placesCompletionView_other.setEnabled(false);
                }
            }
        });


        // enable disable places list based on going switch
        Switch switch_going = (Switch) findViewById(R.id.switch_going);
        switch_going.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // the isChecked will be true if the switch is in the On position
                enableDisablePlacesRadioGroup(isChecked);
            }
        });

    }

    /*
     * enable places radio group if going is true, disable otherwise
     * if going is null, then this method will check whether the going switch is
     * checked or not
     */
    private void enableDisablePlacesRadioGroup(Boolean going)
    {
        // if going is null, check the value of the switch
        if (going == null)
        {
            Switch switch_going = (Switch) findViewById(R.id.switch_going);
            going  = switch_going.isChecked();
        }

        RadioGroup radioGroup_where = (RadioGroup) findViewById(R.id.radioGroup_where);
        if (going == true)
        {
            for(int i = 0; i < radioGroup_where.getChildCount(); i++){
                RadioButton radioButton = (RadioButton)radioGroup_where.getChildAt(i);
                radioButton.setEnabled(true);
            }
        }
        else
        {
            for(int i = 0; i < radioGroup_where.getChildCount(); i++){
                RadioButton radioButton = (RadioButton)radioGroup_where.getChildAt(i);
                radioButton.setChecked(false);
                radioButton.setEnabled(false);

                placesCompletionView_other =
                        (PlacesCompletionView) findViewById(R.id.placesCompletionView_other);
                placesCompletionView_other.setText("");
                placesCompletionView_other.setEnabled(false);
            }
        }
    }

    // listeners for the send cancel buttons
    private void submitCancelButtonListeners()
    {
        // send RSVP button
        Button button_send = (Button) findViewById(R.id.button_send);
        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRSVP();
            }
        });

        // cancel button
        Button button_cancel = (Button) findViewById(R.id.button_cancel);
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, HomeActivity.class);
                finish();
                startActivity(intent);
            }
        });
    }

    private void loadData(final Invitation invitation) {

        // set what
        TextView textView_whatValue = (TextView) findViewById(R.id.textView_whatValue);
        textView_whatValue.setText(invitation.getWhat().toString());

        // set when
        TextView textView_whenValue = (TextView) findViewById(R.id.textView_whenValue);
        textView_whenValue.setText(Constants.simpleDateFormat.format(invitation.getWhen()));

        // going switch
        final Switch switch_going = (Switch) findViewById(R.id.switch_going);
        switch_going.requestFocus();

        // retrieve logged in user's invitationResponse to update going status and vote
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        String loggedInUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference loggedInUserInvitationResponseReference =
                firebaseDatabase.getReference("InvitationResponse/" +
                        invitation.getInvitationId() + "/" + loggedInUserId + "/");
        ValueEventListener invitationResponseListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                InvitationResponse invitationResponse =
                        dataSnapshot.getValue(InvitationResponse.class);

                if (Constants.goingOptions.YES.toString().equals(invitationResponse.getGoing()))
                {
                    switch_going.setChecked(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Invitation response failed, log a message
                Log.w(TAG, "loadInvitationResponse:onCancelled", databaseError.toException());
                // ...
            }
        };
        loggedInUserInvitationResponseReference.addValueEventListener(invitationResponseListener);



        loadInvitationResponses(invitation);
        loadPlaces(invitation);
    }

    // list of who's
    private void loadInvitationResponses(final Invitation invitation)
    {
        // Firebase database
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        String loggedInUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // set who
        final TextView textView_whoValue = (TextView) findViewById(R.id.textView_whoValue);

        // invitation reference
        DatabaseReference invitationResponseReference =
                firebaseDatabase.getReference("InvitationResponse/" + invitation.getInvitationId() + "/");


        if (context != null) {

            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    // A new invitationResponse has been added
                    InvitationResponse invitationResponse = dataSnapshot.getValue(InvitationResponse.class);

                    // user reference
                    DatabaseReference userReference =
                            firebaseDatabase.getReference("User/" + invitationResponse.getUserId() + "/");
                    ValueEventListener postListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            textView_whoValue.setText(textView_whoValue.getText() + user.getFullName() + "\n");
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // ...
                        }
                    };
                    userReference.addValueEventListener(postListener);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                    InvitationResponse invitationResponse = dataSnapshot.getValue(InvitationResponse.class);

                    // user reference
                    DatabaseReference userReference =
                            firebaseDatabase.getReference("User/" + invitationResponse.getUserId() + "/");
                    ValueEventListener postListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            textView_whoValue.setText(textView_whoValue.getText() + user.getFullName() + "\n");
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // ...
                        }
                    };
                    userReference.addValueEventListener(postListener);

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                    InvitationResponse invitationResponse = dataSnapshot.getValue(InvitationResponse.class);
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                    // ...
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                    Toast.makeText(context, "Failed to load InvitationResponse.", Toast.LENGTH_SHORT).show();
                }
            };

            invitationResponseReference.addChildEventListener(childEventListener);
        }
    }

    // list of places
    private void loadPlaces(Invitation invitation)
    {
        // Firebase database
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        String loggedInUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // place reference
        DatabaseReference placeReference =
                firebaseDatabase.getReference("Place/" + invitation.getInvitationId() + "/");

        final RadioGroup radioGroup_where = (RadioGroup) findViewById(R.id.radioGroup_where);
        if (context != null) {

            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    // A new place has been added
                    Place place = dataSnapshot.getValue(Place.class);
                    RadioButton radioButton = new RadioButton(context);
                    radioButton.setText(place.getName());

                    int radioButtonId = radioIdToPlaceId.size();
                    radioIdToPlaceId.put(radioButtonId, place.getPlaceId());
                    radioButton.setId(radioButtonId);
                    radioGroup_where.addView(radioButton, 0);
                    enableDisablePlacesRadioGroup(null);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                    Place place = dataSnapshot.getValue(Place.class);
                    RadioButton radioButton = new RadioButton(context);
                    radioButton.setText(place.getName());

                    int radioButtonId = radioIdToPlaceId.size();
                    radioIdToPlaceId.put(radioButtonId, place.getPlaceId());
                    radioButton.setId(radioButtonId);
                    radioGroup_where.addView(radioButton, 0);
                    enableDisablePlacesRadioGroup(null);

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                    // ...
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                    // ...
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(context, "Failed to load Place.", Toast.LENGTH_SHORT).show();
                }
            };

            placeReference.addChildEventListener(childEventListener);
        }


        // Todo: select place if voted
    }

    /*
     * setup where autocomplete
    */
    private void setupWhereAutocomplete() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(com.google.android.gms.location.places.Places.GEO_DATA_API)
                    .addApi(com.google.android.gms.location.places.Places.PLACE_DETECTION_API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .enableAutoManage(this, this).build();
        }

        /*
            LatLngBounds based on phone's location
            - http://stackoverflow.com/questions/32352407/how-to-set-correct-lat-and-lng-based-on-current-location
         */
        LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
                new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));
        LatLngBounds bounds = BOUNDS_GREATER_SYDNEY;
        // don't have permission to access location

        if (mLastLocation != null)
        {
            bounds = setBounds(mLastLocation, 5000); // 10 km radius
        }


        PlaceAutocompleteAdapter adapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, bounds,
                null);


        placesCompletionView_other =
                (PlacesCompletionView)findViewById(R.id.placesCompletionView_other);
        placesCompletionView_other.setAdapter(adapter);
        placesCompletionView_other.allowDuplicates(false);
        placesCompletionView_other.setTokenLimit(1); // can only add 1 suggestion
        placesCompletionView_other.performBestGuess(false); // allows free entry
        placesCompletionView_other.setTokenListener(placesCompletionView_other);
    }

    private void sendRSVP()
    {

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.


            // permissions
            // http://stackoverflow.com/questions/33327984/call-requires-permissions-that-may-be-rejected-by-user

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_ACCESS_COARSE_LOCATION);

        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        // now that mLastLocation has been set, we can set the proper bounbds
        // for the where autocomplete
        setupWhereAutocomplete();
    }

    protected void onStart() {
        mGoogleApiClient.connect();

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.


            // permissions
            // http://stackoverflow.com/questions/33327984/call-requires-permissions-that-may-be-rejected-by-user

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_ACCESS_COARSE_LOCATION);

        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        // now that mLastLocation has been set, we can set the proper bounbds
        // for the where autocomplete
        setupWhereAutocomplete();

        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
