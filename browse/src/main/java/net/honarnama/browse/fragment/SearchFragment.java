package net.honarnama.browse.fragment;


import com.mikepenz.iconics.view.IconicsImageView;
import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;

import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.adapter.ItemsAdapter;
import net.honarnama.browse.adapter.ItemsParseAdapter;
import net.honarnama.browse.adapter.ShopsAdapter;
import net.honarnama.browse.model.Item;
import net.honarnama.browse.model.Shop;
import net.honarnama.core.model.Store;
import net.honarnama.core.utils.WindowUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.List;

import bolts.Continuation;
import bolts.Task;


/**
 * Created by elnaz on 2/11/16.
 */
public class SearchFragment extends HonarnamaBrowseFragment implements View.OnClickListener {
    public static SearchFragment mSearchFragment;
    private ListView mListView;

    ItemsAdapter mItemsAdapter;
    ShopsAdapter mShopsAdapter;
    String msearchTerm;
    public EditText mSearchEditText;
    public View mSearchButton;


    private ToggleButton mItemsToggleButton;
    private ToggleButton mShopsToggleButton;
    private ToggleButton mEventsToggleButton;

    public synchronized static SearchFragment getInstance() {
        if (mSearchFragment == null) {
            mSearchFragment = new SearchFragment();
        }
        return mSearchFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        mSearchEditText = (EditText) rootView.findViewById(R.id.serach_term);
        mSearchButton = rootView.findViewById(R.id.search_btn);

        mItemsToggleButton = (ToggleButton) rootView.findViewById(R.id.items_tg_btn);
        mShopsToggleButton = (ToggleButton) rootView.findViewById(R.id.shops_tg_btn);
        mEventsToggleButton = (ToggleButton) rootView.findViewById(R.id.events_tg_btn);
        mItemsToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemsToggleButton.setChecked(true);
                mShopsToggleButton.setChecked(false);
                mEventsToggleButton.setChecked(false);
            }
        });
        mShopsToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShopsToggleButton.setChecked(true);
                mItemsToggleButton.setChecked(false);
                mEventsToggleButton.setChecked(false);
            }
        });

        mEventsToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventsToggleButton.setChecked(true);
                mItemsToggleButton.setChecked(false);
                mShopsToggleButton.setChecked(false);
            }
        });


        mListView = (ListView) rootView.findViewById(R.id.listView);
        mSearchButton.setOnClickListener(this);

        mItemsAdapter = new ItemsAdapter(HonarnamaBrowseApp.getInstance());
        mShopsAdapter = new ShopsAdapter(HonarnamaBrowseApp.getInstance());

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
        return getString(R.string.hornama);
    }

    @Override
    public void onSelectedTabClick() {

    }
//
//    @Override
//    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//        ParseObject selectedItem = (ParseObject) mItemsParseAdapter.getItem(position);
//        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
//        controlPanelActivity.displayItemPage(selectedItem.getObjectId(), false);
//    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.search_btn) {
            msearchTerm = mSearchEditText.getText().toString().trim();

            if (mItemsToggleButton.isChecked()) {
                mListView.setAdapter(mItemsAdapter);
                searchItems();
                return;
            }

            if (mShopsToggleButton.isChecked()) {
                mListView.setAdapter(mShopsAdapter);
                searchShops();
                return;
            }

            if (mEventsToggleButton.isChecked()) {
                mListView.setAdapter(mItemsAdapter);
                searchEvents();
                return;
            }
        }
    }

    public void searchItems() {
        Item.search(msearchTerm).continueWith(new Continuation<List<Item>, Object>() {
            @Override
            public Object then(Task<List<Item>> task) throws Exception {
                if (task.isFaulted()) {
                    logE("Searching items with search term" + msearchTerm + " failed. Error: " + task.getError(), "", task.getError());
                    if (isVisible()) {
                        Toast.makeText(getActivity(), HonarnamaBrowseApp.getInstance().getString(R.string.error_getting_items_list) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                    }
                } else {
                    List<Item> foundItems = task.getResult();
                    mItemsAdapter.setItems(foundItems);
                    mItemsAdapter.notifyDataSetChanged();
                }
                return null;
            }
        });
    }

    public void searchShops() {
        Shop.search(msearchTerm).continueWith(new Continuation<List<Store>, Object>() {
            @Override
            public Object then(Task<List<Store>> task) throws Exception {
                if (task.isFaulted()) {
                    logE("Searching shops with search term" + msearchTerm + " failed. Error: " + task.getError(), "", task.getError());
                    if (isVisible()) {
                        Toast.makeText(getActivity(), HonarnamaBrowseApp.getInstance().getString(R.string.error_getting_shop_lsit) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                    }
                } else {
                    List<Store> foundItems = task.getResult();
                    mShopsAdapter.setShops(foundItems);
                    mShopsAdapter.notifyDataSetChanged();
                }
                return null;
            }
        });
    }

    public void searchEvents() {
        Item.search(msearchTerm).continueWith(new Continuation<List<Item>, Object>() {
            @Override
            public Object then(Task<List<Item>> task) throws Exception {
                if (task.isFaulted()) {
                    logE("Searching items with search term" + msearchTerm + " failed. Error: " + task.getError(), "", task.getError());
                    if (isVisible()) {
                        Toast.makeText(getActivity(), HonarnamaBrowseApp.getInstance().getString(R.string.error_getting_items_list) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                    }
                } else {
                    List<Item> foundItems = task.getResult();
                    mItemsAdapter.setItems(foundItems);
                    mItemsAdapter.notifyDataSetChanged();
                }
                return null;
            }
        });
    }
}

