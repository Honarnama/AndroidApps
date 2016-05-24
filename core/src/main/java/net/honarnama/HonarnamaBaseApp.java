package net.honarnama;

import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import com.crashlytics.android.Crashlytics;

import net.honarnama.base.BuildConfig;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by elnaz on 7/22/15.
 */
public abstract class HonarnamaBaseApp extends Application {

    public static final boolean DEBUG = BuildConfig.DEBUG;
    public static final String WEB_ADDRESS = "https://www.honarnama.net";
    public static final String PRODUCTION_TAG = "Honarnama";
    public static final int INTENT_IMAGE_SELECTOR_CODE_RANGE_START = 1000;

    public static final int INTENT_TELEGRAM_CODE = 2000;
    public static final int INTENT_REGISTER_CODE = 2001;

    public static final int INTENT_CHOOSE_CATEGORY_CODE = 3000;

    public static final int INTENT_FILTER_ITEMS_CODE = 4000;
    public static final int INTENT_FILTER_EVENT_CODE = 4001;
    public static final int INTENT_FILTER_SHOP_CODE = 4002;

    public static final String EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_EMAIL = "EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_EMAIL";
    public static final String EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_MOBILE = "EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_MOBILE";
    public static final String EXTRA_KEY_TELEGRAM_CODE = "EXTRA_KEY_TELEGRAM_CODE";
    public static final String EXTRA_KEY_INTENT_CALLER = "intent_origin";
    public static final String EXTRA_KEY_PROVINCE_ID = "selectedProvinceId";
    public static final String EXTRA_KEY_PROVINCE_NAME = "selectedProvinceName";
    public static final String EXTRA_KEY_CITY_ID = "selectedCityId";
    public static final String EXTRA_KEY_CITY_NAME = "selectedCityName";
    public static final String EXTRA_KEY_FILTER_SUB_CAT_ROW_SELECTED = "isFilterSubCategoryRowSelected";
    public static final String EXTRA_KEY_SUB_CATS = "subCats";
    public static final String EXTRA_KEY_ALL_IRAN = "all_iran";
    public static final String EXTRA_KEY_FILTER_APPLIED = "filter_applied";
    public static final String EXTRA_KEY_CATEGORY_ID = "selectedCategoryId";
    public static final String EXTRA_KEY_CATEGORY_PARENT_ID = "selectedCategoryParentId";
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

    public static String PREF_KEY_META_VERSION = "meta_version";

    public static String PREF_NAME_SELL_APP = "honarnama_sell";
    public static String PREF_NAME_BROWSE_APP = "honarnama_browse";
    public static String PREF_NAME_COMMON = "honarnama_apps";

    public static String SELL_PACKAGE_NAME = "net.honarnama.sell";
    public static String BROWSE_PACKAGE_NAME = "net.honarnama.browse";

    private static HonarnamaBaseApp singleton;
    private static SharedPreferences mCommonSharedPref;

    public static String PACKAGE_NAME;

    public synchronized static HonarnamaBaseApp getInstance() {
        if (BuildConfig.DEBUG) {
            Log.d(HonarnamaBaseApp.PRODUCTION_TAG, "Base App Constructor called. ");
        }
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
        }

        setCommonSharedPref(getSharedPreferences(HonarnamaBaseApp.PREF_NAME_COMMON, Context.MODE_PRIVATE));
        PACKAGE_NAME = getApplicationContext().getPackageName();

        //TODO inja comment konam
        try {
            ProviderInstaller.installIfNeeded(HonarnamaBaseApp.getInstance());
        } catch (GooglePlayServicesRepairableException e) {
            Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "GooglePlayServicesRepairableException!", e);
            Crashlytics.logException(e);
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "GooglePlayServicesNotAvailableException!", e);
            Crashlytics.logException(e);
        } catch (Exception e) {
            Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Exception trying to install needed modules!", e);
            Crashlytics.logException(e);
        }

    }

    public static void setCommonSharedPref(SharedPreferences commonSharedPref) {
        mCommonSharedPref = commonSharedPref;
    }

    public static SharedPreferences getCommonSharedPref() {
        return mCommonSharedPref;
    }

    abstract public Tracker getDefaultTracker();

    @Override
    public void onTerminate() {
        GRPCUtils grpcUtils = GRPCUtils.getInstanceIfCreated();
        if (grpcUtils != null) {
            try {
                grpcUtils.close();
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    Log.e(PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                            "Error Closing GRPC.", e);
                } else {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    String stackTrace = sw.toString();
                    Crashlytics.log(Log.ERROR, PRODUCTION_TAG, "Error Closing GRPC. Error: " + e + ". stackTrace: " + stackTrace);
                }
            }
        }
        super.onTerminate();
    }
}
