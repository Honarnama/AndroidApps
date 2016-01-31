package net.honarnama.sell.fragments;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ImageSelector;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.activity.ChooseCategoryActivity;
import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.core.model.Category;
import net.honarnama.core.model.Item;
import net.honarnama.core.utils.GenericGravityTextWatcher;
import net.honarnama.core.utils.HonarnamaUser;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;
import net.honarnama.sell.activity.ControlPanelActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import bolts.Continuation;
import bolts.Task;


public class EditItemFragment extends HonarnamaBaseFragment implements View.OnClickListener {

    public static EditItemFragment mEditItemFragment;

    private static final String SAVE_INSTANCE_STATE_KEY_DIRTY = "dirty";
    private static final String SAVE_INSTANCE_STATE_KEY_ITEM_ID = "itemId";
    private static final String SAVE_INSTANCE_STATE_KEY_TITLE = "title";
    private static final String SAVE_INSTANCE_STATE_KEY_DESCRIPTION = "description";
    private static final String SAVE_INSTANCE_STATE_KEY_PRICE = "price";
    private static final String SAVE_INSTANCE_STATE_KEY_CATEGORY_ID = "categoryId";
    private static final String SAVE_INSTANCE_STATE_KEY_CATEGORY_NAME = "categoryName";


    private EditText mTitleEditText;
    private EditText mDescriptionEditText;
    private EditText mPriceEditText;
    private TextView mImagesTitleTextView;
    private ScrollView mScrollView;

    private ProgressDialog mLoadingDialog;
    private Button mChooseCategoryButton;
    private TextView mCategoryTextView;

    private ImageSelector[] mItemImages;

    private Item mItem;
    private String mItemId;

    private boolean mDirty = false;
    private boolean mCreateNew = false;

    private String mCategoryId;
    private String mCategoryName;

    private boolean mFragmentHasView = false;

    public synchronized static EditItemFragment getInstance() {
        if (mEditItemFragment == null) {
            mEditItemFragment = new EditItemFragment();
        }
        return mEditItemFragment;
    }

    public void reset(Context context, boolean createNew) {

        if (mFragmentHasView) {
            mTitleEditText.setText("");
            mDescriptionEditText.setText("");
            mPriceEditText.setText("");
            mChooseCategoryButton.setText(context.getString(R.string.select));
            for (ImageSelector imageSelector : mItemImages) {
                imageSelector.removeSelectedImage();
            }
            mTitleEditText.setError(null);
            mDescriptionEditText.setError(null);
            mPriceEditText.setError(null);
            mCategoryTextView.setError(null);
            mImagesTitleTextView.setError(null);
        }
        mItem = null;
        mItemId = null;
        mCategoryId = null;
        mCategoryName = null;
        setDirty(false);
        mCreateNew = createNew;
    }

    public void setItemId(Context context, String itemId) {
        reset(context, false);
        mItemId = itemId;
    }

    private void setDirty(boolean dirty) {
        mDirty = dirty;
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
    public void onResume() {
        super.onResume();
        TextWatcher textWatcherToMarkDirty = new TextWatcher() {
            String mValue;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mValue = charSequence.toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mValue != editable + "") {
                    mDirty = true;
                }
            }
        };
        mTitleEditText.addTextChangedListener(textWatcherToMarkDirty);
        mDescriptionEditText.addTextChangedListener(textWatcherToMarkDirty);
        mPriceEditText.addTextChangedListener(textWatcherToMarkDirty);
        mPriceEditText.addTextChangedListener(new GenericGravityTextWatcher(mPriceEditText));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences sharedPref = HonarnamaBaseApp.getInstance().getSharedPreferences(HonarnamaUser.getCurrentUser().getUsername(), Context.MODE_PRIVATE);
        if (!sharedPref.getBoolean(HonarnamaSellApp.PREF_LOCAL_DATA_STORE_FOR_ITEM_SYNCED, false)) {

            if (!NetworkManager.getInstance().isNetworkEnabled(true) || !sharedPref.getBoolean(HonarnamaSellApp.PREF_LOCAL_DATA_STORE_SYNCED, false)) {

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_SYNCED, false);
                editor.commit();

                Intent intent = new Intent(getActivity(), ControlPanelActivity.class);
                getActivity().finish();
                startActivity(intent);
            }

        }

        mFragmentHasView = true;
        final View rootView = inflater.inflate(R.layout.fragment_edit_item, container, false);

        mTitleEditText = (EditText) rootView.findViewById(R.id.editProductTitle);
        mDescriptionEditText = (EditText) rootView.findViewById(R.id.editProductDescription);
        mPriceEditText = (EditText) rootView.findViewById(R.id.editItemPrice);
        mImagesTitleTextView = (TextView) rootView.findViewById(R.id.edit_item_images_title_text_view);
        mScrollView = (ScrollView) rootView.findViewById(R.id.edit_item_scroll_view);

        mCategoryTextView = (TextView) rootView.findViewById(R.id.edit_item_category_text_view);
        mChooseCategoryButton = (Button) rootView.findViewById(R.id.edit_item_category_semi_button);

        mChooseCategoryButton.setOnClickListener(this);

        ImageSelector.OnImageSelectedListener onImageSelectedListener =
                new ImageSelector.OnImageSelectedListener() {
                    @Override
                    public boolean onImageSelected(Uri selectedImage, boolean cropped) {
                        mImagesTitleTextView.setError(null);
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

        mItemImages = new ImageSelector[]{
                (ImageSelector) rootView.findViewById(R.id.itemImage1),
                (ImageSelector) rootView.findViewById(R.id.itemImage2),
                (ImageSelector) rootView.findViewById(R.id.itemImage3),
                (ImageSelector) rootView.findViewById(R.id.itemImage4)
        };
        for (ImageSelector imageSelector : mItemImages) {
            imageSelector.setActivity(this.getActivity());
            imageSelector.setOnImageSelectedListener(onImageSelectedListener);
        }

        // Mind fuck starts from here
        // The fragment is created
        // * What if the wants to create a new item_row?
        //    mCreateNew = true
        // * What if we were in the middle of editing an item_row, and the user clicked on create new?
        //    mCreateNew = true
        // * What if we were in the middle of creating new item_row, and phone called, and fragment was killed?
        //    mCreateNew = false, savedDirty = true, savedItemId = null
        // * What if we were in the middle of editing an item_row, and phone called, and fragment was killed?
        //    mCreateNew = false, savedDirty = true, savedItemId = THE_ID
        // * What if the wants to edit an item_row?
        //    mCreateNew = false, savedDirty = false, mItemId = THE_ID

        logD(null, "onCreateView :: mCreateNew= " + mCreateNew);
        if (mCreateNew) {
            reset(getActivity(), true);
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
                for (ImageSelector imageSelector : mItemImages) {
                    imageSelector.restore(savedInstanceState);
                }
                mItemId = savedItemId;
                mTitleEditText.setText(savedInstanceState.getString("title"));
                mDescriptionEditText.setText(savedInstanceState.getString("description"));
                mChooseCategoryButton.setText(savedInstanceState.getString("categoryName"));
                mCategoryId = savedInstanceState.getString("categoryId");
                mPriceEditText.setText(savedInstanceState.getString("price"));
            } else {
                if (mItemId != null) {
                    showLoadingDialog();
                    ParseQuery<Item> query = ParseQuery.getQuery(Item.class);

                    if (sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_ITEM_SYNCED, false)) {
                        if (BuildConfig.DEBUG) {
                            Log.d(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getActivity().getClass().getName(), "getting items from Local data store");
                        }
                        query.fromLocalDatastore();
                    } else {
                        if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                            return null;
                        }
                    }

                    query.getInBackground(mItemId, new GetCallback<Item>() {
                        @Override
                        public void done(Item item, ParseException e) {
                            hideLoadingDialog();
                            if (e != null) {
                                logE("Exception while loading item_row", "mItemId= " + mItemId, e);
                                if (isVisible()) {
                                    Toast.makeText(getActivity(), getString(R.string.error_loading_item) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                // TODO: check if still we are need this
                                mItem = item;
                                mTitleEditText.setText(mItem.getTitle());
                                mDescriptionEditText.setText(mItem.getDescription());
                                mPriceEditText.setText(mItem.getPrice() + "");
                                mCategoryId = mItem.getCategoryId();
                                mChooseCategoryButton.setText(getString(R.string.getting_information));
                                new Category().findCategoryName(mCategoryId, getActivity()).continueWith(new Continuation<String, Object>() {
                                    @Override
                                    public Object then(Task<String> task) throws Exception {
                                        if (task.isFaulted()) {
                                            if (isVisible()) {
                                                Toast.makeText(getActivity(), getString(R.string.error_finding_category_name) + getString(R.string.please_check_internet_connection), Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            mChooseCategoryButton.setText(task.getResult());
                                        }
                                        return null;
                                    }
                                });

                                ParseFile[] images = mItem.getImages();

                                int counter = -1;
                                for (int i = 0; i < Item.NUMBER_OF_IMAGES; i++) {
                                    if (images[i] != null) {
                                        counter++;
                                        switch (counter) {
                                            case 0:
                                                rootView.findViewById(R.id.loadingPanel_1).setVisibility(View.VISIBLE);
                                                rootView.findViewById(R.id.itemImage1).setVisibility(View.GONE);
                                                break;
                                            case 1:
                                                rootView.findViewById(R.id.loadingPanel_2).setVisibility(View.VISIBLE);
                                                rootView.findViewById(R.id.itemImage2).setVisibility(View.GONE);
                                                break;
                                            case 2:
                                                rootView.findViewById(R.id.loadingPanel_3).setVisibility(View.VISIBLE);
                                                rootView.findViewById(R.id.itemImage3).setVisibility(View.GONE);
                                                break;
                                            case 3:
                                                rootView.findViewById(R.id.loadingPanel_4).setVisibility(View.VISIBLE);
                                                rootView.findViewById(R.id.itemImage4).setVisibility(View.GONE);
                                                break;
                                        }
                                        final int finalCounter = counter;
                                        mItemImages[counter].loadInBackground(images[i], new GetDataCallback() {
                                            @Override
                                            public void done(byte[] data, ParseException e) {
                                                switch (finalCounter) {
                                                    case 0:
                                                        rootView.findViewById(R.id.loadingPanel_1).setVisibility(View.GONE);
                                                        rootView.findViewById(R.id.itemImage1).setVisibility(View.VISIBLE);
                                                        break;
                                                    case 1:
                                                        rootView.findViewById(R.id.loadingPanel_2).setVisibility(View.GONE);
                                                        rootView.findViewById(R.id.itemImage2).setVisibility(View.VISIBLE);
                                                        break;
                                                    case 2:
                                                        rootView.findViewById(R.id.loadingPanel_3).setVisibility(View.GONE);
                                                        rootView.findViewById(R.id.itemImage3).setVisibility(View.VISIBLE);
                                                        break;
                                                    case 3:
                                                        rootView.findViewById(R.id.loadingPanel_4).setVisibility(View.GONE);
                                                        rootView.findViewById(R.id.itemImage4).setVisibility(View.VISIBLE);
                                                        break;
                                                }
                                                if (e == null) {
                                                    if (data != null) {
                                                        logD(null, "Fetched! Data length: " + data.length);
                                                    }
                                                } else {
                                                    if (isVisible()) {
                                                        Toast.makeText(getActivity(), getString(R.string.error_displaying_image) + getString(R.string.please_check_internet_connection), Toast.LENGTH_SHORT).show();
                                                    }
                                                    logE("Exception while loading image", "", e);
                                                }
                                            }
                                        });
                                    }
                                }
                                mDirty = false;
                            }
                        }
                    });
                } else {
//                    logE("Unexpected state!");
                }
            }
        }

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
                    saveItem();
                }
                break;
            case R.id.edit_item_category_semi_button:
                Intent intent = new Intent(getActivity(), ChooseCategoryActivity.class);
                startActivityForResult(intent, HonarnamaSellApp.INTENT_CHOOSE_CATEGORY_CODE);
                break;
            default:
                break;
        }
    }

    private boolean isFormInputsValid() {

        final String title = mTitleEditText.getText().toString();
        final String price = mPriceEditText.getText().toString();
        final String description = mDescriptionEditText.getText().toString();


        if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
            return false;
        }

        boolean noImage = true;
        for (ImageSelector imageSelector : mItemImages) {
            if ((imageSelector.getFinalImageUri() != null) || (imageSelector.getParseFile() != null && !imageSelector.isDeleted())) {
                noImage = false;
                break;
            }
        }
        if (noImage) {
            mImagesTitleTextView.requestFocus();
            mImagesTitleTextView.setError(getString(R.string.error_edit_item_no_image));
            mScrollView.fullScroll(ScrollView.FOCUS_UP);
            return false;
        }

        if (title.trim().length() == 0) {
            mTitleEditText.requestFocus();
            mTitleEditText.setError(getString(R.string.error_edit_item_title_is_empty));
            return false;
        }

        if (price.trim().length() == 0) {
            mPriceEditText.requestFocus();
            mPriceEditText.setError(getString(R.string.error_item_price_not_set));
            return false;
        }

        if (Integer.valueOf(price.trim()) < 100) {
            mPriceEditText.requestFocus();
            mPriceEditText.setError(getString(R.string.error_item_price_is_low));
            return false;
        }


        if (mCategoryId == null) {
            mCategoryTextView.requestFocus();
            mCategoryTextView.setError(getString(R.string.error_category_is_not_selected));
            return false;
        }

        if (description.trim().length() == 0) {
            mDescriptionEditText.requestFocus();
            mDescriptionEditText.setError(getString(R.string.error_edit_item_description_is_empty));
            return false;
        }


        if (!mDirty) {
            if (isVisible()) {
                Toast.makeText(getActivity(), getString(R.string.item_not_changed), Toast.LENGTH_LONG).show();
            }
            return false;
        }

        return true;
    }

    private void saveItem() {
        String title = mTitleEditText.getText().toString().trim();
        String description = mDescriptionEditText.getText().toString().trim();
        Number price = Integer.valueOf(mPriceEditText.getText().toString().trim());

        final ProgressDialog sendingDataProgressDialog = new ProgressDialog(getActivity());
        sendingDataProgressDialog.setCancelable(false);
        sendingDataProgressDialog.setMessage(getString(R.string.sending_data));
        sendingDataProgressDialog.show();

        try {
            Item.saveWithImages(mItem, title, description, mCategoryId, price, mItemImages).continueWith(new Continuation<Item, Void>() {
                @Override
                public Void then(Task<Item> task) throws Exception {
                    logD(null, "saveItem, Back to then");
                    sendingDataProgressDialog.dismiss();
                    if (task.isCompleted()) {
                        logD(null, "saveItem, task.isCompleted()");
                        if (isVisible()) {
                            Toast.makeText(getActivity(), getString(R.string.item_saved_successfully), Toast.LENGTH_LONG).show();
                        }
                        mDirty = false;
                        mItem = task.getResult();
                        mItemId = mItem.getObjectId();
                        logD(null, "saveItem, mItem= " + mItem + ", mItemId= " + mItemId);
                    } else {
                        if (task.isFaulted()) {
                            logE("Fault while saveItem", "", task.getError());
                        } else {
                            logD("Canceled while saveItem", "");
                        }
                        if (isVisible()) {
                            Toast.makeText(getActivity(), getString(R.string.error_saving_item) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                        }
                    }
                    ControlPanelActivity activity = (ControlPanelActivity) getActivity();
                    activity.switchFragment(ItemsFragment.getInstance());
                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR);
        } catch (IOException ioe) {
            logE("Exception while saveItem", "", ioe);
            if (sendingDataProgressDialog.isShowing()) {
                sendingDataProgressDialog.dismiss();
            }
            if (isVisible()) {
                Toast.makeText(getActivity(), getString(R.string.error_saving_item) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (ImageSelector imageSelector : mItemImages) {
            if (imageSelector.onActivityResult(requestCode, resultCode, data)) {
                return;
            }
        }
        switch (requestCode) {
            case HonarnamaSellApp.INTENT_CHOOSE_CATEGORY_CODE:

                if (resultCode == getActivity().RESULT_OK) {
                    mCategoryTextView.setError(null);
                    mCategoryName = data.getStringExtra("selectedCategoryName");
                    mCategoryId = data.getStringExtra("selectedCategoryObjectId");
                    setDirty(true);
                    mChooseCategoryButton.setText(mCategoryName);
                }
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mDirty) {
            for (ImageSelector imageSelector : mItemImages) {
                imageSelector.onSaveInstanceState(outState);
            }
            outState.putBoolean(SAVE_INSTANCE_STATE_KEY_DIRTY, true);
            outState.putString(SAVE_INSTANCE_STATE_KEY_ITEM_ID, mItemId);
            outState.putString(SAVE_INSTANCE_STATE_KEY_TITLE, mTitleEditText.getText().toString().trim());
            outState.putString(SAVE_INSTANCE_STATE_KEY_DESCRIPTION, mDescriptionEditText.getText().toString().trim());
            outState.putString(SAVE_INSTANCE_STATE_KEY_PRICE, mPriceEditText.getText().toString().trim());
            outState.putString(SAVE_INSTANCE_STATE_KEY_CATEGORY_ID, mCategoryId);
            outState.putString(SAVE_INSTANCE_STATE_KEY_CATEGORY_NAME, mChooseCategoryButton.getText().toString());
        }
    }
}
