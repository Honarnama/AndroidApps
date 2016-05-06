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
import net.honarnama.core.adapter.CityAdapter;
import net.honarnama.core.adapter.ProvincesAdapter;
import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.core.helper.MetaUpdater;
import net.honarnama.core.interfaces.MetaUpdateListener;
import net.honarnama.core.model.City;
import net.honarnama.core.model.Province;
import net.honarnama.core.utils.GravityTextWatcher;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.core.utils.ObservableScrollView;
import net.honarnama.core.utils.WindowUtil;
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
    public TreeMap<Number, Province> mProvinceObjectsTreeMap = new TreeMap<>();
    public HashMap<Integer, String> mProvincesHashMap = new HashMap<>();

    private EditText mCityEditText;
    public TreeMap<Number, HashMap<Integer, String>> mCityOrderedTreeMap = new TreeMap<>();
    public HashMap<Integer, String> mCityHashMap = new HashMap<>();

    public int mSelectedProvinceId = -1;
    public String mSelectedProvinceName;

    public int mSelectedCityId = -1;
    public String mSelectedCityName;

    public ProgressBar mBannerProgressBar;
    public ProgressBar mLogoProgressBar;

    public TextView mStatusBarTextView;
    public RelativeLayout mStoreNotVerifiedNotif;

    public static StoreFragment mStoreFragment;

    private Tracker mTracker;

    public boolean mIsNew = true;
    private long mStoreId = -1;

    public MetaUpdateListener mMetaUpdateListener;

    private CoordinatorLayout mCoordinatorLayout;

    Snackbar mSnackbar;

    private boolean mDirty = false;
    TextWatcher mTextWatcherToMarkDirty;

    RelativeLayout mMainContent;
    TextView mEmptyView;


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
                dismissProgressDialog();
                switch (replyCode) {
                    case ReplyProperties.OK:
                        loadOfflineData();
                        break;

                    case ReplyProperties.UPGRADE_REQUIRED:
                        if (isAdded() && activity != null) {
                            ((ControlPanelActivity) activity).displayUpgradeRequiredDialog();
                        }
                        break;

                    default:
                        displayLongToast(getStringInFragment(R.string.error_occured) + getStringInFragment(R.string.check_net_connection));
                        break;
                }
            }
        };


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
                if (isAdded() && activity != null) {
                    WindowUtil.hideKeyboard(activity);
                }
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
                    if (mSelectedProvinceId != selectedProvince.getId()) {
                        setDirty(true);
                    }
                    mProvinceEditText.setError(null);
                    mSelectedProvinceId = selectedProvince.getId();
                    mSelectedProvinceName = selectedProvince.getName();
                    mProvinceEditText.setText(mSelectedProvinceName);
                    rePopulateCityList();
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
        mCityEditText.setError(null);
        cityDialog.setCancelable(true);
        cityDialog.setTitle(getStringInFragment(R.string.select_city));
        cityDialog.show();
    }

    private boolean formInputsAreValid() {
        if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
            return false;
        }

        if (!isAdded()) {
            return false;
        }

        mNameEditText.setError(null);
        mProvinceEditText.setError(null);
        mCityEditText.setError(null);
        mDescriptionEditText.setError(null);
        mCellNumberEditText.setError(null);
        mPhoneNumberEditText.setError(null);

        if (mNameEditText.getText().toString().trim().length() == 0) {
            mNameEditText.requestFocus();
            mNameEditText.setError(getStringInFragment(R.string.error_store_name_cant_be_empty));
            return false;
        }

        if (mSelectedProvinceId < 0) {
            mProvinceEditText.requestFocus();
            mProvinceEditText.setError(getStringInFragment(R.string.error_store_province_not_set));
            return false;
        }

        if (mSelectedCityId < 0) {
            mCityEditText.requestFocus();
            mCityEditText.setError(getStringInFragment(R.string.error_store_city_not_set));
            return false;
        }

        if (mDescriptionEditText.getText().toString().trim().length() == 0) {
            mDescriptionEditText.requestFocus();
            mDescriptionEditText.setError(getStringInFragment(R.string.store_desc_cant_be_empty));
            return false;
        }

        if (mCellNumberEditText.getText().toString().trim().length() == 0 && mPhoneNumberEditText.getText().toString().trim().length() == 0) {
            mCellNumberEditText.requestFocus();
            mCellNumberEditText.setError(getStringInFragment(R.string.fill_at_least_one_communication_ways));
            return false;
        }

        if (mCellNumberEditText.getText().toString().trim().length() > 0) {
            String mobileNumberPattern = "^09\\d{9}$";
            if (!mCellNumberEditText.getText().toString().trim().matches(mobileNumberPattern)) {
                mCellNumberEditText.requestFocus();
                mCellNumberEditText.setError(getStringInFragment(net.honarnama.base.R.string.error_mobile_number_is_not_valid));
                return false;
            }
        }

        if (mPhoneNumberEditText.getText().toString().trim().length() > 0) {
            String phoneNumberPattern = "^(0[0-9]{2,3}-?)?[0-9]{6,14}$";
            if (!mPhoneNumberEditText.getText().toString().trim().matches(phoneNumberPattern)) {
                mPhoneNumberEditText.requestFocus();
                mPhoneNumberEditText.setError(getStringInFragment(R.string.error_phone_number_is_not_valid));
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
        if (isAdded()) {
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
            mNameEditText.setText(store.name);
            mDescriptionEditText.setText(store.description);

            mPhoneNumberEditText.setText(store.publicPhoneNumber);
            mCellNumberEditText.setText(store.publicCellNumber);

            LocationId storeLocation = store.locationId;
            City city = City.getCityById(storeLocation.cityId);
            Province province = Province.getProvinceById(storeLocation.provinceId);

            if (city != null) {
                mCityEditText.setText(city.getName());
                mSelectedCityId = city.getId();
            }

            if (province != null) {
                mProvinceEditText.setText(province.getName());
                mSelectedProvinceId = province.getId();
            }

            if (store.reviewStatus == HonarnamaProto.NOT_REVIEWED) {
                mStatusBarTextView.setVisibility(View.VISIBLE);
            }

            if (store.reviewStatus == HonarnamaProto.CHANGES_NEEDED) {
                mStoreNotVerifiedNotif.setVisibility(View.VISIBLE);
                mStatusBarTextView.setVisibility(View.VISIBLE);
                mStatusBarTextView.setText(getStringInFragment(R.string.please_apply_requested_modification));
            }

            if (loadImages && !TextUtils.isEmpty(store.logo) && activity != null) {
                logD("Loading logo ...");

                mLogoProgressBar.setVisibility(View.VISIBLE);
                Picasso.with(activity).load(store.logo)
                        .error(R.drawable.default_logo_hand)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .into(mLogoImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                mLogoProgressBar.setVisibility(View.GONE);
                                mLogoImageView.setFileSet(true);
                            }

                            @Override
                            public void onError() {
                                mLogoProgressBar.setVisibility(View.GONE);
                                displayShortToast(getStringInFragment(R.string.error_displaying_store_logo) + getStringInFragment(R.string.check_net_connection));
                            }
                        });

            }

            if (loadImages && !TextUtils.isEmpty(store.banner) && activity != null) {
                logD("Loading logo ...");
                mBannerProgressBar.setVisibility(View.VISIBLE);

                Picasso.with(activity).load(store.banner)
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
                                displayShortToast(getStringInFragment(R.string.error_displaying_store_banner) + getStringInFragment(R.string.check_net_connection));
                            }
                        });
            }
        }

        mNameEditText.addTextChangedListener(mTextWatcherToMarkDirty);
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
        if (mLogoImageView != null) {
            mLogoImageView.onSaveInstanceState(outState);
        }

        if (mBannerImageView != null) {
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
            if (isAdded()) {
                mMainContent.setVisibility(View.GONE);
                mEmptyView.setText(getStringInFragment(R.string.getting_information));
                mEmptyView.setVisibility(View.VISIBLE);
                displayProgressDialog(null);
            }
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
                logE("Error getting user info. simpleRequest:" + simpleRequest + ". Error: " + e, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(GetStoreReply getStoreReply) {
            super.onPostExecute(getStoreReply);

            Activity activity = getActivity();
            if (BuildConfig.DEBUG) {
                logD("getStoreReply is: " + getStoreReply);
            }

            dismissProgressDialog();
            if (getStoreReply != null) {
                switch (getStoreReply.replyProperties.statusCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:
                        if (activity != null) {
                            ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) activity);
                            if (controlPanelActivity != null) {
                                controlPanelActivity.displayUpgradeRequiredDialog();
                            }
                        }
                        break;
                    case ReplyProperties.CLIENT_ERROR:
                        switch (getStoreReply.errorCode) {
                            case GetStoreReply.STORE_NOT_FOUND:
                                mIsNew = true;
                                if (isAdded()) {
                                    mEmptyView.setVisibility(View.GONE);
                                    mMainContent.setVisibility(View.VISIBLE);
                                }
                                logD("Store not found.");
                                break;

                            case GetStoreReply.NO_CLIENT_ERROR:
                                logE("Got NO_CLIENT_ERROR code for getting user (id " + HonarnamaUser.getId() + ") store. simpleRequest: " + simpleRequest);
                                displayShortToast(getStringInFragment(R.string.error_occured));
                                break;
                        }
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        if (isAdded()) {
                            mEmptyView.setText(getStringInFragment(R.string.error_getting_store_info));
                            displaySnackbar();
                            displayShortToast(getStringInFragment(R.string.server_error_try_again));
                        }
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        HonarnamaUser.logout(activity);
                        if (activity == null) {
                            displayLongToast(getStringInFragment(R.string.login_again));
                        }
                        break;

                    case ReplyProperties.OK:

                        if (getStoreReply.store != null) {
                            if (isAdded()) {
                                mEmptyView.setVisibility(View.GONE);
                                mMainContent.setVisibility(View.VISIBLE);
                            }
                            setStoreInfo(getStoreReply.store, true);
                        } else {
                            if (isAdded()) {
                                displayShortToast(getStringInFragment(R.string.error_getting_store_info));
                                displaySnackbar();
                                logE("Got OK code for getting user (id " + HonarnamaUser.getId() + ") store, but store was null. simpleRequest: " + simpleRequest);
                            }
                        }

                        break;
                }

            } else {
                if (isAdded()) {
                    mEmptyView.setText(getStringInFragment(R.string.error_getting_store_info));
                    displaySnackbar();
                    displayShortToast(getStringInFragment(R.string.check_net_connection));
                }
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

            if (!isAdded()) {
                return;
            }

            createOrUpdateStoreRequest.store.name = mNameEditText.getText().toString().trim();
            createOrUpdateStoreRequest.store.description = mDescriptionEditText.getText().toString().trim();
            createOrUpdateStoreRequest.store.publicPhoneNumber = mPhoneNumberEditText.getText().toString().trim();
            createOrUpdateStoreRequest.store.publicCellNumber = mCellNumberEditText.getText().toString().trim();
        }

        @Override
        protected CreateOrUpdateStoreReply doInBackground(Void... voids) {

            if (TextUtils.isEmpty(createOrUpdateStoreRequest.store.name)) {
                return null;
            }

            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            createOrUpdateStoreRequest.requestProperties = rp;

            createOrUpdateStoreRequest.store.locationId = new LocationId();
            createOrUpdateStoreRequest.store.locationId.provinceId = mSelectedProvinceId;
            createOrUpdateStoreRequest.store.locationId.cityId = mSelectedCityId;

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
                        dismissProgressDialog();
                        if (activity != null) {
                            ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) activity);
                            controlPanelActivity.displayUpgradeRequiredDialog();
                        }
                        break;
                    case ReplyProperties.CLIENT_ERROR:
                        switch (createOrUpdateStoreReply.errorCode) {
                            case CreateOrUpdateStoreReply.NO_CLIENT_ERROR:
                                logE("Got NO_CLIENT_ERROR code for createOrUpdate user (id " + HonarnamaUser.getId() + ") store. createOrUpdateStoreRequest: " + createOrUpdateStoreRequest);
                                cToastMsg = getStringInFragment(R.string.error_occured);
                                break;

                            case CreateOrUpdateStoreReply.DUPLICATE_NAME:
                                mNameEditText.setError(getStringInFragment(R.string.store_name_already_exists));
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
                        if (activity == null) {
                            cToastMsg = getStringInFragment(R.string.login_again);
                        }
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
                cToastMsg = getStringInFragment(R.string.error_connecting_to_Server) + getStringInFragment(R.string.check_net_connection);
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
                    //TODO add isAdded()  in other fragments
                    if (isAdded() && mSelectedProvinceId > 0) {
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
                    if (isAdded() && mSelectedCityId > 0) {
                        mCityEditText.setText(mCityHashMap.get(mSelectedCityId));
                    }
                }
                return null;
            }
        });
    }

    public void displaySnackbar() {
        if (!isAdded()) {
            return;
        }

        if (mSnackbar != null && mSnackbar.isShown()) {
            mSnackbar.dismiss();
        }


        Activity activity = getActivity();

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(" ").append(getStringInFragment(R.string.error_connecting_to_Server)).append(" ");

        mSnackbar = Snackbar.make(mCoordinatorLayout, builder, Snackbar.LENGTH_INDEFINITE);
        View sbView = mSnackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setBackgroundColor(getResources().getColor(R.color.amber));
        textView.setSingleLine(false);
        textView.setGravity(Gravity.CENTER);
        Spannable spannable = (Spannable) textView.getText();
        if (activity != null) {
            spannable.setSpan(new ImageSpan(activity, android.R.drawable.stat_notify_sync), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        sbView.setBackgroundColor(getResources().getColor(R.color.amber));

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkManager.getInstance().isNetworkEnabled(true)) {
                    new getStoreAsync().execute();
                    if (mSnackbar != null && mSnackbar.isShown()) {
                        mSnackbar.dismiss();
                    }
                }
            }
        });

        mSnackbar.show();
    }

}
