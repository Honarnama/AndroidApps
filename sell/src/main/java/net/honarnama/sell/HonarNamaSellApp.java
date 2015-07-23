package net.honarnama.sell;


import android.util.Log;

public class HonarNamaSellApp extends net.honarnama.HonarNamaBaseApp {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Log.i(PRODUCTION_TAG, "HonarNamaSellApp.onCreate()");
        }
    }
}
