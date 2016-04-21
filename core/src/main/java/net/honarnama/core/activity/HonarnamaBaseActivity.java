package net.honarnama.core.activity;

import com.crashlytics.android.Crashlytics;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.R;
import net.honarnama.core.utils.CommonUtil;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by reza on 7/23/15.
 */
public abstract class HonarnamaBaseActivity extends AppCompatActivity {

    private boolean announced = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG && !announced) {
            Log.d(HonarnamaBaseApp.PRODUCTION_TAG, "Activity created,\tadb catlog tag:   'Honarnama/" + getLocalClassName() + ":V'");
            announced = true;
        }
    }

    String getDebugTag() {
        if (BuildConfig.DEBUG) {
            return HonarnamaBaseApp.PRODUCTION_TAG + "/" + getLocalClassName();
        } else {
            return HonarnamaBaseApp.PRODUCTION_TAG;
        }
    }

    String getMessage(String sharedMsg, String debugMsg) {
        if ((debugMsg != null) && BuildConfig.DEBUG) {
            String message;
            if (sharedMsg != null) {
                message = sharedMsg + " //  debugMsg: " + debugMsg;
            } else {
                message = debugMsg;
            }
            return message;
        } else if (sharedMsg != null) {
            return sharedMsg;
        }
        return null;
    }

    public void logE(String sharedMsg, String debugMsg, Throwable throwable) {
        if (BuildConfig.DEBUG) {
            Log.e(getDebugTag(), getMessage(sharedMsg, debugMsg), throwable);
        } else if (sharedMsg != null) {
            Crashlytics.log(Log.ERROR, getDebugTag(), sharedMsg);
        }
    }

    public void logE(String sharedMsg, String debugMsg) {
        logE(sharedMsg, debugMsg, null);
    }

    public void logE(String sharedMsg) {
        logE(sharedMsg, null, null);
    }

    public void logI(String sharedMsg, String debugMsg) {
        Log.i(getDebugTag(), getMessage(sharedMsg, debugMsg));
    }

    public void logD(String sharedMsg, String debugMsg) {
        if (BuildConfig.DEBUG) {
            Log.d(getDebugTag(), getMessage(sharedMsg, debugMsg));
        }
    }

    public void logD(String debugMsg) {
        if (BuildConfig.DEBUG) {
            Log.d(getDebugTag(), getMessage(null, debugMsg));
        }
    }

    public void askToRate(final String callingApp) {
        final Dialog dialog = new Dialog(this, R.style.CustomDialogTheme);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.ask_for_starts_dialog);
        Button letsRateBtn = (Button) dialog.findViewById(R.id.lets_rate);
        Button rateLaterBtn = (Button) dialog.findViewById(R.id.rate_later);
        letsRateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBazaarRatingIntent(callingApp);
                SharedPreferences.Editor editor = HonarnamaBaseApp.getCommonSharedPref().edit();
                if (callingApp == HonarnamaBaseApp.PREF_NAME_SELL_APP) {
                    editor.putBoolean(HonarnamaBaseApp.PREF_KEY_SELL_APP_RATED, true);
                } else {
                    editor.putBoolean(HonarnamaBaseApp.PREF_KEY_BROWSE_APP_RATED, true);
                }
                editor.commit();
                dialog.dismiss();
                finish();
            }
        });
        rateLaterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });
        dialog.show();
    }

    public void callBazaarRatingIntent(final String callingApp) {
        if (CommonUtil.isPackageInstalled("com.farsitel.bazaar", this)) {
            Intent intent = new Intent(Intent.ACTION_EDIT);
            if (HonarnamaBaseApp.PACKAGE_NAME == HonarnamaBaseApp.SELL_PACKAGE_NAME) {
                intent.setData(Uri.parse("bazaar://details?id=" + HonarnamaBaseApp.SELL_PACKAGE_NAME));
            } else {
                intent.setData(Uri.parse("bazaar://details?id=" + HonarnamaBaseApp.BROWSE_PACKAGE_NAME));
            }
            intent.setPackage("com.farsitel.bazaar");
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.bazaar_is_not_installed, Toast.LENGTH_LONG).show();
        }
    }

    public void callBazaarViewAppPageIntent() {
        if (CommonUtil.isPackageInstalled("com.farsitel.bazaar", this)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (HonarnamaBaseApp.PACKAGE_NAME == HonarnamaBaseApp.SELL_PACKAGE_NAME) {
                intent.setData(Uri.parse("bazaar://details?id=" + HonarnamaBaseApp.SELL_PACKAGE_NAME));
            } else {
                intent.setData(Uri.parse("bazaar://details?id=" + HonarnamaBaseApp.BROWSE_PACKAGE_NAME));
            }
            intent.setPackage("com.farsitel.bazaar");
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.bazaar_is_not_installed, Toast.LENGTH_LONG).show();
        }
    }

    public void displayUpgradeRequiredDialog() {
        final Dialog dialog = new Dialog(this, R.style.CustomDialogTheme);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.upgrade_required_dialog);
        Button letsRateBtn = (Button) dialog.findViewById(R.id.lets_rate);
        Button rateLaterBtn = (Button) dialog.findViewById(R.id.rate_later);
        letsRateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBazaarViewAppPageIntent();
                dialog.dismiss();
            }
        });
        rateLaterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}
