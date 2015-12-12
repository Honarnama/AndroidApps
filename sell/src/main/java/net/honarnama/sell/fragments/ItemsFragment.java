package net.honarnama.sell.fragments;

import com.parse.ParseObject;

import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.sell.R;
import net.honarnama.sell.activity.ControlPanelActivity;
import net.honarnama.sell.adapter.ItemsAdapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;


public class ItemsFragment extends HonarnamaBaseFragment implements AdapterView.OnItemClickListener {

    ItemsAdapter mAdapter;

    public static ItemsFragment mItemsFragment;

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.nav_title_items);
    }

    public synchronized static ItemsFragment getInstance() {
        if (mItemsFragment == null) {
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
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        controlPanelActivity.switchFragmentToEditItem(item.getObjectId());
    }
}
