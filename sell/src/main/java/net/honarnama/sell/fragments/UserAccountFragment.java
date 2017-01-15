package net.honarnama.sell.fragments;


import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.honarnama.GRPCUtils;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.fragment.HonarnamaBaseFragment;
import net.honarnama.base.utils.NetworkManager;
import net.honarnama.nano.Account;
import net.honarnama.nano.AuthServiceGrpc;
import net.honarnama.nano.CreateOrUpdateAccountRequest;
import net.honarnama.nano.ReplyProperties;
import net.honarnama.nano.RequestProperties;
import net.honarnama.nano.UpdateAccountReply;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;
import net.honarnama.sell.activity.ControlPanelActivity;
import net.honarnama.sell.model.HonarnamaUser;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

    public synchronized static UserAccountFragment getInstance() {
        if (mUserAccountFragment == null) {
            mUserAccountFragment = new UserAccountFragment();
        }
        return mUserAccountFragment;
    }

    @Override
    public String getTitle() {
        return getStringInFragment(R.string.nav_title_seller_account);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Activity activity = getActivity();
        if (!HonarnamaUser.isLoggedIn()) {
            if (BuildConfig.DEBUG) {
                logD("User was not logged in!");
            }
            HonarnamaUser.logout(activity);
            displayLongToast(getStringInFragment(R.string.login_again));
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

        setErrorInFragment(mNameEditText, "");

        mTracker = HonarnamaSellApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("AccountFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        setUserInfo();

        if (isAdded()) {
            ControlPanelActivity activity = (ControlPanelActivity) getActivity();
            if (activity != null) {
                activity.setTitle(getTitle());
            }
        }

    }

    public void setUserInfo() {

        setTextInFragment(mNameEditText, HonarnamaUser.getName());
        setCheckedInFragment(mGenderWoman, false);
        setCheckedInFragment(mGenderMan, false);
        setCheckedInFragment(mGenderUnspecified, false);

        switch (HonarnamaUser.getGender()) {
            case Account.FEMALE:
                setCheckedInFragment(mGenderWoman, true);
                break;
            case Account.MALE:
                setCheckedInFragment(mGenderMan, true);
                break;
            case Account.UNSPECIFIED:
                setCheckedInFragment(mGenderUnspecified, true);
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
                if (getTextInFragment(mNameEditText).length() > 0) {
                    changeUserProfile();
                } else {
                    requestFocusInFragment(mNameEditText);
                    setErrorInFragment(mNameEditText, getStringInFragment(R.string.error_name_not_set));
                    displayShortToast(getStringInFragment(R.string.error_name_not_set));
                }
                break;
        }

    }

    private void changeUserProfile() {
        if (NetworkManager.getInstance().isNetworkEnabled(true)) {
            new UpdateAccountAsync().execute();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

    public class UpdateAccountAsync extends AsyncTask<Void, Void, UpdateAccountReply> {
        String name;
        int genderCode = -1;
        CreateOrUpdateAccountRequest createOrUpdateAccountRequest;
        String cToastMsg = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            createOrUpdateAccountRequest = new CreateOrUpdateAccountRequest();
            createOrUpdateAccountRequest.account = new Account();

            if (!isAdded() || mGenderWoman == null) {
                return;
            }
            genderCode = mGenderWoman.isChecked() ? Account.FEMALE : (mGenderMan.isChecked() ? Account.MALE : Account.UNSPECIFIED);
            name = getTextInFragment(mNameEditText);

            createOrUpdateAccountRequest.account.name = name;
            createOrUpdateAccountRequest.account.gender = genderCode;

            displayProgressDialog(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (!TextUtils.isEmpty(cToastMsg)) {
                        displayLongToast(cToastMsg);
                        cToastMsg = "";
                    }
                }
            });
        }

        @Override
        protected UpdateAccountReply doInBackground(Void... voids) {
            if (genderCode == -1) {
                return null;
            }
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            createOrUpdateAccountRequest.requestProperties = rp;
            if (BuildConfig.DEBUG) {
                logD("createOrUpdateAccountRequest is: " + createOrUpdateAccountRequest);
            }
            AuthServiceGrpc.AuthServiceBlockingStub stub;
            try {
                stub = GRPCUtils.getInstance().getAuthServiceGrpc();
                UpdateAccountReply updateAccountReply = stub.updateAccount(createOrUpdateAccountRequest);
                return updateAccountReply;
            } catch (Exception e) {
                logE("Error running createOrUpdateAccountRequest. createOrUpdateAccountRequest: " + createOrUpdateAccountRequest + ". Error:" + e, e);
                return null;
            }

        }

        @Override
        protected void onPostExecute(UpdateAccountReply updateAccountReply) {
            super.onPostExecute(updateAccountReply);

            Activity activity = getActivity();

            if (BuildConfig.DEBUG) {
                logD("updateAccountReply is: " + updateAccountReply);
            }

            if (updateAccountReply != null) {
                switch (updateAccountReply.replyProperties.statusCode) {
                    case ReplyProperties.CLIENT_ERROR:
                        switch (ReplyProperties.CLIENT_ERROR) {
                            case UpdateAccountReply.NO_CLIENT_ERROR:
                                logE("Got NO_CLIENT_ERROR code updateAccountReply. createOrUpdateAccountRequest: " + createOrUpdateAccountRequest + ". user Id: " + HonarnamaUser.getId());
                                cToastMsg = getStringInFragment(R.string.error_getting_info);
                                break;
                            case UpdateAccountReply.ACCOUNT_NOT_FOUND:
                                cToastMsg = getStringInFragment(R.string.account_not_found);
                                break;
                            case UpdateAccountReply.FORBIDDEN:
                                logE("Got FORBIDDEN reply while trying to update user " + HonarnamaUser.getId() + ". createOrUpdateAccountRequest: " + createOrUpdateAccountRequest);
                                cToastMsg = getStringInFragment(R.string.not_allowed_to_do_this_action);
                                break;
                        }
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        logE("Server error upon UpdateAccountAsync. request: " + createOrUpdateAccountRequest);
                        cToastMsg = getStringInFragment(R.string.server_error_try_again);
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        HonarnamaUser.logout(activity);
                        displayLongToast(getStringInFragment(R.string.login_again));
                        break;

                    case ReplyProperties.OK:
                        HonarnamaUser.setName(name);
                        HonarnamaUser.setGender(genderCode);
                        cToastMsg = getStringInFragment(R.string.successfully_changed_user_info);
                        break;
                }
            } else {
                cToastMsg = getStringInFragment(R.string.error_connecting_server_try_again);
            }

            dismissProgressDialog();
        }
    }
}
