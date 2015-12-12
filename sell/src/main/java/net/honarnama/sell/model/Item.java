package net.honarnama.sell.model;

import com.parse.ImageSelector;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.core.utils.ParseIO;
import net.honarnama.sell.BuildConfig;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

@ParseClassName("Item")
public class Item extends ParseObject {
    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/model.Item";
    public final static int NUMBER_OF_IMAGES = 4;

    public Item() {
        super();
    }

    public Item(ParseUser owner, String title, String description, String categoryId) {
        super();
        put("title", title);
        put("description", description);
        put("owner", owner);
        // TODO: categoryId
    }

    private void update(String title, String description, String categoryId) {
        put("title", title);
        put("description", description);
        // TODO: categoryId
    }

    public String getTitle() {
        return getString("title");
    }

    public String getDescription() {
        return getString("description");
    }

    public ParseFile[] getImages() {
        ParseFile[] res = new ParseFile[NUMBER_OF_IMAGES];
        for (int i = 0; i < NUMBER_OF_IMAGES; i++) {
            ParseFile imageFile = getParseFile("image_" + (i + 1));
            res[i] = imageFile;
        }
        return res;
    }

    public static Task<Item> saveWithImages(final Item originalItem, final String title, final String description, final String categoryId,final ImageSelector[] itemImages) throws IOException {
        final ArrayList<ParseFile> parseFileImages = new ArrayList<ParseFile>();
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
                }
            } else if (parseFile != null) {
                Log.d(DEBUG_TAG, "saveWithImages, existing file: " + parseFile);
                parseFileImages.add(parseFile);
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
            item.update(title, description, categoryId);
        } else {
            item = new Item(ParseUser.getCurrentUser(), title, description, categoryId);
        }

        Task<Void> t = Task.whenAll(tasks).continueWithTask(new Continuation<Void, Task<Void>>() {

            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                if (BuildConfig.DEBUG) {
                    Log.d(DEBUG_TAG, "saveWithImages, image.saveInBackground s are done. item= " + item);
                }
                if (!task.isCompleted()) {
                    return task;
                }
                int count = 0;
                for (ParseFile parseFile : parseFileImages) {
                    count++;
                    item.put("image_" + count, parseFile);
                }
                return item.saveInBackground();
            }
        });

        return t.continueWithTask(new Continuation<Void, Task<Item>>() {

            @Override
            public Task<Item> then(Task<Void> task) throws Exception {
                if (BuildConfig.DEBUG) {
                    Log.d(DEBUG_TAG, "saveWithImages, item.saveInBackground is done.");
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
}
