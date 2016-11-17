package net.honarnama.browse.adapter;

import com.parse.ImageSelector;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.honarnama.base.model.City;
import net.honarnama.browse.R;
import net.honarnama.base.utils.TextUtil;
import net.honarnama.nano.Event;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
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
    public Event getItem(int position) {
        return mEvents.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Event event = mEvents.get(position);
        final ViewHolderWithImage mViewHolderWithImage;

//        ParseFile eventBanner = event.getParseFile(Event.BANNER);

        if (convertView == null || !(convertView.getTag() instanceof ViewHolderWithImage)) {
            convertView = View.inflate(mContext, R.layout.event_row, null);
            mViewHolderWithImage = new ViewHolderWithImage(convertView);
            convertView.setTag(mViewHolderWithImage);
        } else {
            mViewHolderWithImage = (ViewHolderWithImage) convertView.getTag();
        }

        mViewHolderWithImage.title.setText(TextUtil.convertEnNumberToFa(event.name));
        mViewHolderWithImage.desc.setText(TextUtil.convertEnNumberToFa(event.description));
        mViewHolderWithImage.place.setText(City.getCityById(event.locationCriteria.cityId).getName());

        String image = event.banner;
        mViewHolderWithImage.imageLoadingPanel.setVisibility(View.VISIBLE);

        if (image != null) {
            Uri imageUri = Uri.parse(image);
            Picasso.with(mContext).load(imageUri.toString())
                    .error(R.drawable.party_flags)
                    .into(mViewHolderWithImage.icon, new Callback() {
                        @Override
                        public void onSuccess() {
                            mViewHolderWithImage.imageLoadingPanel.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            mViewHolderWithImage.imageLoadingPanel.setVisibility(View.GONE);
                        }
                    });
        } else {
            Picasso.with(mContext).load(R.drawable.party_flags)
                    .error(R.drawable.party_flags)
                    .into(mViewHolderWithImage.icon, new Callback() {
                        @Override
                        public void onSuccess() {
                            mViewHolderWithImage.imageLoadingPanel.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            mViewHolderWithImage.imageLoadingPanel.setVisibility(View.GONE);
                        }
                    });
        }

//        mViewHolderWithImage.icon.setVisibility(View.GONE);
//        mViewHolderWithImage.icon.loadInBackground(event.getFile(Event.BANNER), new GetDataCallback() {
//            @Override
//            public void done(byte[] data, ParseException e) {
//                mViewHolderWithImage.imageLoadingPanel.setVisibility(View.GONE);
//                mViewHolderWithImage.icon.setVisibility(View.VISIBLE);
//            }
//        });
//

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

//    private class ViewHolderWithoutImage {
//        TextView title;
//        TextView desc;
//        RelativeLayout eventRowContainer;
//        TextView place;
//
//        public ViewHolderWithoutImage(View view) {
//            title = (TextView) view.findViewById(R.id.event_title_in_list);
//            desc = (TextView) view.findViewById(R.id.event_desc_in_list);
//            eventRowContainer = (RelativeLayout) view.findViewById(R.id.event_row_container);
//            place = (TextView) view.findViewById(R.id.event_place_text_view);
//
//        }
//    }
}
