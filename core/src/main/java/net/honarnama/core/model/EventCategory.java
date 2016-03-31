package net.honarnama.core.model;

import com.crashlytics.android.Crashlytics;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.helper.DatabaseHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

import static net.honarnama.core.helper.DatabaseHelper.COL_EVENT_CAT_ID;
import static net.honarnama.core.helper.DatabaseHelper.COL_EVENT_CAT_NAME;
import static net.honarnama.core.helper.DatabaseHelper.COL_EVENT_CAT_ORDER;


public class EventCategory {
    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/eventCatModel";

    public static String TABLE_NAME = DatabaseHelper.TABLE_EVENT_CATEGORIES;
    public static int mOrder;
    public static String mName;
    public static int mId;


    public TreeMap<Number, HashMap<String, String>> mEventCatsTreeMap = new TreeMap<Number, HashMap<String, String>>();
    public TreeMap<Number, EventCategory> mEventCatObjectTreeMap = new TreeMap<Number, EventCategory>();

    public EventCategory() {
        super();
    }


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

    public void setId(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }

    // Insert a post into the database
    public static Task<Void> resetEventCategories(net.honarnama.nano.EventCategory[] eventCategories) {

        final TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        // Create and/or open the database for writing
        SQLiteDatabase db = DatabaseHelper.getInstance(HonarnamaBaseApp.getInstance()).getWritableDatabase();
        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL(DatabaseHelper.CREATE_TABLE_EVENT_CATEGORIES);
            for (int i = 0; i < eventCategories.length; i++) {
                ContentValues values = new ContentValues();
                net.honarnama.nano.EventCategory eventCategory = eventCategories[i];
                values.put(COL_EVENT_CAT_ID, eventCategory.id);
                values.put(COL_EVENT_CAT_NAME, eventCategory.name);
                db.insertOrThrow(TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
            tcs.trySetResult(null);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(DEBUG_TAG, "Error while trying to add eventCategories to database", e);
            } else {
                Crashlytics.log(Log.ERROR, DEBUG_TAG, "Error while trying to add eventCategories to database // Error: " + e);
            }
            tcs.trySetError(e);
        } finally {
            db.endTransaction();
        }
        return tcs.getTask();
    }

    public Task<TreeMap<Number, EventCategory>> getAllEventCategoriesSorted() {

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

        List<EventCategory> eventCategories = new ArrayList<>();

        SQLiteDatabase db = DatabaseHelper.getInstance(HonarnamaBaseApp.getInstance()).getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_EVENT_CAT_ORDER + " ASC";
        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    EventCategory eventCategory = new EventCategory();
                    eventCategory.setId(cursor.getInt(cursor.getColumnIndex(COL_EVENT_CAT_ID)));
                    eventCategory.setName(cursor.getString(cursor.getColumnIndex(COL_EVENT_CAT_NAME)));
                    eventCategory.setOrder(cursor.getInt(cursor.getColumnIndex(COL_EVENT_CAT_ORDER)));
                    eventCategories.add(eventCategory);
                } while (cursor.moveToNext());
            }
            tcs.trySetResult(eventCategories);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(DEBUG_TAG, "Error while trying to get event categories from database", e);
            } else {
                Crashlytics.log(Log.ERROR, DEBUG_TAG, "Error while trying to get event categories from database // Error: " + e);
            }
            tcs.trySetError(e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return tcs.getTask();
    }

    public static EventCategory getCategoryById(int categoryId) {
//        final TaskCompletionSource<EventCategory> tcs = new TaskCompletionSource<>();

        SQLiteDatabase db = DatabaseHelper.getInstance(HonarnamaBaseApp.getInstance()).getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_EVENT_CAT_ID + " = " + categoryId;
        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor != null) {
                cursor.moveToFirst();
                EventCategory eventCategory = new EventCategory();
                eventCategory.setId(cursor.getInt(cursor.getColumnIndex(COL_EVENT_CAT_ID)));
                eventCategory.setName(cursor.getString(cursor.getColumnIndex(COL_EVENT_CAT_NAME)));
                eventCategory.setOrder(cursor.getInt(cursor.getColumnIndex(COL_EVENT_CAT_ORDER)));
                return eventCategory;
            } else {
                if (BuildConfig.DEBUG) {
                    Log.d(DEBUG_TAG, "Error while trying to get event category from database. Event cat not found for catId " + categoryId);
                }
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(DEBUG_TAG, "Error while trying to get event category from database", e);
            } else {
                Crashlytics.log(Log.ERROR, DEBUG_TAG, "Error while trying to get event category from database // Error: " + e);
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return null;
    }
}
