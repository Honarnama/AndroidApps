package net.honarnama.sell;


import com.parse.ParseObject;

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

        if (BuildConfig.DEBUG) {
            Log.i(PRODUCTION_TAG, "HonarnamaSellApp.onCreate()");
        }
    }
}
