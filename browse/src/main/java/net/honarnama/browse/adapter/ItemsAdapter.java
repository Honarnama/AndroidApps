package net.honarnama.browse.adapter;

import com.mikepenz.iconics.view.IconicsImageView;
import com.parse.ImageSelector;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.honarnama.base.BuildConfig;
import net.honarnama.base.model.ArtCategory;
import net.honarnama.base.utils.TextUtil;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.nano.ArtCategoryCriteria;
import net.honarnama.nano.Item;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by elnaz on 2/15/16.
 */
public class ItemsAdapter extends BaseAdapter {
    public final static String DEBUG_TAG = HonarnamaBrowseApp.PRODUCTION_TAG + "/ItemsAdapter";
    Context mContext;
    List<Item> mItems;
    private static LayoutInflater mInflater = null;
    private boolean mIsForBookmarks = false;
    private View.OnClickListener onDeleteBookmarkListener;

    public ItemsAdapter(Context context) {
        mContext = context;
        mItems = new ArrayList<Item>();
        mInflater = LayoutInflater.from(mContext);
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
            convertView = mInflater.inflate(R.layout.item_row, parent, false);
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

        if (mIsForBookmarks) {
            mViewHolder.deleteBookmark.setVisibility(View.VISIBLE);
            mViewHolder.deleteBookmark.setTag(position);
            mViewHolder.deleteBookmark.setOnClickListener(this.onDeleteBookmarkListener);
        }

        ArtCategoryCriteria categoryCriteria = item.artCategoryCriteria;
        new ArtCategory().getCategoryNameById(categoryCriteria.level1Id).continueWith(new Continuation<String, Object>() {
            @Override
            public Object then(Task<String> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(DEBUG_TAG, "Error getting art cat name.");
                } else {
                    mViewHolder.itemCat.setText(task.getResult());
                }
                return null;
            }
        });


        String image = item.images[0];
        mViewHolder.itemIconLoadingPanel.setVisibility(View.VISIBLE);

        if (image.trim().length() > 0) {
            Uri imageUri = Uri.parse(image);
            Picasso.with(mContext).load(imageUri.toString())
                    .error(R.drawable.camera_insta)
                    .into(mViewHolder.icon, new Callback() {
                        @Override
                        public void onSuccess() {
                            mViewHolder.itemIconLoadingPanel.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            mViewHolder.itemIconLoadingPanel.setVisibility(View.GONE);
                        }
                    });
        } else {
            Picasso.with(mContext).load(R.drawable.camera_insta)
                    .error(R.drawable.camera_insta)
                    .into(mViewHolder.icon, new Callback() {
                        @Override
                        public void onSuccess() {
                            mViewHolder.itemIconLoadingPanel.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
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
        mItems.addAll(itemList);
    }

    public void removeItem(int position) {
        mItems.remove(position);
    }

    private class MyViewHolder {
        TextView title;
        TextView desc;
        TextView itemCat;
        ImageSelector icon;
        RelativeLayout itemRowContainer;
        RelativeLayout itemIconLoadingPanel;
        IconicsImageView deleteBookmark;


        public MyViewHolder(View view) {
            title = (TextView) view.findViewById(R.id.item_row_title);
            desc = (TextView) view.findViewById(R.id.item_row_desc);
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
