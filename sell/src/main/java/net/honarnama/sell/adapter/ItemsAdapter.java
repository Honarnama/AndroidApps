package net.honarnama.sell.adapter;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import net.honarnama.sell.R;

import android.content.Context;

/**
 * Created by reza on 11/5/15.
 */
public class ItemsAdapter extends ParseQueryAdapter<ParseObject> {

    public ItemsAdapter(Context context) {
        super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery<ParseObject> create() {
                ParseQuery query = new ParseQuery("item");
                query.whereEqualTo("owner", ParseUser.getCurrentUser());
                return query;
            }
        }, R.layout.item);

        setTextKey("title");
        setImageKey("image_1");
    }

}
