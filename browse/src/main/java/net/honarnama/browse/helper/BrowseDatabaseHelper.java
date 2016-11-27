package net.honarnama.browse.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by elnaz on 3/29/16.
 */
public class BrowseDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "honarnama_browse";
    private static final int DATABASE_VERSION = 1;
    private static BrowseDatabaseHelper sInstance;

    public static synchronized BrowseDatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new BrowseDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private BrowseDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static final String TABLE_BOOKMARKS = "bookmarks";
    public static final String COL_BOOKMARK_ID = "id";
    public static final String COL_BOOKMARK_ITEM_ID = "item_id";
    public static final String COL_BOOKMARK_ITEM_NAME = "item_name";
    public static final String COL_BOOKMARK_ITEM_DESC = "item_desc";
    public static final String COL_BOOKMARK_ITEM_CAT_L1 = "item_cat_l1";
    public static final String COL_BOOKMARK_ITEM_CAT_L2 = "item_cat_l2";
    public static final String COL_BOOKMARK_ITEM_IMG = "item_img";
    public static final String COL_BOOKMARK_CREATE_DATE = "bookmarked_create_date";


    public static final String CREATE_TABLE_BOOKMARKS = "CREATE TABLE " + TABLE_BOOKMARKS +
            "(" +
            COL_BOOKMARK_ID + " INTEGER PRIMARY KEY autoincrement," + // Define a primary key
            COL_BOOKMARK_ITEM_ID + " INTEGER," +
            COL_BOOKMARK_ITEM_NAME + " TEXT," +
            COL_BOOKMARK_ITEM_DESC + " TEXT," +
            COL_BOOKMARK_ITEM_CAT_L1 + " INTEGER," +
            COL_BOOKMARK_ITEM_CAT_L2 + " INTEGER," +
            COL_BOOKMARK_ITEM_IMG + " TEXT," +
            COL_BOOKMARK_CREATE_DATE + " int " +
            ")";


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_BOOKMARKS);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_BOOKMARKS);
            onCreate(db);
        }

    }


}
