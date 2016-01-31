package net.honarnama.core.model;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.R;
import net.honarnama.core.utils.HonarnamaUser;
import net.honarnama.core.utils.NetworkManager;

import android.accounts.NetworkErrorException;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * Created by elnaz on 1/9/16.
 */

//TODO cache sstore

public class CacheData {

    SharedPreferences mSharedPreferences;
    Context mContext;
    final SharedPreferences.Editor mPrefEditor;

    public CacheData(Context context) {
        mContext = context;
//        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mSharedPreferences = context.getSharedPreferences(HonarnamaUser.getCurrentUser().getUsername(), Context.MODE_PRIVATE);
        mPrefEditor = mSharedPreferences.edit();
    }

    public Task<Void> startSyncing() {

        final TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        final ProgressDialog receivingDataProgressDialog = new ProgressDialog(mContext);
        receivingDataProgressDialog.setCancelable(false);
        receivingDataProgressDialog.setMessage(mContext.getResources().getString(R.string.syncing_data));
        receivingDataProgressDialog.show();

        cacheCategories().continueWith(new Continuation<Void, Object>() {
            @Override
            public Object then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    mPrefEditor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_CATEGORIES_SYNCED, false);
//                    editor.commit();
                    if (BuildConfig.DEBUG) {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "Receiving remote data for categories failed. Code: " + task.getError() +
                                "//" + task.getError().getMessage());
                    } else {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Receiving remote data for categories failed");
                    }
                } else {
                    mPrefEditor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_CATEGORIES_SYNCED, true);
                }
                return null;
            }
        }).continueWithTask(new Continuation<Object, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Object> task) throws Exception {
                return cacheProvinces();
            }
        }).continueWith(new Continuation<Void, Object>() {
            @Override
            public Object then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    mPrefEditor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_PROVINCES_SYNCED, false);
                    if (BuildConfig.DEBUG) {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "Caching provinces task failed. Code: " + task.getError() +
                                "//" + task.getError().getMessage());
                    } else {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Caching provinces task failed.");
                    }
                } else {
                    mPrefEditor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_PROVINCES_SYNCED, true);
                }
                return null;
            }
        }).continueWithTask(new Continuation<Object, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Object> task) throws Exception {
                return cacheCity();
            }
        }).continueWith(new Continuation<Void, Object>() {
            @Override
            public Object then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    mPrefEditor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_CITY_SYNCED, false);
                    if (BuildConfig.DEBUG) {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "Caching cities task failed. Code: " + task.getError() +
                                "//" + task.getError().getMessage());
                    } else {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Caching cities task failed.");
                    }
                } else {
                    mPrefEditor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_CITY_SYNCED, true);
                }
                return null;
            }
        }).continueWithTask(new Continuation<Object, Task<Object>>() {
            @Override
            public Task<Object> then(Task<Object> task) throws Exception {
                return cacheUserStore().continueWith(new Continuation<Store, Object>() {
                    @Override
                    public Object then(Task<Store> task) throws Exception {
                        if (task.isFaulted()) {
                            mPrefEditor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_STORE_SYNCED, false);
                            if (BuildConfig.DEBUG) {
                                Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "Caching Store Task Failed. Code: " + task.getError() +
                                        "//" + task.getError().getMessage());
                            } else {
                                Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Caching Store Task Failed.");
                            }
                        } else {
                            mPrefEditor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_STORE_SYNCED, true);
                        }
                        return null;
                    }
                });
            }
        }).continueWithTask(new Continuation<Object, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Object> task) throws Exception {
                return cacheUserItems().continueWith(new Continuation<Void, Void>() {
                    @Override
                    public Void then(Task<Void> task) throws Exception {
                        if (task.isFaulted()) {
                            mPrefEditor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_ITEM_SYNCED, false);
                            if (BuildConfig.DEBUG) {
                                Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "Caching Item Task Failed. Code: " + task.getError() +
                                        "//" + task.getError().getMessage());
                            } else {
                                Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Caching Item Task Failed.");
                            }
                        } else {
                            mPrefEditor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_ITEM_SYNCED, true);
                        }
                        return null;
                    }
                });
            }
        }).continueWith(new Continuation<Void, Object>() {
            @Override
            public Object then(Task<Void> task) throws Exception {
                mPrefEditor.commit();
                receivingDataProgressDialog.dismiss();
                if (mSharedPreferences.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_CATEGORIES_SYNCED, false) &&
                        mSharedPreferences.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_PROVINCES_SYNCED, false) &&
                        mSharedPreferences.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_CITY_SYNCED, false) &&
                        mSharedPreferences.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_STORE_SYNCED, false) &&
                        mSharedPreferences.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_ITEM_SYNCED, false)
                        ) {
                    mPrefEditor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_SYNCED, true);
                    mPrefEditor.commit();
                    tcs.trySetResult(null);
                } else {
                    Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "caching data task failed.");
                    mPrefEditor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_SYNCED, false);
                    mPrefEditor.commit();
                    tcs.trySetError(new RuntimeException("Syncing data failed."));
                }
                return null;
            }
        });

        return tcs.getTask();
    }

    public Task<Void> cacheCategories() {
        ParseQuery<Category> parseQuery = ParseQuery.getQuery(Category.class);
        return findAsync(parseQuery).onSuccessTask(new Continuation<List<ParseObject>, Task<Void>>() {
            @Override
            public Task<Void> then(Task<List<ParseObject>> task) throws Exception {
                List<ParseObject> categories = task.getResult();
                return recacheCategoryAsync(categories);
            }
        });
    }


    public Task<List<ParseObject>> findAsync(final ParseQuery parseQuery) {
        final TaskCompletionSource<List<ParseObject>> tcs = new TaskCompletionSource<>();
        if (!NetworkManager.getInstance().isNetworkEnabled(false)) {
            tcs.trySetError(new NetworkErrorException("No Network connection"));
            return tcs.getTask();
        }
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    tcs.trySetResult(objects);
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "finding remote data for " + parseQuery.getClassName() + " failed. Code: " + e.getCode() + " // " + e.getMessage());
                    } else {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "finding remote data for " + parseQuery.getClassName() + " failed.");
                    }
                    tcs.trySetError(e);
                }
            }
        });
        return tcs.getTask();
    }

    public Task<Void> recacheCategoryAsync(final List<ParseObject> categories) {
        final TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        ParseObject.unpinAllInBackground(Category.OBJECT_NAME, new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    ParseObject.pinAllInBackground(Category.OBJECT_NAME, categories, new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        if (BuildConfig.DEBUG) {
                                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "recacheCategoryAsync failed. Code: " + e.getCode() + " // " + e.getMessage());
                                        } else {
                                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "recaching category failed.");
                                        }
                                        tcs.trySetError(e);
                                    } else {
                                        tcs.trySetResult(null);
                                    }
                                }
                            }
                    );
                } else {
                    tcs.trySetError(e);
                }
            }
        });
        return tcs.getTask();
    }

    public Task<Void> cacheProvinces() {
        ParseQuery<Provinces> parseQuery = ParseQuery.getQuery(Provinces.class);
        return findAsync(parseQuery).onSuccessTask(new Continuation<List<ParseObject>, Task<Void>>() {
            @Override
            public Task<Void> then(Task<List<ParseObject>> task) throws Exception {
                List<ParseObject> provinces = task.getResult();
                return recacheProvincesAsync(provinces);
            }
        });
    }

    public Task<Void> recacheProvincesAsync(final List<ParseObject> categories) {
        final TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        ParseObject.unpinAllInBackground(Provinces.OBJECT_NAME, new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    ParseObject.pinAllInBackground(Provinces.OBJECT_NAME, categories, new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        tcs.trySetResult(null);
                                    } else {
                                        if (BuildConfig.DEBUG) {
                                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "recacheProvincesAsync failed. Code: " + e.getCode() + " // " + e.getMessage());
                                        } else {
                                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "recaching provinces failed.");
                                        }
                                        tcs.trySetError(e);
                                    }
                                }
                            }
                    );
                } else {
                    tcs.trySetError(e);
                }
            }
        });
        return tcs.getTask();
    }

    public Task<Void> cacheCity() {
        ParseQuery<City> parseQuery = ParseQuery.getQuery(City.class);
        parseQuery.setLimit(10000);
        return findAsync(parseQuery).onSuccessTask(new Continuation<List<ParseObject>, Task<Void>>() {
            @Override
            public Task<Void> then(Task<List<ParseObject>> task) throws Exception {
                List<ParseObject> cityList = task.getResult();
                return recacheCityAsync(cityList);
            }
        });
    }


    public Task<Void> recacheCityAsync(final List<ParseObject> cityList) {
        final TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        ParseObject.unpinAllInBackground(City.OBJECT_NAME, new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    ParseObject.pinAllInBackground(City.OBJECT_NAME, cityList, new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        tcs.trySetResult(null);
                                    } else {
                                        if (BuildConfig.DEBUG) {
                                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "recacheCityAsync failed. Code: " + e.getCode() + " // " + e.getMessage());
                                        } else {
                                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "recaching cities failed.");
                                        }
                                        tcs.trySetError(e);
                                    }
                                }
                            }
                    );
                } else {
                    tcs.trySetError(e);
                }
            }
        });
        return tcs.getTask();
    }

    public Task<Store> cacheUserStore() {
        final TaskCompletionSource<Store> tcs = new TaskCompletionSource<>();
        ParseQuery<Store> query = ParseQuery.getQuery(Store.class);
        query.whereEqualTo(Store.OWNER, HonarnamaUser.getCurrentUser());

        if (!NetworkManager.getInstance().isNetworkEnabled(false)) {
            tcs.trySetError(new NetworkErrorException("No Network connection"));
            return tcs.getTask();
        }

        query.getFirstInBackground(new GetCallback<Store>() {
            @Override
            public void done(final Store store, ParseException e) {
                if (e == null) {
                    tcs.trySetResult(store);

                    final List<Store> tempStoreList = new ArrayList<Store>() {{
                        add(store);
                    }};

                    ParseObject.unpinAllInBackground(Store.OBJECT_NAME, tempStoreList, new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                ParseObject.pinAllInBackground(Store.OBJECT_NAME, tempStoreList, new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                                                    editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_STORE_SYNCED, true);
                                                    editor.commit();
                                                } else {
                                                    tcs.trySetError(e);
                                                }
                                            }
                                        }
                                );
                            } else {
                                tcs.trySetError(e);
                            }
                        }
                    });

                } else {
                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        if (BuildConfig.DEBUG) {
                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                    "Caching user store result: User does not have any store yet.");
                        }

                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_STORE_SYNCED, true);
                        editor.commit();
                        tcs.setResult(null);
                    } else {
                        if (BuildConfig.DEBUG) {
                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                    "Caching store task failed.  Error Code: " + e.getCode() +
                                            "//" + e.getMessage() + " // " + e, e);
                        } else {
                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Caching store task failed. "
                                    + e.getMessage());
                        }
                        tcs.trySetError(e);
                    }

                }

            }
        });
        return tcs.getTask();
    }

    public Task<Void> cacheUserItems() {
        final TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        ParseQuery<Item> query = ParseQuery.getQuery(Item.class);
        query.whereEqualTo(Item.OWNER, HonarnamaUser.getCurrentUser());

        if (!NetworkManager.getInstance().isNetworkEnabled(false)) {
            tcs.trySetError(new NetworkErrorException("No Network connection"));
            return tcs.getTask();
        }

        query.findInBackground(new FindCallback<Item>() {
            @Override
            public void done(final List<Item> items, ParseException e) {
                if (e == null) {
                    tcs.setResult(null);
                    ParseObject.unpinAllInBackground(Item.OBJECT_NAME, items, new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                ParseObject.pinAllInBackground(Item.OBJECT_NAME, items, new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {

                                        if (e == null) {
                                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                                            editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_ITEM_SYNCED, true);
                                            editor.commit();
                                        } else {
                                            tcs.trySetError(e);
                                        }
                                    }
                                });
                            } else {
                                tcs.trySetError(e);
                            }
                        }
                    });
                } else {
                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        if (BuildConfig.DEBUG) {
                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                    "User does not have any items yet.");
                        }
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_ITEM_SYNCED, true);
                        editor.commit();
                        tcs.setResult(null);
                    } else {
                        if (BuildConfig.DEBUG) {
                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                    "Caching item task failed.  Error Code: " + e.getCode() +
                                            "//" + e.getMessage() + " // " + e, e);
                        } else {
                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Caching item task failed. "
                                    + e.getMessage());
                        }
                        tcs.trySetError(e);
                    }

                }
            }
        });
        return tcs.getTask();
    }

}
