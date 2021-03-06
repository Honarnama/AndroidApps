package net.honarnama.base.model;

import com.crashlytics.android.Crashlytics;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.helper.DatabaseHelper;
import net.honarnama.nano.Location;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

import static net.honarnama.base.helper.DatabaseHelper.COL_LOCATIONS_ID;
import static net.honarnama.base.helper.DatabaseHelper.COL_LOCATIONS_NAME;
import static net.honarnama.base.helper.DatabaseHelper.COL_LOCATIONS_ORDER;
import static net.honarnama.base.helper.DatabaseHelper.COL_LOCATIONS_PARENT_ID;
import static net.honarnama.base.helper.DatabaseHelper.COL_LOCATIONS_TYPE;


/**
 * Created by elnaz on 1/9/16.
 */
public class City {

    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/cityModel";

    public static String TABLE_NAME = DatabaseHelper.TABLE_LOCATIONS;
    public String mName;
    public int mOrder;
    public int mId;
    public int mParentId;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getOrder() {
        return mOrder;
    }

    public void setOrder(int order) {
        mOrder = order;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getParentId() {
        return mParentId;
    }

    public void setParentId(int parentId) {
        mParentId = parentId;
    }

    public static int ALL_CITY_ID = 0;
    public static String ALL_CITY_NAME = "تمام شهرها";

    public TreeMap<Number, HashMap<Integer, String>> mCityOrderedTreehMap = new TreeMap<>();

    public Task<TreeMap<Number, HashMap<Integer, String>>> getAllCitiesSorted(final int parentId) {

        final TaskCompletionSource<TreeMap<Number, HashMap<Integer, String>>> tcs = new TaskCompletionSource<>();

        findCitiesAsync(parentId).continueWith(new Continuation<List<City>, Object>() {
            @Override
            public Object then(Task<List<City>> task) throws Exception {
                if (task.isFaulted()) {
                    tcs.trySetError(task.getError());
                } else {
                    List<City> cities = task.getResult();

                    for (int i = 0; i < cities.size(); i++) {
                        City city = cities.get(i);
                        HashMap<Integer, String> tempMap = new HashMap();
                        tempMap.put(city.getId(), city.getName());
                        mCityOrderedTreehMap.put(city.getOrder(), tempMap);
                    }

                    if (BuildConfig.DEBUG) {
                        Log.d(DEBUG_TAG, "City list: " + mCityOrderedTreehMap);
                    }
                    tcs.trySetResult(mCityOrderedTreehMap);
                }

                return null;
            }
        });

        return tcs.getTask();
    }


    public Task<List<City>> findCitiesAsync(int parentId) {
        final TaskCompletionSource<List<City>> tcs = new TaskCompletionSource<>();
        List<City> cities = new ArrayList<>();

        SQLiteDatabase db = DatabaseHelper.getInstance(HonarnamaBaseApp.getInstance()).getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_LOCATIONS_TYPE + " = " + Location.CITY +
                " AND " + COL_LOCATIONS_PARENT_ID + " = " + parentId + " ORDER BY " + COL_LOCATIONS_ORDER + " ASC";
        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    net.honarnama.base.model.City city = new net.honarnama.base.model.City();
                    city.setId(cursor.getInt(cursor.getColumnIndex(COL_LOCATIONS_ID)));
                    city.setName(cursor.getString(cursor.getColumnIndex(COL_LOCATIONS_NAME)));
                    city.setOrder(cursor.getInt(cursor.getColumnIndex(COL_LOCATIONS_ORDER)));
                    cities.add(city);
                } while (cursor.moveToNext());
                tcs.trySetResult(cities);
            } else {
                if (BuildConfig.DEBUG) {
                    Log.e(DEBUG_TAG, "No cities for specified province id " + parentId + "found.");
                }
                tcs.trySetError(new Exception("No cities for specified province id " + parentId + "found."));
            }

        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(DEBUG_TAG, "Error while trying to get city list for province: " + parentId, e);
            } else {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String stackTrace = sw.toString();
                Crashlytics.log(Log.ERROR, DEBUG_TAG, "Error while trying to get city list for province: " + parentId + ". // Error: " + e + ". stackTrace: " + stackTrace);
            }
            tcs.trySetError(e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return tcs.getTask();
    }

    public static City getCityById(int cityId) {
//        final TaskCompletionSource<City> tcs = new TaskCompletionSource<>();

        SQLiteDatabase db = DatabaseHelper.getInstance(HonarnamaBaseApp.getInstance()).getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_LOCATIONS_TYPE + " = " + Location.CITY +
                " AND id = " + cityId;
        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor != null) {
                cursor.moveToFirst();
                City city = new City();
                city.setId(cursor.getInt(cursor.getColumnIndex(COL_LOCATIONS_ID)));
                city.setName(cursor.getString(cursor.getColumnIndex(COL_LOCATIONS_NAME)));
                city.setOrder(cursor.getInt(cursor.getColumnIndex(COL_LOCATIONS_ORDER)));
                return city;
            } else {
                if (BuildConfig.DEBUG) {
                    Log.d(DEBUG_TAG, "Error while trying to get city from database. City not found for cityId " + cityId);
                }
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(DEBUG_TAG, "Error while trying to get city from database.", e);
            } else {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String stackTrace = sw.toString();
                Crashlytics.log(Log.ERROR, DEBUG_TAG, "Error while trying to get city from database. // Error: " + e + ". stackTrace: " + stackTrace);
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return null;
    }

}
