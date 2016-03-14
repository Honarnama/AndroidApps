package net.honarnama.browse.adapter;

import com.parse.GetDataCallback;
import com.parse.ImageSelector;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.honarnama.browse.R;
import net.honarnama.browse.model.Item;
import net.honarnama.browse.model.Shop;
import net.honarnama.core.model.City;
import net.honarnama.core.model.Store;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by elnaz on 2/23/16.
 */
public class ShopsParseAdapter extends ParseQueryAdapter {
    public Context mContext;

    public ShopsParseAdapter(Context context, QueryFactory<ParseObject> queryFactory) {
        // Use the QueryFactory to construct a PQA that will only show
        // Todos marked as high-pri
        super(context, queryFactory);
        mContext = context;
    }

    // Customize the layout by overriding getItemView
    @Override
    public View getItemView(ParseObject object, View convertView, ViewGroup parent) {

        Store shop = (Store) object;
//        final ViewHolderWithoutImage mViewHolderWithoutImage;
        final ViewHolderWithImage mViewHolderWithImage;

        ParseFile shopLogo = shop.getParseFile(Shop.LOGO);


        if (convertView == null || !(convertView.getTag() instanceof ViewHolderWithImage)) {
            convertView = View.inflate(getContext(), R.layout.shop_row, null);
            mViewHolderWithImage = new ViewHolderWithImage(convertView);
            convertView.setTag(mViewHolderWithImage);
        } else {
            mViewHolderWithImage = (ViewHolderWithImage) convertView.getTag();
        }

        super.getItemView(shop, convertView, parent);

        mViewHolderWithImage.title.setText(shop.getName());
        mViewHolderWithImage.desc.setText(shop.getDescription());
        mViewHolderWithImage.place.setText(shop.getParseObject(Shop.CITY).getString(City.NAME));
//
//        mViewHolderWithImage.shopLogoLoadingPanel.setVisibility(View.VISIBLE);
//        mViewHolderWithImage.icon.setVisibility(View.GONE);
//        mViewHolderWithImage.icon.loadInBackground(shop.getParseFile(Shop.LOGO), new GetDataCallback() {
//            @Override
//            public void done(byte[] data, ParseException e) {
//                mViewHolderWithImage.shopLogoLoadingPanel.setVisibility(View.GONE);
//                mViewHolderWithImage.icon.setVisibility(View.VISIBLE);
//            }
//        });

        ParseFile image = shop.getParseFile(Shop.LOGO);
        mViewHolderWithImage.shopLogoLoadingPanel.setVisibility(View.VISIBLE);
        if (image != null) {
            Uri imageUri = Uri.parse(image.getUrl());
            Picasso.with(mContext).load(imageUri.toString())
                    .error(R.drawable.default_logo_hand)
                    .into(mViewHolderWithImage.icon, new Callback() {
                        @Override
                        public void onSuccess() {
                            mViewHolderWithImage.shopLogoLoadingPanel.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            mViewHolderWithImage.shopLogoLoadingPanel.setVisibility(View.GONE);
                        }
                    });
        } else {
            Picasso.with(mContext).load(R.drawable.default_logo_hand)
                    .error(R.drawable.default_logo_hand)
                    .into(mViewHolderWithImage.icon, new Callback() {
                        @Override
                        public void onSuccess() {
                            mViewHolderWithImage.shopLogoLoadingPanel.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            mViewHolderWithImage.shopLogoLoadingPanel.setVisibility(View.GONE);
                        }
                    });
        }


        return convertView;
    }

    private class ViewHolderWithImage {
        TextView title;
        TextView desc;
        ImageSelector icon;
        RelativeLayout shopLogoLoadingPanel;
        TextView place;

        public ViewHolderWithImage(View view) {
            title = (TextView) view.findViewById(R.id.shop_title_in_list);
            desc = (TextView) view.findViewById(R.id.shop_desc_in_list);
            icon = (ImageSelector) view.findViewById(R.id.shop_logo_in_list);
            shopLogoLoadingPanel = (RelativeLayout) view.findViewById(R.id.shop_logo_loading_panel);
            place = (TextView) view.findViewById(R.id.shop_place_text_view);

        }
    }

//    private class ViewHolderWithoutImage {
//        TextView title;
//        TextView desc;
//        TextView place;
//
//        public ViewHolderWithoutImage(View view) {
//            title = (TextView) view.findViewById(R.id.shop_title_in_list);
//            desc = (TextView) view.findViewById(R.id.shop_desc_in_list);
//            place = (TextView) view.findViewById(R.id.shop_place_text_view);
//
//        }
//    }

}
