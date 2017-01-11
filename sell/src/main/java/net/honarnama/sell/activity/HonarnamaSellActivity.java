package net.honarnama.sell.activity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.activity.HonarnamaBaseActivity;
import net.honarnama.base.helper.MetaUpdater;
import net.honarnama.base.interfaces.MetaUpdateListener;
import net.honarnama.nano.ReplyProperties;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by elnaz on 5/26/16.
 */
public class HonarnamaSellActivity extends HonarnamaBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        checkGooglePlayServicesUpdate();
    }

    public void checkGooglePlayServicesUpdate() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (status == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            logE("GooglePlayServices is not updated!");
            ((Dialog) GooglePlayServicesUtil.getErrorDialog(status, this, 10)).show();
        }
    }


    public void checkAndUpdateMeta() {
        MetaUpdateListener metaUpdateListener = new MetaUpdateListener() {
            @Override
            public void onMetaUpdateDone(int replyCode) {
                if (net.honarnama.base.BuildConfig.DEBUG) {
                    logD("Sell Meta Update replyCode: " + replyCode);
                }
                switch (replyCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:
                        displayUpgradeRequiredDialog();
                        break;
                }
            }
        };
        long metaVersion = getSharedPreferences(HonarnamaBaseApp.PREF_NAME_SELL_APP, Context.MODE_PRIVATE).getLong(HonarnamaBaseApp.PREF_KEY_META_VERSION, 0);
        MetaUpdater metaUpdater = new MetaUpdater(metaUpdateListener, metaVersion);
        metaUpdater.execute();
    }


//    public void checkGooglePlayAvailability() {
//        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
//        if (status != ConnectionResult.SUCCESS) {
//            logE("GooglePlayServices is not available. ConnectionResult: " + status);
//            ((Dialog) GooglePlayServicesUtil.getErrorDialog(status, this, 10)).show();
//        }
//    }
}
