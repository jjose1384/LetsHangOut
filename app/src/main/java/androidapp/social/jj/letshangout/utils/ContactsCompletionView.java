package androidapp.social.jj.letshangout.utils;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tokenautocomplete.TokenCompleteTextView;

import java.util.HashMap;
import java.util.Map;

import androidapp.social.jj.letshangout.R;
import androidapp.social.jj.letshangout.dto.User;

/**
 * Created by Jason on 1/19/2017.
 */

public class ContactsCompletionView extends TokenCompleteTextView<User>
        implements TokenCompleteTextView.TokenListener{

    private Map<String, User> inviteesMap = new HashMap<>();

    public ContactsCompletionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View getViewForObject(User user) {

        LayoutInflater l = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        TextView view = (TextView) l.inflate(R.layout.contact_token, (ViewGroup) getParent(), false);
        view.setText(user.getFullName()); // what gets displayed in field after selection

        return view;
    }

    @Override
    protected User defaultObject(String completionText) {
        //Stupid simple example of guessing if we have an email or not
        int index = completionText.indexOf('@');
        if (index == -1) {
            return new User(null, completionText, completionText.replace(" ", "") + "@example.com", null);
        } else {
            return new User(null, completionText.substring(0, index), completionText, null);
        }
    }

    public Map<String, User> getInviteesMap() {
        return inviteesMap;
    }

    @Override
    public void onTokenAdded(Object token) {
        User user = (User) token;
        inviteesMap.put(user.getUserId(), user);

        System.out.println("User added: " + user);
    }

    @Override
    public void onTokenRemoved(Object token) {
        User user = (User) token;
        inviteesMap.remove(user.getUserId());

        System.out.println("User removed: " + user);
    }
}