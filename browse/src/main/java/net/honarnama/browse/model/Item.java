package net.honarnama.browse.model;

import net.honarnama.base.model.ArtCategory;
import net.honarnama.base.model.Store;

import java.util.List;

import bolts.Task;

/**
 * Created by elnaz on 2/15/16.
 */
public class Item extends net.honarnama.base.model.Item {

    public Item(int ownerId, String name, String description, ArtCategory category, Number price, Store store) {
        super(ownerId, name, description, category, price, store);
    }

    public static Task<List<Item>> getItemsByOwner(final String ownerId) {
        return null;
        //TODO
    }

    public static Task<List<Item>> getSimilarItemsByCategory(final ArtCategory category, final String itemId) {
        //TODO
        return null;
    }

    public static net.honarnama.nano.Item getItemById(final String itemId) {
        //TODO
        return null;
    }

    public static Task<List<Item>> search(final String searchTerm) {

        //TODO ask server for random results
        //TODO ask server to search for either persian and english texts

        return null;
    }

}
