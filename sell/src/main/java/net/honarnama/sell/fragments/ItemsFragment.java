package net.honarnama.sell.fragments;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import net.honarnama.sell.R;
import net.honarnama.sell.activity.ControlPanelActivity;
import net.honarnama.sell.adapter.ItemsAdapter;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;


public class ItemsFragment extends Fragment implements AdapterView.OnItemClickListener {

    ItemsAdapter mAdapter;

    public static ItemsFragment mItemsFragment;

    public synchronized static ItemsFragment getInstance()
    {
        if (mItemsFragment == null)
        {
            mItemsFragment = new ItemsFragment();
        }
        return mItemsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_items, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.items_listview);
        mAdapter = new ItemsAdapter(getActivity());
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ParseObject item = mAdapter.getItem(i);
//        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
//        controlPanelActivity.displayView(ControlPanelActivity.DRAWER_INDEX_ITEM_EDIT, item);
    }
}
