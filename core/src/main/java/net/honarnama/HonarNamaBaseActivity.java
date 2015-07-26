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

    String getMessage(String productionMsg, String debugMsg) {
        if ((debugMsg != null) && BuildConfig.DEBUG) {
            String message;
            if (productionMsg != null) {
                message = productionMsg + " // " + debugMsg;
            } else {
                message = debugMsg;
            }
            return message;
        } else if (productionMsg != null) {
            return productionMsg;
        }
        return null;
    }

    public void logE(String productionMsg, String debugMsg, Throwable throwable) {
        if (BuildConfig.DEBUG) {
            Log.e(getTag(), getMessage(productionMsg, debugMsg), throwable);
        } else if (productionMsg != null) {
            Log.e(getTag(), productionMsg);
        }
    }

    public void logE(String productionMsg, String debugMsg) {
        Log.e(getTag(), getMessage(productionMsg, debugMsg));
    }

    public void logE(String message) {
        logE(message, null);
    }

    public void logI(String productionMsg, String debugMsg) {
        Log.i(getTag(), getMessage(productionMsg, debugMsg));
    }

    public void logD(String productionMsg, String debugMsg) {
        Log.d(getTag(), getMessage(productionMsg, debugMsg));
    }

}
