package net.honarnama;

import com.google.android.gms.analytics.Tracker;

import com.crashlytics.android.Crashlytics;

import net.honarnama.base.BuildConfig;
import net.honarnama.core.helper.DatabaseHelper;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
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
    public static final int INTENT_IMAGE_SELECTOR_CODE_RANGE_START = 1000;

    public static final int INTENT_TELEGRAM_CODE = 2000;
    public static final int INTENT_REGISTER_CODE = 2001;

    public static final int INTENT_CHOOSE_CATEGORY_CODE = 3000;

    public static final int INTENT_FILTER_ITEMS_CODE = 4000;
    public static final int INTENT_FILTER_EVENT_CODE = 4001;
    public static final int INTENT_FILTER_SHOP_CODE = 4002;

    public static final int INTENT_FILTER_SET_DEFAULT_LOCATION = 5000;

    public static final String EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_EMAIL = "EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_EMAIL";
    public static final String EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_MOBILE = "EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_MOBILE";
    public static final String EXTRA_KEY_TELEGRAM_CODE = "EXTRA_KEY_TELEGRAM_CODE";
    public static final String EXTRA_KEY_UNCAUGHT_EXCEPTION = "EXTRA_KEY_UNCAUGHT_EXCEPTION";
    public static final String EXTRA_KEY_INTENT_CALLER = "intent_origin";
    public static final String EXTRA_KEY_PROVINCE_ID = "selectedProvinceId";
    public static final String EXTRA_KEY_PROVINCE_NAME = "selectedProvinceName";
    public static final String EXTRA_KEY_CITY_ID = "selectedCityId";
    public static final String EXTRA_KEY_CITY_NAME = "selectedCityName";
    public static final String EXTRA_KEY_FILTER_SUB_CAT_ROW_SELECTED = "isFilterSubCategoryRowSelected";
    public static final String EXTRA_KEY_SUB_CATS = "subCats";
    public static final String EXTRA_KEY_ALL_IRAN = "all_iran";
    public static final String EXTRA_KEY_FILTER_APPLIED = "filter_applied";
    public static final String EXTRA_KEY_CATEGORY_ID = "selectedCategoryObjectId";
    public static final String EXTRA_KEY_CATEGORY_NAME = "selectedCategoryName";

    public static final String PREF_KEY_DEFAULT_LOCATION_PROVINCE_ID = "default_province_id";
    public static final String PREF_KEY_DEFAULT_LOCATION_PROVINCE_NAME = "default_province_name";
    public static final String PREF_KEY_DEFAULT_LOCATION_CITY_ID = "default_city_id";
    public static final String PREF_KEY_DEFAULT_LOCATION_CITY_NAME = "default_city_name";
    public static final String PREF_KEY_BROWSE_APP_RATED = "browse_app_rated";
    public static final String PREF_KEY_SELL_APP_RATED = "sell_app_rated";
    public static final String PREF_KEY_LOGIN_TOKEN = "login_token";
    public static final String PREF_KEY_TELEGRAM_TOKEN = "telegram_token";
    public static final String PREF_KEY_TELEGRAM_TOKEN_SET_DATE = "telegram_token_set_date";

    public static File APP_FOLDER;
    public static File APP_IMAGES_FOLDER;

    private static final String PARSE_APPLICATION_ID = "RgwhQeuzLGKtYyS1mkkIkKVtST3hMamyXyJzP8Cu";
    private static final String PARSE_CLIENT_KEY = "1izVO8rxN6x28PEjgDCZSeXdVPfHxskX3ECKvcrg";

    public static String PREF_KEY_META_VERSION = "meta_version";

    public static String PREF_NAME_SELL_APP = "honarnama_sell";
    public static String PREF_NAME_BROWSE_APP = "honarnama_browse";
    public static String PREF_NAME_COMMON = "honarnama_apps";

    private static HonarnamaBaseApp singleton;
    private static SharedPreferences mCommonSharedPref;

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
        DatabaseHelper.getInstance(HonarnamaBaseApp.getInstance());
        setCommonSharedPref(getSharedPreferences(HonarnamaBaseApp.PREF_NAME_COMMON, Context.MODE_PRIVATE));
    }

    public static void setCommonSharedPref(SharedPreferences commonSharedPref) {
        mCommonSharedPref = commonSharedPref;
    }

    public static SharedPreferences getCommonSharedPref() {
        return mCommonSharedPref;
    }

    public static String getParseApplicationId() {
        return HonarnamaBaseApp.PARSE_APPLICATION_ID;
    }

    public static String getParseClientKey() {
        return HonarnamaBaseApp.PARSE_CLIENT_KEY;
    }

    abstract public Tracker getDefaultTracker();

    @Override
    public void onTerminate() {
        GRPCUtils grpcUtils = GRPCUtils.getInstanceIfCreated();
        if (grpcUtils != null) {
            try {
                grpcUtils.close();
            } catch (InterruptedException ie) {
                // TODO
            }
        }
        super.onTerminate();
    }
}
