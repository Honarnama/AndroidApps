package net.honarnama.base.fragment;


import net.honarnama.base.R;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.InputStream;


public class AboutFragment extends HonarnamaBaseFragment {

    public static AboutFragment mAboutFragment;

    public synchronized static AboutFragment getInstance() {
//        if (mAboutFragment == null) {
        mAboutFragment = new AboutFragment();
        Bundle args = new Bundle();
        mAboutFragment.setArguments(args);
//        }
        return mAboutFragment;
    }

    @Override
    public String getTitle() {
        return getStringInFragment(R.string.about_us);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        TextView aboutTextView = (TextView) rootView.findViewById(R.id.about_us_text_view);
        try {
            Resources res = getResources();
            InputStream in_s = res.openRawResource(R.raw.about_us);
            byte[] b = new byte[in_s.available()];
            in_s.read(b);
            aboutTextView.setText(new String(b));
        } catch (Exception e) {
            logE("Error setting about us text: " + e, e);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded() && getActivity() != null) {
            getActivity().setTitle(getTitle());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }
}
