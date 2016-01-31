package net.honarnama.sell.fragments;

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
import net.honarnama.core.adapter.ProvincesAdapter;
import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.core.model.City;
import net.honarnama.core.model.Item;
import net.honarnama.core.model.Provinces;
import net.honarnama.core.model.Store;
import net.honarnama.core.utils.GenericGravityTextWatcher;
import net.honarnama.core.utils.HonarnamaUser;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.core.utils.ObservableScrollView;
import net.honarnama.core.utils.ParseIO;
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
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

public class StoreInfoFragment extends HonarnamaBaseFragment implements View.OnClickListener, ObservableScrollView.OnScrollChangedListener {

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
    public TreeMap<Number, HashMap<String, String>> mProvincesOrderedTreeMap = new TreeMap<Number, HashMap<String, String>>();
    public HashMap<String, String> mProvincesHashMap = new HashMap<String, String>();

    private EditText mCityEditEext;
    public TreeMap<Number, HashMap<String, String>> mCityOrderedTreeMap = new TreeMap<Number, HashMap<String, String>>();
    public HashMap<String, String> mCityHashMap = new HashMap<String, String>();

    ProgressDialog mSendingDataProgressDialog;
    ParseFile mParseFileLogo;
    ParseFile mParseFileBanner;

    public String mSelectedProvinceId;
    public String mSelectedProvinceName;

    public String mSelectedCityId;
    public String mSelectedCityName;

    public ProgressBar mBannerProgressBar;
    public ProgressBar mLogoProgressBar;

    public TextView mStatusBarTextView;
    public RelativeLayout mStoreNotVerifiedNotif;

    public static StoreInfoFragment mStoreInfoFragment;

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.nav_title_store_info);
    }

    public synchronized static StoreInfoFragment getInstance() {
        if (mStoreInfoFragment == null) {
            mStoreInfoFragment = new StoreInfoFragment();
        }
        return mStoreInfoFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences sharedPref =  HonarnamaBaseApp.getInstance().getSharedPreferences(HonarnamaUser.getCurrentUser().getUsername(), Context.MODE_PRIVATE);
        if (!sharedPref.getBoolean(HonarnamaSellApp.PREF_LOCAL_DATA_STORE_FOR_STORE_SYNCED, false)) {

            if (!NetworkManager.getInstance().isNetworkEnabled(true) || !sharedPref.getBoolean(HonarnamaSellApp.PREF_LOCAL_DATA_STORE_SYNCED, false)) {

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_SYNCED, false);
                editor.commit();

                Intent intent = new Intent(getActivity(), ControlPanelActivity.class);
                getActivity().finish();
                startActivity(intent);
            }

        }

        View rootView = inflater.inflate(R.layout.fragment_store_info, container, false);
        // Inflate the layout for this fragment

        mBannerProgressBar = (ProgressBar) rootView.findViewById(R.id.banner_progress_bar);
        mLogoProgressBar = (ProgressBar) rootView.findViewById(R.id.logo_progress_bar);

        mSendingDataProgressDialog = new ProgressDialog(getActivity());

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

        setStoredStoreInfo();
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

            mSelectedProvinceId = Provinces.DEFAULT_PROVINCE_ID;
            mSelectedCityId = City.DEFAULT_CITY_ID;

            mProvinceEditEext.setText(Provinces.DEFAULT_PROVINCE_NAME);
            mCityEditEext.setText(City.DEFAULT_CITY_NAME);

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
                    mSendingDataProgressDialog.setCancelable(false);
                    mSendingDataProgressDialog.setMessage(getString(R.string.sending_data));
                    mSendingDataProgressDialog.show();
                    uploadStoreLogo();
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
        provincesAdapter = new ProvincesAdapter(getActivity(), mProvincesOrderedTreeMap);
        provincesListView.setAdapter(provincesAdapter);
        provincesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> selectedProvince = mProvincesOrderedTreeMap.get(position + 1);
                for (String key : selectedProvince.keySet()) {
                    mSelectedProvinceId = key;
                }
                for (String value : selectedProvince.values()) {
                    mSelectedProvinceName = value;
                    mProvinceEditEext.setText(mSelectedProvinceName);
                }
                rePopulateCityList();
                provinceDialog.dismiss();
            }
        });
        provinceDialog.setCancelable(true);
        provinceDialog.setTitle("انتخاب استان");
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
        cityDialog.setTitle("انتخاب شهر");
        cityDialog.show();
    }

    private boolean AreFormInputsValid() {
        if (mNameEditText.getText().toString().trim().length() == 0) {
            mNameEditText.requestFocus();
            mNameEditText.setError(getString(R.string.error_store_name_cant_be_empty));
            return false;
        }
        if (mSelectedProvinceId == null) {
            mProvinceEditEext.requestFocus();
            mProvinceEditEext.setError(getString(R.string.error_store_province_not_set));
        }

        if (mSelectedCityId == null) {
            mCityEditEext.requestFocus();
            mCityEditEext.setError(getString(R.string.error_store_city_not_set));
        }

        if (mCellNumberEditText.getText().toString().trim().length() == 0 && mPhoneNumberEditText.getText().toString().trim().length() == 0) {
            mCellNumberEditText.requestFocus();
            mCellNumberEditText.setError("حداقل یکی از گزینه ‌های تماس را پر کنید.");
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

    public void uploadStoreLogo() {

        if (!NetworkManager.getInstance().isNetworkEnabled(false)) {
            mSendingDataProgressDialog.dismiss();
            if (isVisible()) {
                Toast.makeText(getActivity(), R.string.error_uploading_logo + getString(R.string.please_check_internet_connection), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (!mLogoImageView.isChanged() || mLogoImageView.getFinalImageUri() == null) {
//            saveStore(null);
            uploadStoreBanner();
            return;
        }

        final File storeLogoImageFile = new File(mLogoImageView.getFinalImageUri().getPath());
        try {
            mParseFileLogo = ParseIO.getParseFileFromFile(HonarnamaSellApp.STORE_LOGO_FILE_NAME,
                    storeLogoImageFile);
            mParseFileLogo.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {
//                        saveStore(parseFile);
                        uploadStoreBanner();
//                        try {
//                            ParseIO.copyFile(storeLogoImageFile, new File(HonarnamaBaseApp.APP_IMAGES_FOLDER, HonarnamaSellApp.STORE_LOGO_FILE_NAME));
//                        } catch (IOException e1) {
//                            if (BuildConfig.DEBUG) {
//                                Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
//                                        "Error copying store logo to sd card " + e1, e1);
//                            } else {
//                                Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Error copying store logo to sd card"
//                                        + e1.getMessage());
//                            }
//                        }
                    } else {
                        if (isVisible()) {
                            Toast.makeText(getActivity(), R.string.error_uploading_logo + getString(R.string.please_try_again), Toast.LENGTH_LONG).show();
                        }
                        if (BuildConfig.DEBUG) {
                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "Uploading Store Logo Failed. Code: " + e.getCode() +
                                    "//" + e.getMessage() + " // " + e);
                        } else {
                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Uploading Store Logo Failed. Code: " + e.getCode() +
                                    "//" + e.getMessage() + " // " + e);
                        }
                        mSendingDataProgressDialog.dismiss();
                    }
                }
            });
        } catch (IOException ioe) {
            mSendingDataProgressDialog.dismiss();
            if (isVisible()) {
                Toast.makeText(getActivity(), R.string.error_uploading_logo + getString(R.string.please_try_again), Toast.LENGTH_LONG).show();
            }
            if (BuildConfig.DEBUG) {
                Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                        "Failed on preparing store logo image. " + ioe, ioe);
            } else {
                Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Failed on preparing store logo image. ioe="
                        + ioe.getMessage());
            }
        }
    }

    public void uploadStoreBanner() {

        if (!NetworkManager.getInstance().isNetworkEnabled(false)) {
            mSendingDataProgressDialog.dismiss();
            if (isVisible()) {
                Toast.makeText(getActivity(), getString(R.string.error_uploading_banner) + getString(R.string.please_check_internet_connection), Toast.LENGTH_SHORT).show();
            }
            return;
        }


        if (!mBannerImageView.isChanged() || mBannerImageView.getFinalImageUri() == null) {
            saveStore();
            return;
        }
        final File storeBannerImageFile = new File(mBannerImageView.getFinalImageUri().getPath());
        try {
            mParseFileBanner = ParseIO.getParseFileFromFile(HonarnamaSellApp.STORE_BANNER_FILE_NAME,
                    storeBannerImageFile);
            mParseFileBanner.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        saveStore();
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
                        if (BuildConfig.DEBUG) {
                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "Uploading Store Banner Failed. Code: " + e.getCode() +
                                    "//" + e.getMessage() + " // " + e);
                        } else {
                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Uploading Store Banner Failed. Code: " + e.getCode() +
                                    "//" + e.getMessage() + " // " + e);
                        }
                    }
                }
            });
        } catch (IOException ioe) {
            mSendingDataProgressDialog.dismiss();
            if (isVisible()) {
                Toast.makeText(getActivity(), getString(R.string.error_uploading_banner) + getString(R.string.please_try_again), Toast.LENGTH_LONG).show();
            }

            if (BuildConfig.DEBUG) {
                Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                        "Failed on preparing store banner image. " + ioe, ioe);
            } else {
                Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Failed on preparing store banner image. ioe="
                        + ioe.getMessage());
            }

        }
    }


    private void saveStore() {

        if (!NetworkManager.getInstance().isNetworkEnabled(false)) {
            mSendingDataProgressDialog.dismiss();
            if (isVisible()) {
                Toast.makeText(getActivity(), getString(R.string.error_updating_store_info) + getString(R.string.please_check_internet_connection), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        ParseQuery<Store> query = ParseQuery.getQuery(Store.class);
        query.whereEqualTo(Store.OWNER, HonarnamaUser.getCurrentUser());
        query.getFirstInBackground(new GetCallback<Store>() {
            @Override
            public void done(final Store store, ParseException e) {
                final Store storeObject;
                if (e == null) {
                    storeObject = store;
                } else {
                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        storeObject = new Store();
                        storeObject.setOwner(HonarnamaUser.getCurrentUser());
                    } else {
                        mSendingDataProgressDialog.dismiss();
                        if (isVisible()) {
                            Toast.makeText(getActivity(), getString(R.string.error_updating_store_info) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                        }
                        if (BuildConfig.DEBUG) {
                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                    "Error changing Store Info.  Error Code: " + e.getCode() +
                                            "//" + e.getMessage() + " // " + e, e);
                        } else {
                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Error changing Store Info. "
                                    + e.getMessage());
                        }
                        return;
                    }
                }
                storeObject.setName(mNameEditText.getText().toString().trim());
                storeObject.setDescription(mDescriptionEditText.getText().toString().trim());
                storeObject.setPhoneNumber(mPhoneNumberEditText.getText().toString().trim());
                storeObject.setCellNumber(mCellNumberEditText.getText().toString().trim());
                storeObject.setProvinceId(mSelectedProvinceId);
                storeObject.setCityId(mSelectedCityId);

                if (mLogoImageView.isDeleted()) {
                    storeObject.remove(Store.LOGO);
                } else if (mLogoImageView.isChanged() && mParseFileLogo != null) {
                    storeObject.setLogo(mParseFileLogo);
                }

                if (mBannerImageView.isDeleted()) {
                    storeObject.remove(Store.BANNER);
                } else if (mBannerImageView.isChanged() && mParseFileBanner != null) {
                    storeObject.setBanner(mParseFileBanner);
                }

                storeObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        mSendingDataProgressDialog.dismiss();
                        if (e == null) {
                            if (isVisible()) {
                                Toast.makeText(getActivity(), getString(R.string.successfully_changed_store_info), Toast.LENGTH_LONG).show();
                            }
                            storeObject.pinInBackground();
                        } else {
                            logE("Saving store failed, storeObject= " + storeObject + "// error: " + e, "Saving store failed, error message= " + e.getMessage(), e);
                            try {
                                JSONObject error = new JSONObject(e.getMessage());
                                if ((error.has("code")) && error.get("code").toString().equals("3001")) {
                                    if (isVisible()) {
                                        Toast.makeText(getActivity(), "نام فروشگاه تکراری است. لطفا نام دیگری انتخاب کنید.", Toast.LENGTH_LONG).show();
                                        mNameEditText.setError("نام فروشگاه تکراری است. لطفا نام دیگری انتخاب کنید.");
                                    }
                                } else if ((error.has("code")) && error.get("code").toString().equals("3002")) {
                                    Toast.makeText(getActivity(), "شما فروشگاه دیگری دارید. یا صاحب این فروشگاه نیستید.", Toast.LENGTH_LONG).show();
                                } else {
                                    if (isVisible()) {
                                        Toast.makeText(getActivity(), getString(R.string.saving_store_info_failed) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                                    }
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                                if (isVisible()) {
                                    Toast.makeText(getActivity(), getString(R.string.saving_store_info_failed) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                    }
                });

            }
        });
    }

    private void setStoredStoreInfo() {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();

        final Provinces provinces = new Provinces();
        final City city = new City();

        getUserStoreAsync().continueWith(new Continuation<Store, Void>() {
            @Override
            public Void then(Task<Store> task) throws Exception {
                if (task.isFaulted()) {
                    if (BuildConfig.DEBUG) {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                "Getting Store Task Failed." +
                                        "//" + task.getError().getMessage(), task.getError());
                    } else {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Getting Store Task Failed. // " + task.getError().getMessage());
                    }
                    progressDialog.dismiss();
                    if (isVisible()) {
                        Toast.makeText(getActivity(), getString(R.string.getting_store_info_failed) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                    }

                    Intent intent = new Intent(getActivity(), ControlPanelActivity.class);
                    getActivity().finish();
                    startActivity(intent);

                } else {
                    Store store = task.getResult();
                    if (store != null) {
                        mNameEditText.setText(store.getName());
                        mDescriptionEditText.setText(store.getDescription());

                        mPhoneNumberEditText.setText(store.getPhoneNumber());
                        mCellNumberEditText.setText(store.getCellNumber());

                        mSelectedProvinceId = store.getProvinceId();
                        mSelectedCityId = store.getCityId();

                        mCityEditEext.setText(getString(R.string.getting_information));
                        mProvinceEditEext.setText(getString(R.string.getting_information));


                        if (store.getStatus() == Store.STATUS_CODE_CONFIRMATION_WAITING) {
                            mStatusBarTextView.setVisibility(View.VISIBLE);
                        }

                        if (store.getStatus() == Store.STATUS_CODE_NOT_VERIFIED) {
                            mStoreNotVerifiedNotif.setVisibility(View.VISIBLE);
                            mStatusBarTextView.setVisibility(View.VISIBLE);
                            mStatusBarTextView.setText("لطفا تغییرات خواسته شده را اعمال کنید.");
                        }

                        mLogoProgressBar.setVisibility(View.VISIBLE);
                        mLogoImageView.loadInBackground(store.getLogo(), new GetDataCallback() {
                            @Override
                            public void done(byte[] data, ParseException e) {
                                mLogoProgressBar.setVisibility(View.GONE);
                                if (e != null) {
                                    if (BuildConfig.DEBUG) {
                                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                                "Getting logo image failed" +
                                                        "//" + e.getMessage(), e);
                                    } else {
                                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Getting  logo image failed. // " + e.getMessage());
                                    }
                                    if (progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
                                    if (isVisible()) {
                                        Toast.makeText(getActivity(), "خطا در نمایش تصویر لوگو." + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });

                        mBannerProgressBar.setVisibility(View.VISIBLE);
                        mBannerImageView.loadInBackground(store.getBanner(), new GetDataCallback() {
                            @Override
                            public void done(byte[] data, ParseException e) {
                                mBannerProgressBar.setVisibility(View.GONE);
                                if (e != null) {
                                    if (BuildConfig.DEBUG) {
                                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                                "Getting banner image failed" +
                                                        "//" + e.getMessage(), e);
                                    } else {
                                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Getting  banner image failed. // " + e.getMessage());
                                    }
                                    if (progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
                                    if (isVisible()) {
                                        Toast.makeText(getActivity(), "خطا در نمایش تصویر بنر." + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
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
        }).continueWithTask(new Continuation<Void, Task<TreeMap<Number, HashMap<String, String>>>>() {
            @Override
            public Task<TreeMap<Number, HashMap<String, String>>> then(Task<Void> task) throws Exception {
                if (mSelectedProvinceId == null) {
                    mSelectedProvinceId = Provinces.DEFAULT_PROVINCE_ID;

                }
                if (mSelectedCityId == null) {
                    mSelectedCityId = City.DEFAULT_CITY_ID;
                }
                return provinces.getOrderedProvinces(HonarnamaBaseApp.getInstance());
            }
        }).continueWith(new Continuation<TreeMap<Number, HashMap<String, String>>, Object>() {
            @Override
            public Object then(Task<TreeMap<Number, HashMap<String, String>>> task) throws Exception {
                if (task.isFaulted()) {
                    mProvinceEditEext.setText(Provinces.DEFAULT_PROVINCE_NAME);
                    if (BuildConfig.DEBUG) {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                "Getting Province Task Failed" +
                                        "//" + task.getError().getMessage(), task.getError());
                    } else {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Getting Province Task Failed. // " + task.getError().getMessage());
                    }
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    if (isVisible()) {
                        Toast.makeText(getActivity(), getString(R.string.error_getting_province_list) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                    }
                } else {
                    mProvincesOrderedTreeMap = task.getResult();
                    for (HashMap<String, String> provinceMap : mProvincesOrderedTreeMap.values()) {
                        for (Map.Entry<String, String> provinceSet : provinceMap.entrySet()) {
                            mProvincesHashMap.put(provinceSet.getKey(), provinceSet.getValue());
                        }
                    }
                    mProvinceEditEext.setText(mProvincesHashMap.get(mSelectedProvinceId));
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
                    if (BuildConfig.DEBUG) {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                "Getting City List Task Failed." +
                                        "//" + task.getError().getMessage(), task.getError());
                    } else {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Getting City List Task Failed. // " + task.getError().getMessage());
                    }
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
                    mCityEditEext.setText(mCityHashMap.get(mSelectedCityId));

                }
                if ((isVisible()) && !NetworkManager.getInstance().isNetworkEnabled(true)) {
                    Toast.makeText(getActivity(), "برای مشاهده به‌روزترین اطلاعات دستگاه خود را به اینترنت متصل کنید.", Toast.LENGTH_LONG).show();
                }
                return null;
            }
        });

    }

    public Task<Store> getUserStoreAsync() {
        final TaskCompletionSource<Store> tcs = new TaskCompletionSource<>();
        ParseQuery<Store> query = ParseQuery.getQuery(Store.class);

        query.whereEqualTo(Store.OWNER, HonarnamaUser.getCurrentUser());

//        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences sharedPref =  HonarnamaBaseApp.getInstance().getSharedPreferences(HonarnamaUser.getCurrentUser().getUsername(), Context.MODE_PRIVATE);

        if (!NetworkManager.getInstance().isNetworkEnabled(false)) {
            if (sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_STORE_SYNCED, false)) {
                if (BuildConfig.DEBUG) {
                    Log.d(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getActivity().getClass().getName(), "getting store info from local datastore");
                }
                query.fromLocalDatastore();
            } else {
                tcs.setError(new NetworkErrorException("No network connection + Offline ddata not available for store"));
                return tcs.getTask();
            }
        }


        query.getFirstInBackground(new GetCallback<Store>() {
            @Override
            public void done(final Store store, ParseException e) {
                if (e == null) {
                    tcs.trySetResult(store);
//                    if (!sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_STORE_SYNCED, false)) {

                    final List<Store> tempStoreList = new ArrayList<Store>() {{
                        add(store);
                    }};

                    ParseObject.unpinAllInBackground(Store.OBJECT_NAME, tempStoreList, new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {

                                ParseObject.pinAllInBackground(Store.OBJECT_NAME, tempStoreList, new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    SharedPreferences.Editor editor = sharedPref.edit();
                                                    editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_STORE_SYNCED, true);
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
                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                    "Getting User Store Result: User does not have any store yet.");
                        }
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_STORE_SYNCED, true);
                        editor.commit();
                        tcs.trySetResult(null);
                    } else {
                        tcs.trySetError(e);
                        if (BuildConfig.DEBUG) {
                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                    "Error Getting Store Info.  Error Code: " + e.getCode() +
                                            "//" + e.getMessage() + " // " + e, e);
                        } else {
                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Error Getting Store Info. "
                                    + e.getMessage());
                        }
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

}
