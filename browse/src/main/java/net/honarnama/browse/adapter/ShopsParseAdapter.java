package net.honarnama.browse.adapter;

import com.parse.GetDataCallback;
import com.parse.ImageSelector;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import net.honarnama.browse.R;
import net.honarnama.browse.model.Shop;
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
        final MyViewHolder mViewHolder;

        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.shop_row, null);
            mViewHolder = new MyViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        super.getItemView(shop, convertView, parent);

        mViewHolder.title.setText(shop.getString("name"));
        mViewHolder.desc.setText(shop.getString("description"));
        mViewHolder.shopPlace.setText(shop.getParseObject(Store.CITY).getString("name"));

        mViewHolder.shopLogoLoadingPanel.setVisibility(View.VISIBLE);
        mViewHolder.icon.setVisibility(View.GONE);
        mViewHolder.icon.loadInBackground(shop.getParseFile(Shop.LOGO), new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                mViewHolder.shopLogoLoadingPanel.setVisibility(View.GONE);
                mViewHolder.icon.setVisibility(View.VISIBLE);
            }
        });
        return convertView;
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
