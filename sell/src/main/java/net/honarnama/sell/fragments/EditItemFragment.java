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
import net.honarnama.base.activity.ChooseArtCategoryActivity;
import net.honarnama.base.fragment.HonarnamaBaseFragment;
import net.honarnama.base.model.ArtCategory;
import net.honarnama.base.utils.GravityTextWatcher;
import net.honarnama.base.utils.NetworkManager;
import net.honarnama.base.utils.PriceFormatterTextWatcher;
import net.honarnama.base.utils.TextUtil;
import net.honarnama.base.utils.WindowUtil;
import net.honarnama.nano.ArtCategoryCriteria;
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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

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
    private static final String SAVE_INSTANCE_STATE_KEY_CONTENT_IS_VISIBLE = "content_is_visible";

    private EditText mTitleEditText;
    private EditText mDescriptionEditText;
    private EditText mPriceEditText;
    private TextView mImagesTitleTextView;
    private ScrollView mScrollView;
    private RelativeLayout[] mItemImageLoadingPannel;
    private Button mChooseCategoryButton;
    private TextView mCategoryTextView;
    private CoordinatorLayout mCoordinatorLayout;
    LinearLayout mMainContent;
    TextView mEmptyView;
    Snackbar mSnackbar;

    private ImageSelector[] mItemImages;

    private long mItemId = -1;
    private boolean mDirty = false;
    private boolean mCreateNew = false;
    private int mCategoryId = -1;
    private int mCategoryParentId = -1;
    private String mCategoryName = "";
    private Tracker mTracker;

    TextWatcher mTextWatcherToMarkDirty;

    public synchronized static EditItemFragment getInstance() {
        if (mEditItemFragment == null) {
            mEditItemFragment = new EditItemFragment();
        }
        return mEditItemFragment;
    }

    public void reset(boolean createNew) {

        setTextInFragment(mTitleEditText, "");
        setTextInFragment(mDescriptionEditText, "");
        setTextInFragment(mPriceEditText, "");
        setTextInFragment(mChooseCategoryButton, getStringInFragment(R.string.select));

        if (mItemImages != null) {
            for (ImageSelector imageSelector : mItemImages) {
                if (imageSelector != null && isAdded()) {
                    imageSelector.removeSelectedImage();
                    imageSelector.setChanged(false);
                    imageSelector.setDeleted(false);
                }
            }
        }

        setErrorInFragment(mTitleEditText, "");
        setErrorInFragment(mDescriptionEditText, "");
        setErrorInFragment(mPriceEditText, "");
        setErrorInFragment(mCategoryTextView, "");
        setErrorInFragment(mImagesTitleTextView, "");
        mItemId = -1;
        mCategoryId = -1;
        mCategoryParentId = -1;
        mCategoryName = "";
        setDirty(false);

        mCreateNew = createNew;

        if (BuildConfig.DEBUG) {
            Log.d("STOPPED_ACTIVITY", "reset of EIF. mCreateNew: " + mCreateNew);
        }

        if (mCreateNew) {
            setVisibilityInFragment(mMainContent, View.VISIBLE);
            setVisibilityInFragment(mEmptyView, View.GONE);
        }

        dismissSnackbar();
    }

    public void setItemId(long itemId) {
        reset(false);
        mItemId = itemId;
    }

    private void setDirty(boolean dirty) {
        mDirty = dirty;
    }

    public boolean isDirty() {
        return mDirty;
    }

    @Override
    public String getTitle() {
        if (mItemId > 0) {
            return getStringInFragment(R.string.nav_title_edit_item);
        } else {
            return getStringInFragment(R.string.register_new_item);
        }
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

        Activity activity = getActivity();

        mTextWatcherToMarkDirty = new TextWatcher() {
            String mValue;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mValue = charSequence + "";
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!mValue.equals(editable + "")) {
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
                        setErrorInFragment(mImagesTitleTextView, "");
                        mDirty = true;
                        return true;
                    }

                    @Override
                    public void onImageRemoved() {
                        mDirty = true;
                    }

                    @Override
                    public void onImageSelectionFailed() {
                        if (BuildConfig.DEBUG) {
                            logD("onImageSelectionFailed");
                        }
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
            if (activity != null && imageSelector != null && isAdded()) {
                imageSelector.setActivity(activity);
                imageSelector.setOnImageSelectedListener(onImageSelectedListener);
            } else {
                if (BuildConfig.DEBUG) {
                    logD("Setting imageSelector activity and listeners failed. Fragment activity was null or fragment was not added.");
                }
            }
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

        mMainContent = (LinearLayout) rootView.findViewById(R.id.main_content);
        mEmptyView = (TextView) rootView.findViewById(R.id.empty_view);

        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id
                .coordinatorLayout);

        if (mCreateNew) {
            reset(true);
            mTracker = HonarnamaSellApp.getInstance().getDefaultTracker();
            mTracker.setScreenName("AddItem");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        } else {
            mTracker = HonarnamaSellApp.getInstance().getDefaultTracker();
            mTracker.setScreenName("EditItem");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }

        if (BuildConfig.DEBUG) {
            Log.d("STOPPED_ACTIVITY", "onCreateView of EIF. savedInstanceState: " + savedInstanceState);
        }

        if (savedInstanceState != null) {
            mDirty = savedInstanceState.getBoolean(SAVE_INSTANCE_STATE_KEY_DIRTY);
            mItemId = savedInstanceState.getLong(SAVE_INSTANCE_STATE_KEY_ITEM_ID);
            for (ImageSelector imageSelector : mItemImages) {
                imageSelector.restore(savedInstanceState);
            }
            setTextInFragment(mTitleEditText, savedInstanceState.getString(SAVE_INSTANCE_STATE_KEY_TITLE));
            setTextInFragment(mDescriptionEditText, savedInstanceState.getString(SAVE_INSTANCE_STATE_KEY_DESCRIPTION));
            setTextInFragment(mChooseCategoryButton, savedInstanceState.getString(SAVE_INSTANCE_STATE_KEY_CATEGORY_NAME));
            setTextInFragment(mPriceEditText, savedInstanceState.getString(SAVE_INSTANCE_STATE_KEY_PRICE));
            mCategoryId = savedInstanceState.getInt(SAVE_INSTANCE_STATE_KEY_CATEGORY_ID);
            mCategoryParentId = savedInstanceState.getInt(SAVE_INSTANCE_STATE_KEY_CATEGORY_PARENT_ID);

            if (mItemId > 0 && !savedInstanceState.getBoolean(SAVE_INSTANCE_STATE_KEY_CONTENT_IS_VISIBLE)) {
                if (BuildConfig.DEBUG) {
                    Log.d("STOPPED_ACTIVITY", "calling getItemAsync. mItemId: " + mItemId + ". SAVE_INSTANCE_STATE_KEY_CONTENT_IS_VISIBLE: " + savedInstanceState.getBoolean(SAVE_INSTANCE_STATE_KEY_CONTENT_IS_VISIBLE));
                }
                new getItemAsync().execute();
            }

            if (isAdded() && mTitleEditText != null) {
                mTitleEditText.addTextChangedListener(mTextWatcherToMarkDirty);
                mDescriptionEditText.addTextChangedListener(mTextWatcherToMarkDirty);
                mPriceEditText.addTextChangedListener(mTextWatcherToMarkDirty);
            }

        } else {
            if (mItemId >= 0) {
                new getItemAsync().execute();
            }
        }

        rootView.findViewById(R.id.saveItemButton).setOnClickListener(this);

        if (activity != null) {
            ((ControlPanelActivity) activity).checkAndAskStoragePermission(activity);
        }

        resetErrors();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (BuildConfig.DEBUG) {
            Log.d("STOPPED_ACTIVITY", "onResume of EIF. mItemId: " + mItemId);
        }

        if (isAdded()) {
            mPriceEditText.addTextChangedListener(new GravityTextWatcher(mPriceEditText));
            ControlPanelActivity activity = (ControlPanelActivity) getActivity();
            if (activity != null) {
                activity.setTitle(getTitle());
            }
        }
        if (mItemId <= 0) { //new item
            setVisibilityInFragment(mMainContent, View.VISIBLE);
            setVisibilityInFragment(mEmptyView, View.GONE);
        }
    }


    @Override
    public void onClick(View view) {
        Activity activity = getActivity();
        switch (view.getId()) {
            case R.id.saveItemButton:
                WindowUtil.hideKeyboard(activity);
                if (formInputsAreValid()) {
                    new CreateOrUpdateItemAsync().execute();
                }
                break;
            case R.id.choose_art_category_btn:
                if (activity != null) {
                    Intent intent = new Intent(activity, ChooseArtCategoryActivity.class);
                    startActivityForResult(intent, HonarnamaSellApp.INTENT_CHOOSE_CATEGORY_CODE);
                }
                break;
            default:
                break;
        }
    }

    public void resetErrors() {
        setErrorInFragment(mImagesTitleTextView, "");
        setErrorInFragment(mTitleEditText, "");
        setErrorInFragment(mPriceEditText, "");
        setErrorInFragment(mCategoryTextView, "");
        setErrorInFragment(mDescriptionEditText, "");
    }

    private boolean formInputsAreValid() {

        if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
            return false;
        }

        String title = getTextInFragment(mTitleEditText);
        String price = TextUtil.normalizePrice(getTextInFragment(mPriceEditText));
        String description = getTextInFragment(mDescriptionEditText);

        resetErrors();

        boolean noImage = true;
        for (ImageSelector imageSelector : mItemImages) {
            if (isAdded() && imageSelector != null &&
                    (imageSelector.getFinalImageUri() != null) || (imageSelector.isFileSet() && !imageSelector.isDeleted())
                    ) {
                noImage = false;
                break;
            }
        }
        if (noImage) {
            requestFocusInFragment(mImagesTitleTextView);
            setErrorInFragment(mImagesTitleTextView, getStringInFragment(R.string.error_edit_item_no_image));
            if (isAdded() && mScrollView != null) {
                mScrollView.fullScroll(ScrollView.FOCUS_UP);
            }
            displayShortToast(getStringInFragment(R.string.error_edit_item_no_image));
            return false;
        }

        if (title.length() == 0) {
            requestFocusInFragment(mTitleEditText);
            setErrorInFragment(mTitleEditText, getStringInFragment(R.string.error_edit_item_title_is_empty));
            displayShortToast(getStringInFragment(R.string.error_edit_item_title_is_empty));
            return false;
        }

        if (price.length() == 0) {
            requestFocusInFragment(mPriceEditText);
            setErrorInFragment(mPriceEditText, getStringInFragment(R.string.error_item_price_not_set));
            displayShortToast(getStringInFragment(R.string.error_item_price_not_set));
            return false;
        }

        if (Integer.valueOf(price) < 100) {
            requestFocusInFragment(mPriceEditText);
            setErrorInFragment(mPriceEditText, getStringInFragment(R.string.error_item_price_is_low));
            displayShortToast(getStringInFragment(R.string.error_item_price_is_low));
            return false;
        }

        if (mCategoryId < 0) {
            requestFocusInFragment(mCategoryTextView);
            setErrorInFragment(mCategoryTextView, getStringInFragment(R.string.error_category_is_not_selected));
            displayShortToast(getStringInFragment(R.string.error_category_is_not_selected));
            return false;
        }

        if (description.length() == 0) {
            requestFocusInFragment(mDescriptionEditText);
            setErrorInFragment(mDescriptionEditText, getStringInFragment(R.string.error_edit_item_description_is_empty));
            displayShortToast(getStringInFragment(R.string.error_edit_item_description_is_empty));
            return false;
        }

        if (!mDirty) {
            displayShortToast(getStringInFragment(R.string.item_not_changed));
            return false;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case HonarnamaSellApp.INTENT_CHOOSE_CATEGORY_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    int selectedCatId = data.getIntExtra(HonarnamaBaseApp.EXTRA_KEY_CATEGORY_ID, 0);
                    int selectedCatParentId = data.getIntExtra(HonarnamaBaseApp.EXTRA_KEY_CATEGORY_PARENT_ID, 0);
                    if (selectedCatId != mCategoryId || selectedCatParentId != mCategoryParentId) {
                        setDirty(true);
                    }

                    setErrorInFragment(mCategoryTextView, "");
                    mCategoryName = data.getStringExtra(HonarnamaBaseApp.EXTRA_KEY_CATEGORY_NAME);
                    mCategoryId = selectedCatId;
                    mCategoryParentId = selectedCatParentId;
                    setTextInFragment(mChooseCategoryButton, mCategoryName);
                }
                break;
            default:

                if (mItemImages != null) {
                    for (ImageSelector imageSelector : mItemImages) {
                        if (imageSelector != null && imageSelector.onActivityResult(requestCode, resultCode, data)) {
                            return;
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mItemImages != null) {
            for (ImageSelector imageSelector : mItemImages) {
                if (imageSelector != null) {
                    imageSelector.onSaveInstanceState(outState);
                }
            }
        }

        outState.putBoolean(SAVE_INSTANCE_STATE_KEY_DIRTY, mDirty);
        outState.putLong(SAVE_INSTANCE_STATE_KEY_ITEM_ID, mItemId);
        outState.putString(SAVE_INSTANCE_STATE_KEY_TITLE, getTextInFragment(mTitleEditText));
        outState.putString(SAVE_INSTANCE_STATE_KEY_DESCRIPTION, getTextInFragment(mDescriptionEditText));
        outState.putString(SAVE_INSTANCE_STATE_KEY_PRICE, getTextInFragment(mPriceEditText));
        outState.putInt(SAVE_INSTANCE_STATE_KEY_CATEGORY_ID, mCategoryId);
        outState.putInt(SAVE_INSTANCE_STATE_KEY_CATEGORY_PARENT_ID, mCategoryParentId);
        outState.putString(SAVE_INSTANCE_STATE_KEY_CATEGORY_NAME, getTextInFragment(mChooseCategoryButton));
        outState.putBoolean(SAVE_INSTANCE_STATE_KEY_CONTENT_IS_VISIBLE, (mMainContent.getVisibility() == View.VISIBLE));
    }

    public class getItemAsync extends AsyncTask<Void, Void, GetItemReply> {
        GetOrDeleteItemRequest getOrDeleteItemRequest;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setVisibilityInFragment(mMainContent, View.GONE);
            setTextInFragment(mEmptyView, getStringInFragment(R.string.getting_information));
            setVisibilityInFragment(mEmptyView, View.VISIBLE);
            displayProgressDialog(null);
        }

        @Override
        protected GetItemReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();

            getOrDeleteItemRequest = new GetOrDeleteItemRequest();
            getOrDeleteItemRequest.id = mItemId;
            getOrDeleteItemRequest.requestProperties = rp;
            GetItemReply getItemReply;

            if (BuildConfig.DEBUG) {
                logD("getOrDeleteItemRequest is: " + getOrDeleteItemRequest);
            }

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

            Activity activity = getActivity();

            if (BuildConfig.DEBUG) {
                logD("getItemReply is: " + getItemReply);
            }
            dismissProgressDialog();
            if (getItemReply != null) {
                switch (getItemReply.replyProperties.statusCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:
                        if (activity != null) {
                            ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) activity);
                            controlPanelActivity.displayUpgradeRequiredDialog();
                        } else {
                            displayLongToast(getStringInFragment(R.string.upgrade_to_new_version));
                        }
                        break;
                    case ReplyProperties.CLIENT_ERROR:
                        switch (getItemReply.errorCode) {

                            case GetItemReply.NO_CLIENT_ERROR:
                                logE("Got NO_CLIENT_ERROR code for getting item in EditItemFragment. getOrDeleteItemRequest: " + getOrDeleteItemRequest + ". User Id: " + HonarnamaUser.getId() + ".");
                                displayShortToast(getStringInFragment(R.string.error_getting_info));
                                break;

                            case GetItemReply.ITEM_NOT_FOUND:
                                displayLongToast(getStringInFragment(R.string.item_not_found));
                                break;

                            case GetItemReply.FORBIDDEN:
                                displayLongToast(getStringInFragment(R.string.not_allowed_to_do_this_action));
                                logE("Got FORBIDDEN reply while trying to get item. getOrDeleteItemRequest: " + getOrDeleteItemRequest + ". User Id: " + HonarnamaUser.getId() + ".");
                                break;
                        }
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        setTextInFragment(mEmptyView, getStringInFragment(R.string.error_getting_item_info));
                        displayRetrySnackbar();
                        displayShortToast(getStringInFragment(R.string.server_error_try_again));
                        logE("Server error upon getItemAsync. request: " + getOrDeleteItemRequest);
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        HonarnamaUser.logout(activity);
                        displayLongToast(getStringInFragment(R.string.login_again));
                        break;

                    case ReplyProperties.OK:
                        if (getItemReply.item != null) {
                            setVisibilityInFragment(mEmptyView, View.GONE);
                            setVisibilityInFragment(mMainContent, View.VISIBLE);
                            setItemInfo(getItemReply.item, true);
                        } else {
                            if (isAdded()) {
                                setTextInFragment(mEmptyView, getStringInFragment(R.string.error_getting_item_info));
                                displayShortToast(getStringInFragment(R.string.error_getting_item_info));
                                displayRetrySnackbar();
                                logE("Got OK code for getting item (id " + mItemId + "), but item was null. getOrDeleteItemRequest: " + getOrDeleteItemRequest);
                            }
                        }
                        break;
                }

            } else {
                setTextInFragment(mEmptyView, getStringInFragment(R.string.error_getting_item_info));
                displayRetrySnackbar();
            }
        }
    }


    public void setItemInfo(net.honarnama.nano.Item item, boolean loadImages) {

        if (!isAdded()) {
            return;
        }
        if (item != null) {
            mItemId = item.id;
            Activity activity = getActivity();

            if (item.artCategoryCriteria.level2Id > 0) {
                mCategoryParentId = item.artCategoryCriteria.level1Id;
                mCategoryId = item.artCategoryCriteria.level2Id;
            } else {
                mCategoryParentId = 0;
                mCategoryId = item.artCategoryCriteria.level1Id;
            }

            setTextInFragment(mTitleEditText, item.name);
            setTextInFragment(mDescriptionEditText, item.description);
            setTextInFragment(mPriceEditText, item.price + "");
            setTextInFragment(mChooseCategoryButton, getStringInFragment(R.string.getting_information));
            new ArtCategory().getCategoryNameById(mCategoryId).continueWith(new Continuation<String, Object>() {
                @Override
                public Object then(Task<String> task) throws Exception {
                    if (task.isFaulted()) {
                        displayLongToast(getStringInFragment(R.string.error_finding_category_name) + getStringInFragment(R.string.check_net_connection));
                        setTextInFragment(mChooseCategoryButton, getStringInFragment(R.string.error_getting_info));
                        mCategoryId = -1;
                        mCategoryParentId = -1;
                    } else {
                        setTextInFragment(mChooseCategoryButton, task.getResult());
                    }
                    return null;
                }
            });

            if (loadImages && activity != null && isAdded()) {
                for (int i = 0; i < mItemImages.length; i++) {
                    if (!TextUtils.isEmpty(item.images[i])) {

                        final String itemImage = item.images[i];

                        setVisibilityInFragment(mItemImageLoadingPannel[i], View.VISIBLE);
                        setVisibilityInFragment(mItemImages[i], View.GONE);

                        final int index = i;

                        if (!isAdded()) {
                            break;
                        }
                        Picasso.with(activity).load(itemImage)
                                .error(R.drawable.camera_insta)
                                .memoryPolicy(MemoryPolicy.NO_CACHE)
                                .networkPolicy(NetworkPolicy.NO_CACHE)
                                .into(mItemImages[i], new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        setVisibilityInFragment(mItemImageLoadingPannel[index], View.GONE);
                                        setVisibilityInFragment(mItemImages[index], View.VISIBLE);
                                        if (mItemImages[index] != null && isAdded()) {
                                            mItemImages[index].setFileSet(true);
                                            mItemImages[index].setLoadingURL(itemImage);
                                        }
                                    }

                                    @Override
                                    public void onError() {
                                        setVisibilityInFragment(mItemImageLoadingPannel[index], View.GONE);
                                        displayShortToast(getStringInFragment(R.string.error_displaying_image) + getStringInFragment(R.string.check_net_connection));
                                        setVisibilityInFragment(mItemImages[index], View.VISIBLE);
                                    }
                                });
//
//                        if (BuildConfig.DEBUG) {
////                            Log.d("STOPPED_ACTIVITY", "mItemImages[index].getDrawable(): " + mItemImages[index].getDrawable());
//                        }

                    }

                }
            }

        }

        if (isAdded() && mTitleEditText != null) {
            mTitleEditText.addTextChangedListener(mTextWatcherToMarkDirty);
            mDescriptionEditText.addTextChangedListener(mTextWatcherToMarkDirty);
            mPriceEditText.addTextChangedListener(mTextWatcherToMarkDirty);
        }
        setDirty(false);
    }

    public class CreateOrUpdateItemAsync extends AsyncTask<Void, Void, CreateOrUpdateItemReply> {
        CreateOrUpdateItemRequest cCreateOrUpdateItemRequest;
        String cToastMsg = "";

        String title = "";
        String description = "";
        long price = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            title = getTextInFragment(mTitleEditText);
            description = getTextInFragment(mDescriptionEditText);
            price = Integer.valueOf(TextUtil.normalizePrice(TextUtil.convertFaNumberToEn(getTextInFragment(mPriceEditText))));

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

            if (TextUtils.isEmpty(title)) {
                return null;
            }

            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            cCreateOrUpdateItemRequest = new CreateOrUpdateItemRequest();
            cCreateOrUpdateItemRequest.item = new net.honarnama.nano.Item();
            cCreateOrUpdateItemRequest.item.id = mItemId;
            cCreateOrUpdateItemRequest.item.name = title;
            cCreateOrUpdateItemRequest.item.description = description;
            cCreateOrUpdateItemRequest.item.price = price;
            cCreateOrUpdateItemRequest.item.artCategoryCriteria = new ArtCategoryCriteria();
            if (mCategoryParentId == 0) {
                cCreateOrUpdateItemRequest.item.artCategoryCriteria.level1Id = mCategoryId;
                cCreateOrUpdateItemRequest.item.artCategoryCriteria.level2Id = mCategoryId;
            } else {
                cCreateOrUpdateItemRequest.item.artCategoryCriteria.level1Id = mCategoryParentId;
                cCreateOrUpdateItemRequest.item.artCategoryCriteria.level2Id = mCategoryId;
            }

            cCreateOrUpdateItemRequest.changingImage = new int[4];
            for (int i = 0; i < mItemImages.length; i++) {
                if (mItemImages[i] != null && mItemImages[i].isDeleted()) {
                    cCreateOrUpdateItemRequest.changingImage[i] = HonarnamaProto.DELETE;
                } else if (mItemImages[i] != null && mItemImages[i].isChanged() && mItemImages[i].getFinalImageUri() != null) {
                    cCreateOrUpdateItemRequest.changingImage[i] = HonarnamaProto.PUT;
                } else {
                    cCreateOrUpdateItemRequest.changingImage[i] = HonarnamaProto.NOOP;
                }
            }

            cCreateOrUpdateItemRequest.requestProperties = rp;
            if (BuildConfig.DEBUG) {
                logD("CreateOrUpdateItemRequest is: " + cCreateOrUpdateItemRequest);
            }
            CreateOrUpdateItemReply createOrUpdateItemReply;
            try {
                SellServiceGrpc.SellServiceBlockingStub stub = GRPCUtils.getInstance().getSellServiceGrpc();
                if (mItemId > 0) {
                    createOrUpdateItemReply = stub.updateItem(cCreateOrUpdateItemRequest);
                } else {
                    createOrUpdateItemReply = stub.createItem(cCreateOrUpdateItemRequest);
                }
                return createOrUpdateItemReply;
            } catch (Exception e) {
                logE("Error running cCreateOrUpdateItemRequest. cCreateOrUpdateItemRequest: " + cCreateOrUpdateItemRequest + ". Error: " + e, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final CreateOrUpdateItemReply createOrUpdateItemReply) {
            super.onPostExecute(createOrUpdateItemReply);

            if (BuildConfig.DEBUG) {
                logD("createOrUpdateItemReply is: " + createOrUpdateItemReply);
            }

            Activity activity = getActivity();
            if (createOrUpdateItemReply != null) {
                switch (createOrUpdateItemReply.replyProperties.statusCode) {

                    case ReplyProperties.OK:
                        setItemInfo(createOrUpdateItemReply.uptodateItem, false);

                        ArrayList<Task<Void>> tasks = new ArrayList<>();
                        for (int i = 0; i < mItemImages.length; i++) {
                            if (!isAdded()) {
                                break;
                            }
                            if (!TextUtils.isEmpty(createOrUpdateItemReply.imageModificationUrl[i]) && mItemImages[i] != null && mItemImages[i].getFinalImageUri() != null) {
                                final File file = new File(mItemImages[i].getFinalImageUri().getPath());
                                tasks.add(new Uploader(file, createOrUpdateItemReply.imageModificationUrl[i]).upload());
                            }

                            if (mItemImages[i] != null && mItemImages[i].isDeleted()) {
                                mItemImages[i].setDeleted(false);
                            }
                        }

                        Task.whenAll(tasks).continueWith(new Continuation<Void, Object>() {
                            @Override
                            public Object then(Task<Void> task) throws Exception {
                                if (task.isFaulted()) {
                                    cToastMsg = getStringInFragment(R.string.error_sending_images) + getStringInFragment(R.string.check_net_connection);
                                    setDirty(true);
                                } else {
                                    mDirty = false;
                                    cToastMsg = getStringInFragment(R.string.item_saved_successfully);
                                    for (int i = 0; i < mItemImages.length; i++) {
                                        if (mItemImages[i] != null) {
                                            mItemImages[i].setChanged(false);
                                        }
                                    }
                                }
                                dismissProgressDialog();
                                return null;
                            }
                        });
                        break;

                    case ReplyProperties.CLIENT_ERROR:
                        switch (createOrUpdateItemReply.errorCode) {
                            case CreateOrUpdateItemReply.NO_CLIENT_ERROR:
                                logE("Got NO_CLIENT_ERROR code for updating item. cCreateOrUpdateItemRequest: " + cCreateOrUpdateItemRequest + ". User Id: " + HonarnamaUser.getId() + ".");
                                cToastMsg = getStringInFragment(R.string.error_getting_info);
                                break;
                            case CreateOrUpdateItemReply.FORBIDDEN:
                                logE("Got FORBIDDEN reply while trying createOrUpdateItem. cCreateOrUpdateItemRequest: " + cCreateOrUpdateItemRequest + ". User Id: " + HonarnamaUser.getId() + ".");
                                cToastMsg = getStringInFragment(R.string.not_allowed_to_do_this_action);
                                break;
                            case CreateOrUpdateItemReply.ITEM_NOT_FOUND:
                                cToastMsg = getStringInFragment(R.string.item_not_found);
                                break;
                            case CreateOrUpdateItemReply.EMPTY_ITEM:
                                logE("CreateOrUpdateItemReply was EMPTY_ITEM. cCreateOrUpdateItemRequest: " + cCreateOrUpdateItemRequest);
                                cToastMsg = getStringInFragment(R.string.error_getting_info);
                                break;
                            case CreateOrUpdateItemReply.STORE_NOT_CREATED:
                                cToastMsg = getStringInFragment(R.string.store_not_created);
                                break;
                        }
                        dismissProgressDialog();
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        logE("Server error upon cCreateOrUpdateItemRequest. request: " + cCreateOrUpdateItemRequest);
                        cToastMsg = getStringInFragment(R.string.server_error_try_again);
                        dismissProgressDialog();
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        HonarnamaUser.logout(activity);
                        cToastMsg = getStringInFragment(R.string.login_again);
                        dismissProgressDialog();
                        break;

                    case ReplyProperties.UPGRADE_REQUIRED:
                        if (activity != null) {
                            ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) activity);
                            if (controlPanelActivity != null) {
                                controlPanelActivity.displayUpgradeRequiredDialog();
                            }
                        } else {
                            cToastMsg = getStringInFragment(R.string.upgrade_to_new_version);
                        }
                        dismissProgressDialog();
                        break;
                }

            } else {
                cToastMsg = getStringInFragment(R.string.error_connecting_server_try_again);
                dismissProgressDialog();
            }
        }
    }

    public void displayRetrySnackbar() {

        dismissSnackbar();

        Activity activity = getActivity();
        View sbView = null;
        TextView textView = null;

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(" ").append(getStringInFragment(R.string.error_connecting_server_retry)).append(" ");

        if (!isAdded()) {
            return;
        }
        mSnackbar = Snackbar.make(mCoordinatorLayout, builder, Snackbar.LENGTH_INDEFINITE);

        if (mSnackbar != null) {
            sbView = mSnackbar.getView();
        }
        if (sbView != null) {
            sbView.setBackgroundColor(getResources().getColor(R.color.amber));
            textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        }

        if (textView != null) {
            textView.setBackgroundColor(getResources().getColor(R.color.amber));
            textView.setSingleLine(false);
            textView.setGravity(Gravity.CENTER);
            Spannable spannable = (Spannable) textView.getText();
            if (activity != null) {
                spannable.setSpan(new ImageSpan(activity, android.R.drawable.stat_notify_sync), textView.getText().length() - 1, textView.getText().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NetworkManager.getInstance().isNetworkEnabled(true)) {

                        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
                        controlPanelActivity.checkAndUpdateMeta(false);

                        new getItemAsync().execute();
                        dismissSnackbar();
                    }
                }
            });
        }

        if (isAdded() && mSnackbar != null) {
            mSnackbar.show();
        }

    }

    public void dismissSnackbar() {
        if (isAdded() && mSnackbar != null && mSnackbar.isShown()) {
            mSnackbar.dismiss();
        }
    }
}
