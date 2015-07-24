package net.honarnama.sell;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import net.honarnama.HonarNamaBaseActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class LoginActivity extends HonarNamaBaseActivity implements View.OnClickListener {
    private TextView mRegisterAsSellerTextView;
    private Button mLoginButton;
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private TextView mErrorMessageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            checkUserState(currentUser);
        }

        setContentView(R.layout.activity_login);
        mRegisterAsSellerTextView = (TextView) findViewById(R.id.register_as_seller_text_view);
        mRegisterAsSellerTextView.setOnClickListener(this);

        mLoginButton = (Button) findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(this);

        mUsernameEditText = (EditText) findViewById(R.id.login_username_edit_text);
        mPasswordEditText = (EditText) findViewById(R.id.login_password_edit_text);
        mErrorMessageTextView = (TextView) findViewById(R.id.login_error_msg);

        logI(null, "created!");
    }

    @Override
    protected void onResume() {
        super.onResume();

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            checkUserState(currentUser);
        }
    }

    private void checkUserState(ParseUser currentUser) {
        String activationMethod = currentUser.getString("activationMethod");
        boolean isVerified = false;
        if ("email".equals(activationMethod)) {
            isVerified = currentUser.getBoolean("emailVerified");
        } else if ("mobileNumber".equals(activationMethod)) {
            isVerified = currentUser.getBoolean("telegramVerified");
        }

        if (!isVerified) {
            Toast.makeText(this, "You have not verified your account yet!", Toast.LENGTH_LONG).show();
        }

        boolean isShopOwner = currentUser.getBoolean("isShopOwner");
        if (isShopOwner) {
            Toast.makeText(this, "Currently logged in!", Toast.LENGTH_LONG).show();
            //finish();
            // TODO: Go to next page if yes
        } else {
            // TODO: Error message
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

        if (username.length() == 0) {
            mUsernameEditText.requestFocus();
            mUsernameEditText.setError(getString(R.string.error_register_username_is_empty));
            return;
        }

        if (password.length() == 0) {
            mPasswordEditText.requestFocus();
            mPasswordEditText.setError(getString(R.string.error_register_password_is_empty));
            return;
        }

        ParseUser.logInInBackground(username, password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    Toast.makeText(LoginActivity.this, "Horray!", Toast.LENGTH_LONG).show();
                } else {
                    // Signup failed. Look at the ParseException to see what happened.
                    logE("Sign-up Failed. Code: ", e.getMessage(), e);
                    mErrorMessageTextView.setVisibility(View.VISIBLE);
                    mErrorMessageTextView.setText("نام کاربری یا رمز عبور اشتباه است.");

                }
            }
        });
    }
}
