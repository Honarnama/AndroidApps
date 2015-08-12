package net.honarnama;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

import net.honarnama.base.BuildConfig;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by elnaz on 7/22/15.
 */
public class HonarNamaBaseApp extends Application {

    public static final boolean DEBUG = BuildConfig.DEBUG;
    public static final String DOMAIN = "honarnama.net";
    public static final String PRODUCTION_TAG = "HonarNama";
    private static HonarNamaBaseApp singleton;
    private static final String PARSE_APPLICATION_ID = "RgwhQeuzLGKtYyS1mkkIkKVtST3hMamyXyJzP8Cu";
    private static final String PARSE_CLIENT_KEY = "1izVO8rxN6x28PEjgDCZSeXdVPfHxskX3ECKvcrg";

    public static final int INTENT_IMAGE_SELECTOR_CODE_RANGE_START = 10000;
    public static final int INTENT_TELEGRAM_CODE = 1003;

    public static File APP_FOLDER;
    public static File APP_IMAGES_FOLDER;

    public static String STORE_LOGO_FILE_NAME = "store_logo.jpg";


    public static HonarNamaBaseApp getInstance() {
        return singleton;
    }

    @Override
    public void onCreate() {

        super.onCreate();

        singleton = this;

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, HonarNamaBaseApp.getParseApplicationId(), HonarNamaBaseApp.getParseClientKey());

        ParseUser.enableAutomaticUser();
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
        return HonarNamaBaseApp.PARSE_APPLICATION_ID;
    }

    public static String getParseClientKey() {
        return HonarNamaBaseApp.PARSE_CLIENT_KEY;
    }

}
