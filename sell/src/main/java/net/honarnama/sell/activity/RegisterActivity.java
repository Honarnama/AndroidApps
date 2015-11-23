package net.honarnama.sell.activity;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import net.honarnama.HonarnamaBaseActivity;
import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;

import com.parse.ImageSelector;

import net.honarnama.utils.GenericGravityTextWatcher;
import net.honarnama.utils.NetworkManager;
import net.honarnama.utils.ParseIO;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


public class RegisterActivity extends HonarnamaBaseActivity implements View.OnClickListener {
    private TextView mAddNationalCardTextView;
    private ImageSelector mNationalCardImageView;

    private EditText mLastnameEditText;
    private EditText mFirstnameEditText;
    private EditText mMobileNumberEditText;
    private EditText mEmailAddressEditText;
    private EditText mPasswordEdiText;
    private EditText mConfirmPasswordEditText;
    private EditText mBankCardNumberEditText;

    private RadioButton mActivateWithEmail;
    private RadioButton mActivateWithMobileNumber;

    private Button mRegisterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAddNationalCardTextView = (TextView) findViewById(R.id.register_national_card_title_text_view);
        mAddNationalCardTextView.setOnClickListener(this);

        mNationalCardImageView = (ImageSelector) findViewById(R.id.register_national_card_image_view);
        mNationalCardImageView.setActivity(this);
        mNationalCardImageView.restore(savedInstanceState);

        mNationalCardImageView.setOnImageSelectedListener(new ImageSelector.OnImageSelectedListener() {
            @Override
            public boolean onImageSelected(Uri selectedImage, boolean cropped) {
                return true;
            }

            @Override
            public void onImageRemoved() { }

            @Override
            public void onImageSelectionFailed() { }
        });

        mFirstnameEditText = (EditText) findViewById(R.id.register_firstname_edit_text);
        mLastnameEditText = (EditText) findViewById(R.id.register_lastname_edit_text);
        mMobileNumberEditText = (EditText) findViewById(R.id.register_mobile_number_edit_text);
        mEmailAddressEditText = (EditText) findViewById(R.id.register_email_address_edit_text);
        mPasswordEdiText = (EditText) findViewById(R.id.register_password_edit_text);
        mConfirmPasswordEditText = (EditText) findViewById(R.id.register_confirm_password_edit_text);
        mBankCardNumberEditText = (EditText) findViewById(R.id.register_bank_card_number_edit_text);
        mRegisterButton = (Button) findViewById(R.id.register_button);

        mActivateWithEmail = (RadioButton) findViewById(R.id.register_activate_with_email);
        mActivateWithMobileNumber = (RadioButton) findViewById(R.id.register_activate_with_telegram);

        mMobileNumberEditText.addTextChangedListener(new GenericGravityTextWatcher(mMobileNumberEditText));
        mEmailAddressEditText.addTextChangedListener(new GenericGravityTextWatcher(mEmailAddressEditText));
        mPasswordEdiText.addTextChangedListener(new GenericGravityTextWatcher(mPasswordEdiText));
        mConfirmPasswordEditText.addTextChangedListener(new GenericGravityTextWatcher(mConfirmPasswordEditText));
        mBankCardNumberEditText.addTextChangedListener(new GenericGravityTextWatcher(mBankCardNumberEditText));
        mRegisterButton.setOnClickListener(this);
        mActivateWithEmail.setOnClickListener(this);
        mActivateWithMobileNumber.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
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
            case R.id.register_national_card_title_text_view:
                mNationalCardImageView.selectPhoto();
                break;

            case R.id.register_button:
                registerSeller();
                break;

            case R.id.register_activate_with_email:
            case R.id.register_activate_with_telegram:
                changeMandatoryFieldsStarMarker();
            default:
                break;

        }

    }

    public void registerSeller() {

        if (!NetworkManager.getInstance().isNetworkEnabled(this, true)) {
            return;
        }

        if (!enteredValuesAreValid()) {
            return; // TODO: feedback
        }

        final ProgressDialog sendingDataProgressDialog = new ProgressDialog(RegisterActivity.this);
        sendingDataProgressDialog.setCancelable(false);
        sendingDataProgressDialog.setMessage(getString(R.string.sending_data));
        sendingDataProgressDialog.show();

//        Bitmap bitmap = ((BitmapDrawable) mNationalCardImageView.getDrawable()).getBitmap();
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
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

    private void signUserUpInParse(ParseFile parseFile, ProgressDialog sendingDataProgressDialog) {
        final String activationMethod = mActivateWithEmail.isChecked() ? "email" : "mobileNumber";

        final ParseUser user = new ParseUser();

        if ("email".equals(activationMethod)) {
            user.setUsername(mEmailAddressEditText.getText().toString().trim());
        } else {
            user.setUsername(mMobileNumberEditText.getText().toString().trim());
        }

        if (mEmailAddressEditText.getText().toString().trim().length() == 0) {
            user.setEmail(mMobileNumberEditText.getText().toString().trim() + "@" + HonarnamaBaseApp.DOMAIN);
        } else {
            user.setEmail(mEmailAddressEditText.getText().toString().trim());
        }

        user.setPassword(mPasswordEdiText.getText().toString());

        user.put("mobileNumber", mMobileNumberEditText.getText().toString().trim());
        user.put("firstname", mFirstnameEditText.getText().toString().trim());
        user.put("lastname", mLastnameEditText.getText().toString().trim());
        user.put("activationMethod", activationMethod);
        user.put("bankCardNumber", mBankCardNumberEditText.getText().toString().trim());
        user.put("isShopOwner", true);
        user.put("nationalCardImage", parseFile);
        final ProgressDialog progressDialog = sendingDataProgressDialog;
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                progressDialog.dismiss();
                if (e == null) {
                    user.fetchInBackground(new GetCallback<ParseObject>() {

                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            if ("mobileNumber".equals(activationMethod)) {
                                showTelegramActivationDialog(parseObject.getString("telegramCode"));
                                Toast.makeText(RegisterActivity.this, getString(R.string.successful_signup), Toast.LENGTH_LONG).show();
                            }

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
                    logE("Sign-up Failed. Code: " + e.getCode(),
                            e.getMessage(), e);
                }
            }
        });
    }

    private void showTelegramActivationDialog(final String activationCode) {
        final AlertDialog.Builder telegramActivationDialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.DialogStyle));
        telegramActivationDialog.setTitle(getString(R.string.telegram_activation_dialog_title));
        telegramActivationDialog.setItems(new String[]{getString(R.string.telegram_activation_option_text)},
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

        if (mActivateWithMobileNumber.isChecked()) {
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

        return true;

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if ((mNationalCardImageView != null) &&
                mNationalCardImageView.onActivityResult(requestCode, resultCode, intent)) {
            return;
        }
        switch (requestCode) {
            case HonarnamaBaseApp.INTENT_TELEGRAM_CODE:
                finish();
                break;
            default:
                logD(null, "Unexpected requestCode= " + requestCode);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mNationalCardImageView != null) {
            mNationalCardImageView.onSaveInstanceState(outState);
        }
    }
}
