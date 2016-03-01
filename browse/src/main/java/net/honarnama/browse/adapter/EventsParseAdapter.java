package net.honarnama.browse.adapter;

import com.parse.GetDataCallback;
import com.parse.ImageSelector;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import net.honarnama.browse.R;
import net.honarnama.core.model.City;
import net.honarnama.core.model.Event;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by elnaz on 2/29/16.
 */
public class EventsParseAdapter extends ParseQueryAdapter {

    public EventsParseAdapter(Context context) {
        // Use the QueryFactory to construct a PQA that will only show
        // Todos marked as high-pri

        super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery create() {
                ParseQuery<Event> parseQuery = new ParseQuery<Event>(Event.class);
                parseQuery.whereEqualTo(Event.STATUS, Event.STATUS_CODE_VERIFIED);
                parseQuery.whereEqualTo(Event.VALIDITY_CHECKED, true);
                parseQuery.include(Event.CITY);
                return parseQuery;
            }
        });
    }

    // Customize the layout by overriding getItemView
    @Override
    public View getItemView(ParseObject object, View convertView, ViewGroup parent) {

        Event event = (Event) object;
        final ViewHolderWithoutImage mViewHolderWithoutImage;
        final ViewHolderWithImage mViewHolderWithImage;

        ParseFile eventBanner = event.getParseFile(Event.BANNER);
        if (eventBanner == null) {
            if (convertView == null || !(convertView.getTag() instanceof ViewHolderWithoutImage)) {
                convertView = View.inflate(getContext(), R.layout.event_row, null);
                mViewHolderWithoutImage = new ViewHolderWithoutImage(convertView);
                convertView.setTag(mViewHolderWithoutImage);
            } else {
                mViewHolderWithoutImage = (ViewHolderWithoutImage) convertView.getTag();
            }

            super.getItemView(event, convertView, parent);

            mViewHolderWithoutImage.title.setText(event.getName());
            mViewHolderWithoutImage.desc.setText(event.getDescription());
            mViewHolderWithoutImage.place.setText(event.getParseObject(Event.CITY).getString(City.NAME));

        } else {
            if (convertView == null || !(convertView.getTag() instanceof ViewHolderWithImage)) {
                convertView = View.inflate(getContext(), R.layout.event_row, null);
                mViewHolderWithImage = new ViewHolderWithImage(convertView);
                convertView.setTag(mViewHolderWithImage);
            } else {
                mViewHolderWithImage = (ViewHolderWithImage) convertView.getTag();
            }

            super.getItemView(event, convertView, parent);

            mViewHolderWithImage.title.setText(event.getName());
            mViewHolderWithImage.desc.setText(event.getDescription());
            mViewHolderWithImage.place.setText(event.getParseObject(Event.CITY).getString(City.NAME));

            mViewHolderWithImage.imageLoadingPanel.setVisibility(View.VISIBLE);
            mViewHolderWithImage.icon.setVisibility(View.GONE);
            mViewHolderWithImage.icon.loadInBackground(event.getParseFile(Event.BANNER), new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    mViewHolderWithImage.imageLoadingPanel.setVisibility(View.GONE);
                    mViewHolderWithImage.icon.setVisibility(View.VISIBLE);
                }
            });
        }

        return convertView;
    }

    private class ViewHolderWithImage {
        TextView title;
        TextView desc;
        ImageSelector icon;
        RelativeLayout imageLoadingPanel;
        TextView place;

        public ViewHolderWithImage(View view) {
            title = (TextView) view.findViewById(R.id.event_title_in_list);
            desc = (TextView) view.findViewById(R.id.event_desc_in_list);
            icon = (ImageSelector) view.findViewById(R.id.event_image_in_list);
            imageLoadingPanel = (RelativeLayout) view.findViewById(R.id.event_image_loading_panel);
            place = (TextView) view.findViewById(R.id.event_place_text_view);

        }
    }

    private class ViewHolderWithoutImage {
        TextView title;
        TextView desc;
        RelativeLayout eventRowContainer;
        TextView place;

        public ViewHolderWithoutImage(View view) {
            title = (TextView) view.findViewById(R.id.event_title_in_list);
            desc = (TextView) view.findViewById(R.id.event_desc_in_list);
            eventRowContainer = (RelativeLayout) view.findViewById(R.id.event_row_container);
            place = (TextView) view.findViewById(R.id.event_place_text_view);

        }
    }
}
