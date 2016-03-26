package net.honarnama.browse.dialog;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.HonarnamaBrowseActivity;
import net.honarnama.browse.widget.HorizontalNumberPicker;
import net.honarnama.core.adapter.CityAdapter;
import net.honarnama.core.adapter.ProvincesAdapter;
import net.honarnama.core.model.City;
import net.honarnama.core.model.Provinces;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.core.utils.TextUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by elnaz on 3/2/16.
 */
public class ItemFilterDialogActivity extends HonarnamaBrowseActivity implements View.OnClickListener {

    public Activity mActivity;
    private EditText mProvinceEditText;

    public String mSelectedProvinceId;
    public String mSelectedProvinceName;

    public Dialog mDialog;
    public String mSelectedCityId;
    public String mSelectedCityName;
    public TreeMap<Number, Provinces> mProvincesObjectsTreeMap = new TreeMap<Number, Provinces>();
    public HashMap<String, String> mProvincesHashMap = new HashMap<String, String>();

    private EditText mCityEditEext;
    public TreeMap<Number, HashMap<String, String>> mCityOrderedTreeMap = new TreeMap<Number, HashMap<String, String>>();
    public HashMap<String, String> mCityHashMap = new HashMap<String, String>();
    private Provinces mSelectedProvince;

    HorizontalNumberPicker mMinPriceHorizontalPicker;
    HorizontalNumberPicker mMaxPriceHorizontalPicker;
    public int mMinPriceIndex;
    public int mMaxPriceIndex;

    public CheckBox mAllIranCheckBox;

    IconicsImageView mRefetchProvinces;
    IconicsImageView mRefetchCities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_filter_dialog);

        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


        mActivity = ItemFilterDialogActivity.this;
        mProvinceEditText = (EditText) findViewById(R.id.province_edit_text);
        mProvinceEditText.setOnClickListener(this);
        mProvinceEditText.setKeyListener(null);

        mCityEditEext = (EditText) findViewById(R.id.city_edit_text);
        mCityEditEext.setOnClickListener(this);
        mCityEditEext.setKeyListener(null);

        mRefetchProvinces = (IconicsImageView) findViewById(R.id.refetchProvinces);
        mRefetchProvinces.setOnClickListener(this);

        mRefetchCities = (IconicsImageView) findViewById(R.id.refetchCities);
        mRefetchCities.setOnClickListener(this);

        final IconicsDrawable unCheckedDrawable = new IconicsDrawable(ItemFilterDialogActivity.this)
                .icon(GoogleMaterial.Icon.gmd_check_box_outline_blank)
                .color(getResources().getColor(R.color.dark_cyan))
                .sizeDp(20);
        mAllIranCheckBox = (CheckBox) findViewById(R.id.all_iran_checkbox);
        mAllIranCheckBox.setButtonDrawable(unCheckedDrawable);
        mAllIranCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mAllIranCheckBox.setButtonDrawable(
                            new IconicsDrawable(ItemFilterDialogActivity.this)
                                    .icon(GoogleMaterial.Icon.gmd_check_box)
                                    .color(getResources().getColor(R.color.dark_cyan))
                                    .sizeDp(20)
                    );
                } else {
                    mAllIranCheckBox.setButtonDrawable(unCheckedDrawable);
                }
            }
        });

        Intent intent = getIntent();
        mSelectedProvinceId = intent.getStringExtra(HonarnamaBrowseApp.EXTRA_KEY_PROVINCE_ID);
        if (TextUtils.isEmpty(mSelectedProvinceId)) {
            mSelectedProvinceId = getDefaultLocationProvinceId();
        }
        mSelectedCityId = intent.getStringExtra(HonarnamaBrowseApp.EXTRA_KEY_CITY_ID);
        if (TextUtils.isEmpty(mSelectedCityId)) {
            mSelectedCityId = getDefaultLocationCityId();
        }

        if (intent.hasExtra(HonarnamaBaseApp.EXTRA_KEY_ALL_IRAN)) {
            mAllIranCheckBox.setChecked(intent.getBooleanExtra(HonarnamaBaseApp.EXTRA_KEY_ALL_IRAN, true));
        }

        if (TextUtils.isEmpty(mSelectedCityId)) {
            mSelectedCityId = City.ALL_CITY_ID;
        }

        mSelectedCityName = City.ALL_CITY_NAME;

        mMinPriceHorizontalPicker = (HorizontalNumberPicker) this.findViewById(R.id.min_price);
        mMaxPriceHorizontalPicker = (HorizontalNumberPicker) this.findViewById(R.id.max_price);

        String priceList[] = getResources().getStringArray(R.array.price_values);
        int priceListSize = priceList.length;
        String perisanPriceList[] = new String[priceListSize];

        NumberFormat formatter = TextUtil.getPriceNumberFormmat(Locale.ENGLISH);
        for (int i = 0; i < priceListSize; i++) {
            if (i == priceListSize - 1) {
                perisanPriceList[i] = "بیشترین قیمت";
            } else {
                long rawPrice = Long.valueOf(priceList[i]);
                String formattedPrice = formatter.format(rawPrice);
                String price = TextUtil.convertEnNumberToFa(formattedPrice);
                perisanPriceList[i] = price;
            }
        }


        if (intent.hasExtra(HonarnamaBrowseApp.EXTRA_KEY_MIN_PRICE_INDEX)) {
            mMinPriceIndex = intent.getIntExtra(HonarnamaBrowseApp.EXTRA_KEY_MIN_PRICE_INDEX, 0);
        } else {
            mMinPriceIndex = 0;
        }

        if (intent.hasExtra(HonarnamaBrowseApp.EXTRA_KEY_MAX_PRICE_INDEX)) {
            mMaxPriceIndex = intent.getIntExtra(HonarnamaBrowseApp.EXTRA_KEY_MAX_PRICE_INDEX, priceListSize - 1);
        } else {
            mMaxPriceIndex = priceListSize - 1;
        }

        mMinPriceHorizontalPicker.setSpinnerValues(perisanPriceList);
        mMinPriceHorizontalPicker.setActualValues(priceList);
        mMinPriceHorizontalPicker.setSelectedIndex(mMinPriceIndex);

        mMaxPriceHorizontalPicker.setSpinnerValues(perisanPriceList);
        mMaxPriceHorizontalPicker.setActualValues(priceList);
        mMaxPriceHorizontalPicker.setSelectedIndex(mMaxPriceIndex);

        findViewById(R.id.apply_filter).setOnClickListener(this);
        findViewById(R.id.remove_filter).setOnClickListener(this);

        fetchProvincesAndCities();

        IconicsImageView closeButton = (IconicsImageView) findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        setIntent(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.province_edit_text:
                displayProvinceDialog();
                break;

            case R.id.city_edit_text:
                displayCityDialog();
                break;

            case R.id.apply_filter:
                if (!mMaxPriceHorizontalPicker.getActualSelectedValue().equals("MAX")) {
                    if (Integer.valueOf((String) mMinPriceHorizontalPicker.getActualSelectedValue()) >
                            Integer.valueOf((String) mMaxPriceHorizontalPicker.getActualSelectedValue())) {
                        Toast.makeText(ItemFilterDialogActivity.this,
                                getString(R.string.error_wrong_price_range) + " " + getString(R.string.error_min_price_greater_than_max_price), Toast.LENGTH_LONG).show();
                        break;
                    }
                }
                setFilters();
                break;

            case R.id.remove_filter:
                removeFilters();
                break;

            case R.id.refetchProvinces:
            case R.id.refetchCities:
                if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                    break;
                }
                mRefetchProvinces.setVisibility(View.GONE);
                mRefetchCities.setVisibility(View.GONE);
                fetchProvincesAndCities();
                break;

        }
    }


    private void displayProvinceDialog() {

        ListView provincesListView;
        ProvincesAdapter provincesAdapter;

        final Dialog provinceDialog = new Dialog(mActivity, R.style.DialogStyle);

        provinceDialog.setContentView(R.layout.choose_province);

        provincesListView = (ListView) provinceDialog.findViewById(net.honarnama.base.R.id.provinces_list_view);
        provincesAdapter = new ProvincesAdapter(mActivity, mProvincesObjectsTreeMap);
        provincesListView.setAdapter(provincesAdapter);

        provincesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedProvince = mProvincesObjectsTreeMap.get(position + 1);
                mSelectedProvinceId = mSelectedProvince.getObjectId();
                mSelectedProvinceName = mSelectedProvince.getName();
                mProvinceEditText.setText(mSelectedProvinceName);

                mAllIranCheckBox.setChecked(false);

                rePopulateCityList();
                if (provinceDialog.isShowing()) {
                    provinceDialog.dismiss();
                }
            }
        });
        provinceDialog.setCancelable(true);
        provinceDialog.setTitle(mActivity.getString(R.string.select_province));
        provinceDialog.show();
    }

    private void rePopulateCityList() {
        City city = new City();
        city.getOrderedCities(mActivity, mSelectedProvinceId).continueWith(new Continuation<TreeMap<Number, HashMap<String, String>>, Object>() {
            @Override
            public Object then(Task<TreeMap<Number, HashMap<String, String>>> task) throws Exception {
                if (task.isFaulted()) {
                    if ((mDialog.isShowing())) {
                        Toast.makeText(mActivity, mActivity.getString(R.string.error_getting_city_list) + mActivity.getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                    }
                } else {
                    mCityOrderedTreeMap = task.getResult();
                    for (HashMap<String, String> cityMap : mCityOrderedTreeMap.values()) {
                        for (Map.Entry<String, String> citySet : cityMap.entrySet()) {
                            mCityHashMap.put(citySet.getKey(), citySet.getValue());

                        }
                    }

                    mCityHashMap.put(City.ALL_CITY_ID, City.ALL_CITY_NAME);
                    HashMap<String, String> allCitiesHashMap = new HashMap<>();
                    allCitiesHashMap.put(City.ALL_CITY_ID, City.ALL_CITY_NAME);
                    mCityOrderedTreeMap.put(0, allCitiesHashMap);

                    Set<String> tempSet = mCityOrderedTreeMap.get(0).keySet();
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
        final CityAdapter cityAdapter;

        final Dialog cityDialog = new Dialog(mActivity, R.style.DialogStyle);
        cityDialog.setContentView(R.layout.choose_city);
        cityListView = (ListView) cityDialog.findViewById(net.honarnama.base.R.id.city_list_view);

        cityAdapter = new CityAdapter(mActivity, mCityOrderedTreeMap);
        cityListView.setAdapter(cityAdapter);
        cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                HashMap<String, String> selectedCity = mCityOrderedTreeMap.get(position);
                for (String key : selectedCity.keySet()) {
                    mSelectedCityId = key;
                }
                for (String value : selectedCity.values()) {
                    mSelectedCityName = value;
                    mCityEditEext.setText(mSelectedCityName);
                }

                mAllIranCheckBox.setChecked(false);

                if (cityDialog.isShowing()) {
                    cityDialog.dismiss();
                }
            }
        });
        cityDialog.setCancelable(true);
        cityDialog.setTitle(mActivity.getString(R.string.select_city));
        cityDialog.show();
    }


    public void setFilters() {
        Intent data = new Intent();
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_PROVINCE_ID, mSelectedProvinceId);
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_PROVINCE_NAME, mSelectedProvinceName);
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_CITY_ID, mSelectedCityId);
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_CITY_NAME, mSelectedCityName);
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_MIN_PRICE_INDEX, mMinPriceHorizontalPicker.getSelectedIndex());
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_MIN_PRICE_VALUE, mMinPriceHorizontalPicker.getActualSelectedValue());
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_MAX_PRICE_INDEX, mMaxPriceHorizontalPicker.getSelectedIndex());
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_MAX_PRICE_VALUE, mMaxPriceHorizontalPicker.getActualSelectedValue());
        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_ALL_IRAN, mAllIranCheckBox.isChecked());
        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_FILTER_APPLIED, true);
        setResult(RESULT_OK, data);
        finish();
    }

    public void removeFilters() {
        Intent data = new Intent();
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_PROVINCE_ID, "");
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_PROVINCE_NAME, "");
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_CITY_ID, "");
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_CITY_NAME, "");
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_MIN_PRICE_INDEX, -1);
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_MIN_PRICE_VALUE, "");
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_MAX_PRICE_INDEX, -1);
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_MAX_PRICE_VALUE, "");
        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_ALL_IRAN, true);
        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_FILTER_APPLIED, false);

        setResult(RESULT_OK, data);
        finish();
    }

    public void fetchProvincesAndCities() {
        final Provinces provinces = new Provinces();
        final City city = new City();

        mProvinceEditText.setHint(getString(R.string.getting_information));
        mCityEditEext.setHint(getString(R.string.getting_information));

        provinces.getOrderedProvinceObjects(HonarnamaBaseApp.getInstance()).
                continueWith(new Continuation<TreeMap<Number, Provinces>, Object>() {
                    @Override
                    public Object then(Task<TreeMap<Number, Provinces>> task) throws Exception {
                        if (task.isFaulted()) {
                            mRefetchProvinces.setVisibility(View.VISIBLE);
                            mRefetchCities.setVisibility(View.VISIBLE);
                            mProvinceEditText.setHint(ItemFilterDialogActivity.this.getString(R.string.error_occured));
                            logE("Getting Province Task Failed. Msg: " + task.getError().getMessage() + " // Error: " + task.getError(), "", task.getError());
                            Toast.makeText(ItemFilterDialogActivity.this, getString(R.string.error_getting_province_list) + getString(R.string.please_check_internet_connection), Toast.LENGTH_SHORT).show();
                        } else {
                            mProvincesObjectsTreeMap = task.getResult();
                            for (Provinces province : mProvincesObjectsTreeMap.values()) {
                                if (TextUtils.isEmpty(mSelectedProvinceId)) {
                                    mSelectedProvinceId = province.getObjectId();
                                }
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

                if (task.isFaulted()) {
                    mRefetchProvinces.setVisibility(View.VISIBLE);
                    mRefetchCities.setVisibility(View.VISIBLE);
                    mCityEditEext.setHint(ItemFilterDialogActivity.this.getString(R.string.error_occured));
                    logE("Getting City List Task Failed. Msg: " + task.getError().getMessage() + "//  Error: " + task.getError(), "", task.getError());
                    Toast.makeText(ItemFilterDialogActivity.this, getString(R.string.error_getting_city_list) + getString(R.string.please_check_internet_connection), Toast.LENGTH_SHORT).show();
                } else {
                    mCityOrderedTreeMap = task.getResult();

                    for (HashMap<String, String> cityMap : mCityOrderedTreeMap.values()) {
                        for (Map.Entry<String, String> citySet : cityMap.entrySet()) {
                            if (TextUtils.isEmpty(mSelectedCityId)) {
                                mSelectedCityId = citySet.getKey();
                            }
                            mCityHashMap.put(citySet.getKey(), citySet.getValue());
                        }
                    }

                    mCityHashMap.put(City.ALL_CITY_ID, City.ALL_CITY_NAME);
                    HashMap<String, String> allCitiesHashMap = new HashMap<String, String>();
                    allCitiesHashMap.put(City.ALL_CITY_ID, City.ALL_CITY_NAME);
                    mCityOrderedTreeMap.put(0, allCitiesHashMap);

                    mCityEditEext.setText(mCityHashMap.get(mSelectedCityId));

                }

                return null;
            }
        });

    }
}

