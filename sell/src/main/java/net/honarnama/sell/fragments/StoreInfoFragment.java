package net.honarnama.sell.fragments;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import net.honarnama.HonarNamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.sell.R;
import net.honarnama.sell.widget.ImageSelector;
import net.honarnama.utils.NetworkManager;
import net.honarnama.utils.ParseIO;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class StoreInfoFragment extends Fragment implements View.OnClickListener {

    private EditText mStoreNameEditText;
    private EditText mStorePlicyEditText;
    private Button mRegisterStoreButton;
    private ImageSelector mStoreLogoImageView;

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
        mStoreLogoImageView = (ImageSelector) rootView.findViewById(R.id.store_logo_image_view);

        mStoreLogoImageView.setOnImageSelectedListener(new ImageSelector.OnImageSelectedListener() {
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
        mRegisterStoreButton.setOnClickListener(this);
        mStoreLogoImageView.setActivity(this.getActivity());
        mStoreLogoImageView.restore(savedInstanceState);

        // TODO: load current store info

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
        mStoreLogoImageView.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mStoreLogoImageView != null) {
            mStoreLogoImageView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onDestroy() {
        if (mStoreLogoImageView != null) {
            mStoreLogoImageView.onDestroy();
        }
        super.onDestroy();
    }

    public void uploadStoreLogo() {

        if (!NetworkManager.getInstance().isNetworkEnabled(getActivity(), true)) {
            return;
        }

        final ProgressDialog sendingDataProgressDialog = new ProgressDialog(getActivity());
        sendingDataProgressDialog.setCancelable(false);
        sendingDataProgressDialog.setMessage(getString(R.string.sending_data));
        sendingDataProgressDialog.show();

        File storeLogoImageFile = new File(mStoreLogoImageView.getFinalImageUri().getPath());
        try {
            final ParseFile parseFile = ParseIO.getParseFileFromFile("store_logo.jpeg",
                    storeLogoImageFile);
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
        } catch (IOException ioe) {
            Toast.makeText(StoreInfoFragment.this.getActivity(), " خطا در ارسال تصویر. لطفاً دوباره تلاش کنید. ",
                    Toast.LENGTH_LONG).show();

            if (BuildConfig.DEBUG) {
                Log.e(HonarNamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                        "Failed on preparing store logo image.", ioe);
            } else {
                Log.e(HonarNamaBaseApp.PRODUCTION_TAG, "Failed on preparing store logo image. ioe="
                        + ioe.getMessage());
            }

            sendingDataProgressDialog.dismiss();
        }
    }

    public void registerStore(ParseFile parseFile, ProgressDialog progressDialog) {
        ParseUser currentUser = ParseUser.getCurrentUser();

        ParseObject storeInfo = new ParseObject("StoreInfo");
        storeInfo.put("name", mStoreNameEditText.getText().toString().trim());
        storeInfo.put("logo", parseFile);
        storeInfo.put("policy", mStorePlicyEditText.getText().toString().trim());
        storeInfo.put("owner", currentUser);

        storeInfo.saveInBackground();

        progressDialog.dismiss();

        // TODO: user feedback
    }

    //TODO remove temp file
}
