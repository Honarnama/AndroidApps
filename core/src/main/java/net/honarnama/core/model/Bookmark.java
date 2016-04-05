package net.honarnama.core.model;

import net.honarnama.HonarnamaBaseApp;

import bolts.Task;

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
