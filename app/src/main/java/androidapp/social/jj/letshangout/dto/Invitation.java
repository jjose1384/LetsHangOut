package androidapp.social.jj.letshangout.dto;

import java.io.Serializable;

/**
 * Created by Jason on 1/21/2017.
 */

public class Invitation implements Serializable {

    String invitationId; // pk
    String what; // what the event is
    Long when; // when the event is taking place
    boolean closed; // if the invitation is closed, invitees can't add to the where options
    Long invitationSent; // time when the invitation was sent
    String sender; // sender userId
    Long rsvpBy; // invitees need to respond by this time

    /*
        Default constructor required by Firebase
     */
    public Invitation() {
    }

    public Invitation(String invitationId, String what, Long when, boolean closed, Long invitationSent, String sender, Long rsvpBy) {
        this.invitationId = invitationId;
        this.what = what;
        this.when = when;
        this.closed = closed;
        this.invitationSent = invitationSent;
        this.sender = sender;
        this.rsvpBy = rsvpBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Invitation that = (Invitation) o;

        return invitationId.equals(that.invitationId);

    }

    @Override
    public int hashCode() {
        return invitationId.hashCode();
    }

    public String getInvitationId() {
        return invitationId;
    }

    public void setInvitationId(String invitationId) {
        this.invitationId = invitationId;
    }

    public String getWhat() {
        return what;
    }

    public void setWhat(String what) {
        this.what = what;
    }

    public Long getWhen() {
        return when;
    }

    public void setWhen(Long when) {
        this.when = when;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public Long getInvitationSent() {
        return invitationSent;
    }

    public void setInvitationSent(Long invitationSent) {
        this.invitationSent = invitationSent;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Long getRsvpBy() {
        return rsvpBy;
    }

    public void setRsvpBy(Long rsvpBy) {
        this.rsvpBy = rsvpBy;
    }
}
