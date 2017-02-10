package net.honarnama.browse.fragment;


import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.mikepenz.iconics.view.IconicsImageView;

import net.honarnama.GRPCUtils;
import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.activity.ChooseArtCategoryActivity;
import net.honarnama.base.model.City;
import net.honarnama.base.model.Province;
import net.honarnama.base.utils.NetworkManager;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.adapter.ItemsAdapter;
import net.honarnama.browse.dialog.ItemFilterDialogActivity;
import net.honarnama.browse.dialog.LocationFilterDialogActivity;
import net.honarnama.nano.ArtCategoryCriteria;
import net.honarnama.nano.BrowseItemsReply;
import net.honarnama.nano.BrowseItemsRequest;
import net.honarnama.nano.BrowseServiceGrpc;
import net.honarnama.nano.LocationCriteria;
import net.honarnama.nano.ReplyProperties;
import net.honarnama.nano.RequestProperties;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

/**
 * Created by elnaz on 2/11/16.
 */

public class ItemsFragment extends HonarnamaBrowseFragment implements AdapterView.OnItemClickListener, View.OnClickListener {
    public static ItemsFragment mItemsFragment;
    private ListView mListView;
    public int mSelectedCategoryId;
    public int mSelectedCategoryParentId;
    public String mSelectedCategoryName;

    public int mMinPriceIndex = -1;
    public String mMinPriceValue;
    public int mMaxPriceIndex = -1;
    public String mMaxPriceValue;
    public String mSearchTerm = "";

    ItemsAdapter mItemsAdapter;
    public Button mCategoryFilterButton;

    public RelativeLayout mEmptyListContainer;
    public RelativeLayout mFilterContainer;
    private int mSelectedProvinceId = -1;
    private int mSelectedCityId = -1;
    private String mSelectedProvinceName;
    private boolean mIsFilterSubCategoryRowSelected = false;
    private boolean mIsAllIranChecked = true;
    private boolean mIsFilterApplied = false;

    private TextView mFilterTextView;
    private IconicsImageView mFilterIcon;

    public RelativeLayout mOnErrorRetry;

    public TextView mLocationCriteriaTextView;

    private Tracker mTracker;

    boolean mUserScrolled = false;
    public LinearLayout mLoadingCircle;

    public RelativeLayout mLoadMoreProgressContainer;

    public long mNextPageId = 0;
    public boolean mHasMoreItems = true;

    public boolean mOnScrollIsLoading = false;

    public synchronized static ItemsFragment getInstance() {
        if (mItemsFragment == null) {
            mItemsFragment = new ItemsFragment();
        }
        return mItemsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = HonarnamaBrowseApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("ItemsFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_items, container, false);

        mListView = (ListView) rootView.findViewById(R.id.items_listView);
        mEmptyListContainer = (RelativeLayout) rootView.findViewById(R.id.empty_list_container);

        mFilterContainer = (RelativeLayout) rootView.findViewById(R.id.filter_container);
        mFilterContainer.setOnClickListener(this);
        mFilterTextView = (TextView) rootView.findViewById(R.id.filter_text_view);
        mFilterIcon = (IconicsImageView) rootView.findViewById(R.id.filter_icon);

        mOnErrorRetry = (RelativeLayout) rootView.findViewById(R.id.on_error_retry_container);
        mOnErrorRetry.setOnClickListener(this);

        View header = inflater.inflate(R.layout.item_list_header, null);
        mCategoryFilterButton = (Button) header.findViewById(R.id.category_filter_btn);
        if (!TextUtils.isEmpty(mSelectedCategoryName)) {
            mCategoryFilterButton.setText(mSelectedCategoryName);
        }
        mCategoryFilterButton.setOnClickListener(this);

        mListView.addHeaderView(header);

        mItemsAdapter = new ItemsAdapter(getContext());
        mListView.setAdapter(mItemsAdapter);

        getItems(false);
        mListView.setOnItemClickListener(this);

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // If scroll state is touch scroll then set userScrolled
                // true
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    mUserScrolled = true;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                // Now check if userScrolled is true and also check if
                // the item is end then update list view and set
                // userScrolled to false
                if (mUserScrolled
                        && firstVisibleItem + visibleItemCount == totalItemCount && mHasMoreItems) {

                    if (!mOnScrollIsLoading) {
                        mOnScrollIsLoading = true;
                        mUserScrolled = false;
                        getItems(true);
                    }
                }

            }
        });

        rootView.findViewById(R.id.filter_location).setOnClickListener(this);

        mLoadingCircle = (LinearLayout) rootView.findViewById(R.id.loading_circle_container);
        mLoadMoreProgressContainer = (RelativeLayout) rootView.findViewById(R.id.loadMoreProgressContainer);

        mLocationCriteriaTextView = (TextView) rootView.findViewById(R.id.location_criteria_text_view);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        changeFilterTitle();
        changeLocationFilterTitle();
    }


    @Override
    public String getTitle() {
        return getStringInFragment(R.string.hornama);
    }

    @Override
    public void onSelectedTabClick() {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        net.honarnama.nano.Item selectedItem = mItemsAdapter.getItem(position - 1);
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        if (selectedItem != null) {
            controlPanelActivity.displayItemPage(selectedItem.id, false);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.category_filter_btn) {
            Intent intent = new Intent(getActivity(), ChooseArtCategoryActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            getParentFragment().startActivityForResult(intent, HonarnamaBrowseApp.INTENT_CHOOSE_CATEGORY_CODE);
        }
        if (v.getId() == R.id.filter_container) {
            Intent intent = new Intent(getActivity(), ItemFilterDialogActivity.class);
            if (mMinPriceIndex > -1) {
                intent.putExtra(HonarnamaBrowseApp.EXTRA_KEY_MIN_PRICE_INDEX, mMinPriceIndex);
            }

            if (mMaxPriceIndex > -1) {
                intent.putExtra(HonarnamaBrowseApp.EXTRA_KEY_MAX_PRICE_INDEX, mMaxPriceIndex);
            }

            intent.putExtra(HonarnamaBrowseApp.EXTRA_KEY_SEARCH_TERM, mSearchTerm);

            getParentFragment().startActivityForResult(intent, HonarnamaBrowseApp.INTENT_FILTER_ITEMS_CODE);
        }

        if (v.getId() == R.id.on_error_retry_container) {
            if (NetworkManager.getInstance().isNetworkEnabled(true)) {
                setVisibilityInFragment(mLoadingCircle, View.VISIBLE);
                ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
                controlPanelActivity.refreshTopFragment();
            }
        }

        if (v.getId() == R.id.filter_location) {
            Intent intent = new Intent(getActivity(), LocationFilterDialogActivity.class);
            intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_PROVINCE_ID, mSelectedProvinceId);
            intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_CITY_ID, mSelectedCityId);
            intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_ALL_IRAN, mIsAllIranChecked);
            getParentFragment().startActivityForResult(intent, HonarnamaBrowseApp.INTENT_FILTER_ITEMS_LOCATION);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case HonarnamaBaseApp.INTENT_CHOOSE_CATEGORY_CODE:

                if (resultCode == getActivity().RESULT_OK) {

                    boolean isFilterSubCategoryRowSelected = data.getBooleanExtra(HonarnamaBaseApp.EXTRA_KEY_FILTER_SUB_CAT_ROW_SELECTED, false);
//                    ArrayList<String> subCatList = data.getStringArrayListExtra(HonarnamaBaseApp.EXTRA_KEY_SUB_CATS);

                    mSelectedCategoryName = data.getStringExtra(HonarnamaBaseApp.EXTRA_KEY_CATEGORY_NAME);
                    mCategoryFilterButton.setText(mSelectedCategoryName);
                    mSelectedCategoryId = data.getIntExtra(HonarnamaBaseApp.EXTRA_KEY_CATEGORY_ID, 0);
                    mSelectedCategoryParentId = data.getIntExtra(HonarnamaBaseApp.EXTRA_KEY_CATEGORY_PARENT_ID, 0);

//                    mSubCatList = subCatList;
                    mIsFilterSubCategoryRowSelected = isFilterSubCategoryRowSelected;
                    getItems(false);
                }
                break;

            case HonarnamaBaseApp.INTENT_FILTER_ITEMS_CODE:

                if (resultCode == getActivity().RESULT_OK) {
                    mMinPriceIndex = data.getIntExtra(HonarnamaBrowseApp.EXTRA_KEY_MIN_PRICE_INDEX, -1);
                    mMinPriceValue = data.getStringExtra(HonarnamaBrowseApp.EXTRA_KEY_MIN_PRICE_VALUE);
                    mMaxPriceIndex = data.getIntExtra(HonarnamaBrowseApp.EXTRA_KEY_MAX_PRICE_INDEX, -1);
                    mMaxPriceValue = data.getStringExtra(HonarnamaBrowseApp.EXTRA_KEY_MAX_PRICE_VALUE);
                    mIsFilterApplied = data.getBooleanExtra(HonarnamaBaseApp.EXTRA_KEY_FILTER_APPLIED, false);
                    mSearchTerm = data.getStringExtra(HonarnamaBrowseApp.EXTRA_KEY_SEARCH_TERM);
                    changeFilterTitle();
                    getItems(false);
                }
                break;

            case HonarnamaBaseApp.INTENT_FILTER_ITEMS_LOCATION:
                if (resultCode == getActivity().RESULT_OK) {
                    mSelectedProvinceId = data.getIntExtra(HonarnamaBaseApp.EXTRA_KEY_PROVINCE_ID, Province.ALL_PROVINCE_ID);
                    mSelectedProvinceName = data.getStringExtra(HonarnamaBaseApp.EXTRA_KEY_PROVINCE_NAME);
                    mSelectedCityId = data.getIntExtra(HonarnamaBaseApp.EXTRA_KEY_CITY_ID, City.ALL_CITY_ID);
                    mIsAllIranChecked = data.getBooleanExtra(HonarnamaBaseApp.EXTRA_KEY_ALL_IRAN, true);
                    changeLocationFilterTitle();
                    getItems(false);
                }
                break;
        }
    }

    private void changeFilterTitle() {
        if (mIsFilterApplied) {
            mFilterTextView.setTextColor(getResources().getColor(R.color.dark_cyan));
            setTextInFragment(mFilterTextView, getStringInFragment(R.string.filtered));
            mFilterIcon.setColor(getResources().getColor(R.color.dark_cyan));
        } else {
            mFilterTextView.setTextColor(getResources().getColor(R.color.text_color));
            setTextInFragment(mFilterTextView, getStringInFragment(R.string.filter));
            mFilterIcon.setColor(getResources().getColor(R.color.text_color));
        }
    }

    private void changeLocationFilterTitle() {
        if (mIsAllIranChecked) {
            setTextInFragment(mLocationCriteriaTextView, getStringInFragment(R.string.all_over_iran));
        } else {
            if (mSelectedCityId > 0) {
                setTextInFragment(mLocationCriteriaTextView, getStringInFragment(R.string.city) + " " + City.getCityById(mSelectedCityId).getName());
            } else if (mSelectedProvinceId > 0) {
                setTextInFragment(mLocationCriteriaTextView, getStringInFragment(R.string.province) + " " + Province.getProvinceById(mSelectedProvinceId).getName());
            } else {
                setTextInFragment(mLocationCriteriaTextView, getStringInFragment(R.string.all_over_iran));
            }
        }
    }

    public void onPreNewQuery() {
        mNextPageId = 0;
        setVisibilityInFragment(mEmptyListContainer, View.GONE);
        mItemsAdapter.setItems(null);
        mItemsAdapter.notifyDataSetChanged();
        setVisibilityInFragment(mLoadMoreProgressContainer, View.GONE);
        setVisibilityInFragment(mLoadingCircle, View.VISIBLE);
        mUserScrolled = false;
    }

    public void getItems(boolean onScroll) {
        if (!onScroll) {
            onPreNewQuery();
        }
        new getItemsAsync(onScroll).execute();
    }

    public class getItemsAsync extends AsyncTask<Void, Void, BrowseItemsReply> {
        BrowseItemsRequest browseItemsRequest;

        boolean onScroll = false;

        public getItemsAsync(boolean onScrollStateChanged) {
            super();
            this.onScroll = onScrollStateChanged;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setVisibilityInFragment(mEmptyListContainer, View.GONE);
            setVisibilityInFragment(mOnErrorRetry, View.GONE);
            if (onScroll) {
                setVisibilityInFragment(mLoadMoreProgressContainer, View.VISIBLE);
            }
        }

        @Override
        protected BrowseItemsReply doInBackground(Void... voids) {

            if (!NetworkManager.getInstance().isNetworkEnabled(false)) {
                return null;
            }

            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            browseItemsRequest = new BrowseItemsRequest();
            browseItemsRequest.requestProperties = rp;

            ArtCategoryCriteria artCategoryCriteria = new ArtCategoryCriteria();

            if (mIsFilterSubCategoryRowSelected) {
//                if (mSubCatList == null && mSubCatList.isEmpty()) {
                artCategoryCriteria.level1Id = mSelectedCategoryParentId;
//                }
            } else {
                artCategoryCriteria.level2Id = mSelectedCategoryId;
            }

            browseItemsRequest.searchTerm = mSearchTerm;

            browseItemsRequest.artCategoryCriteria = artCategoryCriteria;

            if (mMinPriceIndex > -1 && mMinPriceValue != null && !(mMinPriceValue.equals("MAX"))) {
                browseItemsRequest.minPrice = Integer.valueOf(mMinPriceValue);
            }

            if (mMaxPriceIndex > -1 && mMaxPriceValue != null && !(mMaxPriceValue.equals("MAX"))) {
                browseItemsRequest.maxPrice = Integer.valueOf(mMaxPriceValue);
            }

            LocationCriteria locationCriteria = new LocationCriteria();
            if (!mIsAllIranChecked) {
                if (mSelectedProvinceId > 0) {
                    locationCriteria.provinceId = mSelectedProvinceId;
                }

                if (mSelectedCityId > 0) {
                    locationCriteria.cityId = mSelectedCityId;
                }
            }
            browseItemsRequest.locationCriteria = locationCriteria;

            browseItemsRequest.nextPageId = mNextPageId;

            BrowseItemsReply getItemsReply;
            if (BuildConfig.DEBUG) {
                logD("Request for getting items is: " + browseItemsRequest);
            }
            try {
                BrowseServiceGrpc.BrowseServiceBlockingStub stub = GRPCUtils.getInstance().getBrowseServiceGrpc();
                getItemsReply = stub.getItems(browseItemsRequest);
                return getItemsReply;
            } catch (Exception e) {
                logE("Error running getItems request. request: " + browseItemsRequest + ". Error: " + e, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(BrowseItemsReply browseItemsReply) {
            super.onPostExecute(browseItemsReply);

            if (BuildConfig.DEBUG) {
                logD("browseItemsReply: " + browseItemsReply);
            }
            setVisibilityInFragment(mLoadingCircle, View.GONE);
            setVisibilityInFragment(mLoadMoreProgressContainer, View.GONE);
            mOnScrollIsLoading = false;
            Activity activity = getActivity();

            if (browseItemsReply != null) {
                switch (browseItemsReply.replyProperties.statusCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:
                        if (activity != null) {
                            ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) activity);
                            controlPanelActivity.displayUpgradeRequiredDialog();
                        } else {
                            displayLongToast(getStringInFragment(R.string.upgrade_to_new_version));
                        }
                        break;
                    case ReplyProperties.CLIENT_ERROR:
                        // TODO
                        break;
                    case ReplyProperties.SERVER_ERROR:
                        if (mItemsAdapter.getCount() == 0) {
                            setVisibilityInFragment(mOnErrorRetry, View.VISIBLE);
                        }
                        displayLongToast(getStringInFragment(R.string.server_error_try_again));
                        logE("Server error running getItems request. request: " + browseItemsRequest);
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        break;

                    case ReplyProperties.OK:
                        setVisibilityInFragment(mOnErrorRetry, View.GONE);
                        if (isAdded()) {
                            net.honarnama.nano.Item[] items = browseItemsReply.items;
                            ArrayList itemsList = new ArrayList();
                            for (net.honarnama.nano.Item item : items) {
                                itemsList.add(0, item);
                            }

                            if (itemsList.size() < PAGE_SIZE) {
                                mHasMoreItems = false;
                            } else {
                                mHasMoreItems = true;
                            }

                            mNextPageId = browseItemsReply.nextPageId;

                            if (onScroll) {
                                mItemsAdapter.addItems(itemsList);
                            } else {
                                mItemsAdapter.setItems(itemsList);
                            }

                            mItemsAdapter.notifyDataSetChanged();

                            if (itemsList.size() == 0) {
                                setVisibilityInFragment(mEmptyListContainer, View.VISIBLE);
                            }

                        }
                        break;
                }

            } else {
                displayShortToast(getStringInFragment(R.string.check_net_connection));
                if (mItemsAdapter.getCount() == 0) {
                    setVisibilityInFragment(mOnErrorRetry, View.VISIBLE);
                }
            }
        }
    }

}

