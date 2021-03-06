package net.honarnama.browse.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.mikepenz.iconics.view.IconicsImageView;

import net.honarnama.GRPCUtils;
import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.model.City;
import net.honarnama.base.model.Province;
import net.honarnama.base.utils.NetworkManager;
import net.honarnama.base.utils.WindowUtil;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.adapter.EventsAdapter;
import net.honarnama.browse.adapter.ItemsAdapter;
import net.honarnama.browse.adapter.ShopsAdapter;
import net.honarnama.nano.BrowseEventsReply;
import net.honarnama.nano.BrowseEventsRequest;
import net.honarnama.nano.BrowseItemsReply;
import net.honarnama.nano.BrowseItemsRequest;
import net.honarnama.nano.BrowseServiceGrpc;
import net.honarnama.nano.BrowseStoresReply;
import net.honarnama.nano.BrowseStoresRequest;
import net.honarnama.nano.Event;
import net.honarnama.nano.Item;
import net.honarnama.nano.LocationCriteria;
import net.honarnama.nano.ReplyProperties;
import net.honarnama.nano.RequestProperties;
import net.honarnama.nano.Store;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

/**
 * Created by elnaz on 2/11/16.
 */
public class SearchFragment extends HonarnamaBrowseFragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    public static SearchFragment mSearchFragment;
    private ListView mListView;

    ItemsAdapter mItemsAdapter;
    ShopsAdapter mShopsAdapter;
    EventsAdapter mEventsAdapter;

    String msearchTerm;
    //    public EditText mSearchEditText;
//    public View mSearchButton;
    public RelativeLayout mEmptyListContainer;
    public LinearLayout mLoadingCircle;


    private ToggleButton mItemsToggleButton;
    private ToggleButton mShopsToggleButton;
    private ToggleButton mEventsToggleButton;

    public RelativeLayout mOnErrorRetry;
    public SearchSegment mSearchSegment;

    private boolean mIsAllIranChecked = true;
    public RelativeLayout mFilterContainer;
    private boolean mIsFilterApplied = false;
    private TextView mFilterTextView;
    private IconicsImageView mFilterIcon;
    private int mSelectedProvinceId = -1;
    private String mSelectedProvinceName;
    private int mSelectedCityId = -1;

    Tracker mTracker;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTracker = HonarnamaBrowseApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("SearchFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public synchronized static SearchFragment getInstance() {
        if (mSearchFragment == null) {
            mSearchFragment = new SearchFragment();
        }
        return mSearchFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

//        mSearchEditText = (EditText) rootView.findViewById(R.id.serach_term);
//        mSearchButton = rootView.findViewById(R.id.search_btn);

        mFilterContainer = (RelativeLayout) rootView.findViewById(R.id.filter_container);
        mFilterContainer.setOnClickListener(this);
        mFilterTextView = (TextView) rootView.findViewById(R.id.filter_text_view);
        mFilterIcon = (IconicsImageView) rootView.findViewById(R.id.filter_icon);

        mItemsToggleButton = (ToggleButton) rootView.findViewById(R.id.items_tg_btn);
        mShopsToggleButton = (ToggleButton) rootView.findViewById(R.id.shops_tg_btn);
        mEventsToggleButton = (ToggleButton) rootView.findViewById(R.id.events_tg_btn);
        mItemsToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemsToggleButton.setChecked(true);
                mShopsToggleButton.setChecked(false);
                mEventsToggleButton.setChecked(false);
            }
        });
        mShopsToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShopsToggleButton.setChecked(true);
                mItemsToggleButton.setChecked(false);
                mEventsToggleButton.setChecked(false);
            }
        });

        mEventsToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventsToggleButton.setChecked(true);
                mItemsToggleButton.setChecked(false);
                mShopsToggleButton.setChecked(false);
            }
        });

        mEmptyListContainer = (RelativeLayout) rootView.findViewById(R.id.empty_list_container);
        mLoadingCircle = (LinearLayout) rootView.findViewById(R.id.loading_circle_container);
        mListView = (ListView) rootView.findViewById(R.id.listView);

//        mSearchButton.setOnClickListener(this);

        mItemsAdapter = new ItemsAdapter(HonarnamaBrowseApp.getInstance(), this);
        mShopsAdapter = new ShopsAdapter(HonarnamaBrowseApp.getInstance());
        mEventsAdapter = new EventsAdapter(HonarnamaBrowseApp.getInstance());

        mOnErrorRetry = (RelativeLayout) rootView.findViewById(R.id.on_error_retry_container);
        mOnErrorRetry.setOnClickListener(this);

        mListView.setOnItemClickListener(this);

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSearchSegment != null) {
            if (mSearchSegment == SearchSegment.ITEMS) {
                mItemsToggleButton.setChecked(true);
                mShopsToggleButton.setChecked(false);
                mEventsToggleButton.setChecked(false);
                mListView.setAdapter(mItemsAdapter);
                searchItems();
            } else if (mSearchSegment == SearchSegment.SHOPS) {
                mShopsToggleButton.setChecked(true);
                mItemsToggleButton.setChecked(false);
                mEventsToggleButton.setChecked(false);
                mListView.setAdapter(mShopsAdapter);
                searchShops();
            } else {
                if (mSearchSegment == SearchSegment.EVENTS) {
                    mEventsToggleButton.setChecked(true);
                    mItemsToggleButton.setChecked(false);
                    mShopsToggleButton.setChecked(false);
                    mListView.setAdapter(mEventsAdapter);
                    searchEvents();
                }
            }
        }
//        mSearchEditText.requestFocus();
//        InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//        mgr.showSoftInput(mSearchEditText, InputMethodManager.SHOW_IMPLICIT);
        getActivity().invalidateOptionsMenu();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case HonarnamaBaseApp.INTENT_FILTER_SHOPS_CODE:
                if (resultCode == getActivity().RESULT_OK) {
                    mSelectedProvinceId = data.getIntExtra(HonarnamaBaseApp.EXTRA_KEY_PROVINCE_ID, Province.ALL_PROVINCE_ID);
                    mSelectedProvinceName = data.getStringExtra(HonarnamaBaseApp.EXTRA_KEY_PROVINCE_NAME);
                    mSelectedCityId = data.getIntExtra(HonarnamaBaseApp.EXTRA_KEY_CITY_ID, City.ALL_CITY_ID);
                    mIsAllIranChecked = data.getBooleanExtra(HonarnamaBaseApp.EXTRA_KEY_ALL_IRAN, true);
                    mIsFilterApplied = data.getBooleanExtra(HonarnamaBaseApp.EXTRA_KEY_FILTER_APPLIED, false);
                    changeFilterTitle();
                    search();
                }
                break;
        }
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
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        if (mSearchSegment == SearchSegment.ITEMS) {
            Item selectedItem = mItemsAdapter.getItem(position);
            if (selectedItem != null) {
                controlPanelActivity.displayItemPage(selectedItem.id, false);
            }
        }

        if (mSearchSegment == SearchSegment.SHOPS) {
            Store selectedShop = mShopsAdapter.getItem(position);
            if (selectedShop != null) {
                controlPanelActivity.displayShopPage(selectedShop.id, false);
            }
        }

        if (mSearchSegment == SearchSegment.EVENTS) {
            Event selectedEvent = mEventsAdapter.getItem(position);
            if (selectedEvent != null) {
                controlPanelActivity.displayEventPage(selectedEvent.id, false);
            }
        }
    }

    @Override
    public void onClick(View v) {
//        if (v.getId() == R.id.search_btn) {
//            search();
//        }

        if (v.getId() == R.id.on_error_retry_container) {
            if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                return;
            }
            ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
            controlPanelActivity.refreshTopFragment();
        }

//        if (v.getId() == R.id.filter_container) {
//            Intent intent = new Intent(getActivity(), ShopFilterDialogActivity.class);
//            intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_PROVINCE_ID, mSelectedProvinceId);
//            intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_CITY_ID, mSelectedCityId);
//            intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_ALL_IRAN, mIsAllIranChecked);
//            getParentFragment().startActivityForResult(intent, HonarnamaBrowseApp.INTENT_FILTER_SHOP_CODE);
//        }
    }

    public void searchItems() {
        if (TextUtils.isEmpty(msearchTerm)) {
            return;
        }

        setVisibilityInFragment(mEmptyListContainer, View.GONE);
        setVisibilityInFragment(mLoadingCircle, View.VISIBLE);
        mListView.setEmptyView(mLoadingCircle);
        setVisibilityInFragment(mOnErrorRetry, View.GONE);
        ArrayList emptyList = new ArrayList<>();
        mItemsAdapter.setItems(emptyList);
        mItemsAdapter.notifyDataSetChanged();
        new searchItemsAsync().execute();
    }

    public void searchShops() {
        if (TextUtils.isEmpty(msearchTerm)) {
            return;
        }
        setVisibilityInFragment(mEmptyListContainer, View.GONE);
        setVisibilityInFragment(mLoadingCircle, View.VISIBLE);
        mListView.setEmptyView(mLoadingCircle);
        setVisibilityInFragment(mOnErrorRetry, View.GONE);
        List<Store> emptyList = new ArrayList<>();
        mShopsAdapter.setShops(emptyList);
        mShopsAdapter.notifyDataSetChanged();
        new searchShopsAsync().execute();
    }

    public void searchEvents() {
        if (TextUtils.isEmpty(msearchTerm)) {
            return;
        }
        setVisibilityInFragment(mEmptyListContainer, View.GONE);
        setVisibilityInFragment(mLoadingCircle, View.VISIBLE);
        mListView.setEmptyView(mLoadingCircle);
        setVisibilityInFragment(mOnErrorRetry, View.GONE);
        List<Event> emptyList = new ArrayList<>();
        mEventsAdapter.setEvents(emptyList);
        mEventsAdapter.notifyDataSetChanged();
        new searchEventsAsync().execute();
    }

    public void resetFields() {
//        if (mSearchEditText != null) {
//            mSearchEditText.setText("");
        setCheckedInFragment(mItemsToggleButton, true);
        setCheckedInFragment(mShopsToggleButton, false);
        setCheckedInFragment(mEventsToggleButton, false);
        msearchTerm = "";
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public enum SearchSegment {
        ITEMS("items", 0),
        SHOPS("shops", 1),
        EVENTS("events", 2);

        private String stringValue;
        private int intValue;

        private SearchSegment(String toString, int value) {
            stringValue = toString;
            intValue = value;
        }
    }

    public class searchItemsAsync extends AsyncTask<Void, Void, BrowseItemsReply> {
        BrowseItemsRequest browseItemsRequest;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("searchItems")
                    .build());
        }

        @Override
        protected BrowseItemsReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            browseItemsRequest = new BrowseItemsRequest();
            browseItemsRequest.requestProperties = rp;

            browseItemsRequest.searchTerm = msearchTerm;

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

            BrowseItemsReply getItemsReply;
            if (BuildConfig.DEBUG) {
                logD("Request for searching items is: " + browseItemsRequest);
            }
            try {
                BrowseServiceGrpc.BrowseServiceBlockingStub stub = GRPCUtils.getInstance().getBrowseServiceGrpc();
                getItemsReply = stub.getItems(browseItemsRequest);
                return getItemsReply;
            } catch (Exception e) {
                logE("Error running getItems in search fragment request. request: " + browseItemsRequest + ". Error: " + e, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(BrowseItemsReply browseItemsReply) {
            super.onPostExecute(browseItemsReply);

            setVisibilityInFragment(mLoadingCircle, View.GONE);
            setVisibilityInFragment(mEmptyListContainer, View.VISIBLE);
            mListView.setEmptyView(mEmptyListContainer);

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
                        mItemsAdapter.setItems(null);
                        setVisibilityInFragment(mEmptyListContainer, View.VISIBLE);
                        mItemsAdapter.notifyDataSetChanged();
                        setVisibilityInFragment(mOnErrorRetry, View.VISIBLE);
                        displayLongToast(getStringInFragment(R.string.server_error_try_again));
                        logE("Server error searching items. request: " + browseItemsRequest);
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

                            if (itemsList.size() == 0) {
                                setVisibilityInFragment(mEmptyListContainer, View.VISIBLE);
                            }
                            mItemsAdapter.setItems(itemsList);
                            mItemsAdapter.notifyDataSetChanged();
                        }
                        break;
                }

            } else {
                if (isAdded()) {
                    mItemsAdapter.setItems(null);
                    setVisibilityInFragment(mEmptyListContainer, View.VISIBLE);
                    mItemsAdapter.notifyDataSetChanged();
                    setVisibilityInFragment(mOnErrorRetry, View.VISIBLE);
                    displayLongToast(getStringInFragment(R.string.check_net_connection));
                }
            }
        }
    }

    public class searchShopsAsync extends AsyncTask<Void, Void, BrowseStoresReply> {
        BrowseStoresRequest browseStoresRequest;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("searchShops")
                    .build());

        }

        @Override
        protected BrowseStoresReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            browseStoresRequest = new BrowseStoresRequest();
            browseStoresRequest.requestProperties = rp;

            browseStoresRequest.searchTerm = msearchTerm;

            LocationCriteria locationCriteria = new LocationCriteria();
            if (!mIsAllIranChecked) {
                if (mSelectedProvinceId > 0) {
                    locationCriteria.provinceId = mSelectedProvinceId;
                }

                if (mSelectedCityId > 0) {
                    locationCriteria.cityId = mSelectedCityId;
                }
            }
            browseStoresRequest.locationCriteria = locationCriteria;

            BrowseStoresReply browseStoresReply;
            if (BuildConfig.DEBUG) {
                logD("Request for searching stores is: " + browseStoresRequest);
            }
            try {
                BrowseServiceGrpc.BrowseServiceBlockingStub stub = GRPCUtils.getInstance().getBrowseServiceGrpc();
                browseStoresReply = stub.getStores(browseStoresRequest);
                return browseStoresReply;
            } catch (Exception e) {
                logE("Error running getStores request in search fragment. request: " + browseStoresRequest + ". Error: " + e, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(BrowseStoresReply browseStoresReply) {
            super.onPostExecute(browseStoresReply);

            setVisibilityInFragment(mLoadingCircle, View.GONE);
            setVisibilityInFragment(mEmptyListContainer, View.VISIBLE);
            if (mListView != null) {
                mListView.setEmptyView(mEmptyListContainer);
            }

            Activity activity = getActivity();

            if (browseStoresReply != null) {
                switch (browseStoresReply.replyProperties.statusCode) {
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
                        mShopsAdapter.setShops(null);
                        setVisibilityInFragment(mEmptyListContainer, View.VISIBLE);
                        mShopsAdapter.notifyDataSetChanged();
                        setVisibilityInFragment(mOnErrorRetry, View.VISIBLE);
                        displayLongToast(getStringInFragment(R.string.server_error_try_again));
                        logE("Server Error searching shops. request: " + browseStoresRequest);
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        break;

                    case ReplyProperties.OK:
                        setVisibilityInFragment(mOnErrorRetry, View.GONE);
                        if (isAdded()) {
                            net.honarnama.nano.Store[] stores = browseStoresReply.stores;
                            ArrayList shopsList = new ArrayList();
                            for (net.honarnama.nano.Store store : stores) {
                                shopsList.add(0, store);
                            }
                            if (shopsList.size() == 0) {
                                setVisibilityInFragment(mEmptyListContainer, View.VISIBLE);
                            }
                            mShopsAdapter.setShops(shopsList);
                            mShopsAdapter.notifyDataSetChanged();
                        }
                        break;
                }

            } else {
                mShopsAdapter.setShops(null);
                setVisibilityInFragment(mEmptyListContainer, View.VISIBLE);
                mShopsAdapter.notifyDataSetChanged();
                setVisibilityInFragment(mOnErrorRetry, View.VISIBLE);
                displayLongToast(getStringInFragment(R.string.check_net_connection));
            }
        }
    }

    public class searchEventsAsync extends AsyncTask<Void, Void, BrowseEventsReply> {
        BrowseEventsRequest browseEventsRequest;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("searchEvents")
                    .build());
        }

        @Override
        protected BrowseEventsReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            browseEventsRequest = new BrowseEventsRequest();
            browseEventsRequest.requestProperties = rp;

            browseEventsRequest.searchTerm = msearchTerm;

            LocationCriteria locationCriteria = new LocationCriteria();
            if (!mIsAllIranChecked) {
                if (mSelectedProvinceId > 0) {
                    locationCriteria.provinceId = mSelectedProvinceId;
                }

                if (mSelectedCityId > 0) {
                    locationCriteria.cityId = mSelectedCityId;
                }
            }
            browseEventsRequest.locationCriteria = locationCriteria;

            BrowseEventsReply browseEventsReply;
            if (BuildConfig.DEBUG) {
                logD("Request for searching events is: " + browseEventsRequest);
            }
            try {
                BrowseServiceGrpc.BrowseServiceBlockingStub stub = GRPCUtils.getInstance().getBrowseServiceGrpc();
                browseEventsReply = stub.getEvents(browseEventsRequest);
                return browseEventsReply;
            } catch (Exception e) {
                logE("Error searching events request. request: " + browseEventsRequest + ". Error: " + e, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(BrowseEventsReply browseEventsReply) {
            super.onPostExecute(browseEventsReply);

            setVisibilityInFragment(mLoadingCircle, View.GONE);
            setVisibilityInFragment(mEmptyListContainer, View.VISIBLE);
            mListView.setEmptyView(mEmptyListContainer);

            Activity activity = getActivity();

            if (browseEventsReply != null) {
                switch (browseEventsReply.replyProperties.statusCode) {
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
                        mEventsAdapter.setEvents(null);
                        setVisibilityInFragment(mEmptyListContainer, View.VISIBLE);
                        mEventsAdapter.notifyDataSetChanged();
                        setVisibilityInFragment(mOnErrorRetry, View.VISIBLE);
                        displayLongToast(getStringInFragment(R.string.server_error_try_again));
                        logE("Server error searching events. request: " + browseEventsRequest);
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        break;

                    case ReplyProperties.OK:
                        setVisibilityInFragment(mOnErrorRetry, View.GONE);
                        if (isAdded()) {
                            net.honarnama.nano.Event[] events = browseEventsReply.events;
                            ArrayList eventsList = new ArrayList();
                            for (net.honarnama.nano.Event event : events) {
                                eventsList.add(0, event);
                            }
                            if (eventsList.size() == 0) {
                                setVisibilityInFragment(mEmptyListContainer, View.VISIBLE);
                            }
                            mEventsAdapter.setEvents(eventsList);
                            mEventsAdapter.notifyDataSetChanged();
                        }
                        break;
                }

            } else {
                mEventsAdapter.setEvents(null);
                setVisibilityInFragment(mEmptyListContainer, View.VISIBLE);
                mEventsAdapter.notifyDataSetChanged();
                setVisibilityInFragment(mOnErrorRetry, View.VISIBLE);
                displayLongToast(getStringInFragment(R.string.check_net_connection));
            }
        }
    }

    private void changeFilterTitle() {
        if (mIsFilterApplied) {
            mFilterTextView.setTextColor(getResources().getColor(R.color.dark_cyan));
            mFilterTextView.setText(R.string.change_filter);
            mFilterIcon.setColor(getResources().getColor(R.color.dark_cyan));
        } else {
            mFilterTextView.setTextColor(getResources().getColor(R.color.text_color));
            mFilterTextView.setText(getStringInFragment(R.string.filter_geo));
            mFilterIcon.setColor(getResources().getColor(R.color.text_color));
        }
    }

    public void search() {
        if (isVisible()) {
            WindowUtil.hideKeyboard(getActivity());
        }
        if (TextUtils.isEmpty(msearchTerm)) {
            displayLongToast(getStringInFragment(R.string.enter_search_term));
            return;
        }

        if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
            return;
        }

        setVisibilityInFragment(mEmptyListContainer, View.GONE);
        setVisibilityInFragment(mLoadingCircle, View.VISIBLE);
        mListView.setEmptyView(mLoadingCircle);
        setVisibilityInFragment(mOnErrorRetry, View.GONE);

        if (mItemsToggleButton.isChecked()) {
            mListView.setAdapter(mItemsAdapter);
            mSearchSegment = SearchSegment.ITEMS;
            searchItems();
            return;
        }
        if (mShopsToggleButton.isChecked()) {
            mListView.setAdapter(mShopsAdapter);
            mSearchSegment = SearchSegment.SHOPS;
            searchShops();
            return;
        }
        if (mEventsToggleButton.isChecked()) {
            mListView.setAdapter(mEventsAdapter);
            mSearchSegment = SearchSegment.EVENTS;
            searchEvents();
            return;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menu.clear();
        inflater.inflate(R.menu.menu_search_fragment, menu);

        final MenuItem menuItem = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) menuItem.getActionView();

        if (menuItem == null) {
            return;
        }

        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menuItem.setVisible(true);

        searchView.setQueryHint(getStringInFragment(R.string.enter_search_term));
        searchView.setIconifiedByDefault(false);

        View searchBtn = searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
        final EditText searchET = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchBtn.setClickable(true);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msearchTerm = searchET.getText().toString().trim();
                search();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Toast like print
                msearchTerm = query.toString().trim();
                search();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }
}

