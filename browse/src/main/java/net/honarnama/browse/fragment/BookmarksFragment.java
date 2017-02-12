package net.honarnama.browse.fragment;


import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.honarnama.base.BuildConfig;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.adapter.ItemsAdapter;
import net.honarnama.browse.dialog.ConfirmationDialog;
import net.honarnama.browse.model.Bookmark;

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
public class BookmarksFragment extends HonarnamaBrowseFragment implements AdapterView.OnItemClickListener, View.OnClickListener {
    public static BookmarksFragment mBookmarksFragment;
    private ListView mListView;

    ItemsAdapter mItemsAdapter;
    public LinearLayout mLoadingCircle;
    public RelativeLayout mEmptyListContainer;
    public net.honarnama.nano.Item mSelectedItem;

    public ConfirmationDialog mConfirmationDialog;

    Tracker mTracker;

    public synchronized static BookmarksFragment getInstance() {
//        if (mBookmarksFragment == null) {
        mBookmarksFragment = new BookmarksFragment();
//        }
        return mBookmarksFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = HonarnamaBrowseApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("BookmarksFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_bookmarks, container, false);

        mListView = (ListView) rootView.findViewById(R.id.items_listView);
        mEmptyListContainer = (RelativeLayout) rootView.findViewById(R.id.empty_list_container);
        mListView.setEmptyView(mEmptyListContainer);

        mLoadingCircle = (LinearLayout) rootView.findViewById(R.id.loading_circle_container);

        mItemsAdapter = new ItemsAdapter(HonarnamaBrowseApp.getInstance(), this);
        mItemsAdapter.setForBookmarks(true);
        mItemsAdapter.setOnDeleteBookmarkListener(this);
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
    public String getTitle() {
        return getStringInFragment(R.string.hornama);
    }

    @Override
    public void onSelectedTabClick() {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        mSelectedItem = mItemsAdapter.getItem(position);
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        if (mSelectedItem != null) {
            controlPanelActivity.displayItemPage(mSelectedItem.id, false);
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.delete_bookmark) {
            final int selectedPos = (int) v.getTag();
            final Long itemId = mItemsAdapter.getItemId(selectedPos);

            mConfirmationDialog = new ConfirmationDialog(getActivity(),
                    getStringInFragment(R.string.remove_bookmark_dialog_title),
                    getStringInFragment(R.string.remove_bookmark_dialog_msg)
            );
            mConfirmationDialog.showDialog(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Bookmark.removeBookmark(itemId);
                        mItemsAdapter.removeItem(selectedPos);
                        mItemsAdapter.notifyDataSetChanged();
                    } catch (Exception ex) {
                        logE("Error deleting bookmark. ex: ", ex);
                        displayShortToast("خطا در حذف محصول نشان شده.");
                    }
                    mConfirmationDialog.dismiss();
                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mConfirmationDialog != null && mConfirmationDialog.isShowing()) {
            try {
                mConfirmationDialog.dismiss();
            } catch (Exception ex) {

            }
        }
    }

}

