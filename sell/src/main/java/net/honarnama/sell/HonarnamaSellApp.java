package net.honarnama.sell;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseObject;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.model.Item;

import android.app.Application;
import android.util.Log;

import io.fabric.sdk.android.Fabric;

public class HonarnamaSellApp extends HonarnamaBaseApp {

    public static String STORE_LOGO_FILE_NAME = "store_logo.jpg";
    public static String STORE_BANNER_FILE_NAME = "store_banner.jpg";

    @Override
    public void onCreate() {
        ParseObject.registerSubclass(Item.class);
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        if (BuildConfig.DEBUG) {
            Log.i(PRODUCTION_TAG, "HonarnamaSellApp.onCreate()");
        }
    }

    private Tracker mTracker;
    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }
}
