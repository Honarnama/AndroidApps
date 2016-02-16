package net.honarnama.browse.adapter;

import com.parse.GetDataCallback;
import com.parse.ImageSelector;
import com.parse.ParseException;

import net.honarnama.browse.R;
import net.honarnama.browse.model.Item;

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
 * Created by elnaz on 2/15/16.
 */
public class ShopItemsAdapter extends BaseAdapter {

    Context mContext;
    List<Item> mItems;
    private static LayoutInflater mInflater = null;

    public ShopItemsAdapter(Context context) {
        mContext = context;
        mItems = new ArrayList<Item>();
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
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

        final Item item = mItems.get(position);
        // Setting all values in listview
        mViewHolder.title.setText(item.getTitle());

        mViewHolder.itemIconLoadingPanel.setVisibility(View.VISIBLE);
        mViewHolder.icon.setVisibility(View.GONE);
        mViewHolder.icon.loadInBackground(item.getParseFile(Item.IMAGE_1), new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                mViewHolder.itemIconLoadingPanel.setVisibility(View.GONE);
                mViewHolder.icon.setVisibility(View.VISIBLE);
            }
        });
        return convertView;

    }

    public void addAll(List<Item> itemList) {
        mItems.addAll(itemList);
    }

    private class MyViewHolder {
        TextView title;
        ImageSelector icon;
        RelativeLayout itemRowContainer;
        RelativeLayout itemIconLoadingPanel;

        public MyViewHolder(View view) {
            title = (TextView) view.findViewById(R.id.item_title_in_list);
            icon = (ImageSelector) view.findViewById(R.id.item_image_in_list);
            itemRowContainer = (RelativeLayout) view.findViewById(R.id.item_row_outer_container);
            itemIconLoadingPanel = (RelativeLayout) view.findViewById(R.id.item_icon_loading_panel);
        }
    }

}
