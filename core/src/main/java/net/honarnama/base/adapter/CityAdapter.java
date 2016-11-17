package net.honarnama.base.adapter;


import net.honarnama.base.R;

import android.content.Context;
import android.graphics.Color;
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
public class CityAdapter extends BaseAdapter {
    private final Context mContext;
    public TreeMap<Number, HashMap<Integer, String>> mCityTreeMap;
    public List<String> mCityNameList = new ArrayList<String>();
    public List<Integer> mCityIdsList = new ArrayList();

    public CityAdapter(Context context, TreeMap<Number, HashMap<Integer, String>> cityTreeMap) {
        super();
        mContext = context;
        mCityTreeMap = cityTreeMap;

        if (mCityTreeMap != null) {
            for (HashMap<Integer, String> cityHashMap : mCityTreeMap.values()) {
                for (HashMap.Entry<Integer, String> city : cityHashMap.entrySet()) {
                    mCityIdsList.add(city.getKey());
                    mCityNameList.add(city.getValue());
                }
            }
        }
    }

    @Override
    public int getCount() {
        if (mCityTreeMap != null) {
            return mCityTreeMap.size();
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

        if(mCityIdsList.get(position) == 0){
            provinceNameTextView.setBackgroundColor(mContext.getResources().getColor(R.color.amber_super_extra_light));
        }
        provinceNameTextView.setText(mCityNameList.get(position));
        return rowView;
    }
}
