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

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * Created by elnaz on 1/9/16.
 */
@ParseClassName("Provinces")
public class Provinces extends ParseObject {
    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/provinceModel";

    public static String OBJECT_NAME = "Provinces";
    public static String NAME = "name";
    public static String ORDER = "order";
    public static String OBJECT_ID = "objectId";
    public static String DEFAULT_PROVINCE_ID = "4ADtQvS2KR";
    public static String DEFAULT_PROVINCE_NAME = "آذربایجان شرقی";

    public TreeMap<Number, HashMap<String, String>> mProvincesTreeMap = new TreeMap<Number, HashMap<String, String>>();
    public Context mContext;
//    ProgressDialog mReceivingDataProgressDialog;

    public Provinces() {
        super();
    }

    public String getName() {
        return getString(NAME);
    }

    public Number getOrder() {
        return getNumber(ORDER);
    }

    public Task<TreeMap<Number, HashMap<String, String>>> getOrderedProvinces(Context context) {

        mContext = context;

        final TaskCompletionSource<TreeMap<Number, HashMap<String, String>>> tcs = new TaskCompletionSource<>();

        findProvincesAsync().continueWith(new Continuation<List<Provinces>, Object>() {
            @Override
            public Object then(Task<List<Provinces>> task) throws Exception {
                if (task.isFaulted()) {
                    tcs.trySetError(task.getError());
                } else {

                    List<Provinces> provinces = task.getResult();
                    for (int i = 0; i < provinces.size(); i++) {

                        Provinces province = provinces.get(i);
                        HashMap<String, String> tempMap = new HashMap<String, String>();
                        tempMap.put(province.getObjectId(), province.getName());
                        mProvincesTreeMap.put(province.getOrder(), tempMap);
                    }
                    tcs.trySetResult(mProvincesTreeMap);
                }

                return null;
            }
        });

        return tcs.getTask();
    }


    public Task<List<Provinces>> findProvincesAsync() {
        final TaskCompletionSource<List<Provinces>> tcs = new TaskCompletionSource<>();

        ParseQuery<Provinces> parseQuery = ParseQuery.getQuery(Provinces.class);
        parseQuery.orderByAscending(Provinces.ORDER);
//        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        final SharedPreferences sharedPref = HonarnamaBaseApp.getInstance().getSharedPreferences(HonarnamaUser.getCurrentUser().getUsername(), Context.MODE_PRIVATE);
        if (sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_PROVINCES_SYNCED, false)) {
            if (BuildConfig.DEBUG) {
                Log.d(DEBUG_TAG, "Getting provinces list from Local datastore");
            }
            parseQuery.fromLocalDatastore();
        } else {

            if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                tcs.setError(new NetworkErrorException("Network connection failed"));
                return tcs.getTask();
            }
//            mReceivingDataProgressDialog.show();
        }


        parseQuery.findInBackground(new FindCallback<Provinces>() {
            @Override
            public void done(final List<Provinces> provincesList, ParseException e) {
                if (e == null) {
//                    if (mReceivingDataProgressDialog.isShowing()) {
//                        mReceivingDataProgressDialog.dismiss();
//                    }

                    if (!sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_PROVINCES_SYNCED, false)) {
                        ParseObject.unpinAllInBackground(Provinces.OBJECT_NAME, provincesList, new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    ParseObject.pinAllInBackground(Provinces.OBJECT_NAME, provincesList, new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        SharedPreferences.Editor editor = sharedPref.edit();
                                                        editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_PROVINCES_SYNCED, true);
                                                        editor.commit();
                                                    }

                                                }
                                            }
                                    );
                                }

                            }
                        });
                    }
                    tcs.trySetResult(provincesList);
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.e(DEBUG_TAG, "Finding provinces failed. Code: " + e.getCode() + " // " + e.getMessage(), e);
                    } else {
                        Crashlytics.log(Log.ERROR, DEBUG_TAG, "Finding provinces failed. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e);
                    }
                    tcs.trySetError(e);
                }
            }
        });
        return tcs.getTask();
    }

    public Task<Provinces> getProvinceById(String provinceId) {
        final TaskCompletionSource<Provinces> tcs = new TaskCompletionSource<>();

        ParseQuery<Provinces> parseQuery = ParseQuery.getQuery(Provinces.class);
        parseQuery.whereEqualTo(OBJECT_ID, provinceId);
        final SharedPreferences sharedPref = HonarnamaBaseApp.getInstance().getSharedPreferences(HonarnamaUser.getCurrentUser().getUsername(), Context.MODE_PRIVATE);
        if (sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_PROVINCES_SYNCED, false)) {
            if (BuildConfig.DEBUG) {
                Log.d(DEBUG_TAG, "Getting province by id from local datastore");
            }
            parseQuery.fromLocalDatastore();
        } else {
            if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                tcs.setError(new NetworkErrorException("Network connection failed"));
                return tcs.getTask();
            }
        }

        parseQuery.getFirstInBackground(new GetCallback<Provinces>() {
            @Override
            public void done(Provinces province, ParseException e) {
                if (e == null) {
                    tcs.trySetResult(province);
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.e(DEBUG_TAG, "Finding province by id failed.Code: " + e.getCode() + " // " + e.getMessage(), e);
                    } else {
                        Crashlytics.log(Log.ERROR, DEBUG_TAG, "Finding province by id failed.Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e);
                    }
                    tcs.trySetError(e);
                }
            }
        });
        return tcs.getTask();
    }

}
