package net.honarnama.sell.fragments;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ImageSelector;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;
import net.honarnama.core.activity.ChooseCategoryActivity;
import net.honarnama.sell.model.Item;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.core.utils.ParseIO;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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


public class EditItemFragment extends HonarnamaBaseFragment implements View.OnClickListener {

    public static EditItemFragment mEditItemFragment;

    private static final String SAVE_INSTANCE_STATE_KEY_DIRTY = "dirty";
    private static final String SAVE_INSTANCE_STATE_KEY_ITEM_ID = "itemId";
    private static final String SAVE_INSTANCE_STATE_KEY_TITLE = "title";
    private static final String SAVE_INSTANCE_STATE_KEY_DESCRIPTION = "description";

    private EditText mProductTitle;
    private EditText mProductDescription;
    private TextView mItemImageHint;
    private ProgressDialog mLoadingDialog;
    private Button mChooseCategoryButton;

    private ImageSelector[] itemImages;

    private Item mItem;
    private String mItemId;

    private boolean mDirty = false;
    private boolean mCreateNew = false;

    private String mSelectedCategoryObjectId;
    private String mSelectedCategoryName;

    public synchronized static EditItemFragment getInstance() {
        if (mEditItemFragment == null) {
            mEditItemFragment = new EditItemFragment();
        }
        return mEditItemFragment;
    }

    public void reset(boolean createNew) {
        mItem = null;
        mItemId = null;

        mDirty = false;
        mCreateNew = createNew;
    }

    public void setItemId(String itemId) {
        reset(false);
        mItemId = itemId;
    }

    public boolean isDirty() {
        return mDirty;
    }

    @Override
    public String getTitle(Context context) {
        if (mItemId != null) {
            return context.getString(R.string.nav_title_edit_item);
        } else {
            return context.getString(R.string.nav_title_new_item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_item, container, false);

        mProductTitle = (EditText) rootView.findViewById(R.id.editProductTitle);
        mProductDescription = (EditText) rootView.findViewById(R.id.editProductDescription);
        mItemImageHint = (TextView) rootView.findViewById(R.id.itemImageHint);
        mChooseCategoryButton = (Button) rootView.findViewById(R.id.choose_category_button);

        mChooseCategoryButton.setOnClickListener(this);
        TextWatcher textWatcherToMarkDirty = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mDirty = true;
            }
        };

        ImageSelector.OnImageSelectedListener onImageSelectedListener =
                new ImageSelector.OnImageSelectedListener() {
                    @Override
                    public boolean onImageSelected(Uri selectedImage, boolean cropped) {
                        mItemImageHint.setError(null);
                        mDirty = true;
                        return true;
                    }

                    @Override
                    public void onImageRemoved() {
                        mDirty = true;
                    }

                    @Override
                    public void onImageSelectionFailed() {
                    }
                };

        itemImages = new ImageSelector[]{
                (ImageSelector) rootView.findViewById(R.id.itemImage1),
                (ImageSelector) rootView.findViewById(R.id.itemImage2),
                (ImageSelector) rootView.findViewById(R.id.itemImage3),
                (ImageSelector) rootView.findViewById(R.id.itemImage4)
        };
        for (ImageSelector imageSelector : itemImages) {
            imageSelector.setActivity(this.getActivity());
            imageSelector.setOnImageSelectedListener(onImageSelectedListener);
        }

        // Mind fuck starts from here
        // The fragment is created
        // * What if the wants to create a new item?
        //    mCreateNew = true
        // * What if we were in the middle of editing an item, and the user clicked on create new?
        //    mCreateNew = true
        // * What if we were in the middle of creating new item, and phone called, and fragment was killed?
        //    mCreateNew = false, savedDirty = true, savedItemId = null
        // * What if we were in the middle of editing an item, and phone called, and fragment was killed?
        //    mCreateNew = false, savedDirty = true, savedItemId = THE_ID
        // * What if the wants to edit an item?
        //    mCreateNew = false, savedDirty = false, mItemId = THE_ID

        logD(null, "onCreateView :: mCreateNew= " + mCreateNew);

        if (mCreateNew) {
            mProductTitle.setText("");
            mProductDescription.setText("");
            for (ImageSelector imageSelector : itemImages) {
                imageSelector.removeSelectedImage();
            }
        } else {
            boolean savedDirty = false;
            String savedItemId = null;
            if (savedInstanceState != null) {
                savedDirty = savedInstanceState.getBoolean(SAVE_INSTANCE_STATE_KEY_DIRTY);
                savedItemId = savedInstanceState.getString(SAVE_INSTANCE_STATE_KEY_ITEM_ID);
            }

            logD(null, "onCreateView :: savedDirty= " + savedDirty + ", savedItemId= " + savedItemId);

            if (savedDirty) {
                mDirty = true;
                for (ImageSelector imageSelector : itemImages) {
                    imageSelector.restore(savedInstanceState);
                }
                mItemId = savedItemId;
                mProductTitle.setText(savedInstanceState.getString("title"));
                mProductDescription.setText(savedInstanceState.getString("description"));
            } else {
                if (mItemId != null) {
                    showLoadingDialog();
                    ParseQuery<Item> query = ParseQuery.getQuery(Item.class);
                    query.getInBackground(mItemId, new GetCallback<Item>() {
                        @Override
                        public void done(Item item, ParseException e) {
                            if (e != null) {
                                logE("Exception while loading item", "mItemId= " + mItemId, e);
                                Toast.makeText(getActivity(), getActivity().getString(R.string.error_loading_item), Toast.LENGTH_LONG).show();
                            } else {
                                // TODO: check if still we are need this
                                mItem = item;
                                mProductTitle.setText(mItem.getTitle());
                                mProductDescription.setText(mItem.getDescription());
                                ParseFile[] images = mItem.getImages();
                                for (int i = 0; i < Item.NUMBER_OF_IMAGES; i++) {
                                    if (images[i] != null) {
                                        itemImages[i].loadInBackground(images[i], new GetDataCallback() {
                                            @Override
                                            public void done(byte[] data, ParseException e) {
                                                if (e == null) {
                                                    logD(null, "Fetched! Data length: " + data.length);
                                                } else {
                                                    logE("Exception while loading image", "", e);
                                                }
                                            }
                                        });
                                    }
                                }
                                mDirty = false;
                            }
                            hideLoadingDialog();
                        }
                    });
                } else {
                    logE("Unexpected state!");
                }
            }
        }

        mProductTitle.addTextChangedListener(textWatcherToMarkDirty);
        mProductDescription.addTextChangedListener(textWatcherToMarkDirty);

        rootView.findViewById(R.id.saveItemButton).setOnClickListener(this);

        return rootView;
    }

    private void showLoadingDialog() {
        if (mLoadingDialog == null || !mLoadingDialog.isShowing()) {
            mLoadingDialog = ProgressDialog.show(getActivity(), "", getString(R.string.login_dialog_text), false);
            mLoadingDialog.setCancelable(false);
        }
    }

    private void hideLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.hide();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.saveItemButton:
                if (isFormInputsValid()) {
                    saveItemImages();
                }
                break;
            case R.id.choose_category_button:
                Intent intent = new Intent(getActivity(), ChooseCategoryActivity.class);
                startActivityForResult(intent, HonarnamaSellApp.INTENT_CHOOSE_CATEGORY_CODE);
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
            if ((imageSelector.getFinalImageUri() != null) || (imageSelector.getParseFile() != null)) {
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

        if (mItemId != null) {
            // TODO
            Toast.makeText(getActivity(), "Not implemented yet!", Toast.LENGTH_LONG).show();
            return;
        }

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
            logE("Failed on preparing item images", "", e);
            sendingDataProgressDialog.dismiss();
            Toast.makeText(getActivity(), R.string.error_sending_images, Toast.LENGTH_LONG).show();
        }

        final boolean[] errorOccuredUploadingFiles = {false};
        for (int count = 0; count < parseFileImages.size(); count++) {
            final int fileNumber = count + 1;
            final ParseFile parseFile = parseFileImages.get(count);
            parseFile.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        if (fileNumber == parseFileImages.size()) {
                            saveItemInfo(parseFileImages, sendingDataProgressDialog);
                        }
                    } else {

                        if (errorOccuredUploadingFiles[0] == false) {
                            Toast.makeText(getActivity(), " خطا در ارسال تصویر. لطفاً دوباره تلاش کنید. ", Toast.LENGTH_LONG).show();
                            errorOccuredUploadingFiles[0] = true;
                        }
                        logE("Uploading image failed.", "Code: " + e.getCode(), e);
                        sendingDataProgressDialog.dismiss();
                        return;
                    }
                }
            }, new ProgressCallback() {
                public void done(Integer percentDone) {
                    if (percentDone % 10 == 0) {
                        logD(null, "Uploading Store Logo Image - percentDone= " + percentDone);
                    }
                }
            });
        }

        if (errorOccuredUploadingFiles[0]) {
            return;
        }

    }

    private void saveItemInfo(ArrayList<ParseFile> parseFileImages, final ProgressDialog sendingDataProgressDialog) {

        String title = mProductTitle.getText().toString().trim();
        String description = mProductDescription.getText().toString().trim();

        final Item itemInfo = new Item(ParseUser.getCurrentUser(), title, description);
        int count = 0;
        for (ParseFile parseFile : parseFileImages) {
            count++;
            itemInfo.put("image_" + count, parseFile);
        }
        itemInfo.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getActivity(), R.string.edit_item_save, Toast.LENGTH_LONG).show();
                    mDirty = false;
                    mItem = itemInfo;
                    mItemId = itemInfo.getObjectId();
                } else {
                    logE("Exception while saveItemInfo", "", e);
                    Toast.makeText(getActivity(), R.string.error_saving_item, Toast.LENGTH_LONG).show();
                }
                sendingDataProgressDialog.dismiss();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (ImageSelector imageSelector : itemImages) {
            if (imageSelector.onActivityResult(requestCode, resultCode, data)) {
                return;
            }
        }
        switch (requestCode) {
            case HonarnamaSellApp.INTENT_CHOOSE_CATEGORY_CODE:

                if (resultCode == getActivity().RESULT_OK) {
                    mSelectedCategoryName = data.getStringExtra("selectedCategoryName");
                    mSelectedCategoryObjectId = data.getStringExtra("selectedCategoryObjectId");
                    Toast.makeText(getActivity(), mSelectedCategoryObjectId, Toast.LENGTH_LONG).show();
                    Toast.makeText(getActivity(), mSelectedCategoryName, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mDirty) {
            for (ImageSelector imageSelector : itemImages) {
                imageSelector.onSaveInstanceState(outState);
            }
            outState.putBoolean(SAVE_INSTANCE_STATE_KEY_DIRTY, true);
            outState.putString(SAVE_INSTANCE_STATE_KEY_ITEM_ID, mItemId);
            outState.putString(SAVE_INSTANCE_STATE_KEY_TITLE, mProductTitle.getText().toString().trim());
            outState.putString(SAVE_INSTANCE_STATE_KEY_DESCRIPTION, mProductDescription.getText().toString().trim());
        }
    }
}
