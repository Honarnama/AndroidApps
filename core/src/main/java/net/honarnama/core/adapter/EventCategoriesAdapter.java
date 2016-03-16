package net.honarnama.core.adapter;


import net.honarnama.base.R;
import net.honarnama.core.model.EventCategory;
import net.honarnama.core.model.Provinces;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by elnaz on 11/29/15.
 */
public class EventCategoriesAdapter extends BaseAdapter {
    private final Context mContext;
    public TreeMap<Number, EventCategory> mEventCategoryObjectsTreeMap;
    public List<String> mCategoryNameList = new ArrayList<>();
    public List<String> mCategoryIdsList = new ArrayList<>();

    public EventCategoriesAdapter(Context context, TreeMap<Number, EventCategory> categoryTreeMap) {
        super();
        mContext = context;
        mEventCategoryObjectsTreeMap = categoryTreeMap;

        if (mEventCategoryObjectsTreeMap != null) {
            for (EventCategory eventCategory : mEventCategoryObjectsTreeMap.values()) {
                mCategoryIdsList.add(eventCategory.getObjectId());
                mCategoryNameList.add(eventCategory.getName());
            }
        }


    }

    @Override
    public int getCount() {
        if (mEventCategoryObjectsTreeMap != null) {
            return mEventCategoryObjectsTreeMap.size();
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

        TextView provinceNameTextView = (TextView) rowView.findViewById(R.id.province_name_text_view);
        if (position == 0) {
            provinceNameTextView.setBackgroundColor(mContext.getResources().getColor(R.color.amber_super_extra_light));
        }

        provinceNameTextView.setText(mCategoryNameList.get(position));
        return rowView;
    }
}
