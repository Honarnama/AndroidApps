package net.honarnama.sell.fragments;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.parse.ImageSelector;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import net.honarnama.GRPCUtils;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.adapter.CityAdapter;
import net.honarnama.base.adapter.EventCategoriesAdapter;
import net.honarnama.base.adapter.ProvincesAdapter;
import net.honarnama.base.fragment.HonarnamaBaseFragment;
import net.honarnama.base.helper.MetaUpdater;
import net.honarnama.base.interfaces.MetaUpdateListener;
import net.honarnama.base.model.City;
import net.honarnama.base.model.EventCategory;
import net.honarnama.base.model.Province;
import net.honarnama.base.utils.GravityTextWatcher;
import net.honarnama.base.utils.JalaliCalendar;
import net.honarnama.base.utils.NetworkManager;
import net.honarnama.base.utils.ObservableScrollView;
import net.honarnama.base.utils.WindowUtil;
import net.honarnama.nano.CreateOrUpdateEventReply;
import net.honarnama.nano.CreateOrUpdateEventRequest;
import net.honarnama.nano.GetEventReply;
import net.honarnama.nano.HonarnamaProto;
import net.honarnama.nano.LocationCriteria;
import net.honarnama.nano.ReplyProperties;
import net.honarnama.nano.RequestProperties;
import net.honarnama.nano.SellServiceGrpc;
import net.honarnama.nano.SimpleRequest;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;
import net.honarnama.sell.activity.ControlPanelActivity;
import net.honarnama.sell.model.HonarnamaUser;
import net.honarnama.sell.utils.Uploader;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import bolts.Continuation;
import bolts.Task;
import io.fabric.sdk.android.services.concurrency.AsyncTask;

public class EventManagerFragment extends HonarnamaBaseFragment implements View.OnClickListener, ObservableScrollView.OnScrollChangedListener {

    private static final String SAVE_INSTANCE_STATE_KEY_EVENT_ID = "eventId";
    private static final String SAVE_INSTANCE_STATE_KEY_DIRTY = "dirty";
    private static final String SAVE_INSTANCE_STATE_KEY_NAME = "name";
    private static final String SAVE_INSTANCE_STATE_KEY_CATEGORY = "category";
    private static final String SAVE_INSTANCE_STATE_KEY_PROVINCE_ID = "province_id";
    private static final String SAVE_INSTANCE_STATE_KEY_PROVINCE_NAME = "province_name";
    private static final String SAVE_INSTANCE_STATE_KEY_CITY_ID = "city_id";
    private static final String SAVE_INSTANCE_STATE_KEY_CITY_NAME = "city_name";
    private static final String SAVE_INSTANCE_STATE_KEY_DESC = "desc";
    private static final String SAVE_INSTANCE_STATE_KEY_ADDR = "address";
    private static final String SAVE_INSTANCE_STATE_KEY_PHONE = "phone";
    private static final String SAVE_INSTANCE_STATE_KEY_CELL = "cell";
    private static final String SAVE_INSTANCE_STATE_KEY_ACTIVE = "active";
    private static final String SAVE_INSTANCE_STATE_KEY_CONTENT_IS_VISIBLE = "content_is_visible";
    private static final String SAVE_INSTANCE_STATE_KEY_REVIEW_STATUS = "review_status";

    private static final String SAVE_INSTANCE_STATE_KEY_START_DATE = "start_date";
    private static final String SAVE_INSTANCE_STATE_KEY_END_DATE = "end_date";

    private static final String SAVE_INSTANCE_STATE_KEY_START_YEAR = "start_year";
    private static final String SAVE_INSTANCE_STATE_KEY_START_MONTH = "start_month";
    private static final String SAVE_INSTANCE_STATE_KEY_START_DAY = "start_day";

    private static final String SAVE_INSTANCE_STATE_KEY_END_YEAR = "end_year";
    private static final String SAVE_INSTANCE_STATE_KEY_END_MONTH = "end_month";
    private static final String SAVE_INSTANCE_STATE_KEY_END_DAY = "end_day";

    private EditText mNameEditText;
    private EditText mAddressEditText;
    private EditText mDescriptionEditText;
    private EditText mPhoneNumberEditText;
    private EditText mCellNumberEditText;
    private Button mRegisterEventButton;
    private ImageSelector mBannerImageView;
    private RadioButton mActiveBtn;
    private RadioButton mPassiveBtn;
    private ObservableScrollView mScrollView;
    private View mBannerFrameLayout;
    private Button mEventCatBtn;
    private TextView mEventCatLabel;
    private EditText mProvinceEditText;
    private EditText mCityEditText;
    public TextView mStartLabelTextView;
    public TextView mEndLabelTextView;
    public ProgressBar mBannerProgressBar;
    public TextView mStatusBarTextView;
    public RelativeLayout mEventNotVerifiedNotif;
    private CoordinatorLayout mCoordinatorLayout;
    RelativeLayout mMainContent;
    TextView mEmptyView;
    private Spinner mStartDaySpinner, mStartMonthSpinner, mStartYearSpinner;
    private Spinner mEndDaySpinner, mEndMonthSpinner, mEndYearSpinner;
    Snackbar mSnackbar;

    List<EventCategory> mEventCategories = new ArrayList<>();
    public HashMap<Integer, String> mEventCategoriesHashMap = new HashMap<>();
    public TreeMap<Number, Province> mProvinceObjectsTreeMap = new TreeMap<>();
    public HashMap<Integer, String> mProvincesHashMap = new HashMap<>();
    public TreeMap<Number, HashMap<Integer, String>> mCityOrderedTreeMap = new TreeMap<>();
    public HashMap<Integer, String> mCityHashMap = new HashMap<>();

    public Date mStartDate;
    public Date mEndDate;
    public int mSelectedProvinceId = -1;
    public String mSelectedProvinceName;
    public int mSelectedCityId = -1;
    public String mSelectedCityName;
    public int mSelectedCatId = -1;
    public String mSelectedCatName;

    public static EventManagerFragment mEventManagerFragment;

    private Tracker mTracker;

    public boolean mIsNew = true;

    private long mEventId = -1;
    private int mReviewStatus = -1;

    public MetaUpdateListener mMetaUpdateListener;

    private boolean mDirty = false;
    TextWatcher mTextWatcherToMarkDirty;

    private boolean mActive = true;

    @Override
    public String getTitle() {
        return getStringInFragment(R.string.nav_title_event_manager);
    }

    private void setDirty(boolean dirty) {
        mDirty = dirty;
    }

    public boolean isDirty() {
        return mDirty;
    }

    public synchronized static EventManagerFragment getInstance() {
        if (mEventManagerFragment == null) {
            mEventManagerFragment = new EventManagerFragment();
        }
        return mEventManagerFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        mTracker = HonarnamaSellApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("EventManagerFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final Activity activity = getActivity();
        mTextWatcherToMarkDirty = new TextWatcher() {
            String mValue;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mValue = charSequence.toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!mValue.equals(editable + "")) {
                    mDirty = true;
                }
            }
        };

        ImageSelector.OnImageSelectedListener onImageSelectedListener =
                new ImageSelector.OnImageSelectedListener() {
                    @Override
                    public boolean onImageSelected(Uri selectedImage, boolean cropped) {
                        mDirty = true;
                        return true;
                    }

                    @Override
                    public void onImageRemoved() {
                        mDirty = true;
                    }

                    @Override
                    public void onImageSelectionFailed() {
                    }
                };

        View rootView = inflater.inflate(R.layout.fragment_event_manager, container, false);

        mBannerProgressBar = (ProgressBar) rootView.findViewById(R.id.banner_progress_bar);

        mNameEditText = (EditText) rootView.findViewById(R.id.event_name_edit_text);
        mAddressEditText = (EditText) rootView.findViewById(R.id.event_address_edit_text);
        mDescriptionEditText = (EditText) rootView.findViewById(R.id.event_description_edit_text);
        mPhoneNumberEditText = (EditText) rootView.findViewById(R.id.event_phone_number);
        mCellNumberEditText = (EditText) rootView.findViewById(R.id.event_cell_number);

        mStartDaySpinner = (Spinner) rootView.findViewById(R.id.start_day);
        mStartMonthSpinner = (Spinner) rootView.findViewById(R.id.start_month);
        mStartYearSpinner = (Spinner) rootView.findViewById(R.id.start_year);

        mEndDaySpinner = (Spinner) rootView.findViewById(R.id.end_day);
        mEndMonthSpinner = (Spinner) rootView.findViewById(R.id.end_month);
        mEndYearSpinner = (Spinner) rootView.findViewById(R.id.end_year);

        mStartLabelTextView = (TextView) rootView.findViewById(R.id.start_label_text_view);
        mEndLabelTextView = (TextView) rootView.findViewById(R.id.end_label_text_view);

        ArrayAdapter<String> daysAdapter = new ArrayAdapter(getFragmentContext(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.days));
        mStartDaySpinner.setAdapter(daysAdapter);
        mEndDaySpinner.setAdapter(daysAdapter);

        ArrayAdapter<String> monthsAdapter = new ArrayAdapter(getFragmentContext(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.months));
        mStartMonthSpinner.setAdapter(monthsAdapter);
        mEndMonthSpinner.setAdapter(monthsAdapter);

        ArrayAdapter<String> yearsAdapter = new ArrayAdapter(getFragmentContext(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.years));
        mStartYearSpinner.setAdapter(yearsAdapter);
        mEndYearSpinner.setAdapter(yearsAdapter);

        mActiveBtn = (RadioButton) rootView.findViewById(R.id.active_event);
        mPassiveBtn = (RadioButton) rootView.findViewById(R.id.passive_event);

        mActiveBtn.setOnClickListener(this);
        mPassiveBtn.setOnClickListener(this);

        mStatusBarTextView = (TextView) rootView.findViewById(R.id.event_status_bar_text_view);
        mEventNotVerifiedNotif = (RelativeLayout) rootView.findViewById(R.id.event_not_verified_notif_container);

        mBannerFrameLayout = rootView.findViewById(R.id.event_banner_frame_layout);
        mScrollView = (ObservableScrollView) rootView.findViewById(R.id.event_manager_scroll_view);
        mScrollView.setOnScrollChangedListener(this);

        mEventCatBtn = (Button) rootView.findViewById(R.id.event_cat_button);
        mEventCatLabel = (TextView) rootView.findViewById(R.id.event_cat_label);
        mEventCatBtn.setOnClickListener(this);

        mProvinceEditText = (EditText) rootView.findViewById(R.id.event_province_edit_text);
        mProvinceEditText.setOnClickListener(this);
        mProvinceEditText.setKeyListener(null);

        mCityEditText = (EditText) rootView.findViewById(R.id.event_city_edit_text);
        mCityEditText.setOnClickListener(this);
        mCityEditText.setKeyListener(null);

        mRegisterEventButton = (Button) rootView.findViewById(R.id.register_event_button);
        mRegisterEventButton.setOnClickListener(this);
        mBannerImageView = (ImageSelector) rootView.findViewById(R.id.event_banner_image_view);
        mMainContent = (RelativeLayout) rootView.findViewById(R.id.main_content);
        mEmptyView = (TextView) rootView.findViewById(R.id.empty_view);
        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinatorLayout);

        loadOfflineData();

        if (activity != null) {
            mBannerImageView.setActivity(activity);
            mBannerImageView.setOnImageSelectedListener(onImageSelectedListener);
        }

        reset();

        if (savedInstanceState != null) {
            mBannerImageView.restore(savedInstanceState);
            mEventId = savedInstanceState.getLong(SAVE_INSTANCE_STATE_KEY_EVENT_ID);
            setTextInFragment(mNameEditText, savedInstanceState.getString(SAVE_INSTANCE_STATE_KEY_NAME));
            setTextInFragment(mDescriptionEditText, savedInstanceState.getString(SAVE_INSTANCE_STATE_KEY_DESC));
            setTextInFragment(mAddressEditText, savedInstanceState.getString(SAVE_INSTANCE_STATE_KEY_ADDR));
            setTextInFragment(mPhoneNumberEditText, savedInstanceState.getString(SAVE_INSTANCE_STATE_KEY_PHONE));
            setTextInFragment(mCellNumberEditText, savedInstanceState.getString(SAVE_INSTANCE_STATE_KEY_CELL));
            setCheckedInFragment(mActiveBtn, savedInstanceState.getBoolean(SAVE_INSTANCE_STATE_KEY_ACTIVE));
            setCheckedInFragment(mPassiveBtn, !savedInstanceState.getBoolean(SAVE_INSTANCE_STATE_KEY_ACTIVE));

            mActive = mActiveBtn.isChecked() ? true : false;

            Long savedStartDate = savedInstanceState.getLong(SAVE_INSTANCE_STATE_KEY_START_DATE);
            Long savedEndDate = savedInstanceState.getLong(SAVE_INSTANCE_STATE_KEY_END_DATE);

            if (savedStartDate != null && savedStartDate > 0) {
                mStartDate = new Date(savedStartDate);
            }

            if (savedEndDate != null && savedEndDate > 0) {
                mEndDate = new Date(savedEndDate);
            }

            mStartYearSpinner.setSelection(savedInstanceState.getInt(SAVE_INSTANCE_STATE_KEY_START_YEAR));
            mStartMonthSpinner.setSelection(savedInstanceState.getInt(SAVE_INSTANCE_STATE_KEY_START_MONTH));
            mStartDaySpinner.setSelection(savedInstanceState.getInt(SAVE_INSTANCE_STATE_KEY_START_DAY));

            mEndYearSpinner.setSelection(savedInstanceState.getInt(SAVE_INSTANCE_STATE_KEY_END_YEAR));
            mEndMonthSpinner.setSelection(savedInstanceState.getInt(SAVE_INSTANCE_STATE_KEY_END_MONTH));
            mEndDaySpinner.setSelection(savedInstanceState.getInt(SAVE_INSTANCE_STATE_KEY_END_DAY));

            mSelectedCatId = savedInstanceState.getInt(SAVE_INSTANCE_STATE_KEY_CATEGORY);
            mSelectedProvinceId = savedInstanceState.getInt(SAVE_INSTANCE_STATE_KEY_PROVINCE_ID);
            mSelectedProvinceName = savedInstanceState.getString(SAVE_INSTANCE_STATE_KEY_PROVINCE_NAME);
            mSelectedCityId = savedInstanceState.getInt(SAVE_INSTANCE_STATE_KEY_CITY_ID);
            mSelectedCityName = savedInstanceState.getString(SAVE_INSTANCE_STATE_KEY_CITY_NAME);

            rePopulateCityList(false);

            mReviewStatus = savedInstanceState.getInt(SAVE_INSTANCE_STATE_KEY_REVIEW_STATUS);
            setReviewInfo(mReviewStatus);

            if (!savedInstanceState.getBoolean(SAVE_INSTANCE_STATE_KEY_CONTENT_IS_VISIBLE)) {
                new getEventAsync().execute();
            } else {
                if (BuildConfig.DEBUG) {
                    Log.d("STOPPED_ACTIVITY", "calling getEventAsync. mEventId: " + mEventId + ". SAVE_INSTANCE_STATE_KEY_CONTENT_IS_VISIBLE: " + savedInstanceState.getBoolean(SAVE_INSTANCE_STATE_KEY_CONTENT_IS_VISIBLE));
                }
                setVisibilityInFragment(mEmptyView, View.GONE);
                setVisibilityInFragment(mMainContent, View.VISIBLE);
            }

            mDirty = savedInstanceState.getBoolean(SAVE_INSTANCE_STATE_KEY_DIRTY);

            addListenersToMakeDirty();

        } else {
            new getEventAsync().execute();
        }

        mMetaUpdateListener = new MetaUpdateListener() {
            @Override
            public void onMetaUpdateDone(int replyCode) {
                if (BuildConfig.DEBUG) {
                    logD("Meta Update replyCode: " + replyCode);
                }
                dismissProgressDialog();
                switch (replyCode) {
                    case ReplyProperties.OK:
                        loadOfflineData();
                        break;

                    case ReplyProperties.UPGRADE_REQUIRED:
                        if (activity != null) {
                            ((ControlPanelActivity) activity).displayUpgradeRequiredDialog();
                        } else {
                            displayLongToast(getStringInFragment(R.string.upgrade_to_new_version));
                        }
                        break;
                    default:
                        displayLongToast(getStringInFragment(R.string.error_getting_info));
                        break;
                }
            }
        };

        if (activity != null) {
            ((ControlPanelActivity) activity).checkAndAskStoragePermission(activity);
        }

        //TODO test with savedInstancestate
        resetErrors();

        return rootView;
    }

    public void loadOfflineData() {
        new Province().getAllProvincesSorted().continueWith(new Continuation<TreeMap<Number, Province>, Object>() {
            @Override
            public Object then(Task<TreeMap<Number, Province>> task) throws Exception {
                if (task.isFaulted()) {
                    displayShortToast(getStringInFragment(R.string.error_getting_province_list));
                } else {
                    mProvinceObjectsTreeMap = task.getResult();
                    if (mProvinceObjectsTreeMap != null) {
                        for (Province province : mProvinceObjectsTreeMap.values()) {
                            if (mSelectedProvinceId < 0) {
                                mSelectedProvinceId = province.getId();
                                mSelectedProvinceName = province.getName();
                            }
                            mProvincesHashMap.put(province.getId(), province.getName());
                        }
                    }
                    if (mSelectedProvinceId > 0) {
                        setTextInFragment(mProvinceEditText, mProvincesHashMap.get(mSelectedProvinceId));
                    }
                }
                return null;
            }
        }).continueWithTask(new Continuation<Object, Task<TreeMap<Number, HashMap<Integer, String>>>>() {
            @Override
            public Task<TreeMap<Number, HashMap<Integer, String>>> then(Task<Object> task) throws Exception {
                return new City().getAllCitiesSorted(mSelectedProvinceId);
            }
        }).continueWith(new Continuation<TreeMap<Number, HashMap<Integer, String>>, Object>() {
            @Override
            public Object then(Task<TreeMap<Number, HashMap<Integer, String>>> task) throws Exception {
                if (task.isFaulted()) {
                    displayShortToast(getStringInFragment(R.string.error_getting_city_list));
                } else {
                    mCityOrderedTreeMap = task.getResult();
                    if (mCityOrderedTreeMap != null) {
                        for (HashMap<Integer, String> cityMap : mCityOrderedTreeMap.values()) {
                            for (Map.Entry<Integer, String> citySet : cityMap.entrySet()) {
                                if (mSelectedCityId < 0) {
                                    mSelectedCityId = citySet.getKey();
                                    mSelectedCityName = citySet.getValue();
                                }
                                mCityHashMap.put(citySet.getKey(), citySet.getValue());
                            }
                        }
                    }
                    if (mSelectedCityId > 0) {
                        setTextInFragment(mCityEditText, mCityHashMap.get(mSelectedCityId));
                    }
                }
                return null;
            }
        }).continueWithTask(new Continuation<Object, Task<List<EventCategory>>>() {
            @Override
            public Task<List<EventCategory>> then(Task<Object> task) throws Exception {
                return EventCategory.getAllEventCategoriesSorted();
            }
        }).continueWith(new Continuation<List<EventCategory>, Object>() {
            @Override
            public Object then(Task<List<EventCategory>> task) throws Exception {
                if (task.isFaulted()) {
                    displayShortToast(getStringInFragment(R.string.error_getting_event_cat_list));
                } else {
                    mEventCategories = task.getResult();
                    if (mEventCategories != null) {
                        for (int i = 0; i < mEventCategories.size(); i++) {
                            mEventCategoriesHashMap.put(mEventCategories.get(i).getId(), mEventCategories.get(i).getName());
                        }
                    }
                    if (mSelectedCatId > 0) {
                        setTextInFragment(mEventCatBtn, mEventCategoriesHashMap.get(mSelectedCatId));
                    }
                }
                return null;
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()) {
            mPhoneNumberEditText.addTextChangedListener(new GravityTextWatcher(mPhoneNumberEditText));
            mCellNumberEditText.addTextChangedListener(new GravityTextWatcher(mCellNumberEditText));
            ControlPanelActivity activity = (ControlPanelActivity) getActivity();
            if (activity != null) {
                activity.setTitle(getTitle());
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        Activity activity = getActivity();
        switch (view.getId()) {
            case R.id.register_event_button:
                WindowUtil.hideKeyboard(activity);
                if (formInputsAreValid()) {
                    new CreateOrUpdateEventAsync().execute();
                }
                break;

            case R.id.event_cat_button:
                displayEventCategoryDialog();
                break;

            case R.id.event_province_edit_text:
                displayProvinceDialog();
                break;

            case R.id.event_city_edit_text:
                displayCityDialog();
                break;

            case R.id.active_event:
                if (mActive != true) {
                    setDirty(true);
                    mActive = true;
                }
                break;

            case R.id.passive_event:
                if (mActive != false) {
                    setDirty(true);
                    mActive = false;
                }
                break;
        }
    }

    private void displayProvinceDialog() {
        Activity activity = getActivity();
        ListView provincesListView;
        ProvincesAdapter provincesAdapter;

        if (!(activity != null && isAdded())) {
            return;
        }

        final Dialog provinceDialog = new Dialog(activity, R.style.DialogStyle);
        if (mProvinceObjectsTreeMap.isEmpty()) {
            provinceDialog.setContentView(R.layout.dialog_no_data_found);
            provinceDialog.findViewById(R.id.no_data_retry_icon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                        return;
                    }

                    provinceDialog.dismiss();
                    displayProgressDialog(null);
                    new MetaUpdater(mMetaUpdateListener, 0).execute();

                }
            });

        } else {
            provinceDialog.setContentView(R.layout.choose_province);
            provincesListView = (ListView) provinceDialog.findViewById(net.honarnama.base.R.id.provinces_list_view);
            provincesAdapter = new ProvincesAdapter(activity, mProvinceObjectsTreeMap);
            provincesListView.setAdapter(provincesAdapter);
            provincesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Province selectedProvince = mProvinceObjectsTreeMap.get(position + 1);
                    if (selectedProvince != null) {
                        setErrorInFragment(mProvinceEditText, "");
                        if (mSelectedProvinceId != selectedProvince.getId()) {
                            setDirty(true);
                        }
                        mSelectedProvinceId = selectedProvince.getId();
                        mSelectedProvinceName = selectedProvince.getName();
                        setTextInFragment(mProvinceEditText, mSelectedProvinceName);
                        rePopulateCityList(true);
                    }
                    provinceDialog.dismiss();
                }
            });
        }
        provinceDialog.setCancelable(true);
        provinceDialog.setTitle(getStringInFragment(R.string.select_province));
        provinceDialog.show();
    }

    private void rePopulateCityList(final boolean setSelectedCityt) {
        City city = new City();
        city.getAllCitiesSorted(mSelectedProvinceId).continueWith(new Continuation<TreeMap<Number, HashMap<Integer, String>>, Object>() {
            @Override
            public Object then(Task<TreeMap<Number, HashMap<Integer, String>>> task) throws Exception {
                if (task.isFaulted()) {
                    displayLongToast(getStringInFragment(R.string.error_getting_city_list) + getStringInFragment(R.string.check_net_connection));
                } else {
                    mCityOrderedTreeMap = task.getResult();
                    if (mCityOrderedTreeMap != null && !mCityOrderedTreeMap.isEmpty()) {
                        for (HashMap<Integer, String> cityMap : mCityOrderedTreeMap.values()) {
                            for (Map.Entry<Integer, String> citySet : cityMap.entrySet()) {
                                mCityHashMap.put(citySet.getKey(), citySet.getValue());
                            }
                        }

                        if (setSelectedCityt) {
                            Set<Integer> tempSet = mCityOrderedTreeMap.get(1).keySet();
                            for (Integer key : tempSet) {
                                mSelectedCityId = key;
                                mSelectedCityName = mCityHashMap.get(key);
                                setTextInFragment(mCityEditText, mSelectedCityName);
                            }
                        }
                    }

                }
                return null;
            }
        });
    }

    private void displayCityDialog() {
        ListView cityListView;
        CityAdapter cityAdapter;
        Activity activity = getActivity();

        if (!(activity != null && isAdded())) {
            return;
        }

        final Dialog cityDialog = new Dialog(activity, R.style.DialogStyle);

        if (mCityOrderedTreeMap.isEmpty()) {
            cityDialog.setContentView(R.layout.dialog_no_data_found);
            cityDialog.findViewById(R.id.no_data_retry_icon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                        return;
                    }
                    cityDialog.dismiss();
                    displayProgressDialog(null);
                    new MetaUpdater(mMetaUpdateListener, 0).execute();
                }
            });

        } else {
            cityDialog.setContentView(R.layout.choose_city);
            cityListView = (ListView) cityDialog.findViewById(net.honarnama.base.R.id.city_list_view);

            cityAdapter = new CityAdapter(activity, mCityOrderedTreeMap);
            cityListView.setAdapter(cityAdapter);
            cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    HashMap<Integer, String> selectedCity = mCityOrderedTreeMap.get(position + 1);
                    if (selectedCity != null) {
                        setErrorInFragment(mCityEditText, "");
                        for (int key : selectedCity.keySet()) {
                            if (mSelectedCityId != key) {
                                setDirty(true);
                            }
                            mSelectedCityId = key;
                        }
                        for (String value : selectedCity.values()) {
                            mSelectedCityName = value;
                            setTextInFragment(mCityEditText, mSelectedCityName);
                        }
                    }
                    cityDialog.dismiss();
                }
            });
        }
        cityDialog.setCancelable(true);
        cityDialog.setTitle(getStringInFragment(R.string.select_city));
        cityDialog.show();
    }


    private void displayEventCategoryDialog() {

        ListView eventCatsListView;
        EventCategoriesAdapter eventCatsAdapter;

        Activity activity = getActivity();
        if (!(activity != null && isAdded())) {
            return;
        }

        final Dialog eventCatDialog = new Dialog(activity, R.style.DialogStyle);

        if (mEventCategories.isEmpty()) {
            eventCatDialog.setContentView(R.layout.dialog_no_data_found);
            eventCatDialog.findViewById(R.id.no_data_retry_icon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                        return;
                    }
                    eventCatDialog.dismiss();
                    displayProgressDialog(null);
                    new MetaUpdater(mMetaUpdateListener, 0).execute();
                }
            });

        } else {
            eventCatDialog.setContentView(R.layout.choose_event_category);
            eventCatsListView = (ListView) eventCatDialog.findViewById(net.honarnama.base.R.id.event_category_list_view);
            eventCatsAdapter = new EventCategoriesAdapter(activity, mEventCategories);
            eventCatsListView.setAdapter(eventCatsAdapter);
            eventCatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    EventCategory eventCategory = mEventCategories.get(position);
                    if (mSelectedCatId != eventCategory.getId()) {
                        setDirty(true);
                    }
                    setErrorInFragment(mEventCatLabel, "");
                    mSelectedCatId = eventCategory.getId();
                    mSelectedCatName = eventCategory.getName();
                    setTextInFragment(mEventCatBtn, mSelectedCatName);
                    eventCatDialog.dismiss();
                }
            });
        }
        eventCatDialog.setCancelable(true);
        eventCatDialog.setTitle(getStringInFragment(R.string.select_event_cat));
        eventCatDialog.show();
    }

    public void resetErrors() {
        setErrorInFragment(mNameEditText, "");
        setErrorInFragment(mEventCatLabel, "");
        setErrorInFragment(mProvinceEditText, "");
        setErrorInFragment(mCityEditText, "");
        setErrorInFragment(mAddressEditText, "");
        setErrorInFragment(mStartLabelTextView, "");
        setErrorInFragment(mEndLabelTextView, "");
        setErrorInFragment(mDescriptionEditText, "");
        setErrorInFragment(mCellNumberEditText, "");
        setErrorInFragment(mPhoneNumberEditText, "");
    }

    private boolean formInputsAreValid() {
        if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
            return false;
        }

        if (!isAdded()) {
            return false;
        }

        resetErrors();

        if (getTextInFragment(mNameEditText).length() == 0) {
            requestFocusInFragment(mNameEditText);
            setErrorInFragment(mNameEditText, getStringInFragment(R.string.error_event_name_cant_be_empty));
            displayShortToast(getStringInFragment(R.string.error_event_name_cant_be_empty));
            return false;
        }

        if (mSelectedCatId == -1) {
            requestFocusInFragment(mEventCatLabel);
            setErrorInFragment(mEventCatLabel, getStringInFragment(R.string.error_event_cat_is_not_selected));
            displayShortToast(getStringInFragment(R.string.error_event_cat_is_not_selected));
            return false;
        }

        if (mSelectedProvinceId < 0) {
            requestFocusInFragment(mProvinceEditText);
            setErrorInFragment(mProvinceEditText, getStringInFragment(R.string.error_event_province_not_set));
            displayShortToast(getStringInFragment(R.string.error_event_province_not_set));
            return false;
        }

        if (mSelectedCityId < 0) {
            requestFocusInFragment(mCityEditText);
            setErrorInFragment(mCityEditText, getStringInFragment(R.string.error_event_city_not_set));
            displayShortToast(getStringInFragment(R.string.error_event_city_not_set));
            return false;
        }

        if (getTextInFragment(mAddressEditText).length() == 0) {
            requestFocusInFragment(mAddressEditText);
            setErrorInFragment(mAddressEditText, getStringInFragment(R.string.error_event_address_is_not_specified));
            displayShortToast(getStringInFragment(R.string.error_event_address_is_not_specified));
            return false;
        }

        if (!isAdded() || mStartYearSpinner == null) {
            return false;
        }

        String startYearValue = mStartYearSpinner.getItemAtPosition(mStartYearSpinner.getSelectedItemPosition()).toString();
        String startMonthValue = (mStartMonthSpinner.getSelectedItemPosition() + 1) + "";
        String startDayValue = (mStartDaySpinner.getSelectedItemPosition() + 1) + "";

        String userEnteredStartDate = startYearValue + "/" + startMonthValue + "/" + startDayValue;
        Date startDate = JalaliCalendar.getGregorianDate(userEnteredStartDate);
        if (mStartDate != null) {
            if (mStartDate.getDay() != startDate.getDay() || mStartDate.getMonth() != startDate.getMonth() || mStartDate.getYear() != startDate.getYear()) {
                setDirty(true);
            }
        } else {
            if (startDate != null) {
                setDirty(true);
            }
        }
        mStartDate = startDate;
        String checkJalaliDate = JalaliCalendar.getJalaliDate(mStartDate);
        if (!isAdded()) {
            return false;
        }
        if (!checkJalaliDate.equals(userEnteredStartDate)) {
            requestFocusInFragment(mStartDaySpinner);
            setErrorInFragment(mStartLabelTextView, getStringInFragment(R.string.wrong_start_date));
            displayShortToast(getStringInFragment(R.string.wrong_start_date));
            return false;
        }

        if (!isAdded()) {
            return false;
        }
        String toYearValue = mEndYearSpinner.getItemAtPosition(mEndYearSpinner.getSelectedItemPosition()).toString();
        String toMonthValue = (mEndMonthSpinner.getSelectedItemPosition() + 1) + "";
        String toDayValue = (mEndDaySpinner.getSelectedItemPosition() + 1) + "";

        String userEnteredEndDate = toYearValue + "/" + toMonthValue + "/" + toDayValue;
        Date endDate = JalaliCalendar.getGregorianDate(userEnteredEndDate);
        if (mEndDate != null) {
            if (mEndDate.getDay() != endDate.getDay() || mEndDate.getMonth() != endDate.getMonth() || mEndDate.getYear() != endDate.getYear()) {
                setDirty(true);
            }
        } else {
            if (endDate != null) {
                setDirty(true);
            }
        }
        mEndDate = endDate;
        checkJalaliDate = JalaliCalendar.getJalaliDate(mEndDate);

        if (!checkJalaliDate.equals(userEnteredEndDate)) {
            requestFocusInFragment(mEndDaySpinner);
            setErrorInFragment(mEndLabelTextView, getStringInFragment(R.string.wrong_end_date));
            displayShortToast(getStringInFragment(R.string.wrong_end_date));
            return false;
        }

        if (mStartDate.after(mEndDate)) {
            requestFocusInFragment(mStartDaySpinner);
            setErrorInFragment(mStartLabelTextView, "تاریخ شروع بزرگتر از تاریخ پایان است.");
            displayShortToast("تاریخ شروع بزرگتر از تاریخ پایان است.");
            return false;
        }

        if (System.currentTimeMillis() > mEndDate.getTime()) {
            requestFocusInFragment(mEndDaySpinner);
            setErrorInFragment(mEndLabelTextView, "تاریخ پایان رویداد گذشته است.");
            displayShortToast("تاریخ پایان رویداد گذشته است.");
            return false;
        }

        if (getTextInFragment(mDescriptionEditText).length() == 0) {
            requestFocusInFragment(mDescriptionEditText);
            setErrorInFragment(mDescriptionEditText, getStringInFragment(R.string.error_event_desc_cant_be_empty));
            displayShortToast(getStringInFragment(R.string.error_event_desc_cant_be_empty));
            return false;
        }

        if (getTextInFragment(mCellNumberEditText).length() == 0 && getTextInFragment(mPhoneNumberEditText).length() == 0) {
            requestFocusInFragment(mCellNumberEditText);
            setErrorInFragment(mCellNumberEditText, getStringInFragment(R.string.fill_at_least_one_communication_ways));
            displayShortToast(getStringInFragment(R.string.fill_at_least_one_communication_ways));
            return false;
        }

        if (getTextInFragment(mCellNumberEditText).length() > 0) {
            String mobileNumberPattern = "^09\\d{9}$";
            if (!getTextInFragment(mCellNumberEditText).matches(mobileNumberPattern)) {
                requestFocusInFragment(mCellNumberEditText);
                setErrorInFragment(mCellNumberEditText, getStringInFragment(net.honarnama.base.R.string.error_mobile_number_is_not_valid));
                displayShortToast(getStringInFragment(net.honarnama.base.R.string.error_mobile_number_is_not_valid));
                return false;
            }
        }

        if (getTextInFragment(mPhoneNumberEditText).length() > 0) {
            String phoneNumberPattern = "^(0[0-9]{2,3}-?)?[0-9]{6,14}$";
            if (!getTextInFragment(mPhoneNumberEditText).matches(phoneNumberPattern)) {
                requestFocusInFragment(mPhoneNumberEditText);
                setErrorInFragment(mPhoneNumberEditText, getStringInFragment(R.string.error_phone_number_is_not_valid));
                displayShortToast(getStringInFragment(R.string.error_phone_number_is_not_valid));
                return false;
            }
        }

        if (!mDirty) {
            displayLongToast(getStringInFragment(R.string.item_not_changed));
            return false;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (isAdded() && mBannerImageView != null) {
            mBannerImageView.onActivityResult(requestCode, resultCode, intent);
        }
    }

    private void setEventInfo(net.honarnama.nano.Event event, boolean loadImages) {
        if (!isAdded()) {
            return;
        }
        if (event != null) {
            if (BuildConfig.DEBUG) {
                logD("event info: " + event);
            }
            Activity activity = getActivity();
            mEventId = event.id;
            mIsNew = false;
            mStartDate = new Date(event.startAt * 1000);
            String jalaliStartDate = JalaliCalendar.getJalaliDate(mStartDate);
            String[] separatedJalaliStartDate = jalaliStartDate.split("/");
            String startYear = separatedJalaliStartDate[0];
            int startMonth = Integer.valueOf(separatedJalaliStartDate[1]);
            int startDay = Integer.valueOf(separatedJalaliStartDate[2]);

            setSelectionInFragment(mStartDaySpinner, startDay - 1);
            setSelectionInFragment(mStartMonthSpinner, startMonth - 1);

            if (!isAdded()) {
                return;
            }

            ArrayAdapter<String> yearAdapter = (ArrayAdapter<String>) mStartYearSpinner.getAdapter();
            if (yearAdapter != null) {
                setSelectionInFragment(mStartYearSpinner, yearAdapter.getPosition(startYear));
            }

            mEndDate = new Date(event.endAt * 1000);
            String jalaliEndDate = JalaliCalendar.getJalaliDate(mEndDate);
            String[] separatedJalaliEndDate = jalaliEndDate.split("/");
            String endYear = separatedJalaliEndDate[0];
            int endMonth = Integer.valueOf(separatedJalaliEndDate[1]);
            int endDay = Integer.valueOf(separatedJalaliEndDate[2]);

            setSelectionInFragment(mEndDaySpinner, endDay - 1);
            setSelectionInFragment(mEndMonthSpinner, endMonth - 1);

            if (!isAdded()) {
                return;
            }

            yearAdapter = (ArrayAdapter<String>) mEndYearSpinner.getAdapter();
            if (yearAdapter != null) {
                setSelectionInFragment(mEndYearSpinner, yearAdapter.getPosition(endYear));
            }

            mActive = event.active;

            setCheckedInFragment(mActiveBtn, event.active);
            setCheckedInFragment(mPassiveBtn, !event.active);

            setTextInFragment(mNameEditText, event.name);
            setTextInFragment(mAddressEditText, event.address);
            setTextInFragment(mDescriptionEditText, event.description);
            setTextInFragment(mPhoneNumberEditText, event.phoneNumber);
            setTextInFragment(mCellNumberEditText, event.cellNumber);

            mSelectedCatId = event.eventCategoryId;
            mSelectedProvinceId = event.locationCriteria.provinceId;
            rePopulateCityList(false);
            mSelectedCityId = event.locationCriteria.cityId;

            Province province = Province.getProvinceById(mSelectedProvinceId);
            City city = City.getCityById(mSelectedCityId);

            setTextInFragment(mEventCatBtn, getStringInFragment(R.string.getting_information));
            EventCategory eventCategory = EventCategory.getCategoryById(mSelectedCatId);

            if (eventCategory == null) {
                displayLongToast(getStringInFragment(R.string.error_finding_category_name) + getStringInFragment(R.string.check_net_connection));
                setTextInFragment(mEventCatBtn, getStringInFragment(R.string.error_getting_info));
                mSelectedCatId = -1;
            } else {
                setTextInFragment(mEventCatBtn, eventCategory.getName());
            }

            if (!TextUtils.isEmpty(city.getName())) {
                setTextInFragment(mCityEditText, city.getName());
            }

            if (!TextUtils.isEmpty(province.getName())) {
                setTextInFragment(mProvinceEditText, province.getName());
            }

            mReviewStatus = event.reviewStatus;
            setReviewInfo(mReviewStatus);

            if (loadImages && !TextUtils.isEmpty(event.banner) && activity != null && isAdded()) {
                setVisibilityInFragment(mBannerProgressBar, View.VISIBLE);
                final String eventBanner = event.banner;
                Picasso.with(activity).load(event.banner)
                        .error(R.drawable.party_flags)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .into(mBannerImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                setVisibilityInFragment(mBannerProgressBar, View.GONE);
                                if (isAdded() && mBannerImageView != null) {
                                    mBannerImageView.setFileSet(true);
                                    mBannerImageView.setLoadingURL(eventBanner);
                                }
                            }

                            @Override
                            public void onError() {
                                setVisibilityInFragment(mBannerProgressBar, View.GONE);
                                displayShortToast(getStringInFragment(R.string.error_displaying_event_banner) + getStringInFragment(R.string.check_net_connection));
                            }
                        });
            }
        }

        addListenersToMakeDirty();
        setDirty(false);
    }

    public void setReviewInfo(int reviewStatus) {
        if (reviewStatus == HonarnamaProto.NOT_REVIEWED) {
            setVisibilityInFragment(mStatusBarTextView, View.VISIBLE);
            setTextInFragment(mStatusBarTextView, getStringInFragment(R.string.waiting_to_be_confirmed));
        }

        if (reviewStatus == HonarnamaProto.CHANGES_NEEDED) {
            setVisibilityInFragment(mEventNotVerifiedNotif, View.VISIBLE);
            setVisibilityInFragment(mStatusBarTextView, View.VISIBLE);
            setTextInFragment(mStatusBarTextView, getStringInFragment(R.string.please_apply_requested_modification));
        }
    }

    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        if (isAdded()) {
            int scrollY = mScrollView.getScrollY();
            // Add parallax effect
            mBannerFrameLayout.setTranslationY(scrollY * 0.5f);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mBannerImageView != null) {
            mBannerImageView.onSaveInstanceState(outState);
        }

        outState.putLong(SAVE_INSTANCE_STATE_KEY_EVENT_ID, mEventId);
        outState.putBoolean(SAVE_INSTANCE_STATE_KEY_DIRTY, mDirty);
        outState.putString(SAVE_INSTANCE_STATE_KEY_NAME, getTextInFragment(mNameEditText));
        outState.putInt(SAVE_INSTANCE_STATE_KEY_PROVINCE_ID, mSelectedProvinceId);
        outState.putString(SAVE_INSTANCE_STATE_KEY_PROVINCE_NAME, mSelectedProvinceName);
        outState.putInt(SAVE_INSTANCE_STATE_KEY_CITY_ID, mSelectedCityId);
        outState.putString(SAVE_INSTANCE_STATE_KEY_CITY_NAME, mSelectedCityName);
        outState.putString(SAVE_INSTANCE_STATE_KEY_DESC, getTextInFragment(mDescriptionEditText));
        outState.putString(SAVE_INSTANCE_STATE_KEY_PHONE, getTextInFragment(mPhoneNumberEditText));
        outState.putString(SAVE_INSTANCE_STATE_KEY_CELL, getTextInFragment(mCellNumberEditText));
        outState.putBoolean(SAVE_INSTANCE_STATE_KEY_CONTENT_IS_VISIBLE, (mMainContent.getVisibility() == View.VISIBLE));
        outState.putInt(SAVE_INSTANCE_STATE_KEY_REVIEW_STATUS, mReviewStatus);
        outState.putString(SAVE_INSTANCE_STATE_KEY_ADDR, getTextInFragment(mAddressEditText));
        outState.putInt(SAVE_INSTANCE_STATE_KEY_CATEGORY, mSelectedCatId);
        outState.putBoolean(SAVE_INSTANCE_STATE_KEY_ACTIVE, mActiveBtn.isChecked());


        outState.putInt(SAVE_INSTANCE_STATE_KEY_START_YEAR, mStartYearSpinner.getSelectedItemPosition());
        outState.putInt(SAVE_INSTANCE_STATE_KEY_START_MONTH, mStartMonthSpinner.getSelectedItemPosition());
        outState.putInt(SAVE_INSTANCE_STATE_KEY_START_DAY, mStartDaySpinner.getSelectedItemPosition());

        outState.putInt(SAVE_INSTANCE_STATE_KEY_END_YEAR, mEndYearSpinner.getSelectedItemPosition());
        outState.putInt(SAVE_INSTANCE_STATE_KEY_END_MONTH, mEndMonthSpinner.getSelectedItemPosition());
        outState.putInt(SAVE_INSTANCE_STATE_KEY_END_DAY, mEndDaySpinner.getSelectedItemPosition());

        if (mStartDate != null) {
            outState.putLong(SAVE_INSTANCE_STATE_KEY_START_DATE, mStartDate.getTime());
        }
        if (mEndDate != null) {
            outState.putLong(SAVE_INSTANCE_STATE_KEY_END_DATE, mEndDate.getTime());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_main, menu);
    }

    public class getEventAsync extends AsyncTask<Void, Void, GetEventReply> {
        SimpleRequest simpleRequest;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setVisibilityInFragment(mMainContent, View.GONE);
            setTextInFragment(mEmptyView, getStringInFragment(R.string.getting_information));
            setVisibilityInFragment(mEmptyView, View.VISIBLE);
            displayProgressDialog(null);
        }

        @Override
        protected GetEventReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            simpleRequest = new SimpleRequest();
            simpleRequest.requestProperties = rp;

            if (BuildConfig.DEBUG) {
                logD("simpleRequest for getting myEvent is: " + simpleRequest);
            }

            GetEventReply getEventReply;
            try {
                SellServiceGrpc.SellServiceBlockingStub stub = GRPCUtils.getInstance().getSellServiceGrpc();
                getEventReply = stub.getMyEvent(simpleRequest);
                return getEventReply;
            } catch (Exception e) {
                logE("Error getting event info. simpleRequest: " + simpleRequest + ". Error: " + e, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(GetEventReply getEventReply) {
            super.onPostExecute(getEventReply);

            if (BuildConfig.DEBUG) {
                logD("getEventReply is: " + getEventReply);
            }

            Activity activity = getActivity();

            dismissProgressDialog();
            if (getEventReply != null) {
                switch (getEventReply.replyProperties.statusCode) {
                    case ReplyProperties.OK:
                        if (getEventReply.event != null) {
                            setVisibilityInFragment(mEmptyView, View.GONE);
                            setVisibilityInFragment(mMainContent, View.VISIBLE);
                            setEventInfo(getEventReply.event, true);
                        } else {
                            setTextInFragment(mEmptyView, getStringInFragment(R.string.error_getting_event_info));
                            displayShortToast(getStringInFragment(R.string.error_getting_event_info));
                            displayRetrySnackbar();
                            logE("Got OK code for getting user (id " + HonarnamaUser.getId() + ") event, but event was null. simpleRequest: " + simpleRequest);
                        }
                        break;

                    case ReplyProperties.CLIENT_ERROR:
                        switch (getEventReply.errorCode) {
                            case GetEventReply.NO_CLIENT_ERROR:
                                logE("Got NO_CLIENT_ERROR code for getting user (id " + HonarnamaUser.getId() + ") event. simpleRequest: " + simpleRequest);
                                displayShortToast(getStringInFragment(R.string.error_getting_info));
                                break;

                            case GetEventReply.EVENT_NOT_FOUND:
                                setVisibilityInFragment(mEmptyView, View.GONE);
                                setVisibilityInFragment(mMainContent, View.VISIBLE);
                                mIsNew = true;
                                if (BuildConfig.DEBUG) {
                                    logD("Event not found.");
                                }
                                break;
                        }
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        setTextInFragment(mEmptyView, getStringInFragment(R.string.error_getting_event_info));
                        displayRetrySnackbar();
                        displayShortToast(getStringInFragment(R.string.server_error_try_again));
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        HonarnamaUser.logout(activity);
                        displayLongToast(getStringInFragment(R.string.login_again));
                        break;

                    case ReplyProperties.UPGRADE_REQUIRED:
                        if (activity != null) {
                            ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) activity);
                            controlPanelActivity.displayUpgradeRequiredDialog();
                        } else {
                            displayLongToast(getStringInFragment(R.string.upgrade_to_new_version));
                        }
                        break;
                }

            } else {
                setTextInFragment(mEmptyView, getStringInFragment(R.string.error_getting_event_info));
                displayRetrySnackbar();
            }
        }
    }

    public class CreateOrUpdateEventAsync extends AsyncTask<Void, Void, CreateOrUpdateEventReply> {
        CreateOrUpdateEventRequest createOrUpdateEventRequest;

        String cToastMsg = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            displayProgressDialog(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (!TextUtils.isEmpty(cToastMsg)) {
                        displayLongToast(cToastMsg);
                        cToastMsg = "";
                    }
                }
            });

            createOrUpdateEventRequest = new CreateOrUpdateEventRequest();
            createOrUpdateEventRequest.event = new net.honarnama.nano.Event();
            if (BuildConfig.DEBUG) {
                logD("CreateOrUpdateEventAsync:: mSelectedCityId: " + mSelectedCityId);
            }
            createOrUpdateEventRequest.event.name = getTextInFragment(mNameEditText);
            createOrUpdateEventRequest.event.description = getTextInFragment(mDescriptionEditText);
            createOrUpdateEventRequest.event.address = getTextInFragment(mAddressEditText);
            createOrUpdateEventRequest.event.phoneNumber = getTextInFragment(mPhoneNumberEditText);
            createOrUpdateEventRequest.event.cellNumber = getTextInFragment(mCellNumberEditText);
            createOrUpdateEventRequest.event.eventCategoryId = mSelectedCatId;
            createOrUpdateEventRequest.event.active = (mActiveBtn != null) ? mActiveBtn.isChecked() : false;
            createOrUpdateEventRequest.event.startAt = mStartDate.getTime() / 1000;
            createOrUpdateEventRequest.event.endAt = mEndDate.getTime() / 1000;
            createOrUpdateEventRequest.event.locationCriteria = new LocationCriteria();
            createOrUpdateEventRequest.event.locationCriteria.provinceId = mSelectedProvinceId;
            createOrUpdateEventRequest.event.locationCriteria.cityId = mSelectedCityId;

            if (mBannerImageView != null && mBannerImageView.isDeleted()) {
                createOrUpdateEventRequest.changingBanner = HonarnamaProto.DELETE;
            } else if (mBannerImageView != null && mBannerImageView.isChanged() && mBannerImageView.getFinalImageUri() != null) {
                createOrUpdateEventRequest.changingBanner = HonarnamaProto.PUT;
            } else {
                createOrUpdateEventRequest.changingBanner = HonarnamaProto.NOOP;
            }

        }

        @Override
        protected CreateOrUpdateEventReply doInBackground(Void... voids) {

            if (TextUtils.isEmpty(createOrUpdateEventRequest.event.name)) {
                return null;
            }

            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            createOrUpdateEventRequest.requestProperties = rp;
            if (BuildConfig.DEBUG) {
                logD("createOrUpdateEventRequest is: " + createOrUpdateEventRequest);
            }

            CreateOrUpdateEventReply createOrUpdateEventReply;
            try {
                SellServiceGrpc.SellServiceBlockingStub stub = GRPCUtils.getInstance().getSellServiceGrpc();
                if (mEventId > 0) {
                    createOrUpdateEventRequest.event.id = mEventId;
                    createOrUpdateEventReply = stub.updateEvent(createOrUpdateEventRequest);
                } else {
                    createOrUpdateEventReply = stub.createEvent(createOrUpdateEventRequest);
                }
                return createOrUpdateEventReply;
            } catch (Exception e) {
                logE("Error running createOrUpdateEventRequest. createOrUpdateEventRequest: " + createOrUpdateEventRequest + ". Error: " + e, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final CreateOrUpdateEventReply createOrUpdateEventReply) {
            super.onPostExecute(createOrUpdateEventReply);

            Activity activity = getActivity();
            if (BuildConfig.DEBUG) {
                logD("createOrUpdateEventReply is: " + createOrUpdateEventReply);
            }
            if (createOrUpdateEventReply != null) {
                switch (createOrUpdateEventReply.replyProperties.statusCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:
                        if (activity != null) {
                            ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) activity);
                            controlPanelActivity.displayUpgradeRequiredDialog();
                        } else {
                            cToastMsg = getStringInFragment(R.string.upgrade_to_new_version);
                        }
                        dismissProgressDialog();
                        break;
                    case ReplyProperties.CLIENT_ERROR:
                        switch (createOrUpdateEventReply.errorCode) {
                            case CreateOrUpdateEventReply.NO_CLIENT_ERROR:
                                logE("Got NO_CLIENT_ERROR code for createOrUpdateEventReply. createOrUpdateEventRequest: " + createOrUpdateEventRequest + ". User id: " + HonarnamaUser.getId());
                                cToastMsg = getStringInFragment(R.string.error_getting_info);
                                break;
                            case CreateOrUpdateEventReply.EVENT_NOT_FOUND:
                                cToastMsg = getStringInFragment(R.string.event_not_found);
                                break;
                            case CreateOrUpdateEventReply.EMPTY_EVENT:
                                logE("createOrUpdateEventReply was EMPTY_EVENT. createOrUpdateEventRequest: " + createOrUpdateEventRequest);
                                cToastMsg = getStringInFragment(R.string.error_getting_info);
                                break;
                            case CreateOrUpdateEventReply.STORE_NOT_CREATED:
                                cToastMsg = getStringInFragment(R.string.store_not_created);
                                break;
                            case CreateOrUpdateEventReply.ALREADY_HAS_EVENT:
                                cToastMsg = "در حال حاضر رویداد دیگری دارید.";
                                break;
                        }
                        dismissProgressDialog();
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        cToastMsg = getStringInFragment(R.string.server_error_try_again);
                        dismissProgressDialog();
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        HonarnamaUser.logout(activity);
                        cToastMsg = getStringInFragment(R.string.login_again);
                        dismissProgressDialog();
                        break;

                    case ReplyProperties.OK:
                        setReviewInfo(HonarnamaProto.NOT_REVIEWED);
                        setEventInfo(createOrUpdateEventReply.uptodateEvent, false);

                        if (isAdded() && mBannerImageView != null && mBannerImageView.isDeleted()) {
                            mBannerImageView.setDeleted(false);
                        }

                        if (!TextUtils.isEmpty(createOrUpdateEventReply.bannerModificationUrl) && mBannerImageView != null) {
                            final File bannerImageFile = (mBannerImageView.getFinalImageUri() != null) ? new File(mBannerImageView.getFinalImageUri().getPath()) : null;
                            if (bannerImageFile == null) {
                                if (BuildConfig.DEBUG) {
                                    logD("Uploading event image failed. bannerImageFile was null.");
                                }
                                cToastMsg = getStringInFragment(R.string.error_uploading_event_banner);
                                setDirty(true);
                                dismissProgressDialog();
                                return;
                            }
                            final Uploader uploader = new Uploader(bannerImageFile, createOrUpdateEventReply.bannerModificationUrl);
                            uploader.upload().continueWith(new Continuation<Void, Object>() {
                                @Override
                                public Object then(Task<Void> task) throws Exception {
                                    if (BuildConfig.DEBUG) {
                                        logD("Continue event image upload task.");
                                    }
                                    if (task.isFaulted()) {
                                        if (BuildConfig.DEBUG) {
                                            logD("Uploading event image failed.");
                                        }
                                        setDirty(true);
                                        cToastMsg = getStringInFragment(R.string.error_uploading_event_banner) + getStringInFragment(R.string.check_net_connection);
                                    } else {
                                        if (isAdded() && mBannerImageView != null)
                                            mBannerImageView.setChanged(false);
                                        if (BuildConfig.DEBUG) {
                                            logD("Uploading event image done.");
                                        }
                                        cToastMsg = getStringInFragment(R.string.successfully_changed_event_info);
                                    }

                                    dismissProgressDialog();
                                    return null;
                                }
                            });
                        } else {
                            cToastMsg = getStringInFragment(R.string.successfully_changed_event_info);
                            dismissProgressDialog();
                        }
                        break;
                }

            } else {
                cToastMsg = getStringInFragment(R.string.error_connecting_server_try_again);
                dismissProgressDialog();
            }
        }

    }

    public void displayRetrySnackbar() {

        dismissSnackbar();
        Activity activity = getActivity();

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(" ").append(getStringInFragment(R.string.error_connecting_server_try_again)).append(" ");

        View sbView = null;
        TextView textView = null;

        if (!isAdded()) {
            return;
        }
        mSnackbar = Snackbar.make(mCoordinatorLayout, builder, Snackbar.LENGTH_INDEFINITE);
        if (mSnackbar != null) {
            sbView = mSnackbar.getView();
        }
        if (sbView != null) {
            sbView.setBackgroundColor(getResources().getColor(R.color.amber));
            textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        }

        if (textView != null) {
            textView.setBackgroundColor(getResources().getColor(R.color.amber));
            textView.setSingleLine(false);
            textView.setGravity(Gravity.CENTER);

            Spannable spannable = (Spannable) textView.getText();

            if (spannable != null && activity != null) {
                spannable.setSpan(new ImageSpan(activity, android.R.drawable.stat_notify_sync), textView.getText().length() - 1, textView.getText().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NetworkManager.getInstance().isNetworkEnabled(true)) {

                        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
                        controlPanelActivity.checkAndUpdateMeta(false);

                        new getEventAsync().execute();
                        dismissSnackbar();
                    }
                }
            });
        }

        if (isAdded() && mSnackbar != null) {
            mSnackbar.show();
        }
    }

    public void dismissSnackbar() {
        if (isAdded() && mSnackbar != null && mSnackbar.isShown()) {
            mSnackbar.dismiss();
        }
    }

    public void reset() {
        if (BuildConfig.DEBUG) {
            logD("reset of SF");
        }
        setTextInFragment(mNameEditText, "");
        setTextInFragment(mAddressEditText, "");
        setTextInFragment(mDescriptionEditText, "");
        setTextInFragment(mPhoneNumberEditText, "");
        setTextInFragment(mCellNumberEditText, "");

        if (mBannerImageView != null && isAdded()) {
            mBannerImageView.removeSelectedImage();
            mBannerImageView.setChanged(false);
            mBannerImageView.setDeleted(false);
        }

        resetErrors();

        mIsNew = true;
        mEventId = -1;

        mReviewStatus = -1;

        setDirty(false);
        addListenersToMakeDirty();
        dismissSnackbar();
    }

    public void addListenersToMakeDirty() {
        if (isAdded() && mNameEditText != null) {
            mNameEditText.addTextChangedListener(mTextWatcherToMarkDirty);
            mAddressEditText.addTextChangedListener(mTextWatcherToMarkDirty);
            mDescriptionEditText.addTextChangedListener(mTextWatcherToMarkDirty);
            mPhoneNumberEditText.addTextChangedListener(mTextWatcherToMarkDirty);
            mCellNumberEditText.addTextChangedListener(mTextWatcherToMarkDirty);
        }
    }

}
