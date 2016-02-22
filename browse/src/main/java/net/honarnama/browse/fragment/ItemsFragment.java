package net.honarnama.browse.fragment;


import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.adapter.ItemsAdapter;
import net.honarnama.browse.model.Item;
import net.honarnama.core.utils.WindowUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by elnaz on 2/11/16.
 */
public class ItemsFragment extends HonarnamaBrowseFragment implements AdapterView.OnItemClickListener {
    public static ItemsFragment mItemsFragment;
    private ListView mListView;

    private ItemsAdapter mAdapter;


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
        mListView = (ListView) rootView.findViewById(R.id.shop_items_listView);
        final TextView emptyListTextVie = (TextView) rootView.findViewById(R.id.empty_items_list_view);
        mListView.setEmptyView(emptyListTextVie);

        final LinearLayout loadingCircle = (LinearLayout) rootView.findViewById(R.id.loading_circle_container);
        emptyListTextVie.setVisibility(View.GONE);
        loadingCircle.setVisibility(View.VISIBLE);

        Item.getRandomItems().continueWith(new Continuation<List<Item>, Object>() {
            @Override
            public Object then(Task<List<Item>> task) throws Exception {
                loadingCircle.setVisibility(View.GONE);
                emptyListTextVie.setVisibility(View.VISIBLE);
                emptyListTextVie.setText(HonarnamaBrowseApp.getInstance().getString(R.string.no_item_found));
                if (task.isFaulted()) {
                    logE("Getting random items failed. Error: " + task.getError(), "", task.getError());
                    if (isVisible()) {
                        Toast.makeText(getActivity(), HonarnamaBrowseApp.getInstance().getString(R.string.error_getting_items_list) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG);
                    }
                } else {
                    List<Item> items = task.getResult();
                    mAdapter.addAll(items);
                    mAdapter.notifyDataSetChanged();
                    WindowUtil.setListViewHeightBasedOnChildren(mListView);
                }
                return null;
            }
        });

        mAdapter = new ItemsAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        return rootView;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public String getTitle(Context context) {
        return null;
    }

    @Override
    public void onSelectedTabClick() {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}

