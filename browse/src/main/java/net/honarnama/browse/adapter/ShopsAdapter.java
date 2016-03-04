package net.honarnama.browse.adapter;

import com.parse.GetDataCallback;
import com.parse.ImageSelector;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

import net.honarnama.browse.R;
import net.honarnama.browse.model.Item;
import net.honarnama.browse.model.Shop;
import net.honarnama.core.model.City;
import net.honarnama.core.model.Event;
import net.honarnama.core.model.Store;

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
public class ShopsAdapter extends BaseAdapter {

    Context mContext;
    List<Store> mShops;
    private static LayoutInflater mInflater = null;

    public ShopsAdapter(Context context) {
        mContext = context;
        mShops = new ArrayList<Store>();
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mShops.size();
    }

    @Override
    public Object getItem(int position) {
        return mShops.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolderWithoutImage mViewHolderWithoutImage;
        final ViewHolderWithImage mViewHolderWithImage;

        final ParseObject store = mShops.get(position);
        ParseFile shopLogo = store.getParseFile(Shop.LOGO);

        if (shopLogo == null) {
            if (convertView == null || !(convertView.getTag() instanceof ViewHolderWithoutImage)) {
                convertView = View.inflate(mContext, R.layout.shop_row, null);
                mViewHolderWithoutImage = new ViewHolderWithoutImage(convertView);
                convertView.setTag(mViewHolderWithoutImage);
            } else {
                mViewHolderWithoutImage = (ViewHolderWithoutImage) convertView.getTag();
            }

            mViewHolderWithoutImage.title.setText(store.getString("name"));
            mViewHolderWithoutImage.desc.setText(store.getString("description"));
            mViewHolderWithoutImage.shopPlace.setText(store.getParseObject(Store.CITY).getString("name"));

        } else {
            if (convertView == null || !(convertView.getTag() instanceof ViewHolderWithImage)) {
                convertView = View.inflate(mContext, R.layout.shop_row, null);
                mViewHolderWithImage = new ViewHolderWithImage(convertView);
                convertView.setTag(mViewHolderWithImage);
            } else {
                mViewHolderWithImage = (ViewHolderWithImage) convertView.getTag();
            }

            mViewHolderWithImage.title.setText(store.getString("name"));
            mViewHolderWithImage.desc.setText(store.getString("description"));
            mViewHolderWithImage.shopPlace.setText(store.getParseObject(Store.CITY).getString("name"));

            mViewHolderWithImage.shopLogoLoadingPanel.setVisibility(View.VISIBLE);
            mViewHolderWithImage.icon.setVisibility(View.GONE);
            mViewHolderWithImage.icon.loadInBackground(shopLogo, new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    mViewHolderWithImage.shopLogoLoadingPanel.setVisibility(View.GONE);
                    mViewHolderWithImage.icon.setVisibility(View.VISIBLE);
                }
            });
        }

        return convertView;

    }

    public void setShops(List<Store> shopList) {
        mShops = shopList;
    }

    private class ViewHolderWithImage {
        TextView title;
        TextView desc;
        ImageSelector icon;
        RelativeLayout shopLogoLoadingPanel;
        TextView shopPlace;

        public ViewHolderWithImage(View view) {
            title = (TextView) view.findViewById(R.id.shop_title_in_list);
            desc = (TextView) view.findViewById(R.id.shop_desc_in_list);
            icon = (ImageSelector) view.findViewById(R.id.shop_logo_in_list);
            shopLogoLoadingPanel = (RelativeLayout) view.findViewById(R.id.shop_logo_loading_panel);
            shopPlace = (TextView) view.findViewById(R.id.shop_place_text_view);

        }
    }

    private class ViewHolderWithoutImage {
        TextView title;
        TextView desc;
        TextView shopPlace;

        public ViewHolderWithoutImage(View view) {
            title = (TextView) view.findViewById(R.id.shop_title_in_list);
            desc = (TextView) view.findViewById(R.id.shop_desc_in_list);
            shopPlace = (TextView) view.findViewById(R.id.shop_place_text_view);

        }
    }
}
