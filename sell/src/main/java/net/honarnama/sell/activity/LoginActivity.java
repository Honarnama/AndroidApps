package net.honarnama.sell.activity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import net.honarnama.GRPCUtils;
import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.activity.HonarnamaBaseActivity;
import net.honarnama.core.utils.GenericGravityTextWatcher;
import net.honarnama.nano.AuthServiceGrpc;
import net.honarnama.nano.ReplyProperties;
import net.honarnama.nano.RequestProperties;
import net.honarnama.nano.SimpleRequest;
import net.honarnama.nano.WhoAmIReply;
import net.honarnama.sell.model.HonarnamaUser;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

//TODO ersale mojadad link faal sazi baraye email
public class LoginActivity extends HonarnamaBaseActivity implements View.OnClickListener {
    private TextView mRegisterAsSellerTextView;
    private Button mLoginButton;
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private View mMessageContainer;
    private TextView mLoginMessageTextView;
    //    private View mResendActivationLinkButton;
    private ProgressDialog mLoadingDialog;
    private TextView mForgotPasswordTextView;
    private LinearLayout mTelegramLoginContainer;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTracker = HonarnamaSellApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("LoginActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        setContentView(R.layout.activity_login);
        mRegisterAsSellerTextView = (TextView) findViewById(R.id.register_as_seller_text_view);
        mRegisterAsSellerTextView.setOnClickListener(this);

        mLoginButton = (Button) findViewById(R.id.send_login_link_btn);
        mLoginButton.setOnClickListener(this);

        mUsernameEditText = (EditText) findViewById(R.id.login_username_edit_text);
        mMessageContainer = findViewById(R.id.login_message_container);
        mLoginMessageTextView = (TextView) findViewById(R.id.login_message_text_view);
//        mResendActivationLinkButton = findViewById(R.id.resend_activation_link);
//        mResendActivationLinkButton.setOnClickListener(this);


        mUsernameEditText.addTextChangedListener(new GenericGravityTextWatcher(mUsernameEditText));

        mTelegramLoginContainer = (LinearLayout) findViewById(R.id.telegram_login_container);
        mTelegramLoginContainer.setOnClickListener(this);

        //TODO if user is loggedin
        if (HonarnamaUser.isLoggedIn()) {
            gotoControlPanel();
        } else {
            processIntent(getIntent());
        }

    }

    private void showLoadingDialog() {
        if (mLoadingDialog == null || !mLoadingDialog.isShowing()) {
            mLoadingDialog = ProgressDialog.show(this, "", getString(R.string.login_dialog_text), false);
            mLoadingDialog.setCancelable(false);
        }
    }

    private void hideLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.hide();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    private void processIntent(Intent intent) {
        mLoginMessageTextView.setText("");
        Uri data = intent.getData();
        if (BuildConfig.DEBUG) {
            logD("processIntent :: data= " + data);
        }

        if (data != null) {
            final String loginToken = data.getQueryParameter("token"); //login with email
            final String telegramToken = data.getQueryParameter("telegramToken"); // login with telegram
            final String register = data.getQueryParameter("register");
            if (BuildConfig.DEBUG) {
                logD("token= " + loginToken + ", telegramToken= " + telegramToken + ", register= " + register);
            }
            if (loginToken != null && loginToken.length() > 0) {
                HonarnamaUser.login(loginToken);
                gotoControlPanel();
            } else if (telegramToken != null && telegramToken.length() > 0) {
                showLoadingDialog();
                HonarnamaUser.telegramLogInInBackground(telegramToken, new LogInCallback() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        if (e == null) {
                            parseUser.fetchInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject object, ParseException e) {
                                    hideLoadingDialog();
                                    gotoControlPanel();
                                }
                            });

                        } else {
                            hideLoadingDialog();
                            logE("Error while logging in using token. " + " // telegramToken= " + telegramToken + " // Error: " + e, "", e);
                            Toast.makeText(LoginActivity.this, getString(R.string.error_login_failed) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else if ("true".equals(register)) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(registerIntent, HonarnamaBaseApp.INTENT_REGISTER_CODE);
            }
        }
    }

    private void gotoControlPanel() {
        new getMeAsyncTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (HonarnamaUser.isLoggedIn()) {
            gotoControlPanel();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_as_seller_text_view:
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(intent, HonarnamaBaseApp.INTENT_REGISTER_CODE);
                break;
            case R.id.send_login_link_btn:
                mMessageContainer.setVisibility(View.GONE);
                Intent forgotPasswordIntent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(forgotPasswordIntent);
                break;
            case R.id.telegram_login_container:
                Intent telegramIntent;
                telegramIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/HonarNamaBot?start=**/login"));

                if (telegramIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(telegramIntent);
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.please_install_telegram), Toast.LENGTH_LONG).show();
                }
                break;

            default:
                break;
        }
    }

    private void signUserIn() {
        String username = mUsernameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        if (!(NetworkManager.getInstance().isNetworkEnabled(true))) {
            return;
        }

        if (username.trim().length() == 0) {
            mUsernameEditText.requestFocus();
            mUsernameEditText.setError(getString(R.string.error_login_username_is_empty));
            return;
        }

        if (password.trim().length() == 0) {
            mPasswordEditText.requestFocus();
            mPasswordEditText.setError(getString(R.string.error_register_password_is_empty));
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage(getString(R.string.sending_data));
        progressDialog.setCancelable(false);
        progressDialog.show();

        ParseUser.logInInBackground(username, password, new LogInCallback() {
            public void done(final ParseUser user, ParseException e) {
                if (user != null) {
                    user.fetchInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject object, ParseException e) {
//                            user.pinInBackground();
                            progressDialog.dismiss();
                            gotoControlPanel();
                        }
                    });

                } else {
                    progressDialog.dismiss();
                    // Signup failed. Look at the ParseException to see what happened.
                    logE("logInInBackground Failed. Code: " + e.getCode() + " // Msg: " + e.getMessage(), "", e);
                    mMessageContainer.setVisibility(View.VISIBLE);
                    mLoginMessageTextView.setText(getString(R.string.error_login_invalid_user_or_password));
//                    mResendActivationLinkButton.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }

    }

    private void showTelegramActivationDialog(final String activationCode) {
        final AlertDialog.Builder telegramActivationDialog = new AlertDialog.Builder(new ContextThemeWrapper(this, net.honarnama.base.R.style.DialogStyle));
        telegramActivationDialog.setTitle(getString(net.honarnama.base.R.string.telegram_activation_dialog_title));
        telegramActivationDialog.setItems(new String[]{getString(net.honarnama.base.R.string.telegram_activation_option_text)},
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent telegramIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/HonarNamaBot?start=" + activationCode));
                            if (telegramIntent.resolveActivity(getPackageManager()) != null) {
                                startActivityForResult(telegramIntent, HonarnamaBaseApp.INTENT_TELEGRAM_CODE);
                            } else {
                                Toast.makeText(LoginActivity.this, getString(R.string.please_install_telegram), Toast.LENGTH_LONG).show();
                            }
                        }
                        dialog.dismiss();
                    }
                });
        telegramActivationDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == HonarnamaBaseApp.INTENT_REGISTER_CODE) {
                if (intent.hasExtra(HonarnamaBaseApp.EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_EMAIL)) {
                    if (intent.getBooleanExtra(HonarnamaBaseApp.EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_EMAIL, false)) {
                        mMessageContainer.setVisibility(View.VISIBLE);
                        mLoginMessageTextView.setText(getString(R.string.verification_email_sent));
                    }
                } else if (intent.hasExtra(HonarnamaBaseApp.EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_MOBILE)) {
                    mMessageContainer.setVisibility(View.VISIBLE);
                    mLoginMessageTextView.setText(getString(R.string.telegram_activation_timeout_message));
                    String telegramToken = intent.getStringExtra(HonarnamaBaseApp.EXTRA_KEY_TELEGRAM_CODE);
                    showTelegramActivationDialog(telegramToken);
                }
            }

            if (requestCode == HonarnamaBaseApp.INTENT_TELEGRAM_CODE) {
                //finish();
                gotoControlPanel();
            }
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }


    public class getMeAsyncTask extends AsyncTask<Void, Void, WhoAmIReply> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getString(R.string.please_wait));
            progressDialog.show();
        }

        @Override
        protected WhoAmIReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            SimpleRequest simpleRequest = new SimpleRequest();

            simpleRequest.requestProperties = rp;

            WhoAmIReply whoAmIReply;
            try {
                AuthServiceGrpc.AuthServiceBlockingStub stub = GRPCUtils.getInstance().getAuthServiceGrpc();
                whoAmIReply = stub.whoAmI(simpleRequest);
                return whoAmIReply;
            } catch (InterruptedException e) {
                logE("Error getting user info. Error: " + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(WhoAmIReply whoAmI) {
            super.onPostExecute(whoAmI);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (whoAmI != null) {
                logE("inja whoAmI is: " + whoAmI);
                switch (whoAmI.replyProperties.statusCode) {

                    case ReplyProperties.CLIENT_ERROR:
                        //TODO
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        //TODO
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        //TODO toast
                        HonarnamaUser.logout(null);
                        break;

                    case ReplyProperties.OK:
                        HonarnamaUser.setName(whoAmI.account.name);
                        HonarnamaUser.setGender(whoAmI.account.gender);
                        HonarnamaUser.setId(whoAmI.account.id);
                        Intent intent = new Intent(LoginActivity.this, ControlPanelActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                }

            } else {
                //TODO toast
            }
        }
    }

}
