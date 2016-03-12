package net.honarnama.sell.fragments;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ImageSelector;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.adapter.CityAdapter;
import net.honarnama.core.adapter.EventCategoriesAdapter;
import net.honarnama.core.adapter.ProvincesAdapter;
import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.core.model.City;
import net.honarnama.core.model.Event;
import net.honarnama.core.model.EventCategory;
import net.honarnama.core.model.Provinces;
import net.honarnama.core.utils.GenericGravityTextWatcher;
import net.honarnama.core.utils.HonarnamaUser;
import net.honarnama.core.utils.JalaliCalendar;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.core.utils.ObservableScrollView;
import net.honarnama.core.utils.ParseIO;
import net.honarnama.core.utils.TextUtil;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;
import net.honarnama.sell.activity.ControlPanelActivity;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

public class EventManagerFragment extends HonarnamaBaseFragment implements View.OnClickListener, ObservableScrollView.OnScrollChangedListener {

    private EditText mNameEditText;
    private EditText mAddressEditText;
    private EditText mDescriptionEditText;
    private EditText mPhoneNumberEditText;
    private EditText mCellNumberEditText;
    private Button mRegisterEventButton;
    private ImageSelector mBannerImageView;

    private RadioButton mActive;
    private RadioButton mPassive;

    private ObservableScrollView mScrollView;

    private View mBannerFrameLayout;

    private Button mEventCatBtn;
    private TextView mEventCatLabel;
    public TreeMap<Number, EventCategory> mEventCategoryObjectsTreeMap = new TreeMap<Number, EventCategory>();
    public HashMap<String, String> mEventCategoriesHashMap = new HashMap<>();

    private EditText mProvinceEditText;
    public TreeMap<Number, Provinces> mProvincesObjectsTreeMap = new TreeMap<>();
    public HashMap<String, String> mProvincesHashMap = new HashMap<>();

    private EditText mCityEditEext;
    public TreeMap<Number, HashMap<String, String>> mCityOrderedTreeMap = new TreeMap<>();
    public HashMap<String, String> mCityHashMap = new HashMap<>();


    private Spinner mStartDaySpinner, mStartMonthSpinner, mStartYearSpinner;
    private Spinner mEndDaySpinner, mEndMonthSpinner, mEndYearSpinner;
    public Date mStartDate;
    public Date mEndDate;

    public TextView mStartLabelTextView;
    public TextView mEndLabelTextView;

    ProgressDialog mSendingDataProgressDialog;
    ParseFile mParseFileBanner;

    public String mSelectedProvinceId;
    public String mSelectedProvinceName;

    public String mSelectedCityId;
    public String mSelectedCityName;


    public String mSelectedCatId;
    public String mSelectedCatName;


    public ProgressBar mBannerProgressBar;

    public TextView mStatusBarTextView;
    public RelativeLayout mEventNotVerifiedNotif;

    public static EventManagerFragment mEventManagerFragment;

    private Tracker mTracker;

    public boolean mIsNew = true;

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.nav_title_event_manager);
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

        final SharedPreferences sharedPref = HonarnamaBaseApp.getInstance().getSharedPreferences(HonarnamaUser.getCurrentUser().getUsername(), Context.MODE_PRIVATE);
        if (!sharedPref.getBoolean(HonarnamaSellApp.PREF_LOCAL_DATA_STORE_FOR_EVENT_SYNCED, false)) {

            if (!NetworkManager.getInstance().isNetworkEnabled(true) || !sharedPref.getBoolean(HonarnamaSellApp.PREF_LOCAL_DATA_STORE_FOR_EVENT_SYNCED, false)) {

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_SYNCED, false);
                editor.commit();

                Intent intent = new Intent(getActivity(), ControlPanelActivity.class);
                getActivity().finish();
                startActivity(intent);
            }

        }

        View rootView = inflater.inflate(R.layout.fragment_event_manager, container, false);
        // Inflate the layout for this fragment

        mBannerProgressBar = (ProgressBar) rootView.findViewById(R.id.banner_progress_bar);

        mSendingDataProgressDialog = new ProgressDialog(getActivity());

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

        ArrayAdapter<String> daysAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.days));
        mStartDaySpinner.setAdapter(daysAdapter);
        mEndDaySpinner.setAdapter(daysAdapter);

        ArrayAdapter<String> monthsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.months));
        mStartMonthSpinner.setAdapter(monthsAdapter);
        mEndMonthSpinner.setAdapter(monthsAdapter);

        ArrayAdapter<String> yearsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.years));
        mStartYearSpinner.setAdapter(yearsAdapter);
        mEndYearSpinner.setAdapter(yearsAdapter);


        mActive = (RadioButton) rootView.findViewById(R.id.active_event);
        mPassive = (RadioButton) rootView.findViewById(R.id.passive_event);

        mActive.setOnClickListener(this);
        mPassive.setOnClickListener(this);


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

        mCityEditEext = (EditText) rootView.findViewById(R.id.event_city_edit_text);
        mCityEditEext.setOnClickListener(this);
        mCityEditEext.setKeyListener(null);


        mRegisterEventButton = (Button) rootView.findViewById(R.id.register_event_button);
        mBannerImageView = (ImageSelector) rootView.findViewById(R.id.event_banner_image_view);
        mRegisterEventButton.setOnClickListener(this);

        mBannerImageView.setOnImageSelectedListener(new ImageSelector.OnImageSelectedListener() {
            @Override
            public boolean onImageSelected(Uri selectedImage, boolean cropped) {
                return true;
            }

            @Override
            public void onImageRemoved() {
            }

            @Override
            public void onImageSelectionFailed() {
            }
        });
        mBannerImageView.setActivity(this.getActivity());
        mBannerImageView.restore(savedInstanceState);

        setEventInfo();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPhoneNumberEditText.addTextChangedListener(new GenericGravityTextWatcher(mPhoneNumberEditText));
        mCellNumberEditText.addTextChangedListener(new GenericGravityTextWatcher(mCellNumberEditText));
    }

    public void resetFields() {
        if (mNameEditText != null) {
            mNameEditText.setText("");
            mDescriptionEditText.setText("");
            mPhoneNumberEditText.setText("");
            mCellNumberEditText.setText("");
            mAddressEditText.setText("");

            mActive.setChecked(true);
            mPassive.setChecked(false);

            mSelectedProvinceId = Provinces.DEFAULT_PROVINCE_ID;
            mSelectedCityId = City.DEFAULT_CITY_ID;
            mSelectedCatId = null;

            mEventCatBtn.setText(getString(R.string.select));

            mProvinceEditText.setText(Provinces.DEFAULT_PROVINCE_NAME);
            mCityEditEext.setText(City.DEFAULT_CITY_NAME);

            mNameEditText.setError(null);
            mAddressEditText.setError(null);
            mDescriptionEditText.setError(null);
            mPhoneNumberEditText.setError(null);
            mCellNumberEditText.setError(null);
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //TODO remove if not needed
//        (ControlPanelActivity)activity).onSectionAttached(1);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_event_button:
                if (AreFormInputsValid()) {
                    if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                        return;
                    }
                    mSendingDataProgressDialog.setCancelable(false);
                    mSendingDataProgressDialog.setMessage(getString(R.string.sending_data));
                    mSendingDataProgressDialog.show();
                    uploadEventBanner();
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

        }
    }

    private void displayProvinceDialog() {

        ListView provincesListView;
        ProvincesAdapter provincesAdapter;

        final Dialog provinceDialog = new Dialog(getActivity(), R.style.DialogStyle);

        provinceDialog.setContentView(R.layout.choose_province);

        provincesListView = (ListView) provinceDialog.findViewById(net.honarnama.base.R.id.provinces_list_view);
        provincesAdapter = new ProvincesAdapter(getActivity(), mProvincesObjectsTreeMap);
        provincesListView.setAdapter(provincesAdapter);
        provincesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Provinces selectedProvince = mProvincesObjectsTreeMap.get(position + 1);
                mSelectedProvinceId = selectedProvince.getObjectId();
                mSelectedProvinceName = selectedProvince.getName();
                mProvinceEditText.setText(mSelectedProvinceName);
                rePopulateCityList();
                provinceDialog.dismiss();
            }
        });
        provinceDialog.setCancelable(true);
        provinceDialog.setTitle(getString(R.string.select_province));
        provinceDialog.show();
    }

    private void rePopulateCityList() {
        City city = new City();
        city.getOrderedCities(getActivity(), mSelectedProvinceId).continueWith(new Continuation<TreeMap<Number, HashMap<String, String>>, Object>() {
            @Override
            public Object then(Task<TreeMap<Number, HashMap<String, String>>> task) throws Exception {
                if (task.isFaulted()) {
                    if (isVisible()) {
                        Toast.makeText(getActivity(), getString(R.string.error_getting_city_list) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                    }
                } else {
                    mCityOrderedTreeMap = task.getResult();
                    for (HashMap<String, String> cityMap : mCityOrderedTreeMap.values()) {
                        for (Map.Entry<String, String> citySet : cityMap.entrySet()) {
                            mCityHashMap.put(citySet.getKey(), citySet.getValue());

                        }
                    }

                    Set<String> tempSet = mCityOrderedTreeMap.get(1).keySet();
                    for (String key : tempSet) {
                        mSelectedCityId = key;
                        mCityEditEext.setText(mCityHashMap.get(key));
                    }
                }
                return null;
            }
        });
    }

    private void displayCityDialog() {
        ListView cityListView;
        CityAdapter cityAdapter;

        final Dialog cityDialog = new Dialog(getActivity(), R.style.DialogStyle);
        cityDialog.setContentView(R.layout.choose_city);
        cityListView = (ListView) cityDialog.findViewById(net.honarnama.base.R.id.city_list_view);

        cityAdapter = new CityAdapter(getActivity(), mCityOrderedTreeMap);
        cityListView.setAdapter(cityAdapter);
        cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> selectedCity = mCityOrderedTreeMap.get(position + 1);
                for (String key : selectedCity.keySet()) {
                    mSelectedCityId = key;
                }
                for (String value : selectedCity.values()) {
                    mSelectedCityName = value;
                    mCityEditEext.setText(mSelectedCityName);
                }
                cityDialog.dismiss();
            }
        });
        cityDialog.setCancelable(true);
        cityDialog.setTitle(getString(R.string.select_city));
        cityDialog.show();
    }


    private void displayChooseEventCategoryDialog() {

        ListView eventCatsListView;
        EventCategoriesAdapter eventCatsAdapter;

        final Dialog eventCatDialog = new Dialog(getActivity(), R.style.DialogStyle);

        eventCatDialog.setContentView(R.layout.choose_event_category);

        eventCatsListView = (ListView) eventCatDialog.findViewById(net.honarnama.base.R.id.event_category_list_view);
        eventCatsAdapter = new EventCategoriesAdapter(getActivity(), mEventCategoryObjectsTreeMap);
        eventCatsListView.setAdapter(eventCatsAdapter);
        eventCatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EventCategory eventCategory = mEventCategoryObjectsTreeMap.get(position + 1);
                mSelectedCatId = eventCategory.getObjectId();
                mSelectedCatName = eventCategory.getName();
                mEventCatBtn.setText(mSelectedCatName);
                eventCatDialog.dismiss();
            }
        });
        eventCatDialog.setCancelable(true);
        eventCatDialog.setTitle(getString(R.string.select_event_cat));
        eventCatDialog.show();
    }

    private boolean AreFormInputsValid() {
        if (mNameEditText.getText().toString().trim().length() == 0) {
            mNameEditText.requestFocus();
            mNameEditText.setError(getString(R.string.error_event_name_cant_be_empty));
            return false;
        } else {
            mNameEditText.setError(null);
        }

        if (mSelectedCatId == null) {
            mEventCatLabel.requestFocus();
            mEventCatLabel.setError(getString(R.string.error_event_cat_is_not_selected));
            if (isVisible()) {
                Toast.makeText(getActivity(), getString(R.string.error_event_cat_is_not_selected), Toast.LENGTH_SHORT).show();
            }
            return false;
        } else {
            mEventCatLabel.setError(null);
        }


        if (mSelectedProvinceId == null) {
            mProvinceEditText.requestFocus();
            mProvinceEditText.setError(getString(R.string.error_event_province_not_set));
            return false;
        } else {
            mProvinceEditText.setError(null);
        }

        if (mSelectedCityId == null) {
            mCityEditEext.requestFocus();
            mCityEditEext.setError(getString(R.string.error_event_city_not_set));
            return false;
        } else {
            mCityEditEext.setError(null);
        }

        if (mAddressEditText.getText().toString().trim().length() == 0) {
            mAddressEditText.requestFocus();
            mAddressEditText.setError(getString(R.string.error_event_address_is_not_specified));
            return false;
        } else {
            mAddressEditText.setError(null);
        }


        if (mDescriptionEditText.getText().toString().trim().length() == 0) {
            mDescriptionEditText.requestFocus();
            mDescriptionEditText.setError(getString(R.string.error_event_desc_cant_be_empty));
            return false;
        } else {
            mDescriptionEditText.setError(null);
        }

        if (mCellNumberEditText.getText().toString().trim().length() == 0 && mPhoneNumberEditText.getText().toString().trim().length() == 0) {
            mCellNumberEditText.requestFocus();
            mCellNumberEditText.setError(getString(R.string.fill_at_least_one_communication_ways));
            return false;
        } else {
            mCellNumberEditText.setError(null);
        }


        if (mCellNumberEditText.getText().toString().trim().length() > 0) {
            String mobileNumberPattern = "^09\\d{9}$";
            if (!mCellNumberEditText.getText().toString().trim().matches(mobileNumberPattern)) {
                mCellNumberEditText.requestFocus();
                mCellNumberEditText.setError(getString(net.honarnama.base.R.string.error_mobile_number_is_not_valid));
                return false;
            } else {
                mCellNumberEditText.setError(null);
            }
        }

        if (mPhoneNumberEditText.getText().toString().trim().length() > 0) {
            String phoneNumberPattern = "^(0[0-9]{2,3}-?)?[0-9]{6,14}$";
            if (!mPhoneNumberEditText.getText().toString().trim().matches(phoneNumberPattern)) {
                mPhoneNumberEditText.requestFocus();
                mPhoneNumberEditText.setError(getString(R.string.error_phone_number_is_not_valid));
                return false;
            } else {
                mPhoneNumberEditText.setError(null);
            }
        }

        String fromYearValue = mStartYearSpinner.getItemAtPosition(mStartYearSpinner.getSelectedItemPosition()).toString();
        String fromMonthValue = (mStartMonthSpinner.getSelectedItemPosition() + 1) + "";
        String fromDayValue = (mStartDaySpinner.getSelectedItemPosition() + 1) + "";

        String userEnteredStartDate = fromYearValue + "/" + fromMonthValue + "/" + fromDayValue;
        mStartDate = JalaliCalendar.getGregorianDate(userEnteredStartDate);
        String checkJalaliDate = JalaliCalendar.getJalaliDate(mStartDate);
        if (!checkJalaliDate.equals(userEnteredStartDate)) {
            mStartDaySpinner.requestFocus();
            mStartLabelTextView.setError("تاریخ شروع اشتباه است.");
            if (isVisible()) {
                Toast.makeText(getActivity(), "تاریخ شروع اشتباه است.", Toast.LENGTH_LONG).show();
            }
            return false;
        } else {
            mStartLabelTextView.setError(null);
        }


        String toYearValue = mEndYearSpinner.getItemAtPosition(mEndYearSpinner.getSelectedItemPosition()).toString();
        String toMonthValue = (mEndMonthSpinner.getSelectedItemPosition() + 1) + "";
        String toDayValue = (mEndDaySpinner.getSelectedItemPosition() + 1) + "";

        String userEnteredEndDate = toYearValue + "/" + toMonthValue + "/" + toDayValue;
        mEndDate = JalaliCalendar.getGregorianDate(userEnteredEndDate);
        checkJalaliDate = JalaliCalendar.getJalaliDate(mEndDate);
        if (!checkJalaliDate.equals(userEnteredEndDate)) {
            mEndDaySpinner.requestFocus();
            mEndLabelTextView.setError("تاریخ پایان اشتباه است.");
            if (isVisible()) {
                Toast.makeText(getActivity(), "تاریخ پایان اشتباه است.", Toast.LENGTH_LONG).show();
            }
            return false;
        } else {
            mEndLabelTextView.setError(null);
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mBannerImageView.onActivityResult(requestCode, resultCode, intent);
    }

    public void uploadEventBanner() {

        if (!NetworkManager.getInstance().isNetworkEnabled(false)) {
            mSendingDataProgressDialog.dismiss();
            if (isVisible()) {
                Toast.makeText(getActivity(), getString(R.string.error_uploading_banner) + getString(R.string.please_check_internet_connection), Toast.LENGTH_SHORT).show();
            }
            return;
        }


        if (!mBannerImageView.isChanged() || mBannerImageView.getFinalImageUri() == null) {
            saveEvent();
            return;
        }
        final File storeBannerImageFile = new File(mBannerImageView.getFinalImageUri().getPath());
        try {
            mParseFileBanner = ParseIO.getParseFileFromFile(HonarnamaSellApp.STORE_BANNER_FILE_NAME,
                    storeBannerImageFile);
            mParseFileBanner.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        saveEvent();
//                        try {
//                            ParseIO.copyFile(storeBannerImageFile, new File(HonarnamaBaseApp.APP_IMAGES_FOLDER, HonarnamaSellApp.STORE_BANNER_FILE_NAME));
//                        } catch (IOException e1) {
//                            if (BuildConfig.DEBUG) {
//                                Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
//                                        "Error copying store banner to sd card " + e1, e1);
//                            } else {
//                                Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Error copying store banner to sd card"
//                                        + e1.getMessage());
//                            }
//                        }
                    } else {
                        mSendingDataProgressDialog.dismiss();
                        if (isVisible()) {
                            Toast.makeText(getActivity(), getString(R.string.error_uploading_banner) + getString(R.string.please_try_again), Toast.LENGTH_LONG).show();
                        }
                        logE("Uploading event Banner Failed. Code: " + e.getCode()
                                + "// Msg: " + e.getMessage() + " // Error: " + e, "", e);
                    }
                }
            });
        } catch (IOException ioe) {
            mSendingDataProgressDialog.dismiss();
            if (isVisible()) {
                Toast.makeText(getActivity(), getString(R.string.error_uploading_banner) + getString(R.string.please_try_again), Toast.LENGTH_LONG).show();
            }
            logE("Failed on preparing event banner image. ioe=" + ioe.getMessage() + " // Error: " + ioe, "", ioe);
        }
    }


    private void saveEvent() {

        if (!NetworkManager.getInstance().isNetworkEnabled(false)) {
            mSendingDataProgressDialog.dismiss();
            if (isVisible()) {
                Toast.makeText(getActivity(), getString(R.string.error_updating_event_info) + getString(R.string.please_check_internet_connection), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        ParseQuery<Event> query = ParseQuery.getQuery(Event.class);
        query.whereEqualTo(Event.OWNER, HonarnamaUser.getCurrentUser());
        query.getFirstInBackground(new GetCallback<Event>() {
            @Override
            public void done(final Event event, ParseException e) {
                final Event eventObject;
                if (e == null) {
                    eventObject = event;
                    mIsNew = false;
                } else {
                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        eventObject = new Event();
                        eventObject.setOwner(HonarnamaUser.getCurrentUser());
                        mIsNew = true;
                    } else {
                        mSendingDataProgressDialog.dismiss();
                        if (isVisible()) {
                            Toast.makeText(getActivity(), getString(R.string.error_updating_event_info) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                        }
                        logE("Error changing event Info. Code: " + e.getCode() + " //  Msg: " + e.getMessage() + " // Error: " + e, "", e);
                        return;
                    }
                }

                Provinces province = ParseObject.createWithoutData(Provinces.class, mSelectedProvinceId);
                City city = ParseObject.createWithoutData(City.class, mSelectedCityId);
                EventCategory eventCategory = ParseObject.createWithoutData(EventCategory.class, mSelectedCatId);

                eventObject.setProvince(province);
                eventObject.setCity(city);
                eventObject.setCategory(eventCategory);


                eventObject.setActive(mActive.isChecked());
                eventObject.setName(mNameEditText.getText().toString().trim());
                eventObject.setAddress(mAddressEditText.getText().toString().trim());
                eventObject.setDescription(mDescriptionEditText.getText().toString().trim());
                eventObject.setPhoneNumber(mPhoneNumberEditText.getText().toString().trim());
                eventObject.setCellNumber(mCellNumberEditText.getText().toString().trim());
                eventObject.setStartDate(mStartDate);
                eventObject.setEndtDate(mEndDate);

                if (mBannerImageView.isDeleted()) {
                    eventObject.remove(Event.BANNER);
                } else if (mBannerImageView.isChanged() && mParseFileBanner != null) {
                    eventObject.setBanner(mParseFileBanner);
                }

                eventObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        mSendingDataProgressDialog.dismiss();
                        if (e == null) {
                            if (isVisible()) {
                                Toast.makeText(getActivity(), getString(R.string.successfully_changed_event_info), Toast.LENGTH_LONG).show();
                            }
                            eventObject.pinInBackground();
                        } else {
                            logE("Saving event failed. Code" + e.getCode() + "// Msg: " + e.getMessage() + " // error: " + e, "", e);
                            try {
                                JSONObject error = new JSONObject(e.getMessage());
                                if ((error.has("code")) && error.get("code").toString().equals("3001")) {
                                    if (isVisible()) {
                                        Toast.makeText(getActivity(), getString(R.string.event_name_already_exists), Toast.LENGTH_LONG).show();
                                        mNameEditText.setError(getString(R.string.event_name_already_exists));
                                    }
                                } else if ((error.has("code")) && error.get("code").toString().equals("3002")) {
                                    Toast.makeText(getActivity(), getString(R.string.you_own_another_event_or_u_r_not_the_right_owner), Toast.LENGTH_LONG).show();
                                } else {
                                    if (isVisible()) {
                                        Toast.makeText(getActivity(), getString(R.string.saving_event_info_failed) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                                    }
                                }
                            } catch (JSONException e1) {
                                logE("Saving event failed (JSONException). Code" + e.getCode() + "// Msg: " + e.getMessage() + " // error: " + e, "", e);
                                if (isVisible()) {
                                    Toast.makeText(getActivity(), getString(R.string.saving_event_info_failed) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                    }
                });

            }

        });
    }

    private void setEventInfo() {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();

        final Provinces provinces = new Provinces();
        final City city = new City();
        final EventCategory eventCategory = new EventCategory();

        getUserEventAsync().continueWith(new Continuation<Event, Void>() {
            @Override
            public Void then(Task<Event> task) throws Exception {
                if (task.isFaulted()) {
                    logE("Getting event Task Failed. Msg: " + task.getError().getMessage() + " // Error: " + task.getError(), "", task.getError());
                    progressDialog.dismiss();
                    if (isVisible()) {
                        Toast.makeText(getActivity(), getString(R.string.getting_event_info_failed) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                    }

                    Intent intent = new Intent(getActivity(), ControlPanelActivity.class);
                    getActivity().finish();
                    startActivity(intent);

                } else {
                    Event event = task.getResult();
                    if (event != null) {

                        Date startDate = event.getStartDate();
                        String jalaliStartDate = JalaliCalendar.getJalaliDate(startDate);
                        String[] separatedJalaliStartDate = jalaliStartDate.split("/");
                        String startYear = separatedJalaliStartDate[0];
                        int startMonth = Integer.valueOf(separatedJalaliStartDate[1]);
                        int startDay = Integer.valueOf(separatedJalaliStartDate[2]);
                        mStartDaySpinner.setSelection(startDay - 1);
                        mStartMonthSpinner.setSelection(startMonth - 1);
                        ArrayAdapter<String> yearAdapter = (ArrayAdapter<String>) mStartYearSpinner.getAdapter();
                        mStartYearSpinner.setSelection(yearAdapter.getPosition(startYear));

                        Date endDate = event.getEndDate();
                        String jalaliEndDate = JalaliCalendar.getJalaliDate(endDate);
                        String[] separatedJalaliEndDate = jalaliEndDate.split("/");
                        String endYear = separatedJalaliEndDate[0];
                        int endMonth = Integer.valueOf(separatedJalaliEndDate[1]);
                        int endDay = Integer.valueOf(separatedJalaliEndDate[2]);
                        mEndDaySpinner.setSelection(endDay - 1);
                        mEndMonthSpinner.setSelection(endMonth - 1);
                        yearAdapter = (ArrayAdapter<String>) mEndYearSpinner.getAdapter();
                        mEndYearSpinner.setSelection(yearAdapter.getPosition(endYear));

                        mActive.setChecked(event.getActive());
                        mPassive.setChecked(!event.getActive());

                        mNameEditText.setText(event.getName());
                        mAddressEditText.setText(event.getAddress());
                        mDescriptionEditText.setText(event.getDescription());

                        mPhoneNumberEditText.setText(event.getPhoneNumber());
                        mCellNumberEditText.setText(event.getCellNumber());

                        EventCategory eventCategory = (EventCategory) event.getCategory();
                        Provinces province = (Provinces) event.getProvince();
                        City city = (City) event.getCity();

                        mSelectedCatId = eventCategory.getObjectId();
                        mSelectedProvinceId = event.getProvince().getObjectId();
                        mSelectedCityId = city.getObjectId();

                        if (!TextUtils.isEmpty(eventCategory.getName())) {
                            mEventCatBtn.setText(eventCategory.getName());
                        }

                        if (!TextUtils.isEmpty(city.getName())) {
                            mCityEditEext.setText(city.getName());
                        }

                        if (!TextUtils.isEmpty(province.getName())) {
                            mProvinceEditText.setText(province.getName());
                        }

                        if (event.getStatus() == Event.STATUS_CODE_CONFIRMATION_WAITING) {
                            mStatusBarTextView.setVisibility(View.VISIBLE);
                        }

                        if (event.getStatus() == Event.STATUS_CODE_NOT_VERIFIED) {
                            mEventNotVerifiedNotif.setVisibility(View.VISIBLE);
                            mStatusBarTextView.setVisibility(View.VISIBLE);
                            mStatusBarTextView.setText(getString(R.string.please_apply_requested_modification));
                        }

                        mBannerProgressBar.setVisibility(View.VISIBLE);
                        mBannerImageView.loadInBackground(event.getBanner(), new GetDataCallback() {
                            @Override
                            public void done(byte[] data, ParseException e) {
                                mBannerProgressBar.setVisibility(View.GONE);
                                if (e != null) {
                                    logE("Getting  banner image failed. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e, "", e);
                                    if (progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
                                    if (isVisible()) {
                                        Toast.makeText(getActivity(), getString(R.string.error_displaying_event_banner) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
                    } else {
                        resetFields();
                    }
                }
                return null;
            }
        }).continueWithTask(new Continuation<Void, Task<TreeMap<Number, Provinces>>>() {
            @Override
            public Task<TreeMap<Number, Provinces>> then(Task<Void> task) throws Exception {
                if (mSelectedProvinceId == null) {
                    mSelectedProvinceId = Provinces.DEFAULT_PROVINCE_ID;

                }
                if (mSelectedCityId == null) {
                    mSelectedCityId = City.DEFAULT_CITY_ID;
                }
                return provinces.getOrderedProvinceObjects(HonarnamaBaseApp.getInstance());
            }
        }).continueWith(new Continuation<TreeMap<Number, Provinces>, Object>() {
            @Override
            public Object then(Task<TreeMap<Number, Provinces>> task) throws Exception {
                if (task.isFaulted()) {
                    mProvinceEditText.setText(Provinces.DEFAULT_PROVINCE_NAME);
                    logE("Getting Province Task Failed. Msg: " + task.getError().getMessage() + " // Error: " + task.getError(), "", task.getError());

                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    if (isVisible()) {
                        Toast.makeText(getActivity(), getString(R.string.error_getting_province_list) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                    }
                } else {
                    mProvincesObjectsTreeMap = task.getResult();
                    for (Provinces province : mProvincesObjectsTreeMap.values()) {
                        mProvincesHashMap.put(province.getObjectId(), province.getName());
                    }
                    mProvinceEditText.setText(mProvincesHashMap.get(mSelectedProvinceId));
                }
                return null;
            }
        }).continueWithTask(new Continuation<Object, Task<TreeMap<Number, HashMap<String, String>>>>() {
            @Override
            public Task<TreeMap<Number, HashMap<String, String>>> then(Task<Object> task) throws Exception {
                return city.getOrderedCities(HonarnamaBaseApp.getInstance(), mSelectedProvinceId);
            }
        }).continueWith(new Continuation<TreeMap<Number, HashMap<String, String>>, Object>() {
            @Override
            public Object then(Task<TreeMap<Number, HashMap<String, String>>> task) throws Exception {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                if (task.isFaulted()) {
                    mCityEditEext.setText(City.DEFAULT_CITY_NAME);
                    logE("Getting City List Task Failed. Msg: " + task.getError().getMessage() + "//  Error: " + task.getError(), "", task.getError());
                    if (isVisible()) {
                        Toast.makeText(getActivity(), getString(R.string.error_getting_city_list) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                    }
                } else {
                    mCityOrderedTreeMap = task.getResult();
                    for (HashMap<String, String> cityMap : mCityOrderedTreeMap.values()) {
                        for (Map.Entry<String, String> citySet : cityMap.entrySet()) {
                            mCityHashMap.put(citySet.getKey(), citySet.getValue());
                        }
                    }
                }
                if ((isVisible()) && !NetworkManager.getInstance().isNetworkEnabled(true)) {
                    Toast.makeText(getActivity(), getString(R.string.connec_to_see_updated_notif_message), Toast.LENGTH_LONG).show();
                }
                mCityEditEext.setText(mCityHashMap.get(mSelectedCityId));
                return null;
            }
        }).continueWithTask(new Continuation<Object, Task<TreeMap<Number, EventCategory>>>() {
            @Override
            public Task<TreeMap<Number, EventCategory>> then(Task<Object> task) throws Exception {
                return eventCategory.getOrderedEventCategories();
            }
        }).continueWith(new Continuation<TreeMap<Number, EventCategory>, Object>() {
            @Override
            public Object then(Task<TreeMap<Number, EventCategory>> task) throws Exception {
                if (task.isFaulted()) {
                    logE("Getting Province Task Failed. Msg: " + task.getError().getMessage() + " // Error: " + task.getError(), "", task.getError());

                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    if (isVisible()) {
                        Toast.makeText(getActivity(), getActivity().getString(R.string.error_getting_event_cat_list) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                    }
                } else {
                    mEventCategoryObjectsTreeMap = task.getResult();
                    for (EventCategory category : mEventCategoryObjectsTreeMap.values()) {
                        mEventCategoriesHashMap.put(category.getObjectId(), category.getName());
                    }
                }
                return null;
            }
        });
    }

    public Task<Event> getUserEventAsync() {
        final TaskCompletionSource<Event> tcs = new TaskCompletionSource<>();
        ParseQuery<Event> query = ParseQuery.getQuery(Event.class);
        query.include(Event.CATEGORY);
        query.include(Event.PROVINCE);
        query.include(Event.CITY);

        query.whereEqualTo(Event.OWNER, HonarnamaUser.getCurrentUser());

//        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences sharedPref = HonarnamaBaseApp.getInstance().getSharedPreferences(HonarnamaUser.getCurrentUser().getUsername(), Context.MODE_PRIVATE);

        if (!NetworkManager.getInstance().isNetworkEnabled(false)) {
            if (sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_STORE_SYNCED, false)) {
                if (BuildConfig.DEBUG) {
                    logD("Getting event info from local datastore");
                }
                query.fromLocalDatastore();
            } else {
                tcs.setError(new NetworkErrorException("No network connection + Offline data not available for event"));
                return tcs.getTask();
            }
        }


        query.getFirstInBackground(new GetCallback<Event>() {
            @Override
            public void done(final Event event, ParseException e) {
                if (e == null) {
                    tcs.trySetResult(event);
//                    if (!sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_STORE_SYNCED, false)) {

                    final List<Event> tempEventList = new ArrayList<Event>() {{
                        add(event);
                    }};

                    ParseObject.unpinAllInBackground(Event.OBJECT_NAME, tempEventList, new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {

                                ParseObject.pinAllInBackground(Event.OBJECT_NAME, tempEventList, new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    SharedPreferences.Editor editor = sharedPref.edit();
                                                    editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_EVENT_SYNCED, true);
                                                    editor.commit();
                                                }
                                            }
                                        }
                                );
                            }
                        }
                    });
//                    }

                } else {
                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        if (BuildConfig.DEBUG) {
                            logD("Getting User Event Result: User does not have any event yet.");
                        }
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_STORE_SYNCED, true);
                        editor.commit();
                        tcs.trySetResult(null);
                    } else {
                        tcs.trySetError(e);
                        logE("Error Getting Event Info. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e, "", e);
                    }
                }

            }
        });
        return tcs.getTask();
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

}
