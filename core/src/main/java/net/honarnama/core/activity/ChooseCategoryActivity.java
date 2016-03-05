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
import net.honarnama.core.model.Category;
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

public class ChooseCategoryActivity extends HonarnamaBaseActivity {

    private ListView mCategoriesListView;
    HashMap<Number, String> mCurrentArtCategoriesName;
    HashMap<Number, String> mCurrentArtCategoriesObjectIds;

    CategoriesAdapter mCategoriesAdapter;
    private String mSelectedCategoryObjectId;
    public HashMap<String, ArrayList<String>> mCategoriesHierarchyHashMap = new HashMap<String, ArrayList<String>>();
    public HashMap<String, String> mCategoriesNameHashMap = new HashMap<String, String>();
    public HashMap<String, Number> mCategoriesOrderHashMap = new HashMap<String, Number>();

    public ArrayList<String> mNodeCategories = new ArrayList<String>();
    public HashMap<String, String> mFilterSubCatParentHashMap = new HashMap<String, String>();

    public String mCallingApp = HonarnamaBaseApp.SELL_APP_KEY;

    public String mAllCategoriesFilterObjectId;

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
        if ((intent != null) && intent.hasExtra(HonarnamaBaseApp.INTENT_ORIGIN)) {
            mCallingApp = intent.getStringExtra(HonarnamaBaseApp.INTENT_ORIGIN);
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

        final ProgressDialog receivingDataProgressDialog = new ProgressDialog(this);
        receivingDataProgressDialog.setCancelable(false);
        receivingDataProgressDialog.setMessage(getString(R.string.receiving_data));

        ParseQuery<Category> parseQuery = ParseQuery.getQuery(Category.class);

//        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ChooseCategoryActivity.this);
        String sharedPrefKey;
        if (HonarnamaUser.getCurrentUser() == null || mCallingApp == HonarnamaBaseApp.BROWSE_APP_KEY) {
            sharedPrefKey = HonarnamaBaseApp.BROWSE_APP_KEY;
        } else {
            sharedPrefKey = HonarnamaUser.getCurrentUser().getUsername();
            parseQuery.whereEqualTo(Category.ALL_SUB_CAT_FILTER_TYPE, false);
        }

        final SharedPreferences sharedPref = getSharedPreferences(sharedPrefKey, Context.MODE_PRIVATE);

        if (sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_CATEGORIES_SYNCED, false)) {
            if (BuildConfig.DEBUG) {
                logD("get categories list from LocalDatastore in buildCategoriesHierarchyHashMap");
            }
            parseQuery.fromLocalDatastore();
        } else {
            if (BuildConfig.DEBUG) {
                logD("get categories list from remote database in buildCategoriesHierarchyHashMap");
            }
            if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                return;
            }
            receivingDataProgressDialog.show();
        }
        parseQuery.findInBackground(new FindCallback<Category>() {
            public void done(final List<Category> artCategories, ParseException e) {

                if (!sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_CATEGORIES_SYNCED, false)) {
                    receivingDataProgressDialog.dismiss();
                }
                if (e == null) {
                    if (!sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_CATEGORIES_SYNCED, false)) {
                        ParseObject.unpinAllInBackground(Category.OBJECT_NAME, artCategories, new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    ParseObject.pinAllInBackground(Category.OBJECT_NAME, artCategories, new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        SharedPreferences.Editor editor = sharedPref.edit();
                                                        editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_CATEGORIES_SYNCED, true);
                                                        editor.commit();
                                                    }
                                                }
                                            }
                                    );
                                }
                            }
                        });
                    }

                    for (int i = 0; i < artCategories.size(); i++) {

                        Category artCategory = artCategories.get(i);

                        if (artCategory.getAllSubCatFilterType() == true) {
                            mFilterSubCatParentHashMap.put(artCategory.getObjectId(), artCategory.getParentId());
                        }

                        if ((artCategory.getParentId() != null) && artCategory.getParentId().equals("ALL")) {
                            mAllCategoriesFilterObjectId = artCategory.getObjectId();
                        }

                        if (artCategory.getString("parentId") == null) {//first level category
                            if (!mCategoriesHierarchyHashMap.containsKey(artCategory.getObjectId())) {
                                mCategoriesHierarchyHashMap.put(artCategory.getObjectId(), null);
                            }
                        } else {
                            ArrayList<String> tempArrayList = new ArrayList<String>();
                            tempArrayList.add(artCategory.getObjectId());

                            if (mCategoriesHierarchyHashMap.containsKey(artCategory.getString("parentId"))) {
                                if (mCategoriesHierarchyHashMap.get(artCategory.getString("parentId")) != null) {
                                    tempArrayList.addAll(mCategoriesHierarchyHashMap.get(artCategory.getString("parentId")));
                                }
                            }
                            mCategoriesHierarchyHashMap.put(artCategory.getString("parentId"), tempArrayList);
                        }
                        mCategoriesNameHashMap.put(artCategories.get(i).getObjectId(), artCategories.get(i).getString("name"));
                        mCategoriesOrderHashMap.put(artCategories.get(i).getObjectId(), artCategories.get(i).getNumber("order"));
                    }

                    setNodeCategories();
                    populateList();

                } else {
                    Toast.makeText(ChooseCategoryActivity.this, getString(R.string.syncing_data_failed), Toast.LENGTH_LONG).show();
                    logE("Receiving categories list failed. Code: " + e.getCode() +
                            "//" + e.getMessage() + " // " + e, null, e);
                }
            }
        });
    }

    private void populateList() {

        mCurrentArtCategoriesName = new HashMap<Number, String>();
        mCurrentArtCategoriesObjectIds = new HashMap<Number, String>();

        if (mSelectedCategoryObjectId == null) {
            //nothing is selected yet
            for (String key : mCategoriesHierarchyHashMap.keySet()) {
                int index = mCategoriesOrderHashMap.get(key).intValue();
                if (mCallingApp.equals(HonarnamaBaseApp.SELL_APP_KEY)) {
                    index = index - 1;
                }
                mCurrentArtCategoriesObjectIds.put(index, key);
                mCurrentArtCategoriesName.put(index, mCategoriesNameHashMap.get(key));
            }

        } else {
            ArrayList<String> notSortedCurrentCategoryObjectIds = mCategoriesHierarchyHashMap.get(mSelectedCategoryObjectId);
            for (int i = 0; i < notSortedCurrentCategoryObjectIds.size(); i++) {
                String objectId = notSortedCurrentCategoryObjectIds.get(i);
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
            mCategoriesAdapter = new CategoriesAdapter(ChooseCategoryActivity.this, mCurrentArtCategoriesObjectIds, mCurrentArtCategoriesName, mNodeCategories, mFilterSubCatParentHashMap);
            mCategoriesListView.setAdapter(mCategoriesAdapter);
        }

    }

    public void setNodeCategories() {

        //no child
        for (HashMap.Entry<String, ArrayList<String>> entry : mCategoriesHierarchyHashMap.entrySet()) {
            String key = entry.getKey();
            ArrayList<String> value = entry.getValue();
            if (value == null) {
                mNodeCategories.add(key);
            }
        }

        for (String key : mCategoriesNameHashMap.keySet()) {

            if (!mCategoriesHierarchyHashMap.containsKey(key)) {
                mNodeCategories.add(key);
            }
        }

    }

    public boolean isNodeCategory(String categoryObjectId) {

        if (mNodeCategories.contains(categoryObjectId)) {
            return true;
        }
        return false;
    }

    public boolean isFilterSubCategoryRowSelected(String categoryObjectId) {

        if (mFilterSubCatParentHashMap.containsKey(categoryObjectId)) {
            return true;
        }
        return false;
    }


    public void returnSelectedCategory() {
        Intent data = new Intent();
        if (isFilterSubCategoryRowSelected(mSelectedCategoryObjectId)) {
            ArrayList<String> subCats = new ArrayList<>();
            data.putExtra("isFilterSubCategoryRowSelected", true);
            if (mSelectedCategoryObjectId.equals(mAllCategoriesFilterObjectId)) {
                data.putStringArrayListExtra("subCats", subCats);
            } else {
                subCats = mCategoriesHierarchyHashMap.get(mFilterSubCatParentHashMap.get(mSelectedCategoryObjectId));
                data.putStringArrayListExtra("subCats", subCats);
            }
        }

        data.putExtra("selectedCategoryName", mCategoriesNameHashMap.get(mSelectedCategoryObjectId));
        data.putExtra("selectedCategoryObjectId", mSelectedCategoryObjectId);
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
