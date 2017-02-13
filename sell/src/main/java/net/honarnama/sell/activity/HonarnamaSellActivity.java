package net.honarnama.sell.activity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;

import net.honarnama.base.activity.HonarnamaBaseActivity;
import net.honarnama.base.dialog.CustomAlertDialog;
import net.honarnama.sell.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

/**
 * Created by elnaz on 5/26/16.
 */
public class HonarnamaSellActivity extends HonarnamaBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        checkGooglePlayServicesUpdate();

        if (isGooglePlayAvailable()) {
            if (checkGooglePlayServicesUpdate()) {
                updateAndroidSecurityProvider();
            }
        }
    }

    public boolean isGooglePlayAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            logD("GooglePlayServices is not available. ConnectionResult: " + status);
//            ((Dialog) GooglePlayServicesUtil.getErrorDialog(status, this, 10)).show();
            displayGetGPlayDialog();
            return false;
        }
        return true;
    }

    public boolean checkGooglePlayServicesUpdate() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (status == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            logD("GooglePlayServices is not updated!");
//            ((Dialog) GooglePlayServicesUtil.getErrorDialog(status, this, 10)).show();
            displayGetGPlayDialog();
            return false;
        }
        return true;
    }

    private void updateAndroidSecurityProvider() {
        logD("updateAndroidSecurityProvider");
        try {
            ProviderInstaller.installIfNeeded(this);
        } catch (GooglePlayServicesRepairableException e) {
            // Thrown when Google Play Services is not installed, up-to-date, or enabled
            // Show dialog to allow users to install, update, or otherwise enable Google Play services.
//            GooglePlayServicesUtil.getErrorDialog(e.getConnectionStatusCode(), this, 0);
            displayGetGPlayDialog();
        } catch (GooglePlayServicesNotAvailableException e) {
            logE("SecurityException, Google Play Services not available.");
            displayGetGPlayDialog();
        }
    }


    private void displayGetGPlayDialog() {
        final CustomAlertDialog alertDialog = new CustomAlertDialog(this,
                getString(R.string.gplay_not_found),
                getString(R.string.install_gplay_for_proper_functioning),
                getString(R.string.get_gplay),
                getString(R.string.bikhial)
        );
        alertDialog.showDialog(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGPlayFromBazaar();
                alertDialog.dismiss();
            }
        });
    }


    private void getGPlayFromBazaar() {
        Intent appIntent = new Intent(Intent.ACTION_VIEW);
        appIntent.setData(Uri.parse("bazaar://details?id=" + "com.google.android.gms"));
        appIntent.setPackage("com.farsitel.bazaar");
        startActivity(appIntent);
    }

}
