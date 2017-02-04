package androidapp.social.jj.letshangout.dto;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jason on 1/20/2017.
 */

public class Place implements Serializable{

    private String placeId; // pk
    private String invitationId;
    private String name;
    private String googlePlaceId;
    private int voteCount;


    public Place() {
    }

    public Place(String placeId, String invitationId, String name, String googlePlaceId, int voteCount) {
        this.placeId = placeId;
        this.invitationId = invitationId;
        this.name = name;
        this.googlePlaceId = googlePlaceId;
        this.voteCount = voteCount;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("placeId", placeId);
        result.put("invitationId", invitationId);
        result.put("name", name);
        result.put("googlePlaceId", googlePlaceId);
        result.put("voteCount", voteCount);

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Place place = (Place) o;

        return placeId.equals(place.placeId);

    }

    @Override
    public int hashCode() {
        return placeId.hashCode();
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getInvitationId() {
        return invitationId;
    }

    public void setInvitationId(String invitationId) {
        this.invitationId = invitationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGooglePlaceId() {
        return googlePlaceId;
    }

    public void setGooglePlaceId(String googlePlaceId) {
        this.googlePlaceId = googlePlaceId;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }
}
