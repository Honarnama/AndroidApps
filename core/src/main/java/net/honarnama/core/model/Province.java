package net.honarnama.core.model;

import com.crashlytics.android.Crashlytics;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.helper.DatabaseHelper;
import net.honarnama.nano.Location;

import android.content.Context;
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

import static net.honarnama.core.helper.DatabaseHelper.COL_LOCATIONS_ID;
import static net.honarnama.core.helper.DatabaseHelper.COL_LOCATIONS_NAME;
import static net.honarnama.core.helper.DatabaseHelper.COL_LOCATIONS_ORDER;
import static net.honarnama.core.helper.DatabaseHelper.COL_LOCATIONS_TYPE;

/**
 * Created by elnaz on 1/9/16.
 */
public class Province {
    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/provinceModel";

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

    public TreeMap<Number, HashMap<String, String>> mProvincesTreeMap = new TreeMap<Number, HashMap<String, String>>();
    public TreeMap<Number, Province> mProvincesObjectTreeMap = new TreeMap<Number, Province>();


    public Task<TreeMap<Number, Province>> getAllProvincesSorted() {

        final TaskCompletionSource<TreeMap<Number, Province>> tcs = new TaskCompletionSource<>();

        findProvincesAsync().continueWith(new Continuation<List<Province>, Object>() {
            @Override
            public Object then(Task<List<Province>> task) throws Exception {
                if (task.isFaulted()) {
                    tcs.trySetError(task.getError());
                } else {

                    List<Province> provinces = task.getResult();
                    for (int i = 0; i < provinces.size(); i++) {

                        Province province = provinces.get(i);
                        mProvincesObjectTreeMap.put(province.getOrder(), province);
                    }
                    tcs.trySetResult(mProvincesObjectTreeMap);
                }

                return null;
            }
        });

        return tcs.getTask();
    }

    public Task<List<Province>> findProvincesAsync() {
        final TaskCompletionSource<List<Province>> tcs = new TaskCompletionSource<>();
        List<Province> provinces = new ArrayList<>();

        SQLiteDatabase db = DatabaseHelper.getInstance(HonarnamaBaseApp.getInstance()).getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_LOCATIONS_TYPE + " = " + Location.PROVINCE +
                " ORDER BY " + COL_LOCATIONS_ORDER + " ASC";
        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    net.honarnama.core.model.Province province = new net.honarnama.core.model.Province();
                    province.setId(cursor.getInt(cursor.getColumnIndex(COL_LOCATIONS_ID)));
                    province.setName(cursor.getString(cursor.getColumnIndex(COL_LOCATIONS_NAME)));
                    province.setOrder(cursor.getInt(cursor.getColumnIndex(COL_LOCATIONS_ORDER)));
                    provinces.add(province);
                } while (cursor.moveToNext());
            }
            tcs.trySetResult(provinces);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(DEBUG_TAG, "Error while trying to get province list.", e);
            } else {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String stackTrace = sw.toString();
                Crashlytics.log(Log.ERROR, DEBUG_TAG, "Error while trying to get province list. // Error: " + e + ". stackTrace: " + stackTrace);
            }
            tcs.trySetError(e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return tcs.getTask();
    }

    public static Province getProvinceById(int provinceId) {

        SQLiteDatabase db = DatabaseHelper.getInstance(HonarnamaBaseApp.getInstance()).getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_LOCATIONS_TYPE + " = " + Location.PROVINCE +
                " AND id = " + provinceId;
        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor != null) {
                cursor.moveToFirst();
                Province province = new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex(COL_LOCATIONS_ID)));
                province.setName(cursor.getString(cursor.getColumnIndex(COL_LOCATIONS_NAME)));
                province.setOrder(cursor.getInt(cursor.getColumnIndex(COL_LOCATIONS_ORDER)));
                return province;
            } else {
                if (BuildConfig.DEBUG) {
                    Log.d(DEBUG_TAG, "Error while trying to get province from database. province not found for province id " + provinceId);
                }
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(DEBUG_TAG, "Error while trying to get province from database.", e);
            } else {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String stackTrace = sw.toString();
                Crashlytics.log(Log.ERROR, DEBUG_TAG, "Error while trying to get province from database. // Error: " + e + ". stackTrace: " + stackTrace);
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return null;
    }

}
