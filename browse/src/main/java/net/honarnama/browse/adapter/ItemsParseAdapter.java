package net.honarnama.browse.adapter;

import com.parse.GetDataCallback;
import com.parse.ImageSelector;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.squareup.picasso.Picasso;

import net.honarnama.browse.R;
import net.honarnama.browse.model.Item;
import net.honarnama.core.model.Category;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import bolts.TaskCompletionSource;

/**
 * Created by elnaz on 2/23/16.
 */
public class ItemsParseAdapter extends ParseQueryAdapter {

    public Context mContext;

    public ItemsParseAdapter(Context context, QueryFactory<ParseObject> queryFactory) {
        // Use the QueryFactory to construct a PQA that will only show
        // Todos marked as high-pri
        super(context, queryFactory);
        mContext = context;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    // Customize the layout by overriding getItemView
    @Override
    public View getItemView(ParseObject object, View convertView, ViewGroup parent) {

        Item item = (Item) object;
        final MyViewHolder mViewHolder;

        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.item_row, null);
            mViewHolder = new MyViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        super.getItemView(item, convertView, parent);

        ParseFile image = item.getParseFile(Item.IMAGE_1);
//        if (image != null) {
//            mViewHolder.itemIconLoadingPanel.setVisibility(View.VISIBLE);
//            mViewHolder.icon.setVisibility(View.GONE);
//            mViewHolder.icon.loadInBackground(image, new GetDataCallback() {
//                @Override
//                public void done(byte[] data, ParseException e) {
//                    mViewHolder.itemIconLoadingPanel.setVisibility(View.GONE);
//                    mViewHolder.icon.setVisibility(View.VISIBLE);
//                }
//            });
//        } else {
//            mViewHolder.icon.setImageResource(android.R.color.transparent);
//        }


//        if (image != null) {
//            image.getDataInBackground(new GetDataCallback() {
//
//                @Override
//                public void done(byte[] data, ParseException e) {
//                    mViewHolder.itemIconLoadingPanel.setVisibility(View.GONE);
//                    mViewHolder.icon.setVisibility(View.VISIBLE);
//                    if (data != null && e == null) {
//                        Bitmap bitmap = BitmapFactory
//                                .decodeByteArray(data, 0,
//                                        data.length);
//                        mViewHolder.icon.setImageBitmap(bitmap);
//                    } else {
//                        mViewHolder.icon.setImageResource(android.R.color.transparent);
//                    }
//                }
//            });
//        } else {
//            Log.e("inja", "aks nadarad");
//            mViewHolder.icon.setImageResource(android.R.color.transparent);
//        }

        if (image != null) {
            Uri imageUri = Uri.parse(image.getUrl());
            Picasso.with(mContext).load(imageUri.toString()).into(mViewHolder.icon);
        } else {
            mViewHolder.icon.setImageResource(android.R.color.transparent);
        }

        // Setting all values in listview
        mViewHolder.title.setText(item.getName());
        mViewHolder.desc.setText(item.getDescription());

        Category category = item.getCategory();
        mViewHolder.itemCat.setText(category.getName());

        return convertView;
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
