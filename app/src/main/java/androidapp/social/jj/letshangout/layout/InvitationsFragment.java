package androidapp.social.jj.letshangout.layout;

/**
 * Created by Jason on 1/12/2017.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidapp.social.jj.letshangout.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class InvitationsFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NAME = "section_number";

    public InvitationsFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static InvitationsFragment newInstance(String sectionName) {
        InvitationsFragment fragment = new InvitationsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SECTION_NAME, sectionName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_invitations, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        textView.setText(getString(R.string.section_format, getArguments().getString(ARG_SECTION_NAME)));
        return rootView;
    }
}
