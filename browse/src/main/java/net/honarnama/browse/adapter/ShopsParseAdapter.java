package net.honarnama.browse.adapter;

import com.parse.GetDataCallback;
import com.parse.ImageSelector;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import net.honarnama.browse.R;
import net.honarnama.browse.model.Shop;
import net.honarnama.core.model.City;
import net.honarnama.core.model.Store;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by elnaz on 2/23/16.
 */
public class ShopsParseAdapter extends ParseQueryAdapter {

    public ShopsParseAdapter(Context context) {
        // Use the QueryFactory to construct a PQA that will only show
        // Todos marked as high-pri

        super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery create() {
                ParseQuery<Store> parseQuery = new ParseQuery<Store>(Store.class);
                parseQuery.whereEqualTo(Store.STATUS, Store.STATUS_CODE_VERIFIED);
                parseQuery.whereEqualTo(Store.VALIDITY_CHECKED, true);
                parseQuery.include(Store.CITY);
                return parseQuery;
            }
        });
    }

    // Customize the layout by overriding getItemView
    @Override
    public View getItemView(ParseObject object, View convertView, ViewGroup parent) {

        Store shop = (Store) object;
        final ViewHolderWithoutImage mViewHolderWithoutImage;
        final ViewHolderWithImage mViewHolderWithImage;

        ParseFile shopLogo = shop.getParseFile(Shop.LOGO);

        if (shopLogo == null) {
            if (convertView == null || !(convertView.getTag() instanceof ViewHolderWithoutImage)) {
                convertView = View.inflate(getContext(), R.layout.shop_row, null);
                mViewHolderWithoutImage = new ViewHolderWithoutImage(convertView);
                convertView.setTag(mViewHolderWithoutImage);
            } else {
                mViewHolderWithoutImage = (ViewHolderWithoutImage) convertView.getTag();
            }

            super.getItemView(shop, convertView, parent);

            mViewHolderWithoutImage.title.setText(shop.getName());
            mViewHolderWithoutImage.desc.setText(shop.getDescription());
            mViewHolderWithoutImage.place.setText(shop.getParseObject(Shop.CITY).getString(City.NAME));

        } else {
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

            mViewHolderWithImage.shopLogoLoadingPanel.setVisibility(View.VISIBLE);
            mViewHolderWithImage.icon.setVisibility(View.GONE);
            mViewHolderWithImage.icon.loadInBackground(shop.getParseFile(Shop.LOGO), new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    mViewHolderWithImage.shopLogoLoadingPanel.setVisibility(View.GONE);
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

    private class ViewHolderWithoutImage {
        TextView title;
        TextView desc;
        TextView place;

        public ViewHolderWithoutImage(View view) {
            title = (TextView) view.findViewById(R.id.shop_title_in_list);
            desc = (TextView) view.findViewById(R.id.shop_desc_in_list);
            place = (TextView) view.findViewById(R.id.shop_place_text_view);

        }
    }

}
