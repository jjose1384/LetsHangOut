package androidapp.social.jj.letshangout.layout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import androidapp.social.jj.letshangout.R;
import androidapp.social.jj.letshangout.dto.User;
import androidapp.social.jj.letshangout.utils.ContactsCompletionView;

public class AddEditInvitationActivity extends AppCompatActivity
                implements OnConnectionFailedListener{

    private static final String TAG = "AddEditInvitation";
    private final Context context = this;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_invitation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // setup who autocomplete
        setupWhoAutocomplete();


        // setup google places
        setupGooglePlaces();


        // send Invitation button
        Button button_send = (Button) findViewById(R.id.button_send);
        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createInvitation();

                Intent intent = new Intent(context, HomeActivity.class);
                finish();
                startActivity(intent);
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

    // setup autocomplete with chips
    private void setupWhoAutocomplete()
    {
        ContactsCompletionView completionView;

        User[] people;
        ArrayAdapter<User> adapter;

        people = new User[]{
                new User(null, "Marshall Weir", "marshall@example.com", null),
                new User(null, "Margaret Smith", "margaret@example.com", null),
                new User(null, "Max Jordan", "max@example.com", null),
                new User(null, "Meg Peterson", "meg@example.com", null),
                new User(null, "Amanda Johnson", "amanda@example.com", null),
                new User(null, "Terry Anderson", "terry@example.com", null)
        };

        adapter = new ArrayAdapter<User>(this, android.R.layout.simple_list_item_1, people);

        completionView = (ContactsCompletionView)findViewById(R.id.contactsCompletionView_who);
        completionView.setAdapter(adapter);
    }

    // setup google places autocomplete
    private void setupGooglePlaces()
    {
        mGoogleApiClient = new GoogleApiClient
            .Builder(this)
            .addApi(Places.GEO_DATA_API)
            .addApi(Places.PLACE_DETECTION_API)
            .enableAutoManage(this, this)
            .build();

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }


    private void createInvitation()
    {
        // TODO
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
