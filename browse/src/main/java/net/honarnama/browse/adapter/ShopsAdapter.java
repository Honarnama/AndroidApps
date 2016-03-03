package net.honarnama.browse.adapter;

import com.parse.GetDataCallback;
import com.parse.ImageSelector;
import com.parse.ParseException;
import com.parse.ParseObject;

import net.honarnama.browse.R;
import net.honarnama.browse.model.Item;
import net.honarnama.browse.model.Shop;
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
        final MyViewHolder mViewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.shop_row, parent, false);
            mViewHolder = new MyViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        final ParseObject store = mShops.get(position);
        // Setting all values in listview
        mViewHolder.title.setText(store.getString("name"));
        mViewHolder.desc.setText(store.getString("description"));
        mViewHolder.shopPlace.setText(store.getParseObject(Store.CITY).getString("name"));

        mViewHolder.shopLogoLoadingPanel.setVisibility(View.VISIBLE);
        mViewHolder.icon.setVisibility(View.GONE);
        mViewHolder.icon.loadInBackground(store.getParseFile(Shop.LOGO), new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                mViewHolder.shopLogoLoadingPanel.setVisibility(View.GONE);
                mViewHolder.icon.setVisibility(View.VISIBLE);
            }
        });
        return convertView;

    }

    public void setShops(List<Store> shopList) {
        mShops = shopList;
    }

    private class MyViewHolder {
        TextView title;
        TextView desc;
        ImageSelector icon;
        RelativeLayout shopRowContainer;
        RelativeLayout shopLogoLoadingPanel;
        TextView shopPlace;

        public MyViewHolder(View view) {
            title = (TextView) view.findViewById(R.id.shop_title_in_list);
            desc = (TextView) view.findViewById(R.id.shop_desc_in_list);
            icon = (ImageSelector) view.findViewById(R.id.shop_logo_in_list);
            shopRowContainer = (RelativeLayout) view.findViewById(R.id.shop_row_container);
            shopLogoLoadingPanel = (RelativeLayout) view.findViewById(R.id.shop_logo_loading_panel);
            shopPlace = (TextView) view.findViewById(R.id.shop_place_text_view);

        }
    }
}
