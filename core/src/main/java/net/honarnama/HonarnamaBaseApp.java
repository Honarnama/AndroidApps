package net.honarnama;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseObject;
import com.parse.ParseUser;

import net.honarnama.base.BuildConfig;
import net.honarnama.core.model.Category;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by elnaz on 7/22/15.
 */
public class HonarnamaBaseApp extends Application {

    public static final boolean DEBUG = BuildConfig.DEBUG;
    public static final String DOMAIN = "honarnama.net";
    public static final String PRODUCTION_TAG = "Honarnama";
    public static final int INTENT_IMAGE_SELECTOR_CODE_RANGE_START = 10000;
    public static final int INTENT_TELEGRAM_CODE = 1003;
    public static final int INTENT_CHOOSE_CATEGORY_CODE = 1004;

    public static final int INTENT_REGISTER_CODE = 3000;
    public static final String DISPLAY_SUCCESSFUL_REGISTER_SNACK = "DISPLAY_SUCCESSFUL_REGISTER_SNACK";


    public static File APP_FOLDER;
    public static File APP_IMAGES_FOLDER;

    private static final String PARSE_APPLICATION_ID = "RgwhQeuzLGKtYyS1mkkIkKVtST3hMamyXyJzP8Cu";
    private static final String PARSE_CLIENT_KEY = "1izVO8rxN6x28PEjgDCZSeXdVPfHxskX3ECKvcrg";

    public static String PREF_LOCAL_DATA_STORE_FOR_CATEGORIES_SYNCED = "local_data_store_for_categories_synced";


    private static HonarnamaBaseApp singleton;

    public static HonarnamaBaseApp getInstance() {
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
                Log.e(PRODUCTION_TAG, "NO SDCARD");
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

}
