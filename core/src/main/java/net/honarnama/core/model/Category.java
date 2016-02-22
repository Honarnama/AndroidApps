package net.honarnama.core.model;

import com.crashlytics.android.Crashlytics;
import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.utils.HonarnamaUser;
import net.honarnama.core.utils.NetworkManager;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import bolts.Task;
import bolts.TaskCompletionSource;

@ParseClassName("art_categories")
public class Category extends ParseObject {
    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/categoryModel";

    public static String OBJECT_NAME = "art_categories";
    public static String NAME = "name";
    public static String ORDER = "order";
    public static String OBJECT_ID = "objectId";

    public Category() {
        super();
    }

    public String getName() {
        return getString(NAME);
    }

    public static Task<String> getCategoryNameById(String categoryId) {
        final TaskCompletionSource<String> tcs = new TaskCompletionSource<>();

        ParseQuery<Category> parseQuery = ParseQuery.getQuery(Category.class);
        parseQuery.whereEqualTo(OBJECT_ID, categoryId);
//        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        final SharedPreferences sharedPref = HonarnamaBaseApp.getInstance().getSharedPreferences(HonarnamaUser.getCurrentUser().getUsername(), Context.MODE_PRIVATE);
        if (sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_CATEGORIES_SYNCED, false)) {
            if (BuildConfig.DEBUG) {
                Log.d(DEBUG_TAG, "Getting categories from local datastore");
            }
            parseQuery.fromLocalDatastore();
        } else {

            if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                tcs.setError(new NetworkErrorException("Network connection failed"));
                return tcs.getTask();
            }
        }
        parseQuery.getFirstInBackground(new GetCallback<Category>() {
            @Override
            public void done(Category category, ParseException e) {
                if (e == null) {
                    tcs.trySetResult(category.getName());
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.e(DEBUG_TAG, "Finding  category failed. Code: " + e.getCode() + " // Msg: " + e.getMessage(), e);
                    } else {
                        Crashlytics.log(Log.ERROR, DEBUG_TAG, "Finding  category failed. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error:" + e);
                    }
                    tcs.trySetError(e);
                }
            }
        });
        return tcs.getTask();
    }

    public static Task<Category> getCategoryById(String catId) {
        final TaskCompletionSource<Category> tcs = new TaskCompletionSource<>();

        ParseQuery<Category> parseQuery = ParseQuery.getQuery(Category.class);
        parseQuery.whereEqualTo(OBJECT_ID, catId);
        final SharedPreferences sharedPref = HonarnamaBaseApp.getInstance().getSharedPreferences(HonarnamaUser.getCurrentUser().getUsername(), Context.MODE_PRIVATE);
        if (sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_CATEGORIES_SYNCED, false)) {
            if (BuildConfig.DEBUG) {
                Log.d(DEBUG_TAG, "Getting category by id from local datastore");
            }
            parseQuery.fromLocalDatastore();
        } else {
            if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                tcs.setError(new NetworkErrorException("Network connection failed"));
                return tcs.getTask();
            }
        }

        parseQuery.getFirstInBackground(new GetCallback<Category>() {
            @Override
            public void done(Category category, ParseException e) {
                if (e == null) {
                    tcs.trySetResult(category);
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.e(DEBUG_TAG, "Finding category by id failed. Code: " + e.getCode() + " // " + e.getMessage(), e);
                    } else {
                        Crashlytics.log(Log.ERROR, DEBUG_TAG, "Finding category by id failed. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e);
                    }
                    tcs.trySetError(e);
                }
            }
        });
        return tcs.getTask();
    }
}
