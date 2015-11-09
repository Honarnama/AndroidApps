package net.honarnama.sell.fragments;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import net.honarnama.HonarNamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.sell.R;
import com.parse.ImageSelector;
import net.honarnama.utils.HonarNamaUser;
import net.honarnama.utils.NetworkManager;
import net.honarnama.utils.ParseIO;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
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
    private static String OBJECT_NAME = "store_info";
    private static String OWNER_FIELD = "owner";
    private static String NAME_FIELD = "name";
    private static String POLICY_FIELD = "policy";
    private static String LOGO_FIELD = "logo";


    public static StoreInfoFragment mStoreInfoFragment;

    public synchronized static StoreInfoFragment getInstance() {
        if (mStoreInfoFragment == null) {
            mStoreInfoFragment = new StoreInfoFragment();
        }
        return mStoreInfoFragment;
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

        setStoredStoreInfo();

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
            mStoreNameEditText.setError(getActivity().getString(R.string.error_store_name_cant_be_empty));
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

    public void uploadStoreLogo() {

        if (!NetworkManager.getInstance().isNetworkEnabled(getActivity(), true)) {
            return;
        }

        final ProgressDialog sendingDataProgressDialog = new ProgressDialog(getActivity());
        sendingDataProgressDialog.setCancelable(false);
        sendingDataProgressDialog.setMessage(getString(R.string.sending_data));
        sendingDataProgressDialog.show();

        if (mStoreLogoImageView.getFinalImageUri() == null) {
            registerStore(null, sendingDataProgressDialog);
            return;
        }
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
        ParseQuery<ParseObject> query = ParseQuery.getQuery(OBJECT_NAME);
        query.whereEqualTo(OWNER_FIELD, HonarNamaUser.getCurrentUser());
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject storeInfo, ParseException e) {
                if (e == null) {
                    updateStoreInfo(storeInfo, parseFile, progressDialog);
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

    private void setStoredStoreInfo() {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();

        ParseQuery<ParseObject> query = ParseQuery.getQuery(OBJECT_NAME);
        query.whereEqualTo(OWNER_FIELD, HonarNamaUser.getCurrentUser());
        query.fromLocalDatastore();
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject storeInfo, ParseException e) {
                if (e == null) {
                    mStoreNameEditText.setText(storeInfo.getString(NAME_FIELD));
                    mStorePlicyEditText.setText(storeInfo.getString(POLICY_FIELD));
                    File localStoreLogoFile = new File(HonarNamaBaseApp.APP_IMAGES_FOLDER, HonarNamaBaseApp.STORE_LOGO_FILE_NAME);
                    if (localStoreLogoFile.exists()) {
                        mStoreLogoImageView.setFinalImageUri(Uri.parse(localStoreLogoFile.getAbsolutePath()));
                    }
                    //TODO: Upload the logo file if it doesn't exist on sd card
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

    private void updateStoreInfo(final ParseObject storeObject, ParseFile parseFileParam, ProgressDialog progressDialogParam) {
        final ParseFile parseFile = parseFileParam;
        final ProgressDialog progressDialog = progressDialogParam;

        if (!NetworkManager.getInstance().isNetworkEnabled(getActivity(), true)) {
            progressDialog.dismiss();
            return;
        }

        storeObject.put(NAME_FIELD, mStoreNameEditText.getText().toString().trim());
        storeObject.put(POLICY_FIELD, mStorePlicyEditText.getText().toString().trim());
        if (parseFile != null) {
            storeObject.put(LOGO_FIELD, parseFile);
        }
        storeObject.pinInBackground();
        storeObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.successfully_changed_store_info), Toast.LENGTH_LONG).show();
                } else {
                    // TODO: handle "Invalid: name"
                    Log.e(HonarNamaBaseApp.PRODUCTION_TAG, "storeObject= " + storeObject, e);
                    Toast.makeText(getActivity(), getActivity().getString(R.string.saving_store_info_failed), Toast.LENGTH_LONG).show();
                }
                progressDialog.dismiss();
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

        ParseObject storeInfo = new ParseObject(OBJECT_NAME);
        storeInfo.put(OWNER_FIELD, currentUser);
        storeInfo.put(NAME_FIELD, mStoreNameEditText.getText().toString().trim());
        if (parseFile != null) {
            storeInfo.put(LOGO_FIELD, parseFile);
        }
        storeInfo.put(POLICY_FIELD, mStorePlicyEditText.getText().toString().trim());

        if (!NetworkManager.getInstance().isNetworkEnabled(getActivity(), true)) {
            progressDialog.dismiss();
            return;
        }

        storeInfo.pinInBackground();
        storeInfo.saveInBackground();
        progressDialog.dismiss();
        Toast.makeText(getActivity(), getActivity().getString(R.string.successfully_saved_store_info), Toast.LENGTH_LONG).show();

    }

}
