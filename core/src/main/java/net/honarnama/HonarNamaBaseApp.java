package net.honarnama;

import com.parse.Parse;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

/**
 * Created by elnaz on 7/22/15.
 */
public class HonarNamaBaseApp extends Application {

    public static final String PRODUCTION_TAG = "HonarNama";

    private static HonarNamaBaseApp singleton;
    private static final String PARSE_APPLICATION_ID = "RgwhQeuzLGKtYyS1mkkIkKVtST3hMamyXyJzP8Cu";
    private static final String PARSE_CLIENT_KEY = "1izVO8rxN6x28PEjgDCZSeXdVPfHxskX3ECKvcrg";

    public static HonarNamaBaseApp getInstance() {
        return singleton;
    }

    @Override
    public void onCreate() {

        super.onCreate();

        singleton = this;

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, HonarNamaBaseApp.getParseApplicationId(), HonarNamaBaseApp.getParseClientKey());

    }

    public static String getParseApplicationId() {
        return HonarNamaBaseApp.PARSE_APPLICATION_ID;
    }

    public static String getParseClientKey() {
        return HonarNamaBaseApp.PARSE_CLIENT_KEY;
    }

}
