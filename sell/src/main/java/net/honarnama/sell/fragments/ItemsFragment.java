package net.honarnama.sell.fragments;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import net.honarnama.sell.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


public class ItemsFragment extends Fragment {

    public static ItemsFragment newInstance() {
        return new ItemsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_items, container, false);

        ParseQueryAdapter<ParseObject> adapter =
                new ParseQueryAdapter<ParseObject>(this.getActivity(),
                        new ParseQueryAdapter.QueryFactory<ParseObject>() {
                            public ParseQuery<ParseObject> create() {
                                ParseQuery query = new ParseQuery("item");
                                query.whereEqualTo("owner", ParseUser.getCurrentUser());
                                return query;
                            }
                        });
        adapter.setTextKey("title");
        adapter.setImageKey("image_1");

        ListView listView = (ListView) rootView.findViewById(R.id.items_listview);
        listView.setAdapter(adapter);

        return rootView;
    }

}
