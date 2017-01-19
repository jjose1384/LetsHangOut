package androidapp.social.jj.letshangout.layout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidapp.social.jj.letshangout.R;
import androidapp.social.jj.letshangout.dto.User;

public class RegistrationActivity extends AppCompatActivity {

    private static final String TAG = "Registration";
    private final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // register button
        Button button_register = (Button) findViewById(R.id.button_register);
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();

                Intent intent = new Intent(context, LoginActivity.class);
                finish();
                startActivity(intent);
            }
        });

        // cancel button
        Button button_cancel = (Button) findViewById(R.id.button_cancel);
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, LoginActivity.class);
                finish();
                startActivity(intent);
            }
        });

    }

    private void register() {
        final String email = ((EditText) findViewById(R.id.editText_email)).getText().toString();
        final String password = ((EditText) findViewById(R.id.editText_password)).getText().toString();


        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                // don't keep the user signed in.
                // TODO: will need to code email verification
                firebaseAuth.signOut();

                if (!task.isSuccessful()) // registration failed
                {
                    Toast.makeText(context, R.string.registration_failed, Toast.LENGTH_LONG).show();
                    System.out.println("----> registration failed");
                } else // registration successful
                {
                    Toast.makeText(context, R.string.registration_successful, Toast.LENGTH_LONG).show();
                    System.out.println("----> registration successful");

                    String userId = firebaseAuth.getCurrentUser().getUid();

                    // Register a user
                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference userReference = firebaseDatabase.getReference("User/" + userId);

                    User newUser = new User();
                    newUser.setFullName(((EditText) findViewById(R.id.editText_fullName)).getText().toString());
                    newUser.setEmail(email);
                    newUser.setPassword(password);
                    newUser.setUserId(userId);

                    userReference.setValue(newUser);
                }
            }
        });

    }
}
