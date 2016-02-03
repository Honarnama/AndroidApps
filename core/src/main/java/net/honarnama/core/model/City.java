package net.honarnama.core.model;

import com.crashlytics.android.Crashlytics;
import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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
@ParseClassName("City")
public class City extends ParseObject {

    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/cityModel";

    public static String OBJECT_NAME = "City";
    public static String NAME = "name";
    public static String ORDER = "order";
    public static String OBJECT_ID = "objectId";
    public static String PARENT_ID = "parentId";

    public static String DEFAULT_CITY_ID = "9AXzdV8WWV";
    public static String DEFAULT_CITY_NAME = "آذرشهر";

    public static HashMap<String, String> mDefaultCitiesHashMap = new HashMap<String, String>();


    public TreeMap<Number, HashMap<String, String>> mCityOrderedTreehMap = new TreeMap<Number, HashMap<String, String>>();
    public Context mContext;

    public City() {
        super();
    }

    public String getName() {
        return getString(NAME);
    }

    public Number getOrder() {
        return getNumber(ORDER);
    }

    public Task<TreeMap<Number, HashMap<String, String>>> getOrderedCities(Context context, String parentId) {

        mContext = context;

        final TaskCompletionSource<TreeMap<Number, HashMap<String, String>>> tcs = new TaskCompletionSource<>();

        findCitiesAsync(parentId).continueWith(new Continuation<List<City>, Object>() {
            @Override
            public Object then(Task<List<City>> task) throws Exception {
                if (task.isFaulted()) {
                    tcs.trySetError(task.getError());
                } else {

                    List<City> cities = task.getResult();
                    for (int i = 0; i < cities.size(); i++) {
                        City city = cities.get(i);
                        HashMap<String, String> tempMap = new HashMap<String, String>();
                        tempMap.put(city.getObjectId(), city.getName());
                        mCityOrderedTreehMap.put(city.getOrder(), tempMap);
                    }
                    tcs.trySetResult(mCityOrderedTreehMap);
                }

                return null;
            }
        });

        return tcs.getTask();
    }


    public Task<List<City>> findCitiesAsync(String parentId) {
        final TaskCompletionSource<List<City>> tcs = new TaskCompletionSource<>();

        ParseQuery<City> parseQuery = ParseQuery.getQuery(City.class);
        parseQuery.whereEqualTo(PARENT_ID, parentId);
        parseQuery.orderByAscending(ORDER);

//        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        final SharedPreferences sharedPref = HonarnamaBaseApp.getInstance().getSharedPreferences(HonarnamaUser.getCurrentUser().getUsername(), Context.MODE_PRIVATE);

        if (sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_CITY_SYNCED, false)) {
            if (BuildConfig.DEBUG) {
                Log.d(DEBUG_TAG, " Getting city list from LocalDatastore");
            }
            parseQuery.fromLocalDatastore();
        } else {

            if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                tcs.setError(new NetworkErrorException("Network connection failed"));
                return tcs.getTask();
            }
//            mReceivingDataProgressDialog.show();
        }


        parseQuery.findInBackground(new FindCallback<City>() {
            @Override
            public void done(final List<City> cityList, ParseException e) {
                if (e == null) {
//                    if (mReceivingDataProgressDialog.isShowing()) {
//                        mReceivingDataProgressDialog.dismiss();
//                    }
                    tcs.trySetResult(cityList);
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.e(DEBUG_TAG, "Finding cities failed. Code: " + e.getCode() + " // " + e.getMessage());
                    } else {
                        Crashlytics.log(Log.ERROR, DEBUG_TAG, "Finding  city failed. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error:" + e);
                    }
                    tcs.trySetError(e);
                }
            }
        });
        return tcs.getTask();
    }

}
