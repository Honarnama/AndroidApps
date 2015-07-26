package net.honarnama;

import net.honarnama.base.BuildConfig;

import android.app.Activity;
import android.util.Log;

/**
 * Created by reza on 7/23/15.
 */
public class HonarNamaBaseActivity extends Activity {

    String getTag() {
        if (BuildConfig.DEBUG) {
            return HonarNamaBaseApp.PRODUCTION_TAG + "/" + getLocalClassName();
        } else {
            return HonarNamaBaseApp.PRODUCTION_TAG;
        }
    }

    public void logE(String productionMsg, String debugMsg, Throwable throwable) {
        if ((debugMsg != null) && BuildConfig.DEBUG) {
            String message;
            if (productionMsg != null) {
                message = productionMsg + " // " + debugMsg;
            } else {
                message = debugMsg;
            }
            Log.e(getTag(), message, throwable);
        } else if (productionMsg != null) {
            Log.e(getTag(), productionMsg);
        }
    }

    public void logE(String productionMsg, String debugMsg) {
        if ((debugMsg != null) && BuildConfig.DEBUG) {
            String message;
            if (productionMsg != null) {
                message = productionMsg + " // " + debugMsg;
            } else {
                message = debugMsg;
            }
            Log.e(getTag(), message);
        } else if (productionMsg != null) {
            Log.e(getTag(), productionMsg);
        }
    }

    public void logE(String message) {
        logE(message, message);
    }
    public void logI(String productionMsg, String debugMsg) {
        if ((debugMsg != null) && BuildConfig.DEBUG) {
            String message;
            if (productionMsg != null) {
                message = productionMsg + " // " + debugMsg;
            } else {
                message = debugMsg;
            }
            Log.i(getTag(), message);
        } else if (productionMsg != null) {
            Log.i(getTag(), productionMsg);
        }
    }

}
