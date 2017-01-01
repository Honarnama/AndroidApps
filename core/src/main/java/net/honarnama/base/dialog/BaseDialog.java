package net.honarnama.base.dialog;

import net.honarnama.base.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public abstract class BaseDialog {

    private final AlertDialog mDialog;

    private final Activity mActivity;

    private String mTitle;

    private View mContentView;

    private DialogTaskListener mDialogTaskListener;

    public BaseDialog(Activity activity, String title, boolean cancelable) {
        mActivity = activity;
        mContentView = LayoutInflater.from(activity).inflate(R.layout.dialog_default, null);
        mTitle = title;
        TextView titleTextView = (TextView) mContentView.findViewById(R.id.title);
        if (mTitle != null && !TextUtils.isEmpty(mTitle)) {
            titleTextView.setText(mTitle);
        } else {
            titleTextView.setVisibility(View.GONE);
        }
        ContextThemeWrapper wrapper = new ContextThemeWrapper(activity, R.style.DialogStyle);
        AlertDialog.Builder builder = new AlertDialog.Builder(wrapper);
        mDialog = builder.setCancelable(cancelable).setView(mContentView).create();
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setInverseBackgroundForced(true);
    }

    public BaseDialog(Activity activity, int titleResId, boolean cancelable) {
        this(activity, activity.getString(titleResId), cancelable);
    }

    public BaseDialog(Activity activity, String title) {
        this(activity, title, true);
    }

    public BaseDialog(Activity activity, int titleResId) {
        this(activity, activity.getString(titleResId));
    }

    public BaseDialog(Activity activity) {
        this(activity, null);
    }

    protected void setContent(View view) {
        FrameLayout content = (FrameLayout) mContentView.findViewById(R.id.dialog_content);
        content.addView(view, FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
    }

    protected void setPositiveButton(int titleResId, View.OnClickListener onClickListener) {
        if (titleResId != 0 && onClickListener != null) {
            Button button = (Button) mContentView.findViewById(R.id.button_positive);
            String buttonTitle = mActivity.getString(titleResId);
            buttonTitle = buttonTitle.toUpperCase();
            button.setText(buttonTitle);
            button.setOnClickListener(onClickListener);
            button.setVisibility(View.VISIBLE);
        }
    }

    protected void setNeutralButton(int titleResId, View.OnClickListener onClickListener) {
        if (titleResId != 0 && onClickListener != null) {
            Button button = (Button) mContentView.findViewById(R.id.button_neutral);
            String buttonTitle = mActivity.getString(titleResId);
            buttonTitle = buttonTitle.toUpperCase();
            button.setText(buttonTitle);
            button.setOnClickListener(onClickListener);
            button.setVisibility(View.VISIBLE);
        }
    }

    protected void setNegativeButton(int titleResId, View.OnClickListener onClickListener) {
        if (titleResId != 0 && onClickListener != null) {
            Button button = (Button) mContentView.findViewById(R.id.button_negative);
            String buttonTitle = mActivity.getString(titleResId);
            buttonTitle = buttonTitle.toUpperCase();
            button.setText(buttonTitle);
            button.setOnClickListener(onClickListener);
            button.setVisibility(View.VISIBLE);
        }
    }

    public Context getContext() {
        return mActivity;
    }

    public Activity getActivity() {
        return mActivity;
    }

    public void setDialogTaskListener(DialogTaskListener dialogTaskListener) {
        mDialogTaskListener = dialogTaskListener;
    }

    public void onDialogTaskSuccess() {
        if (mDialogTaskListener != null) {
            mDialogTaskListener.onTaskSuccess();
        }
    }

    public void onDialogTaskFailure() {
        if (mDialogTaskListener != null) {
            mDialogTaskListener.onTaskFailure();
        }
    }

    public void show() {
        mDialog.show();
    }

    public void dismiss() {
        mDialog.dismiss();
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener dismissListener) {
        mDialog.setOnDismissListener(dismissListener);
    }

    public void setOnCancelListener(DialogInterface.OnCancelListener cancelListener) {
        mDialog.setOnCancelListener(cancelListener);
    }

    /**
     * this is used when dialog is doing some kind of task which is dependent to calling activity,
     * like any net task which dialog can do
     */
    public static interface DialogTaskListener {

        /**
         * if task was successful
         */
        public void onTaskSuccess();

        /**
         * if task failed
         */
        public void onTaskFailure();
    }

}
