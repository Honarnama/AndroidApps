package net.honarnama.browse.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.honarnama.base.utils.NetworkManager;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by elnaz on 3/12/16.
 */
public class NoNetFragment extends HonarnamaBrowseFragment implements View.OnClickListener {
    public ImageView mRetryIcon;
    public static NoNetFragment mNoNetFragment;
    private Tracker mTracker;

    public synchronized static NoNetFragment getInstance() {
//        if (mNoNetFragment == null) {
        mNoNetFragment = new NoNetFragment();
//        }
        return mNoNetFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = HonarnamaBrowseApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("NoNetFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_no_network, container, false);
        mRetryIcon = (ImageView) rootView.findViewById(R.id.no_network_fragment_retry_icon);
        mRetryIcon.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public String getTitle() {
        return getStringInFragment(R.string.hornama);
    }

    @Override
    public void onSelectedTabClick() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.no_network_fragment_retry_icon:
                if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                    return;
                }
                ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
                controlPanelActivity.refreshNoNetFragment();
                break;
        }
    }

}
