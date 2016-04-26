package net.honarnama.core.model;

import com.crashlytics.android.Crashlytics;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.helper.DatabaseHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

import bolts.Task;
import bolts.TaskCompletionSource;

import static net.honarnama.core.helper.DatabaseHelper.COL_LOCATIONS_ID;
import static net.honarnama.core.helper.DatabaseHelper.COL_LOCATIONS_NAME;
import static net.honarnama.core.helper.DatabaseHelper.COL_LOCATIONS_ORDER;
import static net.honarnama.core.helper.DatabaseHelper.COL_LOCATIONS_PARENT_ID;
import static net.honarnama.core.helper.DatabaseHelper.COL_LOCATIONS_TYPE;


/**
 * Created by elnaz on 3/29/16.
 */

public class Location {
    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/locationModel";

    public static String TABLE_NAME = DatabaseHelper.TABLE_LOCATIONS;
    public static String mName;
    public static int mOrder;
    public static int mId;
    public static int mParentId;
    public static int mType;

    public Location() {
        super();
    }


    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getParentId() {
        return mParentId;
    }

    public void setParentId(int parentId) {
        mParentId = parentId;
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


    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }


    // Insert a post into the database
    public static Task<Void> resetLocations(net.honarnama.nano.Location[] locations) {

        final TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        // Create and/or open the database for writing
        SQLiteDatabase db = DatabaseHelper.getInstance(HonarnamaBaseApp.getInstance()).getWritableDatabase();
        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL(DatabaseHelper.CREATE_TABLE_LOCATIONS);
            for (int i = 0; i < locations.length; i++) {
                ContentValues values = new ContentValues();
                net.honarnama.nano.Location location = locations[i];
                values.put(COL_LOCATIONS_ID, location.id);
                values.put(COL_LOCATIONS_PARENT_ID, location.parentId);
                values.put(COL_LOCATIONS_NAME, location.name);
                values.put(COL_LOCATIONS_ORDER, location.order);
                values.put(COL_LOCATIONS_TYPE, location.locType);
                db.insertOrThrow(TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
            tcs.trySetResult(null);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(DEBUG_TAG, "Error while trying to reset locations data. ", e);
            } else {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String stackTrace = sw.toString();
                Crashlytics.log(Log.ERROR, DEBUG_TAG, "Error while trying to reset locations data. // Error: " + e + ". stackTrace: " + stackTrace);
            }
            tcs.trySetError(e);
        } finally {
            db.endTransaction();
        }
        return tcs.getTask();
    }


}
