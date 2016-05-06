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
import net.honarnama.core.utils.WindowUtil;
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

    private ImageSelector[] mItemImages;

    private long mItemId;
    private boolean mDirty = false;
    private boolean mCreateNew = false;
    private int mCategoryId = -1;
    private int mCategoryParentId = -1;
    private String mCategoryName;
    private Tracker mTracker;
    Snackbar mSnackbar;

    TextWatcher mTextWatcherToMarkDirty;
    CreateOrUpdateItemAsync mCreateOrUpdateItemAsync;

    public synchronized static EditItemFragment getInstance() {
        if (mEditItemFragment == null) {
            mEditItemFragment = new EditItemFragment();
        }
        return mEditItemFragment;
    }

    public void reset(boolean createNew) {

        if (mTitleEditText != null) {
            mTitleEditText.setText("");
            mDescriptionEditText.setText("");
            mPriceEditText.setText("");
            mChooseCategoryButton.setText(getStringInFragment(R.string.select));
            for (ImageSelector imageSelector : mItemImages) {
                if (imageSelector != null) {
                    imageSelector.removeSelectedImage();
                    imageSelector.setChanged(false);
                    imageSelector.setDeleted(false);
                }
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

        if (mCreateNew && isAdded() && mMainContent != null) {
            mMainContent.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }

        if (isAdded() && mSnackbar != null && mSnackbar.isShown()) {
            mSnackbar.dismiss();
        }
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
    public String getTitle(Context context) {
        if (mItemId > 0) {
            return getStringInFragment(R.string.nav_title_edit_item);
        } else {
            return getStringInFragment(R.string.register_new_item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()) {
            mPriceEditText.addTextChangedListener(new GravityTextWatcher(mPriceEditText));
            if (mCreateNew) {
                mMainContent.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.GONE);
                if (mSnackbar != null && mSnackbar.isShown()) {
                    mSnackbar.dismiss();
                }
            }
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
            if (activity != null) {
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
                if (isAdded()) {
                    mTitleEditText.setText(savedInstanceState.getString(SAVE_INSTANCE_STATE_KEY_TITLE));
                    mDescriptionEditText.setText(savedInstanceState.getString(SAVE_INSTANCE_STATE_KEY_DESCRIPTION));
                    mChooseCategoryButton.setText(savedInstanceState.getString(SAVE_INSTANCE_STATE_KEY_CATEGORY_NAME));
                    mPriceEditText.setText(savedInstanceState.getString(SAVE_INSTANCE_STATE_KEY_PRICE));
                }
                mCategoryId = savedInstanceState.getInt(SAVE_INSTANCE_STATE_KEY_CATEGORY_ID);
                mCategoryParentId = savedInstanceState.getInt(SAVE_INSTANCE_STATE_KEY_CATEGORY_PARENT_ID);
            } else {
                if (mItemId >= 0) {
                    new getItemAsync().execute();
                }
            }
        }

        rootView.findViewById(R.id.saveItemButton).setOnClickListener(this);

        return rootView;
    }


    @Override
    public void onClick(View view) {
        Activity activity = getActivity();
        switch (view.getId()) {
            case R.id.saveItemButton:
                if (activity != null) {
                    WindowUtil.hideKeyboard(activity);
                }
                if (formInputsAreValid()) {
                    if (NetworkManager.getInstance().isNetworkEnabled(true)) {
                        mCreateOrUpdateItemAsync = new CreateOrUpdateItemAsync();
                        mCreateOrUpdateItemAsync.execute();
                    }
                }
                break;
            case R.id.choose_art_category_btn:
                if (activity != null) {
                    Intent intent = new Intent(activity, ChooseArtCategoryActivity.class);
                    intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_INTENT_CALLER, HonarnamaBaseApp.PREF_NAME_SELL_APP);
                    startActivityForResult(intent, HonarnamaSellApp.INTENT_CHOOSE_CATEGORY_CODE);
                }
                break;
            default:
                break;
        }
    }

    private boolean formInputsAreValid() {

        if (!isAdded()) {
            return false;
        }

        if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
            return false;
        }

        final String title = mTitleEditText.getText().toString();
        final String price = TextUtil.normalizePrice(mPriceEditText.getText().toString());
        final String description = mDescriptionEditText.getText().toString();

        mImagesTitleTextView.setError(null);
        mTitleEditText.setError(null);
        mPriceEditText.setError(null);
        mCategoryTextView.setError(null);
        mDescriptionEditText.setError(null);

        boolean noImage = true;
        for (ImageSelector imageSelector : mItemImages) {
            if ((imageSelector.getFinalImageUri() != null) || (imageSelector.isFileSet() && !imageSelector.isDeleted())) {
                noImage = false;
                break;
            }
        }
        if (noImage) {
            mImagesTitleTextView.requestFocus();
            mImagesTitleTextView.setError(getStringInFragment(R.string.error_edit_item_no_image));
            mScrollView.fullScroll(ScrollView.FOCUS_UP);
            return false;
        }

        if (title.trim().length() == 0) {
            mTitleEditText.requestFocus();
            mTitleEditText.setError(getStringInFragment(R.string.error_edit_item_title_is_empty));
            return false;
        }

        if (price.trim().length() == 0) {
            mPriceEditText.requestFocus();
            mPriceEditText.setError(getStringInFragment(R.string.error_item_price_not_set));
            return false;
        }

        if (Integer.valueOf(price.trim()) < 100) {
            mPriceEditText.requestFocus();
            mPriceEditText.setError(getStringInFragment(R.string.error_item_price_is_low));
            return false;
        }


        if (mCategoryId < 0) {
            mCategoryTextView.requestFocus();
            mCategoryTextView.setError(getStringInFragment(R.string.error_category_is_not_selected));
            return false;
        }

        if (description.trim().length() == 0) {
            mDescriptionEditText.requestFocus();
            mDescriptionEditText.setError(getStringInFragment(R.string.error_edit_item_description_is_empty));
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
                    if (isAdded()) {
                        mCategoryTextView.setError(null);
                        mChooseCategoryButton.setText(mCategoryName);
                    }
                    mCategoryName = data.getStringExtra(HonarnamaBaseApp.EXTRA_KEY_CATEGORY_NAME);
                    mCategoryId = selectedCatId;
                    mCategoryParentId = selectedCatParentId;
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
        if (mDirty && isAdded()) {
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
            if (isAdded()) {
                mMainContent.setVisibility(View.GONE);
                mEmptyView.setText(getStringInFragment(R.string.getting_information));
                mEmptyView.setVisibility(View.VISIBLE);
                displayProgressDialog(null);
            }
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
                        }
                        break;
                    case ReplyProperties.CLIENT_ERROR:
                        switch (getItemReply.errorCode) {

                            case GetItemReply.NO_CLIENT_ERROR:
                                logE("Got NO_CLIENT_ERROR code for getting item in EditItemFragment. getOrDeleteItemRequest: " + getOrDeleteItemRequest + ". User Id: " + HonarnamaUser.getId() + ".");
                                displayShortToast(getStringInFragment(R.string.error_occured));
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
                        if (isAdded()) {
                            displaySnackbar();
                            displayShortToast(getStringInFragment(R.string.server_error_try_again));
                        }
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        HonarnamaUser.logout(activity);
                        if (activity == null) {
                            displayLongToast(getStringInFragment(R.string.login_again));
                        }
                        break;

                    case ReplyProperties.OK:
                        if (getItemReply.item != null) {
                            if (isAdded()) {
                                mEmptyView.setVisibility(View.GONE);
                                mMainContent.setVisibility(View.VISIBLE);
                            }
                            setItemInfo(getItemReply.item, true);
                        } else {
                            if (isAdded()) {
                                displayShortToast(getStringInFragment(R.string.error_getting_event_info));
                                displaySnackbar();
                                logE("Got OK code for getting item (id " + mItemId + "), but item was null. getOrDeleteItemRequest: " + getOrDeleteItemRequest);
                            }
                        }
                        break;
                }

            } else {
                if (isAdded()) {
                    mEmptyView.setText(getStringInFragment(R.string.error_getting_item_info));
                    displaySnackbar();
                    displayShortToast(getStringInFragment(R.string.check_net_connection));
                }
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

            if (item.artCategoryId.level2Id > 0) {
                mCategoryParentId = item.artCategoryId.level1Id;
                mCategoryId = item.artCategoryId.level2Id;
            } else {
                mCategoryParentId = 0;
                mCategoryId = item.artCategoryId.level1Id;
            }


            mTitleEditText.setText(item.name);
            mDescriptionEditText.setText(item.description);
            mPriceEditText.setText(item.price + "");
            mChooseCategoryButton.setText(getStringInFragment(R.string.getting_information));
            new ArtCategory().getCategoryNameById(mCategoryId).continueWith(new Continuation<String, Object>() {
                @Override
                public Object then(Task<String> task) throws Exception {
                    if (task.isFaulted()) {
                        displayLongToast(getStringInFragment(R.string.error_finding_category_name) + getStringInFragment(R.string.check_net_connection));
                    } else {
                        mChooseCategoryButton.setText(task.getResult());
                    }
                    return null;
                }
            });

            if (loadImages && activity != null) {
                for (int i = 0; i < 4; i++) {
                    if (!TextUtils.isEmpty(item.images[i])) {

                        String itemImage = item.images[i];

                        mItemImageLoadingPannel[i].setVisibility(View.VISIBLE);
                        mItemImages[i].setVisibility(View.GONE);

                        final int index = i;

                        Picasso.with(activity).load(itemImage)
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
                                        mItemImageLoadingPannel[index].setVisibility(View.GONE);
                                        displayShortToast(getStringInFragment(R.string.error_displaying_image) + getStringInFragment(R.string.check_net_connection));
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
        CreateOrUpdateItemRequest cCreateOrUpdateItemRequest;
        String cToastMsg = "";

        String title = "";
        String description = "";
        long price = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (isAdded()) {
                title = mTitleEditText.getText().toString().trim();
                description = mDescriptionEditText.getText().toString().trim();
                price = Integer.valueOf(TextUtil.normalizePrice(TextUtil.convertFaNumberToEn(mPriceEditText.getText().toString().trim())));
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
            cCreateOrUpdateItemRequest.item.artCategoryId = new ArtCategoryId();
            if (mCategoryParentId == 0) {
                cCreateOrUpdateItemRequest.item.artCategoryId.level1Id = mCategoryId;
            } else {
                cCreateOrUpdateItemRequest.item.artCategoryId.level1Id = mCategoryParentId;
                cCreateOrUpdateItemRequest.item.artCategoryId.level2Id = mCategoryId;
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
            logD("cCreateOrUpdateItemRequest is: " + cCreateOrUpdateItemRequest);
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
            logD("createOrUpdateItemReply is: " + createOrUpdateItemReply);

            Activity activity = getActivity();
            if (createOrUpdateItemReply != null) {
                switch (createOrUpdateItemReply.replyProperties.statusCode) {

                    case ReplyProperties.OK:
                        setItemInfo(createOrUpdateItemReply.uptodateItem, false);

                        ArrayList<Task<Void>> tasks = new ArrayList<>();
                        for (int i = 0; i < mItemImages.length; i++) {
                            if (!TextUtils.isEmpty(createOrUpdateItemReply.imageModificationUrl[i]) && mItemImages[i].getFinalImageUri() != null) {
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
                                cToastMsg = getStringInFragment(R.string.error_occured);
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
                                cToastMsg = getStringInFragment(R.string.error_occured);
                                break;
                            case CreateOrUpdateItemReply.STORE_NOT_CREATED:
                                cToastMsg = getStringInFragment(R.string.store_not_created);
                                break;
                        }
                        dismissProgressDialog();
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        cToastMsg = getStringInFragment(R.string.server_error_try_again);
                        dismissProgressDialog();
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        HonarnamaUser.logout(activity);
                        if (activity == null) {
                            cToastMsg = getStringInFragment(R.string.login_again);
                        }
                        dismissProgressDialog();
                        break;

                    case ReplyProperties.UPGRADE_REQUIRED:
                        dismissProgressDialog();

                        if (activity != null) {
                            ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) activity);
                            if (controlPanelActivity != null) {
                                controlPanelActivity.displayUpgradeRequiredDialog();
                            }
                        }

                        break;
                }

            } else {
                cToastMsg = getStringInFragment(R.string.error_connecting_to_Server) + getStringInFragment(R.string.check_net_connection);
                dismissProgressDialog();
            }
        }
    }

    public void displaySnackbar() {
        if (!isAdded()) {
            return;
        }
        if (mSnackbar != null && mSnackbar.isShown()) {
            mSnackbar.dismiss();
        }

        Activity activity = getActivity();

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(" ").append(getStringInFragment(R.string.error_connecting_to_Server)).append(" ");

        mSnackbar = Snackbar.make(mCoordinatorLayout, builder, Snackbar.LENGTH_INDEFINITE);
        View sbView = mSnackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setBackgroundColor(getResources().getColor(R.color.amber));
        textView.setSingleLine(false);
        textView.setGravity(Gravity.CENTER);
        Spannable spannable = (Spannable) textView.getText();

        if (activity != null) {
            spannable.setSpan(new ImageSpan(activity, android.R.drawable.stat_notify_sync), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
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
