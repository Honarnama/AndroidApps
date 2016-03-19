package net.honarnama.browse.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.mikepenz.iconics.view.IconicsImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.adapter.ShopsParseAdapter;
import net.honarnama.browse.dialog.ShopFilterDialogActivity;
import net.honarnama.core.model.City;
import net.honarnama.core.model.Provinces;
import net.honarnama.core.model.Store;
import net.honarnama.core.utils.NetworkManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class ShopsFragment extends HonarnamaBrowseFragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    //    ShopsAdapter mAdapter;
    public static ShopsFragment mShopsFragment;
    private Tracker mTracker;
    private FragmentActivity mFragmentActivity;
    public RelativeLayout mOnErrorRetry;
    public RelativeLayout mFilterContainer;

    private String mSelectedProvinceId;
    private String mSelectedProvinceName;
    private String mSelectedCityId;

    ShopsParseAdapter mShopsParseAdapter;

    public RelativeLayout mEmptyListContainer;
    public LinearLayout mLoadingCircle;

    private boolean mIsAllIranChecked = true;
    private boolean mIsFilterApplied = false;
    private TextView mFilterTextView;
    private IconicsImageView mFilterIcon;

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

        mFilterContainer = (RelativeLayout) rootView.findViewById(R.id.filter_container);
        mFilterContainer.setOnClickListener(this);
        mFilterTextView = (TextView) rootView.findViewById(R.id.filter_text_view);
        mFilterIcon = (IconicsImageView) rootView.findViewById(R.id.filter_icon);

        mEmptyListContainer = (RelativeLayout) rootView.findViewById(R.id.no_shops_warning_container);
        mLoadingCircle = (LinearLayout) rootView.findViewById(R.id.loading_circle_container);

        mListView.setOnItemClickListener(this);
        listShops();

        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ParseObject selectedStore = (ParseObject) mShopsParseAdapter.getItem(i);
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        if (selectedStore != null) {
            controlPanelActivity.displayShopPage(selectedStore.getObjectId(), false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        changeFilterTitle();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof FragmentActivity) {
            mFragmentActivity = (FragmentActivity) context;
        }
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

            case R.id.filter_container:
                Intent intent = new Intent(getActivity(), ShopFilterDialogActivity.class);
                intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_PROVINCE_ID, mSelectedProvinceId);
                intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_CITY_ID, mSelectedCityId);
                intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_ALL_IRAN, mIsAllIranChecked);
                getParentFragment().startActivityForResult(intent, HonarnamaBrowseApp.INTENT_FILTER_SHOP_CODE);
                break;
        }
    }

    public void onSelectedTabClick() {
    }

    public void listShops() {
        ParseQueryAdapter.QueryFactory<ParseObject> filterFactory =
                new ParseQueryAdapter.QueryFactory<ParseObject>() {
                    public ParseQuery create() {

                        ParseQuery<Store> parseQuery = new ParseQuery<Store>(Store.class);
                        parseQuery.whereEqualTo(Store.STATUS, Store.STATUS_CODE_VERIFIED);
                        parseQuery.whereEqualTo(Store.VALIDITY_CHECKED, true);
                        parseQuery.include(Store.CITY);

                        if (!mIsAllIranChecked) {
                            if (!TextUtils.isEmpty(mSelectedProvinceId)) {
                                Provinces province = ParseObject.createWithoutData(Provinces.class, mSelectedProvinceId);
                                parseQuery.whereEqualTo(Store.PROVINCE, province);
                            }

                            if (!TextUtils.isEmpty(mSelectedCityId)) {
                                if (!mSelectedCityId.equals(City.ALL_CITY_ID)) {
                                    City city = ParseObject.createWithoutData(City.class, mSelectedCityId);
                                    parseQuery.whereEqualTo(Store.CITY, city);
                                }
                            }
                        }
                        return parseQuery;
                    }
                };

        mShopsParseAdapter = new ShopsParseAdapter(getContext(), filterFactory);


        mShopsParseAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener() {
            @Override
            public void onLoading() {
                mLoadingCircle.setVisibility(View.VISIBLE);
                mEmptyListContainer.setVisibility(View.GONE);
                mOnErrorRetry.setVisibility(View.GONE);
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
        });
        mListView.setAdapter(mShopsParseAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case HonarnamaBaseApp.INTENT_FILTER_SHOP_CODE:
                if (resultCode == getActivity().RESULT_OK) {
                    mSelectedProvinceId = data.getStringExtra(HonarnamaBaseApp.EXTRA_KEY_PROVINCE_ID);
                    mSelectedProvinceName = data.getStringExtra(HonarnamaBaseApp.EXTRA_KEY_PROVINCE_NAME);
                    mSelectedCityId = data.getStringExtra(HonarnamaBaseApp.EXTRA_KEY_CITY_ID);
                    mIsAllIranChecked = data.getBooleanExtra(HonarnamaBaseApp.EXTRA_KEY_ALL_IRAN, true);
                    mIsFilterApplied = data.getBooleanExtra(HonarnamaBaseApp.EXTRA_KEY_FILTER_APPLIED, false);
                    changeFilterTitle();
                    listShops();
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
            mFilterTextView.setText(getResources().getString(R.string.filter_geo));
            mFilterIcon.setColor(getResources().getColor(R.color.text_color));
        }
    }

}
