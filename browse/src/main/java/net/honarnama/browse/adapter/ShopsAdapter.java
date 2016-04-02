package net.honarnama.browse.adapter;

import com.parse.GetDataCallback;
import com.parse.ImageSelector;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.honarnama.browse.R;
import net.honarnama.browse.model.Item;
import net.honarnama.browse.model.Shop;
import net.honarnama.core.model.City;
import net.honarnama.core.model.Event;
import net.honarnama.core.model.Store;
import net.honarnama.core.utils.TextUtil;

import android.content.Context;
import android.net.Uri;
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
    public Store getItem(int position) {
        return mShops.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolderWithImage mViewHolderWithImage;

        final Store store = mShops.get(position);

        if (convertView == null || !(convertView.getTag() instanceof ViewHolderWithImage)) {
            convertView = View.inflate(mContext, R.layout.shop_row, null);
            mViewHolderWithImage = new ViewHolderWithImage(convertView);
            convertView.setTag(mViewHolderWithImage);
        } else {
            mViewHolderWithImage = (ViewHolderWithImage) convertView.getTag();
        }

        mViewHolderWithImage.title.setText(TextUtil.convertEnNumberToFa(store.getName()));
        mViewHolderWithImage.desc.setText(TextUtil.convertEnNumberToFa(store.getDescription()));
        mViewHolderWithImage.shopPlace.setText(store.getCity().getName());
        //TODO load image

//        ParseFile image = store.getParseFile(Shop.LOGO);
//        mViewHolderWithImage.shopLogoLoadingPanel.setVisibility(View.VISIBLE);
//        if (image != null) {
//            Uri imageUri = Uri.parse(image.getUrl());
//            Picasso.with(mContext).load(imageUri.toString())
//                    .error(R.drawable.default_logo_hand)
//                    .into(mViewHolderWithImage.icon, new Callback() {
//                        @Override
//                        public void onSuccess() {
//                            mViewHolderWithImage.shopLogoLoadingPanel.setVisibility(View.GONE);
//                        }
//
//                        @Override
//                        public void onError() {
//                            mViewHolderWithImage.shopLogoLoadingPanel.setVisibility(View.GONE);
//                        }
//                    });
//        } else {
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
//        }
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

//    private class ViewHolderWithoutImage {
//        TextView title;
//        TextView desc;
//        TextView shopPlace;
//
//        public ViewHolderWithoutImage(View view) {
//            title = (TextView) view.findViewById(R.id.shop_title_in_list);
//            desc = (TextView) view.findViewById(R.id.shop_desc_in_list);
//            shopPlace = (TextView) view.findViewById(R.id.shop_place_text_view);
//
//        }
//    }
}
