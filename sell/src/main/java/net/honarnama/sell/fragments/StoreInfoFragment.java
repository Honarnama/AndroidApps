package net.honarnama.sell.fragments;

import com.makeramen.roundedimageview.RoundedImageView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.honarnama.HonarNamaBaseApp;
import net.honarnama.sell.R;
import net.honarnama.utils.file.ImageSelector;
import net.honarnama.utils.file.SimpleImageCropper;

import java.io.File;

public class StoreInfoFragment extends Fragment implements View.OnClickListener {

    private ImageSelector mImageSelector;
    private SimpleImageCropper mSimpleImageCropper;

    private EditText mStoreNameEditText;
    private EditText mStorePlicyEditText;
    private Button mRegisterStoreButton;
    private RoundedImageView mStoreLogoImageView;
    public File mCroppedFile;

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

                }
                break;
            case R.id.store_logo_image_view:
                mImageSelector = new ImageSelector(getActivity(), getActivity());
                mImageSelector.selectPhoto();
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
                    Uri imageUri = Uri.parse(mImageSelector.getImagePath());
//                    Bitmap photo = (Bitmap) intent.getExtras().get("data");
                    if (mSimpleImageCropper.checkIfDeviceSupportsImageCrop()) {
                        mSimpleImageCropper.crop(imageUri, mCroppedFile, HonarNamaBaseApp.INTENT_CROP_IMAGE_CODE, 200, 200, 10, 10);
                    } else {
                        mStoreLogoImageView.setImageURI(imageUri);
                    }
                }
                break;
            case HonarNamaBaseApp.INTENT_SELECT_IMAGE_CODE:
                if (resultCode == getActivity().RESULT_OK) {
                    Uri imageUri = intent.getData();
                    if (mSimpleImageCropper.checkIfDeviceSupportsImageCrop()) {
                        mSimpleImageCropper.crop(imageUri, mCroppedFile, HonarNamaBaseApp.INTENT_CROP_IMAGE_CODE, 200, 200, 10, 10);
                    } else {
                        mStoreLogoImageView.setImageURI(imageUri);
                    }
                }
                break;
            case HonarNamaBaseApp.INTENT_CROP_IMAGE_CODE:
//                // get the returned data
//                Bundle extras = intent.getExtras();
//                // get the cropped bitmap
//                if (extras != null) {
//                    Bitmap thePic = extras.getParcelable("data");
//                    mStoreLogoImageView.setImageBitmap(thePic);
//                }
                mStoreLogoImageView.setImageURI(Uri.fromFile(mCroppedFile));
                break;
        }
    }
}
