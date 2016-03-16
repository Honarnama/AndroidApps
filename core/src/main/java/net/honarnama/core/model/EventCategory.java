package net.honarnama.core.model;

import com.crashlytics.android.Crashlytics;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.utils.HonarnamaUser;
import net.honarnama.core.utils.NetworkManager;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

@ParseClassName("event_category")
public class EventCategory extends ParseObject {
    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/eventCatModel";

    public static String OBJECT_NAME = "event_category";
    public static String NAME = "name";
    public static String ORDER = "order";
    public static String OBJECT_ID = "objectId";


    public TreeMap<Number, HashMap<String, String>> mEventCatsTreeMap = new TreeMap<Number, HashMap<String, String>>();
    public TreeMap<Number, EventCategory> mEventCatObjectTreeMap = new TreeMap<Number, EventCategory>();

    public EventCategory() {
        super();
    }


    public String getName() {
        return getString(NAME);
    }

    public Number getOrder() {
        return getNumber(ORDER);
    }

    public static Task<String> getCategoryNameById(String categoryId) {
        final TaskCompletionSource<String> tcs = new TaskCompletionSource<>();

        ParseQuery<EventCategory> parseQuery = ParseQuery.getQuery(EventCategory.class);
        parseQuery.whereEqualTo(OBJECT_ID, categoryId);
        final SharedPreferences sharedPref = HonarnamaBaseApp.getInstance().getSharedPreferences(HonarnamaUser.getCurrentUser().getUsername(), Context.MODE_PRIVATE);
        if (sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_EVENT_CATEGORIES_SYNCED, false)) {
            if (BuildConfig.DEBUG) {
                Log.d(DEBUG_TAG, "Getting event categories from local datastore");
            }
            parseQuery.fromLocalDatastore();
        } else {

            if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                tcs.setError(new NetworkErrorException("Network connection failed"));
                return tcs.getTask();
            }
        }
        parseQuery.getFirstInBackground(new GetCallback<EventCategory>() {
            @Override
            public void done(EventCategory category, ParseException e) {
                if (e == null) {
                    tcs.trySetResult(category.getName());
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.e(DEBUG_TAG, "Finding  event category failed. Code: " + e.getCode() + " // Msg: " + e.getMessage(), e);
                    } else {
                        Crashlytics.log(Log.ERROR, DEBUG_TAG, "Finding  event category failed. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error:" + e);
                    }
                    tcs.trySetError(e);
                }
            }
        });
        return tcs.getTask();
    }

    public static Task<EventCategory> getCategoryById(String catId, String callingAppKey) {
        final TaskCompletionSource<EventCategory> tcs = new TaskCompletionSource<>();

        ParseQuery<EventCategory> parseQuery = ParseQuery.getQuery(EventCategory.class);
        parseQuery.whereEqualTo(OBJECT_ID, catId);

        String sharedPrefKey = "";
        if (HonarnamaUser.getCurrentUser() == null || callingAppKey == HonarnamaBaseApp.BROWSE_APP_KEY) {
            sharedPrefKey = HonarnamaBaseApp.BROWSE_APP_KEY;
        } else {
            sharedPrefKey = HonarnamaUser.getCurrentUser().getUsername();
        }

        final SharedPreferences sharedPref = HonarnamaBaseApp.getInstance().getSharedPreferences(sharedPrefKey, Context.MODE_PRIVATE);
        if (sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_EVENT_CATEGORIES_SYNCED, false)) {
            if (BuildConfig.DEBUG) {
                Log.d(DEBUG_TAG, "Getting event category by id from local datastore");
            }
            parseQuery.fromLocalDatastore();
        } else {
            if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                tcs.setError(new NetworkErrorException("Network connection failed"));
                return tcs.getTask();
            }
        }

        parseQuery.getFirstInBackground(new GetCallback<EventCategory>() {
            @Override
            public void done(EventCategory category, ParseException e) {
                if (e == null) {
                    tcs.trySetResult(category);
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.e(DEBUG_TAG, "Finding event category by id failed. Code: " + e.getCode() + " // " + e.getMessage(), e);
                    } else {
                        Crashlytics.log(Log.ERROR, DEBUG_TAG, "Finding event category by id failed. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e);
                    }
                    tcs.trySetError(e);
                }
            }
        });
        return tcs.getTask();
    }


    public static Task<List<EventCategory>> getCategoriesById(final ArrayList<String> catIds, String callingAppKey) {
        final TaskCompletionSource<List<EventCategory>> tcs = new TaskCompletionSource<>();


        ParseQuery<EventCategory> parseQuery = ParseQuery.getQuery(EventCategory.class);
        parseQuery.whereContainedIn(OBJECT_ID, catIds);

        String sharedPrefKey;
        if (HonarnamaUser.getCurrentUser() == null || callingAppKey == HonarnamaBaseApp.BROWSE_APP_KEY) {
            sharedPrefKey = HonarnamaBaseApp.BROWSE_APP_KEY;
        } else {
            sharedPrefKey = HonarnamaUser.getCurrentUser().getUsername();
        }

        final SharedPreferences sharedPref = HonarnamaBaseApp.getInstance().getSharedPreferences(sharedPrefKey, Context.MODE_PRIVATE);
        if (sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_EVENT_CATEGORIES_SYNCED, false)) {
            if (BuildConfig.DEBUG) {
                Log.d(DEBUG_TAG, "Getting event category by id from local datastore");
            }
            parseQuery.fromLocalDatastore();
        } else {
            if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                tcs.setError(new NetworkErrorException("Network connection failed"));
                return tcs.getTask();
            }
        }

        parseQuery.findInBackground(new FindCallback<EventCategory>() {
            @Override
            public void done(List<EventCategory> categories, ParseException e) {
                if (e == null) {
                    tcs.trySetResult(categories);
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.e(DEBUG_TAG, "Finding event categories with id " + catIds + " failed. Code: " + e.getCode() + " // " + e.getMessage(), e);
                    } else {
                        Crashlytics.log(Log.ERROR, DEBUG_TAG, "Finding event categories with id " + catIds + " failed. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e);
                    }
                    tcs.trySetError(e);
                }
            }
        });
        return tcs.getTask();
    }

    public Task<TreeMap<Number, EventCategory>> getOrderedEventCategories() {

        final TaskCompletionSource<TreeMap<Number, EventCategory>> tcs = new TaskCompletionSource<>();

        findEventCatsAsync().continueWith(new Continuation<List<EventCategory>, Object>() {
            @Override
            public Object then(Task<List<EventCategory>> task) throws Exception {
                if (task.isFaulted()) {
                    tcs.trySetError(task.getError());
                } else {

                    List<EventCategory> categories = task.getResult();
                    for (int i = 0; i < categories.size(); i++) {

                        EventCategory eventCategory = categories.get(i);
                        mEventCatObjectTreeMap.put(eventCategory.getOrder(), eventCategory);
                    }
                    tcs.trySetResult(mEventCatObjectTreeMap);
                }

                return null;
            }
        });

        return tcs.getTask();
    }


    public Task<List<EventCategory>> findEventCatsAsync() {
        final TaskCompletionSource<List<EventCategory>> tcs = new TaskCompletionSource<>();

        ParseQuery<EventCategory> parseQuery = ParseQuery.getQuery(EventCategory.class);
        parseQuery.orderByAscending(EventCategory.ORDER);

        String sharedPrefKey;
        if (HonarnamaUser.getCurrentUser() == null) {
            sharedPrefKey = HonarnamaBaseApp.BROWSE_APP_KEY;
        } else {
            sharedPrefKey = HonarnamaUser.getCurrentUser().getUsername();
        }

        final SharedPreferences sharedPref = HonarnamaBaseApp.getInstance().getSharedPreferences(sharedPrefKey, Context.MODE_PRIVATE);
        if (sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_EVENT_CATEGORIES_SYNCED, false)) {
            if (BuildConfig.DEBUG) {
                Log.d(DEBUG_TAG, "Getting event cat list from Local datastore");
            }
            parseQuery.fromLocalDatastore();
        } else {

            if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                tcs.setError(new NetworkErrorException("Network connection failed"));
                return tcs.getTask();
            }
        }


        parseQuery.findInBackground(new FindCallback<EventCategory>() {
            @Override
            public void done(final List<EventCategory> eventCatsList, ParseException e) {
                if (e == null) {
                    if (!sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_EVENT_CATEGORIES_SYNCED, false)) {
                        ParseObject.unpinAllInBackground(EventCategory.OBJECT_NAME, eventCatsList, new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    ParseObject.pinAllInBackground(EventCategory.OBJECT_NAME, eventCatsList, new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        SharedPreferences.Editor editor = sharedPref.edit();
                                                        editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_EVENT_CATEGORIES_SYNCED, true);
                                                        editor.commit();
                                                    }

                                                }
                                            }
                                    );
                                }

                            }
                        });
                    }
                    tcs.trySetResult(eventCatsList);
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.e(DEBUG_TAG, "Finding event categories list failed. Code: " + e.getCode() + " // " + e.getMessage(), e);
                    } else {
                        Crashlytics.log(Log.ERROR, DEBUG_TAG, "Finding event categories failed. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e);
                    }
                    tcs.trySetError(e);
                }
            }
        });
        return tcs.getTask();
    }
}
