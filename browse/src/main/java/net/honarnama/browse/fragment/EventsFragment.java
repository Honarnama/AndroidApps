package net.honarnama.browse.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.mikepenz.iconics.view.IconicsImageView;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.adapter.EventsAdapter;
import net.honarnama.browse.dialog.EventFilterDialogActivity;
import net.honarnama.core.adapter.EventCategoriesAdapter;
import net.honarnama.core.model.Event;
import net.honarnama.core.model.EventCategory;
import net.honarnama.core.utils.NetworkManager;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bolts.Continuation;
import bolts.Task;


public class EventsFragment extends HonarnamaBrowseFragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    //    ShopsAdapter mAdapter;
    public static EventsFragment mEventsFragment;
    private Tracker mTracker;
    private FragmentActivity mFragmentActivity;
    EventsAdapter mEventsAdapter;
    public RelativeLayout mOnErrorRetry;
    public Button mCategoryFilterButton;
    public RelativeLayout mFilterContainer;
    List<EventCategory> mEventCategories = new ArrayList<>();
    public HashMap<Integer, String> mEventCategoriesHashMap = new HashMap<>();
    public int mSelectedCatId = -1;
    public String mSelectedCatName;

    private String mSelectedProvinceId;
    private String mSelectedProvinceName;
    private String mSelectedCityId;

    public RelativeLayout mEmptyListContainer;
    public LinearLayout mLoadingCircle;

    private ListView mListView;
    private boolean mFilterAllCategoryRowSelected = false;

    private TextView mFilterTextView;
    private IconicsImageView mFilterIcon;

    private boolean mIsAllIranChecked = true;
    private boolean mIsFilterApplied = false;

    @Override
    public String getTitle(Context context) {
        return getString(R.string.hornama);
    }

    public synchronized static EventsFragment getInstance() {
        if (mEventsFragment == null) {
            mEventsFragment = new EventsFragment();
        }
        return mEventsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTracker = HonarnamaBrowseApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("EventsFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_events, container, false);
        mListView = (ListView) rootView.findViewById(R.id.events_listView);


        View header = inflater.inflate(R.layout.item_list_header, null);
        mCategoryFilterButton = (Button) header.findViewById(R.id.category_filter_btn);
        if (!TextUtils.isEmpty(mSelectedCatName)) {
            mCategoryFilterButton.setText(mSelectedCatName);
        }
        mCategoryFilterButton.setOnClickListener(this);

        mListView.addHeaderView(header);
        mEmptyListContainer = (RelativeLayout) rootView.findViewById(R.id.no_events_warning_container);

        mOnErrorRetry = (RelativeLayout) rootView.findViewById(R.id.on_error_retry_container);
        mOnErrorRetry.setOnClickListener(this);

        mFilterContainer = (RelativeLayout) rootView.findViewById(R.id.filter_container);
        mFilterContainer.setOnClickListener(this);

        mFilterTextView = (TextView) rootView.findViewById(R.id.filter_text_view);
        mFilterIcon = (IconicsImageView) rootView.findViewById(R.id.filter_icon);

        mLoadingCircle = (LinearLayout) rootView.findViewById(R.id.loading_circle_container);

        mListView.setOnItemClickListener(this);

        final EventCategory eventCategory = new EventCategory();
        eventCategory.getAllEventCategoriesSorted().continueWith(new Continuation<List<EventCategory>, Object>() {
            @Override
            public Object then(Task<List<EventCategory>> task) throws Exception {
                if (task.isFaulted()) {
                    logE("Getting Event Task Failed. Msg: " + task.getError().getMessage() + " // Error: " + task.getError(), task.getError());
                    displayShortToast(getString(R.string.error_getting_event_cat_list) + getString(R.string.check_net_connection));
                } else {
                    mEventCategories = task.getResult();
                    if (mEventCategories != null) {
                        for (int i = 0; i < mEventCategories.size(); i++) {
                            mEventCategoriesHashMap.put(mEventCategories.get(i).getId(), mEventCategories.get(i).getName());
                        }
                    }
                }
                return null;
            }
        });
        listEvents();
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Event selectedEvent = mEventsAdapter.getItem(i - 1);
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        if (selectedEvent != null) {
            controlPanelActivity.displayEventPage(selectedEvent.getId(), false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        changeFilterTitle();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof FragmentActivity) {
            mFragmentActivity = (FragmentActivity) context;
        }
    }

    public void onSelectedTabClick() {
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.on_error_retry_container:
                if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                    return;
                }
                ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
                controlPanelActivity.refreshTopFragment();

                break;

            case R.id.category_filter_btn:
                displayChooseEventCategoryDialog();
                break;

            case R.id.filter_container:
                Intent intent = new Intent(getActivity(), EventFilterDialogActivity.class);
                intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_PROVINCE_ID, mSelectedProvinceId);
                intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_CITY_ID, mSelectedCityId);
                intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_ALL_IRAN, mIsAllIranChecked);
                getParentFragment().startActivityForResult(intent, HonarnamaBrowseApp.INTENT_FILTER_EVENT_CODE);
                break;

        }
    }


    private void displayChooseEventCategoryDialog() {

        ListView eventCatsListView;
        EventCategoriesAdapter eventCatsAdapter;

        final Dialog eventCatDialog = new Dialog(getActivity(), R.style.DialogStyle);
        eventCatDialog.setContentView(R.layout.choose_event_category);

        eventCatsListView = (ListView) eventCatDialog.findViewById(net.honarnama.base.R.id.event_category_list_view);
        eventCatsAdapter = new EventCategoriesAdapter(getActivity(), mEventCategories);
        eventCatsListView.setAdapter(eventCatsAdapter);
        eventCatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    mFilterAllCategoryRowSelected = true;
                } else {
                    mFilterAllCategoryRowSelected = false;
                }
                EventCategory eventCategory = mEventCategories.get(position);
                mSelectedCatId = eventCategory.getId();
                mSelectedCatName = eventCategory.getName();
                mCategoryFilterButton.setText(mSelectedCatName);
                eventCatDialog.dismiss();
                listEvents();
            }
        });
        eventCatDialog.setCancelable(true);
        eventCatDialog.setTitle(getString(R.string.select_event_cat));
        eventCatDialog.show();
    }


    public void listEvents() {

        EventCategory eventCategory = null;

        if (mSelectedCatId >= 0 && !mFilterAllCategoryRowSelected) {
            eventCategory = EventCategory.getCategoryById(mSelectedCatId);
        }

        mEventsAdapter = new EventsAdapter(getContext());
        mListView.setAdapter(mEventsAdapter);
    }


    class onQueryLoadListener{
//        //TODO remove this listener
//        @Override
//        public void onLoading() {
//            mEmptyListContainer.setVisibility(View.GONE);
//            mOnErrorRetry.setVisibility(View.GONE);
//            mLoadingCircle.setVisibility(View.VISIBLE);
//        }
//
//        @Override
//        public void onLoaded(List objects, Exception e) {
//
//            mLoadingCircle.setVisibility(View.GONE);
//            if (e == null) {
//                if ((objects != null) && objects.size() > 0) {
//                    mEmptyListContainer.setVisibility(View.GONE);
//                } else {
//                    mEmptyListContainer.setVisibility(View.VISIBLE);
//                }
//            } else {
//                mEmptyListContainer.setVisibility(View.VISIBLE);
//                if (((ParseException) e).getCode() == ParseException.OBJECT_NOT_FOUND) {
//                    if (isVisible()) {
//                        Toast.makeText(getActivity(), getActivity().getString(R.string.no_event_found), Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    if (isVisible()) {
//                        Toast.makeText(getActivity(), getString(R.string.error_getting_event_list) + getString(R.string.check_net_connection), Toast.LENGTH_SHORT).show();
//                    }
//                    mOnErrorRetry.setVisibility(View.VISIBLE);
//                }
//            }
//        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case HonarnamaBaseApp.INTENT_FILTER_EVENT_CODE:
                if (resultCode == getActivity().RESULT_OK) {
                    mSelectedProvinceId = data.getStringExtra(HonarnamaBaseApp.EXTRA_KEY_PROVINCE_ID);
                    mSelectedProvinceName = data.getStringExtra(HonarnamaBaseApp.EXTRA_KEY_PROVINCE_NAME);
                    mSelectedCityId = data.getStringExtra(HonarnamaBaseApp.EXTRA_KEY_CITY_ID);
                    mIsAllIranChecked = data.getBooleanExtra(HonarnamaBaseApp.EXTRA_KEY_ALL_IRAN, true);
                    mIsFilterApplied = data.getBooleanExtra(HonarnamaBaseApp.EXTRA_KEY_FILTER_APPLIED, false);

                    changeFilterTitle();

                    listEvents();
                }
                break;
        }
    }

    private void changeFilterTitle() {
        if (mIsFilterApplied) {
            mFilterTextView.setTextColor(getResources().getColor(R.color.dark_cyan));
            mFilterTextView.setText(R.string.change_filter);
            mFilterIcon.setColor(getResources().getColor(R.color.dark_cyan));
        } else {
            mFilterTextView.setTextColor(getResources().getColor(R.color.text_color));
            mFilterTextView.setText(getResources().getString(R.string.filter_geo));
            mFilterIcon.setColor(getResources().getColor(R.color.text_color));
        }
    }


}
