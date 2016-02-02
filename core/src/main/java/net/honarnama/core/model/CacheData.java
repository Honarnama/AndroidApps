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
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * Created by elnaz on 1/9/16.
 */

//TODO cache sstore

public class CacheData extends HonarnamaBaseModel {

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
                    logE("Caching categories task failed. Error Msg : " + task.getError().getMessage(), " // task error: " + task.getError(), task.getError());
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
                    logE("Caching provinces task failed. Error Msg : " + task.getError().getMessage(), " // task error: " + task.getError(), task.getError());
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
                    logE("Caching cities task failed. Error Msg : " + task.getError().getMessage(), " // task error: " + task.getError(), task.getError());
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
                            logE("Caching Store Task Failed. Error Msg : " + task.getError().getMessage(), " // task error: " + task.getError(), task.getError());
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
                            logE("Caching Item Task Failed. Error Msg : " + task.getError().getMessage(), " // task error: " + task.getError(), task.getError());
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
                    logE("caching data task failed.");
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
                    logE("Finding remote data failed. Error Code: " + e.getCode(), " // Error Msg: " + e.getMessage() + " // findind data for object " + parseQuery.getClassName() + " // Error: " + e);
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
                                        logE("Recaching category failed. Error Code: " + e.getCode(), " // Error Msg: " + e.getMessage() + " // Error:" + e, e);
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
                                        logE("Recaching provinces failed. Error Code: " + e.getCode(), " // Error Msg: " + e.getMessage() + " // Error:" + e, e);
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
                                        logE("Recaching cities failed. Error Code: " + e.getCode(), " // Error Msg: " + e.getMessage() + " // Error:" + e, e);
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
                            logD("Caching user store result: User does not have any store yet. Error: " + e);
                        }

                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_STORE_SYNCED, true);
                        editor.commit();
                        tcs.setResult(null);
                    } else {
                        logE("Caching store task failed. Error Code: " + e.getCode(), "// Error Msg: " + e.getMessage() + " // Error: " + e, e);
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
                            logD("User does not have any items yet.");
                        }
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_ITEM_SYNCED, true);
                        editor.commit();
                        tcs.setResult(null);
                    } else {
                        logE("Caching item task failed. Error Code: " + e.getCode(), " // Msg: " + e.getMessage() + " // Error: " + e, e);
                        tcs.trySetError(e);
                    }

                }
            }
        });
        return tcs.getTask();
    }

}
