package net.honarnama.sell;

import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import net.honarnama.HonarNamaBaseActivity;
import net.honarnama.utils.GenericGravityTextWatcher;
import net.honarnama.utils.HonarNamaUser;
import net.honarnama.utils.NetworkManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends HonarNamaBaseActivity implements View.OnClickListener {
    private TextView mRegisterAsSellerTextView;
    private Button mLoginButton;
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private TextView mErrorMessageTextView;
    private ProgressDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (HonarNamaUser.isShopOwner() && HonarNamaUser.isVerified()) {
            //Go to controlPanel Activity
            gotoControlPanel();

        } else {
            setContentView(R.layout.activity_login);
            mRegisterAsSellerTextView = (TextView) findViewById(R.id.register_as_seller_text_view);
            mRegisterAsSellerTextView.setOnClickListener(this);

            mLoginButton = (Button) findViewById(R.id.login_button);
            mLoginButton.setOnClickListener(this);

            mUsernameEditText = (EditText) findViewById(R.id.login_username_edit_text);
            mPasswordEditText = (EditText) findViewById(R.id.login_password_edit_text);
            mErrorMessageTextView = (TextView) findViewById(R.id.login_error_msg);

            mUsernameEditText.addTextChangedListener(new GenericGravityTextWatcher(mUsernameEditText));
            mPasswordEditText.addTextChangedListener(new GenericGravityTextWatcher(mPasswordEditText));

            if (HonarNamaUser.getCurrentUser() != null) {
                logE("Parse user is not empty");
                showLoadingDialog();
                HonarNamaUser.getCurrentUser().fetchInBackground(new GetCallback<ParseObject>() {
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

        logI(null, "created!");
    }

    private void showLoadingDialog() {
        mLoadingDialog = ProgressDialog.show(this, "", getString(R.string.login_dialog_text), false);
        mLoadingDialog.setCancelable(false);
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

        if (data != null) {
            final String token = data.getQueryParameter("token");
            final String register = data.getQueryParameter("register");

            if (token != null && token.length() > 0) {
                HonarNamaUser.telegramLogInInBackground(token, new LogInCallback() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        hideLoadingDialog();
                        if (e == null) {
                            gotoControlPanelOrRaiseError();
                        } else {
                            // TODO: error message
                            logE("Error while logging in using token", "token= " + token, e);
                        }
                    }
                });
            } else if ("true".equals(register)) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        }
    }

    private void gotoControlPanel() {
        Intent intent = new Intent(this, ControlPanel.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (HonarNamaUser.isShopOwner() && HonarNamaUser.isVerified()) {
            gotoControlPanel();
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_as_seller_text_view:
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                break;
            case R.id.login_button:
                mErrorMessageTextView.setVisibility(View.GONE);
                signUserIn();
                break;
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
                    logE("Sign-up Failed. Code: ", e.getMessage(), e);
                    mErrorMessageTextView.setVisibility(View.VISIBLE);
                    mErrorMessageTextView.setText("نام کاربری یا رمز عبور اشتباه است.");
                }
            }
        });
    }

    private void gotoControlPanelOrRaiseError() {
        if (!HonarNamaUser.isVerified()) {
            logE("Login Failed. Account is not activated");
            mErrorMessageTextView.setVisibility(View.VISIBLE);
            mErrorMessageTextView.setText(" شما هنوز حساب کاربری خود را فعال نکردید. ارسال مجدد لینک ");
        } else if (!HonarNamaUser.isShopOwner()) {
            logE("Login Failed. User is not a shop owner");
            mErrorMessageTextView.setVisibility(View.VISIBLE);
            mErrorMessageTextView.setText(" شما هنوز حساب غرفه‌داری باز نکردید. ");
        } else {
            gotoControlPanel();
        }
    }
}
