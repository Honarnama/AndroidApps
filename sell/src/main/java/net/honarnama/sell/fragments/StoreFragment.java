package net.honarnama.sell.fragments;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.parse.ImageSelector;
import com.parse.ParseFile;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import net.honarnama.GRPCUtils;
import net.honarnama.core.adapter.CityAdapter;
import net.honarnama.core.adapter.ProvincesAdapter;
import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.core.model.City;
import net.honarnama.core.model.Province;
import net.honarnama.core.utils.GenericGravityTextWatcher;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.core.utils.ObservableScrollView;
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
import net.honarnama.sell.utils.AwsUploader;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import android.widget.Toast;

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

    private EditText mProvinceEditEext;
    public TreeMap<Number, Province> mProvinceObjectsTreeMap = new TreeMap<>();
    public HashMap<Integer, String> mProvincesHashMap = new HashMap<>();

    private EditText mCityEditEext;
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

    ProgressDialog mProgressDialog;

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.nav_title_store_info);
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

        if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
            Intent intent = new Intent(getActivity(), ControlPanelActivity.class);
            getActivity().finish();
            startActivity(intent);
        }

        View rootView = inflater.inflate(R.layout.fragment_store, container, false);
        // Inflate the layout for this fragment

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

        mProvinceEditEext = (EditText) rootView.findViewById(R.id.store_province_edit_text);
        mProvinceEditEext.setOnClickListener(this);
        mProvinceEditEext.setKeyListener(null);

        mCityEditEext = (EditText) rootView.findViewById(R.id.store_city_edit_text);
        mCityEditEext.setOnClickListener(this);
        mCityEditEext.setKeyListener(null);


        mRegisterStoreButton = (Button) rootView.findViewById(R.id.register_store_button);
        mLogoImageView = (ImageSelector) rootView.findViewById(R.id.store_logo_image_view);
        mBannerImageView = (ImageSelector) rootView.findViewById(R.id.store_banner_image_view);
        mRegisterStoreButton.setOnClickListener(this);

        mLogoImageView.setOnImageSelectedListener(new ImageSelector.OnImageSelectedListener() {
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
        mLogoImageView.setActivity(this.getActivity());
        mLogoImageView.restore(savedInstanceState);

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

        new getStoreAsync().execute();
        new Province().getAllProvincesSorted(getActivity()).continueWith(new Continuation<TreeMap<Number, Province>, Object>() {
            //TODO if meta is not updated
            @Override
            public Object then(Task<TreeMap<Number, Province>> task) throws Exception {
                if (task.isFaulted()) {
                    //TODO
                } else {
                    mProvinceObjectsTreeMap = task.getResult();
                    for (Province province : mProvinceObjectsTreeMap.values()) {
                        if (mSelectedProvinceId < 0) {
                            mSelectedProvinceId = province.getId();
                            mSelectedProvinceName = province.getName();
                        }
                        mProvincesHashMap.put(province.getId(), province.getName());
                    }
                    mProvinceEditEext.setText(mProvincesHashMap.get(mSelectedProvinceId));
                }
                return null;
            }
        }).continueWithTask(new Continuation<Object, Task<TreeMap<Number, HashMap<Integer, String>>>>() {
            @Override
            public Task<TreeMap<Number, HashMap<Integer, String>>> then(Task<Object> task) throws Exception {
                return new City().getAllCitiesSorted(getActivity(), mSelectedProvinceId);
            }
        }).continueWith(new Continuation<TreeMap<Number, HashMap<Integer, String>>, Object>() {
            @Override
            public Object then(Task<TreeMap<Number, HashMap<Integer, String>>> task) throws Exception {

                if (task.isFaulted()) {
                    logE("Getting City List Task Failed. Msg: " + task.getError().getMessage() + "//  Error: " + task.getError(), "", task.getError());
                    if (isVisible()) {
                        Toast.makeText(getActivity(), getString(R.string.error_getting_city_list) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                    }
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
                    mCityEditEext.setText(mCityHashMap.get(mSelectedCityId));

                }
                if ((isVisible()) && !NetworkManager.getInstance().isNetworkEnabled(true)) {
                    Toast.makeText(getActivity(), getString(R.string.connect_to_see_most_updated_info), Toast.LENGTH_LONG).show();
                }
                return null;
            }
        });

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

            mNameEditText.setError(null);
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
            case R.id.register_store_button:
                if (AreFormInputsValid()) {
                    if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                        return;
                    }
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

        final Dialog provinceDialog = new Dialog(getActivity(), R.style.DialogStyle);

        provinceDialog.setContentView(R.layout.choose_province);

        provincesListView = (ListView) provinceDialog.findViewById(net.honarnama.base.R.id.provinces_list_view);
        provincesAdapter = new ProvincesAdapter(getActivity(), mProvinceObjectsTreeMap);
        provincesListView.setAdapter(provincesAdapter);
        provincesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Province selectedProvince = mProvinceObjectsTreeMap.get(position + 1);
                mSelectedProvinceId = selectedProvince.getId();
                mSelectedProvinceName = selectedProvince.getName();
                mProvinceEditEext.setText(mSelectedProvinceName);
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
        city.getAllCitiesSorted(getActivity(), mSelectedProvinceId).continueWith(new Continuation<TreeMap<Number, HashMap<Integer, String>>, Object>() {
            @Override
            public Object then(Task<TreeMap<Number, HashMap<Integer, String>>> task) throws Exception {
                if (task.isFaulted()) {
                    if (isVisible()) {
                        Toast.makeText(getActivity(), getString(R.string.error_getting_city_list) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                    }
                } else {
                    mCityOrderedTreeMap = task.getResult();
                    for (HashMap<Integer, String> cityMap : mCityOrderedTreeMap.values()) {
                        for (Map.Entry<Integer, String> citySet : cityMap.entrySet()) {
                            mCityHashMap.put(citySet.getKey(), citySet.getValue());

                        }
                    }

                    Set<Integer> tempSet = mCityOrderedTreeMap.get(1).keySet();
                    for (int key : tempSet) {
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
                HashMap<Integer, String> selectedCity = mCityOrderedTreeMap.get(position + 1);
                for (int key : selectedCity.keySet()) {
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

    private boolean AreFormInputsValid() {
        if (mNameEditText.getText().toString().trim().length() == 0) {
            mNameEditText.requestFocus();
            mNameEditText.setError(getString(R.string.error_store_name_cant_be_empty));
            return false;
        }

        if (mSelectedProvinceId < 0) {
            mProvinceEditEext.requestFocus();
            mProvinceEditEext.setError(getString(R.string.error_store_province_not_set));
        }

        if (mSelectedCityId < 0) {
            mCityEditEext.requestFocus();
            mCityEditEext.setError(getString(R.string.error_store_city_not_set));
        }

        if (mDescriptionEditText.getText().toString().trim().length() == 0) {
            mDescriptionEditText.requestFocus();
            mDescriptionEditText.setError(getString(R.string.store_desc_cant_be_empty));
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

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mLogoImageView.onActivityResult(requestCode, resultCode, intent);
        mBannerImageView.onActivityResult(requestCode, resultCode, intent);
    }

    private void saveStore_() {

        if (!NetworkManager.getInstance().isNetworkEnabled(false)) {
            if (isVisible()) {
                Toast.makeText(getActivity(), getString(R.string.error_updating_store_info) + getString(R.string.please_check_internet_connection), Toast.LENGTH_SHORT).show();
            }
            return;
        }

//        ParseQuery<Store> query = ParseQuery.getQuery(Store.class);
//        query.whereEqualTo(Store.OWNER, HonarnamaUser.getCurrentUser());
//        query.getFirstInBackground(
//                new GetCallback<Store>() {
//                    @Override
//                    public void done(final Store store, ParseException e) {
//                        final Store storeObject;
//                        if (e == null) {
//                            storeObject = store;
//                            mIsNew = false;
//                        } else {
//                            if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
//                                storeObject = new Store();
//                                storeObject.setOwner(HonarnamaUser.getCurrentUser());
//                                mIsNew = true;
//                            } else {
//                                mSendingDataProgressDialog.dismiss();
//                                if (isVisible()) {
//                                    Toast.makeText(getActivity(), getString(R.string.error_updating_store_info) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
//                                }
//                                logE("Error changing Store Info. Code: " + e.getCode() + " //  Msg: " + e.getMessage() + " // Error: " + e, "", e);
//                                return;
//                            }
//                        }
//
//                        Province province = Province.getProvinceById(mSelectedProvinceId);
//                        City city = City.getCityById(mSelectedCityId);
//
//                        if (province == null || city == null) {
//                            mSendingDataProgressDialog.dismiss();
//                            if (isVisible()) {
//                                Toast.makeText(getActivity(), getString(R.string.error_updating_store_info) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
//                            }
//                        } else {
//                            storeObject.setProvince(province);
//                            storeObject.setCity(city);
//
//
//                            storeObject.setName(mNameEditText.getText().toString().trim());
//                            storeObject.setDescription(mDescriptionEditText.getText().toString().trim());
//                            storeObject.setPhoneNumber(mPhoneNumberEditText.getText().toString().trim());
//                            storeObject.setCellNumber(mCellNumberEditText.getText().toString().trim());
//
//                            if (mLogoImageView.isDeleted()) {
//                                storeObject.remove(Store.LOGO);
//                            } else if (mLogoImageView.isChanged() && mParseFileLogo != null) {
//                                storeObject.setLogo(mParseFileLogo);
//                            }
//
//                            if (mBannerImageView.isDeleted()) {
//                                storeObject.remove(Store.BANNER);
//                            } else if (mBannerImageView.isChanged() && mParseFileBanner != null) {
//                                storeObject.setBanner(mParseFileBanner);
//                            }
//
//                            storeObject.saveInBackground(new SaveCallback() {
//                                @Override
//                                public void done(ParseException e) {
//                                    mSendingDataProgressDialog.dismiss();
//                                    if (e == null) {
//                                        if (isVisible()) {
//                                            Toast.makeText(getActivity(), getString(R.string.successfully_changed_store_info), Toast.LENGTH_LONG).show();
//                                        }
//                                        storeObject.pinInBackground();
//                                        if (mIsNew) {
//                                            Item.setUserItemsStore(storeObject);
//                                        }
//                                    } else {
//                                        logE("Saving store failed. Code" + e.getCode() + "// Msg: " + e.getMessage() + " // error: " + e, "", e);
//                                        try {
//                                            JSONObject error = new JSONObject(e.getMessage());
//                                            if ((error.has("code")) && error.get("code").toString().equals("3001")) {
//                                                if (isVisible()) {
//                                                    Toast.makeText(getActivity(), getString(R.string.store_name_already_exists), Toast.LENGTH_LONG).show();
//                                                    mNameEditText.setError(getString(R.string.store_name_already_exists));
//                                                }
//                                            } else if ((error.has("code")) && error.get("code").toString().equals("3002")) {
//                                                Toast.makeText(getActivity(), getString(R.string.you_own_another_store_or_u_r_not_the_right_owner), Toast.LENGTH_LONG).show();
//                                            } else {
//                                                if (isVisible()) {
//                                                    Toast.makeText(getActivity(), getString(R.string.saving_store_info_failed) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
//                                                }
//                                            }
//                                        } catch (JSONException e1) {
//                                            logE("Saving store failed (JSONException). Code" + e.getCode() + "// Msg: " + e.getMessage() + " // error: " + e, "", e);
//                                            if (isVisible()) {
//                                                Toast.makeText(getActivity(), getString(R.string.saving_store_info_failed) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
//                                            }
//                                        }
//                                    }
//
//                                }
//                            });
//
//                        }
//                    }
//
//                }
//        );
        // TODO save store
    }

    private void setStoreInfo(net.honarnama.nano.Store store, boolean postExecuteOfCreateOrUpdateStore) {
        logE("inja", "store is: " + store.toString());
        if (store != null) {
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
                mCityEditEext.setText(city.getName());
                mSelectedCityId = city.getId();
            }

            if (province != null) {
                mProvinceEditEext.setText(province.getName());
                mSelectedProvinceId = province.getId();
            }

            if (store.reviewStatus == HonarnamaProto.NOT_REVIEWED) {
                mStatusBarTextView.setVisibility(View.VISIBLE);
            }

            if (store.reviewStatus == HonarnamaProto.CHANGES_NEEDED) {
                mStoreNotVerifiedNotif.setVisibility(View.VISIBLE);
                mStatusBarTextView.setVisibility(View.VISIBLE);
                mStatusBarTextView.setText(getString(R.string.please_apply_requested_modification));
            }

            if (!postExecuteOfCreateOrUpdateStore && !TextUtils.isEmpty(store.logo)) {
                logE("inja", "start loading logo is...");

                mLogoProgressBar.setVisibility(View.VISIBLE);
                //TODO load logo
//                        mLogoImageView.loadInBackground(store.getLogo(), new GetDataCallback() {
//                            @Override
//                            public void done(byte[] data, ParseException e) {
//                                mLogoProgressBar.setVisibility(View.GONE);
//                                if (e != null) {
//                                    logE("Getting  logo image failed. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error:" + e, "", e);
//                                    if (progressDialog.isShowing()) {
//                                        progressDialog.dismiss();
//                                    }
//                                    if (isVisible()) {
//                                        Toast.makeText(getActivity(), getString(R.string.error_displaying_store_logo) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
//                                    }
//                                }
//                            }
//                        });

                Picasso.with(getActivity()).load(store.logo)
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
                            }
                        });

            }
            //TODO load banner

            if (!postExecuteOfCreateOrUpdateStore && !TextUtils.isEmpty(store.banner)) {
                logE("inja", "start loading banner is...");
                mBannerProgressBar.setVisibility(View.VISIBLE);
//                        mBannerImageView.loadInBackground(store.getBanner(), new GetDataCallback() {
//                            @Override
//                            public void done(byte[] data, ParseException e) {
//                                mBannerProgressBar.setVisibility(View.GONE);
//                                if (e != null) {
//                                    logE("Getting  banner image failed. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e, "", e);
//                                    if (progressDialog.isShowing()) {
//                                        progressDialog.dismiss();
//                                    }
//                                    if (isVisible()) {
//                                        Toast.makeText(getActivity(), getString(R.string.error_displaying_store_banner) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
//                                    }
//                                }
//                            }
//                        });

                Picasso.with(getActivity()).load(store.banner)
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
                            }
                        });
            }
        } else {
            resetFields();
        }
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

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setCancelable(false);
                mProgressDialog.setMessage(getString(R.string.please_wait));
            }
            if (getActivity() != null && isVisible()) {
                //TODO check if this checking prevents exception or not
                //TODO  if this checking prevents add to others dialog too
                mProgressDialog.show();
            }
        }

        @Override
        protected GetStoreReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            SimpleRequest simpleRequest = new SimpleRequest();

            simpleRequest.requestProperties = rp;

            GetStoreReply getStoreReply;
            try {
                SellServiceGrpc.SellServiceBlockingStub stub = GRPCUtils.getInstance().getSellServiceGrpc();
                getStoreReply = stub.getMyStore(simpleRequest);
                return getStoreReply;
            } catch (InterruptedException e) {
                logE("Error getting user info. Error: " + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(GetStoreReply getStoreReply) {
            super.onPostExecute(getStoreReply);
            if (!getActivity().isFinishing()) { // or call isFinishing() if min sdk version < 17
                dismissProgressDialog();
            }
            if (getStoreReply != null) {
                switch (getStoreReply.replyProperties.statusCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:
                        ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) getActivity());
                        if (controlPanelActivity != null) {
                            controlPanelActivity.displayUpgradeRequiredDialog();
                        }
                        break;
                    case ReplyProperties.CLIENT_ERROR:
                        switch (getStoreReply.errorCode) {
                            case GetStoreReply.STORE_NOT_FOUND:
                                mIsNew = true;
                                logE("inja Store not found");
                                break;

                            case GetStoreReply.NO_CLIENT_ERROR:
                                //TODO bug report
                                break;
                        }
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        //TODO
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        //TODO toast
                        HonarnamaUser.logout(getActivity());
                        break;

                    case ReplyProperties.OK:
                        setStoreInfo(getStoreReply.store, false);
                        break;
                }

            } else {
                //TODO toast
            }
        }
    }


    public class CreateOrUpdateStoreAsync extends AsyncTask<Void, Void, CreateOrUpdateStoreReply> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setCancelable(false);
                mProgressDialog.setMessage(getString(R.string.please_wait));
            }
            mProgressDialog.show();
        }


        @Override
        protected CreateOrUpdateStoreReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();

            CreateOrUpdateStoreRequest createOrUpdateStoreRequest = new CreateOrUpdateStoreRequest();
            createOrUpdateStoreRequest.store = new net.honarnama.nano.Store();
            createOrUpdateStoreRequest.store.name = mNameEditText.getText().toString().trim();
            createOrUpdateStoreRequest.store.description = mDescriptionEditText.getText().toString().trim();
            createOrUpdateStoreRequest.store.publicPhoneNumber = mPhoneNumberEditText.getText().toString().trim();
            createOrUpdateStoreRequest.store.publicCellNumber = mCellNumberEditText.getText().toString().trim();
            createOrUpdateStoreRequest.requestProperties = rp;

            createOrUpdateStoreRequest.store.locationId = new LocationId();
            createOrUpdateStoreRequest.store.locationId.provinceId = mSelectedProvinceId;
            createOrUpdateStoreRequest.store.locationId.cityId = mSelectedCityId;

            createOrUpdateStoreRequest.store.id = mStoreId;

            if (mLogoImageView.isDeleted()) {
                logE("inja Delete logo");
                //TODO test
                createOrUpdateStoreRequest.changingLogo = HonarnamaProto.DELETE;
            } else if (mLogoImageView.isChanged() && mLogoImageView.getFinalImageUri() != null) {
                createOrUpdateStoreRequest.changingLogo = HonarnamaProto.PUT;
            }

            if (mBannerImageView.isDeleted()) {
                logE("inja Delete banner");
                //TODO test
                createOrUpdateStoreRequest.changingBanner = HonarnamaProto.DELETE;
            } else if (mBannerImageView.isChanged() && mBannerImageView.getFinalImageUri() != null) {
                createOrUpdateStoreRequest.changingBanner = HonarnamaProto.PUT;
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
            } catch (InterruptedException e) {
                logE("Error getting user info. Error: " + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final CreateOrUpdateStoreReply createOrUpdateStoreReply) {
            super.onPostExecute(createOrUpdateStoreReply);

            logE("inja", "createOrUpdateStoreReply is " + createOrUpdateStoreReply);
            if (createOrUpdateStoreReply != null) {
                switch (createOrUpdateStoreReply.replyProperties.statusCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:
                        dismissProgressDialog();
                        ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) getActivity());
                        if (controlPanelActivity != null) {
                            controlPanelActivity.displayUpgradeRequiredDialog();
                        }
                        break;
                    case ReplyProperties.CLIENT_ERROR:
                        dismissProgressDialog();
                        switch (createOrUpdateStoreReply.errorCode) {
                            case CreateOrUpdateStoreReply.NO_CLIENT_ERROR:
                                //TODO bug report
                                break;

                            case CreateOrUpdateStoreReply.DUPLICATE_NAME:
                                if (isVisible()) {
                                    Toast.makeText(getActivity(), getString(R.string.store_name_already_exists), Toast.LENGTH_SHORT).show();
                                    mNameEditText.setError(getString(R.string.store_name_already_exists));
                                }
                                break;

                            case CreateOrUpdateStoreReply.STORE_NOT_FOUND:
                                //TODO
                                break;

                            case CreateOrUpdateStoreReply.EMPTY_STORE:
                                break;
                        }
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        dismissProgressDialog();
                        //TODO
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        dismissProgressDialog();
                        //TODO toast
                        HonarnamaUser.logout(getActivity());
                        break;

                    case ReplyProperties.OK:
                        setStoreInfo(createOrUpdateStoreReply.uptodateStore, true);

                        if (TextUtils.isEmpty(createOrUpdateStoreReply.bannerModificationUrl) && TextUtils.isEmpty(createOrUpdateStoreReply.logoModificationUrl)) {
                            if (!getActivity().isFinishing()) { // or call isFinishing() if min sdk version < 17
                                dismissProgressDialog();
                            }
                            if (isVisible()) {
                                Toast.makeText(getActivity(), getString(R.string.successfully_changed_store_info), Toast.LENGTH_SHORT).show();
                            }
                        }

                        if (!TextUtils.isEmpty(createOrUpdateStoreReply.bannerModificationUrl) && mBannerImageView.getFinalImageUri() != null) {
                            final File storeBannerImageFile = new File(mBannerImageView.getFinalImageUri().getPath());
                            final AwsUploader aws = new AwsUploader(storeBannerImageFile, createOrUpdateStoreReply.bannerModificationUrl);

                            if (!TextUtils.isEmpty(createOrUpdateStoreReply.logoModificationUrl) && mLogoImageView.getFinalImageUri() != null) {
                                aws.upload().onSuccessTask(new Continuation<Void, Task<Void>>() {
                                    @Override
                                    public Task<Void> then(Task<Void> task) throws Exception {
                                        final File storeLogoImageFile = new File(mLogoImageView.getFinalImageUri().getPath());
                                        final AwsUploader aws = new AwsUploader(storeLogoImageFile, createOrUpdateStoreReply.logoModificationUrl);
                                        return aws.upload();
                                    }
                                }).continueWith(new Continuation<Void, Object>() {
                                    @Override
                                    public Object then(Task<Void> task) throws Exception {
                                        dismissProgressDialog();
                                        if (task.isFaulted()) {
                                            if (isVisible()) {
                                                Toast.makeText(getActivity(),
                                                        "خطا در ارسال تصاویر."
                                                                + getString(R.string.please_check_internet_connection), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        return null;
                                    }
                                });

                            } else {
                                aws.upload().continueWith(new Continuation<Void, Object>() {
                                    @Override
                                    public Object then(Task<Void> task) throws Exception {
                                        dismissProgressDialog();
                                        if (task.isFaulted()) {
                                            if (isVisible()) {
                                                Toast.makeText(getActivity(),
                                                        "خطا در ارسال تصاویر."
                                                                + getString(R.string.please_check_internet_connection), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        return null;
                                    }
                                });
                            }
                        } else if (!TextUtils.isEmpty(createOrUpdateStoreReply.logoModificationUrl) && mLogoImageView.getFinalImageUri() != null) {
                            final File storeLogoImageFile = new File(mLogoImageView.getFinalImageUri().getPath());
                            final AwsUploader aws = new AwsUploader(storeLogoImageFile, createOrUpdateStoreReply.logoModificationUrl);
                            aws.upload().continueWith(new Continuation<Void, Object>() {
                                @Override
                                public Object then(Task<Void> task) throws Exception {
                                    dismissProgressDialog();
                                    if (task.isFaulted()) {
                                        if (isVisible()) {
                                            Toast.makeText(getActivity(),
                                                    "خطا در ارسال تصاویر."
                                                            + getString(R.string.please_check_internet_connection), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    return null;
                                }
                            });
                        }
                        break;
                }

            } else {
                //TODO toast
            }
        }
    }


    private void dismissProgressDialog() {
        if (!getActivity().isFinishing()) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    }

}
