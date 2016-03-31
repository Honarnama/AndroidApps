package net.honarnama.browse.model;

import com.crashlytics.android.Crashlytics;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import net.honarnama.base.BuildConfig;
import net.honarnama.core.model.ArtCategory;
import net.honarnama.core.model.Store;
import net.honarnama.core.utils.NetworkManager;

import android.accounts.NetworkErrorException;
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
        parseQuery.whereEqualTo(Item.STATUS, STATUS_CODE_VERIFIED);
        parseQuery.whereEqualTo(Item.VALIDITY_CHECKED, true);
        parseQuery.include(Item.CATEGORY);

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

    public static Task<List<Item>> getSimilarItemsByCategory(final ArtCategory category, final String itemId) {
        final TaskCompletionSource<List<Item>> tcs = new TaskCompletionSource<>();

        final ParseQuery<Item> parseQuery = new ParseQuery<Item>(Item.class);
        parseQuery.whereEqualTo(Item.CATEGORY, category);
        parseQuery.whereEqualTo(Item.STATUS, STATUS_CODE_VERIFIED);
        parseQuery.whereEqualTo(Item.VALIDITY_CHECKED, true);
        parseQuery.whereNotEqualTo(Item.OBJECT_ID, itemId);

        parseQuery.countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException e) {
                if (e != null) {
                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        tcs.setResult(null);
                    } else {
                        tcs.setError(e);
                    }
                } else {
                    final ParseQuery<Item> query = new ParseQuery<Item>(Item.class);
                    query.whereEqualTo(Item.CATEGORY, category);
                    query.whereEqualTo(Item.STATUS, STATUS_CODE_VERIFIED);
                    query.whereEqualTo(Item.VALIDITY_CHECKED, true);
                    query.whereNotEqualTo(Item.OBJECT_ID, itemId);
                    query.setLimit(6);
                    //TODO ask server to return random results
//                    Random random = new Random();
//                    if (count > 6) {
//                        int randNo = random.nextInt(count - 6);
//                        if (randNo > 0) {
//                            query.setSkip(randNo);
//                        }
//                    }
                    query.findInBackground(new FindCallback<Item>() {
                        @Override
                        public void done(List<Item> items, ParseException e) {
                            if (e != null) {
                                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                                    tcs.setResult(null);
                                } else {
                                    tcs.setError(e);
                                }
                            } else {
                                tcs.setResult(items);
                            }
                        }
                    });

                }
            }
        });
        return tcs.getTask();
    }


    public static Task<ParseObject> getItemById(final String itemId) {
        final TaskCompletionSource<ParseObject> tcs = new TaskCompletionSource<>();

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(OBJECT_NAME);
        parseQuery.whereEqualTo(VALIDITY_CHECKED, true);
        parseQuery.whereEqualTo(STATUS, Store.STATUS_CODE_VERIFIED);
        parseQuery.whereEqualTo(OBJECT_ID, itemId);
        parseQuery.include(STORE);
        parseQuery.include("store.province");
        parseQuery.include("store.city");

        if (!NetworkManager.getInstance().isNetworkEnabled(false)) {
            tcs.setError(new NetworkErrorException("No network connection."));
            return tcs.getTask();
        }

        parseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject item, ParseException e) {
                if (e == null) {
                    tcs.trySetResult(item);
                } else {
                    tcs.trySetError(e);
                    if (BuildConfig.DEBUG) {
                        Log.e(DEBUG_TAG,
                                "Error getting item info for " + itemId + ". Error Code: " + e.getCode() + " //  Error Msg: " + e.getMessage(), e);
                    } else {
                        Crashlytics.log(Log.ERROR, DEBUG_TAG, "Error getting item info for " + item + ". Error Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e);
                    }
                }
            }
        });

        return tcs.getTask();
    }

    public static Task<List<Item>> search(final String searchTerm) {

        //TODO ask server for random results
        //TODO ask server to search for either persian and english texts

        final TaskCompletionSource<List<Item>> tcs = new TaskCompletionSource<>();
        ParseQuery<Item> parseQuery = new ParseQuery<Item>(Item.class);
        parseQuery.whereContains(Item.NAME, searchTerm);
        parseQuery.whereEqualTo(Item.STATUS, STATUS_CODE_VERIFIED);
        parseQuery.whereEqualTo(Item.VALIDITY_CHECKED, true);
        parseQuery.setLimit(50);

        parseQuery.findInBackground(new FindCallback<Item>() {
            @Override
            public void done(final List<Item> items, ParseException e) {
                if (e == null) {
                    tcs.trySetResult(items);
                } else {
                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        if (BuildConfig.DEBUG) {
                            Log.d(DEBUG_TAG, "Searching items with search term " + searchTerm + " does not have any results.");
                        }
                        tcs.trySetResult(null);
                    } else {
                        tcs.trySetError(e);
                        if (BuildConfig.DEBUG) {
                            Log.e(DEBUG_TAG,
                                    "Searching items with search term " + searchTerm + " failed Error Code: " + e.getCode() + " //  Error Msg: " + e.getMessage(), e);
                        } else {
                            Crashlytics.log(Log.ERROR, DEBUG_TAG, "Searching items with search term " + searchTerm + " failed Error Code: " + e.getCode() + " //  Error Msg: " + e.getMessage() + " // Error: " + e);
                        }
                    }
                }
            }
        });
        return tcs.getTask();
    }

}
