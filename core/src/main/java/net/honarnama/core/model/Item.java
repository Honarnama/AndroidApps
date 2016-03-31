package net.honarnama.core.model;

import com.crashlytics.android.Crashlytics;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ImageSelector;
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

public class Item {
    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/itemModel";
    public final static int NUMBER_OF_IMAGES = 4;


    //Defining fields
    public String mName;
    public String mDescription;
    public ArtCategory mCategory;
    public Number mPrice;
    public File mImage1;
    public File mImage2;
    public File mImage3;
    public File mImage4;
    public boolean mValidityChecked;
    public int mStatus;
    public int mOwnerId;
    public Store mStore;
    public int mId;

    public static Number STATUS_CODE_CONFIRMATION_WAITING = 0;
    public static Number STATUS_CODE_NOT_VERIFIED = -1;
    public static Number STATUS_CODE_VERIFIED = 1;


    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public ArtCategory getCategory() {
        return mCategory;
    }

    public void setCategory(ArtCategory category) {
        mCategory = category;
    }

    public Number getPrice() {
        return mPrice;
    }

    public void setPrice(Number price) {
        mPrice = price;
    }

    public File getImage1() {
        return mImage1;
    }

    public void setImage1(File image1) {
        mImage1 = image1;
    }

    public File getImage2() {
        return mImage2;
    }

    public void setImage2(File image2) {
        mImage2 = image2;
    }

    public File getImage3() {
        return mImage3;
    }

    public void setImage3(File image3) {
        mImage3 = image3;
    }

    public File getImage4() {
        return mImage4;
    }

    public void setImage4(File image4) {
        mImage4 = image4;
    }

    public boolean isValidityChecked() {
        return mValidityChecked;
    }

    public void setValidityChecked(boolean validityChecked) {
        mValidityChecked = validityChecked;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public int getOwnerId() {
        return mOwnerId;
    }

    public void setOwnerId(int ownerId) {
        mOwnerId = ownerId;
    }

    public Store getStore() {
        return mStore;
    }

    public void setStore(Store store) {
        mStore = store;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public Item(int ownerId, String name, String description, ArtCategory category, Number price, Store store) {
        super();
        setName(name);
        setDescription(description);
        setOwnerId(ownerId);
        setCategory(category);
        setPrice(price);
        if (store != null) {
            setStore(store);
        }
    }

    private void update(String name, String description, ArtCategory category, Number price, Store store) {
        setName(name);
        setDescription(description);
        setCategory(category);
        setPrice(price);
        if (store != null) {
            setStore(store);
        }
    }

    public File[] getImages() {
        File[] res = new File[NUMBER_OF_IMAGES];
        File imageFile;

        int count = 0;
        if (getImage1() != null) {
            imageFile = getImage1();
            res[count] = imageFile;
            count++;
        }
        if (getImage2() != null) {
            imageFile = getImage2();
            res[count] = imageFile;
            count++;
        }

        if (getImage3() != null) {
            imageFile = getImage3();
            res[count] = imageFile;
            count++;
        }

        if (getImage4() != null) {
            imageFile = getImage4();
            res[count] = imageFile;
            count++;
        }

        return res;
    }

    public static Task<Item> saveWithImages(final Item originalItem, final String name, final String description,
                                            final ArtCategory category, final Number price, final ImageSelector[] itemImages, Store store) throws IOException {
        final ArrayList<File> parseFileImages = new ArrayList();
        final ArrayList<File> parseFileImagesToRemove = new ArrayList();
//        final ArrayList<ParseFile> parseFileThumbnails = new ArrayList<ParseFile>();
        final ArrayList<Task<Void>> tasks = new ArrayList<Task<Void>>();

        int counter = 0;
        for (ImageSelector imageSelector : itemImages) {

            File file = imageSelector.getFile();
            if (imageSelector.isChanged()) {
                if (imageSelector.getFinalImageUri() != null) {
                    counter++;
                    file = ParseIO.getParseFileFromFile(
                            "image_" + imageSelector.getImageSelectorIndex() + ".jpeg",
                            new File(imageSelector.getFinalImageUri().getPath())
                    );
                    if (BuildConfig.DEBUG) {
                        Log.d(DEBUG_TAG, "saveWithImages, new file: " + file);
                    }
                    parseFileImages.add(file);
                } else {
                    parseFileImagesToRemove.add(file);
                }
            } else if (file != null) {
                if (BuildConfig.DEBUG) {
                    Log.d(DEBUG_TAG, "saveWithImages, existing file: " + file);
                }
                if (!imageSelector.isDeleted()) {
                    parseFileImages.add(file);
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


    public static Task<Void> deleteItem(Context context, int itemId) {
        final TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        if (!NetworkManager.getInstance().isNetworkEnabled(false)) {
            tcs.setError(new NetworkErrorException("Network connection failed"));
            return tcs.getTask();
        }

        ParseQuery<Item> parseQuery = new ParseQuery<Item>(Item.class);
        parseQuery.whereEqualTo(Item.OWNER, HonarnamaUser.getCurrentUser());
        parseQuery.whereEqualTo(Item.OBJECT_ID, itemId);
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
