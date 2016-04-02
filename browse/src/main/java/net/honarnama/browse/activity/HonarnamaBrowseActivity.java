package net.honarnama.browse.activity;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.core.activity.HonarnamaBaseActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

/**
 * Created by elnaz on 2/11/16.
 */
public class HonarnamaBrowseActivity extends HonarnamaBaseActivity {
    public final static String SELECTED_TAB_EXTRA_KEY = "selectedTabIndex";

    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = getSharedPreferences(HonarnamaBaseApp.BROWSE_APP_KEY, Context.MODE_PRIVATE);
    }

    public int getDefaultLocationProvinceId() {
        return mSharedPreferences.getInt(HonarnamaBaseApp.EXTRA_KEY_DEFAULT_LOCATION_PROVINCE_ID, 0);
    }

    public int getDefaultLocationCityId() {
        return mSharedPreferences.getInt(HonarnamaBaseApp.EXTRA_KEY_DEFAULT_LOCATION_CITY_ID, 0);
    }


    public String getDefaultLocationProvinceName() {
        return mSharedPreferences.getString(HonarnamaBaseApp.EXTRA_KEY_DEFAULT_LOCATION_PROVINCE_NAME, "");
    }

    public String getDefaultLocationCityName() {
        return mSharedPreferences.getString(HonarnamaBaseApp.EXTRA_KEY_DEFAULT_LOCATION_CITY_NAME, "");
    }

}
