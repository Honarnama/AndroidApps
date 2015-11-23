package net.honarnama.sell.adapter;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import net.honarnama.sell.R;
import net.honarnama.sell.model.Item;

import android.content.Context;

/**
 * Created by reza on 11/5/15.
 */
public class ItemsAdapter extends ParseQueryAdapter<Item> {

    public ItemsAdapter(Context context) {
        super(context, new ParseQueryAdapter.QueryFactory<Item>() {
            public ParseQuery<Item> create() {
                ParseQuery<Item> query = new ParseQuery<Item>(Item.class);
                query.whereEqualTo("owner", ParseUser.getCurrentUser());
                return query;
            }
        }, R.layout.item);

        setTextKey("title");
        setImageKey("image_1");
    }

}
