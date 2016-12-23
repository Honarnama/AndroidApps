package net.honarnama.sell.fragments;


import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.honarnama.base.fragment.HonarnamaBaseFragment;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;
import net.honarnama.sell.activity.ControlPanelActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ResourceBundle;


public class CPanelFragment extends HonarnamaBaseFragment implements View.OnClickListener {

    public static CPanelFragment mCPanelFragment;
    private Tracker mTracker;

    public synchronized static CPanelFragment getInstance() {
        if (mCPanelFragment == null) {
            mCPanelFragment = new CPanelFragment();
        }
        return mCPanelFragment;
    }

    @Override
    public String getTitle() {
        return getStringInFragment(R.string.toolbar_title);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTracker = HonarnamaSellApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("CPanelFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_cpanel, container, false);

        rootView.findViewById(R.id.cpanel_manage_store).setOnClickListener(this);
        rootView.findViewById(R.id.cpanel_items).setOnClickListener(this);
        rootView.findViewById(R.id.cpanel_event).setOnClickListener(this);
        rootView.findViewById(R.id.cpanel_new_item).setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()) {
            ControlPanelActivity activity = (ControlPanelActivity) getActivity();
            if (activity != null) {
                activity.setTitle(getTitle());
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        ControlPanelActivity activity = (ControlPanelActivity) getActivity();
        switch (v.getId()) {
            case R.id.cpanel_manage_store:
                if (activity != null) {
                    activity.resetMenuIcons();
                    activity.switchFragment(StoreFragment.getInstance());
                }
                break;

            case R.id.cpanel_items:
                if (activity != null) {
                    activity.resetMenuIcons();
                    activity.switchFragment(ItemsFragment.getInstance());
                }
                break;

            case R.id.cpanel_event:
                if (activity != null) {
                    activity.resetMenuIcons();
                    activity.switchFragment(EventManagerFragment.getInstance());
                }
                break;

            case R.id.cpanel_new_item:
                if (activity != null) {
                    activity.resetMenuIcons();
                    activity.switchFragment(EditItemFragment.getInstance());
                }
                break;

        }
    }
}
