package net.honarnama.sell;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import com.crashlytics.android.Crashlytics;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.sell.activity.LoginActivity;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import io.fabric.sdk.android.Fabric;

public class HonarnamaSellApp extends HonarnamaBaseApp {

    public static String STORE_LOGO_FILE_NAME = "store_logo.jpg";
    public static String STORE_BANNER_FILE_NAME = "store_banner.jpg";
    // uncaught exception handler variable
    private Thread.UncaughtExceptionHandler defaultUEH;

    public static final String PRODUCTION_TAG = "HonarnamaSell";


    // handler listener
    private Thread.UncaughtExceptionHandler _unCaughtExceptionHandler =
            new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {

                    if (BuildConfig.DEBUG) {
                        Log.e(PRODUCTION_TAG, "Uncaught Exception arosed. Exception: " + ex, ex);
                    } else {
                        Crashlytics.log(Log.ERROR, PRODUCTION_TAG, "Uncaught Exception arised. Exception: " + ex);
                    }

                    //Restart app on unhandle exception
//                    Intent restartIntent = new Intent(getApplicationContext(), LoginActivity.class);
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//                        restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    }
//                    restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//                    startActivity(restartIntent);
//                    android.os.Process.killProcess(Process.myPid());
//                    System.exit(0);
                }
            };

    public HonarnamaSellApp() {
        super();
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        // setup handler for uncaught exception
        Thread.setDefaultUncaughtExceptionHandler(_unCaughtExceptionHandler);
    }

    @Override
    public void onCreate() {
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
