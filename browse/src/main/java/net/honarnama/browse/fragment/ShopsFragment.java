package net.honarnama.browse.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;

import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.adapter.ItemsParseAdapter;
import net.honarnama.browse.adapter.ShopsAdapter;
import net.honarnama.browse.adapter.ShopsParseAdapter;
import net.honarnama.browse.model.Shop;
import net.honarnama.core.utils.NetworkManager;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.security.acl.Owner;
import java.util.List;

import bolts.Continuation;
import bolts.Task;


public class ShopsFragment extends HonarnamaBrowseFragment implements AdapterView.OnItemClickListener, View.OnClickListener {

//    ShopsAdapter mAdapter;
    public static ShopsFragment mShopsFragment;
    private Tracker mTracker;
    public ImageView mRetryIcon;
    private FragmentActivity mFragmentActivity;
    ShopsParseAdapter mShopsParseAdapter;


    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.shops);
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

        if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
            final View rootView = inflater.inflate(R.layout.fragment_no_network, container, false);
            mRetryIcon = (ImageView) rootView.findViewById(R.id.no_network_fragment_retry_icon);
            mRetryIcon.setOnClickListener(this);
            return rootView;
        }

        final View rootView = inflater.inflate(R.layout.fragment_shops, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.shops_listView);

        final RelativeLayout emptyListContainer = (RelativeLayout) rootView.findViewById(R.id.no_shops_warning_container);
        listView.setEmptyView(emptyListContainer);

        final LinearLayout loadingCircle = (LinearLayout) rootView.findViewById(R.id.loading_circle_container);

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
//                    mAdapter.addAll(shopList);
//                    mAdapter.notifyDataSetChanged();
//                }
//                return null;
//            }
//        });

//        mAdapter = new ShopsAdapter(getActivity());
//        listView.setAdapter(mAdapter);
//        listView.setOnItemClickListener(this);

        mShopsParseAdapter = new ShopsParseAdapter(HonarnamaBrowseApp.getInstance());
        mShopsParseAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener() {
            @Override
            public void onLoading() {
                loadingCircle.setVisibility(View.VISIBLE);
                emptyListContainer.setVisibility(View.GONE);
            }

            @Override
            public void onLoaded(List objects, Exception e) {
                loadingCircle.setVisibility(View.GONE);

                if (mShopsParseAdapter.isEmpty()) {
                    emptyListContainer.setVisibility(View.VISIBLE);
                }
            }
        });
        listView.setAdapter(mShopsParseAdapter);
        listView.setOnItemClickListener(this);
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ParseObject selectedStore = (ParseObject) mShopsParseAdapter.getItem(i);
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        controlPanelActivity.displayShopPage(selectedStore.getObjectId(), false);
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

            case R.id.no_network_fragment_retry_icon:
                if (NetworkManager.getInstance().isNetworkEnabled(true)) {
                    FragmentTransaction ft = mFragmentActivity.getSupportFragmentManager().beginTransaction();
                    ft.detach(this).attach(this).commit();
                }
                break;
        }
    }

    public void onSelectedTabClick() {
    }
}
