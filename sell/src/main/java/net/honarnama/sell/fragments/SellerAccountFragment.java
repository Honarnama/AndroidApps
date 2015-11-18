package net.honarnama.sell.fragments;


import com.parse.GetDataCallback;
import com.parse.ImageSelector;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import net.honarnama.HonarNamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.sell.HonarNamaSellApp;
import net.honarnama.sell.R;
import net.honarnama.utils.GenericGravityTextWatcher;
import net.honarnama.utils.NetworkManager;
import net.honarnama.utils.ParseIO;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 */
public class SellerAccountFragment extends Fragment implements View.OnClickListener {

    public static SellerAccountFragment mSellerAccountFragment;

    private EditText mFirstnameEditText;
    private EditText mLastnameEditText;
    private Button mAlterNameButton;
    private ParseUser mCurrentUser;
    private EditText mNewPasswordEditText;

    private Button mChangePasswordButton;
    private RelativeLayout mVerificationDocsLayer;

    private EditText mBankCardNumberEditText;
    private ImageSelector mNationalCardImageView;
    private Button mResendVerificationDocsButton;

    private TextView mNationalCardTitleTextView;

    public synchronized static SellerAccountFragment getInstance() {
        if (mSellerAccountFragment == null) {
            mSellerAccountFragment = new SellerAccountFragment();
        }
        return mSellerAccountFragment;
    }

    public SellerAccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mCurrentUser = ParseUser.getCurrentUser();
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_seller_account, container, false);

        mFirstnameEditText = (EditText) rootView.findViewById(R.id.seller_account_firstname_edit_text);
        mLastnameEditText = (EditText) rootView.findViewById(R.id.seller_account_lastname_edit_text);
        mFirstnameEditText.setText(mCurrentUser.get("firstname").toString());
        mLastnameEditText.setText(mCurrentUser.get("lastname").toString());
        mAlterNameButton = (Button) rootView.findViewById(R.id.seller_account_alter_name_button);
        mAlterNameButton.setOnClickListener(this);

        mNewPasswordEditText = (EditText) rootView.findViewById(R.id.seller_account_new_password_edit_text);
        mChangePasswordButton = (Button) rootView.findViewById(R.id.seller_account_alter_password_button);
        mChangePasswordButton.setOnClickListener(this);

        mNewPasswordEditText.addTextChangedListener(new GenericGravityTextWatcher(mNewPasswordEditText));

        mVerificationDocsLayer = (RelativeLayout) rootView.findViewById(R.id.seller_accouunt_verification_docs_layer);

        if (mCurrentUser.has("isVerifiedShopOwner")) {
            if (!Boolean.valueOf(mCurrentUser.get("isVerifiedShopOwner").toString())) {
                mVerificationDocsLayer.setVisibility(View.VISIBLE);
                mBankCardNumberEditText = (EditText) rootView.findViewById(R.id.seller_account_bank_card_number_edit_text);
                mBankCardNumberEditText.setText(mCurrentUser.get("bankCardNumber").toString());
                mBankCardNumberEditText.addTextChangedListener(new GenericGravityTextWatcher(mBankCardNumberEditText));
                mResendVerificationDocsButton = (Button) rootView.findViewById(R.id.seller_account_resend_verification_docs_button);
                mResendVerificationDocsButton.setOnClickListener(this);
                mNationalCardTitleTextView = (TextView) rootView.findViewById(R.id.seller_account_national_card_title_text_view);

                mNationalCardImageView = (ImageSelector) rootView.findViewById(R.id.seller_account_national_card_image_view);
                mNationalCardImageView.setActivity(getActivity());
                mNationalCardImageView.restore(savedInstanceState);

                mNationalCardImageView.loadInBackground(mCurrentUser.getParseFile("nationalCardImage"), new GetDataCallback() {
                    @Override
                    public void done(byte[] data, ParseException e) {
                        mNationalCardImageView.setImageIsLoaded(true);
                    }
                });
                mNationalCardImageView.setOnImageSelectedListener(new ImageSelector.OnImageSelectedListener() {
                    @Override
                    public boolean onImageSelected(Uri selectedImage, boolean cropped) {
                        return true;
                    }

                    @Override
                    public boolean onImageRemoved() {
                        return false;
                    }

                    @Override
                    public void onImageSelectionFailed() {
                    }
                });
            }
        }


        return rootView;
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.seller_account_alter_name_button:
                if (!NetworkManager.getInstance().isNetworkEnabled(getActivity(), true)) {
                    return;
                }
                changeUserName();
                break;
            case R.id.seller_account_alter_password_button:
                if (!NetworkManager.getInstance().isNetworkEnabled(getActivity(), true)) {
                    return;
                }
                if (mNewPasswordEditText.getText().toString().trim().length() > 0) {
                    Toast.makeText(getActivity(), mNewPasswordEditText.toString().trim().length() + "", Toast.LENGTH_LONG).show();
                    changePassword();
                } else {
                    mNewPasswordEditText.requestFocus();
                    mNewPasswordEditText.setError(getString(R.string.error_password_field_can_not_be_empty));
                }
                break;
            case R.id.seller_account_resend_verification_docs_button:
                if (!NetworkManager.getInstance().isNetworkEnabled(getActivity(), true)) {
                    return;
                }
                if (verificationDocsAreValid()) {
                    uploadNationalCardImage();
                }
                break;

        }

    }

    private void changeUserName() {
        final ProgressDialog sendingDataProgressDialog = new ProgressDialog(getActivity());
        sendingDataProgressDialog.setCancelable(false);
        sendingDataProgressDialog.setMessage(getString(R.string.sending_data));
        sendingDataProgressDialog.show();


        mCurrentUser.put("firstname", mFirstnameEditText.getText().toString().trim());
        mCurrentUser.put("lastname", mLastnameEditText.getText().toString().trim());

        mCurrentUser.pinInBackground();
        mCurrentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.successfully_changed_user_name), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.changing_user_name_failed), Toast.LENGTH_LONG).show();
                    if (BuildConfig.DEBUG) {
                        Log.e(HonarNamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                "Error changing user name.  Error Code: " + e.getCode() +
                                        "//" + e.getMessage() + " // " + e, e);
                    } else {
                        Log.e(HonarNamaBaseApp.PRODUCTION_TAG, "Error changing user name. "
                                + e.getMessage());
                    }
                }
                sendingDataProgressDialog.dismiss();
            }
        });
    }

    private void changePassword() {
        final ProgressDialog sendingDataProgressDialog = new ProgressDialog(getActivity());
        sendingDataProgressDialog.setCancelable(false);
        sendingDataProgressDialog.setMessage(getString(R.string.sending_data));
        sendingDataProgressDialog.show();

        mCurrentUser.setPassword(mNewPasswordEditText.getText().toString());
        mCurrentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                sendingDataProgressDialog.dismiss();
                if (null == e) {
                    Toast.makeText(getActivity(), R.string.successfully_changed_password, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), R.string.changing_password_failed, Toast.LENGTH_LONG).show();
                    if (BuildConfig.DEBUG) {
                        Log.e(HonarNamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                "Error changing password.  Error Code: " + e.getCode() +
                                        "//" + e.getMessage() + " // " + e, e);
                    } else {
                        Log.e(HonarNamaBaseApp.PRODUCTION_TAG, "Error changing password. "
                                + e.getMessage());
                    }
                }
            }
        });

    }

    //TODO Load ImageView from DB
    private boolean verificationDocsAreValid() {
        if (!mNationalCardImageView.getImageIsLoaded() && mNationalCardImageView.getFinalImageUri() == null) {
            mNationalCardImageView.requestFocus();
            mNationalCardTitleTextView.setError(getString(R.string.error_national_card_image_is_not_set));
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

    private void uploadNationalCardImage() {
        if (!NetworkManager.getInstance().isNetworkEnabled(getActivity(), true)) {
            return;
        }

        final ProgressDialog sendingDataProgressDialog = new ProgressDialog(getActivity());
        sendingDataProgressDialog.setCancelable(false);
        sendingDataProgressDialog.setMessage(getString(R.string.sending_data));
        sendingDataProgressDialog.show();

        if (mNationalCardImageView.getFinalImageUri() == null) {
            if (mNationalCardImageView.getImageIsLoaded()) {
                //No need to upload file
                resendVerificationDocs(null, sendingDataProgressDialog);
            }
            return;
        }

        final File nationalCardImageFile = new File(mNationalCardImageView.getFinalImageUri().getPath());
        try {
            final ParseFile parseFile = ParseIO.getParseFileFromFile(HonarNamaSellApp.NATIONAL_CARD_FILE_NAME,
                    nationalCardImageFile);
            parseFile.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        try {
                            ParseIO.copyFile(nationalCardImageFile, new File(HonarNamaBaseApp.APP_IMAGES_FOLDER, HonarNamaSellApp.NATIONAL_CARD_FILE_NAME));
                        } catch (IOException e1) {
                            if (BuildConfig.DEBUG) {
                                Log.e(HonarNamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                        "Error copying national card image to sd card " + e1, e1);
                            } else {
                                Log.e(HonarNamaBaseApp.PRODUCTION_TAG, "Error copying national card image to sd card"
                                        + e1.getMessage());
                            }
                        }
                        mNationalCardImageView.setImageIsLoaded(true);
                        resendVerificationDocs(parseFile, sendingDataProgressDialog);
                    } else {
                        Toast.makeText(getActivity(), R.string.uploading_image_failed, Toast.LENGTH_LONG).show();
                        if (BuildConfig.DEBUG) {
                            Log.e(HonarNamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "Uploading national card image Failed. Code: " + e.getCode() +
                                    "//" + e.getMessage() + " // " + e);
                        } else {
                            Log.e(HonarNamaBaseApp.PRODUCTION_TAG, "Uploading national card image Failed. Code: " + e.getCode() +
                                    "//" + e.getMessage() + " // " + e);
                        }
                        sendingDataProgressDialog.dismiss();
                    }
                }
            });
        } catch (IOException ioe) {
            Toast.makeText(getActivity(), R.string.uploading_image_failed,
                    Toast.LENGTH_LONG).show();

            if (BuildConfig.DEBUG) {
                Log.e(HonarNamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                        "Failed on preparing national card image. " + ioe, ioe);
            } else {
                Log.e(HonarNamaBaseApp.PRODUCTION_TAG, "Failed on preparing national card image. ioe="
                        + ioe.getMessage());
            }

            sendingDataProgressDialog.dismiss();
        }
    }

    private void resendVerificationDocs(ParseFile parseFile, final ProgressDialog progressDialog) {

        mCurrentUser.put("bankCardNumber", mBankCardNumberEditText.getText().toString().trim());
        if (parseFile != null) {
            mCurrentUser.put("nationalCardImage", parseFile);
        }

        mCurrentUser.pinInBackground();
        mCurrentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

                if (e == null) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.successfully_sent_vereification_docs), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.sending_verification_docs_failed), Toast.LENGTH_LONG).show();
                    if (BuildConfig.DEBUG) {
                        Log.e(HonarNamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                "Failed on sending verification docs. " + e, e);
                    } else {
                        Log.e(HonarNamaBaseApp.PRODUCTION_TAG, "Failed on sending verification docs. error msg="
                                + e.getMessage());
                    }
                }
                progressDialog.dismiss();

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mNationalCardImageView.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mNationalCardImageView != null) {
            mNationalCardImageView.onSaveInstanceState(outState);
        }
    }
}
