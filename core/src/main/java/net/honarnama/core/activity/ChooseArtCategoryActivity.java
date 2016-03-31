package net.honarnama.core.activity;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.R;
import net.honarnama.core.adapter.CategoriesAdapter;
import net.honarnama.core.model.ArtCategory;
import net.honarnama.core.utils.HonarnamaUser;
import net.honarnama.core.utils.NetworkManager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChooseArtCategoryActivity extends HonarnamaBaseActivity {

    private ListView mCategoriesListView;
    HashMap<Number, String> mCurrentArtCategoriesName;
    HashMap<Number, Integer> mCurrentArtCategoriesObjectIds;

    CategoriesAdapter mCategoriesAdapter;
    private Integer mSelectedCategoryObjectId;
    public HashMap<Integer, ArrayList<Integer>> mCategoriesHierarchyHashMap = new HashMap();
    public HashMap<Integer, String> mCategoriesNameHashMap = new HashMap();
    public HashMap<Integer, Number> mCategoriesOrderHashMap = new HashMap();

    public ArrayList<Integer> mNodeCategories = new ArrayList();
    public HashMap<Integer, Integer> mFilterSubCatParentHashMap = new HashMap();

    public String mCallingApp = HonarnamaBaseApp.SELL_APP_KEY;

    public int mAllSubCategoriesFilterObjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_category);
        mCategoriesListView = (ListView) findViewById(R.id.art_categories_list_view);
        getArtCategoriesList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if ((intent != null) && intent.hasExtra(HonarnamaBaseApp.EXTRA_KEY_INTENT_ORIGIN)) {
            mCallingApp = intent.getStringExtra(HonarnamaBaseApp.EXTRA_KEY_INTENT_ORIGIN);
        }
        setIntent(intent);
    }

    private void getArtCategoriesList() {

        if (mCategoriesHierarchyHashMap == null || mCategoriesHierarchyHashMap.isEmpty()) {
            buildCategoriesHierarchyHashMap();

        } else {
            populateList();
        }

    }

    private void buildCategoriesHierarchyHashMap() {
//
//        final ProgressDialog receivingDataProgressDialog = new ProgressDialog(this);
//        receivingDataProgressDialog.setCancelable(false);
//        receivingDataProgressDialog.setMessage(getString(R.string.receiving_data));

        List<ArtCategory> artCategories;
        if (HonarnamaUser.getCurrentUser() == null || mCallingApp == HonarnamaBaseApp.BROWSE_APP_KEY) {
            artCategories = ArtCategory.getAllArtCategories(true);
        } else {
            artCategories = ArtCategory.getAllArtCategories(false);
        }


        for (int i = 0; i < artCategories.size(); i++) {

            ArtCategory artCategory = artCategories.get(i);

            mCategoriesNameHashMap.put(artCategories.get(i).getId(), artCategories.get(i).getName());
            mCategoriesOrderHashMap.put(artCategories.get(i).getId(), artCategories.get(i).getOrder());

            if (artCategory.getAllSubCatFilterType() == true) {
                mFilterSubCatParentHashMap.put(artCategory.getId(), artCategory.getParentId());
            }

            if (artCategory.getParentId() > 0 && artCategory.getParentId() == 0) { //Zero belongs to all categories filter type
                mAllSubCategoriesFilterObjectId = artCategory.getId();
            }

            if (artCategory.getParentId() == 0) {//first level category
                if (!mCategoriesHierarchyHashMap.containsKey(artCategory.getId())) {
                    mCategoriesHierarchyHashMap.put(artCategory.getId(), null);
                }
            } else {
                ArrayList<Integer> tempArrayList = new ArrayList();
                tempArrayList.add(artCategory.getId());

                if (mCategoriesHierarchyHashMap.containsKey(artCategory.getParentId())) {
                    if (mCategoriesHierarchyHashMap.get(artCategory.getParentId()) != null) {
                        tempArrayList.addAll(mCategoriesHierarchyHashMap.get(artCategory.getParentId()));
                    }
                }
                mCategoriesHierarchyHashMap.put(artCategory.getParentId(), tempArrayList);
            }

        }

        setNodeCategories();
        populateList();
    }

    private void populateList() {

        mCurrentArtCategoriesName = new HashMap();
        mCurrentArtCategoriesObjectIds = new HashMap();

        if (mSelectedCategoryObjectId == null) {
            //nothing is selected yet
            for (Integer key : mCategoriesHierarchyHashMap.keySet()) {

                int index = mCategoriesOrderHashMap.get(key).intValue();
                if (mCallingApp.equals(HonarnamaBaseApp.SELL_APP_KEY)) {
                    index = index - 1;
                }
                mCurrentArtCategoriesObjectIds.put(index, key);
                mCurrentArtCategoriesName.put(index, mCategoriesNameHashMap.get(key));
            }

        } else {
            ArrayList<Integer> notSortedCurrentCategoryObjectIds = mCategoriesHierarchyHashMap.get(mSelectedCategoryObjectId);
            for (int i = 0; i < notSortedCurrentCategoryObjectIds.size(); i++) {
                int objectId = notSortedCurrentCategoryObjectIds.get(i);
                int index = mCategoriesOrderHashMap.get(objectId).intValue();
                if (mCallingApp.equals(HonarnamaBaseApp.SELL_APP_KEY)) {
                    index = index - 1;
                }
                mCurrentArtCategoriesObjectIds.put(index, objectId);
                mCurrentArtCategoriesName.put(index, mCategoriesNameHashMap.get(objectId));
            }
        }

        mCategoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedCategoryObjectId = mCurrentArtCategoriesObjectIds.get(position);
                mCategoriesAdapter.setSelectedPosition(position);
                if (isNodeCategory(mSelectedCategoryObjectId) || isFilterSubCategoryRowSelected(mSelectedCategoryObjectId)) {
                    returnSelectedCategory();
                } else {
                    getArtCategoriesList();
                }
            }
        });

        if (mCategoriesAdapter != null) {
            mCategoriesAdapter.refreshArtCategories(mCurrentArtCategoriesObjectIds, mCurrentArtCategoriesName);
        } else {
            mCategoriesAdapter = new CategoriesAdapter(ChooseArtCategoryActivity.this, mCurrentArtCategoriesObjectIds, mCurrentArtCategoriesName, mNodeCategories, mFilterSubCatParentHashMap);
            mCategoriesListView.setAdapter(mCategoriesAdapter);
        }

    }

    public void setNodeCategories() {

        //no child
        for (HashMap.Entry<Integer, ArrayList<Integer>> entry : mCategoriesHierarchyHashMap.entrySet()) {
            int key = entry.getKey();
            ArrayList<Integer> value = entry.getValue();
            if (value == null) {
                mNodeCategories.add(key);
            }
        }

        for (int key : mCategoriesNameHashMap.keySet()) {

            if (!mCategoriesHierarchyHashMap.containsKey(key)) {
                mNodeCategories.add(key);
            }
        }

    }

    public boolean isNodeCategory(int categoryObjectId) {

        if (mNodeCategories.contains(categoryObjectId)) {
            return true;
        }
        return false;
    }

    public boolean isFilterSubCategoryRowSelected(int categoryObjectId) {

        if (mFilterSubCatParentHashMap.containsKey(categoryObjectId)) {
            return true;
        }
        return false;
    }


    public void returnSelectedCategory() {
        Intent data = new Intent();
        if (isFilterSubCategoryRowSelected(mSelectedCategoryObjectId)) {
            ArrayList<Integer> subCats = new ArrayList<>();
            data.putExtra(HonarnamaBaseApp.EXTRA_KEY_FILTER_SUB_CAT_ROW_SELECTED, true);
            if (mSelectedCategoryObjectId.equals(mAllSubCategoriesFilterObjectId)) {
                data.putIntegerArrayListExtra(HonarnamaBaseApp.EXTRA_KEY_SUB_CATS, subCats);
            } else {
                subCats = mCategoriesHierarchyHashMap.get(mFilterSubCatParentHashMap.get(mSelectedCategoryObjectId));
                data.putIntegerArrayListExtra(HonarnamaBaseApp.EXTRA_KEY_SUB_CATS, subCats);
            }
        }

        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_CATEGORY_NAME, mCategoriesNameHashMap.get(mSelectedCategoryObjectId));
        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_CATEGORY_ID, mSelectedCategoryObjectId);

        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onBackPressed() {

        if (mSelectedCategoryObjectId != null) {
            mSelectedCategoryObjectId = null;
            getArtCategoriesList();
        } else {
            finish();
        }
    }
}
