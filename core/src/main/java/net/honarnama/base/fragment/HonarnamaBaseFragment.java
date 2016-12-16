package net.honarnama.base.fragment;

import com.crashlytics.android.Crashlytics;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * Created by reza on 11/23/15.
 */
public abstract class HonarnamaBaseFragment extends Fragment {

    private boolean announced = false;
    public Context mContext;
    ProgressDialog mProgressDialog;

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
        Log.d(getDebugTag(), getMessage(sharedMsg, debugMsg));
    }

    public void logD(String debugMsg) {
        if (BuildConfig.DEBUG) {
            Log.d(getDebugTag(), getMessage(null, debugMsg));
        }
    }


    public void displayLongToast(String message) {
        try {
            Activity activity = getActivity();
            if (isAdded() && activity != null && !activity.isFinishing()) {
                Toast.makeText(getFragmentContext(), message, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            logD("Exception displaying dialog. e: " + e);
        }
    }

    public void displayShortToast(String message) {
        try {
            Activity activity = getActivity();
            if (isAdded() && activity != null && !activity.isFinishing()) {
                Toast.makeText(getFragmentContext(), message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            logD("Exception displaying dialog. e: " + e);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public void displayProgressDialog(DialogInterface.OnDismissListener onDismissListener) {
        try {
            dismissProgressDialog();
            Activity activity = getActivity();
            if (activity != null && !activity.isFinishing() && isAdded()) {
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setMessage(getString(R.string.please_wait));

                if (onDismissListener != null) {
                    mProgressDialog.setOnDismissListener(onDismissListener);
                }

                mProgressDialog.show();
            }
        } catch (Exception e) {
            logE("Exception displaying dialog. e: " + e, e);
        }
    }

    public void dismissProgressDialog() {
        Activity activity = getActivity();
        if (isAdded() && activity != null && !activity.isFinishing()) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    }

    public String getStringInFragment(int stringId) {
        return getFragmentContext().getResources().getString(stringId);
    }

    public Context getFragmentContext() {
        if (isAdded() && getActivity() != null) {
            return getActivity();
        } else {
            return HonarnamaBaseApp.getInstance();
        }
    }

    public void setTextInFragment(EditText editText, String text) {
        if (editText != null && isAdded()) {
            editText.setText(text);
        }
    }

    public void setTextInFragment(Button button, String text) {
        if (button != null && isAdded()) {
            button.setText(text);
        }
    }

    public void setTextInFragment(TextView textView, String text) {
        if (textView != null && isAdded()) {
            textView.setText(text);
        }
    }

    public void setSelectionInFragment(Spinner spinner, int pos) {
        if (spinner != null && isAdded()) {
            spinner.setSelection(pos);
        }
    }

    public void setCheckedInFragment(RadioButton radioButton, boolean checked) {
        if (radioButton != null && isAdded()) {
            radioButton.setChecked(checked);
        }
    }

    public void setCheckedInFragment(ToggleButton toggleButton, boolean checked) {
        if (toggleButton != null && isAdded()) {
            toggleButton.setChecked(checked);
        }
    }

    public String getTextInFragment(EditText editText) {
        if (editText != null && isAdded()) {
            return editText.getText().toString().trim();
        }
        return "";
    }


    public String getTextInFragment(Button button) {
        if (button != null && isAdded()) {
            return button.getText().toString().trim();
        }
        return "";
    }

    public void setErrorInFragment(EditText editText, String errorMsg) {
        if (editText != null && isAdded()) {
            if (TextUtils.isEmpty(errorMsg)) {
                editText.setError(null);
            } else {
                editText.setError(errorMsg);
            }
        }
    }

    public void setErrorInFragment(TextView textView, String errorMsg) {
        if (textView != null && isAdded()) {
            if (TextUtils.isEmpty(errorMsg)) {
                textView.setError(null);
            } else {
                textView.setError(errorMsg);
            }
        }
    }

    public void requestFocusInFragment(TextView textView) {
        if (textView != null && isAdded()) {
            textView.requestFocus();
        }
    }

    public void requestFocusInFragment(EditText editText) {
        if (editText != null && isAdded()) {
            editText.requestFocus();
        }
    }

    public void requestFocusInFragment(Spinner spinner) {
        if (spinner != null && isAdded()) {
            spinner.requestFocus();
        }
    }

    public void setVisibilityInFragment(View view, int visibility) {
        if (view != null && isAdded()) {
            view.setVisibility(visibility);
        }
    }

}
