package net.honarnama.base.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by elnaz on 3/29/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "honarnama";
    private static final int DATABASE_VERSION = 1;
    private static DatabaseHelper sInstance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static final String TABLE_ART_CATEGORIES = "art_categories";
    public static final String TABLE_EVENT_CATEGORIES = "event_categories";
    public static final String TABLE_LOCATIONS = "locations";


    // Art Categories Table Columns
    public static final String COL_ART_CAT_ID = "id";
    public static final String COL_ART_CAT_PARENT_ID = "parent_id";
    public static final String COL_ART_CAT_NAME = "name";
    public static final String COL_ART_CAT_ORDER = "art_cat_order";
    public static final String COL_ART_CAT_ALL_SUBCAT_FILTER_TYPE = "all_subcat_filter_type";

    // Event Categories Table Columns
    public static final String COL_EVENT_CAT_ID = "id";
    public static final String COL_EVENT_CAT_NAME = "name";
    public static final String COL_EVENT_CAT_ORDER = "event_cat_order";

    // Locations Table Columns
    public static final String COL_LOCATIONS_ID = "id";
    public static final String COL_LOCATIONS_PARENT_ID = "parent_id";
    public static final String COL_LOCATIONS_NAME = "name";
    public static final String COL_LOCATIONS_ORDER = "location_order";
    public static final String COL_LOCATIONS_TYPE = "type";


    public static final String CREATE_TABLE_ART_CATEGORIES = "CREATE TABLE " + TABLE_ART_CATEGORIES +
            "(" +
            COL_ART_CAT_ID + " INTEGER PRIMARY KEY," + // Define a primary key
            COL_ART_CAT_PARENT_ID + " INTEGER," +
            COL_ART_CAT_NAME + " TEXT," +
            COL_ART_CAT_ORDER + " INTEGER," +
            COL_ART_CAT_ALL_SUBCAT_FILTER_TYPE + " INTEGER" +
            ")";

    public static final String CREATE_TABLE_EVENT_CATEGORIES = "CREATE TABLE " + TABLE_EVENT_CATEGORIES +
            "(" +
            COL_EVENT_CAT_ID + " INTEGER PRIMARY KEY," + // Define a primary key
            COL_EVENT_CAT_NAME + " TEXT," +
            COL_EVENT_CAT_ORDER + " INTEGER" +
            ")";

    public static final String CREATE_TABLE_LOCATIONS = "CREATE TABLE " + TABLE_LOCATIONS +
            "(" +
            COL_LOCATIONS_ID + " INTEGER PRIMARY KEY," + // Define a primary key
            COL_LOCATIONS_PARENT_ID + " INTEGER," +
            COL_LOCATIONS_NAME + " TEXT," +
            COL_LOCATIONS_ORDER + " INTEGER," +
            COL_LOCATIONS_TYPE + " INTEGER" +
            ")";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ART_CATEGORIES);
        db.execSQL(CREATE_TABLE_EVENT_CATEGORIES);
        db.execSQL(CREATE_TABLE_LOCATIONS);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ART_CATEGORIES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENT_CATEGORIES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
            onCreate(db);
        }

    }


}
