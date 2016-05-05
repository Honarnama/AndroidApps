package net.honarnama.sell.fragments;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.parse.ImageSelector;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import net.honarnama.GRPCUtils;
import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.adapter.CityAdapter;
import net.honarnama.core.adapter.EventCategoriesAdapter;
import net.honarnama.core.adapter.ProvincesAdapter;
import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.core.helper.MetaUpdater;
import net.honarnama.core.interfaces.MetaUpdateListener;
import net.honarnama.core.model.City;
import net.honarnama.core.model.EventCategory;
import net.honarnama.core.model.Province;
import net.honarnama.core.utils.GravityTextWatcher;
import net.honarnama.core.utils.JalaliCalendar;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.core.utils.ObservableScrollView;
import net.honarnama.core.utils.WindowUtil;
import net.honarnama.nano.CreateOrUpdateEventReply;
import net.honarnama.nano.CreateOrUpdateEventRequest;
import net.honarnama.nano.GetEventReply;
import net.honarnama.nano.HonarnamaProto;
import net.honarnama.nano.LocationId;
import net.honarnama.nano.ReplyProperties;
import net.honarnama.nano.RequestProperties;
import net.honarnama.nano.SellServiceGrpc;
import net.honarnama.nano.SimpleRequest;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;
import net.honarnama.sell.activity.ControlPanelActivity;
import net.honarnama.sell.model.HonarnamaUser;
import net.honarnama.sell.utils.Uploader;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
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
    List<EventCategory> mEventCategories = new ArrayList<>();
    public HashMap<Integer, String> mEventCategoriesHashMap = new HashMap<>();

    private EditText mProvinceEditText;
    public TreeMap<Number, Province> mProvinceObjectsTreeMap = new TreeMap<>();
    public HashMap<Integer, String> mProvincesHashMap = new HashMap<>();

    private EditText mCityEditText;
    public TreeMap<Number, HashMap<Integer, String>> mCityOrderedTreeMap = new TreeMap<>();
    public HashMap<Integer, String> mCityHashMap = new HashMap<>();


    private Spinner mStartDaySpinner, mStartMonthSpinner, mStartYearSpinner;
    private Spinner mEndDaySpinner, mEndMonthSpinner, mEndYearSpinner;
    public Date mStartDate;
    public Date mEndDate;

    public TextView mStartLabelTextView;
    public TextView mEndLabelTextView;

    ProgressDialog mSendingDataProgressDialog;

    public int mSelectedProvinceId = -1;
    public String mSelectedProvinceName;

    public int mSelectedCityId = -1;
    public String mSelectedCityName;

    public int mSelectedCatId = -1;
    public String mSelectedCatName;

    public ProgressBar mBannerProgressBar;

    public TextView mStatusBarTextView;
    public RelativeLayout mEventNotVerifiedNotif;

    public static EventManagerFragment mEventManagerFragment;

    private Tracker mTracker;

    public boolean mIsNew = true;

    private long mEventId = -1;

    public MetaUpdateListener mMetaUpdateListener;

    Snackbar mSnackbar;

    private CoordinatorLayout mCoordinatorLayout;

    private boolean mDirty = false;
    TextWatcher mTextWatcherToMarkDirty;

    private boolean mEventStatus = true;

    RelativeLayout mMainContent;
    TextView mEmptyView;

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.nav_title_event_manager);
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
        setHasOptionsMenu(true);

        mTracker = HonarnamaSellApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("EventManagerFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
//            Intent intent = new Intent(mActivity, ControlPanelActivity.class);
//            mActivity.finish();
//            startActivity(intent);
//        }

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
                if (mValue != editable + "") {
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

        mSendingDataProgressDialog = new ProgressDialog(mActivity);

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

        ArrayAdapter<String> daysAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.days));
        mStartDaySpinner.setAdapter(daysAdapter);
        mEndDaySpinner.setAdapter(daysAdapter);


        ArrayAdapter<String> monthsAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.months));
        mStartMonthSpinner.setAdapter(monthsAdapter);
        mEndMonthSpinner.setAdapter(monthsAdapter);

        ArrayAdapter<String> yearsAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.years));
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
        mBannerImageView = (ImageSelector) rootView.findViewById(R.id.event_banner_image_view);
        mRegisterEventButton.setOnClickListener(this);

        mBannerImageView.setOnImageSelectedListener(onImageSelectedListener);
        mBannerImageView.setActivity(mActivity);
        if (savedInstanceState != null) {
            mBannerImageView.restore(savedInstanceState);
        }

        mMainContent = (RelativeLayout) rootView.findViewById(R.id.main_content);
        mEmptyView = (TextView) rootView.findViewById(R.id.empty_view);

        loadOfflineData();
        new getEventAsync().execute();

        mMetaUpdateListener = new MetaUpdateListener() {
            @Override
            public void onMetaUpdateDone(int replyCode) {
                dismissProgressDialog();
                switch (replyCode) {
                    case ReplyProperties.OK:
                        loadOfflineData();
                        break;

                    case ReplyProperties.UPGRADE_REQUIRED:
                        ((ControlPanelActivity) mActivity).displayUpgradeRequiredDialog();
                        break;

                    default:
                        displayLongToast(getString(R.string.error_occured) + getString(R.string.check_net_connection));
                        break;
                }
            }
        };

        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id
                .coordinatorLayout);

        return rootView;
    }

    public void loadOfflineData() {
        new Province().getAllProvincesSorted().continueWith(new Continuation<TreeMap<Number, Province>, Object>() {
            @Override
            public Object then(Task<TreeMap<Number, Province>> task) throws Exception {
                if (task.isFaulted()) {
                    displayShortToast(getString(R.string.error_getting_province_list));
                } else {
                    mProvinceObjectsTreeMap = task.getResult();
                    for (Province province : mProvinceObjectsTreeMap.values()) {
                        if (mSelectedProvinceId < 0) {
                            mSelectedProvinceId = province.getId();
                            mSelectedProvinceName = province.getName();
                        }
                        mProvincesHashMap.put(province.getId(), province.getName());
                    }

                    if (mSelectedProvinceId > 0) {
                        mProvinceEditText.setText(mProvincesHashMap.get(mSelectedProvinceId));
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
                    displayShortToast(getString(R.string.error_getting_city_list));
                } else {
                    mCityOrderedTreeMap = task.getResult();
                    for (HashMap<Integer, String> cityMap : mCityOrderedTreeMap.values()) {
                        for (Map.Entry<Integer, String> citySet : cityMap.entrySet()) {
                            if (mSelectedCityId < 0) {
                                mSelectedCityId = citySet.getKey();
                                mSelectedCityName = citySet.getValue();
                            }
                            mCityHashMap.put(citySet.getKey(), citySet.getValue());
                        }
                    }
                    if (mSelectedCityId > 0) {
                        mCityEditText.setText(mCityHashMap.get(mSelectedCityId));
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
                    displayShortToast(getString(R.string.error_getting_event_cat_list));
                } else {
                    mEventCategories = task.getResult();
                    for (int i = 0; i < mEventCategories.size(); i++) {
                        mEventCategoriesHashMap.put(mEventCategories.get(i).getId(), mEventCategories.get(i).getName());
                    }
                    if (mSelectedCatId > 0) {
                        mEventCatBtn.setText(mEventCategoriesHashMap.get(mSelectedCatId));
                    }
                }
                return null;
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        mPhoneNumberEditText.addTextChangedListener(new GravityTextWatcher(mPhoneNumberEditText));
        mCellNumberEditText.addTextChangedListener(new GravityTextWatcher(mCellNumberEditText));
    }

//    public void resetFields() {
//        if (mNameEditText != null) {
//            mNameEditText.setText("");
//            mDescriptionEditText.setText("");
//            mPhoneNumberEditText.setText("");
//            mCellNumberEditText.setText("");
//            mAddressEditText.setText("");
//            mActiveBtn.setChecked(true);
//            mPassiveBtn.setChecked(false);
//            mSelectedCatId = -1;
//            mEventCatBtn.setText(getString(R.string.select));
//            mNameEditText.setError(null);
//            mAddressEditText.setError(null);
//            mDescriptionEditText.setError(null);
//            mPhoneNumberEditText.setError(null);
//            mCellNumberEditText.setError(null);
//            setDirty(false);
//        }
//    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_event_button:
                WindowUtil.hideKeyboard(mActivity);
                if (formInputsAreValid()) {
                    new CreateOrUpdateEventAsync().execute();
                }
                break;

            case R.id.event_cat_button:
                displayChooseEventCategoryDialog();
                break;

            case R.id.event_province_edit_text:
                displayProvinceDialog();
                break;

            case R.id.event_city_edit_text:
                displayCityDialog();
                break;

            case R.id.active_event:
                if (mEventStatus != true) {
                    setDirty(true);
                }
                break;

            case R.id.passive_event:
                if (mEventStatus != false) {
                    setDirty(true);
                }
                break;
        }
    }

    private void displayProvinceDialog() {

        ListView provincesListView;
        ProvincesAdapter provincesAdapter;

        final Dialog provinceDialog = new Dialog(mActivity, R.style.DialogStyle);

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
            provincesAdapter = new ProvincesAdapter(mActivity, mProvinceObjectsTreeMap);
            provincesListView.setAdapter(provincesAdapter);
            provincesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Province selectedProvince = mProvinceObjectsTreeMap.get(position + 1);
                    if (mSelectedProvinceId != selectedProvince.getId()) {
                        setDirty(true);
                    }
                    mSelectedProvinceId = selectedProvince.getId();
                    mSelectedProvinceName = selectedProvince.getName();
                    mProvinceEditText.setText(mSelectedProvinceName);
                    rePopulateCityList();
                    provinceDialog.dismiss();
                }
            });
        }
        provinceDialog.setCancelable(true);
        provinceDialog.setTitle(getString(R.string.select_province));
        provinceDialog.show();
    }

    private void rePopulateCityList() {
        City city = new City();
        city.getAllCitiesSorted(mSelectedProvinceId).continueWith(new Continuation<TreeMap<Number, HashMap<Integer, String>>, Object>() {
            @Override
            public Object then(Task<TreeMap<Number, HashMap<Integer, String>>> task) throws Exception {
                if (task.isFaulted()) {
                    displayLongToast(getString(R.string.error_getting_city_list) + getString(R.string.check_net_connection));
                } else {
                    mCityOrderedTreeMap = task.getResult();
                    if (!mCityOrderedTreeMap.isEmpty()) {
                        for (HashMap<Integer, String> cityMap : mCityOrderedTreeMap.values()) {
                            for (Map.Entry<Integer, String> citySet : cityMap.entrySet()) {
                                mCityHashMap.put(citySet.getKey(), citySet.getValue());
                            }
                        }

                        Set<Integer> tempSet = mCityOrderedTreeMap.get(1).keySet();
                        for (Integer key : tempSet) {
                            mSelectedCityId = key;
                            mCityEditText.setText(mCityHashMap.get(key));
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

        final Dialog cityDialog = new Dialog(mActivity, R.style.DialogStyle);

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

            cityAdapter = new CityAdapter(mActivity, mCityOrderedTreeMap);
            cityListView.setAdapter(cityAdapter);
            cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    HashMap<Integer, String> selectedCity = mCityOrderedTreeMap.get(position + 1);
                    for (int key : selectedCity.keySet()) {
                        if (mSelectedCityId != key) {
                            setDirty(true);
                        }
                        mSelectedCityId = key;
                    }
                    for (String value : selectedCity.values()) {
                        mSelectedCityName = value;
                        mCityEditText.setText(mSelectedCityName);
                    }
                    cityDialog.dismiss();
                }
            });
        }
        cityDialog.setCancelable(true);
        cityDialog.setTitle(getString(R.string.select_city));
        cityDialog.show();
    }


    private void displayChooseEventCategoryDialog() {

        ListView eventCatsListView;
        EventCategoriesAdapter eventCatsAdapter;

        final Dialog eventCatDialog = new Dialog(mActivity, R.style.DialogStyle);

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
            eventCatsAdapter = new EventCategoriesAdapter(mActivity, mEventCategories);
            eventCatsListView.setAdapter(eventCatsAdapter);
            eventCatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    EventCategory eventCategory = mEventCategories.get(position);
                    if (mSelectedCatId != eventCategory.getId()) {
                        setDirty(true);
                    }
                    mEventCatLabel.setError(null);
                    mSelectedCatId = eventCategory.getId();
                    mSelectedCatName = eventCategory.getName();
                    mEventCatBtn.setText(mSelectedCatName);
                    eventCatDialog.dismiss();
                }
            });
        }
        eventCatDialog.setCancelable(true);
        eventCatDialog.setTitle(getString(R.string.select_event_cat));
        eventCatDialog.show();
    }

    private boolean formInputsAreValid() {
        if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
            return false;
        }

        mNameEditText.setError(null);
        mEventCatLabel.setError(null);
        mProvinceEditText.setError(null);
        mCityEditText.setError(null);
        mAddressEditText.setError(null);
        mStartLabelTextView.setError(null);
        mEndLabelTextView.setError(null);
        mDescriptionEditText.setError(null);
        mCellNumberEditText.setError(null);
        mPhoneNumberEditText.setError(null);

        if (mNameEditText.getText().toString().trim().length() == 0) {
            mNameEditText.requestFocus();
            mNameEditText.setError(getString(R.string.error_event_name_cant_be_empty));
            return false;
        }

        if (mSelectedCatId == -1) {
            mEventCatLabel.requestFocus();
            mEventCatLabel.setError(getString(R.string.error_event_cat_is_not_selected));
            displayShortToast(getString(R.string.error_event_cat_is_not_selected));
            return false;
        }

        if (mSelectedProvinceId < 0) {
            mProvinceEditText.requestFocus();
            mProvinceEditText.setError(getString(R.string.error_event_province_not_set));
            return false;
        }

        if (mSelectedCityId < 0) {
            mCityEditText.requestFocus();
            mCityEditText.setError(getString(R.string.error_event_city_not_set));
            return false;
        }

        if (mAddressEditText.getText().toString().trim().length() == 0) {
            mAddressEditText.requestFocus();
            mAddressEditText.setError(getString(R.string.error_event_address_is_not_specified));
            return false;
        }

        String fromYearValue = mStartYearSpinner.getItemAtPosition(mStartYearSpinner.getSelectedItemPosition()).toString();
        String fromMonthValue = (mStartMonthSpinner.getSelectedItemPosition() + 1) + "";
        String fromDayValue = (mStartDaySpinner.getSelectedItemPosition() + 1) + "";

        String userEnteredStartDate = fromYearValue + "/" + fromMonthValue + "/" + fromDayValue;
        Date startDate = JalaliCalendar.getGregorianDate(userEnteredStartDate);
        if (mStartDate.getDay() != startDate.getDay() || mStartDate.getMonth() != startDate.getMonth() || mStartDate.getYear() != startDate.getYear()) {
            setDirty(true);
        }
        mStartDate = startDate;
        String checkJalaliDate = JalaliCalendar.getJalaliDate(mStartDate);
        if (!checkJalaliDate.equals(userEnteredStartDate)) {
            mStartDaySpinner.requestFocus();
            mStartLabelTextView.setError(mContext.getString(R.string.wrong_start_date));
            displayLongToast(mContext.getString(R.string.wrong_start_date));
            return false;
        }

        String toYearValue = mEndYearSpinner.getItemAtPosition(mEndYearSpinner.getSelectedItemPosition()).toString();
        String toMonthValue = (mEndMonthSpinner.getSelectedItemPosition() + 1) + "";
        String toDayValue = (mEndDaySpinner.getSelectedItemPosition() + 1) + "";

        String userEnteredEndDate = toYearValue + "/" + toMonthValue + "/" + toDayValue;
        Date endDate = JalaliCalendar.getGregorianDate(userEnteredEndDate);
        if (mEndDate.getDay() != endDate.getDay() || mEndDate.getMonth() != endDate.getMonth() || mEndDate.getYear() != endDate.getYear()) {
            setDirty(true);
        }
        mEndDate = endDate;
        checkJalaliDate = JalaliCalendar.getJalaliDate(mEndDate);
        if (!checkJalaliDate.equals(userEnteredEndDate)) {
            mEndDaySpinner.requestFocus();
            mEndLabelTextView.setError(mContext.getString(R.string.wrong_end_date));
            displayLongToast(mContext.getString(R.string.wrong_end_date));
            return false;
        }

        if (mStartDate.after(mEndDate)) {
            mStartDaySpinner.requestFocus();
            mStartLabelTextView.setError("تاریخ شروع بزرگتر از تاریخ پایان است.");
            displayLongToast("تاریخ شروع بزرگتر از تاریخ پایان است.");
            return false;
        }

        if (System.currentTimeMillis() > mEndDate.getTime()) {
            mEndDaySpinner.requestFocus();
            mEndLabelTextView.setError("تاریخ پایان رویداد گذشته است.");
            displayLongToast("تاریخ پایان رویداد گذشته است.");
            return false;
        }


        if (mDescriptionEditText.getText().toString().trim().length() == 0) {
            mDescriptionEditText.requestFocus();
            mDescriptionEditText.setError(getString(R.string.error_event_desc_cant_be_empty));
            return false;
        }

        if (mCellNumberEditText.getText().toString().trim().length() == 0 && mPhoneNumberEditText.getText().toString().trim().length() == 0) {
            mCellNumberEditText.requestFocus();
            mCellNumberEditText.setError(getString(R.string.fill_at_least_one_communication_ways));
            return false;
        }

        if (mCellNumberEditText.getText().toString().trim().length() > 0) {
            String mobileNumberPattern = "^09\\d{9}$";
            if (!mCellNumberEditText.getText().toString().trim().matches(mobileNumberPattern)) {
                mCellNumberEditText.requestFocus();
                mCellNumberEditText.setError(getString(net.honarnama.base.R.string.error_mobile_number_is_not_valid));
                return false;
            }
        }

        if (mPhoneNumberEditText.getText().toString().trim().length() > 0) {
            String phoneNumberPattern = "^(0[0-9]{2,3}-?)?[0-9]{6,14}$";
            if (!mPhoneNumberEditText.getText().toString().trim().matches(phoneNumberPattern)) {
                mPhoneNumberEditText.requestFocus();
                mPhoneNumberEditText.setError(getString(R.string.error_phone_number_is_not_valid));
                return false;
            }
        }

        if (!mDirty) {
            displayLongToast(getString(R.string.item_not_changed));
            return false;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mBannerImageView.onActivityResult(requestCode, resultCode, intent);
    }

    private void setEventInfo(net.honarnama.nano.Event event, boolean loadImages) {
        if (event != null) {
            logD("event info: " + event);
            mEventId = event.id;
            mIsNew = false;
            mStartDate = new Date(event.startAt * 1000);
            String jalaliStartDate = JalaliCalendar.getJalaliDate(mStartDate);
            String[] separatedJalaliStartDate = jalaliStartDate.split("/");
            String startYear = separatedJalaliStartDate[0];
            int startMonth = Integer.valueOf(separatedJalaliStartDate[1]);
            int startDay = Integer.valueOf(separatedJalaliStartDate[2]);
            mStartDaySpinner.setSelection(startDay - 1);
            mStartMonthSpinner.setSelection(startMonth - 1);
            ArrayAdapter<String> yearAdapter = (ArrayAdapter<String>) mStartYearSpinner.getAdapter();
            mStartYearSpinner.setSelection(yearAdapter.getPosition(startYear));

            mEndDate = new Date(event.endAt * 1000);
            String jalaliEndDate = JalaliCalendar.getJalaliDate(mEndDate);
            String[] separatedJalaliEndDate = jalaliEndDate.split("/");
            String endYear = separatedJalaliEndDate[0];
            int endMonth = Integer.valueOf(separatedJalaliEndDate[1]);
            int endDay = Integer.valueOf(separatedJalaliEndDate[2]);
            mEndDaySpinner.setSelection(endDay - 1);
            mEndMonthSpinner.setSelection(endMonth - 1);
            yearAdapter = (ArrayAdapter<String>) mEndYearSpinner.getAdapter();
            mEndYearSpinner.setSelection(yearAdapter.getPosition(endYear));


            mEventStatus = event.active;
            mActiveBtn.setChecked(event.active);
            mPassiveBtn.setChecked(!event.active);

            mNameEditText.setText(event.name);
            mAddressEditText.setText(event.address);
            mDescriptionEditText.setText(event.description);

            mPhoneNumberEditText.setText(event.phoneNumber);
            mCellNumberEditText.setText(event.cellNumber);

            mSelectedCatId = event.eventCategoryId;
            mSelectedProvinceId = event.locationId.provinceId;
            mSelectedCityId = event.locationId.cityId;

            Province province = Province.getProvinceById(mSelectedProvinceId);
            City city = City.getCityById(mSelectedCityId);

            mEventCatBtn.setText(getString(R.string.getting_information));
            EventCategory eventCategory = EventCategory.getCategoryById(mSelectedCatId);

            if (eventCategory == null) {
                displayShortToast(getString(R.string.error_finding_category_name) + getString(R.string.check_net_connection));
            } else {
                mEventCatBtn.setText(eventCategory.getName());
            }

            if (!TextUtils.isEmpty(city.getName())) {
                mCityEditText.setText(city.getName());
            }

            if (!TextUtils.isEmpty(province.getName())) {
                mProvinceEditText.setText(province.getName());
            }

            if (event.reviewStatus == HonarnamaProto.NOT_REVIEWED) {
                mStatusBarTextView.setVisibility(View.VISIBLE);
            }

            if (event.reviewStatus == HonarnamaProto.CHANGES_NEEDED) {
                mEventNotVerifiedNotif.setVisibility(View.VISIBLE);
                mStatusBarTextView.setVisibility(View.VISIBLE);
                mStatusBarTextView.setText(getString(R.string.please_apply_requested_modification));
            }

            if (loadImages && !TextUtils.isEmpty(event.banner)) {
                mBannerProgressBar.setVisibility(View.VISIBLE);

                Picasso.with(mActivity).load(event.banner)
                        .error(R.drawable.party_flags)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .into(mBannerImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                mBannerProgressBar.setVisibility(View.GONE);
                                mBannerImageView.setFileSet(true);
                            }

                            @Override
                            public void onError() {
                                mBannerProgressBar.setVisibility(View.GONE);
                                displayShortToast(getString(R.string.error_displaying_event_banner) + getString(R.string.check_net_connection));
                            }
                        });
            }
        }

        mNameEditText.addTextChangedListener(mTextWatcherToMarkDirty);
        mAddressEditText.addTextChangedListener(mTextWatcherToMarkDirty);
        mDescriptionEditText.addTextChangedListener(mTextWatcherToMarkDirty);
        mPhoneNumberEditText.addTextChangedListener(mTextWatcherToMarkDirty);
        mCellNumberEditText.addTextChangedListener(mTextWatcherToMarkDirty);

        setDirty(false);
    }

    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        int scrollY = mScrollView.getScrollY();
        // Add parallax effect
        mBannerFrameLayout.setTranslationY(scrollY * 0.5f);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mBannerImageView != null) {
            mBannerImageView.onSaveInstanceState(outState);
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
            mMainContent.setVisibility(View.GONE);
            mEmptyView.setText(getString(R.string.getting_information));
            mEmptyView.setVisibility(View.VISIBLE);
            displayProgressDialog(null);
        }

        @Override
        protected GetEventReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            simpleRequest = new SimpleRequest();
            simpleRequest.requestProperties = rp;

            logD("simpleRequest for getting myEvent is: " + simpleRequest);
            GetEventReply getEventReply;
            try {
                SellServiceGrpc.SellServiceBlockingStub stub = GRPCUtils.getInstance().getSellServiceGrpc();
                getEventReply = stub.getMyEvent(simpleRequest);
                return getEventReply;
            } catch (Exception e) {
                logE("Error getting user info. simpleRequest: " + simpleRequest + ". Error: " + e, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(GetEventReply getEventReply) {
            super.onPostExecute(getEventReply);

            if (BuildConfig.DEBUG) {
                logD("getEventReply is: " + getEventReply);
            }

            dismissProgressDialog();
            if (getEventReply != null) {
                switch (getEventReply.replyProperties.statusCode) {
                    case ReplyProperties.OK:
                        if (getEventReply.event != null) {
                            mEmptyView.setVisibility(View.GONE);
                            mMainContent.setVisibility(View.VISIBLE);
                            setEventInfo(getEventReply.event, true);
                        } else {
                            if (isVisible()) {
                                displayShortToast(getString(R.string.error_getting_event_info));
                                displaySnackbar();
                                logE("Got OK code for getting user (id " + HonarnamaUser.getId() + ") event, but event was null. simpleRequest: " + simpleRequest);
                            }
                        }
                        break;

                    case ReplyProperties.CLIENT_ERROR:
                        switch (getEventReply.errorCode) {
                            case GetEventReply.NO_CLIENT_ERROR:
                                logE("Got NO_CLIENT_ERROR code for getting user (id " + HonarnamaUser.getId() + ") event. simpleRequest: " + simpleRequest);
                                displayShortToast(getString(R.string.error_occured));
                                break;

                            case GetEventReply.EVENT_NOT_FOUND:
                                mEmptyView.setVisibility(View.GONE);
                                mMainContent.setVisibility(View.VISIBLE);
                                mIsNew = true;
                                logD("Event not found.");
                                break;
                        }
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        if (isVisible()) {
                            displaySnackbar();
                            displayLongToast(getString(R.string.server_error_try_again));
                        }
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        HonarnamaUser.logout(mActivity);
                        break;

                    case ReplyProperties.UPGRADE_REQUIRED:
                        ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) mActivity);
                        if (controlPanelActivity != null) {
                            controlPanelActivity.displayUpgradeRequiredDialog();
                        }
                        break;
                }

            } else {
                if (isVisible()) {
                    mEmptyView.setText(getString(R.string.error_getting_event_info));
                    displaySnackbar();
                    displayShortToast(getString(R.string.check_net_connection));
                }
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
        }

        @Override
        protected CreateOrUpdateEventReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();

            createOrUpdateEventRequest = new CreateOrUpdateEventRequest();
            createOrUpdateEventRequest.event = new net.honarnama.nano.Event();
            createOrUpdateEventRequest.event.name = mNameEditText.getText().toString().trim();
            createOrUpdateEventRequest.event.description = mDescriptionEditText.getText().toString().trim();
            createOrUpdateEventRequest.event.address = mAddressEditText.getText().toString().trim();
            createOrUpdateEventRequest.event.phoneNumber = mPhoneNumberEditText.getText().toString().trim();
            createOrUpdateEventRequest.event.cellNumber = mCellNumberEditText.getText().toString().trim();
            createOrUpdateEventRequest.event.eventCategoryId = mSelectedCatId;
            createOrUpdateEventRequest.event.active = mActiveBtn.isChecked();
            createOrUpdateEventRequest.event.startAt = mStartDate.getTime() / 1000;
            createOrUpdateEventRequest.event.endAt = mEndDate.getTime() / 1000;
            createOrUpdateEventRequest.event.locationId = new LocationId();
            createOrUpdateEventRequest.event.locationId.provinceId = mSelectedProvinceId;
            createOrUpdateEventRequest.event.locationId.cityId = mSelectedCityId;
            createOrUpdateEventRequest.requestProperties = rp;

            if (mBannerImageView.isDeleted()) {
                createOrUpdateEventRequest.changingBanner = HonarnamaProto.DELETE;
            } else if (mBannerImageView.isChanged() && mBannerImageView.getFinalImageUri() != null) {
                createOrUpdateEventRequest.changingBanner = HonarnamaProto.PUT;
            } else {
                createOrUpdateEventRequest.changingBanner = HonarnamaProto.NOOP;
            }

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

            logD("createOrUpdateEventReply is: " + createOrUpdateEventReply);
            if (createOrUpdateEventReply != null) {
                switch (createOrUpdateEventReply.replyProperties.statusCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:
                        dismissProgressDialog();
                        ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) mActivity);
                        if (controlPanelActivity != null) {
                            controlPanelActivity.displayUpgradeRequiredDialog();
                        }
                        break;
                    case ReplyProperties.CLIENT_ERROR:
                        switch (createOrUpdateEventReply.errorCode) {
                            case CreateOrUpdateEventReply.NO_CLIENT_ERROR:
                                logE("Got NO_CLIENT_ERROR code for createOrUpdateEventReply. createOrUpdateEventRequest: " + createOrUpdateEventRequest + ". User id: " + HonarnamaUser.getId());
                                cToastMsg = getString(R.string.error_occured);
                                break;
                            case CreateOrUpdateEventReply.EVENT_NOT_FOUND:
//                                displayShortToast(getString(R.string.event_not_found));
                                cToastMsg = getString(R.string.event_not_found);
                                break;
                            case CreateOrUpdateEventReply.EMPTY_EVENT:
                                logE("createOrUpdateEventReply was EMPTY_EVENT. createOrUpdateEventRequest: " + createOrUpdateEventRequest);
//                                displayShortToast(getString(R.string.error_occured));
                                cToastMsg = getString(R.string.error_occured);
                                break;
                            case CreateOrUpdateEventReply.STORE_NOT_CREATED:
//                                displayLongToast(getString(R.string.store_not_created));
                                cToastMsg = getString(R.string.store_not_created);
                                break;
                            case CreateOrUpdateEventReply.ALREADY_HAS_EVENT:
                                cToastMsg = "در حال حاضر رویداد دیگری دارید.";
                                break;
                        }
                        dismissProgressDialog();
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        cToastMsg = getString(R.string.server_error_try_again);
//                        displayShortToast(getString(R.string.server_error_try_again));
                        dismissProgressDialog();
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        dismissProgressDialog();
                        HonarnamaUser.logout(mActivity);
                        break;

                    case ReplyProperties.OK:
                        setEventInfo(createOrUpdateEventReply.uptodateEvent, false);

                        if (mBannerImageView.isDeleted()) {
                            mBannerImageView.setDeleted(false);
                        }

                        if (!TextUtils.isEmpty(createOrUpdateEventReply.bannerModificationUrl) && mBannerImageView.getFinalImageUri() != null) {
                            final File bannerImageFile = new File(mBannerImageView.getFinalImageUri().getPath());
                            final Uploader aws = new Uploader(bannerImageFile, createOrUpdateEventReply.bannerModificationUrl);
                            aws.upload().continueWith(new Continuation<Void, Object>() {
                                @Override
                                public Object then(Task<Void> task) throws Exception {
                                    if (BuildConfig.DEBUG) {
                                        logD("Continue event image upload task.");
                                    }

                                    if (task.isFaulted()) {
                                        if (BuildConfig.DEBUG) {
                                            logD("Uploading event image failed.");
                                        }
                                        cToastMsg = HonarnamaBaseApp.getInstance().getString(R.string.error_uploading_event_banner) + HonarnamaBaseApp.getInstance().getString(R.string.check_net_connection);
                                    } else {
                                        mBannerImageView.setChanged(false);
                                        if (BuildConfig.DEBUG) {
                                            logD("Uploading event image done.");
                                        }
                                        cToastMsg = HonarnamaBaseApp.getInstance().getString(R.string.successfully_changed_event_info);
                                    }

                                    dismissProgressDialog();
                                    return null;
                                }
                            });
                        } else {
                            cToastMsg = HonarnamaBaseApp.getInstance().getString(R.string.successfully_changed_event_info);
                            dismissProgressDialog();
//                            displayLongToast(getString(R.string.successfully_changed_event_info));
                        }
                        break;
                }

            } else {
                cToastMsg = HonarnamaBaseApp.getInstance().getString(R.string.error_connecting_to_Server) + HonarnamaBaseApp.getInstance().getString(R.string.check_net_connection);
                dismissProgressDialog();
            }
        }

    }
//
//    private void dismissProgressDialog() {
//        Activity activity = mActivity;
//        if (activity != null && !activity.isFinishing()) {
//            if (mProgressDialog != null && mProgressDialog.isShowing()) {
//                mProgressDialog.dismiss();
//            }
//        }
//
//    }
//
//    private void displayProgressDialog(DialogInterface.OnDismissListener onDismissListener) {
//        if (mProgressDialog == null) {
//            mProgressDialog = new ProgressDialog(mActivity);
//            mProgressDialog.setCancelable(false);
//            mProgressDialog.setMessage(getString(R.string.please_wait));
//        }
//
//        if (onDismissListener != null) {
//            mProgressDialog.setOnDismissListener(onDismissListener);
//        }
//
//        Activity activity = mActivity;
//        if (activity != null && !activity.isFinishing() && isVisible()) {
//            mProgressDialog.show();
//        }
//    }


    public void displaySnackbar() {
        if (mSnackbar != null && mSnackbar.isShown()) {
            mSnackbar.dismiss();
        }

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(" ").append(getString(R.string.error_connecting_to_Server)).append(" ");

        mSnackbar = Snackbar.make(mCoordinatorLayout, builder, Snackbar.LENGTH_INDEFINITE);
        View sbView = mSnackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setBackgroundColor(getResources().getColor(R.color.amber));
        textView.setSingleLine(false);
        textView.setGravity(Gravity.CENTER);
        Spannable spannable = (Spannable) textView.getText();
        spannable.setSpan(new ImageSpan(mActivity, android.R.drawable.stat_notify_sync), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        sbView.setBackgroundColor(getResources().getColor(R.color.amber));

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkManager.getInstance().isNetworkEnabled(true)) {
                    new getEventAsync().execute();
                    if (mSnackbar != null && mSnackbar.isShown()) {
                        mSnackbar.dismiss();
                    }
                }
            }
        });

        mSnackbar.show();
    }

}
