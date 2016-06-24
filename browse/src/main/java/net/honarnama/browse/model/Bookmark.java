package net.honarnama.browse.model;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.browse.helper.BrowseDatabaseHelper;
import net.honarnama.core.helper.DatabaseHelper;
import net.honarnama.core.model.HonarnamaBaseModel;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import static net.honarnama.browse.helper.BrowseDatabaseHelper.COL_BOOKMARK_CREATE_DATE;
import static net.honarnama.browse.helper.BrowseDatabaseHelper.COL_BOOKMARK_ITEM_ID;
import static net.honarnama.browse.helper.BrowseDatabaseHelper.TABLE_BOOKMARKS;

/**
 * Created by elnaz on 5/28/16.
 */
public class Bookmark extends HonarnamaBaseModel {
    String mId;
    String mItemId;
    String mDate;

    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/bookmarkModel";

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getItemId() {
        return mItemId;
    }

    public void setItemId(String itemId) {
        mItemId = itemId;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public Bookmark() {
        super();
    }

    public Bookmark(net.honarnama.core.model.Item item) {
        super();
    }

    public net.honarnama.core.model.Item getItem() {
        return null;
    }

    public static void bookmarkItem(net.honarnama.nano.Item item) throws SQLException {
        SQLiteDatabase db = BrowseDatabaseHelper.getInstance(HonarnamaBaseApp.getInstance()).getWritableDatabase();
        ContentValues values = new ContentValues();
        if (BuildConfig.DEBUG) {
            Log.d(DEBUG_TAG, "Bookmark Item: " + item);
        }
        values.put(COL_BOOKMARK_ITEM_ID, item.id);
        values.put(COL_BOOKMARK_CREATE_DATE, System.currentTimeMillis());
        db.insertOrThrow(TABLE_BOOKMARKS, null, values);
    }

    public static boolean removeBookmark(final String itemId) {
        if (BuildConfig.DEBUG) {
            Log.d(DEBUG_TAG, "Remove Bookmark with id: " + itemId);
        }
        SQLiteDatabase db = BrowseDatabaseHelper.getInstance(HonarnamaBaseApp.getInstance()).getReadableDatabase();
        return db.delete(TABLE_BOOKMARKS, COL_BOOKMARK_ITEM_ID + "=" + itemId, null) > 0;
    }

    public boolean isBookmarkedAlready(String itemId) {
        SQLiteDatabase db = DatabaseHelper.getInstance(HonarnamaBaseApp.getInstance()).getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_BOOKMARKS + " WHERE " + COL_BOOKMARK_ITEM_ID + " = " + itemId;
        Cursor cursor = db.rawQuery(query, null);
        try {
            if (cursor != null && cursor.getCount() > 0) {
                return true;
            }
        } catch (Exception e) {
            logE("Error while checking bookmark state of item with id: " + itemId, e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return false;
    }

}
