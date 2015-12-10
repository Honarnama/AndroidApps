package net.honarnama.browse.fragments;


import net.honarnama.browse.R;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by elnaz on 12/10/15.
 */
public class RecentActivitiesFragment extends Fragment {
    private static RecentActivitiesFragment mRecentActivitiesFragment;
    public synchronized static RecentActivitiesFragment getInstance() {
        if (mRecentActivitiesFragment == null) {
            mRecentActivitiesFragment = new RecentActivitiesFragment();
        }
        return mRecentActivitiesFragment;
    }

    public String getTitle(Context context) {
        return context.getString(R.string.recent_activity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_recent_activity, container, false);
        return rootView;

    }
}
