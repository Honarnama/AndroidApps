package net.honarnama.base.adapter;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.honarnama.base.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by elnaz on 11/29/15.
 */
public class CategoriesAdapter extends BaseAdapter {
    private final Context mContext;
    private HashMap<Number, String> mArtCategoriesName;
    private HashMap<Number, Integer> mArtCategoriesObjectIds;

    private int mSelectedPosition;
    public ArrayList<Integer> mNodeCategories = new ArrayList();
    public HashMap<Integer, Integer> mFilterSubCatParentHashMap = new HashMap<>();


    public CategoriesAdapter(Context context, HashMap<Number, Integer> artCategoriesObjectIds, HashMap<Number,
            String> artCategoriesName, ArrayList<Integer> nodeCategories, HashMap<Integer, Integer> filterSubCatParentHashMap) {
        super();
        mContext = context;
        mArtCategoriesObjectIds = artCategoriesObjectIds;
        mArtCategoriesName = artCategoriesName;
        mNodeCategories = nodeCategories;
        mFilterSubCatParentHashMap = filterSubCatParentHashMap;
    }

    @Override
    public int getCount() {
        return mArtCategoriesName.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void refreshArtCategories(HashMap<Number, Integer> artCategoriesObjectIds, HashMap<Number, String> artCategoriesName) {

        mArtCategoriesObjectIds = artCategoriesObjectIds;
        mArtCategoriesName = artCategoriesName;

        notifyDataSetChanged();
    }

    public void setSelectedPosition(int selectedPosition) {
        mSelectedPosition = selectedPosition;
//        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = layoutInflater.inflate(R.layout.category_row, parent, false);

        TextView categoryNameTextView = (TextView) rowView.findViewById(R.id.category_name_text_view);

        if (mFilterSubCatParentHashMap.containsKey(mArtCategoriesObjectIds.get(position))) {
            categoryNameTextView.setBackgroundColor(mContext.getResources().getColor(R.color.amber_super_extra_light));
        }

        ImageView categoryDrillDownArrowImageView = (ImageView) rowView.findViewById(R.id.category_drill_down_arrow_image_view);

        if (!mNodeCategories.contains(mArtCategoriesObjectIds.get(position)) && !mFilterSubCatParentHashMap.containsKey(mArtCategoriesObjectIds.get(position))) {
            categoryDrillDownArrowImageView.setVisibility(View.VISIBLE);
            categoryDrillDownArrowImageView.setImageDrawable(new IconicsDrawable(mContext)
                    .icon(GoogleMaterial.Icon.gmd_arrow_back)
                    .color(mContext.getResources().getColor(R.color.nokhodi_botte_jeghe))
                    .sizeDp(15));
        }

        categoryNameTextView.setText(mArtCategoriesName.get(position));
        return rowView;
    }
}
