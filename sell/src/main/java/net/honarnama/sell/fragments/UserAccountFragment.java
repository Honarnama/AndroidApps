package net.honarnama.sell.fragments;


import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.honarnama.GRPCUtils;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.nano.Account;
import net.honarnama.nano.AuthServiceGrpc;
import net.honarnama.nano.CreateOrUpdateAccountRequest;
import net.honarnama.nano.ReplyProperties;
import net.honarnama.nano.RequestProperties;
import net.honarnama.nano.UpdateAccountReply;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;
import net.honarnama.sell.model.HonarnamaUser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

import io.fabric.sdk.android.services.concurrency.AsyncTask;


public class UserAccountFragment extends HonarnamaBaseFragment implements View.OnClickListener {

    public static UserAccountFragment mUserAccountFragment;
    private EditText mNameEditText;
    private Button mAlterNameButton;

    private ToggleButton mGenderWoman;
    private ToggleButton mGenderMan;
    private ToggleButton mGenderUnspecified;

    private Tracker mTracker;

    ProgressDialog mProgressDialog;

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

        if (!HonarnamaUser.isLoggedIn()) {
            if (BuildConfig.DEBUG) {
                logD("User was not logged in!");
            }
            HonarnamaUser.logout(getActivity());
        }

        View rootView = inflater.inflate(R.layout.fragment_user_account, container, false);
        mNameEditText = (EditText) rootView.findViewById(R.id.account_name_edit_text);
        mAlterNameButton = (Button) rootView.findViewById(R.id.alter_account_info_btn);
        mAlterNameButton.setOnClickListener(this);

        mGenderWoman = (ToggleButton) rootView.findViewById(R.id.account_gender_woman);
        mGenderMan = (ToggleButton) rootView.findViewById(R.id.account_gender_man);
        mGenderUnspecified = (ToggleButton) rootView.findViewById(R.id.account_gender_not_said);

        mGenderWoman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGenderWoman.setChecked(true);
                mGenderMan.setChecked(false);
                mGenderUnspecified.setChecked(false);
            }
        });
        mGenderMan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGenderMan.setChecked(true);
                mGenderWoman.setChecked(false);
                mGenderUnspecified.setChecked(false);
            }
        });

        mGenderUnspecified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGenderUnspecified.setChecked(true);
                mGenderWoman.setChecked(false);
                mGenderMan.setChecked(false);
            }
        });
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
        mNameEditText.setText(HonarnamaUser.getName());
        mGenderWoman.setChecked(false);
        mGenderMan.setChecked(false);
        mGenderUnspecified.setChecked(false);

        switch (HonarnamaUser.getGender()) {
            case Account.FEMALE:
                mGenderWoman.setChecked(true);
                break;
            case Account.MALE:
                mGenderMan.setChecked(true);
                break;
            case Account.UNSPECIFIED:
                mGenderUnspecified.setChecked(true);
                break;
        }


    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.alter_account_info_btn:
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
        }

    }

    private void changeUserProfile() {
        if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
            return;
        } else {
            new UpdateAccountAsync().execute();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

    public class UpdateAccountAsync extends AsyncTask<Void, Void, UpdateAccountReply> {
        String name;
        int genderCode;
        CreateOrUpdateAccountRequest createOrUpdateAccountRequest;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            genderCode = mGenderWoman.isChecked() ? Account.FEMALE : (mGenderMan.isChecked() ? Account.MALE : Account.UNSPECIFIED);
            name = mNameEditText.getText().toString().trim();

            displayProgressDialog();
        }

        @Override
        protected UpdateAccountReply doInBackground(Void... voids) {
            createOrUpdateAccountRequest = new CreateOrUpdateAccountRequest();
            createOrUpdateAccountRequest.account = new Account();
            createOrUpdateAccountRequest.account.id = HonarnamaUser.getId();
            createOrUpdateAccountRequest.account.name = name;
            createOrUpdateAccountRequest.account.gender = genderCode;

            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            createOrUpdateAccountRequest.requestProperties = rp;
            if (BuildConfig.DEBUG) {
                logD("createOrUpdateAccountRequest is: " + createOrUpdateAccountRequest);
            }
            AuthServiceGrpc.AuthServiceBlockingStub stub;
            try {
                stub = GRPCUtils.getInstance().getAuthServiceGrpc();
            } catch (Exception e) {
                logE("Error running createOrUpdateAccountRequest. createOrUpdateAccountRequest: " + createOrUpdateAccountRequest + ". Error:" + e, e);
                return null;
            }

            UpdateAccountReply updateAccountReply = stub.updateAccount(createOrUpdateAccountRequest);
            return updateAccountReply;
        }

        @Override
        protected void onPostExecute(UpdateAccountReply updateAccountReply) {
            super.onPostExecute(updateAccountReply);

            if (BuildConfig.DEBUG) {
                logD("updateAccountReply is: " + updateAccountReply);
            }

            dismissProgressDialog();
            if (updateAccountReply != null) {
                switch (updateAccountReply.replyProperties.statusCode) {

                    case ReplyProperties.CLIENT_ERROR:
                        switch (ReplyProperties.CLIENT_ERROR) {
                            case UpdateAccountReply.NO_CLIENT_ERROR:
                                logE("Got NO_CLIENT_ERROR code updateAccountReply. createOrUpdateAccountRequest: " + createOrUpdateAccountRequest + ". user Id: " + HonarnamaUser.getId());
                                displayShortToast(getString(R.string.error_occured));
                                break;
                            case UpdateAccountReply.ACCOUNT_NOT_FOUND:
                                displayLongToast(getString(R.string.account_not_found));
                                break;
                            case UpdateAccountReply.FORBIDDEN:
                                displayLongToast(getString(R.string.not_allowed_to_do_this_action));
                                logE("Got FORBIDDEN reply while trying to update user " + HonarnamaUser.getId() + ". createOrUpdateAccountRequest: " + createOrUpdateAccountRequest);
                                break;
                        }
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        displayShortToast(getString(R.string.server_error_try_again));
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        HonarnamaUser.logout(getActivity());
                        break;

                    case ReplyProperties.OK:
                        HonarnamaUser.setName(name);
                        HonarnamaUser.setGender(genderCode);
                        displayShortToast(getString(R.string.successfully_changed_user_info));
                        break;
                }
            } else {
                displayLongToast(getString(R.string.error_connecting_to_Server) + getString(R.string.check_net_connection));
            }
        }
    }

    private void dismissProgressDialog() {
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    }

    private void displayProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(getString(R.string.please_wait));
        }
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing() && isVisible()) {
            mProgressDialog.show();
        }
    }
}
