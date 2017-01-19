package androidapp.social.jj.letshangout.dto;

import java.io.Serializable;

/**
 * Created by Jason on 1/19/2017.
 */

public class User implements Serializable {

    private String userId; // pk
    private String fullName;
    private String email;
    private String password;


    public User() {
    }

    public User(String userId, String fullName, String email, String password) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() { return fullName; }
}
