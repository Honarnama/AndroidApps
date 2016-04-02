package net.honarnama.browse.adapter;

import com.parse.ImageSelector;
import com.parse.ParseFile;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.model.Item;
import net.honarnama.core.model.ArtCategory;
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
 * Created by elnaz on 2/15/16.
 */
public class ItemsAdapter extends BaseAdapter {
    public final static String DEBUG_TAG = HonarnamaBrowseApp.PRODUCTION_TAG + "/Adapter";
    Context mContext;
    List<Item> mItems;
    private static LayoutInflater mInflater = null;

    public ItemsAdapter(Context context) {
        mContext = context;
        mItems = new ArrayList<Item>();
        mInflater = LayoutInflater.from(mContext);
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
        mViewHolder.title.setText(TextUtil.convertEnNumberToFa(item.getName()));
        mViewHolder.desc.setText(TextUtil.convertEnNumberToFa(item.getDescription()));

        ArtCategory category = item.getCategory();
        mViewHolder.itemCat.setText(category.getName());

        //TODO load image
//        ParseFile image = item.getParseFile(Item.IMAGE_1);
//        mViewHolder.itemIconLoadingPanel.setVisibility(View.VISIBLE);
//
//        if (image != null) {
//            Uri imageUri = Uri.parse(image.getUrl());
//            Picasso.with(mContext).load(imageUri.toString())
//                    .error(R.drawable.camera_insta)
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
//        } else {
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
//        }
        return convertView;

    }


    public void setItems(List<Item> itemList) {
        mItems = itemList;
    }

    private class MyViewHolder {
        TextView title;
        TextView desc;
        TextView itemCat;
        ImageSelector icon;
        RelativeLayout itemRowContainer;
        RelativeLayout itemIconLoadingPanel;


        public MyViewHolder(View view) {
            title = (TextView) view.findViewById(R.id.item_row_title);
            desc = (TextView) view.findViewById(R.id.item_row_desc);
            icon = (ImageSelector) view.findViewById(R.id.item_image_in_list);
            itemRowContainer = (RelativeLayout) view.findViewById(R.id.item_row_outer_container);
            itemIconLoadingPanel = (RelativeLayout) view.findViewById(R.id.item_icon_loading_panel);
            itemCat = (TextView) view.findViewById(R.id.item_row_cat);
        }
    }

}
