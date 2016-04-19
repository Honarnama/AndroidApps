package net.honarnama.sell.activity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

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
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;
import net.honarnama.sell.model.HonarnamaUser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

//TODO ersale mojadad link faal sazi baraye email
//TODO what is the whilte page at first launch
public class LoginActivity extends HonarnamaBaseActivity implements View.OnClickListener {
    private TextView mRegisterAsSellerTextView;
    private Button mLoginButton;
    private EditText mUsernameEditText;
    private View mMessageContainer;
    private TextView mLoginMessageTextView;
    private LinearLayout mTelegramLoginContainer;

    ProgressDialog mProgressDialog;

    private Tracker mTracker;

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
        mRegisterAsSellerTextView = (TextView) findViewById(R.id.register_as_seller_text_view);
        mRegisterAsSellerTextView.setOnClickListener(this);

        mLoginButton = (Button) findViewById(R.id.send_login_link_btn);
        mLoginButton.setOnClickListener(this);

        mUsernameEditText = (EditText) findViewById(R.id.login_username_edit_text);
        mMessageContainer = findViewById(R.id.login_message_container);
        mLoginMessageTextView = (TextView) findViewById(R.id.login_message_text_view);

        mUsernameEditText.addTextChangedListener(new GenericGravityTextWatcher(mUsernameEditText));

        mTelegramLoginContainer = (LinearLayout) findViewById(R.id.telegram_login_container);
        mTelegramLoginContainer.setOnClickListener(this);

        if (HonarnamaUser.isLoggedIn()) {
            gotoControlPanel();
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
        mLoginMessageTextView.setText("");
        Uri data = intent.getData();
        logD("processIntent :: data= " + data);

        if (data != null) {
            final String loginToken = data.getQueryParameter("token"); //login with email
            final String register = data.getQueryParameter("register");
            if (BuildConfig.DEBUG) {
                logD("token= " + loginToken + ", register= " + register);
            }
            if (loginToken != null && loginToken.length() > 0) {
                HonarnamaUser.login(loginToken);
                gotoControlPanel();
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

                String telegramToken = HonarnamaBaseApp.getCommonSharedPref().getString(HonarnamaBaseApp.PREF_KEY_TELEGRAM_TOKEN, "");

                if (!TextUtils.isEmpty(telegramToken)) {
                    long millis = HonarnamaBaseApp.getCommonSharedPref().getLong(HonarnamaBaseApp.PREF_KEY_TELEGRAM_TOKEN_SET_DATE, 0L);
                    long diff = (new Date().getTime()) - millis;

                    long seconds = diff / 1000;
                    long minutes = seconds / 60;
                    long hours = minutes / 60;

                    if (minutes < 24 * 60) {
                        ((TextView) findViewById(R.id.telegram_login_text_view)).setText(getString(R.string.telegram_activation_dialog_title));
                        showTelegramActivationDialog(telegramToken);
                    } else {
                        removeTelegramTempInfo();
                    }
                } else {
                    Intent telegramIntent;
                    telegramIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/HonarNamaBot?start=**/login"));

                    if (telegramIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(telegramIntent);
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.please_install_telegram), Toast.LENGTH_LONG).show();
                    }
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
                removeTelegramTempInfo();
                gotoControlPanel();
            }
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }


    public class getMeAsyncTask extends AsyncTask<Void, Void, WhoAmIReply> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(LoginActivity.this);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setMessage(getString(R.string.please_wait));
            }
            mProgressDialog.show();
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

            if (!LoginActivity.this.isFinishing()) { // or call isFinishing() if min sdk version < 17
                //TODO add this to other async task too
                dismissProgressDialog();
            }

            if (whoAmI != null) {
                switch (whoAmI.replyProperties.statusCode) {

                    case ReplyProperties.CLIENT_ERROR:
                        //TODO
                        break;

                    case ReplyProperties.SERVER_ERROR:
//                        displayShortToast(getString(R.string.server_error_try_again));
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        //TODO displayToast
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
                //TODO displayToast
            }
        }
    }

    public void removeTelegramTempInfo() {
        SharedPreferences.Editor editor = HonarnamaBaseApp.getCommonSharedPref().edit();
        editor.putString(HonarnamaBaseApp.PREF_KEY_TELEGRAM_TOKEN, "");
        editor.putLong(HonarnamaBaseApp.PREF_KEY_TELEGRAM_TOKEN_SET_DATE, 0);
        editor.commit();
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

}
