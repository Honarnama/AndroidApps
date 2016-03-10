package net.honarnama.browse.adapter;

import com.parse.GetDataCallback;
import com.parse.ImageSelector;
import com.parse.ParseException;
import com.parse.ParseFile;

import net.honarnama.browse.R;
import net.honarnama.browse.model.Shop;
import net.honarnama.core.model.City;
import net.honarnama.core.model.Event;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elnaz on 2/13/16.
 */
public class EventsAdapter extends BaseAdapter {

    Context mContext;
    List<Event> mEvents;
    private static LayoutInflater mInflater = null;

    public EventsAdapter(Context context) {
        mContext = context;
        mEvents = new ArrayList<Event>();
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mEvents.size();
    }

    @Override
    public Object getItem(int position) {
        return mEvents.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Event event = mEvents.get(position);
        final ViewHolderWithoutImage mViewHolderWithoutImage;
        final ViewHolderWithImage mViewHolderWithImage;

        ParseFile eventBanner = event.getParseFile(Event.BANNER);
        if (eventBanner == null) {
            if (convertView == null || !(convertView.getTag() instanceof ViewHolderWithoutImage)) {
                convertView = View.inflate(mContext, R.layout.event_row, null);
                mViewHolderWithoutImage = new ViewHolderWithoutImage(convertView);
                convertView.setTag(mViewHolderWithoutImage);
            } else {
                mViewHolderWithoutImage = (ViewHolderWithoutImage) convertView.getTag();
            }

            mViewHolderWithoutImage.title.setText(event.getName());
            mViewHolderWithoutImage.desc.setText(event.getDescription());
            mViewHolderWithoutImage.place.setText(event.getParseObject(Event.CITY).getString(City.NAME));

        } else {
            if (convertView == null || !(convertView.getTag() instanceof ViewHolderWithImage)) {
                convertView = View.inflate(mContext, R.layout.event_row, null);
                mViewHolderWithImage = new ViewHolderWithImage(convertView);
                convertView.setTag(mViewHolderWithImage);
            } else {
                mViewHolderWithImage = (ViewHolderWithImage) convertView.getTag();
            }

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

    public void setEvents(List<Event> eventList) {
        mEvents = eventList;
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