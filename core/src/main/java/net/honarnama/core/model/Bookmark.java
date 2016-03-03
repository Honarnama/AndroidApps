package net.honarnama.core.model;

import com.crashlytics.android.Crashlytics;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.utils.HonarnamaUser;

import android.util.Log;
import android.widget.Toast;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * Created by elnaz on 3/2/16.
 */
@ParseClassName("Bookmark")
public class Bookmark extends ParseObject {

    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/bookmarkModel";

    public static String OBJECT_NAME = "Bookmark";

    public static String ITEM = "item";
    public static String OBJECT_ID = "objectId";

    public Bookmark() {
        super();
    }

    public Bookmark(Item item) {
        super();
        put(ITEM, item);
    }

    public Item getItem() {
        return (Item) getParseObject(ITEM);
    }

    public static Task<Void> bookmarkItem(final Item item) {
        final TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        isBookmarkedAlready(item).continueWith(new Continuation<Boolean, Object>() {
            @Override
            public Object then(Task<Boolean> task) throws Exception {
                if (task.isFaulted()) {
                    tcs.trySetError(task.getError());
                } else {
                    if (task.getResult()) {
                        //item already is bookmarked
                        tcs.trySetResult(null);
                    }
                    Bookmark bookmark = new Bookmark(item);
                    bookmark.pinInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                tcs.trySetResult(null);
                            } else {
                                if (BuildConfig.DEBUG) {
                                    Log.e(DEBUG_TAG, "Saving bookmark failed. Item: " + item + " // Error: " + e);
                                } else {
                                    Crashlytics.log(Log.ERROR, DEBUG_TAG, "Saving bookmark failed. Item: " + item + " // Error: " + e);
                                }
                                tcs.trySetError(e);
                            }
                        }
                    });
                }
                return null;
            }
        });
        return tcs.getTask();
    }


    public static Task<Void> removeBookmark(final Item item) {
        final TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        isBookmarkedAlready(item).continueWith(new Continuation<Boolean, Object>() {
            @Override
            public Object then(Task<Boolean> task) throws Exception {
                if (task.isFaulted()) {
                    tcs.trySetError(task.getError());
                } else {
                    if (!task.getResult()) {
                        //item is not bookmarked
                        tcs.trySetResult(null);
                    }

                    ParseQuery<Bookmark> parseQuery = new ParseQuery<Bookmark>(Bookmark.class);
                    parseQuery.whereEqualTo(Bookmark.ITEM, item);
                    parseQuery.fromLocalDatastore();
                    parseQuery.getFirstInBackground(new GetCallback<Bookmark>() {
                        @Override
                        public void done(final Bookmark bookmark, ParseException e) {
                            if (e == null) {
                                bookmark.deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            tcs.trySetResult(null);
                                        } else {
                                            if (BuildConfig.DEBUG) {
                                                Log.e(DEBUG_TAG, "Removing bookmark failed. Item: " + item + " // Error: " + e);
                                            } else {
                                                Crashlytics.log(Log.ERROR, DEBUG_TAG, "Removing bookmark failed. Item: " + item + " // Error: " + e);
                                            }
                                            tcs.trySetError(e);
                                        }
                                    }
                                });
                            } else {
                                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                                    tcs.trySetResult(null);
                                } else {
                                    if (BuildConfig.DEBUG) {
                                        Log.e(DEBUG_TAG, "Removing bookmark failed. Item: " + item + " // Error: " + e);
                                    } else {
                                        Crashlytics.log(Log.ERROR, DEBUG_TAG, "Removing bookmark failed. Item: " + item + " // Error: " + e);
                                    }
                                    tcs.trySetError(e);
                                }
                            }
                        }
                    });

                }
                return null;
            }
        });
        return tcs.getTask();
    }

    public static Task<Boolean> isBookmarkedAlready(final Item item) {
        final TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();
        ParseQuery<Bookmark> query = ParseQuery.getQuery(Bookmark.class);
        query.whereEqualTo(Bookmark.ITEM, item);
        query.fromLocalDatastore();
        query.getFirstInBackground(new GetCallback<Bookmark>() {
            @Override
            public void done(Bookmark object, ParseException e) {
                if (e == null) {
                    tcs.trySetResult(true);
                } else {
                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        tcs.trySetResult(false);
                    } else {
                        if (BuildConfig.DEBUG) {
                            Log.e(DEBUG_TAG, "Checking if item is already bookmarked failed. Item: " + item + " // Error: " + e);
                        } else {
                            Crashlytics.log(Log.ERROR, DEBUG_TAG, "Checking if item is already bookmarked failed. Item: " + item + " // Error: " + e);
                        }
                        tcs.trySetError(e);
                    }
                }
            }
        });
        return tcs.getTask();
    }


}
