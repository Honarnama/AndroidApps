package net.honarnama.browse.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;

import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.adapter.EventsParseAdapter;
import net.honarnama.browse.adapter.ShopsParseAdapter;
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

import java.util.List;


public class EventsFragment extends HonarnamaBrowseFragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    //    ShopsAdapter mAdapter;
    public static EventsFragment mEventsFragment;
    private Tracker mTracker;
    public ImageView mRetryIcon;
    private FragmentActivity mFragmentActivity;
    EventsParseAdapter mEventsParseAdapter;


    @Override
    public String getTitle(Context context) {
        return getString(R.string.hornama);
    }

    public synchronized static EventsFragment getInstance() {
        if (mEventsFragment == null) {
            mEventsFragment = new EventsFragment();
        }
        return mEventsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTracker = HonarnamaBrowseApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("EventsFragment");
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

        final View rootView = inflater.inflate(R.layout.fragment_events, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.events_listView);

        final RelativeLayout emptyListContainer = (RelativeLayout) rootView.findViewById(R.id.no_events_warning_container);
        listView.setEmptyView(emptyListContainer);

        final LinearLayout loadingCircle = (LinearLayout) rootView.findViewById(R.id.loading_circle_container);

        mEventsParseAdapter = new EventsParseAdapter(HonarnamaBrowseApp.getInstance());
        mEventsParseAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener() {
            @Override
            public void onLoading() {
                loadingCircle.setVisibility(View.VISIBLE);
                emptyListContainer.setVisibility(View.GONE);
            }

            @Override
            public void onLoaded(List objects, Exception e) {
                loadingCircle.setVisibility(View.GONE);

                if (mEventsParseAdapter.isEmpty()) {
                    emptyListContainer.setVisibility(View.VISIBLE);
                }
            }
        });
        listView.setAdapter(mEventsParseAdapter);
        listView.setOnItemClickListener(this);
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ParseObject selectedEvent = (ParseObject) mEventsParseAdapter.getItem(i);
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        controlPanelActivity.displayEventPage(selectedEvent.getObjectId(), false);
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
