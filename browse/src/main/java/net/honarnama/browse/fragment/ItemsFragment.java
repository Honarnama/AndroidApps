package net.honarnama.browse.fragment;


import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.adapter.ItemsParseAdapter;
import net.honarnama.browse.model.Item;
import net.honarnama.core.activity.ChooseCategoryActivity;
import net.honarnama.core.model.Category;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by elnaz on 2/11/16.
 */
public class ItemsFragment extends HonarnamaBrowseFragment implements AdapterView.OnItemClickListener, View.OnClickListener {
    public static ItemsFragment mItemsFragment;
    private ListView mListView;
    public String mCategoryId;

    ItemsParseAdapter mItemsParseAdapter;
    public Button mCategoryFilterButton;

    public RelativeLayout mEmptyListContainer;

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
        mEmptyListContainer = (RelativeLayout) rootView.findViewById(R.id.no_items_warning_container);
//        mListView.setEmptyView(emptyListContainer);

        View header = inflater.inflate(R.layout.item_list_header, null);
        mCategoryFilterButton = (Button) header.findViewById(R.id.category_filter_btn);
        mCategoryFilterButton.setOnClickListener(this);

        mListView.addHeaderView(header);

        final LinearLayout loadingCircle = (LinearLayout) rootView.findViewById(R.id.loading_circle_container);

//        Item.getRandomItems().continueWith(new Continuation<List<Item>, Object>() {
//            @Override
//            public Object then(Task<List<Item>> task) throws Exception {
//                loadingCircle.setVisibility(View.GONE);
//                emptyListTextVie.setVisibility(View.VISIBLE);
//                emptyListTextVie.setText(HonarnamaBrowseApp.getInstance().getString(R.string.no_item_found));
//                if (task.isFaulted()) {
//                    logE("Getting random items failed. Error: " + task.getError(), "", task.getError());
//                    if (isVisible()) {
//                        Toast.makeText(getActivity(), HonarnamaBrowseApp.getInstance().getString(R.string.error_getting_items_list) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG);
//                    }
//                } else {
//                    List<Item> items = task.getResult();
//                    mAdapter.setImages(items);
//                    mAdapter.notifyDataSetChanged();
//                    WindowUtil.setListViewHeightBasedOnChildren(mListView);
//                }
//                return null;
//            }
//        });
//
//        mAdapter = new ItemsAdapter(getActivity());
//        mListView.setAdapter(mAdapter);
        listAllItems();
        mListView.setOnItemClickListener(this);

        return rootView;
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        ParseObject selectedItem = (ParseObject) mItemsParseAdapter.getItem(position);
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        controlPanelActivity.displayItemPage(selectedItem.getObjectId(), false);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.category_filter_btn) {
            Intent intent = new Intent(getActivity(), ChooseCategoryActivity.class);
            intent.putExtra(HonarnamaBaseApp.INTENT_ORIGIN, HonarnamaBaseApp.BROWSE_APP_KEY);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            startActivityForResult(intent, HonarnamaBrowseApp.INTENT_CHOOSE_CATEGORY_CODE);

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case HonarnamaBaseApp.INTENT_CHOOSE_CATEGORY_CODE:

                if (resultCode == getActivity().RESULT_OK) {

                    boolean isFilterSubCategoryRowSelected = data.getBooleanExtra("isFilterSubCategoryRowSelected", false);
                    ArrayList<String> subCatsList = data.getStringArrayListExtra("subCats");

                    mCategoryFilterButton.setText(data.getStringExtra("selectedCategoryName"));
                    mCategoryId = data.getStringExtra("selectedCategoryObjectId");
                    filterListByCategory(isFilterSubCategoryRowSelected, subCatsList);
                }
                break;
        }
    }

    public void filterListByCategory(boolean isFilterSubCategoryRowSelected, ArrayList<String> subCatsList) {
        if (isFilterSubCategoryRowSelected == true) {
            if (subCatsList == null) {
                listAllItems();
                return;
            }
        }

        ArrayList<String> querySubCatIds = new ArrayList<>();
        if (subCatsList == null) {
            querySubCatIds.add(mCategoryId);
        } else {
            querySubCatIds = subCatsList;
        }
        Category.getCategoriesById(querySubCatIds, HonarnamaBaseApp.BROWSE_APP_KEY).continueWith(new Continuation<List<Category>, Object>() {
            @Override
            public Object then(final Task<List<Category>> task) throws Exception {
                if (task.isFaulted()) {
                    return null;
                }
                ParseQueryAdapter.QueryFactory<ParseObject> filterFactory =
                        new ParseQueryAdapter.QueryFactory<ParseObject>() {
                            public ParseQuery create() {
                                ParseQuery<Item> parseQuery = new ParseQuery<Item>(Item.class);
                                parseQuery.whereEqualTo(Item.STATUS, Item.STATUS_CODE_VERIFIED);
                                parseQuery.whereEqualTo(Item.VALIDITY_CHECKED, true);
                                parseQuery.whereContainedIn(Item.CATEGORY, task.getResult());
                                parseQuery.include(Item.CATEGORY);
                                return parseQuery;
                            }
                        };

                mItemsParseAdapter = new ItemsParseAdapter(getContext(), filterFactory);
                mItemsParseAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener() {
                    @Override
                    public void onLoading() {
                    }

                    @Override
                    public void onLoaded(List objects, Exception e) {

                        if (objects.size() == 0) {
                            mEmptyListContainer.setVisibility(View.VISIBLE);
                        } else {
                            mEmptyListContainer.setVisibility(View.GONE);
                        }

                    }
                });
                mListView.setAdapter(mItemsParseAdapter);
                return null;
            }
        });

    }

    public void listAllItems() {
        ParseQueryAdapter.QueryFactory<ParseObject> filterFactory =
                new ParseQueryAdapter.QueryFactory<ParseObject>() {
                    public ParseQuery create() {
                        ParseQuery<Item> parseQuery = new ParseQuery<Item>(Item.class);
                        parseQuery.whereEqualTo(Item.STATUS, Item.STATUS_CODE_VERIFIED);
                        parseQuery.whereEqualTo(Item.VALIDITY_CHECKED, true);
                        parseQuery.include(Item.CATEGORY);
                        return parseQuery;
                    }
                };

        mItemsParseAdapter = new ItemsParseAdapter(getContext(), filterFactory);
        mItemsParseAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener() {
            @Override
            public void onLoading() {
            }

            @Override
            public void onLoaded(List objects, Exception e) {

                if (objects.size() == 0) {
                    mEmptyListContainer.setVisibility(View.VISIBLE);
                } else {
                    mEmptyListContainer.setVisibility(View.GONE);
                }

            }
        });
        mListView.setAdapter(mItemsParseAdapter);
    }
}

