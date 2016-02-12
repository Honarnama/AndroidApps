package net.honarnama.browse.fragment;


import net.honarnama.browse.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by elnaz on 2/11/16.
 */
public class ShopsFragment extends HonarnamaBrowseFragment {
    public static ShopsFragment mShopsFragment;

    public synchronized static ShopsFragment getInstance() {
        if (mShopsFragment == null) {
            mShopsFragment = new ShopsFragment();
        }
        return mShopsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shops, container, false);
        return rootView;
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
    public String getTitle(Context context) {
        return null;
    }
}

