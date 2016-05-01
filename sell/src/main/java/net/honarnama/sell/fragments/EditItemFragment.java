package net.honarnama.sell.fragments;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.parse.ImageSelector;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import net.honarnama.GRPCUtils;
import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.activity.ChooseArtCategoryActivity;
import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.core.model.ArtCategory;
import net.honarnama.core.utils.GravityTextWatcher;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.core.utils.PriceFormatterTextWatcher;
import net.honarnama.core.utils.TextUtil;
import net.honarnama.nano.ArtCategoryId;
import net.honarnama.nano.CreateOrUpdateItemReply;
import net.honarnama.nano.CreateOrUpdateItemRequest;
import net.honarnama.nano.GetItemReply;
import net.honarnama.nano.GetOrDeleteItemRequest;
import net.honarnama.nano.HonarnamaProto;
import net.honarnama.nano.ReplyProperties;
import net.honarnama.nano.RequestProperties;
import net.honarnama.nano.SellServiceGrpc;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;
import net.honarnama.sell.activity.ControlPanelActivity;
import net.honarnama.sell.model.HonarnamaUser;
import net.honarnama.sell.utils.Uploader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import bolts.Continuation;
import bolts.Task;
import io.fabric.sdk.android.services.concurrency.AsyncTask;


public class EditItemFragment extends HonarnamaBaseFragment implements View.OnClickListener {

    public static EditItemFragment mEditItemFragment;

    private static final String SAVE_INSTANCE_STATE_KEY_DIRTY = "dirty";
    private static final String SAVE_INSTANCE_STATE_KEY_ITEM_ID = "itemId";
    private static final String SAVE_INSTANCE_STATE_KEY_TITLE = "title";
    private static final String SAVE_INSTANCE_STATE_KEY_DESCRIPTION = "description";
    private static final String SAVE_INSTANCE_STATE_KEY_PRICE = "price";
    private static final String SAVE_INSTANCE_STATE_KEY_CATEGORY_ID = "categoryId";
    private static final String SAVE_INSTANCE_STATE_KEY_CATEGORY_PARENT_ID = "categoryParentId";
    private static final String SAVE_INSTANCE_STATE_KEY_CATEGORY_NAME = "categoryName";

    private EditText mTitleEditText;
    private EditText mDescriptionEditText;
    private EditText mPriceEditText;
    private TextView mImagesTitleTextView;
    private ScrollView mScrollView;

    private Button mChooseCategoryButton;
    private TextView mCategoryTextView;

    private ImageSelector[] mItemImages;

    private long mItemId;

    private boolean mDirty = false;
    private boolean mCreateNew = false;

    private int mCategoryId = -1;
    private int mCategoryParentId = -1;
    private String mCategoryName;

    private Tracker mTracker;

    ProgressDialog mProgressDialog;
    private RelativeLayout[] mItemImageLoadingPannel;

    Snackbar mSnackbar;
    private CoordinatorLayout mCoordinatorLayout;

    TextWatcher mTextWatcherToMarkDirty;

    public synchronized static EditItemFragment getInstance() {
        if (mEditItemFragment == null) {
            mEditItemFragment = new EditItemFragment();
        }
        return mEditItemFragment;
    }

    public void reset(Context context, boolean createNew) {

        if (mTitleEditText != null) {
            mTitleEditText.setText("");
            mDescriptionEditText.setText("");
            mPriceEditText.setText("");
            mChooseCategoryButton.setText(context.getString(R.string.select));
            for (ImageSelector imageSelector : mItemImages) {
                imageSelector.removeSelectedImage();
                imageSelector.setChanged(false);
                imageSelector.setDeleted(false);
            }
            mTitleEditText.setError(null);
            mDescriptionEditText.setError(null);
            mPriceEditText.setError(null);
            mCategoryTextView.setError(null);
            mImagesTitleTextView.setError(null);
        }
        mItemId = -1;
        mCategoryId = -1;
        mCategoryParentId = -1;
        mCategoryName = null;
        setDirty(false);
        mCreateNew = createNew;
    }

    public void setItemId(Context context, long itemId) {
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
        if (mItemId > 0) {
            return context.getString(R.string.nav_title_edit_item);
        } else {
            return context.getString(R.string.register_new_item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mPriceEditText.addTextChangedListener(new GravityTextWatcher(mPriceEditText));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
//
//        if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
//            Intent intent = new Intent(getActivity(), ControlPanelActivity.class);
//            getActivity().finish();
//            startActivity(intent);
//        }

        mTextWatcherToMarkDirty = new TextWatcher() {
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

        final View rootView = inflater.inflate(R.layout.fragment_edit_item, container, false);

        mTitleEditText = (EditText) rootView.findViewById(R.id.editProductTitle);

        mDescriptionEditText = (EditText) rootView.findViewById(R.id.editProductDescription);

        mPriceEditText = (EditText) rootView.findViewById(R.id.editItemPrice);
        mPriceEditText.addTextChangedListener(new PriceFormatterTextWatcher(mPriceEditText));

        mImagesTitleTextView = (TextView) rootView.findViewById(R.id.edit_item_images_title_text_view);
        mScrollView = (ScrollView) rootView.findViewById(R.id.edit_item_scroll_view);

        mCategoryTextView = (TextView) rootView.findViewById(R.id.edit_item_category_text_view);
        mChooseCategoryButton = (Button) rootView.findViewById(R.id.choose_art_category_btn);

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

        mItemImageLoadingPannel = new RelativeLayout[]{
                (RelativeLayout) rootView.findViewById(R.id.loadingPanel_1),
                (RelativeLayout) rootView.findViewById(R.id.loadingPanel_2),
                (RelativeLayout) rootView.findViewById(R.id.loadingPanel_3),
                (RelativeLayout) rootView.findViewById(R.id.loadingPanel_4)
        };

        for (ImageSelector imageSelector : mItemImages) {
            imageSelector.setActivity(getActivity());
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

        if (BuildConfig.DEBUG) {
            logD("onCreateView :: mCreateNew= " + mCreateNew);
        }

        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id
                .coordinatorLayout);

        if (mCreateNew) {
            reset(getActivity(), true);
            mTracker = HonarnamaSellApp.getInstance().getDefaultTracker();
            mTracker.setScreenName("AddItem");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        } else {
            mTracker = HonarnamaSellApp.getInstance().getDefaultTracker();
            mTracker.setScreenName("EditItem");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());

            boolean savedDirty = false;
            long savedItemId = -1;
            if (savedInstanceState != null) {
                savedDirty = savedInstanceState.getBoolean(SAVE_INSTANCE_STATE_KEY_DIRTY);
                savedItemId = savedInstanceState.getLong(SAVE_INSTANCE_STATE_KEY_ITEM_ID);
            }

            if (BuildConfig.DEBUG) {
                logD("onCreateView :: savedDirty= " + savedDirty + ", savedItemId= " + savedItemId);
            }

            if (savedDirty) {
                mDirty = true;
                for (ImageSelector imageSelector : mItemImages) {
                    imageSelector.restore(savedInstanceState);
                }
                mItemId = savedItemId;
                mTitleEditText.setText(savedInstanceState.getString(SAVE_INSTANCE_STATE_KEY_TITLE));
                mDescriptionEditText.setText(savedInstanceState.getString(SAVE_INSTANCE_STATE_KEY_DESCRIPTION));
                mChooseCategoryButton.setText(savedInstanceState.getString(SAVE_INSTANCE_STATE_KEY_CATEGORY_NAME));
                mCategoryId = savedInstanceState.getInt(SAVE_INSTANCE_STATE_KEY_CATEGORY_ID);
                mCategoryParentId = savedInstanceState.getInt(SAVE_INSTANCE_STATE_KEY_CATEGORY_PARENT_ID);
                mPriceEditText.setText(savedInstanceState.getString(SAVE_INSTANCE_STATE_KEY_PRICE));
            } else {
                if (mItemId >= 0) {
                    mScrollView.setVisibility(View.GONE);
                    new getItemAsync().execute();
                }
            }
        }

        rootView.findViewById(R.id.saveItemButton).setOnClickListener(this);

        return rootView;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.saveItemButton:
                if (formInputsAreValid()) {
                    if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                        return;
                    }
                    new CreateOrUpdateItemAsync().execute();
                }
                break;
            case R.id.choose_art_category_btn:
                Intent intent = new Intent(getActivity(), ChooseArtCategoryActivity.class);
                intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_INTENT_CALLER, HonarnamaBaseApp.PREF_NAME_SELL_APP);
                startActivityForResult(intent, HonarnamaSellApp.INTENT_CHOOSE_CATEGORY_CODE);
                break;
            default:
                break;
        }
    }

    private boolean formInputsAreValid() {

        final String title = mTitleEditText.getText().toString();
        final String price = TextUtil.normalizePrice(mPriceEditText.getText().toString());
        final String description = mDescriptionEditText.getText().toString();

        if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
            return false;
        }

        boolean noImage = true;
        for (ImageSelector imageSelector : mItemImages) {
            if ((imageSelector.getFinalImageUri() != null) || (imageSelector.isFileSet() && !imageSelector.isDeleted())) {
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


        if (mCategoryId < 0) {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case HonarnamaSellApp.INTENT_CHOOSE_CATEGORY_CODE:
                if (resultCode == getActivity().RESULT_OK) {
                    int selectedCatId = data.getIntExtra(HonarnamaBaseApp.EXTRA_KEY_CATEGORY_ID, 0);
                    int selectedCatParentId = data.getIntExtra(HonarnamaBaseApp.EXTRA_KEY_CATEGORY_PARENT_ID, 0);
                    if (selectedCatId != mCategoryId || selectedCatParentId != mCategoryParentId) {
                        setDirty(true);
                    }
                    mCategoryTextView.setError(null);
                    mCategoryName = data.getStringExtra(HonarnamaBaseApp.EXTRA_KEY_CATEGORY_NAME);
                    mCategoryId = selectedCatId;
                    mCategoryParentId = selectedCatParentId;
                    mChooseCategoryButton.setText(mCategoryName);
                }
                break;
            default:
                for (ImageSelector imageSelector : mItemImages) {
                    if (imageSelector.onActivityResult(requestCode, resultCode, data)) {
                        return;
                    }
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
            outState.putLong(SAVE_INSTANCE_STATE_KEY_ITEM_ID, mItemId);
            outState.putString(SAVE_INSTANCE_STATE_KEY_TITLE, mTitleEditText.getText().toString().trim());
            outState.putString(SAVE_INSTANCE_STATE_KEY_DESCRIPTION, mDescriptionEditText.getText().toString().trim());
            outState.putString(SAVE_INSTANCE_STATE_KEY_PRICE, mPriceEditText.getText().toString().trim());
            outState.putInt(SAVE_INSTANCE_STATE_KEY_CATEGORY_ID, mCategoryId);
            outState.putInt(SAVE_INSTANCE_STATE_KEY_CATEGORY_PARENT_ID, mCategoryId);
            outState.putString(SAVE_INSTANCE_STATE_KEY_CATEGORY_NAME, mChooseCategoryButton.getText().toString());
        }
    }

    public class getItemAsync extends AsyncTask<Void, Void, GetItemReply> {
        GetOrDeleteItemRequest getOrDeleteItemRequest;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            displayProgressDialog(null);
        }

        @Override
        protected GetItemReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();

            getOrDeleteItemRequest = new GetOrDeleteItemRequest();
            getOrDeleteItemRequest.id = mItemId;
            getOrDeleteItemRequest.requestProperties = rp;
            GetItemReply getItemReply;
            logD("getOrDeleteItemRequest is: " + getOrDeleteItemRequest);

            try {
                SellServiceGrpc.SellServiceBlockingStub stub = GRPCUtils.getInstance().getSellServiceGrpc();
                getItemReply = stub.getItem(getOrDeleteItemRequest);
                return getItemReply;
            } catch (Exception e) {
                logE("Error getting item info. getOrDeleteItemRequest: " + getOrDeleteItemRequest + ". Error: " + e, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(GetItemReply getItemReply) {
            super.onPostExecute(getItemReply);
            if (BuildConfig.DEBUG) {
                logD("getItemReply is: " + getItemReply);
            }
            dismissProgressDialog();
            if (getItemReply != null) {
                switch (getItemReply.replyProperties.statusCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:
                        ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) getActivity());
                        if (controlPanelActivity != null) {
                            controlPanelActivity.displayUpgradeRequiredDialog();
                        }
                        break;
                    case ReplyProperties.CLIENT_ERROR:
                        switch (getItemReply.errorCode) {

                            case GetItemReply.NO_CLIENT_ERROR:
                                logE("Got NO_CLIENT_ERROR code for getting item in EditItemFragment. getOrDeleteItemRequest: " + getOrDeleteItemRequest + ". User Id: " + HonarnamaUser.getId() + ".");
                                displayShortToast(getString(R.string.error_occured));
                                break;

                            case GetItemReply.ITEM_NOT_FOUND:
                                displayLongToast(getString(R.string.item_not_found));
                                break;

                            case GetItemReply.FORBIDDEN:
                                displayLongToast(getString(R.string.not_allowed_to_do_this_action));
                                logE("Got FORBIDDEN reply while trying to get item. getOrDeleteItemRequest: " + getOrDeleteItemRequest + ". User Id: " + HonarnamaUser.getId() + ".");
                                break;
                        }
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        displaySnackbar();
                        displayShortToast(getString(R.string.server_error_try_again));
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        HonarnamaUser.logout(getActivity());
                        break;

                    case ReplyProperties.OK:
                        mScrollView.setVisibility(View.VISIBLE);
                        setItemInfo(getItemReply.item, true);
                        break;
                }

            } else {
                displaySnackbar();
                displayLongToast(getString(R.string.check_net_connection));
            }
        }
    }


    public void setItemInfo(net.honarnama.nano.Item item, boolean loadImages) {
        if (item != null) {
            mItemId = item.id;

            mTitleEditText.setText(item.name);
            mDescriptionEditText.setText(item.description);
            mPriceEditText.setText(item.price + "");

            if (item.artCategoryId.level2Id > 0) {
                mCategoryParentId = item.artCategoryId.level1Id;
                mCategoryId = item.artCategoryId.level2Id;
            } else {
                mCategoryParentId = 0;
                mCategoryId = item.artCategoryId.level1Id;
            }
            mChooseCategoryButton.setText(getString(R.string.getting_information));
            new ArtCategory().getCategoryNameById(mCategoryId).continueWith(new Continuation<String, Object>() {
                @Override
                public Object then(Task<String> task) throws Exception {
                    if (task.isFaulted()) {
                        displayLongToast(getString(R.string.error_finding_category_name) + getString(R.string.check_net_connection));
                    } else {
                        mChooseCategoryButton.setText(task.getResult());
                    }
                    return null;
                }
            });

            if (loadImages) {
                for (int i = 0; i < 4; i++) {
                    if (!TextUtils.isEmpty(item.images[i])) {
                        String itemImage = item.images[i];

                        mItemImageLoadingPannel[i].setVisibility(View.VISIBLE);
                        mItemImages[i].setVisibility(View.GONE);

                        final int index = i;

                        Picasso.with(getActivity()).load(itemImage)
                                .error(R.drawable.camera_insta)
                                .memoryPolicy(MemoryPolicy.NO_CACHE)
                                .networkPolicy(NetworkPolicy.NO_CACHE)
                                .into(mItemImages[i], new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        mItemImageLoadingPannel[index].setVisibility(View.GONE);
                                        mItemImages[index].setVisibility(View.VISIBLE);
                                        mItemImages[index].setFileSet(true);
                                    }

                                    @Override
                                    public void onError() {
                                        displayShortToast(getString(R.string.error_displaying_image) + getString(R.string.check_net_connection));
                                        mItemImages[index].setVisibility(View.VISIBLE);
                                    }
                                });

                    }

                }
            }
        }

        mTitleEditText.addTextChangedListener(mTextWatcherToMarkDirty);
        mDescriptionEditText.addTextChangedListener(mTextWatcherToMarkDirty);
        mPriceEditText.addTextChangedListener(mTextWatcherToMarkDirty);

        setDirty(false);
    }

    public class CreateOrUpdateItemAsync extends AsyncTask<Void, Void, CreateOrUpdateItemReply> {
        CreateOrUpdateItemRequest createOrUpdateItemRequest;
        String cToastMsg = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            displayProgressDialog(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (!TextUtils.isEmpty(cToastMsg)) {
                        displayLongToast(cToastMsg);
                        cToastMsg = "";
                    }
                }
            });
        }

        @Override
        protected CreateOrUpdateItemReply doInBackground(Void... voids) {

            final String title = mTitleEditText.getText().toString().trim();
            final String description = mDescriptionEditText.getText().toString().trim();
            final long price = Integer.valueOf(TextUtil.normalizePrice(TextUtil.convertFaNumberToEn(mPriceEditText.getText().toString().trim())));

            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();

            createOrUpdateItemRequest = new CreateOrUpdateItemRequest();
            createOrUpdateItemRequest.item = new net.honarnama.nano.Item();
            createOrUpdateItemRequest.item.id = mItemId;
            createOrUpdateItemRequest.item.name = title;
            createOrUpdateItemRequest.item.description = description;
            createOrUpdateItemRequest.item.price = price;
            createOrUpdateItemRequest.item.artCategoryId = new ArtCategoryId();
            if (mCategoryParentId == 0) {
                createOrUpdateItemRequest.item.artCategoryId.level1Id = mCategoryId;
            } else {
                createOrUpdateItemRequest.item.artCategoryId.level1Id = mCategoryParentId;
                createOrUpdateItemRequest.item.artCategoryId.level2Id = mCategoryId;
            }

            createOrUpdateItemRequest.changingImage = new int[4];
            for (int i = 0; i < mItemImages.length; i++) {
                if (mItemImages[i].isDeleted()) {
                    createOrUpdateItemRequest.changingImage[i] = HonarnamaProto.DELETE;
                } else if (mItemImages[i].isChanged() && mItemImages[i].getFinalImageUri() != null) {
                    createOrUpdateItemRequest.changingImage[i] = HonarnamaProto.PUT;
                } else {
                    createOrUpdateItemRequest.changingImage[i] = HonarnamaProto.NOOP;
                }
            }

            createOrUpdateItemRequest.requestProperties = rp;
            logD("createOrUpdateItemRequest is: " + createOrUpdateItemRequest);
            CreateOrUpdateItemReply createOrUpdateItemReply;
            try {
                SellServiceGrpc.SellServiceBlockingStub stub = GRPCUtils.getInstance().getSellServiceGrpc();
                if (mItemId > 0) {
                    createOrUpdateItemReply = stub.updateItem(createOrUpdateItemRequest);
                } else {
                    createOrUpdateItemReply = stub.createItem(createOrUpdateItemRequest);
                }
                return createOrUpdateItemReply;
            } catch (Exception e) {
                logE("Error running createOrUpdateItemRequest. createOrUpdateItemRequest: " + createOrUpdateItemRequest + ". Error: " + e, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final CreateOrUpdateItemReply createOrUpdateItemReply) {
            super.onPostExecute(createOrUpdateItemReply);
            logD("createOrUpdateItemReply is: " + createOrUpdateItemReply);
            if (createOrUpdateItemReply != null) {
                switch (createOrUpdateItemReply.replyProperties.statusCode) {

                    case ReplyProperties.OK:
                        setItemInfo(createOrUpdateItemReply.uptodateItem, false);

                        ArrayList<Task<Void>> tasks = new ArrayList<>();
                        for (int i = 0; i < mItemImages.length; i++) {
                            if (!TextUtils.isEmpty(createOrUpdateItemReply.imageModificationUrl[i]) && mItemImages[i].getFinalImageUri() != null) {
                                final File storeBannerImageFile = new File(mItemImages[i].getFinalImageUri().getPath());
                                tasks.add(new Uploader(storeBannerImageFile, createOrUpdateItemReply.imageModificationUrl[i]).upload());
                            }

                            if (mItemImages[i].isDeleted()) {
                                mItemImages[i].setDeleted(false);
                            }
                        }

                        Task.whenAll(tasks).continueWith(new Continuation<Void, Object>() {
                            @Override
                            public Object then(Task<Void> task) throws Exception {
                                if (task.isFaulted()) {
                                    cToastMsg = getString(R.string.error_sending_images) + getString(R.string.check_net_connection);
//                                    displayShortToast(getString(R.string.error_sending_images) + getString(R.string.check_net_connection));
                                } else {
                                    mDirty = false;
                                    cToastMsg = getString(R.string.item_saved_successfully);
                                    for (int i = 0; i < mItemImages.length; i++) {
                                        mItemImages[i].setChanged(false);
                                    }
//                                    displayShortToast(getString(R.string.item_saved_successfully));
                                }
                                dismissProgressDialog();
                                return null;
                            }
                        });
                        break;

                    case ReplyProperties.CLIENT_ERROR:
                        switch (createOrUpdateItemReply.errorCode) {
                            case CreateOrUpdateItemReply.NO_CLIENT_ERROR:
                                logE("Got NO_CLIENT_ERROR code for updating item. createOrUpdateItemRequest: " + createOrUpdateItemRequest + ". User Id: " + HonarnamaUser.getId() + ".");
//                                displayShortToast(getString(R.string.error_occured));
                                cToastMsg = getString(R.string.error_occured);
                                break;
                            case CreateOrUpdateItemReply.FORBIDDEN:
//                                displayLongToast(getString(R.string.not_allowed_to_do_this_action));
                                logE("Got FORBIDDEN reply while trying createOrUpdateItem. createOrUpdateItemRequest: " + createOrUpdateItemRequest + ". User Id: " + HonarnamaUser.getId() + ".");
                                cToastMsg = getString(R.string.not_allowed_to_do_this_action);
                                break;
                            case CreateOrUpdateItemReply.ITEM_NOT_FOUND:
//                                displayLongToast(getString(R.string.item_not_found));
                                cToastMsg = getString(R.string.item_not_found);
                                break;
                            case CreateOrUpdateItemReply.EMPTY_ITEM:
                                logE("CreateOrUpdateItemReply was EMPTY_ITEM. createOrUpdateItemRequest: " + createOrUpdateItemRequest);
//                                displayShortToast(getString(R.string.error_occured));
                                cToastMsg = getString(R.string.error_occured);
                                break;
                            case CreateOrUpdateItemReply.STORE_NOT_CREATED:
//                                displayLongToast(getString(R.string.store_not_created));
                                cToastMsg = getString(R.string.store_not_created);
                                break;
                        }
                        dismissProgressDialog();
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        cToastMsg = getString(R.string.server_error_try_again);
                        dismissProgressDialog();
//                        displayShortToast(getString(R.string.server_error_try_again));
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        dismissProgressDialog();
                        HonarnamaUser.logout(getActivity());
                        break;

                    case ReplyProperties.UPGRADE_REQUIRED:
                        dismissProgressDialog();
                        ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) getActivity());
                        if (controlPanelActivity != null) {
                            controlPanelActivity.displayUpgradeRequiredDialog();
                        }
                        break;
                }

            } else {
                cToastMsg = getString(R.string.error_connecting_to_Server) + getString(R.string.check_net_connection);
                dismissProgressDialog();
//                displayLongToast(getString(R.string.error_connecting_to_Server) + getString(R.string.check_net_connection));
            }
        }
    }

    private void dismissProgressDialog() {
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    }

    private void displayProgressDialog(DialogInterface.OnDismissListener onDismissListener) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(getString(R.string.please_wait));
        }

        if (onDismissListener != null) {
            mProgressDialog.setOnDismissListener(onDismissListener);
        }

        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing() && isVisible()) {
            mProgressDialog.show();
        }

    }

    public void displaySnackbar() {
        if (mSnackbar != null && mSnackbar.isShown()) {
            mSnackbar.dismiss();
        }

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(" ").append(getString(R.string.error_connecting_to_Server)).append(" ");

        mSnackbar = Snackbar.make(mCoordinatorLayout, builder, Snackbar.LENGTH_INDEFINITE);
        View sbView = mSnackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setBackgroundColor(getResources().getColor(R.color.amber));
        textView.setSingleLine(false);
        textView.setGravity(Gravity.CENTER);
        Spannable spannable = (Spannable) textView.getText();
        spannable.setSpan(new ImageSpan(getActivity(), android.R.drawable.stat_notify_sync), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        sbView.setBackgroundColor(getResources().getColor(R.color.amber));

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkManager.getInstance().isNetworkEnabled(true)) {
                    new getItemAsync().execute();
                    if (mSnackbar != null && mSnackbar.isShown()) {
                        mSnackbar.dismiss();
                    }
                }
            }
        });

        mSnackbar.show();
    }

}
