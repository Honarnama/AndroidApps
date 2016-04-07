package net.honarnama.sell.fragments;


import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.honarnama.GRPCUtils;
import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.nano.Account;
import net.honarnama.nano.AuthServiceGrpc;
import net.honarnama.nano.CreateAccountReply;
import net.honarnama.nano.CreateAccountRequest;
import net.honarnama.nano.CreateOrUpdateAccountRequest;
import net.honarnama.nano.ReplyProperties;
import net.honarnama.nano.RequestProperties;
import net.honarnama.nano.SimpleRequest;
import net.honarnama.nano.UpdateAccountReply;
import net.honarnama.nano.WhoAmIReply;
import net.honarnama.sell.activity.ControlPanelActivity;
import net.honarnama.sell.activity.LoginActivity;
import net.honarnama.sell.model.HonarnamaUser;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import io.fabric.sdk.android.services.concurrency.AsyncTask;


public class UserAccountFragment extends HonarnamaBaseFragment implements View.OnClickListener {

    public static UserAccountFragment mUserAccountFragment;
    //TODO update gender in server
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
    public String getTitle(Context context) {
        return context.getString(R.string.nav_title_seller_account);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //TODO check if user is not logged in, return him/her to login page
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
        //TODO test changing gender
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
        ProgressDialog progressDialog;
        String name;
        int genderCode;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            genderCode = mGenderWoman.isChecked() ? CreateAccountRequest.FEMALE : (mGenderMan.isChecked() ? CreateAccountRequest.MALE : CreateAccountRequest.UNSPECIFIED);
            name = mNameEditText.getText().toString().trim();

            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getString(R.string.please_wait));
            progressDialog.show();
        }

        @Override
        protected UpdateAccountReply doInBackground(Void... voids) {
            final CreateOrUpdateAccountRequest createOrUpdateAccountRequest = new CreateOrUpdateAccountRequest();
            createOrUpdateAccountRequest.account = new Account();
            createOrUpdateAccountRequest.account.id = HonarnamaUser.getId();
            createOrUpdateAccountRequest.account.name = name;
            createOrUpdateAccountRequest.account.gender = genderCode;

            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            createOrUpdateAccountRequest.requestProperties = rp;

            AuthServiceGrpc.AuthServiceBlockingStub stub;
            try {
                stub = GRPCUtils.getInstance().getAuthServiceGrpc();
            } catch (InterruptedException ie) {
                logE("Error occured trying to send register request. Error:" + ie);
                return null;
            }

            UpdateAccountReply updateAccountReply = stub.updateAccount(createOrUpdateAccountRequest);
            return updateAccountReply;
        }

        @Override
        protected void onPostExecute(UpdateAccountReply updateAccountReply) {
            super.onPostExecute(updateAccountReply);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (updateAccountReply != null) {
                logE("inja updateAccountReply is: " + updateAccountReply);
                switch (updateAccountReply.replyProperties.statusCode) {

                    case ReplyProperties.CLIENT_ERROR:
                        switch (ReplyProperties.CLIENT_ERROR) {
                            case UpdateAccountReply.NO_CLIENT_ERROR:
                                break;
                            case UpdateAccountReply.ACCOUNT_NOT_FOUND:
                                //TODO
                                logE("inja ACCOUNT_NOT_FOUND");
                                break;
                            case UpdateAccountReply.FORBIDDEN:
                                //TODO
                                logE("inja FORBIDDEN");
                                break;
                        }
                        //TODO check ErrorCode
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        //TODO
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        //TODO toast
                        HonarnamaUser.logout(getActivity());
                        break;

                    case ReplyProperties.OK:
                        HonarnamaUser.setName(name);
                        HonarnamaUser.setGender(genderCode);
                        //TODO toast
                        break;
                }
            } else {
                //TODO toast
            }
        }
    }
}
