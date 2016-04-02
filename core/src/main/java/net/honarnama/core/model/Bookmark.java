package net.honarnama.core.model;

import com.crashlytics.android.Crashlytics;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.utils.HonarnamaUser;

import android.util.Log;
import android.widget.Toast;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * Created by elnaz on 3/2/16.
 */
public class Bookmark {

    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/bookmarkModel";
    public static String ITEM = "item";

    public Bookmark() {
        super();
    }

    public Bookmark(Item item) {
        super();
    }

    public Item getItem() {
        return null;
    }

    public static Task<Boolean> bookmarkItem(final Item item) {
        return null;
    }

    public static Task<Boolean> removeBookmark(final Item item) {
        return null;
    }

    public static Task<Boolean> isBookmarkedAlready(final Item item) {
        return null;
    }
}
