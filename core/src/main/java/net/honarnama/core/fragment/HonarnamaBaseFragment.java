package net.honarnama.core.fragment;

import com.crashlytics.android.Crashlytics;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by reza on 11/23/15.
 */
public abstract class HonarnamaBaseFragment extends Fragment {

    private boolean announced = false;

    abstract public String getTitle(Context context);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG && !announced) {
            Log.d(HonarnamaBaseApp.PRODUCTION_TAG, "Fragment created,\tadb catlog tag:   'Honarnama/" + getLocalClassName() + ":V'");
            announced = true;
        }
    }

    public String getLocalClassName() {
        String pkg = "";
        if (getActivity() != null) {
            pkg = getActivity().getPackageName();
        }
        String cls = "";
        if (getClass() != null) {
            cls = getClass().getCanonicalName();
        }
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

    String getMessage(String sharedMsg, String debugMsg) {
        if ((debugMsg != null) && BuildConfig.DEBUG) {
            String message;
            if (sharedMsg != null) {
                message = sharedMsg + " // " + debugMsg;
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
        Log.e(getDebugTag(), getMessage(sharedMsg, debugMsg));
    }

    public void logE(String sharedMsg) {
        logE(sharedMsg, null);
    }

    public void logI(String sharedMsg, String debugMsg) {
        Log.i(getDebugTag(), getMessage(sharedMsg, debugMsg));
    }

    public void logD(String sharedMsg, String debugMsg) {
        Log.d(getDebugTag(), getMessage(sharedMsg, debugMsg));
    }

    public void logD(String debugMsg) {
        if (BuildConfig.DEBUG) {
            Log.d(getDebugTag(), getMessage(null, debugMsg));
        }
    }


    public void displayLongToast(String message) {
        if (isVisible()) {
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        }
    }

    public void displayShortToast(String message) {
        if (isVisible()) {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
