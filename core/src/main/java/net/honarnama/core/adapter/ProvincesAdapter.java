package net.honarnama.core.adapter;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;


import net.honarnama.base.R;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by elnaz on 11/29/15.
 */
public class ProvincesAdapter extends BaseAdapter {
    private final Context mContext;
    public TreeMap<Number, HashMap<String, String>> mProvincesHashMap;
    public List<String> mProvincesNameList = new ArrayList<String>();
    public List<String> mProvincesIdsList = new ArrayList<String>();
    ;

    public ProvincesAdapter(Context context, TreeMap<Number, HashMap<String, String>> provincesHashMap) {
        super();
        mContext = context;
        mProvincesHashMap = provincesHashMap;

        for (HashMap<String, String> provinceSet : mProvincesHashMap.values()) {
            for (HashMap.Entry<String, String> province : provinceSet.entrySet()) {
                mProvincesNameList.add(province.getValue());
                mProvincesIdsList.add(province.getKey());
            }
        }

    }

    @Override
    public int getCount() {
        return mProvincesHashMap.size();
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

        provinceNameTextView.setText(mProvincesNameList.get(position));
        return rowView;
    }
}
