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
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.github.jjobes.slidedatetimepicker.DateFragment;
import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import androidapp.social.jj.letshangout.R;
import androidapp.social.jj.letshangout.dao.InvitationDAO;
import androidapp.social.jj.letshangout.dto.User;
import androidapp.social.jj.letshangout.utils.Constants;
import androidapp.social.jj.letshangout.utils.ContactsCompletionView;
import androidapp.social.jj.letshangout.utils.PlaceAutocompleteAdapter;
import androidapp.social.jj.letshangout.utils.PlacesCompletionView;

public class AddEditInvitationActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
                    OnConnectionFailedListener, DateFragment.DateChangedListener{

    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;

    private static final String TAG = "AddEditInvitation";
    private final Context context = this;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private PlacesCompletionView placesCompletionView;
    private ContactsCompletionView contactsCompletionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_invitation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // setup who autocomplete, with chips
        setupWhoAutocomplete();

        // setup where autocomplete with google places, with chips
        setupWhereAutocomplete();

        // setup when datepicker
        setupWhenDatePicker();


        // send Invitation button
        Button button_send = (Button) findViewById(R.id.button_send);
        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createInvitation();
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

    /*
        setup autocomplete with chips
        - based on TokenAutoComplete
        - https://github.com/splitwise/TokenAutoComplete
        - currently filtering is based on fullName only, not email

    */
    private void setupWhoAutocomplete() {
        final ArrayAdapter<User> adapter = new ArrayAdapter<User>(this, android.R.layout.simple_list_item_1); // the view for the dropdown for auto/multiAutocompleteTextView

        DatabaseReference friendsReference = FirebaseDatabase.getInstance().getReference("User");

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new user has been added, add it to the displayed list
                User user = dataSnapshot.getValue(User.class);
                adapter.add(user);

                System.out.println("------------> User added");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A user has changed, use the key to determine if we are displaying this
                // user and if so displayed the changed user.
                User changedUser = dataSnapshot.getValue(User.class);
                String userKey = dataSnapshot.getKey();

                int userIndex = adapter.getPosition(changedUser);

                adapter.remove(changedUser); // remove the user with the same id based on the equals method
                adapter.insert(changedUser, userIndex); // insert the user in the index location that was deleted

                System.out.println("------------> User changed");

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A user has changed, use the key to determine if we are displaying this
                // user and if so remove it.
                User userToRemove = dataSnapshot.getValue(User.class);
                adapter.remove(userToRemove);

                System.out.println("------------> User deleted");

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                // A user has changed position, use the key to determine if we are
                // displaying this user and if so move it.
                User movedUser = dataSnapshot.getValue(User.class);
                String userKey = dataSnapshot.getKey();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "setupWhoAutocomplete:onCancelled", databaseError.toException());
                Toast.makeText(context, "Failed to load friends.", Toast.LENGTH_SHORT).show();
            }
        };

        friendsReference.addChildEventListener(childEventListener);

        contactsCompletionView =
                (ContactsCompletionView) findViewById(R.id.contactsCompletionView_who);
        contactsCompletionView.setAdapter(adapter);
        contactsCompletionView.allowDuplicates(false);
        contactsCompletionView.performBestGuess(true); // does not allow free entry
        contactsCompletionView.setTokenListener(contactsCompletionView);
    }

    /*
        setup where autocomplete

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

        placesCompletionView =
                (PlacesCompletionView)findViewById(R.id.placesCompletionView_where);
        placesCompletionView.setAdapter(adapter);
        placesCompletionView.allowDuplicates(false);
        placesCompletionView.performBestGuess(false); // allows free entry
        placesCompletionView.setTokenListener(placesCompletionView);
    }

    // using slideDateTimePicker to combine Date and Time picker
    private void setupWhenDatePicker()
    {
        final SlideDateTimeListener listener = new SlideDateTimeListener() {

            @Override
            public void onDateTimeSet(Date date)
            {
                EditText editText_when = (EditText) findViewById(R.id.editText_when);
                editText_when.setText(Constants.simpleDateFormat.format(date));
            }

            @Override
            public void onDateTimeCancel()
            {
                // Overriding onDateTimeCancel() is optional.
            }
        };


        ImageButton imageButton_whenCalendar = (ImageButton) findViewById(R.id.imageButton_whenCalendar);
        imageButton_whenCalendar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(listener)
                        .setInitialDate(new Date())
                        //.setMinDate(minDate)
                        //.setMaxDate(maxDate)
                        //.setIs24HourTime(true)
                        //.setTheme(SlideDateTimePicker.HOLO_DARK)
                        //.setIndicatorColor(Color.parseColor("#990000"))
                        .build()
                        .show();
            }
        });
    }


    public static final LatLngBounds setBounds(Location location, int mDistanceInMeters ) {
        double latRadian = Math.toRadians(location.getLatitude());
        double degLatKm = 110.574235;
        double degLongKm = 110.572833 * Math.cos(latRadian);
        double deltaLat = mDistanceInMeters / 1000.0 / degLatKm;
        double deltaLong = mDistanceInMeters / 1000.0 / degLongKm;

        double minLat = location.getLatitude() - deltaLat;
        double minLong = location.getLongitude() - deltaLong;
        double maxLat = location.getLatitude() + deltaLat;
        double maxLong = location.getLongitude() + deltaLong;

        Log.d("Location", "Min: " + Double.toString(minLat) + "," + Double.toString(minLong));
        Log.d("Location", "Max: " + Double.toString(maxLat) + "," + Double.toString(maxLong));

        // Set up the adapter that will retrieve suggestions from the Places Geo Data API that cover
        // the entire world.

        return new LatLngBounds(new LatLng(minLat, minLong), new LatLng(maxLat, maxLong));
    }

    private void createInvitation()
    {
        try
        {
            String what = ((EditText) findViewById(R.id.editText_what)).getText().toString();
            Date when = Constants.simpleDateFormat.parse(((EditText) findViewById(R.id.editText_when)).getText().toString());
            Map<String, User> inviteesMap = contactsCompletionView.getInviteesMap();
            Map<String, AutocompletePrediction> placeMap = placesCompletionView.getPlaceMap();
            InvitationDAO.createInvitation(what, when, inviteesMap, placeMap);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }


        // Navigate to the home screen after creating an invitation
        Intent intent = new Intent(context, HomeActivity.class);
        finish();
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                            mGoogleApiClient);
                    if (mLastLocation != null) {
                        System.out.println("--> Latitude: " + String.valueOf(mLastLocation.getLatitude()));
                        System.out.println("--> Longitude: " + String.valueOf(mLastLocation.getLongitude()));

                        // now that mLastLocation has been set, we can set the proper bounbds
                        // for the where autocomplete
                        setupWhereAutocomplete();
                    }
                } else {
                    // Permission Denied
                    // cannot access location
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onDateChanged(int year, int month, int day) {
        // Todo
    }
}
