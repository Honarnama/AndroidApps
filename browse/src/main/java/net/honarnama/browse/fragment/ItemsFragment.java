package net.honarnama.browse.fragment;


import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.adapter.ItemsParseAdapter;
import net.honarnama.browse.dialog.ItemFilterDialogActivity;
import net.honarnama.browse.model.Item;
import net.honarnama.core.activity.ChooseCategoryActivity;
import net.honarnama.core.model.Category;
import net.honarnama.core.model.City;
import net.honarnama.core.model.Provinces;
import net.honarnama.core.model.Store;
import net.honarnama.core.utils.NetworkManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elnaz on 2/11/16.
 */
public class ItemsFragment extends HonarnamaBrowseFragment implements AdapterView.OnItemClickListener, View.OnClickListener {
    public static ItemsFragment mItemsFragment;
    private ListView mListView;
    public String mSelectedCategoryId;

    ItemsParseAdapter mItemsParseAdapter;
    public Button mCategoryFilterButton;
    public LinearLayout mLoadingCircle;

    public RelativeLayout mEmptyListContainer;
    public RelativeLayout mFilterContainer;
    private Provinces mSelectedProvince;
    private String mSelectedProvinceId;
    private String mSelectedCityId;
    private String mSelectedProvinceName;
    private ArrayList<String> mSubCatList = new ArrayList<>();
    private boolean mIsFilterSubCategoryRowSelected = false;

    public RelativeLayout mOnErrorRetry;

    public synchronized static ItemsFragment getInstance() {
        if (mItemsFragment == null) {
            mItemsFragment = new ItemsFragment();
        }
        return mItemsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_items, container, false);

        mListView = (ListView) rootView.findViewById(R.id.items_listView);
        mEmptyListContainer = (RelativeLayout) rootView.findViewById(R.id.empty_list_container);
        mFilterContainer = (RelativeLayout) rootView.findViewById(R.id.filter_container);
        mFilterContainer.setOnClickListener(this);

        mOnErrorRetry = (RelativeLayout) rootView.findViewById(R.id.on_error_retry_container);
        mOnErrorRetry.setOnClickListener(this);

        View header = inflater.inflate(R.layout.item_list_header, null);
        mCategoryFilterButton = (Button) header.findViewById(R.id.category_filter_btn);
        mCategoryFilterButton.setOnClickListener(this);

        mListView.addHeaderView(header);

        mLoadingCircle = (LinearLayout) rootView.findViewById(R.id.loading_circle_container);

        listItems();
        mListView.setOnItemClickListener(this);


        return rootView;
    }


    @Override
    public String getTitle(Context context) {
        return getString(R.string.hornama);
    }

    @Override
    public void onSelectedTabClick() {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        Toast.makeText(getActivity(), "inja "+position, Toast.LENGTH_SHORT).show();


        ParseObject selectedItem = (ParseObject) mItemsParseAdapter.getItem(position-1);
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        if (selectedItem != null) {
            controlPanelActivity.displayItemPage(selectedItem.getObjectId(), false);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.category_filter_btn) {
            Intent intent = new Intent(getActivity(), ChooseCategoryActivity.class);
            intent.putExtra(HonarnamaBaseApp.INTENT_ORIGIN, HonarnamaBaseApp.BROWSE_APP_KEY);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            startActivityForResult(intent, HonarnamaBrowseApp.INTENT_CHOOSE_CATEGORY_CODE);
        }
        if (v.getId() == R.id.filter_container) {
            Intent intent = new Intent(getActivity(), ItemFilterDialogActivity.class);
            intent.putExtra("selectedProvinceId", mSelectedProvinceId);
            intent.putExtra("selectedCityId", mSelectedCityId);
            startActivityForResult(intent, HonarnamaBrowseApp.INTENT_FILTER_ITEMS_CODE);
        }

        if (v.getId() == R.id.on_error_retry_container) {
            ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
            controlPanelActivity.refreshTopFragment();
        }
    }

    class onQueryLoadListener implements ParseQueryAdapter.OnQueryLoadListener {
        @Override
        public void onLoading() {
            mEmptyListContainer.setVisibility(View.GONE);
            mOnErrorRetry.setVisibility(View.GONE);
            mLoadingCircle.setVisibility(View.VISIBLE);
        }

        @Override
        public void onLoaded(List objects, Exception e) {

            mLoadingCircle.setVisibility(View.GONE);
            if (e == null) {
                if ((objects != null) && objects.size() > 0) {
                    mEmptyListContainer.setVisibility(View.GONE);
                } else {
                    mEmptyListContainer.setVisibility(View.VISIBLE);
                }
            } else {
                mEmptyListContainer.setVisibility(View.VISIBLE);
                if (isVisible()) {
                    Toast.makeText(getActivity(), getString(R.string.error_occured) + getString(R.string.please_check_internet_connection), Toast.LENGTH_SHORT).show();
                }
                mOnErrorRetry.setVisibility(View.VISIBLE);
            }
        }
    }

    public void listItems() {

        final ParseQuery<Store> storeQuery = new ParseQuery<Store>(Store.class);
        storeQuery.whereEqualTo(Store.STATUS, Store.STATUS_CODE_VERIFIED);
        storeQuery.whereEqualTo(Store.VALIDITY_CHECKED, true);

        if (!TextUtils.isEmpty(mSelectedProvinceId)) {
            Provinces province = ParseObject.createWithoutData(Provinces.class, mSelectedProvinceId);
            storeQuery.whereEqualTo(Store.PROVINCE, province);
        }

        if (!TextUtils.isEmpty(mSelectedCityId)) {
            City city = ParseObject.createWithoutData(City.class, mSelectedCityId);
            storeQuery.whereEqualTo(Store.CITY, city);
        }

        ArrayList<Category> queryCategoryIds = new ArrayList<>();
        if (!(mIsFilterSubCategoryRowSelected == true && (mSubCatList == null || mSubCatList.isEmpty()))) {

            ArrayList<String> querySubCatIds = new ArrayList<>();
            if (mSubCatList == null || mSubCatList.isEmpty()) {
                if (!TextUtils.isEmpty(mSelectedCategoryId)) {
                    querySubCatIds.add(mSelectedCategoryId);
                }
            } else {
                querySubCatIds = mSubCatList;
            }

            for (int i = 0; i < querySubCatIds.size(); i++) {
                Category category = ParseObject.createWithoutData(Category.class, querySubCatIds.get(i));
                queryCategoryIds.add(category);
            }

        }

        final ArrayList<Category> finalQueryCategoryIds = queryCategoryIds;


        ParseQueryAdapter.QueryFactory<ParseObject> filterFactory =
                new ParseQueryAdapter.QueryFactory<ParseObject>() {
                    public ParseQuery create() {
                        ParseQuery<Item> parseQuery = new ParseQuery<Item>(Item.class);
                        parseQuery.whereEqualTo(Item.STATUS, Item.STATUS_CODE_VERIFIED);
                        parseQuery.whereEqualTo(Item.VALIDITY_CHECKED, true);
                        parseQuery.whereMatchesQuery(Item.STORE, storeQuery);

                        if (finalQueryCategoryIds != null && !(finalQueryCategoryIds.isEmpty())) {
                            parseQuery.whereContainedIn(Item.CATEGORY, finalQueryCategoryIds);
                        }
                        parseQuery.include(Item.CATEGORY);
                        return parseQuery;
                    }
                };

        mItemsParseAdapter = new ItemsParseAdapter(getContext(), filterFactory);
        mItemsParseAdapter.addOnQueryLoadListener(new onQueryLoadListener());
        mListView.setAdapter(mItemsParseAdapter);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case HonarnamaBaseApp.INTENT_CHOOSE_CATEGORY_CODE:

                if (resultCode == getActivity().RESULT_OK) {

                    boolean isFilterSubCategoryRowSelected = data.getBooleanExtra("isFilterSubCategoryRowSelected", false);
                    ArrayList<String> subCatList = data.getStringArrayListExtra("subCats");

                    mCategoryFilterButton.setText(data.getStringExtra("selectedCategoryName"));
                    mSelectedCategoryId = data.getStringExtra("selectedCategoryObjectId");
                    mSubCatList = subCatList;
                    mIsFilterSubCategoryRowSelected = isFilterSubCategoryRowSelected;
                    listItems();
                }
                break;

            case HonarnamaBaseApp.INTENT_FILTER_ITEMS_CODE:

                if (resultCode == getActivity().RESULT_OK) {
                    mSelectedProvinceId = data.getStringExtra("selectedProvinceId");
                    mSelectedProvinceName = data.getStringExtra("selectedProvinceName");

                    mSelectedCityId = data.getStringExtra("selectedCityId");
                    listItems();
                }
                break;
        }
    }


}

