package net.honarnama.core.model;

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
import android.preference.PreferenceManager;
import android.util.Log;

import bolts.Task;
import bolts.TaskCompletionSource;

@ParseClassName("art_categories")
public class Category extends ParseObject {

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

    public Task<String> findCategoryName(String categoryId, Context mContext) {
        final TaskCompletionSource<String> tcs = new TaskCompletionSource<>();

        ParseQuery<Category> parseQuery = ParseQuery.getQuery(Category.class);
        parseQuery.whereEqualTo(OBJECT_ID, categoryId);
//        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        final SharedPreferences sharedPref = HonarnamaBaseApp.getInstance().getSharedPreferences(HonarnamaUser.getCurrentUser().getUsername(), Context.MODE_PRIVATE);
        if (sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_CATEGORIES_SYNCED, false)) {
            if (BuildConfig.DEBUG) {
                Log.d(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "get category from Local datastore");
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
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "finding  category failed. Code: " + e.getCode() + " // " + e.getMessage());
                    } else {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "finding category failed.");
                    }
                    tcs.trySetError(e);
                }
            }
        });
        return tcs.getTask();
    }

}
