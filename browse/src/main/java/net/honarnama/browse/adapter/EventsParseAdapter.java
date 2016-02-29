package net.honarnama.browse.adapter;

import com.parse.GetDataCallback;
import com.parse.ImageSelector;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import net.honarnama.browse.R;
import net.honarnama.core.model.City;
import net.honarnama.core.model.Event;

import android.content.Context;
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
        final MyViewHolder mViewHolder;

        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.event_row, null);
            mViewHolder = new MyViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        super.getItemView(event, convertView, parent);

        mViewHolder.title.setText(event.getString(Event.NAME));
        mViewHolder.desc.setText(event.getString(Event.DESCRIPTION));
        mViewHolder.place.setText(event.getParseObject(Event.CITY).getString(City.NAME));

        mViewHolder.imageLoadingPanel.setVisibility(View.VISIBLE);
        mViewHolder.icon.setVisibility(View.GONE);
        mViewHolder.icon.loadInBackground(event.getParseFile(Event.BANNER), new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                mViewHolder.imageLoadingPanel.setVisibility(View.GONE);
                mViewHolder.icon.setVisibility(View.VISIBLE);
            }
        });
        return convertView;
    }

    private class MyViewHolder {
        TextView title;
        TextView desc;
        ImageSelector icon;
        RelativeLayout eventRowContainer;
        RelativeLayout imageLoadingPanel;
        TextView place;

        public MyViewHolder(View view) {
            title = (TextView) view.findViewById(R.id.event_title_in_list);
            desc = (TextView) view.findViewById(R.id.event_desc_in_list);
            icon = (ImageSelector) view.findViewById(R.id.event_image_in_list);
            eventRowContainer = (RelativeLayout) view.findViewById(R.id.event_row_container);
            imageLoadingPanel = (RelativeLayout) view.findViewById(R.id.event_image_loading_panel);
            place = (TextView) view.findViewById(R.id.event_place_text_view);

        }
    }

}
