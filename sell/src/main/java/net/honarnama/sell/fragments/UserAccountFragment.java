package net.honarnama.sell.fragments;


import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.core.utils.GenericGravityTextWatcher;
import net.honarnama.core.utils.HonarnamaUser;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;


public class UserAccountFragment extends HonarnamaBaseFragment implements View.OnClickListener {

    public static UserAccountFragment mUserAccountFragment;

    private EditText mNameEditText;
    private Button mAlterNameButton;
    private ParseUser mCurrentUser;
    private EditText mNewPasswordEditText;

    private Button mChangePasswordButton;
    private RelativeLayout mPasswordLayout;

    private ToggleButton mGenderWoman;
    private ToggleButton mGenderMan;
    private ToggleButton mGenderNotSaid;

    private Tracker mTracker;

    public synchronized static UserAccountFragment getInstance() {
        if (mUserAccountFragment == null) {
            mUserAccountFragment = new UserAccountFragment();
        }
        return mUserAccountFragment;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.nav_title_seller_account);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mCurrentUser = ParseUser.getCurrentUser();
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_user_account, container, false);
        mPasswordLayout = (RelativeLayout) rootView.findViewById(R.id.account_password_layer);
        mNameEditText = (EditText) rootView.findViewById(R.id.account_name_edit_text);
        mAlterNameButton = (Button) rootView.findViewById(R.id.account_alter_name_button);
        mAlterNameButton.setOnClickListener(this);

        mNewPasswordEditText = (EditText) rootView.findViewById(R.id.account_new_password_edit_text);
        mChangePasswordButton = (Button) rootView.findViewById(R.id.account_alter_password_button);
        mChangePasswordButton.setOnClickListener(this);

        mGenderWoman = (ToggleButton) rootView.findViewById(R.id.account_gender_woman);
        mGenderMan = (ToggleButton) rootView.findViewById(R.id.account_gender_man);
        mGenderNotSaid = (ToggleButton) rootView.findViewById(R.id.account_gender_not_said);

        if (HonarnamaUser.getActivationMethod() == HonarnamaUser.ActivationMethod.EMAIL) {
            mPasswordLayout.setVisibility(View.VISIBLE);
            mChangePasswordButton.setVisibility(View.VISIBLE);
        }


        mGenderWoman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGenderWoman.setChecked(true);
                mGenderMan.setChecked(false);
                mGenderNotSaid.setChecked(false);
            }
        });
        mGenderMan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGenderMan.setChecked(true);
                mGenderWoman.setChecked(false);
                mGenderNotSaid.setChecked(false);
            }
        });

        mGenderNotSaid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGenderNotSaid.setChecked(true);
                mGenderWoman.setChecked(false);
                mGenderMan.setChecked(false);
            }
        });
        mNewPasswordEditText.addTextChangedListener(new GenericGravityTextWatcher(mNewPasswordEditText));
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mTracker = HonarnamaSellApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("AccountFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        setUserInfo();
    }

    public void setUserInfo() {
        mNameEditText.setText(mCurrentUser.getString("name"));
        mGenderWoman.setChecked(false);
        mGenderMan.setChecked(false);
        mGenderNotSaid.setChecked(false);
        switch (mCurrentUser.getInt("gender")) {
            case HonarnamaBaseApp.GENDER_CODE_WOMAN:
                mGenderWoman.setChecked(true);
                break;
            case HonarnamaBaseApp.GENDER_CODE_MAN:
                mGenderMan.setChecked(true);
                break;
            default:
                mGenderNotSaid.setChecked(true);
        }

        mNewPasswordEditText.setText("");
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.account_alter_name_button:
                if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                    return;
                }
                if (mNameEditText.getText().toString().trim().length() > 0) {
                    changeUserProfile();
                } else {
                    mNameEditText.requestFocus();
                    mNameEditText.setError(getString(R.string.error_name_not_set));
                }
                break;
            case R.id.account_alter_password_button:
                if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                    return;
                }
                if (mNewPasswordEditText.getText().toString().trim().length() > 0) {
                    changePassword();
                } else {
                    mNewPasswordEditText.requestFocus();
                    mNewPasswordEditText.setError(getString(R.string.error_password_field_can_not_be_empty));
                }
                break;
        }

    }

    private void changeUserProfile() {
        final ProgressDialog sendingDataProgressDialog = new ProgressDialog(getActivity());
        sendingDataProgressDialog.setCancelable(false);
        sendingDataProgressDialog.setMessage(getString(R.string.sending_data));
        sendingDataProgressDialog.show();
        mCurrentUser.put("name", mNameEditText.getText().toString().trim());
        int genderCode = mGenderWoman.isChecked() ? 0 : (mGenderMan.isChecked() ? 1 : 2);
        mCurrentUser.put("gender", genderCode);

        mCurrentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                sendingDataProgressDialog.dismiss();
                if (e == null) {
                    if (isVisible()) {
                        Toast.makeText(getActivity(), getString(R.string.message_profile_changed_successfully), Toast.LENGTH_LONG).show();
                    }
                    mCurrentUser.pinInBackground();
                } else {
                    if (isVisible()) {
                        Toast.makeText(getActivity(), getString(R.string.message_altering_profile_Failed), Toast.LENGTH_LONG).show();
                    }
                    logE("Error changing user name. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e, "", e);
                }

            }
        });
    }

    private void changePassword() {
        final ProgressDialog sendingDataProgressDialog = new ProgressDialog(getActivity());
        sendingDataProgressDialog.setCancelable(false);
        sendingDataProgressDialog.setMessage(getString(R.string.sending_data));
        sendingDataProgressDialog.show();

        mCurrentUser.setPassword(mNewPasswordEditText.getText().toString());
        mCurrentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                sendingDataProgressDialog.dismiss();
                if (null == e) {
                    if (isVisible()) {
                        Toast.makeText(getActivity(), getString(R.string.successfully_changed_password), Toast.LENGTH_LONG).show();
                    }
                } else {
                    if (isVisible()) {
                        Toast.makeText(getActivity(), getString(R.string.changing_password_failed), Toast.LENGTH_LONG).show();
                    }
                    logE("Error changing password. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e, "", e);
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

}
