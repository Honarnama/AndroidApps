package net.honarnama.sell.model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by reza on 11/16/15.
 */
@ParseClassName("Item")
public class Item extends ParseObject {
    public final static int NUMBER_OF_IMAGES = 4;

    public Item() {
        super();
    }

    public Item(ParseUser owner, String title, String description) {
        super();
        put("title", title);
        put("description", description);
        put("owner", owner);
    }

    public ParseUser getOwner() {
        return getParseUser("owner");
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
}
