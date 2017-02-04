package androidapp.social.jj.letshangout.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.style.CharacterStyle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.tokenautocomplete.TokenCompleteTextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidapp.social.jj.letshangout.R;

/**
 * Created by Jason on 1/19/2017.
 */

public class PlacesCompletionView extends TokenCompleteTextView<AutocompletePrediction>
        implements TokenCompleteTextView.TokenListener{

    private Map<String, AutocompletePrediction> placeMap = new HashMap<>();

    public PlacesCompletionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View getViewForObject(AutocompletePrediction place) {

        LayoutInflater l = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        TextView view = (TextView) l.inflate(R.layout.place_token, (ViewGroup) getParent(), false);
        view.setText(place.getPrimaryText(null)); // what gets displayed in field after selection

        return view;
    }

    @Override
    protected AutocompletePrediction defaultObject(String completionText) {
        return new DummyAutocompletePrediction(completionText);
    }

    public Map<String, AutocompletePrediction> getPlaceMap() {
        return placeMap;
    }

    @Override
    public void onTokenAdded(Object token) {
        AutocompletePrediction place = (AutocompletePrediction) token;
        placeMap.put(place.getPlaceId(), place);

        System.out.println("Place added: " + token);
    }

    @Override
    public void onTokenRemoved(Object token) {
        AutocompletePrediction place = (AutocompletePrediction) token;
        placeMap.remove(place.getPlaceId());

        System.out.println("Place removed: " + token);
    }

    /*
        returns an object with the text that was entered
    */
    private class DummyAutocompletePrediction
            implements AutocompletePrediction
    {
        String text;

        public DummyAutocompletePrediction(String text) {
            this.text = text;
        }

        @Override
        public CharSequence getFullText(@Nullable CharacterStyle characterStyle) {
            return text;
        }

        @Override
        public CharSequence getPrimaryText(@Nullable CharacterStyle characterStyle) {
            return text;
        }

        @Override
        public CharSequence getSecondaryText(@Nullable CharacterStyle characterStyle) {
            return text;
        }

        @Nullable
        @Override
        public String getPlaceId() {
            return Constants.placeIdPlaceHolderPrefix + text;
        } // for dummy object, the text will be returned as placeId in '-random-<text>' format

        @Nullable
        @Override
        public List<Integer> getPlaceTypes() {
            return null;
        }

        @Override
        public AutocompletePrediction freeze() {
            return null;
        }

        @Override
        public boolean isDataValid() {
            return false;
        }
    }
}