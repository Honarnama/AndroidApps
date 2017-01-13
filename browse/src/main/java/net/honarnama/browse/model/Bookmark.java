package net.honarnama.browse.model;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.model.HonarnamaBaseModel;
import net.honarnama.browse.BuildConfig;
import net.honarnama.browse.helper.BrowseDatabaseHelper;
import net.honarnama.nano.ArtCategoryCriteria;
import net.honarnama.nano.Item;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

import static net.honarnama.browse.helper.BrowseDatabaseHelper.COL_BOOKMARK_CREATE_DATE;
import static net.honarnama.browse.helper.BrowseDatabaseHelper.COL_BOOKMARK_ITEM_CAT_L1;
import static net.honarnama.browse.helper.BrowseDatabaseHelper.COL_BOOKMARK_ITEM_CAT_L2;
import static net.honarnama.browse.helper.BrowseDatabaseHelper.COL_BOOKMARK_ITEM_DESC;
import static net.honarnama.browse.helper.BrowseDatabaseHelper.COL_BOOKMARK_ITEM_ID;
import static net.honarnama.browse.helper.BrowseDatabaseHelper.COL_BOOKMARK_ITEM_IMG;
import static net.honarnama.browse.helper.BrowseDatabaseHelper.COL_BOOKMARK_ITEM_NAME;
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

    public Bookmark(net.honarnama.base.model.Item item) {
        super();
    }

    public net.honarnama.base.model.Item getItem() {
        return null;
    }

    public static void bookmarkItem(net.honarnama.nano.Item item) throws SQLException {
        SQLiteDatabase db = BrowseDatabaseHelper.getInstance(HonarnamaBaseApp.getInstance()).getWritableDatabase();
        ContentValues values = new ContentValues();
        if (BuildConfig.DEBUG) {
            Log.d(DEBUG_TAG, "Bookmark Item: " + item);
        }
        values.put(COL_BOOKMARK_ITEM_ID, item.id);
        values.put(COL_BOOKMARK_ITEM_NAME, item.name);
        values.put(COL_BOOKMARK_ITEM_DESC, item.description);
        values.put(COL_BOOKMARK_ITEM_CAT_L1, item.artCategoryCriteria.level1Id);
        values.put(COL_BOOKMARK_ITEM_CAT_L2, item.artCategoryCriteria.level2Id);

        String itemImage = "";
        for (int i = 0; i < item.images.length; i++) {
            if (!TextUtils.isEmpty(item.images[i])) {
                itemImage = item.images[i];
                break;
            }
        }
        
        values.put(COL_BOOKMARK_ITEM_IMG, itemImage);
        values.put(COL_BOOKMARK_CREATE_DATE, System.currentTimeMillis());
        db.insertOrThrow(TABLE_BOOKMARKS, null, values);
    }

    public static boolean removeBookmark(final Long itemId) {
        if (BuildConfig.DEBUG) {
            Log.d(DEBUG_TAG, "Remove Bookmark with id: " + itemId);
        }
        SQLiteDatabase db = BrowseDatabaseHelper.getInstance(HonarnamaBaseApp.getInstance()).getReadableDatabase();
        return db.delete(TABLE_BOOKMARKS, COL_BOOKMARK_ITEM_ID + "=" + itemId, null) > 0;
    }

    public boolean isBookmarkedAlready(long itemId) {
        SQLiteDatabase db = BrowseDatabaseHelper.getInstance(HonarnamaBaseApp.getInstance()).getReadableDatabase();
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

    public ArrayList<Item> getAllBookmarks() {

        ArrayList<Item> bookmarkedItems = new ArrayList<>();
        SQLiteDatabase db = BrowseDatabaseHelper.getInstance(HonarnamaBaseApp.getInstance()).getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_BOOKMARKS;
        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor.moveToFirst()) {
                while (cursor.isAfterLast() == false) {
                    Item item = new Item();
                    item.id = cursor.getLong(cursor.getColumnIndex(COL_BOOKMARK_ITEM_ID));
                    item.name = cursor.getString(cursor.getColumnIndex(COL_BOOKMARK_ITEM_NAME));
                    item.description = cursor.getString(cursor.getColumnIndex(COL_BOOKMARK_ITEM_DESC));

                    item.artCategoryCriteria = new ArtCategoryCriteria();
                    item.artCategoryCriteria.level1Id = cursor.getInt(cursor.getColumnIndex(COL_BOOKMARK_ITEM_CAT_L1));
                    item.artCategoryCriteria.level2Id = cursor.getInt(cursor.getColumnIndex(COL_BOOKMARK_ITEM_CAT_L2));

                    item.images = new String[1];
                    item.images[0] = cursor.getString(cursor.getColumnIndex(COL_BOOKMARK_ITEM_IMG));

                    bookmarkedItems.add(item);
                    cursor.moveToNext();
                }
                if (net.honarnama.base.BuildConfig.DEBUG) {
                    logD("getting all bookmarked items result: " + bookmarkedItems);
                }
            }

        } catch (Exception e) {
            logE("Error while getting bookmarked items.", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return bookmarkedItems;
    }

}
