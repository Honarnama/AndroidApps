package net.honarnama.base.adapter;

import net.honarnama.base.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class OptionListAdapter extends BaseAdapter {

    private final String[] mItems;

    private final LayoutInflater mInflater;

    public OptionListAdapter(Activity activity, String[] items) {
        mInflater = LayoutInflater.from(activity);
        mItems = items;
    }

    @Override
    public int getCount() {
        return mItems.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }



    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= getCount()) {
            return null;
        }

        LocViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.option_row, null);
            holder = new LocViewHolder();
            holder.item = (TextView) convertView.findViewById(R.id.item_name);

            convertView.setTag(holder);
        } else {
            holder = (LocViewHolder) convertView.getTag();
        }

        holder.item.setText(mItems[position]);

        return convertView;
    }

    public static class LocViewHolder {

        TextView item;
    }
}
