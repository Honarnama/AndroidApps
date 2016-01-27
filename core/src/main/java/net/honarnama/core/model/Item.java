package net.honarnama.core.model;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ImageSelector;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.utils.HonarnamaUser;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.core.utils.ParseIO;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

@ParseClassName("Item")
public class Item extends ParseObject {
    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/model.Item";
    public final static int NUMBER_OF_IMAGES = 4;

    public static String OBJECT_NAME = "Item";

    //Defining fields
    public static String TITLE = "title";
    public static String DESCRIPTION = "description";
    public static String CATEGORY_ID = "categoryId";
    public static String PRICE = "price";
    public static String IMAGE_1 = "image_1";
    public static String IMAGE_2 = "image_2";
    public static String IMAGE_3 = "image_3";
    public static String IMAGE_4 = "image_4";
    public static String STATUS = "status";
    public static String OWNER = "owner";

    public static Number STATUS_CODE_CONFIRMATION_WAITING = 0;
    public static Number STATUS_CODE_NOT_VERIFIED = -1;
    public static Number STATUS_CODE_VERIFIED = 1;


    public Item() {
        super();
    }

    public Item(ParseUser owner, String title, String description, String categoryId, Number price) {
        super();
        put("title", title);
        put("description", description);
        put("owner", owner);
        put("categoryId", categoryId);
        put("price", price);
    }

    private void update(String title, String description, String categoryId, Number price) {
        put("title", title);
        put("description", description);
        put("categoryId", categoryId);
        put("price", price);
    }

    public void setOwner(ParseUser parseUser) {
        put(OWNER, parseUser);
    }

    public String getTitle() {
        return getString("title");
    }

    public Number getPrice() {
        return getNumber("price");
    }

    public Number getStatus() {
        return getNumber(STATUS);
    }

    public String getDescription() {
        return getString("description");
    }

    public String getCategoryId() {
        return getString("categoryId");
    }

    public ParseFile[] getImages() {
        ParseFile[] res = new ParseFile[NUMBER_OF_IMAGES];
        for (int i = 0; i < NUMBER_OF_IMAGES; i++) {
            if (has("image_" + (i + 1))) {
                ParseFile imageFile = getParseFile("image_" + (i + 1));
                res[i] = imageFile;
            }
        }
        return res;
    }

    public static Task<Item> saveWithImages(final Item originalItem, final String title, final String description, final String categoryId, final Number price, final ImageSelector[] itemImages) throws IOException {
        final ArrayList<ParseFile> parseFileImages = new ArrayList<ParseFile>();
        final ArrayList<ParseFile> parseFileImagesToRemove = new ArrayList<ParseFile>();
        final ArrayList<Task<Void>> tasks = new ArrayList<Task<Void>>();

        for (ImageSelector imageSelector : itemImages) {
            ParseFile parseFile = imageSelector.getParseFile();
            if (imageSelector.isChanged()) {
                if (imageSelector.getFinalImageUri() != null) {
                    parseFile = ParseIO.getParseFileFromFile(
                            "image_" + imageSelector.getImageSelectorIndex() + ".jpeg",
                            new File(imageSelector.getFinalImageUri().getPath())
                    );
                    Log.d(DEBUG_TAG, "saveWithImages, new file: " + parseFile);
                    parseFileImages.add(parseFile);
                    tasks.add(parseFile.saveInBackground());
                } else {
                    parseFileImagesToRemove.add(parseFile);
                }
            } else if (parseFile != null) {
                Log.d(DEBUG_TAG, "saveWithImages, existing file: " + parseFile);
                if (!imageSelector.isDeleted()) {
                    parseFileImages.add(parseFile);
                }
            } else {
                Log.d(DEBUG_TAG, "saveWithImages, ignoring " + imageSelector);
            }
        }

        if (BuildConfig.DEBUG) {
            Log.d(DEBUG_TAG, "saveWithImages, will wait for image.saveInBackground s, tasks.size()= " + tasks.size());
        }

        final Item item;
        if (originalItem != null) {
            item = originalItem;
            item.update(title, description, categoryId, price);
        } else {
            item = new Item(ParseUser.getCurrentUser(), title, description, categoryId, price);
        }

        item.remove("image_1");
        item.remove("image_2");
        item.remove("image_3");
        item.remove("image_4");

        Task<Void> task = Task.whenAll(tasks).continueWithTask(new Continuation<Void, Task<Void>>() {

            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                if (BuildConfig.DEBUG) {
                    Log.d(DEBUG_TAG, "saveWithImages, image.saveInBackground s are done. item_row= " + item);
                }
                if (!task.isCompleted()) {
                    return task;
                }
                int count = 0;
                for (ParseFile parseFile : parseFileImages) {
                    count++;
                    item.put("image_" + count, parseFile);
                }
                item.pinInBackground();
                return item.saveInBackground();
            }
        });

        return task.continueWithTask(new Continuation<Void, Task<Item>>() {

            @Override
            public Task<Item> then(Task<Void> task) throws Exception {
                if (BuildConfig.DEBUG) {
                    Log.d(DEBUG_TAG, "saveWithImages, item_row.saveInBackground is done.");
                }
                TaskCompletionSource<Item> res = new TaskCompletionSource<Item>();
                if (task.isCompleted()) {
                    res.setResult(item);
                } else if (task.isFaulted()) {
                    res.setError(task.getError());
                } else {
                    res.setCancelled();
                }
                return res.getTask();
            }
        });
    }


    public static Task<List<Item>> getUserItems(Context context) {
        final TaskCompletionSource<List<Item>> tcs = new TaskCompletionSource<>();
        ParseQuery<Item> parseQuery = new ParseQuery<Item>(Item.class);
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_ITEM_SYNCED, false)) {
            if (BuildConfig.DEBUG) {
                Log.d(HonarnamaBaseApp.PRODUCTION_TAG + "/" + context.getClass().getName(), "getting items from Local data store");
            }
            parseQuery.fromLocalDatastore();
        } else {
            if (!NetworkManager.getInstance().isNetworkEnabled(context, true)) {
                tcs.setError(new NetworkErrorException("Network connection failed"));
                return tcs.getTask();
            }
        }
        parseQuery.whereEqualTo(Item.OWNER, HonarnamaUser.getCurrentUser());
        //TODO set limit for number of ads a user can have
        parseQuery.findInBackground(new FindCallback<Item>() {
            @Override
            public void done(final List<Item> items, ParseException e) {
                if (e == null) {
                    tcs.trySetResult(items);
                    if (!sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_ITEM_SYNCED, false)) {

                        ParseObject.unpinAllInBackground(Item.OBJECT_NAME, items, new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    ParseObject.pinAllInBackground(Item.OBJECT_NAME, items, new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        SharedPreferences.Editor editor = sharedPref.edit();
                                                        editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_ITEM_SYNCED, true);
                                                        editor.commit();
                                                    }
                                                }
                                            }
                                    );
                                }
                            }
                        });
                    }
                } else {
                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        if (BuildConfig.DEBUG) {
                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                    "User does not have any items yet.");
                        }
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_ITEM_SYNCED, true);
                        editor.commit();
                        tcs.trySetResult(null);
                    }
                    else {
                        tcs.trySetError(e);
                        if (BuildConfig.DEBUG) {
                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                    "Error Getting Items.  Error Code: " + e.getCode() +
                                            "//" + e.getMessage() + " // " + e, e);
                        } else {
                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Error Getting Items. "
                                    + e.getMessage());
                        }
                    }
                }
            }
        });
        return tcs.getTask();
    }


    public static Task<Void> deleteItem(Context context, String itemId) {
        final TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        if (!NetworkManager.getInstance().isNetworkEnabled(context, true)) {
            tcs.setError(new NetworkErrorException("Network connection failed"));
            return tcs.getTask();
        }

        ParseQuery<Item> parseQuery = new ParseQuery<Item>(Item.class);
        parseQuery.whereEqualTo(Item.OWNER, HonarnamaUser.getCurrentUser());
        parseQuery.getFirstInBackground(new GetCallback<Item>() {
            @Override
            public void done(final Item item, ParseException e) {
                if (e == null) {
                    item.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                tcs.setResult(null);
                            } else {
                                tcs.trySetError(e);
                            }
                        }
                    });
                } else {
                    tcs.trySetError(e);
                }
            }
        });

        return tcs.getTask();
    }
}
