package net.honarnama.base.activity;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.R;
import net.honarnama.base.adapter.CategoriesAdapter;
import net.honarnama.base.model.ArtCategory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChooseArtCategoryActivity extends HonarnamaBaseActivity {

    private ListView mCategoriesListView;
    HashMap<Number, String> mCurrentArtCategoriesName;
    HashMap<Number, Integer> mCurrentArtCategoriesObjectIds;

    CategoriesAdapter mCategoriesAdapter;
    private Integer mSelectedCategoryObjectId;
    public HashMap<Integer, ArrayList<Integer>> mCategoriesHierarchyHashMap = new HashMap<>();
    public HashMap<Integer, String> mCategoriesNameHashMap = new HashMap<>();
    public HashMap<Integer, Integer> mCategoriesOrderHashMap = new HashMap<>();

    public ArrayList<Integer> mNodeCategories = new ArrayList();
    public HashMap<Integer, Integer> mFilterSubCatParentHashMap = new HashMap<>();

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

        List<ArtCategory> artCategories;
        if (HonarnamaBaseApp.PACKAGE_NAME.equals(HonarnamaBaseApp.SELL_PACKAGE_NAME)) {
            artCategories = ArtCategory.getAllArtCategoriesSorted(false);
        } else {
            artCategories = ArtCategory.getAllArtCategoriesSorted(true);
        }

        for (int i = 0; i < artCategories.size(); i++) {
            ArtCategory artCategory = artCategories.get(i);

            mCategoriesNameHashMap.put(artCategory.getId(), artCategory.getName());
            mCategoriesOrderHashMap.put(artCategory.getId(), artCategory.getOrder());

            if (artCategory.isAllSubCatFilterType() == true) {
                mFilterSubCatParentHashMap.put(artCategory.getId(), artCategory.getParentId());
            }

            if (artCategory.getId() == 0) { //Zero belongs to all categories filter type
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

        if (BuildConfig.DEBUG) {
            logD("Art Categories Hierarchy: " + mCategoriesHierarchyHashMap);
        }

        setNodeCategories();
        populateList();
    }

    private void populateList() {

        mCurrentArtCategoriesName = new HashMap<>();
        mCurrentArtCategoriesObjectIds = new HashMap<>();

        if (mSelectedCategoryObjectId == null) {
            if (BuildConfig.DEBUG) {
                logD("No category is selected yet. mCategoriesHierarchyHashMap size is " + mCategoriesHierarchyHashMap.size());
            }
            //nothing is selected yet
            for (Integer key : mCategoriesHierarchyHashMap.keySet()) {

                int index = mCategoriesOrderHashMap.get(key);
                if (HonarnamaBaseApp.PACKAGE_NAME.equals(HonarnamaBaseApp.SELL_PACKAGE_NAME)) {
                    index = index - 1;
                }
                mCurrentArtCategoriesObjectIds.put(index, key);
                mCurrentArtCategoriesName.put(index, mCategoriesNameHashMap.get(key));
            }
            if (BuildConfig.DEBUG) {
                logD("No category is selected yet. mCurrentArtCategoriesName is " + mCurrentArtCategoriesName);
            }
        } else {
            ArrayList<Integer> notSortedCurrentCategoryObjectIds = mCategoriesHierarchyHashMap.get(mSelectedCategoryObjectId);
            for (int i = 0; i < notSortedCurrentCategoryObjectIds.size(); i++) {
                int objectId = notSortedCurrentCategoryObjectIds.get(i);
                int index = mCategoriesOrderHashMap.get(objectId).intValue();
                if (HonarnamaBaseApp.PACKAGE_NAME.equals(HonarnamaBaseApp.SELL_PACKAGE_NAME)) {
                    index = index - 1;
                }
                mCurrentArtCategoriesObjectIds.put(index, objectId);
                mCurrentArtCategoriesName.put(index, mCategoriesNameHashMap.get(objectId));
            }
        }

        if (BuildConfig.DEBUG) {
            logD("Selected Cat ids: "+ mCurrentArtCategoriesObjectIds);
            logD("Selected Cat names: "+ mCurrentArtCategoriesName);
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

    public boolean isNodeCategory(int categoryId) {
        if (mNodeCategories.contains(categoryId)) {
            return true;
        }
        return false;
    }

    public int getParentId(int categoryId) {
        for (HashMap.Entry<Integer, ArrayList<Integer>> entry : mCategoriesHierarchyHashMap.entrySet()) {
            int key = entry.getKey();
            ArrayList<Integer> childs = entry.getValue();
            if (childs != null) {
                if (childs.contains(categoryId)) {
                    return key;
                }
            }
        }
        return 0;
    }

    public boolean isFilterSubCategoryRowSelected(int categoryId) {

        if (mFilterSubCatParentHashMap.containsKey(categoryId)) {
            return true;
        }
        return false;
    }


    public void returnSelectedCategory() {
        Intent data = new Intent();
        if (isFilterSubCategoryRowSelected(mSelectedCategoryObjectId)) {
            data.putExtra(HonarnamaBaseApp.EXTRA_KEY_FILTER_SUB_CAT_ROW_SELECTED, true);
//            ArrayList<Integer> subCats = new ArrayList<>();
//            if (mSelectedCategoryObjectId.equals(mAllSubCategoriesFilterObjectId)) {
//                data.putIntegerArrayListExtra(HonarnamaBaseApp.EXTRA_KEY_SUB_CATS, subCats);
//            } else {
//                subCats = mCategoriesHierarchyHashMap.get(mFilterSubCatParentHashMap.get(mSelectedCategoryObjectId));
//                data.putIntegerArrayListExtra(HonarnamaBaseApp.EXTRA_KEY_SUB_CATS, subCats);
//            }
        }

        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_CATEGORY_NAME, mCategoriesNameHashMap.get(mSelectedCategoryObjectId));
        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_CATEGORY_ID, mSelectedCategoryObjectId);
        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_CATEGORY_PARENT_ID, getParentId(mSelectedCategoryObjectId));

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
