package net.honarnama.sell.fragments;


import com.crashlytics.android.Crashlytics;

import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.core.utils.FileUtil;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.InputStream;

/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends HonarnamaBaseFragment {

    public static AboutFragment mAboutFragment;

    public synchronized static AboutFragment getInstance() {
        if (mAboutFragment == null) {
            mAboutFragment = new AboutFragment();
        }
        return mAboutFragment;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.about_us);
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
            Crashlytics.log(Log.ERROR, HonarnamaSellApp.PRODUCTION_TAG, "Error setting about us text: " + e);
        }
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
