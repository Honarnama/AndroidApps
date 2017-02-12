package net.honarnama.browse.adapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.parse.ImageSelector;

import net.honarnama.base.BuildConfig;
import net.honarnama.base.model.City;
import net.honarnama.base.utils.TextUtil;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.nano.Store;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
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
    public final static String DEBUG_TAG = HonarnamaBrowseApp.PRODUCTION_TAG + "/ShopsAdapter";
    Context mContext;
    List<Store> mShops;
    private static LayoutInflater mInflater = null;

    public ShopsAdapter(Context context) {
        mContext = context;
        mShops = new ArrayList();
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        if (mShops != null) {
            return mShops.size();
        }
        return 0;
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
//            convertView = mInflater.inflate(R.layout.shop_row, parent, false);

            mViewHolderWithImage = new ViewHolderWithImage(convertView);
            convertView.setTag(mViewHolderWithImage);
        } else {
            mViewHolderWithImage = (ViewHolderWithImage) convertView.getTag();
        }

        mViewHolderWithImage.title.setText(TextUtil.convertEnNumberToFa(store.name));
        mViewHolderWithImage.desc.setText(TextUtil.convertEnNumberToFa(store.description));
        mViewHolderWithImage.shopPlace.setText(City.getCityById(store.locationCriteria.cityId).getName());

        String image = store.logo.trim();
        mViewHolderWithImage.shopLogoLoadingPanel.setVisibility(View.VISIBLE);

        if (image.trim().length() > 0) {
            if (BuildConfig.DEBUG) {
                Log.d(DEBUG_TAG, "shop image: " + image);
            }
            Glide.with(mContext).load(image)
                    .centerCrop()
//                    .crossFade()
                    .error(R.drawable.default_logo_hand)
                    .into(new GlideDrawableImageViewTarget(mViewHolderWithImage.icon) {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                            super.onResourceReady(resource, animation);
                            mViewHolderWithImage.shopLogoLoadingPanel.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            super.onLoadFailed(e, errorDrawable);
                            mViewHolderWithImage.shopLogoLoadingPanel.setVisibility(View.GONE);
                        }
                    });
        } else {
            Glide.with(mContext).load(R.drawable.default_logo_hand)
                    .centerCrop()
                    .error(R.drawable.default_logo_hand)
                    .into(new GlideDrawableImageViewTarget(mViewHolderWithImage.icon) {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                            super.onResourceReady(resource, animation);
                            mViewHolderWithImage.shopLogoLoadingPanel.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            super.onLoadFailed(e, errorDrawable);
                            mViewHolderWithImage.shopLogoLoadingPanel.setVisibility(View.GONE);
                        }
                    });
        }
        return convertView;
    }

    public void setShops(List<Store> shopList) {
        mShops = shopList;
    }

    public void addShops(List<Store> shopList) {
        if (mShops != null) {
            mShops.addAll(shopList);
        }
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
