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
import net.honarnama.base.adapter.ProvincesAdapter;
import net.honarnama.base.fragment.HonarnamaBaseFragment;
import net.honarnama.base.helper.MetaUpdater;
import net.honarnama.base.interfaces.MetaUpdateListener;
import net.honarnama.base.model.City;
import net.honarnama.base.model.Province;
import net.honarnama.base.utils.GravityTextWatcher;
import net.honarnama.base.utils.NetworkManager;
import net.honarnama.base.utils.ObservableScrollView;
import net.honarnama.base.utils.WindowUtil;
import net.honarnama.nano.CreateOrUpdateStoreReply;
import net.honarnama.nano.CreateOrUpdateStoreRequest;
import net.honarnama.nano.GetStoreReply;
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

import android.app.Activity;
import android.app.Dialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import bolts.Continuation;
import bolts.Task;
import io.fabric.sdk.android.services.concurrency.AsyncTask;

public class StoreFragment extends HonarnamaBaseFragment implements View.OnClickListener, ObservableScrollView.OnScrollChangedListener {

    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private EditText mPhoneNumberEditText;
    private EditText mCellNumberEditText;
    private Button mRegisterStoreButton;
    private ImageSelector mLogoImageView;
    private ImageSelector mBannerImageView;
    private ObservableScrollView mScrollView;
    private View mBannerFrameLayout;
    private EditText mProvinceEditText;
    private EditText mCityEditText;
    public ProgressBar mBannerProgressBar;
    public ProgressBar mLogoProgressBar;
    public TextView mStatusBarTextView;
    public RelativeLayout mStoreNotVerifiedNotif;
    Snackbar mSnackbar;
    RelativeLayout mMainContent;
    TextView mEmptyView;

    public TreeMap<Number, Province> mProvinceObjectsTreeMap = new TreeMap<>();
    public HashMap<Integer, String> mProvincesHashMap = new HashMap<>();
    public TreeMap<Number, HashMap<Integer, String>> mCityOrderedTreeMap = new TreeMap<>();
    public HashMap<Integer, String> mCityHashMap = new HashMap<>();

    public int mSelectedProvinceId = -1;
    public String mSelectedProvinceName;

    public int mSelectedCityId = -1;
    public String mSelectedCityName;

    public static StoreFragment mStoreFragment;

    private Tracker mTracker;

    public boolean mIsNew = true;
    private long mStoreId = -1;

    public MetaUpdateListener mMetaUpdateListener;

    private CoordinatorLayout mCoordinatorLayout;

    private boolean mDirty = false;
    TextWatcher mTextWatcherToMarkDirty;

    @Override
    public String getTitle(Context context) {
        return getStringInFragment(R.string.nav_title_store_info);
    }

    private void setDirty(boolean dirty) {
        mDirty = dirty;
    }

    public boolean isDirty() {
        return mDirty;
    }

    public synchronized static StoreFragment getInstance() {
        if (mStoreFragment == null) {
            mStoreFragment = new StoreFragment();
        }
        return mStoreFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mTracker = HonarnamaSellApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("StoreFragment");
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
                if (mValue != editable + "") {
                    mDirty = true;
                }
            }
        };

        View rootView = inflater.inflate(R.layout.fragment_store, container, false);

        mBannerProgressBar = (ProgressBar) rootView.findViewById(R.id.banner_progress_bar);
        mLogoProgressBar = (ProgressBar) rootView.findViewById(R.id.logo_progress_bar);

        mNameEditText = (EditText) rootView.findViewById(R.id.store_name_edit_text);
        mDescriptionEditText = (EditText) rootView.findViewById(R.id.store_description_edit_text);
        mPhoneNumberEditText = (EditText) rootView.findViewById(R.id.store_phone_number);
        mCellNumberEditText = (EditText) rootView.findViewById(R.id.store_cell_number);

        mStatusBarTextView = (TextView) rootView.findViewById(R.id.store_status_bar_text_view);
        mStoreNotVerifiedNotif = (RelativeLayout) rootView.findViewById(R.id.store_not_verified_notif_container);

        mBannerFrameLayout = rootView.findViewById(R.id.store_banner_frame_layout);
        mScrollView = (ObservableScrollView) rootView.findViewById(R.id.store_fragment_scroll_view);
        mScrollView.setOnScrollChangedListener(this);

        mProvinceEditText = (EditText) rootView.findViewById(R.id.store_province_edit_text);
        mProvinceEditText.setOnClickListener(this);
        mProvinceEditText.setKeyListener(null);

        mCityEditText = (EditText) rootView.findViewById(R.id.store_city_edit_text);
        mCityEditText.setOnClickListener(this);
        mCityEditText.setKeyListener(null);

        mRegisterStoreButton = (Button) rootView.findViewById(R.id.register_store_button);

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


        mLogoImageView = (ImageSelector) rootView.findViewById(R.id.store_logo_image_view);
        mBannerImageView = (ImageSelector) rootView.findViewById(R.id.store_banner_image_view);
        mRegisterStoreButton.setOnClickListener(this);

        if (activity != null) {
            mLogoImageView.setOnImageSelectedListener(onImageSelectedListener);
            mLogoImageView.setActivity(activity);
        }
        if (savedInstanceState != null) {
            mLogoImageView.restore(savedInstanceState);
        }

        if (activity != null) {
            mBannerImageView.setOnImageSelectedListener(onImageSelectedListener);
            mBannerImageView.setActivity(activity);
        }
        if (savedInstanceState != null) {
            mBannerImageView.restore(savedInstanceState);
        }
        mMainContent = (RelativeLayout) rootView.findViewById(R.id.main_content);
        mEmptyView = (TextView) rootView.findViewById(R.id.empty_view);

        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinatorLayout);

        loadOfflineData();

        new getStoreAsync().execute();

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
                        displayLongToast(getStringInFragment(R.string.error_occured));
                        break;
                }
            }
        };

        if (activity != null) {
            ((ControlPanelActivity) activity).verifyStoragePermissions(activity);
        }
        resetError();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()) {
            mPhoneNumberEditText.addTextChangedListener(new GravityTextWatcher(mPhoneNumberEditText));
            mCellNumberEditText.addTextChangedListener(new GravityTextWatcher(mCellNumberEditText));
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
            case R.id.register_store_button:
                WindowUtil.hideKeyboard(activity);
                if (formInputsAreValid()) {
                    new CreateOrUpdateStoreAsync().execute();
                }
                break;
            case R.id.store_province_edit_text:
                displayProvinceDialog();
                break;

            case R.id.store_city_edit_text:
                displayCityDialog();
                break;

        }
    }

    private void displayProvinceDialog() {

        ListView provincesListView;
        ProvincesAdapter provincesAdapter;
        Activity activity = getActivity();

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
                        if (mSelectedProvinceId != selectedProvince.getId()) {
                            setDirty(true);
                        }
                        setErrorInFragment(mProvinceEditText, "");
                        mSelectedProvinceId = selectedProvince.getId();
                        mSelectedProvinceName = selectedProvince.getName();
                        setTextInFragment(mProvinceEditText, mSelectedProvinceName);
                        rePopulateCityList();
                    }
                    provinceDialog.dismiss();
                }
            });
        }
        provinceDialog.setCancelable(true);
        provinceDialog.setTitle(getStringInFragment(R.string.select_province));
        provinceDialog.show();
    }

    private void rePopulateCityList() {
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
                        Set<Integer> tempSet = mCityOrderedTreeMap.get(1).keySet();
                        for (int key : tempSet) {
                            mSelectedCityId = key;
                            setTextInFragment(mCityEditText, mCityHashMap.get(key));
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
        cityDialog.setCancelable(true);
        cityDialog.setTitle(getStringInFragment(R.string.select_city));
        cityDialog.show();
    }

    public void resetError() {
        setErrorInFragment(mNameEditText, "");
        setErrorInFragment(mProvinceEditText, "");
        setErrorInFragment(mCityEditText, "");
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

        resetError();

        if (getTextInFragment(mNameEditText).length() == 0) {
            requestFocusInFragment(mNameEditText);
            setErrorInFragment(mNameEditText, getStringInFragment(R.string.error_store_name_cant_be_empty));
            displayShortToast(getStringInFragment(R.string.error_store_name_cant_be_empty));
            return false;
        }

        if (mSelectedProvinceId < 0) {
            requestFocusInFragment(mProvinceEditText);
            setErrorInFragment(mProvinceEditText, getStringInFragment(R.string.error_store_province_not_set));
            displayShortToast(getStringInFragment(R.string.error_store_province_not_set));
            return false;
        }

        if (mSelectedCityId < 0) {
            requestFocusInFragment(mCityEditText);
            setErrorInFragment(mCityEditText, getStringInFragment(R.string.error_store_city_not_set));
            displayShortToast(getStringInFragment(R.string.error_store_city_not_set));
            return false;
        }

        if (getTextInFragment(mDescriptionEditText).length() == 0) {
            requestFocusInFragment(mDescriptionEditText);
            setErrorInFragment(mDescriptionEditText, getStringInFragment(R.string.store_desc_cant_be_empty));
            displayShortToast(getStringInFragment(R.string.store_desc_cant_be_empty));
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

        if (!isDirty()) {
            displayLongToast(getStringInFragment(R.string.item_not_changed));
            return false;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (isAdded() && mLogoImageView != null) {
            mLogoImageView.onActivityResult(requestCode, resultCode, intent);
            mBannerImageView.onActivityResult(requestCode, resultCode, intent);
        }
    }

    private void setStoreInfo(net.honarnama.nano.Store store, boolean loadImages) {
        if (!isAdded()) {
            return;
        }
        if (store != null) {
            logD("store info: " + store);
            Activity activity = getActivity();
            mIsNew = false;
            mStoreId = store.id;
            setTextInFragment(mNameEditText, store.name);
            setTextInFragment(mDescriptionEditText, store.description);
            setTextInFragment(mPhoneNumberEditText, store.publicPhoneNumber);
            setTextInFragment(mCellNumberEditText, store.publicCellNumber);

            LocationId storeLocation = store.locationId;
            City city = City.getCityById(storeLocation.cityId);
            Province province = Province.getProvinceById(storeLocation.provinceId);

            if (city != null) {
                setTextInFragment(mCityEditText, city.getName());
                mSelectedCityId = city.getId();
            }

            if (province != null) {
                setTextInFragment(mProvinceEditText, province.getName());
                mSelectedProvinceId = province.getId();
            }

            if (store.reviewStatus == HonarnamaProto.NOT_REVIEWED) {
                setVisibilityInFragment(mStatusBarTextView, View.VISIBLE);
                setTextInFragment(mStatusBarTextView, getStringInFragment(R.string.waiting_to_be_confirmed));
            }

            if (store.reviewStatus == HonarnamaProto.CHANGES_NEEDED) {
                setVisibilityInFragment(mStoreNotVerifiedNotif, View.VISIBLE);
                setTextInFragment(mStatusBarTextView, getStringInFragment(R.string.please_apply_requested_modification));
            }

            if (loadImages && !TextUtils.isEmpty(store.logo) && activity != null && isAdded()) {
                logD("Loading store logo ...");

                setVisibilityInFragment(mLogoProgressBar, View.VISIBLE);
                Picasso.with(activity).load(store.logo)
                        .error(R.drawable.default_logo_hand)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .into(mLogoImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                setVisibilityInFragment(mLogoProgressBar, View.GONE);
                                if (mLogoImageView != null && isAdded()) {
                                    mLogoImageView.setFileSet(true);
                                }
                            }

                            @Override
                            public void onError() {
                                setVisibilityInFragment(mLogoProgressBar, View.GONE);
                                displayShortToast(getStringInFragment(R.string.error_displaying_store_logo) + getStringInFragment(R.string.check_net_connection));
                            }
                        });

            }

            if (loadImages && !TextUtils.isEmpty(store.banner) && activity != null && isAdded()) {
                logD("Loading store banner...");
                setVisibilityInFragment(mBannerProgressBar, View.VISIBLE);
                Picasso.with(activity).load(store.banner)
                        .error(R.drawable.party_flags)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .into(mBannerImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                setVisibilityInFragment(mBannerProgressBar, View.GONE);
                                if (isAdded() && mBannerImageView != null) {
                                    mBannerImageView.setFileSet(true);
                                }
                            }

                            @Override
                            public void onError() {
                                setVisibilityInFragment(mBannerProgressBar, View.GONE);
                                displayShortToast(getStringInFragment(R.string.error_displaying_store_banner) + getStringInFragment(R.string.check_net_connection));
                            }
                        });
            }
        }

        if (isAdded() && mNameEditText != null) {
            mNameEditText.addTextChangedListener(mTextWatcherToMarkDirty);
            mDescriptionEditText.addTextChangedListener(mTextWatcherToMarkDirty);
            mPhoneNumberEditText.addTextChangedListener(mTextWatcherToMarkDirty);
            mCellNumberEditText.addTextChangedListener(mTextWatcherToMarkDirty);
        }
        setDirty(false);
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
        if (isAdded() && mLogoImageView != null) {
            mLogoImageView.onSaveInstanceState(outState);
        }

        if (isAdded() && mBannerImageView != null) {
            mBannerImageView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    public class getStoreAsync extends AsyncTask<Void, Void, GetStoreReply> {
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
        protected GetStoreReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            simpleRequest = new SimpleRequest();

            simpleRequest.requestProperties = rp;

            if (BuildConfig.DEBUG) {
                logD("Get store request is: " + simpleRequest);
            }
            GetStoreReply getStoreReply;
            try {
                SellServiceGrpc.SellServiceBlockingStub stub = GRPCUtils.getInstance().getSellServiceGrpc();
                getStoreReply = stub.getMyStore(simpleRequest);
                return getStoreReply;
            } catch (Exception e) {
                logE("Error getting store info. simpleRequest:" + simpleRequest + ". Error: " + e, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(GetStoreReply getStoreReply) {
            super.onPostExecute(getStoreReply);

            Activity activity = getActivity();
            if (BuildConfig.DEBUG) {
                logD("get store reply is: " + getStoreReply);
            }

            dismissProgressDialog();
            if (getStoreReply != null) {
                switch (getStoreReply.replyProperties.statusCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:
                        if (activity != null) {
                            ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) activity);
                            controlPanelActivity.displayUpgradeRequiredDialog();
                        } else {
                            displayLongToast(getStringInFragment(R.string.upgrade_to_new_version));
                        }
                        break;
                    case ReplyProperties.CLIENT_ERROR:
                        switch (getStoreReply.errorCode) {
                            case GetStoreReply.STORE_NOT_FOUND:
                                mIsNew = true;
                                setVisibilityInFragment(mEmptyView, View.GONE);
                                setVisibilityInFragment(mMainContent, View.VISIBLE);
                                logD("Store not found.");
                                break;

                            case GetStoreReply.NO_CLIENT_ERROR:
                                logE("Got NO_CLIENT_ERROR code for getting user (id " + HonarnamaUser.getId() + ") store. simpleRequest: " + simpleRequest);
                                displayShortToast(getStringInFragment(R.string.error_occured));
                                break;
                        }
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        setTextInFragment(mEmptyView, getStringInFragment(R.string.error_getting_store_info));
                        displayRetrySnackbar();
                        displayShortToast(getStringInFragment(R.string.server_error_try_again));
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        HonarnamaUser.logout(activity);
                        displayLongToast(getStringInFragment(R.string.login_again));
                        break;

                    case ReplyProperties.OK:
                        if (getStoreReply.store != null) {
                            setVisibilityInFragment(mEmptyView, View.GONE);
                            setVisibilityInFragment(mMainContent, View.VISIBLE);
                            setStoreInfo(getStoreReply.store, true);
                        } else {
                            displayShortToast(getStringInFragment(R.string.error_getting_store_info));
                            displayRetrySnackbar();
                            logE("Got OK code for getting user (id " + HonarnamaUser.getId() + ") store, but store was null. simpleRequest: " + simpleRequest);
                        }

                        break;
                }

            } else {
                setTextInFragment(mEmptyView, getStringInFragment(R.string.error_getting_store_info));
                displayRetrySnackbar();
            }
        }
    }


    public class CreateOrUpdateStoreAsync extends AsyncTask<Void, Void, CreateOrUpdateStoreReply> {
        CreateOrUpdateStoreRequest createOrUpdateStoreRequest;
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

            createOrUpdateStoreRequest = new CreateOrUpdateStoreRequest();
            createOrUpdateStoreRequest.store = new net.honarnama.nano.Store();

            createOrUpdateStoreRequest.store.name = getTextInFragment(mNameEditText);
            createOrUpdateStoreRequest.store.description = getTextInFragment(mDescriptionEditText);
            createOrUpdateStoreRequest.store.publicPhoneNumber = getTextInFragment(mPhoneNumberEditText);
            createOrUpdateStoreRequest.store.publicCellNumber = getTextInFragment(mCellNumberEditText);

            if (mLogoImageView != null && mLogoImageView.isDeleted()) {
                createOrUpdateStoreRequest.changingLogo = HonarnamaProto.DELETE;
            } else if (mLogoImageView != null && mLogoImageView.isChanged() && mLogoImageView.getFinalImageUri() != null) {
                createOrUpdateStoreRequest.changingLogo = HonarnamaProto.PUT;
            } else {
                createOrUpdateStoreRequest.changingLogo = HonarnamaProto.NOOP;
            }

            if (mBannerImageView != null && mBannerImageView.isDeleted()) {
                createOrUpdateStoreRequest.changingBanner = HonarnamaProto.DELETE;
            } else if (mBannerImageView != null && mBannerImageView.isChanged() && mBannerImageView.getFinalImageUri() != null) {
                createOrUpdateStoreRequest.changingBanner = HonarnamaProto.PUT;
            } else {
                createOrUpdateStoreRequest.changingBanner = HonarnamaProto.NOOP;
            }

            createOrUpdateStoreRequest.store.locationId = new LocationId();
            createOrUpdateStoreRequest.store.locationId.provinceId = mSelectedProvinceId;
            createOrUpdateStoreRequest.store.locationId.cityId = mSelectedCityId;
        }

        @Override
        protected CreateOrUpdateStoreReply doInBackground(Void... voids) {

            if (TextUtils.isEmpty(createOrUpdateStoreRequest.store.name)) {
                return null;
            }

            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            createOrUpdateStoreRequest.requestProperties = rp;

            CreateOrUpdateStoreReply createOrUpdateStoreReply;
            try {
                SellServiceGrpc.SellServiceBlockingStub stub = GRPCUtils.getInstance().getSellServiceGrpc();

                if (mStoreId > 0) {
                    createOrUpdateStoreRequest.store.id = mStoreId;
                    createOrUpdateStoreReply = stub.updateStore(createOrUpdateStoreRequest);
                } else {
                    createOrUpdateStoreReply = stub.createStore(createOrUpdateStoreRequest);
                }

                return createOrUpdateStoreReply;
            } catch (Exception e) {
                logE("Error running createOrUpdateStoreRequest. createOrUpdateStoreRequest: " + createOrUpdateStoreRequest + ". Error: " + e, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final CreateOrUpdateStoreReply createOrUpdateStoreReply) {
            super.onPostExecute(createOrUpdateStoreReply);

            Activity activity = getActivity();

            if (BuildConfig.DEBUG) {
                logD("createOrUpdateStoreReply is: " + createOrUpdateStoreReply);
            }

            if (createOrUpdateStoreReply != null) {
                switch (createOrUpdateStoreReply.replyProperties.statusCode) {
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
                        switch (createOrUpdateStoreReply.errorCode) {
                            case CreateOrUpdateStoreReply.NO_CLIENT_ERROR:
                                logE("Got NO_CLIENT_ERROR code for createOrUpdate user (id " + HonarnamaUser.getId() + ") store. createOrUpdateStoreRequest: " + createOrUpdateStoreRequest);
                                cToastMsg = getStringInFragment(R.string.error_occured);
                                break;

                            case CreateOrUpdateStoreReply.DUPLICATE_NAME:
                                setErrorInFragment(mNameEditText, getStringInFragment(R.string.store_name_already_exists));
                                cToastMsg = getStringInFragment(R.string.store_name_already_exists);
                                break;

                            case CreateOrUpdateStoreReply.STORE_NOT_FOUND:
                                cToastMsg = getStringInFragment(R.string.store_not_found);
                                break;

                            case CreateOrUpdateStoreReply.EMPTY_STORE:
                                logE("createOrUpdateStoreReply was EMPTY_STORE. createOrUpdateStoreRequest: " + createOrUpdateStoreRequest);
                                cToastMsg = getStringInFragment(R.string.error_occured);
                                break;

                            case CreateOrUpdateStoreReply.ALREADY_HAS_STORE:
                                cToastMsg = "در حال حاضر فروشگاه دیگری دارید.";
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
                        setStoreInfo(createOrUpdateStoreReply.uptodateStore, false);

                        if (mBannerImageView != null && mBannerImageView.isDeleted()) {
                            mBannerImageView.setDeleted(false);
                        }

                        if (mLogoImageView != null && mLogoImageView.isDeleted()) {
                            mLogoImageView.setDeleted(false);
                        }

                        if (TextUtils.isEmpty(createOrUpdateStoreReply.bannerModificationUrl) && TextUtils.isEmpty(createOrUpdateStoreReply.logoModificationUrl)) {
                            cToastMsg = getStringInFragment(R.string.successfully_changed_store_info);
                            dismissProgressDialog();
                        }

                        if (!TextUtils.isEmpty(createOrUpdateStoreReply.bannerModificationUrl) && mBannerImageView != null && mBannerImageView.getFinalImageUri() != null) {
                            final File storeBannerImageFile = new File(mBannerImageView.getFinalImageUri().getPath());
                            final Uploader uploader = new Uploader(storeBannerImageFile, createOrUpdateStoreReply.bannerModificationUrl);

                            if (!TextUtils.isEmpty(createOrUpdateStoreReply.logoModificationUrl) && mLogoImageView != null && mLogoImageView.getFinalImageUri() != null) {
                                uploader.upload().onSuccessTask(new Continuation<Void, Task<Void>>() {
                                    @Override
                                    public Task<Void> then(Task<Void> task) throws Exception {
                                        final File storeLogoImageFile = new File(mLogoImageView.getFinalImageUri().getPath());
                                        final Uploader aws = new Uploader(storeLogoImageFile, createOrUpdateStoreReply.logoModificationUrl);
                                        return aws.upload();
                                    }
                                }).continueWith(new Continuation<Void, Object>() {
                                    @Override
                                    public Object then(Task<Void> task) throws Exception {

                                        if (task.isFaulted()) {
                                            cToastMsg = getStringInFragment(R.string.error_sending_images) + getStringInFragment(R.string.check_net_connection);
                                        } else {
                                            if (mBannerImageView != null) {
                                                mBannerImageView.setChanged(false);
                                            }
                                            if (mLogoImageView != null) {
                                                mLogoImageView.setChanged(false);
                                            }
                                            cToastMsg = getStringInFragment(R.string.successfully_changed_store_info);
                                        }

                                        dismissProgressDialog();
                                        return null;
                                    }
                                });

                            } else {
                                uploader.upload().continueWith(new Continuation<Void, Object>() {
                                    @Override
                                    public Object then(Task<Void> task) throws Exception {

                                        if (task.isFaulted()) {
                                            cToastMsg = getStringInFragment(R.string.error_sending_images) + getStringInFragment(R.string.check_net_connection);
                                        } else {
                                            if (mBannerImageView != null) {
                                                mBannerImageView.setChanged(false);
                                            }
                                            cToastMsg = getStringInFragment(R.string.successfully_changed_store_info);
                                        }
                                        dismissProgressDialog();
                                        return null;
                                    }
                                });
                            }
                        } else if (!TextUtils.isEmpty(createOrUpdateStoreReply.logoModificationUrl) && mLogoImageView != null && mLogoImageView.getFinalImageUri() != null) {
                            final File storeLogoImageFile = new File(mLogoImageView.getFinalImageUri().getPath());
                            final Uploader uploader = new Uploader(storeLogoImageFile, createOrUpdateStoreReply.logoModificationUrl);
                            uploader.upload().continueWith(new Continuation<Void, Object>() {
                                @Override
                                public Object then(Task<Void> task) throws Exception {

                                    if (task.isFaulted()) {
                                        cToastMsg = getStringInFragment(R.string.error_sending_images) + getStringInFragment(R.string.check_net_connection);
                                    } else {
                                        if (mLogoImageView != null) {
                                            mLogoImageView.setChanged(false);
                                        }
                                        cToastMsg = getStringInFragment(R.string.successfully_changed_store_info);
                                    }

                                    dismissProgressDialog();
                                    return null;
                                }
                            });
                        }
                        break;
                }

            } else {
                cToastMsg = getStringInFragment(R.string.error_connecting_server_try_again);
                dismissProgressDialog();
            }
        }
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
        });
    }

    public void displayRetrySnackbar() {

        dismissSnackbar();
        Activity activity = getActivity();

        View sbView = null;
        TextView textView = null;
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(" ").append(getStringInFragment(R.string.error_connecting_server_retry)).append(" ");

        if (!isAdded()) {
            return;
        }

        mSnackbar = Snackbar.make(mCoordinatorLayout, builder, Snackbar.LENGTH_INDEFINITE);
        if (mSnackbar == null || activity == null) {
            return;
        }

        sbView = mSnackbar.getView();
        if (sbView != null) {
            textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            sbView.setBackgroundColor(getResources().getColor(R.color.amber));
        }
        if (textView != null) {
            textView.setBackgroundColor(getResources().getColor(R.color.amber));
            textView.setSingleLine(false);
            textView.setGravity(Gravity.CENTER);
            Spannable spannable = (Spannable) textView.getText();
            if (activity != null) {
                spannable.setSpan(new ImageSpan(activity, android.R.drawable.stat_notify_sync), textView.getText().length()-1, textView.getText().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NetworkManager.getInstance().isNetworkEnabled(true)) {
                        new getStoreAsync().execute();
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

}
