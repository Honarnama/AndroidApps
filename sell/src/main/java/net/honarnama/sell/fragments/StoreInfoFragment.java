package net.honarnama.sell.fragments;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import net.honarnama.HonarNamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.sell.R;
import net.honarnama.sell.utils.ImageSelector;
import net.honarnama.utils.HonarNamaUser;
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
    private static String objectName = "store_info";
    private static String ownerField = "owner";
    private static String nameField = "name";
    private static String policyField = "policy";
    private static String logoField = "logo";

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

        retrieveUserStore();

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

        final File storeLogoImageFile = new File(mStoreLogoImageView.getFinalImageUri().getPath());
        try {
            final ParseFile parseFile = ParseIO.getParseFileFromFile(HonarNamaBaseApp.STORE_LOGO_FILE_NAME,
                    storeLogoImageFile);
            parseFile.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        registerStore(parseFile, sendingDataProgressDialog);
                        try {
                            ParseIO.copyFile(storeLogoImageFile, new File(HonarNamaBaseApp.APP_IMAGES_FOLDER, HonarNamaBaseApp.STORE_LOGO_FILE_NAME));
                        } catch (IOException e1) {
                            if (BuildConfig.DEBUG) {
                                Log.e(HonarNamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                        "Error copying store logo to sd card " + e1, e1);
                            } else {
                                Log.e(HonarNamaBaseApp.PRODUCTION_TAG, "Error copying store logo to sd card"
                                        + e1.getMessage());
                            }
                        }
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
            });
        } catch (IOException ioe) {
            Toast.makeText(StoreInfoFragment.this.getActivity(), " خطا در ارسال تصویر. لطفاً دوباره تلاش کنید. ",
                    Toast.LENGTH_LONG).show();

            if (BuildConfig.DEBUG) {
                Log.e(HonarNamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                        "Failed on preparing store logo image. " + ioe, ioe);
            } else {
                Log.e(HonarNamaBaseApp.PRODUCTION_TAG, "Failed on preparing store logo image. ioe="
                        + ioe.getMessage());
            }

            sendingDataProgressDialog.dismiss();
        }
    }

    private void registerStore(ParseFile parseFileParam, ProgressDialog progressDialogParam) {
        //check if user already have a registered store
        final ParseFile parseFile = parseFileParam;
        final ProgressDialog progressDialog = progressDialogParam;
        ParseQuery<ParseObject> query = ParseQuery.getQuery(objectName);
        query.whereEqualTo(ownerField, HonarNamaUser.getCurrentUser());
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject storeInfo, ParseException e) {
                if (e == null) {
                    updateStoreInfo(storeInfo.getObjectId(), parseFile, progressDialog);
                } else {
                    addNewStore(parseFile, progressDialog);
                    if (BuildConfig.DEBUG) {
                        Log.e(HonarNamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                "Error geeting current store id. Error :" + e.getCode() +
                                        "//" + e.getMessage() + " // " + e, e);
                    }
                }
            }
        });


    }

    private void retrieveUserStore() {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();

        ParseQuery<ParseObject> query = ParseQuery.getQuery(objectName);
        query.whereEqualTo(ownerField, HonarNamaUser.getCurrentUser());
        query.fromLocalDatastore();
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject storeInfo, ParseException e) {
                if (e == null) {
                    mStoreNameEditText.setText(storeInfo.getString(nameField));
                    mStorePlicyEditText.setText(storeInfo.getString(policyField));
                    File localStoreLogoFile = new File(HonarNamaBaseApp.APP_IMAGES_FOLDER, HonarNamaBaseApp.STORE_LOGO_FILE_NAME);
                    if (localStoreLogoFile.exists()) {
                        mStoreLogoImageView.setImageURI(Uri.parse(localStoreLogoFile.getAbsolutePath()));
                    }
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.e(HonarNamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                "Error getting store info.  Error Code: " + e.getCode() +
                                        "//" + e.getMessage() + " // " + e, e);
                    } else {
                        Log.e(HonarNamaBaseApp.PRODUCTION_TAG, "Error getting store info. "
                                + e.getMessage());
                    }
                }
                progressDialog.dismiss();
            }
        });
    }

    private void updateStoreInfo(String currentObjectIdParam, ParseFile parseFileParam, ProgressDialog progressDialogParam) {
        final ParseUser currentUser = ParseUser.getCurrentUser();
        final ParseFile parseFile = parseFileParam;
        final ParseObject[] storeInfo = new ParseObject[1];
        final ProgressDialog progressDialog = progressDialogParam;
        final String currentObjectId = currentObjectIdParam;
        storeInfo[0] = null;

        if (!NetworkManager.getInstance().isNetworkEnabled(getActivity(), true)) {
            progressDialog.dismiss();
            return;
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery(objectName);
        query.whereEqualTo("owner", currentUser);
        query.getInBackground(currentObjectId, new GetCallback<ParseObject>() {
            public void done(ParseObject storeObject, ParseException e) {
                if (e == null) {
                    storeInfo[0] = storeObject;
                    if (storeInfo[0] == null) {
                        if (currentObjectId != null) {
                            if (BuildConfig.DEBUG) {
                                Log.e(HonarNamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                        "Error geeting current store info for updating existing one. Error code: " + e.getCode() +
                                                "//" + e.getMessage() + " // " + e);
                            }
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), getActivity().getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    storeInfo[0].put(nameField, mStoreNameEditText.getText().toString().trim());
                    storeInfo[0].put(logoField, parseFile);
                    storeInfo[0].put(policyField, mStorePlicyEditText.getText().toString().trim());

                    if (!NetworkManager.getInstance().isNetworkEnabled(getActivity(), true)) {
                        progressDialog.dismiss();
                        return;
                    }

                    storeInfo[0].pinInBackground();
                    storeInfo[0].saveInBackground();
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), getActivity().getString(R.string.successfully_saved_store_info), Toast.LENGTH_LONG).show();

                }
            }
        });
    }


    private void addNewStore(ParseFile parseFileParam, ProgressDialog progressDialogParam) {
        final ParseUser currentUser = ParseUser.getCurrentUser();
        final ParseFile parseFile = parseFileParam;
        final ProgressDialog progressDialog = progressDialogParam;

        if (!NetworkManager.getInstance().isNetworkEnabled(getActivity(), true)) {
            progressDialog.dismiss();
            return;
        }

        ParseObject storeInfo = new ParseObject(objectName);
        storeInfo.put(ownerField, currentUser);
        storeInfo.put(nameField, mStoreNameEditText.getText().toString().trim());
        storeInfo.put(logoField, parseFile);
        storeInfo.put(policyField, mStorePlicyEditText.getText().toString().trim());

        if (!NetworkManager.getInstance().isNetworkEnabled(getActivity(), true)) {
            progressDialog.dismiss();
            return;
        }

        storeInfo.pinInBackground();
        storeInfo.saveInBackground();
        progressDialog.dismiss();
        Toast.makeText(getActivity(), getActivity().getString(R.string.successfully_saved_store_info), Toast.LENGTH_LONG).show();

    }

    //TODO remove temp file
}
