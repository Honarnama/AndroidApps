package net.honarnama.browse.model;

import com.crashlytics.android.Crashlytics;
import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import net.honarnama.base.BuildConfig;

import android.util.Log;

import java.util.List;

import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * Created by elnaz on 2/15/16.
 */
@ParseClassName("Item")
public class Item extends net.honarnama.core.model.Item {

    public static Task<List<Item>> getItemsByOwner(final ParseUser owner) {
        final TaskCompletionSource<List<Item>> tcs = new TaskCompletionSource<>();
        ParseQuery<Item> parseQuery = new ParseQuery<Item>(Item.class);
        parseQuery.whereEqualTo(Item.OWNER, owner);

        parseQuery.findInBackground(new FindCallback<Item>() {
            @Override
            public void done(final List<Item> items, ParseException e) {
                if (e == null) {
                    tcs.trySetResult(items);
                } else {
                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        if (BuildConfig.DEBUG) {
                            Log.d(DEBUG_TAG, "Shop with owner " + owner.getObjectId() + " does not have any items.");
                        }
                        tcs.trySetResult(null);
                    } else {
                        tcs.trySetError(e);
                        if (BuildConfig.DEBUG) {
                            Log.e(DEBUG_TAG,
                                    "Error getting shop items for owner: " + owner.getObjectId() + ". Error Code: " + e.getCode() + " //  Error Msg: " + e.getMessage(), e);
                        } else {
                            Crashlytics.log(Log.ERROR, DEBUG_TAG, "Error getting shop items for owner: " + owner.getObjectId() + ". Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e);
                        }
                    }
                }
            }
        });
        return tcs.getTask();
    }

}
