package net.honarnama.sell.activity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import net.honarnama.core.activity.HonarnamaBaseActivity;

import android.app.Dialog;
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

    public void checkGooglePlayAvailability() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            logE("GooglePlayServices is not available. ConnectionResult: " + status);
            ((Dialog) GooglePlayServicesUtil.getErrorDialog(status, this, 10)).show();
        }
    }
}
