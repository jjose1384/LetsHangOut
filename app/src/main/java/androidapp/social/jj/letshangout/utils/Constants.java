package androidapp.social.jj.letshangout.utils;

import java.text.SimpleDateFormat;

/**
 * Created by Jason on 1/7/2017.
 */

public class Constants {

    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, MMM dd yyyy 'at' hh:mm aaa");
    public static final String placeIdPlaceHolderPrefix = "-random-";

    public enum goingOptions {
        YES,
        NO,
        NOT_RESPONDED;
    }

    public enum invitationListOptions {
        sentInvitations,
        waitingInvitations,
        acceptedInvitations,
        declinedInvitations;
    }
}
