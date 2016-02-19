package net.honarnama.browse.model;

import com.crashlytics.android.Crashlytics;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import net.honarnama.base.BuildConfig;
import net.honarnama.core.model.Store;
import net.honarnama.core.utils.NetworkManager;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.util.Log;

import java.util.List;

import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * Created by elnaz on 2/14/16.
 */
public class Shop extends Store {

    public static Task<List<ParseObject>> getShopList(Context context) {
        final TaskCompletionSource<List<ParseObject>> tcs = new TaskCompletionSource<>();

//        ParseQuery<Store> parseQuery = new ParseQuery<Store>(Store.class);
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Store");
        parseQuery.whereEqualTo(VALIDITY_CHECKED, true);
        parseQuery.whereEqualTo(STATUS, Store.STATUS_CODE_VERIFIED);
        parseQuery.include(CITY);

        if (!NetworkManager.getInstance().isNetworkEnabled(false)) {
            tcs.setError(new NetworkErrorException("No network connection."));
            return tcs.getTask();
        }

        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> stores, ParseException e) {
                if (e == null) {
                    tcs.trySetResult(stores);
                } else {
                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        if (BuildConfig.DEBUG) {
                            Log.d(DEBUG_TAG, "No shop found!");
                        }
                        tcs.trySetResult(null);
                    } else {
                        tcs.trySetError(e);
                        if (BuildConfig.DEBUG) {
                            Log.e(DEBUG_TAG,
                                    "Error getting shop list. Error Code: " + e.getCode() + " //  Error Msg: " + e.getMessage(), e);
                        } else {
                            Crashlytics.log(Log.ERROR, DEBUG_TAG, "Error getting shop list. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e);
                        }
                    }
                }
            }
        });
        return tcs.getTask();
    }

    public static Task<ParseObject> getShopById(final String shopId) {
        final TaskCompletionSource<ParseObject> tcs = new TaskCompletionSource<>();

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Store");
        parseQuery.whereEqualTo(VALIDITY_CHECKED, true);
        parseQuery.whereEqualTo(STATUS, Store.STATUS_CODE_VERIFIED);
        parseQuery.whereEqualTo(OBJECT_ID, shopId);
        parseQuery.include(PROVINCE);
        parseQuery.include(CITY);

        if (!NetworkManager.getInstance().isNetworkEnabled(false)) {
            tcs.setError(new NetworkErrorException("No network connection."));
            return tcs.getTask();
        }

        parseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject store, ParseException e) {
                if (e == null) {
                    tcs.trySetResult(store);
                } else {
                    tcs.trySetError(e);
                    if (BuildConfig.DEBUG) {
                        Log.e(DEBUG_TAG,
                                "Error getting shop info for " + shopId + ". Error Code: " + e.getCode() + " //  Error Msg: " + e.getMessage(), e);
                    } else {
                        Crashlytics.log(Log.ERROR, DEBUG_TAG, "Error getting shop info for " + shopId + ". Error Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e);
                    }
                }
            }
        });

        return tcs.getTask();
    }
}