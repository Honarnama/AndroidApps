package net.honarnama.browse.fragment;

import net.honarnama.GRPCUtils;
import net.honarnama.base.BuildConfig;
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
import net.honarnama.nano.LocationCriteria;
import net.honarnama.nano.ReplyProperties;
import net.honarnama.nano.RequestProperties;
import net.honarnama.nano.Store;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
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
    public EditText mSearchEditText;
    public View mSearchButton;
    public RelativeLayout mEmptyListContainer;
    public LinearLayout mLoadingCircle;


    private ToggleButton mItemsToggleButton;
    private ToggleButton mShopsToggleButton;
    private ToggleButton mEventsToggleButton;

    public RelativeLayout mOnErrorRetry;

    public SearchSegment mSearchSegment;

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

        mSearchEditText = (EditText) rootView.findViewById(R.id.serach_term);
        mSearchButton = rootView.findViewById(R.id.search_btn);

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

        mSearchButton.setOnClickListener(this);

        mItemsAdapter = new ItemsAdapter(HonarnamaBrowseApp.getInstance());
        mShopsAdapter = new ShopsAdapter(HonarnamaBrowseApp.getInstance());
        mEventsAdapter = new EventsAdapter(HonarnamaBrowseApp.getInstance());

        mOnErrorRetry = (RelativeLayout) rootView.findViewById(R.id.on_error_retry_container);
        mOnErrorRetry.setOnClickListener(this);

        mListView.setOnItemClickListener(this);

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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
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
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        if (mSearchSegment == SearchSegment.ITEMS) {
            //TODO
//            ParseObject selectedItem = (ParseObject) mItemsAdapter.getItem(position);
//            if (selectedItem != null) {
//                controlPanelActivity.displayItemPage(selectedItem.getObjectId(), false);
//            }
        }

        if (mSearchSegment == SearchSegment.SHOPS) {
            //TODO
//            ParseObject selectedShop = (ParseObject) mShopsAdapter.getItem(position);
//            if (selectedShop != null) {
//                controlPanelActivity.displayShopPage(selectedShop.getObjectId(), false);
//            }
        }

        if (mSearchSegment == SearchSegment.EVENTS) {
            //TODO
//            ParseObject selectedEvent = (ParseObject) mEventsAdapter.getItem(position);
//            if (selectedEvent != null) {
//                controlPanelActivity.displayEventPage(selectedEvent.getObjectId(), false);
//            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.search_btn) {
            if (isVisible()) {
                WindowUtil.hideKeyboard(getActivity());
            }
            msearchTerm = mSearchEditText.getText().toString().trim();
            if (TextUtils.isEmpty(msearchTerm)) {
                if (isVisible()) {
                    Toast.makeText(getActivity(), "عبارت مورد جستجو را وارد نکردید.", Toast.LENGTH_LONG).show();
                }
                return;
            }

            if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                return;
            }

            mEmptyListContainer.setVisibility(View.GONE);
            mLoadingCircle.setVisibility(View.VISIBLE);
            mListView.setEmptyView(mLoadingCircle);
            mOnErrorRetry.setVisibility(View.GONE);

            if (mItemsToggleButton.isChecked()) {
                mListView.setAdapter(mItemsAdapter);
                mSearchSegment = SearchSegment.ITEMS;
                if (!TextUtils.isEmpty(msearchTerm)) {
                    searchItems();
                }
                return;
            }
            if (mShopsToggleButton.isChecked()) {
                mListView.setAdapter(mShopsAdapter);
                mSearchSegment = SearchSegment.SHOPS;
                if (!TextUtils.isEmpty(msearchTerm)) {
                    searchShops();
                }
                return;
            }
            if (mEventsToggleButton.isChecked()) {
                mListView.setAdapter(mEventsAdapter);
                mSearchSegment = SearchSegment.EVENTS;
                if (!TextUtils.isEmpty(msearchTerm)) {
                    searchEvents();
                }
                return;
            }
        }

        if (v.getId() == R.id.on_error_retry_container) {
            if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                return;
            }
            ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
            controlPanelActivity.refreshTopFragment();
        }
    }

    public void searchItems() {
        mEmptyListContainer.setVisibility(View.GONE);
        mLoadingCircle.setVisibility(View.VISIBLE);
        mListView.setEmptyView(mLoadingCircle);
        mOnErrorRetry.setVisibility(View.GONE);
        ArrayList emptyList = new ArrayList<>();
        mItemsAdapter.setItems(emptyList);
        mItemsAdapter.notifyDataSetChanged();
//TODO
//        Item.search(msearchTerm).continueWith(new Continuation<List<Item>, Object>() {
//            @Override
//            public Object then(Task<List<Item>> task) throws Exception {
//                mLoadingCircle.setVisibility(View.GONE);
//                mEmptyListContainer.setVisibility(View.VISIBLE);
//                mListView.setEmptyView(mEmptyListContainer);
//
//                if (task.isFaulted() && ((ParseException) task.getError()).getCode() != ParseException.OBJECT_NOT_FOUND) {
//                    logE("Searching items with search term" + msearchTerm + " failed. Error: " + task.getError(), "", task.getError());
//                    if (isVisible()) {
//                        Toast.makeText(getActivity(), HonarnamaBrowseApp.getInstance().getString(R.string.error_getting_items_list) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
//                    }
//                    mOnErrorRetry.setVisibility(View.VISIBLE);
//                } else {
//
//                    mOnErrorRetry.setVisibility(View.GONE);
//                    List<Item> foundItems = task.getResult();
//                    mItemsAdapter.setItems(foundItems);
//                    mItemsAdapter.notifyDataSetChanged();
//                }
//                return null;
//            }
//        });
        new searchItemsAsync().execute();

    }

    public void searchShops() {
        mEmptyListContainer.setVisibility(View.GONE);
        mLoadingCircle.setVisibility(View.VISIBLE);
        mListView.setEmptyView(mLoadingCircle);
        mOnErrorRetry.setVisibility(View.GONE);
        List<Store> emptyList = new ArrayList<>();
        mShopsAdapter.setShops(emptyList);
        mShopsAdapter.notifyDataSetChanged();

//        Shop.search(msearchTerm).continueWith(new Continuation<List<Store>, Object>() {
//            @Override
//            public Object then(Task<List<Store>> task) throws Exception {
//
//                mLoadingCircle.setVisibility(View.GONE);
//                mEmptyListContainer.setVisibility(View.VISIBLE);
//                mListView.setEmptyView(mEmptyListContainer);
//
//                if (task.isFaulted() && ((ParseException) task.getError()).getCode() != ParseException.OBJECT_NOT_FOUND) {
//                    logE("Searching shops with search term" + msearchTerm + " failed. Error: " + task.getError(), task.getError());
//                    if (isVisible()) {
//                        Toast.makeText(getActivity(), HonarnamaBrowseApp.getInstance().getString(R.string.error_getting_shop_list) + getString(R.string.check_net_connection), Toast.LENGTH_LONG).show();
//                    }
//                    mOnErrorRetry.setVisibility(View.VISIBLE);
//                } else {
//                    mOnErrorRetry.setVisibility(View.GONE);
//                    List<Store> foundItems = task.getResult();
//                    mShopsAdapter.setShops(foundItems);
//                    mShopsAdapter.notifyDataSetChanged();
//                }
//                return null;
//            }
//        });

        new searchShopsAsync().execute();
    }

    public void searchEvents() {

        mEmptyListContainer.setVisibility(View.GONE);
        mLoadingCircle.setVisibility(View.VISIBLE);
        mListView.setEmptyView(mLoadingCircle);
        mOnErrorRetry.setVisibility(View.GONE);
        List<Event> emptyList = new ArrayList<>();
        mEventsAdapter.setEvents(emptyList);
        mEventsAdapter.notifyDataSetChanged();

        //TODO
//        Event.search(msearchTerm).continueWith(new Continuation<List<Event>, Object>() {
//            @Override
//            public Object then(Task<List<Event>> task) throws Exception {
//                mLoadingCircle.setVisibility(View.GONE);
//                mEmptyListContainer.setVisibility(View.VISIBLE);
//                mListView.setEmptyView(mEmptyListContainer);
//
//                if (task.isFaulted() && ((ParseException) task.getError()).getCode() != ParseException.OBJECT_NOT_FOUND) {
//                    logE("Searching events with search term" + msearchTerm + " failed. Error: " + task.getError(), "", task.getError());
//                    if (isVisible()) {
//                        Toast.makeText(getActivity(), HonarnamaBrowseApp.getInstance().getString(R.string.error_getting_event_list) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
//                    }
//                    mOnErrorRetry.setVisibility(View.VISIBLE);
//                } else {
//                    mOnErrorRetry.setVisibility(View.GONE);
//                    List<Event> foundItems = task.getResult();
//                    mEventsAdapter.setEvents(foundItems);
//                    mEventsAdapter.notifyDataSetChanged();
//                }
//                return null;
//            }
//        });

        new searchEventsAsync().execute();
    }

    public void resetFields() {
//        if (mSearchEditText != null) {
//            mSearchEditText.setText("");
//            mItemsToggleButton.setChecked(true);
//            mShopsToggleButton.setChecked(false);
//            mEventsToggleButton.setChecked(false);
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
        }

        @Override
        protected BrowseItemsReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            browseItemsRequest = new BrowseItemsRequest();
            browseItemsRequest.requestProperties = rp;

            browseItemsRequest.searchTerm = msearchTerm;

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

            mLoadingCircle.setVisibility(View.GONE);
            mEmptyListContainer.setVisibility(View.VISIBLE);
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
                        mEmptyListContainer.setVisibility(View.VISIBLE);
                        mItemsAdapter.notifyDataSetChanged();
                        mOnErrorRetry.setVisibility(View.VISIBLE);
                        displayLongToast(getStringInFragment(R.string.server_error_try_again));
                        logE("Server error searching items. request: " + browseItemsRequest);
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        break;

                    case ReplyProperties.OK:
                        mOnErrorRetry.setVisibility(View.GONE);
                        if (isAdded()) {
                            net.honarnama.nano.Item[] items = browseItemsReply.items;
                            ArrayList itemsList = new ArrayList();
                            for (net.honarnama.nano.Item item : items) {
                                itemsList.add(0, item);
                            }

                            if (itemsList.size() == 0) {
                                mEmptyListContainer.setVisibility(View.VISIBLE);
                            }
                            mItemsAdapter.setItems(itemsList);
                            mItemsAdapter.notifyDataSetChanged();
                        }
                        break;
                }

            } else {
                if (isAdded()) {
                    mItemsAdapter.setItems(null);
                    mEmptyListContainer.setVisibility(View.VISIBLE);
                    mItemsAdapter.notifyDataSetChanged();
                    mOnErrorRetry.setVisibility(View.VISIBLE);
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
        }

        @Override
        protected BrowseStoresReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            browseStoresRequest = new BrowseStoresRequest();
            browseStoresRequest.requestProperties = rp;

            browseStoresRequest.searchTerm = msearchTerm;

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
        }

        @Override
        protected BrowseEventsReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            browseEventsRequest = new BrowseEventsRequest();
            browseEventsRequest.requestProperties = rp;

            browseEventsRequest.searchTerm = msearchTerm;

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

}

