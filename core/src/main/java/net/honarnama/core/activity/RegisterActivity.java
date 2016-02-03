package net.honarnama.core.activity;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.R;
import net.honarnama.core.utils.GenericGravityTextWatcher;
import net.honarnama.core.utils.HonarnamaUser;
import net.honarnama.core.utils.NetworkManager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.ToggleButton;


public class RegisterActivity extends HonarnamaBaseActivity implements View.OnClickListener {

    private EditText mNameEditText;
    private EditText mMobileNumberEditText;
    private EditText mEmailAddressEditText;
    private EditText mPasswordEdiText;
    private EditText mConfirmPasswordEditText;

    private RadioButton mActivateWithEmail;
    private RadioButton mActivateWithTelegram;

    private ToggleButton mGenderWoman;
    private ToggleButton mGenderMan;
    private ToggleButton mGenderNotSaid;

    private Button mRegisterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mNameEditText = (EditText) findViewById(R.id.register_name_edit_text);
        mMobileNumberEditText = (EditText) findViewById(R.id.register_mobile_number_edit_text);
        mEmailAddressEditText = (EditText) findViewById(R.id.register_email_address_edit_text);
        mPasswordEdiText = (EditText) findViewById(R.id.register_password_edit_text);
        mConfirmPasswordEditText = (EditText) findViewById(R.id.register_confirm_password_edit_text);
        mRegisterButton = (Button) findViewById(R.id.register_button);

        mActivateWithEmail = (RadioButton) findViewById(R.id.register_activate_with_email);
        mActivateWithTelegram = (RadioButton) findViewById(R.id.register_activate_with_telegram);

        mMobileNumberEditText.addTextChangedListener(new GenericGravityTextWatcher(mMobileNumberEditText));
        mEmailAddressEditText.addTextChangedListener(new GenericGravityTextWatcher(mEmailAddressEditText));
        mPasswordEdiText.addTextChangedListener(new GenericGravityTextWatcher(mPasswordEdiText));
        mConfirmPasswordEditText.addTextChangedListener(new GenericGravityTextWatcher(mConfirmPasswordEditText));
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
            signUserUpInParse();
        }
        if (viewId == R.id.register_activate_with_email || viewId == R.id.register_activate_with_telegram) {
            changeMandatoryFieldsStarMarker();
        }
    }

    /*
        public void registerUser() {
            final File nationalCardImageFile = new File(mNationalCardImageView.getFinalImageUri().getPath());
            try {
                final ParseFile parseFile = ParseIO.getParseFileFromFile("nationalCardImageFile.jpeg", nationalCardImageFile);

                parseFile.saveInBackground(new SaveCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            try {
                                ParseIO.copyFile(nationalCardImageFile, new File(HonarnamaBaseApp.APP_IMAGES_FOLDER, HonarnamaSellApp.NATIONAL_CARD_FILE_NAME));
                            } catch (IOException e1) {
                                if (BuildConfig.DEBUG) {
                                    Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                            "Error copying national card image to sd card " + e1, e1);
                                } else {
                                    Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Error copying national card image to sd card"
                                            + e1.getMessage());
                                }
                            }
                            signUserUpInParse(parseFile, sendingDataProgressDialog);
                        } else {
                            Toast.makeText(RegisterActivity.this, " خطا در ارسال تصویر. لطفاً دوباره تلاش کنید. ", Toast.LENGTH_LONG).show();
                            logE("Uploading National Card Image Failed. Code: " + e.getCode(),
                                    e.getMessage(), e);
                            sendingDataProgressDialog.dismiss();
                        }
                    }
                }, new ProgressCallback() {
                    public void done(Integer percentDone) {

                        logD(null, "Uploading National Card Image - percentDone= " + percentDone);
                        // Update your progress spinner here. percentDone will be between 0 and 100.
                    }
                });

            } catch (IOException ioe) {
                Toast.makeText(RegisterActivity.this, " خطا در ارسال تصویر. لطفاً دوباره تلاش کنید. ", Toast.LENGTH_LONG).show();
                logE("IOException while uploading file: " + ioe.getMessage(), "nationalCardImageFile= " + nationalCardImageFile, ioe);
                sendingDataProgressDialog.dismiss();
            }
        }
    */
    private void signUserUpInParse() {
        if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
            return;
        }

        if (!enteredValuesAreValid()) {
            return; // TODO: feedback
        }

        ParseUser currentUser = HonarnamaUser.getCurrentUser();
        if (currentUser != null) {
            // do stuff with the user
            HonarnamaUser.logOut();
        }

        final ProgressDialog sendingDataProgressDialog = new ProgressDialog(RegisterActivity.this);
        sendingDataProgressDialog.setCancelable(false);
        sendingDataProgressDialog.setMessage(getString(R.string.sending_data));
        sendingDataProgressDialog.show();

        final String activationMethod = mActivateWithEmail.isChecked() ? "email" : "mobileNumber";

        final ParseUser user = new ParseUser();

        if ("email".equals(activationMethod)) {
            user.setUsername(mEmailAddressEditText.getText().toString().trim());
        } else {
            user.setUsername(mMobileNumberEditText.getText().toString().trim());
        }

        if (mEmailAddressEditText.getText().toString().trim().length() == 0 || "mobileNumber".equals(activationMethod)) {
            user.setEmail(mMobileNumberEditText.getText().toString().trim() + "@" + HonarnamaBaseApp.DOMAIN);
        } else {
            user.setEmail(mEmailAddressEditText.getText().toString().trim());
        }

        if (mActivateWithEmail.isChecked()) {
            user.setPassword(mPasswordEdiText.getText().toString());
        } else {
            user.setPassword(mMobileNumberEditText.getText().toString().trim());
        }

        user.put("userEnteredEmailAddress", mEmailAddressEditText.getText().toString().trim());
        user.put("mobileNumber", mMobileNumberEditText.getText().toString().trim());
        user.put("name", mNameEditText.getText().toString().trim());
        user.put("activationMethod", activationMethod);

        int genderCode = mGenderWoman.isChecked() ? HonarnamaBaseApp.GENDER_CODE_WOMAN : (mGenderMan.isChecked() ? HonarnamaBaseApp.GENDER_CODE_MAN : HonarnamaBaseApp.GENDER_CODE_NOT_SAID);
        user.put("gender", genderCode);

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                sendingDataProgressDialog.dismiss();
                if (e == null) {
                    user.fetchInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            sendUserBackToCallingActivity();
                        }
                    });
                } else {
                    if (e.getCode() == 202) {
                        if ("email".equals(activationMethod)) {
                            mEmailAddressEditText.setError(getString(R.string.error_signup_duplicated_email));
                        } else {
                            mMobileNumberEditText.setError(getString(R.string.error_signup_duplicated_mobile_number));
                        }
                    }

                    Toast.makeText(RegisterActivity.this, getString(R.string.error_signup_correct_mistakes_and_try_again), Toast.LENGTH_LONG).show();
                    logE("Sign-up Failed. Code: " + e.getCode() +
                            " // Error Msg: " + e.getMessage(), "", e);
                }
            }
        });
    }

    private void changeMandatoryFieldsStarMarker() {

        if (mActivateWithEmail.isChecked()) {
            findViewById(R.id.register_email_star_marker).setVisibility(View.VISIBLE);
            findViewById(R.id.register_mobile_number_star_marker).setVisibility(View.GONE);

            findViewById(R.id.register_password_layer).setVisibility(View.VISIBLE);

        } else {
            findViewById(R.id.register_email_star_marker).setVisibility(View.GONE);
            findViewById(R.id.register_mobile_number_star_marker).setVisibility(View.VISIBLE);

            findViewById(R.id.register_password_layer).setVisibility(View.GONE);
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

        if (mActivateWithEmail.isChecked()) {
            if (mPasswordEdiText.getText().toString().trim().length() == 0) {
                mPasswordEdiText.requestFocus();
                mPasswordEdiText.setError(getString(R.string.error_password_field_can_not_be_empty));
                return false;
            }

            if (mConfirmPasswordEditText.getText().toString().trim().length() == 0) {
                mConfirmPasswordEditText.requestFocus();
                mConfirmPasswordEditText.setError(getString(R.string.error_confirm_password_field_cant_be_empty));
                return false;
            }

            if (!mConfirmPasswordEditText.getText().toString().equals(mPasswordEdiText.getText().toString())) {
                mPasswordEdiText.requestFocus();
                mPasswordEdiText.setError(getString(R.string.error_password_and_confirmpassword_does_not_match));
                return false;
            }
        }

        /*
        if (mNationalCardImageView.getFinalImageUri() == null) {
            mNationalCardImageView.requestFocus();
            TextView nationalCardTitleTextView = (TextView) findViewById(R.id.register_national_card_title_text_view);
            nationalCardTitleTextView.setError(getString(R.string.error_national_card_image_is_not_set));
            return false;
        }
        if (mBankCardNumberEditText.getText().toString().trim().length() == 0) {
            mBankCardNumberEditText.requestFocus();
            mBankCardNumberEditText.setError(getString(R.string.error_bank_card_number_cant_be_empty));
            return false;
        }

        String bankCardNumberPattern = "^((\\d{4}-\\d{4}-\\d{4}-\\d{4})|(\\d{4}\\s{1}\\d{4}\\s{1}\\d{4}\\s{1}\\d{4})|(\\d{16}))$";
        if (!mBankCardNumberEditText.getText().toString().matches(bankCardNumberPattern)) {
            mBankCardNumberEditText.requestFocus();
            mBankCardNumberEditText.setError(getString(R.string.error_bank_card_number_is_not_valid));
            return false;
        }
        */
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        /*
        if ((mNationalCardImageView != null) &&
                mNationalCardImageView.onActivityResult(requestCode, resultCode, intent)) {
            return;
        }
        */
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
        /*
        if (mNationalCardImageView != null) {
            mNationalCardImageView.onSaveInstanceState(outState);
        }
        */
    }

    private void sendUserBackToCallingActivity() {
        Intent intent = new Intent();

        switch (HonarnamaUser.getActivationMethod()) {
            case EMAIL:
                intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_EMAIL, true);
                break;

            case MOBILE_NUMBER:
                intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_DISPLAY_REGISTER_SNACK_FOR_MOBILE, true);
                break;

        }

        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
