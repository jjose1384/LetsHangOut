package androidapp.social.jj.letshangout.layout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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

public class AddEditRSVPActivity extends AppCompatActivity{

    private static final String TAG = "AddEditRSVP";
    private final Context context = this;

    // maps the radio button id Integer to the placeId String
    final private HashMap<Integer, String> radioIdToPlaceId = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_rsvp);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // load RSVP data
        Invitation invitation = (Invitation)getIntent().getExtras().get("invitation");
        loadData(invitation);

        // listeners for going switch and ra
        switchRadioGroupListeners();

        // listeners for send cancel buttons
        submitCancelButtonListeners();



    }

    /*
     *  - Logic to enable places list when going is set to yes, or vice versa
     *  - Logic to enable/disable other text box when the other option is selected
     */
    private void switchRadioGroupListeners()
    {

        // enable disable other text box based on other radio option
        final EditText editText_other = (EditText) findViewById(R.id.editText_other);
        RadioGroup radioGroup_where = (RadioGroup) findViewById(R.id.radioGroup_where);
        radioGroup_where.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                if (checkedId == R.id.radioButton_other)
                {
                    // enable other textbox
                    editText_other.setEnabled(true);
                    editText_other.requestFocus();
                }
                else
                {
                    // disable other textbox
                    editText_other.setText("");
                    editText_other.setEnabled(false);
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

    /* enable places radio group if going is true, disable otherwise
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

                EditText editText_other = (EditText) findViewById(R.id.editText_other);
                editText_other.setText("");
                editText_other.setEnabled(false);
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

    private void loadData(Invitation invitation) {

        // set what
        TextView textView_whatValue = (TextView) findViewById(R.id.textView_whatValue);
        textView_whatValue.setText(invitation.getWhat().toString());

        // set when
        TextView textView_whenValue = (TextView) findViewById(R.id.textView_whenValue);
        textView_whenValue.setText(Constants.simpleDateFormat.format(invitation.getWhen()));

        Switch switch_going = (Switch) findViewById(R.id.switch_going);
        switch_going.requestFocus();

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
    }

    private void sendRSVP()
    {

    }

}
