package net.honarnama.browse.fragment;


import net.honarnama.base.BuildConfig;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.adapter.ItemsAdapter;
import net.honarnama.browse.model.Bookmark;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

/**
 * Created by elnaz on 2/11/16.
 */
public class BookmarksFragment extends HonarnamaBrowseFragment implements AdapterView.OnItemClickListener {
    public static BookmarksFragment mBookmarksFragment;
    private ListView mListView;

    ItemsAdapter mItemsAdapter;
    public LinearLayout mLoadingCircle;
    public RelativeLayout mEmptyListContainer;

    public synchronized static BookmarksFragment getInstance() {
//        if (mBookmarksFragment == null) {
        mBookmarksFragment = new BookmarksFragment();
//        }
        return mBookmarksFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_bookmarks, container, false);
        mListView = (ListView) rootView.findViewById(R.id.items_listView);
        mEmptyListContainer = (RelativeLayout) rootView.findViewById(R.id.empty_list_container);
        mListView.setEmptyView(mEmptyListContainer);

        mLoadingCircle = (LinearLayout) rootView.findViewById(R.id.loading_circle_container);

        mItemsAdapter = new ItemsAdapter(HonarnamaBrowseApp.getInstance());

        mListView.setAdapter(mItemsAdapter);
        mListView.setOnItemClickListener(this);
        new getBookmarks().execute();

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public String getTitle(Context context) {
        return getStringInFragment(R.string.bookmarks);
    }

    @Override
    public void onSelectedTabClick() {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        net.honarnama.nano.Item selectedItem = mItemsAdapter.getItem(position);
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        if (selectedItem != null) {
            controlPanelActivity.displayItemPage(selectedItem.id, false);
        }
    }

    public class getBookmarks extends AsyncTask<Void, Void, ArrayList<net.honarnama.nano.Item>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingCircle.setVisibility(View.VISIBLE);
            mEmptyListContainer.setVisibility(View.GONE);
        }

        @Override
        protected ArrayList<net.honarnama.nano.Item> doInBackground(Void... voids) {
            ArrayList<net.honarnama.nano.Item> items = new Bookmark().getAllBookmarks();
            if (BuildConfig.DEBUG) {
                logD("bookmarked items: " + items);
            }
            return items;
        }

        @Override
        protected void onPostExecute(ArrayList<net.honarnama.nano.Item> bookmarkedItems) {
            mLoadingCircle.setVisibility(View.GONE);

            if (bookmarkedItems.size() == 0) {
                mEmptyListContainer.setVisibility(View.VISIBLE);
            }

            mItemsAdapter.setItems(bookmarkedItems);
            mItemsAdapter.notifyDataSetChanged();
        }
    }

}

