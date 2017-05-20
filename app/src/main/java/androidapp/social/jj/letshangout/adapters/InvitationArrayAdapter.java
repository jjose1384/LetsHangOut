package androidapp.social.jj.letshangout.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import androidapp.social.jj.letshangout.R;
import androidapp.social.jj.letshangout.dto.Invitation;

/**
 * Created by Jason on 4/6/2017.
 * Resource: https://www.mkyong.com/android/android-listview-example/
 */

public class InvitationArrayAdapter extends ArrayAdapter<Invitation> {
    private final Context context;
    private final ArrayList<Invitation> values;

    public InvitationArrayAdapter(Context context, ArrayList<Invitation> values) {
        super(context, R.layout.list_item_invitation, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.list_item_invitation, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.textView_label);
        textView.setText(values.get(position).getWhat());

        // Change icon based on name
        String s = values.get(position).getWhat();

        System.out.println(s);

        return rowView;
    }

    public Invitation getItem(int position){
        return values.get(position);
    }
}
