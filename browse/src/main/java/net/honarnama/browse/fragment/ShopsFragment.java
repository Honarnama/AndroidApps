package net.honarnama.browse.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.honarnama.GRPCUtils;
import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.model.City;
import net.honarnama.base.model.Province;
import net.honarnama.base.utils.NetworkManager;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.adapter.ShopsAdapter;
import net.honarnama.browse.dialog.LocationFilterDialogActivity;
import net.honarnama.nano.BrowseServiceGrpc;
import net.honarnama.nano.BrowseStoresReply;
import net.honarnama.nano.BrowseStoresRequest;
import net.honarnama.nano.LocationCriteria;
import net.honarnama.nano.ReplyProperties;
import net.honarnama.nano.RequestProperties;
import net.honarnama.nano.Store;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

public class ShopsFragment extends HonarnamaBrowseFragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    //    ShopsAdapter mAdapter;
    public static ShopsFragment mShopsFragment;
    private Tracker mTracker;
    public RelativeLayout mOnErrorRetry;

    ShopsAdapter mShopsAdapter;

    public RelativeLayout mEmptyListContainer;
    public LinearLayout mLoadingCircle;

    private TextView mLocationCriteriaTextView;
    private int mSelectedProvinceId = -1;
    private String mSelectedProvinceName;
    private int mSelectedCityId = -1;
    private boolean mIsAllIranChecked = true;

    private ListView mListView;

    @Override
    public String getTitle(Context context) {
        return getString(R.string.hornama);
    }

    public synchronized static ShopsFragment getInstance() {
        if (mShopsFragment == null) {
            mShopsFragment = new ShopsFragment();
        }
        return mShopsFragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTracker = HonarnamaBrowseApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("ShopsFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_shops, container, false);
        mListView = (ListView) rootView.findViewById(R.id.shops_listView);

        mOnErrorRetry = (RelativeLayout) rootView.findViewById(R.id.on_error_retry_container);
        mOnErrorRetry.setOnClickListener(this);

        rootView.findViewById(R.id.filter_location).setOnClickListener(this);

        mLocationCriteriaTextView = (TextView) rootView.findViewById(R.id.location_criteria_text_view);

        mEmptyListContainer = (RelativeLayout) rootView.findViewById(R.id.no_shops_warning_container);
        mLoadingCircle = (LinearLayout) rootView.findViewById(R.id.loading_circle_container);

        mListView.setOnItemClickListener(this);

        listShops();

        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Store selectedStore = mShopsAdapter.getItem(i);
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        if (selectedStore != null) {
            controlPanelActivity.displayShopPage(selectedStore.id, false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        changeLocationFilterTitle();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//
//        if (context instanceof FragmentActivity) {
//            mFragmentActivity = (FragmentActivity) context;
//        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.on_error_retry_container:
                if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                    return;
                }
                ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
                controlPanelActivity.refreshTopFragment();
                break;

            case R.id.filter_location:
                Intent intent = new Intent(getActivity(), LocationFilterDialogActivity.class);
                intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_PROVINCE_ID, mSelectedProvinceId);
                intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_CITY_ID, mSelectedCityId);
                intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_ALL_IRAN, mIsAllIranChecked);
                getParentFragment().startActivityForResult(intent, HonarnamaBrowseApp.INTENT_FILTER_SHOPS_LOCATION);
                break;
        }
    }

    public void onSelectedTabClick() {
    }

    public void listShops() {
        new getShopsAsync().execute();
        mShopsAdapter = new ShopsAdapter(getContext());
        mListView.setAdapter(mShopsAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case HonarnamaBaseApp.INTENT_FILTER_SHOPS_LOCATION:
                if (resultCode == getActivity().RESULT_OK) {
                    mSelectedProvinceId = data.getIntExtra(HonarnamaBaseApp.EXTRA_KEY_PROVINCE_ID, Province.ALL_PROVINCE_ID);
                    mSelectedProvinceName = data.getStringExtra(HonarnamaBaseApp.EXTRA_KEY_PROVINCE_NAME);
                    mSelectedCityId = data.getIntExtra(HonarnamaBaseApp.EXTRA_KEY_CITY_ID, City.ALL_CITY_ID);
                    mIsAllIranChecked = data.getBooleanExtra(HonarnamaBaseApp.EXTRA_KEY_ALL_IRAN, true);
                    changeLocationFilterTitle();
                    listShops();
                }
                break;
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

    public class getShopsAsync extends AsyncTask<Void, Void, BrowseStoresReply> {
        BrowseStoresRequest browseStoresRequest;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setVisibilityInFragment(mEmptyListContainer, View.GONE);
            setVisibilityInFragment(mOnErrorRetry, View.GONE);
            setVisibilityInFragment(mLoadingCircle, View.VISIBLE);
        }

        @Override
        protected BrowseStoresReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            browseStoresRequest = new BrowseStoresRequest();
            browseStoresRequest.requestProperties = rp;

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

            if (BuildConfig.DEBUG) {
                logD("Request for getting shops is: " + browseStoresRequest);
            }
            try {
                BrowseServiceGrpc.BrowseServiceBlockingStub stub = GRPCUtils.getInstance().getBrowseServiceGrpc();
                return stub.getStores(browseStoresRequest);
            } catch (Exception e) {
                logE("Error running getshops request. request: " + browseStoresRequest + ". Error: " + e, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(BrowseStoresReply browseStoresReply) {
            super.onPostExecute(browseStoresReply);

            setVisibilityInFragment(mLoadingCircle, View.GONE);

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
                        logE("Server error running getStores request. request: " + browseStoresRequest);
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

}
