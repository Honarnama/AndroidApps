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

    public Task<Void> startSyncing(Context context) {

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        mContext = context;
        final TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        Toast.makeText(mContext, "startSyncing", Toast.LENGTH_SHORT).show();

        final ProgressDialog receivingDataProgressDialog = new ProgressDialog(mContext);
        receivingDataProgressDialog.setCancelable(false);
        receivingDataProgressDialog.setMessage(mContext.getResources().getString(R.string.receiving_data));
        receivingDataProgressDialog.show();

        cacheCategories().continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    if (BuildConfig.DEBUG) {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "Receiving remote data for categories failed. Code: " + task.getError() +
                                "//" + task.getError().getMessage());
                    } else {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Receiving remote data for categories failed. Code: " + task.getError() +
                                "//" + task.getError().getMessage());
                    }
                }
                return cacheProvinces();
            }
        }).continueWith(new Continuation<Void, Object>() {
            @Override
            public Object then(Task<Void> task) throws Exception {
                receivingDataProgressDialog.dismiss();
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                if (task.isFaulted()) {
                    editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_SYNCED, false);
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
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "finding remote data for "+parseQuery.getClassName()+" failed. Code: "+e.getCode() +" // "+ e.getMessage());
                    } else {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "finding remote data for "+parseQuery.getClassName()+" failed.");
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
                                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "recacheCategoryAsync failed. Code: "+e.getCode() +" // "+ e.getMessage());
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
        ParseQuery<Province> parseQuery = ParseQuery.getQuery(Province.class);
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

        ParseObject.unpinAllInBackground(Province.OBJECT_NAME, new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    ParseObject.pinAllInBackground(Province.OBJECT_NAME, categories, new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        tcs.setResult(null);
                                    } else {
                                        if (BuildConfig.DEBUG) {
                                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "recacheProvincesAsync failed. Code: "+e.getCode() +" // "+ e.getMessage());
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
