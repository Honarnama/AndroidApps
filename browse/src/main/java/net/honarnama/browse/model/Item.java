package net.honarnama.browse.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import net.honarnama.core.model.ArtCategory;
import net.honarnama.core.model.Store;

import java.util.List;

import bolts.Task;

/**
 * Created by elnaz on 2/15/16.
 */
@ParseClassName("Item")
public class Item extends net.honarnama.core.model.Item {

    public Item(int ownerId, String name, String description, ArtCategory category, Number price, Store store) {
        super(ownerId, name, description, category, price, store);
    }

    public static Task<List<Item>> getItemsByOwner(final ParseUser owner) {
        return null;
        //TODO
    }

    public static Task<List<Item>> getSimilarItemsByCategory(final ArtCategory category, final String itemId) {
        //TODO
        return null;
    }

    public static Task<ParseObject> getItemById(final String itemId) {
        //TODO
        return null;
    }

    public static Task<List<Item>> search(final String searchTerm) {

        //TODO ask server for random results
        //TODO ask server to search for either persian and english texts

        return null;
    }

}
