package net.honarnama.browse.activity;

import com.crashlytics.android.Crashlytics;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.browse.R;
import net.honarnama.core.activity.HonarnamaBaseActivity;
import net.honarnama.core.activity.RegisterActivity;
import net.honarnama.core.utils.GenericGravityTextWatcher;
import net.honarnama.core.utils.HonarnamaUser;
import net.honarnama.core.utils.NetworkManager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by elnaz on 12/13/15.
 */
public class LoginActivity extends HonarnamaBaseActivity implements View.OnClickListener {

    TextView mRegisterAsCustomerTextView;
    private Button mLoginButton;
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private View mErrorMessageContainer;
    private TextView mErrorMessageTextView;
    private View mErrorMessageButton;
    private TextView mForgotPasswordTextView;

    private ProgressDialog mLoadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mRegisterAsCustomerTextView = (TextView) findViewById(R.id.register_as_customer_text_view);
        mRegisterAsCustomerTextView.setOnClickListener(this);


        mLoginButton = (Button) findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(this);

        mUsernameEditText = (EditText) findViewById(R.id.login_username_edit_text);
        mPasswordEditText = (EditText) findViewById(R.id.login_password_edit_text);
        mErrorMessageContainer = findViewById(R.id.login_error_container);
        mErrorMessageTextView = (TextView) findViewById(R.id.login_error_msg);
        mErrorMessageButton = findViewById(R.id.login_error_btn);
        mErrorMessageButton.setOnClickListener(this);

        mForgotPasswordTextView = (TextView) findViewById(R.id.forgot_password_text_view);
        mForgotPasswordTextView.setOnClickListener(this);

        mUsernameEditText.addTextChangedListener(new GenericGravityTextWatcher(mUsernameEditText));
        mPasswordEditText.addTextChangedListener(new GenericGravityTextWatcher(mPasswordEditText));

        ParseUser user = HonarnamaUser.getCurrentUser();
        if (user != null) {
            logI("Parse user is not empty", "user= " + user.getEmail());
            showLoadingDialog();
            user.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    gotoControlPanelOrRaiseError();
                    hideLoadingDialog();
                }
            });
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
            public void done(ParseUser user, ParseException e) {
                progressDialog.dismiss();
                if (user != null) {
                    gotoControlPanelOrRaiseError();
                } else {
                    // Signup failed. Look at the ParseException to see what happened.
                    logE("Sign-up Failed. Code: ", e.getMessage(), e);
                    mErrorMessageContainer.setVisibility(View.VISIBLE);
                    mErrorMessageTextView.setText(getString(R.string.error_login_invalid_user_or_password));
                    mErrorMessageButton.setVisibility(View.GONE);
                }
            }
        });
    }

    private void gotoControlPanel() {
//        Intent intent = new Intent(this, ControlPanelActivity.class);
//        startActivity(intent);
//        finish();
        Toast.makeText(LoginActivity.this, "Successfully Logged In", Toast.LENGTH_SHORT).show();
    }

    private void gotoControlPanelOrRaiseError() {
        if (!HonarnamaUser.isVerified()) {
            logE("Login Failed. Account is not activated");
            mErrorMessageContainer.setVisibility(View.VISIBLE);
            mErrorMessageTextView.setText(R.string.not_verified);
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
            ParseUser u = HonarnamaUser.getCurrentUser();
            Crashlytics.setUserIdentifier(u.getSessionToken());
            Crashlytics.setUserEmail(u.getEmail());
            Crashlytics.setUserName(u.getUsername());

            gotoControlPanel();
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.register_as_customer_text_view:
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(intent, HonarnamaBaseApp.INTENT_REGISTER_CODE);
                break;
            case R.id.login_button:
                mErrorMessageContainer.setVisibility(View.GONE);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == HonarnamaBaseApp.INTENT_REGISTER_CODE) {
                Toast.makeText(LoginActivity.this, "here", Toast.LENGTH_LONG).show();
                if (intent.hasExtra(HonarnamaBaseApp.EXTRA_KEY_DISPLAY_REGISTER_SNACK)) {
                    if (intent.getBooleanExtra(HonarnamaBaseApp.EXTRA_KEY_DISPLAY_REGISTER_SNACK, false)) {
                        Toast.makeText(LoginActivity.this, getString(R.string.successful_signup), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }
}
