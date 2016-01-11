package net.honarnama.core.model;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * Created by elnaz on 1/9/16.
 */
public class CacheData {

    SharedPreferences mSharedPreferences;
    Context mContext;

    public Task<Void> startSyncing(final Context context) {

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        mContext = context;
        final TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        Toast.makeText(mContext, "startSyncing", Toast.LENGTH_SHORT).show();

        final ProgressDialog receivingDataProgressDialog = new ProgressDialog(mContext);
        receivingDataProgressDialog.setCancelable(false);
        receivingDataProgressDialog.setMessage(mContext.getResources().getString(R.string.syncing_data));
        receivingDataProgressDialog.show();

        final SharedPreferences.Editor editor = mSharedPreferences.edit();

        cacheCategories().continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {

                if (task.isFaulted()) {
                    editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_CATEGORIES_SYNCED, false);
//                    editor.commit();
                    Toast.makeText(context, context.getResources().getString(R.string.syncing_data_failed), Toast.LENGTH_LONG).show();
                    if (BuildConfig.DEBUG) {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "Receiving remote data for categories failed. Code: " + task.getError() +
                                "//" + task.getError().getMessage());
                    } else {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Receiving remote data for categories failed. Code: " + task.getError() +
                                "//" + task.getError().getMessage());
                    }
                    throw new RuntimeException("Syncing data failed");
                } else {

                    editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_CATEGORIES_SYNCED, true);
//                    editor.commit();

                    return cacheProvinces().continueWith(new Continuation<Void, Void>() {
                        @Override
                        public Void then(Task<Void> task) throws Exception {

                            if (task.isFaulted()) {
                                editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_PROVINCES_SYNCED, false);
                                Toast.makeText(context, context.getResources().getString(R.string.syncing_data_failed), Toast.LENGTH_LONG).show();
                                if (BuildConfig.DEBUG) {
                                    Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "Receiving remote data for provinces failed. Code: " + task.getError() +
                                            "//" + task.getError().getMessage());
                                } else {
                                    Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Receiving remote data for provinces failed. Code: " + task.getError() +
                                            "//" + task.getError().getMessage());
                                }
                                throw new RuntimeException("Syncing data failed");
                            }
                            else {
                                editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_PROVINCES_SYNCED, true);
                            }
//                            editor.commit();
                            return null;
                        }
                    });
                }
            }
        }).continueWith(new Continuation<Void, Object>() {
            @Override
            public Object then(Task<Void> task) throws Exception {
                receivingDataProgressDialog.dismiss();
                if (task.isFaulted()) {
                    editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_SYNCED, false);
                    Toast.makeText(context, context.getResources().getString(R.string.syncing_data_failed), Toast.LENGTH_LONG).show();
                } else {
                    editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_SYNCED, true);
                }
                editor.commit();
                tcs.setResult(null);
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
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    tcs.setResult(objects);
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "finding remote data for " + parseQuery.getClassName() + " failed. Code: " + e.getCode() + " // " + e.getMessage());
                    } else {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "finding remote data for " + parseQuery.getClassName() + " failed.");
                    }
                    tcs.setError(e);
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
                                        tcs.setError(e);
                                    } else {
                                        tcs.setResult(null);
                                    }
                                }
                            }
                    );
                } else {
                    tcs.setError(e);
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
                                        tcs.setResult(null);
                                    } else {
                                        if (BuildConfig.DEBUG) {
                                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "recacheProvincesAsync failed. Code: " + e.getCode() + " // " + e.getMessage());
                                        } else {
                                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "recaching provinces failed.");
                                        }
                                        tcs.setError(e);
                                    }
                                }
                            }
                    );
                } else {
                    tcs.setError(e);
                }
            }
        });
        return tcs.getTask();
    }

}
