package net.honarnama.sell;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseObject;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.model.Item;
import net.honarnama.sell.activity.ControlPanelActivity;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import io.fabric.sdk.android.Fabric;

public class HonarnamaSellApp extends HonarnamaBaseApp {

    public static String STORE_LOGO_FILE_NAME = "store_logo.jpg";
    public static String STORE_BANNER_FILE_NAME = "store_banner.jpg";
    // uncaught exception handler variable
    private Thread.UncaughtExceptionHandler defaultUEH;


    // handler listener
    private Thread.UncaughtExceptionHandler _unCaughtExceptionHandler =
            new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    Crashlytics.logException(ex);
                    // here I do logging of exception to a db
                    PendingIntent myActivity = PendingIntent.getActivity(getApplicationContext(),
                            192837, new Intent(getApplicationContext(), ControlPanelActivity.class),
                            PendingIntent.FLAG_ONE_SHOT);

                    AlarmManager alarmManager;
                    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            1000, myActivity);
                    System.exit(2);
                    // re-throw critical exception further to the os (important)
                    //TODO: ask reza is this necessary?
                    defaultUEH.uncaughtException(thread, ex);
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
