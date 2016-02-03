package net.honarnama.sell;


import com.crashlytics.android.Crashlytics;
import com.parse.ParseObject;

import io.fabric.sdk.android.Fabric;
import net.honarnama.HonarnamaBaseApp;
import net.honarnama.core.model.Item;

import android.util.Log;

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
}
