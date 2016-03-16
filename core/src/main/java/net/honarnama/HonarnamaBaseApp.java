package net.honarnama;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import com.crashlytics.android.Crashlytics;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseObject;

import net.honarnama.base.BuildConfig;
import net.honarnama.base.R;
import net.honarnama.core.model.Bookmark;
import net.honarnama.core.model.Category;
import net.honarnama.core.model.City;
import net.honarnama.core.model.Event;
import net.honarnama.core.model.EventCategory;
import net.honarnama.core.model.Item;
import net.honarnama.core.model.Provinces;
import net.honarnama.core.model.Store;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by elnaz on 7/22/15.
 */
public abstract class HonarnamaBaseApp extends Application {

    public static final boolean DEBUG = BuildConfig.DEBUG;
    public static final String DOMAIN = "honarnama.net";
    public static final String PRODUCTION_TAG = "Honarnama";
    public static final int INTENT_IMAGE_SELECTOR_CODE_RANGE_START = 10000;
    public static final int INTENT_TELEGRAM_CODE = 1003;
    public static final int INTENT_CHOOSE_CATEGORY_CODE = 1004;
    public static final int INTENT_FILTER_ITEMS_CODE = 1005;
    public static final int INTENT_CHOOSE_EVENT_CATEGORY_CODE = 1006;


    public static final int INTENT_REGISTER_CODE = 3000;
    public static final String EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_EMAIL = "EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_EMAIL";
    public static final String EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_MOBILE = "EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_MOBILE";
    public static final String EXTRA_KEY_UNCAUGHT_EXCEPTION = "EXTRA_KEY_UNCAUGHT_EXCEPTION";

    public static File APP_FOLDER;
    public static File APP_IMAGES_FOLDER;

    private static final String PARSE_APPLICATION_ID = "RgwhQeuzLGKtYyS1mkkIkKVtST3hMamyXyJzP8Cu";
    private static final String PARSE_CLIENT_KEY = "1izVO8rxN6x28PEjgDCZSeXdVPfHxskX3ECKvcrg";


    public static final int GENDER_CODE_WOMAN = 0;
    public static final int GENDER_CODE_MAN = 1;
    public static final int GENDER_CODE_NOT_SAID = 2;

    public static String PREF_LOCAL_DATA_STORE_FOR_CATEGORIES_SYNCED = "local_data_store_for_categories_synced";
    public static String PREF_LOCAL_DATA_STORE_FOR_EVENT_CATEGORIES_SYNCED = "local_data_store_for_event_categories_synced";

    public static String PREF_LOCAL_DATA_STORE_FOR_PROVINCES_SYNCED = "local_data_store_for_provinces_synced";
    public static String PREF_LOCAL_DATA_STORE_FOR_CITY_SYNCED = "local_data_store_for_city_synced";
    public static String PREF_LOCAL_DATA_STORE_FOR_STORE_SYNCED = "local_data_store_for_store_synced";
    public static String PREF_LOCAL_DATA_STORE_FOR_EVENT_SYNCED = "local_data_store_for_event_synced";
    public static String PREF_LOCAL_DATA_STORE_FOR_ITEM_SYNCED = "local_data_store_for_item_synced";

    public static String SELL_APP_KEY = "honarnama_sell";
    public static String BROWSE_APP_KEY = "honarnama_browse";
    public static String INTENT_ORIGIN = "intent_origin";

    public static String PREF_LOCAL_DATA_STORE_SYNCED = "local_data_store_for_provinces_synced";


    private static HonarnamaBaseApp singleton;

    public synchronized static HonarnamaBaseApp getInstance() {
        return singleton;
    }

    @Override
    public void onCreate() {

        super.onCreate();

        singleton = this;

        if (BuildConfig.DEBUG) {
            Log.d(HonarnamaBaseApp.PRODUCTION_TAG, "App created: " + getPackageName());
        }

        Parse.enableLocalDatastore(this);
        ParseObject.registerSubclass(Category.class);
        ParseObject.registerSubclass(EventCategory.class);
        ParseObject.registerSubclass(Store.class);
        ParseObject.registerSubclass(Event.class);
        ParseObject.registerSubclass(Provinces.class);
        ParseObject.registerSubclass(City.class);
        ParseObject.registerSubclass(Item.class);
        ParseObject.registerSubclass(Bookmark.class);

        Parse.initialize(this, HonarnamaBaseApp.getParseApplicationId(), HonarnamaBaseApp.getParseClientKey());

        ParseACL defaultACL = new ParseACL();
// Optionally enable public read access while disabling public write access.
        defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (BuildConfig.DEBUG) {
                Log.e(PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                        "NO SDCARD");
            } else {
                Crashlytics.log(Log.ERROR, PRODUCTION_TAG, "NO SDCARD");
            }
        } else {
            APP_FOLDER = new File(Environment.getExternalStorageDirectory() + File.separator + "Honarnama");
            APP_FOLDER.mkdirs();
        }

        if (APP_FOLDER != null) {
            APP_IMAGES_FOLDER = new File(APP_FOLDER, "images");
            APP_IMAGES_FOLDER.mkdirs();
        }
    }

    public static String getParseApplicationId() {
        return HonarnamaBaseApp.PARSE_APPLICATION_ID;
    }

    public static String getParseClientKey() {
        return HonarnamaBaseApp.PARSE_CLIENT_KEY;
    }

    abstract public Tracker getDefaultTracker();
}
