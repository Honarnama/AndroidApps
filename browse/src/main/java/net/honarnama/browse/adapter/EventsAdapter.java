package net.honarnama.browse.adapter;

import com.parse.GetDataCallback;
import com.parse.ImageSelector;
import com.parse.ParseException;

import net.honarnama.browse.R;
import net.honarnama.browse.model.Shop;
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
        final MyViewHolder mViewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.event_row, parent, false);
            mViewHolder = new MyViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        final Event event = mEvents.get(position);
        // Setting all values in listview
        mViewHolder.title.setText(event.getName());
        mViewHolder.desc.setText(event.getDescription());
        mViewHolder.place.setText(event.getParseObject(Event.CITY).getString("name"));

        mViewHolder.imageLoadingPanel.setVisibility(View.VISIBLE);
        mViewHolder.image.setVisibility(View.GONE);
        mViewHolder.image.loadInBackground(event.getParseFile(Shop.LOGO), new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                mViewHolder.imageLoadingPanel.setVisibility(View.GONE);
                mViewHolder.image.setVisibility(View.VISIBLE);
            }
        });
        return convertView;

    }

    public void setEvents(List<Event> eventList) {
        mEvents = eventList;
    }

    private class MyViewHolder {
        TextView title;
        TextView desc;
        ImageSelector image;
        RelativeLayout imageLoadingPanel;
        TextView place;

        public MyViewHolder(View view) {
            title = (TextView) view.findViewById(R.id.event_title_in_list);
            desc = (TextView) view.findViewById(R.id.event_desc_in_list);
            image = (ImageSelector) view.findViewById(R.id.event_image_in_list);
            imageLoadingPanel = (RelativeLayout) view.findViewById(R.id.event_image_loading_panel);
            place = (TextView) view.findViewById(R.id.event_place_text_view);

        }
    }
}
