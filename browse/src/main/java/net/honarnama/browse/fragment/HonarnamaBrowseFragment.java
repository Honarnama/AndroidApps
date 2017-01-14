package net.honarnama.browse.fragment;

import net.honarnama.base.fragment.HonarnamaBaseFragment;
import net.honarnama.browse.BuildConfig;
import net.honarnama.browse.HonarnamaBrowseApp;

import android.os.Bundle;
import android.util.Log;

/**
 * Created by elnaz on 2/11/16.
 */
public abstract class HonarnamaBrowseFragment extends HonarnamaBaseFragment {

    private boolean announced = false;

    public static final int PAGE_SIZE = 24;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG && !announced) {
            Log.d(HonarnamaBrowseApp.PRODUCTION_TAG, "Fragment created,\tadb catlog tag:   'Honarnama/" + getLocalClassName() + ":V'");
            announced = true;
        }
    }

    abstract public void onSelectedTabClick();

}
