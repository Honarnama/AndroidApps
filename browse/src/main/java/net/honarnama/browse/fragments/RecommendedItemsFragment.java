package net.honarnama.browse.fragments;

import net.honarnama.HonarnamaBaseFragment;
import net.honarnama.browse.R;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by elnaz on 12/8/15.
 */
public class RecommendedItemsFragment extends Fragment {

    private static RecommendedItemsFragment mRecommendedItemsFragment;
    public synchronized static RecommendedItemsFragment getInstance() {
        if (mRecommendedItemsFragment == null) {
            mRecommendedItemsFragment = new RecommendedItemsFragment();
        }
        return mRecommendedItemsFragment;
    }

    public String getTitle(Context context) {
        return context.getString(R.string.recommended_items);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_recommended_items, container, false);
        return rootView;

    }
}
