package net.honarnama.sell;


import android.util.Log;

public class HonarNamaSellApp extends net.honarnama.HonarNamaBaseApp {

    public static String STORE_LOGO_FILE_NAME = "store_logo.jpg";
    public static String NATIONAL_CARD_FILE_NAME = "national_card.jpg";

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Log.i(PRODUCTION_TAG, "HonarNamaSellApp.onCreate()");
        }
    }
}
