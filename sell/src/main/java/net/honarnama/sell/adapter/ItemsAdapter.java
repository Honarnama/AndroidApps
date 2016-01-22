package net.honarnama.sell.adapter;

import com.parse.GetDataCallback;
import com.parse.ImageSelector;
import com.parse.ParseException;
import com.parse.ParseImageView;

import net.honarnama.sell.R;
import net.honarnama.sell.model.Item;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by reza on 11/5/15.
 */
public class ItemsAdapter extends BaseAdapter {

    Context mContext;
    List<Item> mItems;
    private static LayoutInflater mInflater = null;

    public ItemsAdapter(Context context, List<Item> items) {
        mContext = context;
        mItems = items;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        Log.e("Elnaz", "mItems.size()"+mItems.size() + "");
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
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null) {
            vi = mInflater.inflate(R.layout.item, null);
        }

        TextView title = (TextView) vi.findViewById(R.id.item_title_in_list); // title
        ImageSelector thumb_image = (ImageSelector) vi.findViewById(R.id.item_image_in_list); // thumb image

        Item item = mItems.get(position);
        // Setting all values in listview
        title.setText(item.getTitle());
        thumb_image.loadInBackground(item.getParseFile(Item.IMAGE_1), new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {

            }
        });
        return vi;

    }
}
