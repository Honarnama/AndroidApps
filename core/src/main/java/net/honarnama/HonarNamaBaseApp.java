package net.honarnama;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

import net.honarnama.base.BuildConfig;

import android.app.Application;
import android.os.Environment;

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

    public static final int INTENT_CAPTURE_IMAGE_CODE = 1001;
    public static final int INTENT_SELECT_IMAGE_CODE = 1002;
    public static final int INTENT_TELEGRAM_CODE = 1003;
    public static final int INTENT_CROP_IMAGE_CODE = 1004;

    public static File imagesFolder;


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

        imagesFolder = new File(new File(Environment.getExternalStorageDirectory(), "icm"), "honarnama");
        imagesFolder.mkdirs();


    }

    public static String getParseApplicationId() {
        return HonarNamaBaseApp.PARSE_APPLICATION_ID;
    }

    public static String getParseClientKey() {
        return HonarNamaBaseApp.PARSE_CLIENT_KEY;
    }

}
