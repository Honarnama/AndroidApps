package net.honarnama.sell.fragments;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import net.honarnama.HonarNamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.sell.R;
import com.parse.ImageSelector;
import net.honarnama.utils.NetworkManager;
import net.honarnama.utils.ParseIO;

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
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class EditItemFragment extends Fragment implements View.OnClickListener {

    public static EditItemFragment mEditItemFragment;

    private Button mSaveButton;
    private EditText mProductTitle;
    private EditText mProductDescription;
    private TextView mItemImageHint;

    private ImageSelector[] itemImages;

    private ParseObject mItem;
    private String mItemId;

    public synchronized static EditItemFragment getInstance() {
        if (mEditItemFragment == null) {
            mEditItemFragment = new EditItemFragment();
        }
        return mEditItemFragment;
    }

    public void setItem(ParseObject item) {
        mItem = item;
        if (mItem == null) {
            mItemId = null;
        } else {
            mItemId = mItem.getObjectId();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_item, container, false);

        mProductTitle = (EditText) rootView.findViewById(R.id.editProductTitle);
        mProductDescription = (EditText) rootView.findViewById(R.id.editProductDescription);
        mItemImageHint = (TextView) rootView.findViewById(R.id.itemImageHint);

        ImageSelector.OnImageSelectedListener onImageSelectedListener =
                new ImageSelector.OnImageSelectedListener() {
                    @Override
                    public boolean onImageSelected(Uri selectedImage, boolean cropped) {
                        mItemImageHint.setError(null);
                        return true;
                    }

                    @Override
                    public boolean onImageRemoved() {
                        return false;
                    }

                    @Override
                    public void onImageSelectionFailed() {

                    }
                };

        String savedItemId = null;
        if (savedInstanceState != null) {
            savedItemId = savedInstanceState.getString("mItemId");
        }

        itemImages = new ImageSelector[]{
                (ImageSelector) rootView.findViewById(R.id.itemImage1),
                (ImageSelector) rootView.findViewById(R.id.itemImage2),
                (ImageSelector) rootView.findViewById(R.id.itemImage3),
                (ImageSelector) rootView.findViewById(R.id.itemImage4)
        };
        for (ImageSelector imageSelector : itemImages) {
            imageSelector.setActivity(this.getActivity());
            if (mItemId == savedItemId) {
                imageSelector.restore(savedInstanceState);
            }
            imageSelector.setOnImageSelectedListener(onImageSelectedListener);
        }

        if (mItemId != savedItemId) {
            if (mItem == null) {
                mProductTitle.setText("");
                mProductDescription.setText("");
                for (ImageSelector imageSelector : itemImages) {
                    imageSelector.removeSelectedImage();
                }
            } else {
                mProductTitle.setText(mItem.getString("title"));
                mProductDescription.setText(mItem.getString("description"));
                for (int i=1; i<5; i++) {
                    ParseFile imageFile = mItem.getParseFile("image_" + i);
                    if (imageFile != null) {
                        itemImages[i].loadInBackground(imageFile, new GetDataCallback() {
                            @Override
                            public void done(byte[] data, ParseException e) {
                                if (HonarNamaBaseApp.DEBUG) {
                                    if (e == null) {
                                        Log.i(HonarNamaBaseApp.PRODUCTION_TAG + "/EditItem",
                                                "Fetched! Data length: " + data.length);
                                    } else {
                                        Log.w(HonarNamaBaseApp.PRODUCTION_TAG + "/EditItem",
                                                "Exception while loading image", e);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        } else if (savedInstanceState != null) {
            mProductTitle.setText(savedInstanceState.getString("title"));
            mProductDescription.setText(savedInstanceState.getString("description"));
        }

        mSaveButton = (Button) rootView.findViewById(R.id.saveItemButton);
        mSaveButton.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.saveItemButton:
                if (isFormInputsValid()) {
                    saveItemImages();
                }
                break;
            default:
                break;
        }
    }

    private boolean isFormInputsValid() {
        final String title = mProductTitle.getText().toString();
        final String description = mProductDescription.getText().toString();

        boolean noImage = true;
        for (ImageSelector imageSelector : itemImages) {
            if (imageSelector.getFinalImageUri() != null) {
                noImage = false;
                break;
            }
        }
        if (noImage) {
            mItemImageHint.requestFocus();
            mItemImageHint.setError(getString(R.string.error_edit_item_no_image));
            return false;
        }

        if (title.trim().length() == 0) {
            mProductTitle.requestFocus();
            mProductTitle.setError(getString(R.string.error_edit_item_title_is_empty));
            return false;
        }

        if (description.trim().length() == 0) {
            mProductDescription.requestFocus();
            mProductDescription.setError(getString(R.string.error_edit_item_description_is_empty));
            return false;
        }

        if (!NetworkManager.getInstance().isNetworkEnabled(getActivity(), true)) {
            return false;
        }
        return true;
    }

    private void saveItemImages() {

        final ProgressDialog sendingDataProgressDialog = new ProgressDialog(getActivity());
        sendingDataProgressDialog.setCancelable(false);
        sendingDataProgressDialog.setMessage(getString(R.string.sending_data));
        sendingDataProgressDialog.show();

        final ArrayList<ParseFile> parseFileImages = new ArrayList<ParseFile>();

        try {
            for (ImageSelector imageSelector : itemImages) {
                if (imageSelector.getFinalImageUri() != null) {
                    ParseFile parseFile = ParseIO.getParseFileFromFile(
                            "image_" + imageSelector.getImageSelectorIndex() + ".jpeg",
                            new File(imageSelector.getFinalImageUri().getPath())
                    );
                    parseFileImages.add(parseFile);
                }
            }
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                Log.e(HonarNamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                        "Failed on preparing item images.", e);
            } else {
                Log.e(HonarNamaBaseApp.PRODUCTION_TAG, "Failed on preparing item images. e="
                        + e.getMessage());
            }
            sendingDataProgressDialog.dismiss();
            Toast.makeText(getActivity(), "خطا در ارسال تصاویر. لطفا دوباره تلاش کنید.", Toast.LENGTH_LONG).show();
        }

        final boolean[] errorOccuredUploadingFiles = {false};
        for (int count = 0; count < parseFileImages.size(); count++) {
//        for (final ParseFile parseFile : parseFileImages) {
            final int fileNumber = count + 1;
            final ParseFile parseFile = parseFileImages.get(count);
            parseFile.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        if (fileNumber == parseFileImages.size()) {
                            sendingDataProgressDialog.dismiss();
                            saveItemInfo(parseFileImages);
                        }
                    } else {

                        if (errorOccuredUploadingFiles[0] == false) {
                            Toast.makeText(getActivity(), " خطا در ارسال تصویر. لطفاً دوباره تلاش کنید. ", Toast.LENGTH_LONG).show();
                            errorOccuredUploadingFiles[0] = true;
                        }
                        if (BuildConfig.DEBUG) {
                            Log.e(HonarNamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "Uploading Store Logo Failed. Code: " + e.getCode() +
                                    "//" + e.getMessage() + " // " + e);
                        } else {
                            Log.e(HonarNamaBaseApp.PRODUCTION_TAG, "Uploading Store Logo Failed. Code: " + e.getCode() +
                                    "//" + e.getMessage() + " // " + e);
                        }
                        sendingDataProgressDialog.dismiss();
                        return;
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

        if (errorOccuredUploadingFiles[0]) {
            return;
        }

    }

    public void saveItemInfo(ArrayList<ParseFile> parseFileImages) {

        String title = mProductTitle.getText().toString().trim();
        String description = mProductDescription.getText().toString().trim();

        ParseObject itemInfo = new ParseObject("item");
        itemInfo.put("title", title);
        itemInfo.put("description", description);
        itemInfo.put("owner", ParseUser.getCurrentUser());
        int count = 0;
        for (ParseFile parseFile : parseFileImages) {
            count++;
            itemInfo.put("image_" + count, parseFile);
        }
        itemInfo.saveInBackground();


        Toast.makeText(getActivity(), " محصول شما با موفقیت ذخیره شد. ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (ImageSelector imageSelector : itemImages) {
            if (imageSelector.onActivityResult(requestCode, resultCode, data)) {
                return;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        for (ImageSelector imageSelector : itemImages) {
            imageSelector.onSaveInstanceState(outState);
        }
        outState.putString("mItemId", mItemId);
        outState.putString("title", mProductTitle.getText().toString().trim());
        outState.putString("description", mProductDescription.getText().toString().trim());
    }
}
