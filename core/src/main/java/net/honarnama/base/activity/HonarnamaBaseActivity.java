package net.honarnama.base.activity;

import com.crashlytics.android.Crashlytics;
import com.farsitel.bazaar.IUpdateCheckService;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.R;
import net.honarnama.base.dialog.CustomAlertDialog;
import net.honarnama.base.helper.MetaUpdater;
import net.honarnama.base.interfaces.MetaUpdateListener;
import net.honarnama.base.utils.CommonUtil;
import net.honarnama.nano.ReplyProperties;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import java.util.Date;

/**
 * Created by reza on 7/23/15.
 */
public abstract class HonarnamaBaseActivity extends AppCompatActivity {

    private boolean announced = false;
    public Dialog mAskToRateDialog;

    MetaUpdateListener mMetaUpdateListener;
    UpdateServiceConnection mUpdateConn;

    CustomAlertDialog mUpdateDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG && !announced) {
            Log.d(HonarnamaBaseApp.PRODUCTION_TAG, "Activity created,\tadb catlog tag:   'Honarnama/" + getLocalClassName() + ":V'");
            announced = true;
        }

        mMetaUpdateListener = new MetaUpdateListener() {
            @Override
            public void onMetaUpdateDone(int replyCode) {

                //getting the current time in milliseconds, and creating a Date object from it:
                Date date = new Date(System.currentTimeMillis()); //or simply new Date();

                //converting it back to a milliseconds representation:
                long millis = date.getTime();

                SharedPreferences.Editor editor = HonarnamaBaseApp.getAppSharedPref().edit();
                editor.putLong(HonarnamaBaseApp.PREF_KEY_META_CHECKED_TIME, millis);
                editor.commit();

                if (BuildConfig.DEBUG) {
                    logD("Meta Update replyCode: " + replyCode);
                }
                switch (replyCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:
                        displayUpgradeRequiredDialog();
                        break;
                }
            }
        };

        long lastVCodeCheckTime = HonarnamaBaseApp.getAppSharedPref()
                .getLong(HonarnamaBaseApp.PREF_KEY_VCODE_CHECKED_TIME, 0);

        if (System.currentTimeMillis() > lastVCodeCheckTime + 6 * 60 * 60 * 1000) {
            initUpdateService();
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
        return "";
    }

    public void logE(String sharedMsg, String debugMsg, Throwable throwable) {
        if (BuildConfig.DEBUG) {
            logE(getMessage(sharedMsg, debugMsg), throwable);
        } else if (sharedMsg != null) {
            logE(sharedMsg, throwable);
        }
    }

    public void logE(String sharedMsg, Throwable throwable) {
        if (BuildConfig.DEBUG) {
            Log.e(getDebugTag(), sharedMsg, throwable);
        } else if (sharedMsg != null) {
            Crashlytics.log(Log.ERROR, getDebugTag(), sharedMsg);
            if (throwable != null) {
                Crashlytics.logException(throwable);
            } else {
                Crashlytics.logException(new Throwable(sharedMsg));
            }
        }
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

    public void askToRate() {
        if (BuildConfig.DEBUG) {
            logD("HonarnamaBaseApp.PACKAGE_NAME: " + HonarnamaBaseApp.PACKAGE_NAME);
        }
        mAskToRateDialog = new Dialog(this, R.style.CustomDialogTheme);
        mAskToRateDialog.setCancelable(false);
        mAskToRateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mAskToRateDialog.setContentView(R.layout.ask_for_starts_dialog);
        Button letsRateBtn = (Button) mAskToRateDialog.findViewById(R.id.lets_rate);
        Button rateLaterBtn = (Button) mAskToRateDialog.findViewById(R.id.rate_later);
        letsRateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBazaarRatingIntent();
                SharedPreferences.Editor editor = HonarnamaBaseApp.getAppSharedPref().edit();
                if (HonarnamaBaseApp.PACKAGE_NAME.equals(HonarnamaBaseApp.SELL_PACKAGE_NAME)) {
                    editor.putBoolean(HonarnamaBaseApp.PREF_KEY_SELL_APP_RATED, true);
                } else {
                    editor.putBoolean(HonarnamaBaseApp.PREF_KEY_BROWSE_APP_RATED, true);
                }
                editor.commit();
                mAskToRateDialog.dismiss();
                finish();
            }
        });
        rateLaterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAskToRateDialog.dismiss();
                finish();
            }
        });
        mAskToRateDialog.show();
    }

    public void callBazaarRatingIntent() {
        if (CommonUtil.isPackageInstalled("com.farsitel.bazaar")) {
            Intent intent = new Intent(Intent.ACTION_EDIT);
            if (HonarnamaBaseApp.PACKAGE_NAME.equals(HonarnamaBaseApp.SELL_PACKAGE_NAME)) {
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

    public void callBazaarViewAppPageIntent(String packageName) {
        if (CommonUtil.isPackageInstalled("com.farsitel.bazaar")) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("bazaar://details?id=" + packageName));
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
                callBazaarViewAppPageIntent(HonarnamaBaseApp.PACKAGE_NAME);
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

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     */
    public static boolean checkAndAskStoragePermission(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
            return false;
        }
        return true;
    }

    public void checkAndUpdateMeta(boolean forceUpdate, long serverMetaVersion) {
        long metaVersion = HonarnamaBaseApp.getCurrentMetaVersion();
        if (forceUpdate || metaVersion == 0 || metaVersion < serverMetaVersion) {
            MetaUpdater metaUpdater = new MetaUpdater(mMetaUpdateListener, metaVersion);
            metaUpdater.execute();
        }
    }

//    public void runScheduledMetaUpdate() {
//        if (BuildConfig.DEBUG) {
//            logD("runScheduledMetaUpdate in background (onPause)");
//        }
//        long lastMetaCheckTime = HonarnamaBaseApp.getAppSharedPref()
//                .getLong(HonarnamaBaseApp.PREF_KEY_META_CHECKED_TIME, 0);
//
//        if (System.currentTimeMillis() > lastMetaCheckTime + 24 * 60 * 60 * 1000) {
//            checkAndUpdateMeta(true);
//        }
//    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAskToRateDialog != null && mAskToRateDialog.isShowing()) {
            try {
                mAskToRateDialog.dismiss();
            } catch (Exception ex) {

            }
        }
    }

    public class UpdateServiceConnection implements ServiceConnection {
        IUpdateCheckService service;

        public void onServiceConnected(ComponentName name, IBinder boundService) {
            service = IUpdateCheckService.Stub
                    .asInterface((IBinder) boundService);
            try {
                long vCode = service.getVersionCode(HonarnamaBaseApp.PACKAGE_NAME);
                if (BuildConfig.DEBUG) {
                    logD("bazaar vCode: " + vCode);
                }

                Date date = new Date(System.currentTimeMillis()); //or simply new Date();
                long millis = date.getTime();

                SharedPreferences.Editor editor = HonarnamaBaseApp.getAppSharedPref().edit();
                editor.putLong(HonarnamaBaseApp.PREF_KEY_VCODE_CHECKED_TIME, millis);
                editor.commit();

                if (vCode > 0) {
                    mUpdateDialog = new CustomAlertDialog(HonarnamaBaseActivity.this,
                            getString(R.string.update_app),
                            getString(R.string.want_to_update_app),
                            getString(R.string.yes),
                            getString(R.string.notnow)
                    );

                    mUpdateDialog.showDialog(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            callBazaarViewAppPageIntent(HonarnamaBaseApp.PACKAGE_NAME);
                            mUpdateDialog.dismiss();
                        }
                    });
                }
            } catch (Exception e) {
                logE("Exception getting version code of " + HonarnamaBaseApp.PACKAGE_NAME + " from bazaar.", e);
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            service = null;
            if (BuildConfig.DEBUG) {
                logD("onServiceDisconnected(): Disconnected");
            }
        }
    }

    private void initUpdateService() {
        if (BuildConfig.DEBUG) {
            logD("initService()");
        }
        mUpdateConn = new UpdateServiceConnection();
        Intent i = new Intent(
                "com.farsitel.bazaar.service.UpdateCheckService.BIND");
        i.setPackage("com.farsitel.bazaar");
        boolean ret = bindService(i, mUpdateConn, Context.BIND_AUTO_CREATE);
        if (BuildConfig.DEBUG) {
            logD("initService() bound value: " + ret);
        }
    }

    /** This is our function to un-binds this activity from our service. */
    private void releaseUpdateService() {
        try {
            if (mUpdateConn != null) {
                unbindService(mUpdateConn);
                mUpdateConn = null;
                if (BuildConfig.DEBUG) {
                    logD("releaseUpdateService(): unbound service done.");
                }
            }

        } catch (Exception ex) {
            logE("Exception trying to unbind update check service.", ex);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseUpdateService();
    }
}
