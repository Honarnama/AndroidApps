package net.honarnama.sell.fragments;


import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.sell.R;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 */
public class DefaultFragment extends HonarnamaBaseFragment {

    public static DefaultFragment mDefaultFragment;

    public synchronized static DefaultFragment getInstance() {
        if (mDefaultFragment == null) {
            mDefaultFragment = new DefaultFragment();
        }
        return mDefaultFragment;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.default_app_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_default, container, false);
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
}
