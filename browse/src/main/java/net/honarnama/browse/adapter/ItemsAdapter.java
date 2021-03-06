package net.honarnama.browse.adapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.mikepenz.iconics.view.IconicsImageView;
import com.parse.ImageSelector;

import net.honarnama.base.model.ArtCategory;
import net.honarnama.base.utils.TextUtil;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.fragment.HonarnamaBrowseFragment;
import net.honarnama.nano.ArtCategoryCriteria;
import net.honarnama.nano.Item;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by elnaz on 2/15/16.
 */
public class ItemsAdapter extends BaseAdapter {
    public final static String DEBUG_TAG = HonarnamaBrowseApp.PRODUCTION_TAG + "/ItemsAdapter";
    Context mContext;
    HonarnamaBrowseFragment mFragment;
    List<Item> mItems;
    private static LayoutInflater mInflater = null;
    private boolean mIsForBookmarks = false;
    private View.OnClickListener onDeleteBookmarkListener;

    public ItemsAdapter(Context context, HonarnamaBrowseFragment fragment) {
        mContext = context;
        mItems = new ArrayList<Item>();
        mInflater = LayoutInflater.from(mContext);
        mFragment = fragment;
    }

    public void setForBookmarks(boolean isForBookmarks) {
        mIsForBookmarks = isForBookmarks;
    }

    @Override
    public int getCount() {
        if (mItems != null) {
            return mItems.size();
        }
        return 0;
    }

    @Override
    public Item getItem(int position) {
        if (mItems != null) {
            return mItems.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {

        if (getItem(position) != null) {
            return getItem(position).id;
        }
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final MyViewHolder mViewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_row, null);
            mViewHolder = new MyViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        if (mItems == null) {
            return convertView;
        }

        final Item item = mItems.get(position);
        // Setting all values in listview
        mViewHolder.title.setText(TextUtil.convertEnNumberToFa(item.name));
        mViewHolder.desc.setText(TextUtil.convertEnNumberToFa(item.description));

        NumberFormat formatter = TextUtil.getPriceNumberFormmat(Locale.ENGLISH);
        String formattedPrice = formatter.format(item.price);
        String price = TextUtil.convertEnNumberToFa(formattedPrice);


        if (mIsForBookmarks) {
            mViewHolder.deleteBookmark.setVisibility(View.VISIBLE);
            mViewHolder.deleteBookmark.setTag(position);
            mViewHolder.deleteBookmark.setOnClickListener(this.onDeleteBookmarkListener);
            mViewHolder.price.setVisibility(View.GONE);
        } else {
            mViewHolder.price.setVisibility(View.VISIBLE);
            mViewHolder.price.setText(price + " " + mContext.getString(R.string.toman));
        }
        ArtCategoryCriteria categoryCriteria = item.artCategoryCriteria;
        new ArtCategory().getCategoryNameById(categoryCriteria.level1Id).continueWith(new Continuation<String, Object>() {
            @Override
            public Object then(Task<String> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(DEBUG_TAG, "Error getting art cat name.");
                    mViewHolder.itemCat.setText(mContext.getString(R.string.not_found));
                } else {
                    mViewHolder.itemCat.setText(task.getResult());
                }
                return null;
            }
        });


        String itemImage = "";
        if (item.images != null) {
            for (int i = 0; i < item.images.length; i++) {
                if (!TextUtils.isEmpty(item.images[i])) {
                    itemImage = item.images[i];
                    break;
                }
            }
        }

        mViewHolder.itemIconLoadingPanel.setVisibility(View.VISIBLE);
        if (itemImage.trim().length() > 0) {
            Uri imageUri = Uri.parse(itemImage);
//            Picasso.with(mContext).load(imageUri.toString())
//                    .error(R.drawable.camera_insta)
//                    .resize(120,120)
//                    .into(mViewHolder.icon, new Callback() {
//                        @Override
//                        public void onSuccess() {
//                            mViewHolder.itemIconLoadingPanel.setVisibility(View.GONE);
//                        }
//
//                        @Override
//                        public void onError() {
//                            mViewHolder.itemIconLoadingPanel.setVisibility(View.GONE);
//                        }
//                    });

            Glide.with(mFragment).load(imageUri.toString())
                    .centerCrop()
//                    .crossFade()
                    .error(R.drawable.camera_insta)
                    .into(new GlideDrawableImageViewTarget(mViewHolder.icon) {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                            super.onResourceReady(resource, animation);
                            mViewHolder.itemIconLoadingPanel.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            super.onLoadFailed(e, errorDrawable);
                            mViewHolder.itemIconLoadingPanel.setVisibility(View.GONE);
                        }
                    });

        } else {
            Glide.with(mFragment).load(R.drawable.camera_insta)
                    .centerCrop()
                    .error(R.drawable.camera_insta)
                    .into(new GlideDrawableImageViewTarget(mViewHolder.icon) {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                            super.onResourceReady(resource, animation);
                            mViewHolder.itemIconLoadingPanel.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            super.onLoadFailed(e, errorDrawable);
                            mViewHolder.itemIconLoadingPanel.setVisibility(View.GONE);
                        }
                    });
        }
        return convertView;

    }


    public void setItems(ArrayList<Item> itemList) {
        mItems = itemList;
    }

    public void addItems(ArrayList<Item> itemList) {
        if (mItems != null) {
            mItems.addAll(itemList);
        }
    }

    public void removeItem(int position) {
        mItems.remove(position);
    }

    private class MyViewHolder {
        TextView title;
        TextView desc;
        TextView itemCat;
        TextView price;
        ImageSelector icon;
        RelativeLayout itemRowContainer;
        RelativeLayout itemIconLoadingPanel;
        IconicsImageView deleteBookmark;


        public MyViewHolder(View view) {
            title = (TextView) view.findViewById(R.id.item_row_title);
            desc = (TextView) view.findViewById(R.id.item_row_desc);
            price = (TextView) view.findViewById(R.id.price);
            icon = (ImageSelector) view.findViewById(R.id.item_image_in_list);
            itemRowContainer = (RelativeLayout) view.findViewById(R.id.item_row_outer_container);
            itemIconLoadingPanel = (RelativeLayout) view.findViewById(R.id.item_icon_loading_panel);
            itemCat = (TextView) view.findViewById(R.id.item_row_cat);
            deleteBookmark = (IconicsImageView) view.findViewById(R.id.delete_bookmark);
        }
    }

    public void setOnDeleteBookmarkListener(final View.OnClickListener onClickListener) {
        this.onDeleteBookmarkListener = onClickListener;
    }

}
