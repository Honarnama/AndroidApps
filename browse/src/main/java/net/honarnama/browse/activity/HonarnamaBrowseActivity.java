package net.honarnama.browse.activity;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.activity.HonarnamaBaseActivity;
import net.honarnama.base.helper.MetaUpdater;
import net.honarnama.base.interfaces.MetaUpdateListener;
import net.honarnama.base.model.City;
import net.honarnama.base.model.Province;
import net.honarnama.nano.ReplyProperties;

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
        mSharedPreferences = getSharedPreferences(HonarnamaBaseApp.PREF_NAME_BROWSE_APP, Context.MODE_PRIVATE);
    }

    public int getUserLocationProvinceId() {
        return mSharedPreferences.getInt(HonarnamaBaseApp.PREF_KEY_DEFAULT_LOCATION_PROVINCE_ID, Province.ALL_PROVINCE_ID);
    }

    public int getUserLocationCityId() {
        return mSharedPreferences.getInt(HonarnamaBaseApp.PREF_KEY_DEFAULT_LOCATION_CITY_ID, City.ALL_CITY_ID);
    }


    public String getUserLocationProvinceName() {
        return mSharedPreferences.getString(HonarnamaBaseApp.PREF_KEY_DEFAULT_LOCATION_PROVINCE_NAME, "");
    }

    public String getUserLocationCityName() {
        return mSharedPreferences.getString(HonarnamaBaseApp.PREF_KEY_DEFAULT_LOCATION_CITY_NAME, "");
    }

    public void checkAndUpdateMeta() {
        MetaUpdateListener metaUpdateListener = new MetaUpdateListener() {
            @Override
            public void onMetaUpdateDone(int replyCode) {
                if (net.honarnama.base.BuildConfig.DEBUG) {
                    logD("Meta Update replyCode: " + replyCode);
                }
                switch (replyCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:
                        displayUpgradeRequiredDialog();
                        break;
                }
            }
        };
        long metaVersion = getSharedPreferences(HonarnamaBaseApp.PREF_NAME_BROWSE_APP, Context.MODE_PRIVATE).getLong(HonarnamaBaseApp.PREF_KEY_META_VERSION, 0);
        MetaUpdater metaUpdater = new MetaUpdater(metaUpdateListener, metaVersion);
        metaUpdater.execute();
    }


}
