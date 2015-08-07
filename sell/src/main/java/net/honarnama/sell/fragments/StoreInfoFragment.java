package net.honarnama.sell.fragments;

import com.makeramen.roundedimageview.RoundedDrawable;
import com.makeramen.roundedimageview.RoundedImageView;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.honarnama.HonarNamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.sell.R;
import net.honarnama.utils.file.ImageSelector;
import net.honarnama.utils.file.SimpleImageCropper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class StoreInfoFragment extends Fragment implements View.OnClickListener {

    private ImageSelector mImageSelector;
    private SimpleImageCropper mSimpleImageCropper;

    private EditText mStoreNameEditText;
    private EditText mStorePlicyEditText;
    private Button mRegisterStoreButton;
    private RoundedImageView mStoreLogoImageView;
    public File mCroppedFile;

    File mTempImageFile;

    public static StoreInfoFragment newInstance() {
        StoreInfoFragment fragment = new StoreInfoFragment();
        return fragment;
    }

    public StoreInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_store_info, container, false);
        // Inflate the layout for this fragment

        mStoreNameEditText = (EditText) rootView.findViewById(R.id.store_name_edit_text);
        mStorePlicyEditText = (EditText) rootView.findViewById(R.id.store_policy_edit_text);
        mRegisterStoreButton = (Button) rootView.findViewById(R.id.register_store_button);
        mStoreLogoImageView = (RoundedImageView) rootView.findViewById(R.id.store_logo_image_view);

        mRegisterStoreButton.setOnClickListener(this);
        mStoreLogoImageView.setOnClickListener(this);

        mSimpleImageCropper = new SimpleImageCropper(getActivity());

        mCroppedFile = new File(HonarNamaBaseApp.imagesFolder, "store_logo.jpg");
        return rootView;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //TODO remove if not needed
//        (ControlPanelActivity)activity).onSectionAttached(1);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.register_store_button:
                if (isFormInputsValid()) {
                    uploadStoreLogo();
                }
                break;
            case R.id.store_logo_image_view:
                mImageSelector = new ImageSelector(getActivity(), getActivity());
                if (mTempImageFile == null) {
                    try {
                        mTempImageFile = mImageSelector.createImageFile();
                    } catch (Exception e) {
                        if (BuildConfig.DEBUG) {
                            Log.e(HonarNamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "Error creating temp Image file: " + e.getMessage() + " // " + e);
                        } else {
                            Log.e(HonarNamaBaseApp.PRODUCTION_TAG, "Error creating temp Image file: " + e.getMessage() + " // " + e);
                        }
                    }
                }
                mImageSelector.selectPhoto(mTempImageFile);
                break;
            default:
                break;
        }

    }

    private boolean isFormInputsValid() {
        if (mStoreNameEditText.getText().toString().trim().length() == 0) {
            mStoreNameEditText.requestFocus();
            mStoreNameEditText.setError(" نام فروشگاه نمیتواند خالی باشد. ");
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case HonarNamaBaseApp.INTENT_CAPTURE_IMAGE_CODE:
                if (resultCode == getActivity().RESULT_OK) {
                    Uri imageUri = Uri.parse("file:" + mTempImageFile.getAbsolutePath());
//                    Bitmap photo = (Bitmap) intent.getExtras().get("data");
                    if (mSimpleImageCropper.checkIfDeviceSupportsImageCrop()) {
                        mSimpleImageCropper.crop(imageUri, mCroppedFile, HonarNamaBaseApp.INTENT_CROP_IMAGE_CODE, 400, 400, 10, 10);
                    } else {
                        try {
                            mImageSelector.copy(mTempImageFile, mCroppedFile);
                        } catch (IOException e) {
                            if (BuildConfig.DEBUG) {
                                Log.e(HonarNamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "Error copying Image file: " + e.getMessage() + " // " + e);
                            } else {
                                Log.e(HonarNamaBaseApp.PRODUCTION_TAG, "Error copying Image file: " + e.getMessage() + " // " + e);
                            }
                        }
                        mStoreLogoImageView.setImageURI(Uri.fromFile(mCroppedFile));
                    }
                }
                break;
            case HonarNamaBaseApp.INTENT_SELECT_IMAGE_CODE:
                if (resultCode == getActivity().RESULT_OK) {
                    Uri imageUri = intent.getData();
                    if (mSimpleImageCropper.checkIfDeviceSupportsImageCrop()) {
                        mSimpleImageCropper.crop(imageUri, mCroppedFile, HonarNamaBaseApp.INTENT_CROP_IMAGE_CODE, 400, 400, 10, 10);
                    } else {
                        try {
                            mImageSelector.copy(new File(imageUri.getPath()), mCroppedFile);
                        } catch (IOException e) {
                            if (BuildConfig.DEBUG) {
                                Log.e(HonarNamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "Error copying Image file: " + e.getMessage() + " // " + e);
                            } else {
                                Log.e(HonarNamaBaseApp.PRODUCTION_TAG, "Error copying Image file: " + e.getMessage() + " // " + e);
                            }
                        }
                        mStoreLogoImageView.setImageURI(Uri.fromFile(mCroppedFile));
                    }
                }
                break;
            case HonarNamaBaseApp.INTENT_CROP_IMAGE_CODE:
                mStoreLogoImageView.setImageURI(Uri.fromFile(mCroppedFile));
                break;
        }
    }
    //TODO remove temp file

    public void uploadStoreLogo() {
        final ProgressDialog sendingDataProgressDialog = new ProgressDialog(getActivity());
        sendingDataProgressDialog.setCancelable(false);
        sendingDataProgressDialog.setMessage(getString(R.string.sending_data));
        sendingDataProgressDialog.show();

        Bitmap bitmap = ((RoundedDrawable) mStoreLogoImageView.getDrawable()).getSourceBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] storeLogoImageFile = stream.toByteArray();
        final ParseFile parseFile = new ParseFile("store_logo.jpeg", storeLogoImageFile);

        parseFile.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    registerStore(parseFile, sendingDataProgressDialog);
                } else {
                    Toast.makeText(getActivity(), " خطا در ارسال تصویر. لطفاً دوباره تلاش کنید. ", Toast.LENGTH_LONG).show();
                    if (BuildConfig.DEBUG) {
                        Log.e(HonarNamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "Uploading Store Logo Failed. Code: " + e.getCode() +
                                "//" + e.getMessage() + " // " + e);
                    } else {
                        Log.e(HonarNamaBaseApp.PRODUCTION_TAG, "Uploading Store Logo Failed. Code: " + e.getCode() +
                                "//" + e.getMessage() + " // " + e);
                    }
                    sendingDataProgressDialog.dismiss();
                }
            }
        }, new ProgressCallback() {
            public void done(Integer percentDone) {
                if (BuildConfig.DEBUG) {
                    Log.d(HonarNamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "Uploading Store Logo Image - percentDone= " + percentDone);
                } else {
                    Log.d(HonarNamaBaseApp.PRODUCTION_TAG, "Uploading Store Logo Image - percentDone= " + percentDone);
                    // Update your progress spinner here. percentDone will be between 0 and 100.
                }

            }
        });
    }

    public void registerStore(ParseFile parseFile, ProgressDialog progressDialog)
    {
        ParseUser currentUser = ParseUser.getCurrentUser();

        ParseObject storeInfo = new ParseObject("storeInfo");
        storeInfo.put("name", mStoreNameEditText.getText().toString().trim());
        storeInfo.put("logo", parseFile);
        storeInfo.put("policy", mStorePlicyEditText.getText().toString().trim());
        storeInfo.put("owner", currentUser);

        storeInfo.saveInBackground();

        progressDialog.dismiss();
    }
}
