package net.honarnama.browse.fragment;


import com.mikepenz.iconics.view.IconicsImageView;
import com.parse.ParseException;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.adapter.ItemsAdapter;
import net.honarnama.browse.dialog.ItemFilterDialogActivity;
import net.honarnama.browse.model.Item;
import net.honarnama.core.activity.ChooseArtCategoryActivity;
import net.honarnama.core.model.Province;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
    public String mSelectedCategoryName;

    public int mMinPriceIndex = -1;
    public String mMinPriceValue;
    public int mMaxPriceIndex = -1;
    public String mMaxPriceValue;

    ItemsAdapter mItemsAdapter;
    public Button mCategoryFilterButton;
    public LinearLayout mLoadingCircle;

    public RelativeLayout mEmptyListContainer;
    public RelativeLayout mFilterContainer;
    private Province mSelectedProvince;
    private String mSelectedProvinceId;
    private String mSelectedCityId;
    private String mSelectedProvinceName;
    private ArrayList<String> mSubCatList = new ArrayList<>();
    private boolean mIsFilterSubCategoryRowSelected = false;
    private boolean mIsAllIranChecked = true;
    private boolean mIsFilterApplied = false;

    private TextView mFilterTextView;
    private IconicsImageView mFilterIcon;

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

        mFilterTextView = (TextView) rootView.findViewById(R.id.filter_text_view);
        mFilterIcon = (IconicsImageView) rootView.findViewById(R.id.filter_icon);

        View header = inflater.inflate(R.layout.item_list_header, null);
        mCategoryFilterButton = (Button) header.findViewById(R.id.category_filter_btn);
        if (!TextUtils.isEmpty(mSelectedCategoryName)) {
            mCategoryFilterButton.setText(mSelectedCategoryName);
        }
        mCategoryFilterButton.setOnClickListener(this);

        mListView.addHeaderView(header);

        mLoadingCircle = (LinearLayout) rootView.findViewById(R.id.loading_circle_container);

        listItems();
        mListView.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        changeFilterTitle();
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
        Item selectedItem = mItemsAdapter.getItem(position - 1);
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        if (selectedItem != null) {
            controlPanelActivity.displayItemPage(selectedItem.getId(), false);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.category_filter_btn) {
            Intent intent = new Intent(getActivity(), ChooseArtCategoryActivity.class);
            intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_INTENT_CALLER, HonarnamaBaseApp.PREF_NAME_BROWSE_APP);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            getParentFragment().startActivityForResult(intent, HonarnamaBrowseApp.INTENT_CHOOSE_CATEGORY_CODE);
        }
        if (v.getId() == R.id.filter_container) {
            Intent intent = new Intent(getActivity(), ItemFilterDialogActivity.class);
            intent.putExtra(HonarnamaBrowseApp.EXTRA_KEY_PROVINCE_ID, mSelectedProvinceId);
            intent.putExtra(HonarnamaBrowseApp.EXTRA_KEY_CITY_ID, mSelectedCityId);
            intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_ALL_IRAN, mIsAllIranChecked);
            if (mMinPriceIndex > -1) {
                intent.putExtra(HonarnamaBrowseApp.EXTRA_KEY_MIN_PRICE_INDEX, mMinPriceIndex);
            }

            if (mMaxPriceIndex > -1) {
                intent.putExtra(HonarnamaBrowseApp.EXTRA_KEY_MAX_PRICE_INDEX, mMaxPriceIndex);
            }
            getParentFragment().startActivityForResult(intent, HonarnamaBrowseApp.INTENT_FILTER_ITEMS_CODE);
        }

        if (v.getId() == R.id.on_error_retry_container) {
            if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                return;
            }
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
                if (((ParseException) e).getCode() != ParseException.OBJECT_NOT_FOUND) {
                    logE("Error Querying Items: " + e, e);
                    if (isVisible()) {
                        Toast.makeText(getActivity(), getString(R.string.error_occured) + getString(R.string.check_net_connection), Toast.LENGTH_SHORT).show();
                    }
                    mOnErrorRetry.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void listItems() {
//TODO
//        final ParseQuery<Store> storeQuery = new ParseQuery<Store>(Store.class);
//        storeQuery.whereEqualTo(Store.STATUS, Store.STATUS_CODE_VERIFIED);
//        storeQuery.whereEqualTo(Store.VALIDITY_CHECKED, true);

//        if (!mIsAllIranChecked) {
//            if (!TextUtils.isEmpty(mSelectedProvinceId)) {
//                Province province = ParseObject.createWithoutData(Province.class, mSelectedProvinceId);
//                storeQuery.whereEqualTo(Store.PROVINCE, province);
//            }
//
//            if (!TextUtils.isEmpty(mSelectedCityId)) {
//                if (!mSelectedCityId.equals(City.ALL_CITY_ID)) {
//                    City city = ParseObject.createWithoutData(City.class, mSelectedCityId);
//                    storeQuery.whereEqualTo(Store.CITY, city);
//                }
//            }
//        }
//        ArrayList<ArtCategory> queryCategoryIds = new ArrayList<>();
//        if (!(mIsFilterSubCategoryRowSelected == true && (mSubCatList == null || mSubCatList.isEmpty()))) {
//
//            ArrayList<String> querySubCatIds = new ArrayList<>();
//            if (mSubCatList == null || mSubCatList.isEmpty()) {
//                if (!TextUtils.isEmpty(mSelectedCategoryId)) {
//                    querySubCatIds.add(mSelectedCategoryId);
//                }
//            } else {
//                querySubCatIds = mSubCatList;
//            }
//
//            for (int i = 0; i < querySubCatIds.size(); i++) {
//                ArtCategory category = ParseObject.createWithoutData(ArtCategory.class, querySubCatIds.get(i));
//                queryCategoryIds.add(category);
//            }
//
//        }
//
//        final ArrayList<ArtCategory> finalQueryCategoryIds = queryCategoryIds;
//
////        ParseQueryAdapter.QueryFactory<ParseObject> filterFactory =
////                new ParseQueryAdapter.QueryFactory<ParseObject>() {
////                    public ParseQuery create() {
////                        ParseQuery<Item> parseQuery = new ParseQuery<Item>(Item.class);
////                        parseQuery.whereEqualTo(Item.STATUS, Item.STATUS_CODE_VERIFIED);
////                        parseQuery.whereEqualTo(Item.VALIDITY_CHECKED, true);
////                        parseQuery.whereExists(Item.STORE);
////
////                        if (!mIsAllIranChecked) {
////                            parseQuery.whereMatchesQuery(Item.STORE, storeQuery);
////                        }
////
////                        if (mMinPriceIndex > -1 && mMinPriceValue != null && !(mMinPriceValue.equals("MAX"))) {
////                            parseQuery.whereGreaterThanOrEqualTo(Item.PRICE, Integer.valueOf(mMinPriceValue));
////                        }
////
////                        if (mMaxPriceIndex > -1 && mMaxPriceValue != null && !(mMaxPriceValue.equals("MAX"))) {
////                            parseQuery.whereLessThanOrEqualTo(Item.PRICE, Integer.valueOf(mMaxPriceValue));
////                        }
////
////                        if (finalQueryCategoryIds != null && !(finalQueryCategoryIds.isEmpty())) {
////                            parseQuery.whereContainedIn(Item.CATEGORY, finalQueryCategoryIds);
////                        }
////                        parseQuery.include(Item.CATEGORY);
////                        return parseQuery;
////                    }
////                };

        mItemsAdapter = new ItemsAdapter(getContext());
        mListView.setAdapter(mItemsAdapter);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case HonarnamaBaseApp.INTENT_CHOOSE_CATEGORY_CODE:

                if (resultCode == getActivity().RESULT_OK) {

                    boolean isFilterSubCategoryRowSelected = data.getBooleanExtra(HonarnamaBaseApp.EXTRA_KEY_FILTER_SUB_CAT_ROW_SELECTED, false);
                    ArrayList<String> subCatList = data.getStringArrayListExtra(HonarnamaBaseApp.EXTRA_KEY_SUB_CATS);

                    mSelectedCategoryName = data.getStringExtra(HonarnamaBaseApp.EXTRA_KEY_CATEGORY_NAME);
                    mCategoryFilterButton.setText(mSelectedCategoryName);
                    mSelectedCategoryId = data.getStringExtra(HonarnamaBaseApp.EXTRA_KEY_CATEGORY_ID);

                    mSubCatList = subCatList;
                    mIsFilterSubCategoryRowSelected = isFilterSubCategoryRowSelected;
                    listItems();
                }
                break;

            case HonarnamaBaseApp.INTENT_FILTER_ITEMS_CODE:

                if (resultCode == getActivity().RESULT_OK) {
                    mSelectedProvinceId = data.getStringExtra(HonarnamaBrowseApp.EXTRA_KEY_PROVINCE_ID);
                    mSelectedProvinceName = data.getStringExtra(HonarnamaBrowseApp.EXTRA_KEY_PROVINCE_NAME);
                    mSelectedCityId = data.getStringExtra(HonarnamaBrowseApp.EXTRA_KEY_CITY_ID);
                    mMinPriceIndex = data.getIntExtra(HonarnamaBrowseApp.EXTRA_KEY_MIN_PRICE_INDEX, -1);
                    mMinPriceValue = data.getStringExtra(HonarnamaBrowseApp.EXTRA_KEY_MIN_PRICE_VALUE);
                    mMaxPriceIndex = data.getIntExtra(HonarnamaBrowseApp.EXTRA_KEY_MAX_PRICE_INDEX, -1);
                    mMaxPriceValue = data.getStringExtra(HonarnamaBrowseApp.EXTRA_KEY_MAX_PRICE_VALUE);
                    mIsAllIranChecked = data.getBooleanExtra(HonarnamaBaseApp.EXTRA_KEY_ALL_IRAN, true);
                    mIsFilterApplied = data.getBooleanExtra(HonarnamaBaseApp.EXTRA_KEY_FILTER_APPLIED, false);

                    changeFilterTitle();

                    listItems();
                }
                break;
        }
    }

    private void changeFilterTitle() {
        if (mIsFilterApplied) {
            mFilterTextView.setTextColor(getResources().getColor(R.color.dark_cyan));
            mFilterTextView.setText(R.string.change_filter);
            mFilterIcon.setColor(getResources().getColor(R.color.dark_cyan));
        } else {
            mFilterTextView.setTextColor(getResources().getColor(R.color.text_color));
            mFilterTextView.setText(getResources().getString(R.string.item_filter));
            mFilterIcon.setColor(getResources().getColor(R.color.text_color));
        }
    }

}

