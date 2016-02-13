package net.honarnama.browse.model;

import com.crashlytics.android.Crashlytics;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.model.Store;
import net.honarnama.core.utils.NetworkManager;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.List;

import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * Created by elnaz on 2/14/16.
 */
public class Shop extends Store {

    public static Task<List<Store>> getList(Context context) {
        final TaskCompletionSource<List<Store>> tcs = new TaskCompletionSource<>();
        ParseQuery<Store> parseQuery = new ParseQuery<Store>(Store.class);

        if (!NetworkManager.getInstance().isNetworkEnabled(false)) {
            tcs.setError(new NetworkErrorException("No network connection."));
            return tcs.getTask();
        }

        parseQuery.findInBackground(new FindCallback<Store>() {
            @Override
            public void done(final List<Store> stores, ParseException e) {
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
}
