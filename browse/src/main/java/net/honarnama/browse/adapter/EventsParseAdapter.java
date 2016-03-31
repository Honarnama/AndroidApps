package net.honarnama.browse.adapter;

import com.parse.ImageSelector;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.honarnama.browse.R;
import net.honarnama.core.model.City;
import net.honarnama.core.model.Event;
import net.honarnama.core.utils.TextUtil;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by elnaz on 2/29/16.
 */
public class EventsParseAdapter extends ParseQueryAdapter {

    public Context mContext;

    public EventsParseAdapter(Context context, QueryFactory<ParseObject> queryFactory) {
        // Use the QueryFactory to construct a PQA that will only show
        // Todos marked as high-pri

        super(context, queryFactory);
        mContext = context;
    }

    // Customize the layout by overriding getItemView
    @Override
    public View getItemView(ParseObject object, View convertView, ViewGroup parent) {

        Event event = (Event) object;
//        final ViewHolderWithoutImage mViewHolderWithoutImage;
        final ViewHolderWithImage mViewHolderWithImage;

        if (convertView == null || !(convertView.getTag() instanceof ViewHolderWithImage)) {
            convertView = View.inflate(getContext(), R.layout.event_row, null);
            mViewHolderWithImage = new ViewHolderWithImage(convertView);
            convertView.setTag(mViewHolderWithImage);
        } else {
            mViewHolderWithImage = (ViewHolderWithImage) convertView.getTag();
        }

        super.getItemView(event, convertView, parent);

        mViewHolderWithImage.title.setText(TextUtil.convertEnNumberToFa(event.getName()));
        mViewHolderWithImage.desc.setText(TextUtil.convertEnNumberToFa(event.getDescription()));
        mViewHolderWithImage.place.setText(event.getParseObject(Event.CITY).getString(City.NAME));

//        mViewHolderWithImage.imageLoadingPanel.setVisibility(View.VISIBLE);
//        mViewHolderWithImage.icon.setVisibility(View.GONE);
//        mViewHolderWithImage.icon.loadInBackground(event.getFile(Event.BANNER), new GetDataCallback() {
//            @Override
//            public void done(byte[] data, ParseException e) {
//                mViewHolderWithImage.imageLoadingPanel.setVisibility(View.GONE);
//                mViewHolderWithImage.icon.setVisibility(View.VISIBLE);
//            }
//        });


        ParseFile image = event.getParseFile(Event.BANNER);
        mViewHolderWithImage.imageLoadingPanel.setVisibility(View.VISIBLE);
        if (image != null) {
            Uri imageUri = Uri.parse(image.getUrl());
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
