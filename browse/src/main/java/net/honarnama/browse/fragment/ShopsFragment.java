package net.honarnama.browse.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.adapter.ShopsParseAdapter;
import net.honarnama.browse.model.Item;
import net.honarnama.core.model.Store;
import net.honarnama.core.utils.NetworkManager;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
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

import java.util.List;


public class ShopsFragment extends HonarnamaBrowseFragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    //    ShopsAdapter mAdapter;
    public static ShopsFragment mShopsFragment;
    private Tracker mTracker;
    private FragmentActivity mFragmentActivity;
    ShopsParseAdapter mShopsParseAdapter;
    public RelativeLayout mOnErrorRetry;

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
        ListView listView = (ListView) rootView.findViewById(R.id.shops_listView);


        final RelativeLayout emptyListContainer = (RelativeLayout) rootView.findViewById(R.id.no_shops_warning_container);
        listView.setEmptyView(emptyListContainer);

        final LinearLayout loadingCircle = (LinearLayout) rootView.findViewById(R.id.loading_circle_container);

        mOnErrorRetry = (RelativeLayout) rootView.findViewById(R.id.on_error_retry_container);
        mOnErrorRetry.setOnClickListener(this);

//        Shop.getShopList(getActivity()).continueWith(new Continuation<List<ParseObject>, Object>() {
//            @Override
//            public Object then(Task<List<ParseObject>> task) throws Exception {
//                loadingCircle.setVisibility(View.GONE);
//                if (task.isFaulted()) {
//                    logE("Getting Shops Failed. Error: " + task.getError(), "", task.getError());
//                    if (isVisible()) {
//                        Toast.makeText(getActivity(), HonarnamaBrowseApp.getInstance().getString(R.string.error_getting_shop_lsit) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG);
//                    }
//                } else {
//                    List<ParseObject> shopList = task.getResult();
//                    mAdapter.setImages(shopList);
//                    mAdapter.notifyDataSetChanged();
//                }
//                return null;
//            }
//        });

//        mAdapter = new ShopsAdapter(getActivity());
//        listView.setAdapter(mAdapter);
//        listView.setOnItemClickListener(this);


        ParseQueryAdapter.QueryFactory<ParseObject> filterFactory =
                new ParseQueryAdapter.QueryFactory<ParseObject>() {
                    public ParseQuery create() {
                        ParseQuery<Store> parseQuery = new ParseQuery<Store>(Store.class);
                        parseQuery.whereEqualTo(Store.STATUS, Store.STATUS_CODE_VERIFIED);
                        parseQuery.whereEqualTo(Store.VALIDITY_CHECKED, true);
                        parseQuery.include(Store.CITY);
                        return parseQuery;
                    }
                };

        mShopsParseAdapter = new ShopsParseAdapter(getContext(), filterFactory);
        mShopsParseAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener() {
            @Override
            public void onLoading() {
                loadingCircle.setVisibility(View.VISIBLE);
                emptyListContainer.setVisibility(View.GONE);
                mOnErrorRetry.setVisibility(View.GONE);
            }

            @Override
            public void onLoaded(List objects, Exception e) {
                loadingCircle.setVisibility(View.GONE);
                if (e == null) {
                    if ((objects != null) && objects.size() > 0) {
                        emptyListContainer.setVisibility(View.GONE);
                    } else {
                        emptyListContainer.setVisibility(View.VISIBLE);
                    }
                } else {
                    emptyListContainer.setVisibility(View.VISIBLE);
                    if (isVisible()) {
                        Toast.makeText(getActivity(), getString(R.string.error_occured) + getString(R.string.please_check_internet_connection), Toast.LENGTH_SHORT).show();
                    }
                    mOnErrorRetry.setVisibility(View.VISIBLE);
                }
            }
        });
        listView.setAdapter(mShopsParseAdapter);
        listView.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Toast.makeText(getActivity(), "inja " + i, Toast.LENGTH_SHORT).show();
        ParseObject selectedStore = (ParseObject) mShopsParseAdapter.getItem(i);
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        if (selectedStore != null) {
            controlPanelActivity.displayShopPage(selectedStore.getObjectId(), false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        TextView toolbarTitle = (TextView) ((ControlPanelActivity) getActivity()).findViewById(R.id.toolbar_title);
//        toolbarTitle.setText(getString(R.string.shops));

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

//            case R.id.no_network_fragment_retry_icon:
//                if (NetworkManager.getInstance().isNetworkEnabled(true)) {
//                    FragmentTransaction ft = mFragmentActivity.getSupportFragmentManager().beginTransaction();
//                    ft.detach(this).attach(this).commit();
//                }
//                break;

            case R.id.on_error_retry_container:
                if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                    return;
                }
                ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
                controlPanelActivity.refreshTopFragment();
                break;
        }
    }

    public void onSelectedTabClick() {
    }
}
