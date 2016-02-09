package net.honarnama.core.activity;

import com.crashlytics.android.Crashlytics;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

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
        Log.d(getDebugTag(), getMessage(sharedMsg, debugMsg));
    }

    public void logD(String debugMsg) {
        Log.d(getDebugTag(), getMessage(null, debugMsg));
    }
}
