package net.honarnama.sell.activity;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.core.activity.HonarnamaBaseActivity;
import net.honarnama.core.activity.RegisterActivity;
import net.honarnama.core.utils.GenericGravityTextWatcher;
import net.honarnama.core.utils.HonarnamaUser;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.sell.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class LoginActivity extends HonarnamaBaseActivity implements View.OnClickListener {
    private TextView mRegisterAsSellerTextView;
    private Button mLoginButton;
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private View mMessageContainer;
    private TextView mLoginMessageTextView;
    private View mErrorMessageButton;
    private ProgressDialog mLoadingDialog;
    private TextView mForgotPasswordTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        mRegisterAsSellerTextView = (TextView) findViewById(R.id.register_as_seller_text_view);
        mRegisterAsSellerTextView.setOnClickListener(this);

        mLoginButton = (Button) findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(this);

        mUsernameEditText = (EditText) findViewById(R.id.login_username_edit_text);
        mPasswordEditText = (EditText) findViewById(R.id.login_password_edit_text);
        mMessageContainer = findViewById(R.id.login_message_container);
        mLoginMessageTextView = (TextView) findViewById(R.id.login_message_text_view);
        mErrorMessageButton = findViewById(R.id.login_error_btn);
        mErrorMessageButton.setOnClickListener(this);

        mForgotPasswordTextView = (TextView) findViewById(R.id.forgot_password_text_view);
        mForgotPasswordTextView.setOnClickListener(this);

        mUsernameEditText.addTextChangedListener(new GenericGravityTextWatcher(mUsernameEditText));
        mPasswordEditText.addTextChangedListener(new GenericGravityTextWatcher(mPasswordEditText));

        final ParseUser user = HonarnamaUser.getCurrentUser();
        if (user != null) {
            showLoadingDialog();
            logI("Parse user is not empty", "user= " + user.getEmail());
            user.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException parseException) {
                    //checkIfUserStillExistOnParse
                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.whereEqualTo("username", user.getUsername());
                    query.findInBackground(new FindCallback<ParseUser>() {
                        public void done(List<ParseUser> objects, ParseException e) {
                            if (e == null) {
                                // User Exist On Parse
                                gotoControlPanelOrRaiseError();
                            } else {
                                Log.e("Elnaz", e.getCode() + "");
                                user.logOut();
                            }
                            hideLoadingDialog();
                        }
                    });
                }
            });
        } else {
            processIntent(getIntent());
        }

        logI(null, "created!");
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

        Uri data = intent.getData();

        logI(null, "processIntent :: data= " + data);

        if (data != null) {
            final String telegramToken = data.getQueryParameter("telegramToken");
            final String register = data.getQueryParameter("register");

            logI(null, "telegramToken= " + telegramToken + ", register= " + register);

            if (telegramToken != null && telegramToken.length() > 0) {
                showLoadingDialog();
                HonarnamaUser.telegramLogInInBackground(telegramToken, new LogInCallback() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        hideLoadingDialog();
                        if (e == null) {
                            gotoControlPanelOrRaiseError();
                        } else {
                            logE("Error while logging in using token", "telegramToken= " + telegramToken, e);
                            Toast.makeText(LoginActivity.this, R.string.error_login_failed, Toast.LENGTH_LONG).show();
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
        Intent intent = new Intent(this, ControlPanelActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (HonarnamaUser.isVerified()) {
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
            case R.id.login_button:
                mMessageContainer.setVisibility(View.GONE);
                signUserIn();
                break;
            case R.id.forgot_password_text_view:
                // TODO: what about email?
                Intent telegramIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/HonarNamaBot?start=**/login"));
                if (telegramIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(telegramIntent);
                }
                break;
            case R.id.login_error_btn:
                Intent telegramIntent2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/HonarNamaBot?start=" + HonarnamaUser.getCurrentUser().getString("telegramCode")));
                if (telegramIntent2.resolveActivity(getPackageManager()) != null) {
                    startActivity(telegramIntent2);
                }
            default:
                break;
        }
    }

    private void signUserIn() {
        String username = mUsernameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        if (!(NetworkManager.getInstance().isNetworkEnabled(this, true))) {
            return;
        }

        if (username.trim().length() == 0) {
            mUsernameEditText.requestFocus();
            mUsernameEditText.setError(getString(R.string.error_register_username_is_empty));
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
            public void done(ParseUser user, ParseException e) {
                progressDialog.dismiss();
                if (user != null) {
                    gotoControlPanelOrRaiseError();
                } else {
                    // Signup failed. Look at the ParseException to see what happened.
                    logE("logInInBackground Failed. ", e.getMessage(), e);
                    mMessageContainer.setVisibility(View.VISIBLE);
                    mLoginMessageTextView.setText(getString(R.string.error_login_invalid_user_or_password));
                    mErrorMessageButton.setVisibility(View.GONE);
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

    private void gotoControlPanelOrRaiseError() {
        if (!HonarnamaUser.isVerified()) {
            logE("Login Failed. Account is not activated");
            mMessageContainer.setVisibility(View.VISIBLE);
            mLoginMessageTextView.setText(R.string.not_verified);
            switch (HonarnamaUser.getActivationMethod()) {
                case MOBILE_NUMBER:
                    // TODO: onlt if telegram is installed
                    mErrorMessageButton.setVisibility(View.VISIBLE);
                    break;

                default:
                    mErrorMessageButton.setVisibility(View.GONE);
                    break;
            }
        } else {
            gotoControlPanel();
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
                        mLoginMessageTextView.setText("لینک فعال‌سازی حساب به آدرس ایمیلتان ارسال شد.");
                        mErrorMessageButton.setVisibility(View.GONE);
                    }
                } else if (intent.hasExtra(HonarnamaBaseApp.EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_MOBILE)) {
//                    if (intent.getBooleanExtra(HonarnamaBaseApp.EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_MOBILE, false)) {
//                        mMessageContainer.setVisibility(View.VISIBLE);
//                        mLoginMessageTextView.setText("لینک فعال‌سازی حساب به تلگرام شما ارسال شد.");
//                        mErrorMessageButton.setVisibility(View.GONE);
//                    }
                    if(HonarnamaUser.getCurrentUser() != null) {
                        showTelegramActivationDialog(HonarnamaUser.getCurrentUser().getString("telegramCode"));
                    }
                }
            }

            if(requestCode == HonarnamaBaseApp.INTENT_TELEGRAM_CODE) {
                //finish();
                gotoControlPanelOrRaiseError();
            }
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }


}
