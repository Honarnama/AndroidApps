package net.honarnama.base.model;

import com.crashlytics.android.Crashlytics;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.helper.DatabaseHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import bolts.Task;
import bolts.TaskCompletionSource;

import static net.honarnama.base.helper.DatabaseHelper.COL_ART_CAT_ALL_SUBCAT_FILTER_TYPE;
import static net.honarnama.base.helper.DatabaseHelper.COL_ART_CAT_ID;
import static net.honarnama.base.helper.DatabaseHelper.COL_ART_CAT_NAME;
import static net.honarnama.base.helper.DatabaseHelper.COL_ART_CAT_ORDER;
import static net.honarnama.base.helper.DatabaseHelper.COL_ART_CAT_PARENT_ID;

public class ArtCategory {
    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/artCatModel";

    public static String TABLE_NAME = DatabaseHelper.TABLE_ART_CATEGORIES;

    public String mName;
    public int mOrder;
    public int mId;
    public int mParentId;
    public int mAllSubCatFilterType;

    public boolean isAllSubCatFilterType() {
        return (mAllSubCatFilterType == 1) ? true : false;
    }

    public void setAllSubCatFilterType(int allSubCatFilterType) {
        mAllSubCatFilterType = allSubCatFilterType;
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

    // Insert a post into the database
    public static Task<Void> resetArtCategories(net.honarnama.nano.ArtCategory[] artCategories) {

        final TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        // Create and/or open the database for writing
        SQLiteDatabase db = DatabaseHelper.getInstance(HonarnamaBaseApp.getInstance()).getWritableDatabase();
        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL(DatabaseHelper.CREATE_TABLE_ART_CATEGORIES);
            for (int i = 0; i < artCategories.length; i++) {
                ContentValues values = new ContentValues();
                net.honarnama.nano.ArtCategory artCategory = artCategories[i];
                if (BuildConfig.DEBUG) {
                    Log.d(DEBUG_TAG, "Reset Art Categories // artCategory: " + artCategory);
                }
                values.put(COL_ART_CAT_ID, artCategory.id);
                values.put(COL_ART_CAT_PARENT_ID, artCategory.parentId);
                values.put(COL_ART_CAT_NAME, artCategory.name);
                values.put(COL_ART_CAT_ORDER, artCategory.order);
                values.put(COL_ART_CAT_ALL_SUBCAT_FILTER_TYPE, (artCategory.allSubCatFilterType == true) ? 1 : 0);
                db.insertOrThrow(TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
            tcs.trySetResult(null);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(DEBUG_TAG, "Error while trying to reset artCategories data. ", e);
            } else {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String stackTrace = sw.toString();
                Crashlytics.log(Log.ERROR, DEBUG_TAG, "Error while trying to reset artCategories  data. // Error: " + e + ". stackTrace: " + stackTrace);
            }
            tcs.trySetError(e);
        } finally {
            db.endTransaction();
        }
        return tcs.getTask();
    }

    public static Task<String> getCategoryNameById(int categoryId) {
        final TaskCompletionSource<String> tcs = new TaskCompletionSource<>();

        SQLiteDatabase db = DatabaseHelper.getInstance(HonarnamaBaseApp.getInstance()).getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_ART_CAT_ID + " = " + categoryId;
        Cursor cursor = db.rawQuery(query, null);
        try {
            if (cursor != null) {
                cursor.moveToFirst();
                tcs.trySetResult(cursor.getString(cursor.getColumnIndex(COL_ART_CAT_NAME)));
            } else {
                tcs.trySetError(new Exception("ArtCatNotFound"));
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(DEBUG_TAG, "Error while trying to get art category name from database", e);
            } else {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String stackTrace = sw.toString();
                Crashlytics.log(Log.ERROR, DEBUG_TAG, "Error while trying to get art category name from database // Error: " + e + ". stackTrace: " + stackTrace);
            }
            tcs.trySetError(e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return tcs.getTask();
    }

    public static Task<ArtCategory> getCategoryById(int categoryId) {
        final TaskCompletionSource<ArtCategory> tcs = new TaskCompletionSource<>();

        SQLiteDatabase db = DatabaseHelper.getInstance(HonarnamaBaseApp.getInstance()).getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_ART_CAT_ID + " = " + categoryId;
        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor != null) {
                cursor.moveToFirst();
                ArtCategory artCategory = new ArtCategory();
                artCategory.setId(cursor.getInt(cursor.getColumnIndex(COL_ART_CAT_ID)));
                artCategory.setName(cursor.getString(cursor.getColumnIndex(COL_ART_CAT_NAME)));
                artCategory.setOrder(cursor.getInt(cursor.getColumnIndex(COL_ART_CAT_ORDER)));
                artCategory.setParentId(cursor.getInt(cursor.getColumnIndex(COL_ART_CAT_PARENT_ID)));
                artCategory.setAllSubCatFilterType(cursor.getInt(cursor.getColumnIndex(COL_ART_CAT_ALL_SUBCAT_FILTER_TYPE)));
                tcs.trySetResult(artCategory);
            } else {
                tcs.trySetError(new Exception("ArtCatNotFound"));
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(DEBUG_TAG, "Error while trying to get art category from database", e);
            } else {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String stackTrace = sw.toString();
                Crashlytics.log(Log.ERROR, DEBUG_TAG, "Error while trying to get art category from database // Error: " + e + ". stackTrace: " + stackTrace);
            }
            tcs.trySetError(e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return tcs.getTask();
    }

    public static List<ArtCategory> getAllArtCategoriesSorted(boolean includeAllSubCatFiltertypes) {

        List<ArtCategory> artCategories = new ArrayList<>();

        SQLiteDatabase db = DatabaseHelper.getInstance(HonarnamaBaseApp.getInstance()).getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        if (!includeAllSubCatFiltertypes) {
            query = query + " WHERE " + COL_ART_CAT_ALL_SUBCAT_FILTER_TYPE + " = 0 ";
        }
        query = query + " ORDER BY " + COL_ART_CAT_ORDER + " ASC";
        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    ArtCategory artCategory = new ArtCategory();
                    artCategory.setId(cursor.getInt(cursor.getColumnIndex(COL_ART_CAT_ID)));
                    artCategory.setParentId(cursor.getInt(cursor.getColumnIndex(COL_ART_CAT_PARENT_ID)));
                    artCategory.setAllSubCatFilterType(cursor.getInt(cursor.getColumnIndex(COL_ART_CAT_ALL_SUBCAT_FILTER_TYPE)));
                    artCategory.setName(cursor.getString(cursor.getColumnIndex(COL_ART_CAT_NAME)));
                    artCategory.setOrder(cursor.getInt(cursor.getColumnIndex(COL_ART_CAT_ORDER)));
                    artCategories.add(artCategory);
                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(DEBUG_TAG, "Error while trying to get art categories from database", e);
            } else {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String stackTrace = sw.toString();
                Crashlytics.log(Log.ERROR, DEBUG_TAG, "Error while trying to get art categories from database // Error: " + e + ". stackTrace: " + stackTrace);
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return artCategories;
    }


}
