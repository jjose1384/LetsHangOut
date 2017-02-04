package androidapp.social.jj.letshangout.dto;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jason on 2/3/2017.
 */

public class InvitationResponse implements Serializable {

    private String invitationId; // pk - composite
    private String userId;       // pk - composite
    private String going;       // check Constants.goingOptions - YES, NO, NOT_RESPONDED
    private String vote;        // placeId;

    public InvitationResponse() {
    }

    public InvitationResponse(String invitationId, String userId, String going, String vote) {
        this.invitationId = invitationId;
        this.userId = userId;
        this.going = going;
        this.vote = vote;
    }

    @Exclude
    public Map<String, Object> toMap()
    {
        HashMap<String, Object> result = new HashMap<>();
        result.put("invitationId", invitationId);
        result.put("userId", userId);
        result.put("going", going);
        result.put("vote", vote);

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InvitationResponse that = (InvitationResponse) o;

        if (!invitationId.equals(that.invitationId)) return false;
        return userId.equals(that.userId);

    }

    @Override
    public int hashCode() {
        int result = invitationId.hashCode();
        result = 31 * result + userId.hashCode();
        return result;
    }

    public String getInvitationId() {
        return invitationId;
    }

    public void setInvitationId(String invitationId) {
        this.invitationId = invitationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGoing() {
        return going;
    }

    public void setGoing(String going) {
        this.going = going;
    }

    public String getVote() {
        return vote;
    }

    public void setVote(String vote) {
        this.vote = vote;
    }
}
