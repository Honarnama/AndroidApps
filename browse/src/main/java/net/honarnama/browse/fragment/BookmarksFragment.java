package net.honarnama.browse.fragment;


import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;

import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.adapter.BookmarksParseAdapter;
import net.honarnama.core.model.Bookmark;
import net.honarnama.core.utils.NetworkManager;

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

import java.util.List;

/**
 * Created by elnaz on 2/11/16.
 */
public class BookmarksFragment extends HonarnamaBrowseFragment implements AdapterView.OnItemClickListener {
    public static BookmarksFragment mBookmarksFragment;
    private ListView mListView;

    BookmarksParseAdapter mBookmarksParseAdapter;

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
        final RelativeLayout emptyListContainer = (RelativeLayout) rootView.findViewById(R.id.empty_list_container);
        mListView.setEmptyView(emptyListContainer);

        final LinearLayout loadingCircle = (LinearLayout) rootView.findViewById(R.id.loading_circle_container);

        mBookmarksParseAdapter = new BookmarksParseAdapter(HonarnamaBrowseApp.getInstance());
        mBookmarksParseAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener() {
            @Override
            public void onLoading() {
                loadingCircle.setVisibility(View.VISIBLE);
                emptyListContainer.setVisibility(View.GONE);
            }

            @Override
            public void onLoaded(List objects, Exception e) {
                loadingCircle.setVisibility(View.GONE);

                if (mBookmarksParseAdapter.isEmpty()) {
                    emptyListContainer.setVisibility(View.VISIBLE);
                }
            }
        });
        mListView.setAdapter(mBookmarksParseAdapter);
        mListView.setOnItemClickListener(this);
        return rootView;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.bookmarks);
    }

    @Override
    public void onSelectedTabClick() {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Bookmark selectedBookmark = (Bookmark) mBookmarksParseAdapter.getItem(position);
        ParseObject selectedItem = selectedBookmark.getItem();
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        controlPanelActivity.displayItemPage(selectedItem.getObjectId(), false);
    }

}

