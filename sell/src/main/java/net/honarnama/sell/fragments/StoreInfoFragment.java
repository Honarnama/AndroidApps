package net.honarnama.sell.fragments;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.core.adapter.CityAdapter;
import net.honarnama.core.adapter.ProvincesAdapter;
import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.model.City;
import net.honarnama.core.model.Provinces;
import net.honarnama.core.model.Store;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;

import com.parse.ImageSelector;

import net.honarnama.core.utils.HonarnamaUser;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.core.utils.ParseIO;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

public class StoreInfoFragment extends HonarnamaBaseFragment implements View.OnClickListener {

    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private EditText mPhoneNumberEditText;
    private EditText mCellNumberEditText;
    private Button mRegisterStoreButton;
    private ImageSelector mLogoImageView;
    private ImageSelector mBannerImageView;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_store_info, container, false);
        // Inflate the layout for this fragment

        mSendingDataProgressDialog = new ProgressDialog(getActivity());

        mNameEditText = (EditText) rootView.findViewById(R.id.store_name_edit_text);
        mDescriptionEditText = (EditText) rootView.findViewById(R.id.store_description_edit_text);
        mPhoneNumberEditText = (EditText) rootView.findViewById(R.id.store_phone_number);
        mCellNumberEditText = (EditText) rootView.findViewById(R.id.store_cell_number);

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
                Toast.makeText(getActivity(), mSelectedProvinceName, Toast.LENGTH_LONG).show();
                provinceDialog.dismiss();
            }
        });
        provinceDialog.setCancelable(true);
        provinceDialog.setTitle("انتخاب استان");
        provinceDialog.show();
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
                Toast.makeText(getActivity(), mSelectedCityName, Toast.LENGTH_LONG).show();
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
            mNameEditText.setError(getActivity().getString(R.string.error_store_name_cant_be_empty));
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mLogoImageView.onActivityResult(requestCode, resultCode, intent);
        mBannerImageView.onActivityResult(requestCode, resultCode, intent);
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

    public void uploadStoreLogo() {

        if (!NetworkManager.getInstance().isNetworkEnabled(getActivity(), true)) {
            return;
        }

        if (!mLogoImageView.isChanged() || mLogoImageView.getFinalImageUri() == null) {
//            saveStore(null);
            uploadStoreBanner();
            return;
        }

        Toast.makeText(getActivity(), "Uploading logo", Toast.LENGTH_SHORT).show();
        final File storeLogoImageFile = new File(mLogoImageView.getFinalImageUri().getPath());
        try {
            mParseFileLogo = ParseIO.getParseFileFromFile(HonarnamaSellApp.STORE_LOGO_FILE_NAME,
                    storeLogoImageFile);
            mParseFileLogo.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {
//                        saveStore(parseFile);
                        uploadStoreBanner();
                        try {
                            ParseIO.copyFile(storeLogoImageFile, new File(HonarnamaBaseApp.APP_IMAGES_FOLDER, HonarnamaSellApp.STORE_LOGO_FILE_NAME));
                        } catch (IOException e1) {
                            if (BuildConfig.DEBUG) {
                                Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                        "Error copying store logo to sd card " + e1, e1);
                            } else {
                                Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Error copying store logo to sd card"
                                        + e1.getMessage());
                            }
                        }
                    } else {
                        Toast.makeText(getActivity(), "خطا در ارسال تصویر لوگو. لطفا دوباره تلاش کنید.", Toast.LENGTH_LONG).show();
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
            Toast.makeText(StoreInfoFragment.this.getActivity(), "خطا در ارسال تصویر لوگو. لطفا دوباره تلاش کنید.",
                    Toast.LENGTH_LONG).show();

            if (BuildConfig.DEBUG) {
                Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                        "Failed on preparing store logo image. " + ioe, ioe);
            } else {
                Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Failed on preparing store logo image. ioe="
                        + ioe.getMessage());
            }

            mSendingDataProgressDialog.dismiss();
        }
    }

    public void uploadStoreBanner() {


        if (!NetworkManager.getInstance().isNetworkEnabled(getActivity(), true)) {
            mSendingDataProgressDialog.dismiss();
            return;
        }

        if (!mBannerImageView.isChanged() || mBannerImageView.getFinalImageUri() == null) {
            saveStore();
            return;
        }
        Toast.makeText(getActivity(), "Uploading banner", Toast.LENGTH_SHORT).show();
        final File storeBannerImageFile = new File(mBannerImageView.getFinalImageUri().getPath());
        try {
            mParseFileBanner = ParseIO.getParseFileFromFile(HonarnamaSellApp.STORE_BANNER_FILE_NAME,
                    storeBannerImageFile);
            mParseFileBanner.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        saveStore();
                        try {
                            ParseIO.copyFile(storeBannerImageFile, new File(HonarnamaBaseApp.APP_IMAGES_FOLDER, HonarnamaSellApp.STORE_BANNER_FILE_NAME));
                        } catch (IOException e1) {
                            if (BuildConfig.DEBUG) {
                                Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                        "Error copying store banner to sd card " + e1, e1);
                            } else {
                                Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Error copying store banner to sd card"
                                        + e1.getMessage());
                            }
                        }
                    } else {
                        Toast.makeText(getActivity(), "خطا در ارسال تصویر بنر. لطفا دوباره تلاش کنید. ", Toast.LENGTH_LONG).show();
                        if (BuildConfig.DEBUG) {
                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "Uploading Store Banner Failed. Code: " + e.getCode() +
                                    "//" + e.getMessage() + " // " + e);
                        } else {
                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Uploading Store Banner Failed. Code: " + e.getCode() +
                                    "//" + e.getMessage() + " // " + e);
                        }
                        mSendingDataProgressDialog.dismiss();
                    }
                }
            });
        } catch (IOException ioe) {
            Toast.makeText(StoreInfoFragment.this.getActivity(), "خطا در ارسال تصویر بنر. لطفا دوباره تلاش کنید. ",
                    Toast.LENGTH_LONG).show();

            if (BuildConfig.DEBUG) {
                Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                        "Failed on preparing store banner image. " + ioe, ioe);
            } else {
                Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Failed on preparing store banner image. ioe="
                        + ioe.getMessage());
            }

            mSendingDataProgressDialog.dismiss();
        }
    }


    private void saveStore() {
        if (!NetworkManager.getInstance().isNetworkEnabled(getActivity(), true)) {
            mSendingDataProgressDialog.dismiss();
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
                        Toast.makeText(getActivity(), "خطا در زمان به‌روزرسانی اطلاعات فروشگاه.", Toast.LENGTH_LONG).show();
                        if (BuildConfig.DEBUG) {
                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                    "Error changing Store Info.  Error Code: " + e.getCode() +
                                            "//" + e.getMessage() + " // " + e, e);
                        } else {
                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Error Changing Store Info. "
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

                storeObject.pinInBackground();
                storeObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Toast.makeText(getActivity(), getActivity().getString(R.string.successfully_changed_store_info), Toast.LENGTH_LONG).show();
                        } else {
                            // TODO: handle "Invalid: name"
                            Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "storeObject= " + storeObject, e);
                            Toast.makeText(getActivity(), getActivity().getString(R.string.saving_store_info_failed), Toast.LENGTH_LONG).show();
                        }
                        mSendingDataProgressDialog.dismiss();
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
        provinces.getOrderedProvinces(getActivity()).continueWithTask(new Continuation<TreeMap<Number, HashMap<String, String>>, Task<TreeMap<Number, HashMap<String, String>>>>() {
            @Override
            public Task<TreeMap<Number, HashMap<String, String>>> then(Task<TreeMap<Number, HashMap<String, String>>> task) throws Exception {
                if (task.isFaulted()) {
                    Toast.makeText(getActivity(), "Something went wrong while getting provinces list!", Toast.LENGTH_LONG).show();
//                    throw task.getError();

                } else {
                    mProvincesOrderedTreeMap = task.getResult();
                    for (HashMap<String, String> provinceMap : mProvincesOrderedTreeMap.values()) {
                        for (Map.Entry<String, String> provinceSet : provinceMap.entrySet()) {
                            mProvincesHashMap.put(provinceSet.getKey(), provinceSet.getValue());
                        }
                    }
                }

                return city.getOrderedCities(getActivity(), mSelectedCityId);
            }
        }).continueWithTask(new Continuation<TreeMap<Number, HashMap<String, String>>, Task<Store>>() {
            @Override
            public Task<Store> then(Task<TreeMap<Number, HashMap<String, String>>> task) throws Exception {
                if (task.isFaulted()) {
                    Toast.makeText(getActivity(), "Something went wrong while getting city list!", Toast.LENGTH_LONG).show();
                } else {
                    mCityOrderedTreeMap = task.getResult();
                    for (HashMap<String, String> cityMap : mCityOrderedTreeMap.values()) {
                        for (Map.Entry<String, String> citySet : cityMap.entrySet()) {
                            mCityHashMap.put(citySet.getKey(), citySet.getValue());
                        }
                    }
                }
                return getUserStoreAsync();
            }
        }).continueWith(new Continuation<Store, Object>() {
            @Override
            public Object then(Task<Store> task) throws Exception {
                progressDialog.dismiss();

                if (task.isFaulted()) {
                    if (BuildConfig.DEBUG) {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                "Error Getting Store Info." +
                                        "//" + task.getError().getMessage(), task.getError());
                    } else {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Error Getting Store Info. // " + task.getError().getMessage());
                    }
                    Toast.makeText(getActivity(), getString(R.string.getting_store_info_failed), Toast.LENGTH_LONG).show();

                } else {
                    Store store = task.getResult();
                    if (store == null) {
                        return null;
                    }
                    mNameEditText.setText(store.getName());
                    mDescriptionEditText.setText(store.getDescription());
                    mPhoneNumberEditText.setText(store.getPhoneNumber());
                    mCellNumberEditText.setText(store.getCellNumber());

                    String storeProvinceId = store.getProvinceId();
                    if(storeProvinceId == null){
                        storeProvinceId = Provinces.DEFAULT_PROVINCE_ID;
                    }
                    mSelectedProvinceId = storeProvinceId;

                    String cityId = store.getCityId();
                    if(cityId == null){
                        cityId = City.DEFAULT_CITY_ID;
                    }
                    mSelectedCityId = cityId;

                    mProvinceEditEext.setText(mProvincesHashMap.get(storeProvinceId));
                    mCityEditEext.setText(mCityHashMap.get(cityId));

                    mLogoImageView.loadInBackground(store.getLogo(), new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException e) {

                        }
                    });
                    mBannerImageView.loadInBackground(store.getBanner(), new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException e) {

                        }
                    });

                }
                return null;
            }
        });

    }

    public Task<Store> getUserStoreAsync() {
        final TaskCompletionSource<Store> tcs = new TaskCompletionSource<>();
        ParseQuery<Store> query = ParseQuery.getQuery(Store.class);
        query.whereEqualTo(Store.OWNER, HonarnamaUser.getCurrentUser());
        query.fromLocalDatastore();
        query.getFirstInBackground(new GetCallback<Store>() {
            @Override
            public void done(Store store, ParseException e) {
                if (e == null) {
                    tcs.setResult(store);

                } else {
                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        tcs.setResult(null);
                    } else {
                        tcs.setError(e);
                    }
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
        });
        return tcs.getTask();
    }


}
