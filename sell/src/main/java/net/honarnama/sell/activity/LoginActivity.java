package net.honarnama.sell.activity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.honarnama.GRPCUtils;
import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.utils.GravityTextWatcher;
import net.honarnama.base.utils.NetworkManager;
import net.honarnama.base.utils.WindowUtil;
import net.honarnama.nano.AuthServiceGrpc;
import net.honarnama.nano.ReplyProperties;
import net.honarnama.nano.RequestProperties;
import net.honarnama.nano.SendLoginEmailReply;
import net.honarnama.nano.SendLoginEmailRequest;
import net.honarnama.nano.SimpleRequest;
import net.honarnama.nano.WhoAmIReply;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;
import net.honarnama.sell.model.HonarnamaUser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

//TODO resend login link
public class LoginActivity extends HonarnamaSellActivity implements View.OnClickListener {
    private Button mRegisterAsSellerBtn;
    private Button mLoginButton;
    private EditText mUsernameEditText;

    ProgressDialog mProgressDialog;
    Snackbar mSnackbar;
    private Tracker mTracker;
    private CoordinatorLayout mCoordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) {
            logD("LoginActivity created.");
        }

        mTracker = HonarnamaSellApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("LoginActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        setContentView(R.layout.activity_login);
        mRegisterAsSellerBtn = (Button) findViewById(R.id.register_as_seller_btn);
        mRegisterAsSellerBtn.setOnClickListener(this);

        mLoginButton = (Button) findViewById(R.id.send_login_link_btn);
        mLoginButton.setOnClickListener(this);

        mUsernameEditText = (EditText) findViewById(R.id.login_username_edit_text);

        mUsernameEditText.addTextChangedListener(new GravityTextWatcher(mUsernameEditText));

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .coordinatorLayout);

        if (HonarnamaUser.isLoggedIn()) {
            new getMeAsyncTask().execute();
        } else {
            processIntent(getIntent());
        }

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    private void processIntent(Intent intent) {
        Uri data = intent.getData();
        if (BuildConfig.DEBUG) {
            logD("processIntent :: data= " + data);
        }

        if (data != null) {
            final String loginToken = data.getQueryParameter("token");  //login with email
            final String register = data.getQueryParameter("register");
            if (BuildConfig.DEBUG) {
                logD("token= " + loginToken + ", register= " + register);
            }
            if (loginToken != null && loginToken.length() > 0) {
                HonarnamaUser.login(loginToken);
                if (BuildConfig.DEBUG) {
                    logD("getUserInfo (Calling getMeAsyncTask...)");
                }
                if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                    return;
                } else {
                    new getMeAsyncTask().execute();
                }
            } else if ("true".equals(register)) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(registerIntent, HonarnamaBaseApp.INTENT_REGISTER_CODE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        WindowUtil.hideKeyboard(LoginActivity.this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_as_seller_btn:
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(intent, HonarnamaBaseApp.INTENT_REGISTER_CODE);
                break;
            case R.id.send_login_link_btn:
                if (formInputsAreValid()) {
                    new SendLoginEmailAsync().execute();
                }
                break;
            default:
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (BuildConfig.DEBUG) {
            logD("onActivityResult//resultCode: " + resultCode);
        }

        if (resultCode == Activity.RESULT_OK) {
            if (BuildConfig.DEBUG) {
                logD("onActivityResult::RESULT_OK");
            }
            if (requestCode == HonarnamaBaseApp.INTENT_REGISTER_CODE) {
                if (BuildConfig.DEBUG) {
                    logD("requestCode: " + HonarnamaBaseApp.INTENT_REGISTER_CODE);
                }
                if (intent.hasExtra(HonarnamaBaseApp.EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_EMAIL)) {
                    if (intent.getBooleanExtra(HonarnamaBaseApp.EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_EMAIL, false)) {
                        if (BuildConfig.DEBUG) {
                            logD("display email verification notif.");
                        }
                        displayCustomSnackbar(getString(R.string.verification_email_sent), false);
                        clearErrors();
                    }
                }
            } else {
                if (BuildConfig.DEBUG) {
                    logD("Hide register message notif.");
                }
            }

        }
        super.onActivityResult(requestCode, resultCode, intent);
    }


    public class getMeAsyncTask extends AsyncTask<Void, Void, WhoAmIReply> {
        SimpleRequest simpleRequest;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            displayProgressDialog();
        }

        @Override
        protected WhoAmIReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            simpleRequest = new SimpleRequest();
            simpleRequest.requestProperties = rp;

            if (BuildConfig.DEBUG) {
                logD("Running WhoAmI request to log user in. simpleRequest: " + simpleRequest);
            }
            WhoAmIReply whoAmIReply;
            try {
                AuthServiceGrpc.AuthServiceBlockingStub stub = GRPCUtils.getInstance().getAuthServiceGrpc();
                whoAmIReply = stub.whoAmI(simpleRequest);
                return whoAmIReply;
            } catch (Exception e) {
                logE("Error getting whoAmIReply. simpleRequest: " + simpleRequest + ". Error: " + e, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(WhoAmIReply whoAmIReply) {
            super.onPostExecute(whoAmIReply);
            if (BuildConfig.DEBUG) {
                logD("whoAmIReply: " + whoAmIReply);
            }
            dismissProgressDialog();
            if (whoAmIReply != null) {
                switch (whoAmIReply.replyProperties.statusCode) {

                    case ReplyProperties.CLIENT_ERROR:
                        logE("Got CLIENT_ERROR for whoAmIReply. whoAmIReply: " + whoAmIReply + ". simpleRequest was: " + simpleRequest);
                        Toast.makeText(LoginActivity.this, getString(R.string.error_getting_info), Toast.LENGTH_LONG).show();
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        displayCustomSnackbar(getString(R.string.server_error_try_again), true);
                        logE("Got SERVER_ERROR for whoAmIReply. whoAmIReply: " + whoAmIReply + ". simpleRequest was: " + simpleRequest);
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        //TODO (1:Login link expired or 2: 24 user deleted and user got deleted)
                        if (BuildConfig.DEBUG) {
                            logD("Got NOT_AUTHORIZED reply in reply to WhoAmI request.");
                        }
                        Toast.makeText(LoginActivity.this, getString(R.string.account_not_found) + " یا اعتبار لینک ورود منقضی شده است.", Toast.LENGTH_LONG).show();
                        HonarnamaUser.logout(null);
                        displayCustomSnackbar("در صورتی که حسابتان را قبلا فعال کرده بودید، می‌توانید از طریق فرم بالا، درخواست لینک ورود جدید کنید.", false);
                        break;

                    case ReplyProperties.OK:
                        HonarnamaUser.setName(whoAmIReply.account.name);
                        HonarnamaUser.setGender(whoAmIReply.account.gender);
                        goToControlPanel();
                        break;

                    case ReplyProperties.UPGRADE_REQUIRED:
                        displayUpgradeRequiredDialog();
                        HonarnamaUser.logout(null);
                        break;
                }

            } else {
                displayCustomSnackbar(getString(R.string.error_connecting_server_try_again), true);
            }
        }
    }

    public void goToControlPanel() {
        Intent intent = new Intent(LoginActivity.this, ControlPanelActivity.class);
        startActivity(intent);
        finish();
    }

    private void dismissProgressDialog() {
        if (!LoginActivity.this.isFinishing()) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                try {
                    mProgressDialog.dismiss();
                } catch (Exception ex) {

                }
            }
        }
    }

    private void displayProgressDialog() {
        dismissProgressDialog();
        mProgressDialog = new ProgressDialog(LoginActivity.this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.please_wait));
        if (!LoginActivity.this.isFinishing() && !mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    public void displayCustomSnackbar(String text, Boolean displayGetMeRefreshBtn) {
        if (mSnackbar != null && mSnackbar.isShown()) {
            mSnackbar.dismiss();
        }

        mSnackbar = Snackbar.make(mCoordinatorLayout, "", Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) mSnackbar.getView();
        TextView textView = (TextView) snackbarLayout.findViewById(android.support.design.R.id.snackbar_text);
        textView.setVisibility(View.INVISIBLE);

        View snackView = getLayoutInflater().inflate(R.layout.snackbar, null);
        TextView textViewTop = (TextView) snackView.findViewById(R.id.snack_text);
        textViewTop.setText(text);

        ImageButton imageBtn = (ImageButton) snackView.findViewById(R.id.snack_action);
        if (displayGetMeRefreshBtn) {
            imageBtn.setVisibility(View.VISIBLE);
            imageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NetworkManager.getInstance().isNetworkEnabled(true)) {
                        new getMeAsyncTask().execute();
                    }
                }
            });
        } else {
            imageBtn.setVisibility(View.GONE);
        }

        snackbarLayout.setBackgroundColor(getResources().getColor(R.color.amber));
        snackbarLayout.addView(snackView, 0);
        mSnackbar.show();
    }


    public void clearErrors() {
        mUsernameEditText.setError(null);
    }

    private boolean formInputsAreValid() {
        clearErrors();
        if (mUsernameEditText.getText().toString().trim().length() == 0) {
            mUsernameEditText.requestFocus();
            mUsernameEditText.setError(getString(R.string.error_email_not_set));
            Toast.makeText(LoginActivity.this, getString(R.string.error_email_not_set), Toast.LENGTH_LONG).show();
            return false;
        }
        if (!(android.util.Patterns.EMAIL_ADDRESS.matcher(mUsernameEditText.getText().toString().trim()).matches())) {
            mUsernameEditText.requestFocus();
            mUsernameEditText.setError(getString(R.string.error_email_address_is_not_valid));
            Toast.makeText(LoginActivity.this, getString(R.string.error_email_address_is_not_valid), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public class SendLoginEmailAsync extends AsyncTask<Void, Void, SendLoginEmailReply> {
        String cEmail = "";
        SendLoginEmailRequest sendLoginEmailRequest;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            displayProgressDialog();
        }

        @Override
        protected SendLoginEmailReply doInBackground(Void... voids) {
            sendLoginEmailRequest = new SendLoginEmailRequest();
            sendLoginEmailRequest.email = mUsernameEditText.getText().toString().trim();

            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            sendLoginEmailRequest.requestProperties = rp;

            if (BuildConfig.DEBUG) {
                logD("sendLoginEmailRequest: " + sendLoginEmailRequest);
            }

            AuthServiceGrpc.AuthServiceBlockingStub stub;
            try {
                stub = GRPCUtils.getInstance().getAuthServiceGrpc();
                SendLoginEmailReply sendLoginEmailReply = stub.sendLoginEmail(sendLoginEmailRequest);
                return sendLoginEmailReply;
            } catch (Exception e) {
                logE("Error trying to send login email request. sendLoginEmailRequest: " + sendLoginEmailRequest + ". Error: " + e, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(SendLoginEmailReply sendLoginEmailReply) {
            super.onPostExecute(sendLoginEmailReply);
            dismissProgressDialog();
            if (BuildConfig.DEBUG) {
                logD("sendLoginEmailReply is: " + sendLoginEmailReply);
            }
            if (sendLoginEmailReply != null) {
                switch (sendLoginEmailReply.replyProperties.statusCode) {

                    case ReplyProperties.CLIENT_ERROR:
                        switch (sendLoginEmailReply.errorCode) {
                            case SendLoginEmailReply.ACCOUNT_NOT_FOUND:
                                mUsernameEditText.setError(getString(R.string.no_user_found_matching_email));
                                Toast.makeText(LoginActivity.this, getString(R.string.no_user_found_matching_email), Toast.LENGTH_LONG).show();
                                break;
                            case SendLoginEmailReply.INVALID_EMAIL:
                                mUsernameEditText.setError(getString(R.string.error_email_address_is_not_valid));
                                Toast.makeText(LoginActivity.this, getString(R.string.error_email_address_is_not_valid), Toast.LENGTH_LONG).show();
                                break;
                            case SendLoginEmailReply.NO_CLIENT_ERROR:
                                logE("Got NO_CLIENT_ERROR code for registering user. sendLoginEmailReply: " + sendLoginEmailReply + ". sendLoginEmailRequest: " + sendLoginEmailRequest);
                                Toast.makeText(LoginActivity.this, getString(R.string.error_getting_info), Toast.LENGTH_LONG).show();
                                break;
                        }
//                        logE("Sign-up Failed. errorCode: " + sendLoginEmailReply.errorCode +
//                                " // statusCode: " + sendLoginEmailReply.replyProperties.statusCode +
//                                " // Error Msg: " + sendLoginEmailReply.replyProperties.errorMessage);
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        Toast.makeText(LoginActivity.this, getString(R.string.server_error_try_again), Toast.LENGTH_SHORT).show();
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        HonarnamaUser.logout(LoginActivity.this);
                        break;

                    case ReplyProperties.OK:
                        displayCustomSnackbar(getString(R.string.login_email_sent), false);
                        break;

                    case ReplyProperties.UPGRADE_REQUIRED:
                        displayUpgradeRequiredDialog();
                        break;
                }
            } else {
                Toast.makeText(LoginActivity.this, getString(R.string.error_connecting_server_try_again), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        dismissProgressDialog();
    }
}
