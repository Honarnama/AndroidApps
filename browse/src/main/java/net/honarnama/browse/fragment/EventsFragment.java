package net.honarnama.browse.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.mikepenz.iconics.view.IconicsImageView;

import net.honarnama.GRPCUtils;
import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.adapter.EventCategoriesAdapter;
import net.honarnama.base.model.City;
import net.honarnama.base.model.EventCategory;
import net.honarnama.base.model.Province;
import net.honarnama.base.utils.NetworkManager;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.adapter.EventsAdapter;
import net.honarnama.browse.dialog.LocationFilterDialogActivity;
import net.honarnama.nano.BrowseEventsReply;
import net.honarnama.nano.BrowseEventsRequest;
import net.honarnama.nano.BrowseServiceGrpc;
import net.honarnama.nano.LocationCriteria;
import net.honarnama.nano.ReplyProperties;
import net.honarnama.nano.RequestProperties;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import io.fabric.sdk.android.services.concurrency.AsyncTask;


public class EventsFragment extends HonarnamaBrowseFragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    //    ShopsAdapter mAdapter;
    public static EventsFragment mEventsFragment;
    private Tracker mTracker;
    EventsAdapter mEventsAdapter;
    public RelativeLayout mOnErrorRetry;
    public Button mCategoryFilterButton;
    List<EventCategory> mEventCategories = new ArrayList<>();
    public HashMap<Integer, String> mEventCategoriesHashMap = new HashMap<>();
    public int mSelectedCatId = -1;
    public String mSelectedCatName;

    public TextView mLocationCriteriaTextView;

    private boolean mIsAllIranChecked = true;
    private int mSelectedProvinceId = -1;
    private String mSelectedProvinceName;
    private int mSelectedCityId = -1;

    public RelativeLayout mEmptyListContainer;
    public LinearLayout mLoadingCircle;

    private ListView mListView;
    private boolean mFilterAllCategoryRowSelected = false;

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

        mLoadingCircle = (LinearLayout) rootView.findViewById(R.id.loading_circle_container);

        mListView.setOnItemClickListener(this);

        mLocationCriteriaTextView = (TextView) rootView.findViewById(R.id.location_criteria_text_view);

        rootView.findViewById(R.id.filter_location).setOnClickListener(this);

        final EventCategory eventCategory = new EventCategory();
        eventCategory.getAllEventCategoriesSorted().continueWith(new Continuation<List<EventCategory>, Object>() {
            @Override
            public Object then(Task<List<EventCategory>> task) throws Exception {
                if (task.isFaulted()) {
                    logE("Getting Event Task Failed. Msg: " + task.getError().getMessage() + " // Error: " + task.getError(), task.getError());
                    displayShortToast(getStringInFragment(R.string.error_getting_event_cat_list) + getStringInFragment(R.string.check_net_connection));
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
        net.honarnama.nano.Event selectedEvent = mEventsAdapter.getItem(i - 1);
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        if (selectedEvent != null) {
            controlPanelActivity.displayEventPage(selectedEvent.id, false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        changeLocationFilterTitle();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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

            case R.id.filter_location:
                Intent intent = new Intent(getActivity(), LocationFilterDialogActivity.class);
                intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_PROVINCE_ID, mSelectedProvinceId);
                intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_CITY_ID, mSelectedCityId);
                intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_ALL_IRAN, mIsAllIranChecked);
                getParentFragment().startActivityForResult(intent, HonarnamaBrowseApp.INTENT_FILTER_EVENTS_LOCATION);
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

//        EventCategory eventCategory = null;
//
//        if (mSelectedCatId >= 0 && !mFilterAllCategoryRowSelected) {
//            eventCategory = EventCategory.getCategoryById(mSelectedCatId);
//        }

        new getEventsAsync().execute();

        mEventsAdapter = new EventsAdapter(getContext());
        mListView.setAdapter(mEventsAdapter);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case HonarnamaBaseApp.INTENT_FILTER_EVENTS_LOCATION:
                if (resultCode == getActivity().RESULT_OK) {
                    mSelectedProvinceId = data.getIntExtra(HonarnamaBaseApp.EXTRA_KEY_PROVINCE_ID, Province.ALL_PROVINCE_ID);
                    mSelectedProvinceName = data.getStringExtra(HonarnamaBaseApp.EXTRA_KEY_PROVINCE_NAME);
                    mSelectedCityId = data.getIntExtra(HonarnamaBaseApp.EXTRA_KEY_CITY_ID, City.ALL_CITY_ID);
                    mIsAllIranChecked = data.getBooleanExtra(HonarnamaBaseApp.EXTRA_KEY_ALL_IRAN, true);
                    changeLocationFilterTitle();
                    listEvents();
                }
                break;
        }
    }

    public class getEventsAsync extends AsyncTask<Void, Void, BrowseEventsReply> {
        BrowseEventsRequest browseEventsRequest;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setVisibilityInFragment(mEmptyListContainer, View.GONE);
            setVisibilityInFragment(mOnErrorRetry, View.GONE);
            setVisibilityInFragment(mLoadingCircle, View.VISIBLE);
        }

        @Override
        protected BrowseEventsReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            browseEventsRequest = new BrowseEventsRequest();
            browseEventsRequest.requestProperties = rp;

            if (mSelectedCatId > 0) {
                browseEventsRequest.eventCategoryCriteria = mSelectedCatId;
            }

            LocationCriteria locationCriteria = new LocationCriteria();
            if (!mIsAllIranChecked) {
                if (mSelectedProvinceId > 0) {
                    locationCriteria.provinceId = mSelectedProvinceId;
                }

                if (mSelectedCityId > 0) {
                    locationCriteria.cityId = mSelectedCityId;
                }
            }
            browseEventsRequest.locationCriteria = locationCriteria;

            if (BuildConfig.DEBUG) {
                logD("Request for getting events is: " + browseEventsRequest);
            }
            try {
                BrowseServiceGrpc.BrowseServiceBlockingStub stub = GRPCUtils.getInstance().getBrowseServiceGrpc();
                return stub.getEvents(browseEventsRequest);
            } catch (Exception e) {
                logE("Error running getEvents request. request: " + browseEventsRequest + ". Error: " + e, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(BrowseEventsReply browseEventsReply) {
            super.onPostExecute(browseEventsReply);

            setVisibilityInFragment(mLoadingCircle, View.GONE);

            Activity activity = getActivity();

            if (browseEventsReply != null) {
                switch (browseEventsReply.replyProperties.statusCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:
                        if (activity != null) {
                            ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) activity);
                            controlPanelActivity.displayUpgradeRequiredDialog();
                        } else {
                            displayLongToast(getStringInFragment(R.string.upgrade_to_new_version));
                        }
                        break;
                    case ReplyProperties.CLIENT_ERROR:
                        // TODO
                        break;
                    case ReplyProperties.SERVER_ERROR:
                        mEventsAdapter.setEvents(null);
                        setVisibilityInFragment(mEmptyListContainer, View.VISIBLE);
                        mEventsAdapter.notifyDataSetChanged();
                        setVisibilityInFragment(mOnErrorRetry, View.VISIBLE);
                        displayLongToast(getStringInFragment(R.string.server_error_try_again));
                        logE("Server error running getEvents request. request: " + browseEventsRequest);
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        break;

                    case ReplyProperties.OK:
                        setVisibilityInFragment(mOnErrorRetry, View.GONE);
                        if (isAdded()) {
                            net.honarnama.nano.Event[] events = browseEventsReply.events;
                            ArrayList eventsList = new ArrayList();
                            for (net.honarnama.nano.Event event : events) {
                                eventsList.add(0, event);
                            }
                            if (eventsList.size() == 0) {
                                setVisibilityInFragment(mEmptyListContainer, View.VISIBLE);
                            }
                            mEventsAdapter.setEvents(eventsList);
                            mEventsAdapter.notifyDataSetChanged();
                        }
                        break;
                }

            } else {
                mEventsAdapter.setEvents(null);
                setVisibilityInFragment(mEmptyListContainer, View.VISIBLE);
                mEventsAdapter.notifyDataSetChanged();
                setVisibilityInFragment(mOnErrorRetry, View.VISIBLE);
                displayLongToast(getStringInFragment(R.string.check_net_connection));
            }
        }
    }

    private void changeLocationFilterTitle() {
        if (mIsAllIranChecked) {
            setTextInFragment(mLocationCriteriaTextView, getStringInFragment(R.string.all_over_iran));
        } else {
            if (mSelectedCityId > 0) {
                setTextInFragment(mLocationCriteriaTextView, getStringInFragment(R.string.city) + " " + City.getCityById(mSelectedCityId).getName());
            } else if (mSelectedProvinceId > 0) {
                setTextInFragment(mLocationCriteriaTextView, getStringInFragment(R.string.province) + " " + Province.getProvinceById(mSelectedProvinceId).getName());
            } else {
                setTextInFragment(mLocationCriteriaTextView, getStringInFragment(R.string.all_over_iran));
            }
        }
    }


}
