package net.honarnama.browse.dialog;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.HonarnamaBrowseActivity;
import net.honarnama.base.adapter.CityAdapter;
import net.honarnama.base.adapter.ProvincesAdapter;
import net.honarnama.base.model.City;
import net.honarnama.base.model.Province;
import net.honarnama.base.utils.NetworkManager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by elnaz on 3/2/16.
 */
public class EventFilterDialogActivity extends HonarnamaBrowseActivity implements View.OnClickListener {

    public Activity mActivity;
    private EditText mProvinceEditText;

    public int mSelectedProvinceId;
    public String mSelectedProvinceName;

    public Dialog mDialog;
    public int mSelectedCityId;
    public String mSelectedCityName;
    public TreeMap<Number, Province> mProvincesObjectsTreeMap = new TreeMap<Number, Province>();
    public HashMap<Integer, String> mProvincesHashMap = new HashMap<>();

    private EditText mCityEditEext;
    public TreeMap<Number, HashMap<Integer, String>> mCityOrderedTreeMap = new TreeMap<>();
    public HashMap<Integer, String> mCityHashMap = new HashMap<>();
    private Province mSelectedProvince;

    public CheckBox mAllIranCheckBox;

    IconicsImageView mRefetchProvinces;
    IconicsImageView mRefetchCities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_filter_dialog);

        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


        mActivity = EventFilterDialogActivity.this;
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

        final IconicsDrawable unCheckedDrawable = new IconicsDrawable(EventFilterDialogActivity.this)
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
                            new IconicsDrawable(EventFilterDialogActivity.this)
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
        mSelectedProvinceId = intent.getIntExtra(HonarnamaBrowseApp.EXTRA_KEY_PROVINCE_ID, -1);
        if (mSelectedProvinceId < 0) {
            mSelectedProvinceId = getDefaultLocationProvinceId();
        }
        mSelectedCityId = intent.getIntExtra(HonarnamaBrowseApp.EXTRA_KEY_CITY_ID, -1);
        if (mSelectedCityId < 0) {
            mSelectedCityId = getDefaultLocationCityId();
        }

        if (intent.hasExtra(HonarnamaBaseApp.EXTRA_KEY_ALL_IRAN)) {
            mAllIranCheckBox.setChecked(intent.getBooleanExtra(HonarnamaBaseApp.EXTRA_KEY_ALL_IRAN, true));
        }

        if (mSelectedCityId < 0) {
            mSelectedCityId = City.ALL_CITY_ID;
        }

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
                mSelectedProvinceId = mSelectedProvince.getId();
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
        city.getAllCitiesSorted(mSelectedProvinceId).continueWith(new Continuation<TreeMap<Number, HashMap<Integer, String>>, Object>() {
            @Override
            public Object then(Task<TreeMap<Number, HashMap<Integer, String>>> task) throws Exception {
                if (task.isFaulted()) {
                    if ((mDialog.isShowing())) {
                        Toast.makeText(mActivity, mActivity.getString(R.string.error_getting_city_list) + mActivity.getString(R.string.check_net_connection), Toast.LENGTH_LONG).show();
                    }
                } else {
                    mCityOrderedTreeMap = task.getResult();
                    for (HashMap<Integer, String> cityMap : mCityOrderedTreeMap.values()) {
                        for (Map.Entry<Integer, String> citySet : cityMap.entrySet()) {
                            mCityHashMap.put(citySet.getKey(), citySet.getValue());

                        }
                    }

                    mCityHashMap.put(City.ALL_CITY_ID, City.ALL_CITY_NAME);
                    HashMap<Integer, String> allCitiesHashMap = new HashMap<>();
                    allCitiesHashMap.put(City.ALL_CITY_ID, City.ALL_CITY_NAME);
                    mCityOrderedTreeMap.put(0, allCitiesHashMap);

                    Set<Integer> tempSet = mCityOrderedTreeMap.get(0).keySet();
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
        final CityAdapter cityAdapter;

        final Dialog cityDialog = new Dialog(mActivity, R.style.DialogStyle);
        cityDialog.setContentView(R.layout.choose_city);
        cityListView = (ListView) cityDialog.findViewById(net.honarnama.base.R.id.city_list_view);

        cityAdapter = new CityAdapter(mActivity, mCityOrderedTreeMap);
        cityListView.setAdapter(cityAdapter);
        cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<Integer, String> selectedCity = mCityOrderedTreeMap.get(position);
                for (int key : selectedCity.keySet()) {
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
        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_PROVINCE_ID, mSelectedProvinceId);
        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_PROVINCE_NAME, mSelectedProvinceName);
        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_CITY_ID, mSelectedCityId);
        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_CITY_NAME, mSelectedCityName);
        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_FILTER_APPLIED, true);
        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_ALL_IRAN, mAllIranCheckBox.isChecked());
        setResult(RESULT_OK, data);
        finish();
    }

    public void removeFilters() {
        Intent data = new Intent();
        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_PROVINCE_ID, "");
        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_PROVINCE_NAME, "");
        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_CITY_ID, "");
        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_CITY_NAME, "");
        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_FILTER_APPLIED, false);
        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_ALL_IRAN, true);
        setResult(RESULT_OK, data);
        finish();
    }

    public void fetchProvincesAndCities() {
        findViewById(R.id.apply_filter).setOnClickListener(this);
        findViewById(R.id.remove_filter).setOnClickListener(this);

        final Province provinces = new Province();
        final City city = new City();

        mProvinceEditText.setHint(getString(R.string.select));
        mCityEditEext.setHint(getString(R.string.getting_information));

        provinces.getAllProvincesSorted().
                continueWith(new Continuation<TreeMap<Number, Province>, Object>() {
                    @Override
                    public Object then(Task<TreeMap<Number, Province>> task) throws Exception {
                        if (task.isFaulted()) {
                            mRefetchProvinces.setVisibility(View.VISIBLE);
                            mRefetchCities.setVisibility(View.VISIBLE);
                            mProvinceEditText.setHint(EventFilterDialogActivity.this.getString(R.string.error_occured));
                            logE("Getting Province Task Failed. Msg: " + task.getError().getMessage() + " // Error: " + task.getError(), task.getError());
                            Toast.makeText(EventFilterDialogActivity.this, getString(R.string.error_getting_province_list) + getString(R.string.check_net_connection), Toast.LENGTH_SHORT).show();
                        } else {
                            mProvincesObjectsTreeMap = task.getResult();
                            for (Province province : mProvincesObjectsTreeMap.values()) {
                                if (mSelectedProvinceId < 0) {
                                    mSelectedProvinceId = province.getId();
                                }
                                mProvincesHashMap.put(province.getId(), province.getName());
                            }
                            mProvinceEditText.setText(mProvincesHashMap.get(mSelectedProvinceId));
                        }
                        return null;
                    }
                }).continueWithTask(new Continuation<Object, Task<TreeMap<Number, HashMap<Integer, String>>>>() {
            @Override
            public Task<TreeMap<Number, HashMap<Integer, String>>> then(Task<Object> task) throws Exception {
                return city.getAllCitiesSorted(mSelectedProvinceId);
            }
        }).continueWith(new Continuation<TreeMap<Number, HashMap<Integer, String>>, Object>() {
            @Override
            public Object then(Task<TreeMap<Number, HashMap<Integer, String>>> task) throws Exception {
                if (task.isFaulted()) {
                    mRefetchProvinces.setVisibility(View.VISIBLE);
                    mRefetchCities.setVisibility(View.VISIBLE);
                    mCityEditEext.setHint(EventFilterDialogActivity.this.getString(R.string.error_occured));
                    logE("Getting City List Task Failed. Msg: " + task.getError().getMessage() + "//  Error: " + task.getError(), task.getError());
                    Toast.makeText(EventFilterDialogActivity.this, getString(R.string.error_getting_city_list) + getString(R.string.check_net_connection), Toast.LENGTH_SHORT).show();
                } else {
                    mCityOrderedTreeMap = task.getResult();
                    for (HashMap<Integer, String> cityMap : mCityOrderedTreeMap.values()) {
                        for (Map.Entry<Integer, String> citySet : cityMap.entrySet()) {
                            if (mSelectedCityId < 0) {
                                mSelectedCityId = citySet.getKey();
                            }
                            mCityHashMap.put(citySet.getKey(), citySet.getValue());
                        }
                    }

                    mCityHashMap.put(City.ALL_CITY_ID, City.ALL_CITY_NAME);
                    HashMap<Integer, String> allCitiesHashMap = new HashMap<>();
                    allCitiesHashMap.put(City.ALL_CITY_ID, City.ALL_CITY_NAME);
                    mCityOrderedTreeMap.put(0, allCitiesHashMap);

                    mCityEditEext.setText(mCityHashMap.get(mSelectedCityId));

                }
                return null;
            }
        });

    }
}

