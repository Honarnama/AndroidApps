package net.honarnama.core.adapter;


import net.honarnama.base.R;
import net.honarnama.core.model.EventCategory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by elnaz on 11/29/15.
 */
public class EventCategoriesAdapter extends BaseAdapter {
    private final Context mContext;
    List<EventCategory> mEventCategories = new ArrayList<>();

    public EventCategoriesAdapter(Context context, List<EventCategory> categories) {
        super();
        mContext = context;
        mEventCategories = categories;
    }

    @Override
    public int getCount() {
        if (mEventCategories != null) {
            return mEventCategories.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = layoutInflater.inflate(R.layout.province_row, parent, false);

        TextView nameTextView = (TextView) rowView.findViewById(R.id.province_name_text_view);
        if (position == 0) {
            nameTextView.setBackgroundColor(mContext.getResources().getColor(R.color.amber_super_extra_light));
        }

        nameTextView.setText(mEventCategories.get(position).getName());
        return rowView;
    }
}
