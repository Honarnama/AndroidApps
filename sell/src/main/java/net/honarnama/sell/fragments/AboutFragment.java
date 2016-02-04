package net.honarnama.sell.fragments;


import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.core.utils.FileUtil;
import net.honarnama.sell.R;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

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
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        WebView webview = (WebView) rootView.findViewById(R.id.about_web_view);
//        webview.loadData(FileUtil.getTextFromResource(R.raw.about_us), "text/html", "utf-8");
        webview.loadUrl("file:///android_asset/about_us.html");
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
