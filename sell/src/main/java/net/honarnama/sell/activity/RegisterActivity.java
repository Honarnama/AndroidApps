package net.honarnama.sell.activity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.honarnama.GRPCUtils;
import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.activity.HonarnamaBaseActivity;
import net.honarnama.core.utils.GenericGravityTextWatcher;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.nano.Account;
import net.honarnama.nano.AuthServiceGrpc;
import net.honarnama.nano.CreateAccountReply;
import net.honarnama.nano.CreateOrUpdateAccountRequest;
import net.honarnama.nano.ReplyProperties;
import net.honarnama.nano.RequestProperties;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;
import net.honarnama.sell.model.HonarnamaUser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Date;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

/**
 * Created by elnaz on 2/13/16.
 */
public class RegisterActivity extends HonarnamaBaseActivity implements View.OnClickListener {

    private EditText mNameEditText;
    private EditText mMobileNumberEditText;
    private EditText mEmailAddressEditText;

    private RadioButton mActivateWithEmail;
    private RadioButton mActivateWithTelegram;

    private ToggleButton mGenderWoman;
    private ToggleButton mGenderMan;
    private ToggleButton mGenderNotSaid;

    private Button mRegisterButton;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTracker = HonarnamaSellApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("Register");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        setContentView(R.layout.activity_register);

        mNameEditText = (EditText) findViewById(R.id.register_name_edit_text);
        mMobileNumberEditText = (EditText) findViewById(R.id.register_mobile_number_edit_text);
        mEmailAddressEditText = (EditText) findViewById(R.id.register_email_address_edit_text);
        mRegisterButton = (Button) findViewById(R.id.register_button);

        mActivateWithEmail = (RadioButton) findViewById(R.id.register_activate_with_email);
        mActivateWithTelegram = (RadioButton) findViewById(R.id.register_activate_with_telegram);

        mMobileNumberEditText.addTextChangedListener(new GenericGravityTextWatcher(mMobileNumberEditText));
        mEmailAddressEditText.addTextChangedListener(new GenericGravityTextWatcher(mEmailAddressEditText));
        mRegisterButton.setOnClickListener(this);
        mActivateWithEmail.setOnClickListener(this);
        mActivateWithTelegram.setOnClickListener(this);

        mGenderWoman = (ToggleButton) findViewById(R.id.register_gender_woman);
        mGenderMan = (ToggleButton) findViewById(R.id.register_gender_man);
        mGenderNotSaid = (ToggleButton) findViewById(R.id.register_gender_not_said);
        mGenderWoman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGenderWoman.setChecked(true);
                mGenderMan.setChecked(false);
                mGenderNotSaid.setChecked(false);
            }
        });
        mGenderMan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGenderMan.setChecked(true);
                mGenderWoman.setChecked(false);
                mGenderNotSaid.setChecked(false);
            }
        });

        mGenderNotSaid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGenderNotSaid.setChecked(true);
                mGenderWoman.setChecked(false);
                mGenderMan.setChecked(false);
            }
        });

        TextView rulesTextView = (TextView) findViewById(R.id.honarnama_rules);
        rulesTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == mRegisterButton.getId()) {

            signUserUp();
        }
        if (viewId == R.id.register_activate_with_email || viewId == R.id.register_activate_with_telegram) {
            changeMandatoryFieldsStarMarker();
        }
    }

    private void signUserUp() {
        if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
            return;
        }

        if (!enteredValuesAreValid()) {
            return; // TODO: feedback
        }

        // TODO
//        if (HonarnamaUser.isLoggedIn()) {
//            HonarnamaUser.logOut();
//        }

        new CreateAccountAsync().execute();
    }

    private void changeMandatoryFieldsStarMarker() {

        if (mActivateWithEmail.isChecked()) {
            findViewById(R.id.register_email_star_marker).setVisibility(View.VISIBLE);
            findViewById(R.id.register_mobile_number_star_marker).setVisibility(View.GONE);

        } else {
            findViewById(R.id.register_email_star_marker).setVisibility(View.GONE);
            findViewById(R.id.register_mobile_number_star_marker).setVisibility(View.VISIBLE);
        }
    }

    private boolean enteredValuesAreValid() {

        if (mNameEditText.getText().toString().trim().length() == 0) {
            mNameEditText.requestFocus();
            mNameEditText.setError(getString(R.string.error_name_not_set));
            return false;
        }
        if (mActivateWithEmail.isChecked()) {
            if (mEmailAddressEditText.getText().toString().trim().length() == 0) {
                mEmailAddressEditText.requestFocus();
                mEmailAddressEditText.setError(getString(R.string.error_email_field_can_not_be_empty));
                return false;
            } else {
                boolean isOK = android.util.Patterns.EMAIL_ADDRESS.matcher(mEmailAddressEditText.getText().toString()).matches();
                if (!isOK) {
                    mEmailAddressEditText.requestFocus();
                    mEmailAddressEditText.setError(getString(R.string.error_email_address_is_not_valid));
                    return false;
                }
            }
        }

        if (mActivateWithTelegram.isChecked()) {
            if (mMobileNumberEditText.getText().toString().trim().length() == 0) {
                mMobileNumberEditText.requestFocus();
                mMobileNumberEditText.setError(getString(R.string.error_mobile_number_field_can_not_be_empty));
                return false;
            } else {
                String mobileNumberPattern = "^09\\d{9}$";
                if (!mMobileNumberEditText.getText().toString().matches(mobileNumberPattern)) {
                    mMobileNumberEditText.requestFocus();
                    mMobileNumberEditText.setError(getString(R.string.error_mobile_number_is_not_valid));
                    return false;
                }

            }
        }

        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            default:
                if (BuildConfig.DEBUG) {
                    logD("Unexpected requestCode= " + requestCode);
                }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void sendUserBackToCallingActivity(int activationMethod, String telegramCode) {
        Intent intent = new Intent();

        switch (activationMethod) {
            case Account.EMAIL:
                intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_EMAIL, true);
                break;

            case Account.TELEGRAM:
                intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_MOBILE, true);
                intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_TELEGRAM_CODE, telegramCode);

                SharedPreferences.Editor editor = HonarnamaBaseApp.getCommonSharedPref().edit();
                editor.putString(HonarnamaBaseApp.PREF_KEY_TELEGRAM_TOKEN, telegramCode);
                Date currentDate = new Date();
                editor.putLong(HonarnamaBaseApp.PREF_KEY_TELEGRAM_TOKEN_SET_DATE, currentDate.getTime());
                editor.commit();

                break;
        }

        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public class CreateAccountAsync extends AsyncTask<Void, Void, CreateAccountReply> {
        ProgressDialog progressDialog;
        String name = "";
        int genderCode;
        int activationMethod;
        String email = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            genderCode = mGenderWoman.isChecked() ? Account.FEMALE : (mGenderMan.isChecked() ? Account.MALE : Account.UNSPECIFIED);
            name = mNameEditText.getText().toString().trim();
            activationMethod = mActivateWithEmail.isChecked() ? Account.EMAIL : Account.TELEGRAM;
            if (mEmailAddressEditText.getText().toString().trim().length() > 0) {
                email = mEmailAddressEditText.getText().toString().trim();
            }

            progressDialog = new ProgressDialog(RegisterActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getString(R.string.please_wait));
            progressDialog.show();
        }

        @Override
        protected CreateAccountReply doInBackground(Void... voids) {
            final CreateOrUpdateAccountRequest createOrUpdateAccountRequest = new CreateOrUpdateAccountRequest();
            createOrUpdateAccountRequest.account = new Account();

            createOrUpdateAccountRequest.account.mobileNumber = mMobileNumberEditText.getText().toString().trim();
            createOrUpdateAccountRequest.account.name = mNameEditText.getText().toString().trim();
            createOrUpdateAccountRequest.account.activationMethod = activationMethod;
            createOrUpdateAccountRequest.account.email = email;
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

            CreateAccountReply createAccountReply = stub.createAccount(createOrUpdateAccountRequest);
            return createAccountReply;
        }

        @Override
        protected void onPostExecute(CreateAccountReply createAccountReply) {
            super.onPostExecute(createAccountReply);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (createAccountReply != null) {
                logE("inja createAccountReply is: " + createAccountReply);
                switch (createAccountReply.replyProperties.statusCode) {

                    case ReplyProperties.CLIENT_ERROR:
                        switch (createAccountReply.errorCode) {
                            case CreateAccountReply.DUPLICATE_EMAIL:
                                mEmailAddressEditText.setError(getString(R.string.error_signup_duplicated_email));
                                break;
                            case CreateAccountReply.INVALID_EMAIL:
                                mEmailAddressEditText.setError(getString(R.string.error_email_address_is_not_valid));
                                break;
                            case CreateAccountReply.DUPLICATE_MOBILE_NUMBER:
                                mMobileNumberEditText.setError(getString(R.string.error_signup_duplicated_mobile_number));
                                break;
                            case CreateAccountReply.INVALID_MOBILE_NUMBER:
                                mMobileNumberEditText.setError(getString(R.string.error_mobile_number_is_not_valid));
                                break;
                        }
                        Toast.makeText(RegisterActivity.this, getString(R.string.error_signup_correct_mistakes_and_try_again), Toast.LENGTH_LONG).show();
                        logE("Sign-up Failed. errorCode: " + createAccountReply.errorCode +
                                " // statusCode: " + createAccountReply.replyProperties.statusCode +
                                " // Error Msg: " + createAccountReply.replyProperties.errorMessage);
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        //TODO
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        //TODO displayToast
                        HonarnamaUser.logout(RegisterActivity.this);
                        break;

                    case ReplyProperties.OK:
                        sendUserBackToCallingActivity(activationMethod, createAccountReply.telegramActivationCode);
                        break;
                }
            } else {
                //TODO displayToast
            }
        }
    }
}
