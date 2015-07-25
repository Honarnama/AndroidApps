package net.honarnama.sell;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import net.honarnama.HonarNamaBaseActivity;
import net.honarnama.utils.GenericGravityTextWatcher;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class RegisterActivity extends HonarNamaBaseActivity implements View.OnClickListener {
    private TextView mAddNationalCardTextView;
    private ImageView mNationalCardImageView;
    private String[] mNationalCardImageSourceProvider;
    private String mNationalCardPhotoPath;

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
    private boolean mNationalCardImageIsSet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mNationalCardImageIsSet = false;
        mAddNationalCardTextView = (TextView) findViewById(R.id.register_national_card_title_text_view);
        mAddNationalCardTextView.setOnClickListener(this);

        mNationalCardImageView = (ImageView) findViewById(R.id.register_national_card_image_view);
        mNationalCardImageView.setOnClickListener(this);

        mNationalCardImageSourceProvider = new String[]{getString(R.string.camera_option_text), getString(R.string.choose_from_gallery_option_text)};

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

        logI(null, "created!");
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
            case R.id.register_national_card_image_view:
                takePhoto();
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

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Honarnama_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mNationalCardPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    public void takePhoto() {
        final AlertDialog.Builder nationalCardImageOptionDialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.DialogStyle));
        nationalCardImageOptionDialog.setTitle(getString(R.string.select_national_card_image_dialog_title));
        nationalCardImageOptionDialog.setItems(mNationalCardImageSourceProvider, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    Uri.fromFile(photoFile));
                            startActivityForResult(takePictureIntent, 1001);
                        }

                    }
                } else if (which == 1) {
                    Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhotoIntent, 1002);
                }
                dialog.dismiss();
            }
        });
        nationalCardImageOptionDialog.show();


    }

    public void registerSeller() {

        if (!enteredValuesAreValid()) {
            return; // TODO: feedback
        }

        mRegisterButton.setEnabled(false);

        Bitmap bitmap = ((BitmapDrawable) mNationalCardImageView.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] nationalCardImageFile = stream.toByteArray();
        final ParseFile parseFile = new ParseFile("nationalCardImageFile.jpeg", nationalCardImageFile);

        parseFile.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    signUserUpInParse(parseFile);
                } else {
                    logE("Uploading National Card Image Failed. Code: " + e.getCode(),
                            e.getMessage(), e);
                    mRegisterButton.setEnabled(true);
                }
            }
        }, new ProgressCallback() {
            public void done(Integer percentDone) {

                Log.d(getLocalClassName(), "Uploading National Card Image - percentDone= " + percentDone);
                // Update your progress spinner here. percentDone will be between 0 and 100.
            }
        });


    }

    private void signUserUpInParse(ParseFile parseFile) {
        final String activationMethod = mActivateWithEmail.isChecked() ? "email" : "mobileNumber";

        final ParseUser user = new ParseUser();

        if ("email".equals(activationMethod)) {
            user.setUsername(mEmailAddressEditText.getText().toString());
        } else {
            user.setUsername(mMobileNumberEditText.getText().toString());
        }

        user.setPassword(mPasswordEdiText.getText().toString());
        user.setEmail(mEmailAddressEditText.getText().toString());

        user.put("mobileNumber", mMobileNumberEditText.getText().toString());
        user.put("firstname", mFirstnameEditText.getText().toString());
        user.put("lastname", mLastnameEditText.getText().toString());
        user.put("activationMethod", activationMethod);
        user.put("bankCardNumber", mBankCardNumberEditText.getText().toString());
        user.put("isShopOwner", true);
        user.put("nationalCardImage", parseFile);

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    user.fetchInBackground(new GetCallback<ParseObject>() {

                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            if ("mobileNumber".equals(activationMethod)) {
                                showTelegramActivationDialog(parseObject.getString("telegramCode"));
                            }
                            Toast.makeText(RegisterActivity.this, "Signup Done!", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(RegisterActivity.this, "Signup Failed!", Toast.LENGTH_LONG).show();
                    logE("Sign-up Failed. Code: " + e.getCode(),
                            e.getMessage(), e);
                }
                mRegisterButton.setEnabled(false);
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
                                startActivityForResult(telegramIntent, 1003);
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
            if (mEmailAddressEditText.getText().toString().length() == 0) {
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
            if (mMobileNumberEditText.getText().toString().length() == 0) {
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

        if (mPasswordEdiText.getText().toString().length() == 0) {
            mPasswordEdiText.requestFocus();
            mPasswordEdiText.setError(getString(R.string.error_password_field_can_not_be_empty));
            return false;
        }

        if (mConfirmPasswordEditText.getText().toString().length() == 0) {
            mConfirmPasswordEditText.requestFocus();
            mConfirmPasswordEditText.setError(getString(R.string.error_confirm_password_field_cant_be_empty));
            return false;
        }

        if (!mConfirmPasswordEditText.getText().toString().equals(mPasswordEdiText.getText().toString())) {
            mPasswordEdiText.requestFocus();
            mPasswordEdiText.setError(getString(R.string.error_password_and_confirmpassword_does_not_match));
            return false;
        }

        if (!mNationalCardImageIsSet) {
            mNationalCardImageView.requestFocus();
            TextView nationalCardTitleTextView = (TextView) findViewById(R.id.register_national_card_title_text_view);
            nationalCardTitleTextView.setError(getString(R.string.error_national_card_image_is_not_set));
            return false;
        }
        if (mBankCardNumberEditText.getText().toString().length() == 0) {
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

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case 1001:
                if (resultCode == RESULT_OK) {
                    mNationalCardImageView.setImageURI(Uri.parse(mNationalCardPhotoPath));
                    mNationalCardImageIsSet = true;
                }
                break;
            case 1002:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    mNationalCardPhotoPath = selectedImage.getPath();
                    mNationalCardImageView.setImageURI(selectedImage);
                    mNationalCardImageIsSet = true;
                }
                break;
            case 1003:
                finish();
                break;
        }
    }
}
