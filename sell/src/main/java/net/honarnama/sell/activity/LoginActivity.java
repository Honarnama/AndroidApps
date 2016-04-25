package net.honarnama.sell.activity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.honarnama.GRPCUtils;
import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.activity.HonarnamaBaseActivity;
import net.honarnama.core.utils.GravityTextWatcher;
import net.honarnama.core.utils.WindowUtil;
import net.honarnama.nano.Account;
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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

//TODO ersale mojadad link faal sazi baraye cEmail
//TODO remove unactivated account after 24 hours
public class LoginActivity extends HonarnamaBaseActivity implements View.OnClickListener {
    private Button mRegisterAsSellerBtn;
    private Button mLoginButton;
    private EditText mUsernameEditText;
    private View mMessageContainer;
    private TextView mLoginMessageTextView;
    private LinearLayout mTelegramLoginContainer;
    private TextView mTelegramLoginTextView;

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
        mMessageContainer = findViewById(R.id.login_message_container);
        mLoginMessageTextView = (TextView) findViewById(R.id.login_message_text_view);

        mUsernameEditText.addTextChangedListener(new GravityTextWatcher(mUsernameEditText));

        mTelegramLoginContainer = (LinearLayout) findViewById(R.id.telegram_login_container);
        mTelegramLoginContainer.setOnClickListener(this);

        if (HonarnamaUser.isLoggedIn()) {
            goToControlPanel();
        } else {
            processIntent(getIntent());
        }

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .coordinatorLayout);

        mTelegramLoginTextView = (TextView) findViewById(R.id.telegram_login_text_view);

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
                new getMeAsyncTask().execute();
            } else if ("true".equals(register)) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(registerIntent, HonarnamaBaseApp.INTENT_REGISTER_CODE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMessageContainer.setVisibility(View.GONE);
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
                mMessageContainer.setVisibility(View.GONE);
                //TODO
                Toast.makeText(LoginActivity.this, "لطفا برای ورود از لینکی که هنگام ساخت حساب به آدرس ایمیلتان فرستاده شده، استفاده کنید.", Toast.LENGTH_LONG).show();
                break;
            case R.id.telegram_login_container:

                String telegramToken = HonarnamaBaseApp.getCommonSharedPref().getString(HonarnamaBaseApp.PREF_KEY_TELEGRAM_TOKEN, "");

                if (!TextUtils.isEmpty(telegramToken)) {
                    long millis = HonarnamaBaseApp.getCommonSharedPref().getLong(HonarnamaBaseApp.PREF_KEY_TELEGRAM_TOKEN_SET_DATE, 0L);
                    long diff = (new Date().getTime()) - millis;

                    long seconds = diff / 1000;
                    long minutes = seconds / 60;
                    long hours = minutes / 60;

                    if (BuildConfig.DEBUG) {
                        logD("Time passe after telegram registeration: " + minutes + " minutes.");
                    }

                    if (minutes < 24 * 60) {
                        if (BuildConfig.DEBUG) {
                            logD("Time passe after telegram registeration is less than 24 hours => Show Telegram Activation Dialog. ");
                        }
                        showTelegramActivationDialog(telegramToken);
                    } else {
                        if (BuildConfig.DEBUG) {
                            logD("Time passe after telegram registeration is greater than 24 hours => Call telegram to log user in. ");
                        }
                        removeTelegramTempInfo();
                        callTelegramToLogUserIn();
                    }
                } else {
                    callTelegramToLogUserIn();
                }

                break;

            default:
                break;
        }
    }

    public void callTelegramToLogUserIn() {
        Intent telegramIntent;
        telegramIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/HonarNamaBot?start=**/login"));

        if (telegramIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(telegramIntent);
        } else {
            Toast.makeText(LoginActivity.this, getString(R.string.please_install_telegram), Toast.LENGTH_LONG).show();
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
        WindowUtil.hideKeyboard(LoginActivity.this);
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

                        if (mSnackbar != null && mSnackbar.isShown()) {
                            mSnackbar.dismiss();
                        }
                        mSnackbar = Snackbar
                                .make(mCoordinatorLayout, getString(R.string.verification_email_sent), Snackbar.LENGTH_INDEFINITE);
                        View sbView = mSnackbar.getView();
                        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(getResources().getColor(R.color.gray_dark));
                        textView.setBackgroundColor(getResources().getColor(R.color.amber));
                        textView.setSingleLine(false);
                        sbView.setBackgroundColor(getResources().getColor(R.color.amber));
                        mSnackbar.show();
                    }
                } else if (intent.hasExtra(HonarnamaBaseApp.EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_MOBILE)) {
                    if (BuildConfig.DEBUG) {
                        logD("display telegram verification notif.");
                    }

                    mTelegramLoginTextView.setText(getString(R.string.telegram_activation_dialog_title));

                    if (mSnackbar != null && mSnackbar.isShown()) {
                        mSnackbar.dismiss();
                    }
                    mSnackbar = Snackbar
                            .make(mCoordinatorLayout, getString(R.string.telegram_activation_timeout_message), Snackbar.LENGTH_INDEFINITE);
                    View sbView = mSnackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(getResources().getColor(R.color.gray_dark));
                    textView.setBackgroundColor(getResources().getColor(R.color.amber));
                    textView.setSingleLine(false);
                    sbView.setBackgroundColor(getResources().getColor(R.color.amber));
                    mSnackbar.show();

                    String telegramToken = intent.getStringExtra(HonarnamaBaseApp.EXTRA_KEY_TELEGRAM_CODE);
                    showTelegramActivationDialog(telegramToken);
                }
            } else {
                if (BuildConfig.DEBUG) {
                    logD("Hide register message notif.");
                }
                mMessageContainer.setVisibility(View.GONE);
            }
//
//            if (requestCode == HonarnamaBaseApp.INTENT_TELEGRAM_CODE) {
//                removeTelegramTempInfo();
//                getUserInfo();
//            }
        } else {
            mMessageContainer.setVisibility(View.GONE);
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
            } catch (InterruptedException e) {
                logE("Error getting whoAmIReply. simpleRequest: " + simpleRequest + ". Error: " + e);
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
                        HonarnamaUser.logout(null);
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        Toast.makeText(LoginActivity.this, getString(R.string.server_error_try_again), Toast.LENGTH_SHORT).show();
                        HonarnamaUser.logout(null);
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        //TODO
                        if (BuildConfig.DEBUG) {
                            logD("Got NOT_AUTHORIZED reply in reply to WhoAmI request.");
                        }
                        Toast.makeText(LoginActivity.this, getString(R.string.account_not_found) + " یا اعتبار لینک ورود منقضی شده است.", Toast.LENGTH_LONG).show();

                        HonarnamaUser.logout(null);

                        if (mSnackbar != null && mSnackbar.isShown()) {
                            mSnackbar.dismiss();
                        }

                        mSnackbar = Snackbar
                                .make(mCoordinatorLayout, "در صورتی که حسابتان را قبلا فعال کرده بودید، می‌توانید از طریق فرم بالا، درخواست لینک ورود جدید کنید.", Snackbar.LENGTH_INDEFINITE);

                        View sbView = mSnackbar.getView();
                        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(getResources().getColor(R.color.gray_dark));
                        textView.setBackgroundColor(getResources().getColor(R.color.amber));
                        textView.setSingleLine(false);
                        sbView.setBackgroundColor(getResources().getColor(R.color.amber));
                        mSnackbar.show();

                        break;

                    case ReplyProperties.OK:
                        HonarnamaUser.setName(whoAmIReply.account.name);
                        HonarnamaUser.setGender(whoAmIReply.account.gender);
                        HonarnamaUser.setId(whoAmIReply.account.id);
                        if (whoAmIReply.account.activationMethod == Account.TELEGRAM) {
                            removeTelegramTempInfo();
                        }
                        goToControlPanel();
                        break;

                    case ReplyProperties.UPGRADE_REQUIRED:
                        displayUpgradeRequiredDialog();
                        HonarnamaUser.logout(null);
                        break;
                }

            } else {
                Toast.makeText(LoginActivity.this, getString(R.string.error_connecting_to_Server) + getString(R.string.check_net_connection), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void goToControlPanel() {
        Intent intent = new Intent(LoginActivity.this, ControlPanelActivity.class);
        startActivity(intent);
        finish();
    }

    public void removeTelegramTempInfo() {
        if (BuildConfig.DEBUG) {
            logD("Remove telegram registeration temp data.");
        }
        SharedPreferences.Editor editor = HonarnamaBaseApp.getCommonSharedPref().edit();
        editor.putString(HonarnamaBaseApp.PREF_KEY_TELEGRAM_TOKEN, "");
        editor.putLong(HonarnamaBaseApp.PREF_KEY_TELEGRAM_TOKEN_SET_DATE, 0L);
        editor.commit();
    }


    private void dismissProgressDialog() {
        if (!LoginActivity.this.isFinishing()) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    }

    private void displayProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(LoginActivity.this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(getString(R.string.please_wait));
        }
        if (!LoginActivity.this.isFinishing() && !mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

}
