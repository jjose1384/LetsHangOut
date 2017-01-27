package androidapp.social.jj.letshangout.dto;

import java.io.Serializable;

/**
 * Created by Jason on 1/20/2017.
 */

public class Place implements Serializable{

    String placeId; // pk
    String invitationId;
    String name;

    public Place() {
    }

    public Place(String placeId, String invitationId, String name) {
        this.placeId = placeId;
        this.invitationId = invitationId;
        this.name = name;
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
}
