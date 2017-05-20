package androidapp.social.jj.letshangout.dao;

import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import androidapp.social.jj.letshangout.dto.User;

/**
 * Created by Jason on 4/8/2017.
 */

public class UserDAO {

    public static void registerUser(User newUser)
    {
        // Firebase DB reference
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        Map<String, Object> childUpdates = new HashMap<>();

        Map<String, Object> userValues = newUser.toMap();
        // creating Invitation object
        childUpdates.put("/User/" + newUser.getUserId(), userValues);

        // save all objects atomically
        firebaseDatabase.getReference().updateChildren(childUpdates);

    }
}
