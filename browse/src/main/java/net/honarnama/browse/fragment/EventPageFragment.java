package net.honarnama.browse.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.parse.ImageSelector;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.honarnama.GRPCUtils;
import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.model.City;
import net.honarnama.base.model.Province;
import net.honarnama.base.utils.JalaliCalendar;
import net.honarnama.base.utils.TextUtil;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.base.utils.NetworkManager;
import net.honarnama.base.utils.ObservableScrollView;
import net.honarnama.browse.dialog.ContactDialog;
import net.honarnama.nano.BrowseEventReply;
import net.honarnama.nano.BrowseEventRequest;
import net.honarnama.nano.BrowseItemReply;
import net.honarnama.nano.BrowseItemRequest;
import net.honarnama.nano.BrowseServiceGrpc;
import net.honarnama.nano.Event;
import net.honarnama.nano.ReplyProperties;
import net.honarnama.nano.RequestProperties;
import net.honarnama.nano.Store;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Date;
import java.util.Locale;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

/**
 * Created by elnaz on 2/15/16.
 */
public class EventPageFragment extends HonarnamaBrowseFragment implements View.OnClickListener, AdapterView.OnItemClickListener, ObservableScrollView.OnScrollChangedListener {
    public static EventPageFragment mEventPageFragment;
    private Tracker mTracker;
    public ProgressBar mBannerProgressBar;

    public TextView mNameTextView;
    public TextView mDateTextView;
    public TextView mDescTextView;
    public TextView mAddreddTextView;
    public TextView mPlaceTextView;

    private ObservableScrollView mScrollView;
    private View mBannerFrameLayout;
    private ImageSelector mBannerImageView;
    private ProgressBar mEventInfoProgressBar;

    private RelativeLayout mShare;
    public Long mEventId;

    public RelativeLayout mShopContainer;
    public TextView mShopNameTextView;
    public ImageSelector mShopLogo;

    public RelativeLayout mOnErrorRetry;

    public RelativeLayout mDeletedEventMsg;
    public RelativeLayout mInfoContainer;

    public FloatingActionButton mFab;

    @Override
    public String getTitle(Context context) {
        return getString(R.string.art_event);
    }

    public synchronized static EventPageFragment getInstance(long eventId) {
        EventPageFragment eventPageFragment = new EventPageFragment();
        Bundle args = new Bundle();
        args.putLong("eventId", eventId);
        eventPageFragment.setArguments(args);
//        shopPageFragment.setOwner(owner);
        return eventPageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = HonarnamaBrowseApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("EventPageFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
    }


    @Override
    public void onSelectedTabClick() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_event_page, container, false);
        mEventId = getArguments().getLong("eventId");


        mScrollView = (ObservableScrollView) rootView.findViewById(R.id.fragment_scroll_view);
        mScrollView.setOnScrollChangedListener(this);
        mBannerFrameLayout = rootView.findViewById(R.id.event_banner_frame_layout);
        mBannerImageView = (ImageSelector) rootView.findViewById(R.id.event_banner_image_view);
        mBannerProgressBar = (ProgressBar) rootView.findViewById(R.id.banner_progress_bar);

        mEventInfoProgressBar = (ProgressBar) rootView.findViewById(R.id.event_info_progress_bar);

        mNameTextView = (TextView) rootView.findViewById(R.id.event_name_text_view);
        mDateTextView = (TextView) rootView.findViewById(R.id.event_date_text_view);
        mDescTextView = (TextView) rootView.findViewById(R.id.event_desc_text_view);
        mAddreddTextView = (TextView) rootView.findViewById(R.id.address);
        mPlaceTextView = (TextView) rootView.findViewById(R.id.event_place_text_view);
        mShare = (RelativeLayout) rootView.findViewById(R.id.event_share_container);
        mShare.setOnClickListener(this);

        mOnErrorRetry = (RelativeLayout) rootView.findViewById(R.id.on_error_retry_container);
        mOnErrorRetry.setOnClickListener(this);
        mDeletedEventMsg = (RelativeLayout) rootView.findViewById(R.id.deleted_event_msg);

        mFab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        mInfoContainer = (RelativeLayout) rootView.findViewById(R.id.event_info_container);

        setVisibilityInFragment(mEventInfoProgressBar, View.VISIBLE);
        setVisibilityInFragment(mOnErrorRetry, View.GONE);

        mShopContainer = (RelativeLayout) rootView.findViewById(R.id.event_shop_container);
        mShopNameTextView = (TextView) rootView.findViewById(R.id.shop_name_text_view);
        mShopLogo = (ImageSelector) rootView.findViewById(R.id.store_logo_image_view);

        setHasOptionsMenu(true);

        new getEventAsync().execute();
        return rootView;

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.event_share_container) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, mNameTextView.getText().toString());
            sendIntent.putExtra(Intent.EXTRA_TEXT, "سلام،" + "\n"
                    + "رویداد "
                    + mNameTextView.getText().toString() + "\n" +
                    "تو برنامه هنرنما ثبت شده. "
                    +
                    "جزئیاتشو اینجا میتونی ببینی:"
                    + "\n" + HonarnamaBaseApp.WEB_ADDRESS + "/event/" + mEventId);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }

        if (v.getId() == R.id.on_error_retry_container) {
            if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                return;
            }
            ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
            controlPanelActivity.refreshTopFragment();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        int scrollY = mScrollView.getScrollY();
        // Add parallax effect
        mBannerFrameLayout.setTranslationY(scrollY * 0.5f);
    }

    public class getEventAsync extends AsyncTask<Void, Void, BrowseEventReply> {
        BrowseEventRequest browseEventRequest;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setVisibilityInFragment(mOnErrorRetry, View.GONE);
            setVisibilityInFragment(mDeletedEventMsg, View.GONE);
        }

        @Override
        protected BrowseEventReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            browseEventRequest = new BrowseEventRequest();
            browseEventRequest.requestProperties = rp;

            browseEventRequest.id = mEventId;

            BrowseEventReply getEventReply;
            if (BuildConfig.DEBUG) {
                logD("Request for getting single event is: " + browseEventRequest);
            }
            try {
                BrowseServiceGrpc.BrowseServiceBlockingStub stub = GRPCUtils.getInstance().getBrowseServiceGrpc();
                getEventReply = stub.getEvent(browseEventRequest);
                return getEventReply;
            } catch (Exception e) {
                logE("Error running getEvent request. request: " + browseEventRequest + ". Error: " + e, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(BrowseEventReply browseEventReply) {
            super.onPostExecute(browseEventReply);

            setVisibilityInFragment(mEventInfoProgressBar, View.GONE);

            Activity activity = getActivity();

            if (browseEventReply != null) {
                switch (browseEventReply.replyProperties.statusCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:
                        if (activity != null) {
                            ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) activity);
                            controlPanelActivity.displayUpgradeRequiredDialog();
                        } else {
                            logE("Uncaught error code for getting event. browse request: " + browseEventRequest);
                        }
                        break;
                    case ReplyProperties.CLIENT_ERROR:
                        if (browseEventReply.errorCode == BrowseEventReply.EVENT_NOT_FOUND) {
                            displayLongToast(getStringInFragment(R.string.error_event_no_longer_exists));
                            setVisibilityInFragment(mDeletedEventMsg, View.VISIBLE);
                        } else {
                            setVisibilityInFragment(mOnErrorRetry, View.VISIBLE);
                            displayLongToast(getStringInFragment(R.string.error_displaying_event) + getStringInFragment(R.string.check_net_connection));
                        }
                        break;
                    case ReplyProperties.SERVER_ERROR:
                        setVisibilityInFragment(mOnErrorRetry, View.VISIBLE);
                        displayLongToast(getStringInFragment(R.string.server_error_try_again));
                        logE("Server error running getEvent request. request: " + browseEventRequest);
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        break;

                    case ReplyProperties.OK:
                        setVisibilityInFragment(mOnErrorRetry, View.GONE);
                        if (isAdded()) {
                            net.honarnama.nano.Event event = browseEventReply.event;
                            net.honarnama.nano.Store store = browseEventReply.store;
                            loadEventInfo(event, store);
                        }
                        break;
                }

            } else {
                setVisibilityInFragment(mOnErrorRetry, View.VISIBLE);
                displayLongToast(getStringInFragment(R.string.check_net_connection));
            }
        }

    }

    private void loadEventInfo(final Event event, final Store store) {

        setVisibilityInFragment(mFab, View.VISIBLE);
        setVisibilityInFragment(mInfoContainer, View.VISIBLE);
        setVisibilityInFragment(mOnErrorRetry, View.GONE);
        setVisibilityInFragment(mShare, View.VISIBLE);

        if (event.banner.trim().length() > 0) {
            setVisibilityInFragment(mBannerProgressBar, View.VISIBLE);
            mBannerImageView.setSource(event.banner, mBannerProgressBar);
        }

        setTextInFragment(mNameTextView, TextUtil.convertEnNumberToFa(event.name));
        setTextInFragment(mDescTextView, TextUtil.convertEnNumberToFa(event.description));

        Locale locale = new Locale("fa", "IR");

        Date startDate = new java.util.Date(event.startAt * 1000);
        String jalaliStartDate = JalaliCalendar.getJalaliDate(startDate);

        Date endDate = new java.util.Date(event.endAt * 1000);
        String jalaliEndDate = JalaliCalendar.getJalaliDate(endDate);

        setTextInFragment(mDateTextView, "تاریخ برگزاری رویداد از " +
                TextUtil.convertEnNumberToFa(jalaliStartDate) +
                " تا " +
                TextUtil.convertEnNumberToFa(jalaliEndDate) +
                " است. "
        );

        setTextInFragment(mAddreddTextView, "محل برگزاری:" + " " + event.address);

        String province = Province.getProvinceById(event.locationCriteria.provinceId).getName();
        String city = City.getCityById(event.locationCriteria.cityId).getName();

        setTextInFragment(mPlaceTextView, province + "، " + city);

        setTextInFragment(mShopNameTextView, "محصولی از " + store.name);
        mShopContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
                controlPanelActivity.displayShopPage(store.id, false);
            }
        });

        Picasso.with(mContext).load(R.drawable.default_logo_hand)
                .error(R.drawable.camera_insta)
                .into(mShopLogo, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                    }
                });

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContactDialog contactDialog = new ContactDialog();
                contactDialog.showDialog(getActivity(), event.phoneNumber, event.cellNumber,
                        getResources().getString(R.string.event_contact_dialog_warning_msg));

            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_search_fragment, menu);
        if (menu != null) {
            menu.findItem(R.id.action_search).setVisible(false);
        }
    }
}


