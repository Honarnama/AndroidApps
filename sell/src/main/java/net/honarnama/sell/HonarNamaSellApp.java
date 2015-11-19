package net.honarnama.sell;


import net.honarnama.HonarnamaBaseApp;

import android.util.Log;

public class HonarnamaSellApp extends HonarnamaBaseApp {

    public static String STORE_LOGO_FILE_NAME = "store_logo.jpg";
    public static String NATIONAL_CARD_FILE_NAME = "national_card.jpg";

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Log.i(PRODUCTION_TAG, "HonarnamaSellApp.onCreate()");
        }
    }
}
