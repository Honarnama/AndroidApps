package net.honarnama;

import net.honarnama.base.BuildConfig;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by reza on 11/23/15.
 */
public abstract class HonarnamaBaseFragment extends Fragment {

    private static boolean announced = false;

    abstract public String getTitle(Context context);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG && !announced) {
            Log.d(HonarnamaBaseApp.PRODUCTION_TAG, "Fragment created,\tadb catlog tag: 'Honarnama/" + getLocalClassName() + ":V'");
            announced = true;
        }
    }

    public String getLocalClassName() {
        final String pkg = getActivity().getPackageName();
        final String cls = getClass().getCanonicalName();
        int packageLen = pkg.length();
        if (!cls.startsWith(pkg) || cls.length() <= packageLen
                || cls.charAt(packageLen) != '.') {
            return cls;
        }
        return cls.substring(packageLen + 1);
    }

    String getDebugTag() {
        if (BuildConfig.DEBUG) {
            return HonarnamaBaseApp.PRODUCTION_TAG + "/" + getLocalClassName();
        } else {
            return HonarnamaBaseApp.PRODUCTION_TAG;
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
            Log.e(getDebugTag(), getMessage(productionMsg, debugMsg), throwable);
        } else if (productionMsg != null) {
            Log.e(getDebugTag(), productionMsg);
        }
    }

    public void logE(String productionMsg, String debugMsg) {
        Log.e(getDebugTag(), getMessage(productionMsg, debugMsg));
    }

    public void logE(String message) {
        logE(message, null);
    }

    public void logI(String productionMsg, String debugMsg) {
        Log.i(getDebugTag(), getMessage(productionMsg, debugMsg));
    }

    public void logD(String productionMsg, String debugMsg) {
        Log.d(getDebugTag(), getMessage(productionMsg, debugMsg));
    }

}
