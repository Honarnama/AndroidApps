package net.honarnama.browse;

import com.parse.ParseObject;

import net.honarnama.HonarnamaBaseApp;

import android.util.Log;

/**
 * Created by elnaz on 12/18/15.
 */
public class HonarnamaBrowseApp extends HonarnamaBaseApp {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Log.i(PRODUCTION_TAG, "HonarnamaBrowseApp.onCreate()");
        }
    }
}
