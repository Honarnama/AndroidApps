package net.honarnama.browse;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import com.parse.Parse;
import com.parse.ParseObject;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.browse.model.Item;

import android.app.Application;
import android.util.Log;

/**
 * Created by elnaz on 12/18/15.
 */
public class HonarnamaBrowseApp extends HonarnamaBaseApp {

    public static final String PRODUCTION_TAG = "HonarnamaBrowse";

    public static final String EXTRA_KEY_PROVINCE_ID = "selectedProvinceId";
    public static final String EXTRA_KEY_PROVINCE_NAME = "selectedProvinceName";
    public static final String EXTRA_KEY_CITY_ID = "selectedCityId";
    public static final String EXTRA_KEY_CITY_NAME = "selectedCityName";
    public static final String EXTRA_KEY_MIN_PRICE_INDEX = "minPriceIndex";
    public static final String EXTRA_KEY_MIN_PRICE_VALUE = "minPriceValue";
    public static final String EXTRA_KEY_MAX_PRICE_INDEX = "maxPriceIndex";
    public static final String EXTRA_KEY_MAX_PRICE_VALUE = "maxPriceValue";

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Log.i(PRODUCTION_TAG, "HonarnamaBrowseApp.onCreate()");
        }
        ParseObject.registerSubclass(Item.class);
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
