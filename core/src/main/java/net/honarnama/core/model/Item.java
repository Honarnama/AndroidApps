package net.honarnama.core.model;

import com.crashlytics.android.Crashlytics;
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
import net.honarnama.core.utils.FileUtil;
import net.honarnama.core.utils.HonarnamaUser;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.core.utils.ParseIO;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    public static String NAME = "name";
    public static String DESCRIPTION = "description";
    public static String CATEGORY = "category";
    public static String PRICE = "price";
    public static String IMAGE_1 = "image_1";
    public static String IMAGE_2 = "image_2";
    public static String IMAGE_3 = "image_3";
    public static String IMAGE_4 = "image_4";
    public static String THUMBNAIL = "thumbnail";
    public static String VALIDITY_CHECKED = "validity_checked";

    public static String STATUS = "status";
    public static String OWNER = "owner";
    public static String STORE = "store";
    public static String OBJECT_ID = "objectId";

    public static Number STATUS_CODE_CONFIRMATION_WAITING = 0;
    public static Number STATUS_CODE_NOT_VERIFIED = -1;
    public static Number STATUS_CODE_VERIFIED = 1;


    public Item() {
        super();
    }

    public Item(ParseUser owner, String name, String description, Category category, Number price, Store store) {
        super();
        put(NAME, name);
        put(DESCRIPTION, description);
        put(OWNER, owner);
        put(CATEGORY, category);
        put(PRICE, price);
        if (store != null) {
            put(STORE, store);
        }
    }

    private void update(String name, String description, Category category, Number price, Store store) {
        put(NAME, name);
        put(DESCRIPTION, description);
        put(CATEGORY, category);
        put(PRICE, price);
        if (store != null) {
            put(STORE, store);
        }
    }

    public void setOwner(ParseUser parseUser) {
        put(OWNER, parseUser);
    }

    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        put(NAME, name);
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

    public Category getCategory() {
        return (Category) getParseObject(CATEGORY);
    }

    public Store getStore() {
        return (Store) getParseObject(STORE);
    }

    public void setStore(Store store) {
        put(STORE, store);
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

    public static Task<Item> saveWithImages(final Item originalItem, final String name, final String description,
                                            final Category category, final Number price, final ImageSelector[] itemImages, Store store) throws IOException {
        final ArrayList<ParseFile> parseFileImages = new ArrayList<ParseFile>();
        final ArrayList<ParseFile> parseFileImagesToRemove = new ArrayList<ParseFile>();
//        final ArrayList<ParseFile> parseFileThumbnails = new ArrayList<ParseFile>();
        final ArrayList<Task<Void>> tasks = new ArrayList<Task<Void>>();

        int counter = 0;
        for (ImageSelector imageSelector : itemImages) {

            ParseFile parseFile = imageSelector.getParseFile();
            if (imageSelector.isChanged()) {
                if (imageSelector.getFinalImageUri() != null) {
                    counter++;
                    parseFile = ParseIO.getParseFileFromFile(
                            "image_" + imageSelector.getImageSelectorIndex() + ".jpeg",
                            new File(imageSelector.getFinalImageUri().getPath())
                    );
                    if (BuildConfig.DEBUG) {
                        Log.d(DEBUG_TAG, "saveWithImages, new file: " + parseFile);
                    }
                    parseFileImages.add(parseFile);
                    tasks.add(parseFile.saveInBackground());
//                    if (counter == 1) {
//                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                        Bitmap thumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imageSelector.getFinalImageUri().getPath()), 120, 120);
////                        thumbImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
////                        OutputStream outputStream = new FileOutputStream("thumbnail.jpeg");
////                        stream.writeTo(outputStream);
//                        ParseFile thumbParseFile = ParseIO.getParseFileFromFile("thumbnail.jpeg", FileUtil.convertBitmapToFile(thumbImage));
//                        parseFileThumbnails.add(thumbParseFile);
//                        tasks.add(thumbParseFile.saveInBackground());
//                    }
                } else {
                    parseFileImagesToRemove.add(parseFile);
                }
            } else if (parseFile != null) {
                if (BuildConfig.DEBUG) {
                    Log.d(DEBUG_TAG, "saveWithImages, existing file: " + parseFile);
                }
                if (!imageSelector.isDeleted()) {
                    parseFileImages.add(parseFile);
                }
            } else {
                if (BuildConfig.DEBUG) {
                    Log.d(DEBUG_TAG, "saveWithImages, ignoring " + imageSelector);
                }
            }
        }

        if (BuildConfig.DEBUG) {
            Log.d(DEBUG_TAG, "saveWithImages, will wait for image.saveInBackground s, tasks.size()= " + tasks.size());
        }

        final Item item;
        if (originalItem != null) {
            item = originalItem;
            item.update(name, description, category, price, store);
        } else {
            item = new Item(ParseUser.getCurrentUser(), name, description, category, price, store);
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
//                item.put(THUMBNAIL, parseFileThumbnails.get(0));
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
                    item.pinInBackground();
                    res.setResult(item);
                } else if (task.isFaulted()) {
                    if (BuildConfig.DEBUG) {
                        Log.e(DEBUG_TAG, "Saving Item failed. Item: " + item + " // Task faulted: " + task.getError());
                    } else {
                        Crashlytics.log(Log.ERROR, DEBUG_TAG, "Saving Item failed. Item: " + item + " // Task faulted with error: " + task.getError());
                    }
                    res.setError(task.getError());
                } else {
                    res.setCancelled();
                }
                return res.getTask();
            }
        });
    }


    public static void setUserItemsStore(final Store store) {
        ParseQuery<Item> query = ParseQuery.getQuery(Item.class);
        query.whereEqualTo(Store.OWNER, HonarnamaUser.getCurrentUser());
        query.findInBackground(new FindCallback<Item>() {
            @Override
            public void done(List<Item> objects, ParseException e) {
                for (Item item : objects) {
                    item.setStore(store);
                    item.pinInBackground();
                    item.saveEventually();
                }
            }
        });
    }

    public static Task<List<Item>> getUserItems(Context context) {
        final TaskCompletionSource<List<Item>> tcs = new TaskCompletionSource<>();
        ParseQuery<Item> parseQuery = new ParseQuery<Item>(Item.class);
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        if (!NetworkManager.getInstance().isNetworkEnabled(false)) {
            if (sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_ITEM_SYNCED, false)) {
                if (BuildConfig.DEBUG) {
                    Log.d(DEBUG_TAG, "Getting items from local data store");
                }
                parseQuery.fromLocalDatastore();
            } else {
                tcs.setError(new NetworkErrorException("No network connection + Offline ddata not available"));
                return tcs.getTask();
            }
        }


//        if (sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_ITEM_SYNCED, false)) {
//            if (BuildConfig.DEBUG) {
//                Log.d(HonarnamaBaseApp.PRODUCTION_TAG + "/" + context.getClass().getName(), "getting items from Local data store");
//            }
//            parseQuery.fromLocalDatastore();
//        } else {
//            if (!NetworkManager.getInstance().isNetworkEnabled(context, true)) {
//                tcs.setError(new NetworkErrorException("Network connection failed"));
//                return tcs.getTask();
//            }
//        }
        parseQuery.whereEqualTo(Item.OWNER, HonarnamaUser.getCurrentUser());
        //TODO set limit for number of ads a user can have
        parseQuery.findInBackground(new FindCallback<Item>() {
            @Override
            public void done(final List<Item> items, ParseException e) {
                if (e == null) {
                    tcs.trySetResult(items);
//                    if (!sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_ITEM_SYNCED, false)) {
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
//                    }
                } else {
                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        if (BuildConfig.DEBUG) {
                            Log.d(DEBUG_TAG, "User does not have any items yet.");
                        }
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_ITEM_SYNCED, true);
                        editor.commit();
                        tcs.trySetResult(null);
                    } else {
                        tcs.trySetError(e);
                        if (BuildConfig.DEBUG) {
                            Log.e(DEBUG_TAG,
                                    "Error getting user Items. Error Code: " + e.getCode() + " //  Error Msg: " + e.getMessage(), e);
                        } else {
                            Crashlytics.log(Log.ERROR, DEBUG_TAG, "Error getting user Items. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e);
                        }
                    }
                }
            }
        });
        return tcs.getTask();
    }


    public static Task<Void> deleteItem(Context context, String itemId) {
        final TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        if (!NetworkManager.getInstance().isNetworkEnabled(false)) {
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
