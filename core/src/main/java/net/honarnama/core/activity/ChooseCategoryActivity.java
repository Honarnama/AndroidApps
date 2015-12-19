package net.honarnama.core.activity;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.R;
import net.honarnama.core.adapter.CategoriesAdapter;
import net.honarnama.core.model.Category;
import net.honarnama.core.utils.NetworkManager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_category);

        mCategoriesListView = (ListView) findViewById(R.id.art_categories_list_view);
        getArtCategoriesList();
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

        final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        if (sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_CATEGORIES_SYNCED, false)) {
            logD(null, "get categories list from LocalDatastore in buildCategoriesHierarchyHashMap");
            parseQuery.fromLocalDatastore();
        } else {
            logD(null, "get categories list from remote database in buildCategoriesHierarchyHashMap");
            if (!NetworkManager.getInstance().isNetworkEnabled(this, true)) {
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
                                ParseObject.pinAllInBackground(Category.OBJECT_NAME, artCategories, new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                SharedPreferences.Editor editor = sharedPref.edit();
                                                editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_CATEGORIES_SYNCED, true);
                                                editor.commit();
                                            }
                                        }
                                );
                            }
                        });
                    }

                    Toast.makeText(ChooseCategoryActivity.this, artCategories.size()+"", Toast.LENGTH_SHORT).show();

                    for (int i = 0; i < artCategories.size(); i++) {

                        Category artCategory = artCategories.get(i);

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
                mCurrentArtCategoriesObjectIds.put(mCategoriesOrderHashMap.get(key).intValue() - 1, key);
                mCurrentArtCategoriesName.put(mCategoriesOrderHashMap.get(key).intValue() - 1, mCategoriesNameHashMap.get(key));
            }

        } else {
            ArrayList<String> notSortedCurrentCategoryObjectIds = mCategoriesHierarchyHashMap.get(mSelectedCategoryObjectId);
            for (int i = 0; i < notSortedCurrentCategoryObjectIds.size(); i++) {
                String objectId = notSortedCurrentCategoryObjectIds.get(i);
                mCurrentArtCategoriesObjectIds.put((mCategoriesOrderHashMap.get(objectId).intValue()) - 1, objectId);
                mCurrentArtCategoriesName.put((mCategoriesOrderHashMap.get(objectId).intValue()) - 1, mCategoriesNameHashMap.get(objectId));
            }
        }

        mCategoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedCategoryObjectId = mCurrentArtCategoriesObjectIds.get(position);
                mCategoriesAdapter.setSelectedPosition(position);
                if (!isNodeCategory(mSelectedCategoryObjectId)) {
                    getArtCategoriesList();
                } else {
                    returnSelectedCategory();
                }
            }
        });

        if (mCategoriesAdapter != null) {
            mCategoriesAdapter.refreshArtCategories(mCurrentArtCategoriesObjectIds, mCurrentArtCategoriesName);
        } else {
            mCategoriesAdapter = new CategoriesAdapter(ChooseCategoryActivity.this, mCurrentArtCategoriesObjectIds, mCurrentArtCategoriesName, mNodeCategories);
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

    public void returnSelectedCategory() {
        Intent data = new Intent();
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
