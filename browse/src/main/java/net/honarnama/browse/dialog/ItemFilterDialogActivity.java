package net.honarnama.browse.dialog;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.HonarnamaBrowseActivity;
import net.honarnama.browse.widget.HorizontalNumberPicker;
import net.honarnama.base.adapter.CityAdapter;
import net.honarnama.base.adapter.ProvincesAdapter;
import net.honarnama.base.model.City;
import net.honarnama.base.model.Province;
import net.honarnama.base.utils.NetworkManager;
import net.honarnama.base.utils.TextUtil;

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

    HorizontalNumberPicker mMinPriceHorizontalPicker;
    HorizontalNumberPicker mMaxPriceHorizontalPicker;

    public EditText mSearchEditText;
    public int mMinPriceIndex;
    public int mMaxPriceIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_filter_dialog);

        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        mActivity = ItemFilterDialogActivity.this;

        Intent intent = getIntent();

        mMinPriceHorizontalPicker = (HorizontalNumberPicker) this.findViewById(R.id.min_price);
        mMaxPriceHorizontalPicker = (HorizontalNumberPicker) this.findViewById(R.id.max_price);

        mSearchEditText = (EditText) findViewById(R.id.serach_term);

        if (intent.hasExtra(HonarnamaBrowseApp.EXTRA_KEY_SEARCH_TERM)) {
            mSearchEditText.setText(intent.getStringExtra(HonarnamaBrowseApp.EXTRA_KEY_SEARCH_TERM));
        } else {
            mSearchEditText.setText("");
        }

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

        IconicsImageView closeButton = (IconicsImageView) findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        checkAndUpdateMeta();
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

        }
    }

    public void setFilters() {
        Intent data = new Intent();
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_MIN_PRICE_INDEX, mMinPriceHorizontalPicker.getSelectedIndex());
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_MIN_PRICE_VALUE, mMinPriceHorizontalPicker.getActualSelectedValue());
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_MAX_PRICE_INDEX, mMaxPriceHorizontalPicker.getSelectedIndex());
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_MAX_PRICE_VALUE, mMaxPriceHorizontalPicker.getActualSelectedValue());
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_SEARCH_TERM, mSearchEditText.getText().toString().trim());
        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_FILTER_APPLIED, true);
        setResult(RESULT_OK, data);
        finish();
    }

    public void removeFilters() {
        Intent data = new Intent();
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_MIN_PRICE_INDEX, -1);
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_MIN_PRICE_VALUE, "");
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_MAX_PRICE_INDEX, -1);
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_MAX_PRICE_VALUE, "");
        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_FILTER_APPLIED, false);
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_SEARCH_TERM, "");

        setResult(RESULT_OK, data);
        finish();
    }

}

