package net.honarnama.browse.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.mikepenz.iconics.view.IconicsImageView;
import com.mikepenz.iconics.view.IconicsTextView;
import com.parse.ImageSelector;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.honarnama.GRPCUtils;
import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.model.City;
import net.honarnama.base.model.Province;
import net.honarnama.base.utils.NetworkManager;
import net.honarnama.base.utils.ObservableScrollView;
import net.honarnama.base.utils.TextUtil;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.adapter.ImageAdapter;
import net.honarnama.browse.dialog.ConfirmationDialog;
import net.honarnama.browse.dialog.ContactDialog;
import net.honarnama.browse.model.Bookmark;
import net.honarnama.nano.BrowseItemReply;
import net.honarnama.nano.BrowseItemRequest;
import net.honarnama.nano.BrowseServiceGrpc;
import net.honarnama.nano.Item;
import net.honarnama.nano.ReplyProperties;
import net.honarnama.nano.RequestProperties;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

/**
 * Created by elnaz on 2/15/16.
 */
public class ItemPageFragment extends HonarnamaBrowseFragment implements View.OnClickListener, AdapterView.OnItemClickListener, ObservableScrollView.OnScrollChangedListener {
    public static ShopPageFragment mShopPageFragment;
    private Tracker mTracker;
    public ProgressBar mBannerProgressBar;

    public TextView mNameTextView;
    public TextView mPriceTextView;
    public TextView mDescTextView;
    public TextView mPlaceTextView;
    public TextView mShopNameTextView;
    public ImageSelector mShopLogo;
    private LinearLayout mDotsLayout;
    public LinearLayout mInnerLayout;
    public RelativeLayout mSimilarTitleContainer;

    public IconicsImageView mBookmarkBack;
    public FloatingActionButton mFab;
    public RelativeLayout mDeletedItemMsg;
    public RelativeLayout mInfoContainer;
    public RelativeLayout mSimilarItemsContainer;


    static TextView mDotsText[];
    private int mDotsCount;

    private ObservableScrollView mScrollView;
    private View mBannerFrameLayout;
    private RelativeLayout mShare;
    public IconicsImageView mBookmarkImageView;
    public IconicsImageView mRemoveBoomarkImageView;

    public long mItemId;

    ImageAdapter mImageAdapter;

    public RelativeLayout mShopContainer;

    public Item mItem;
    LayoutParams mLayoutParams;
    LinearLayout mNext, mPrev;
    int mSimilarItemViewWidth;
    GestureDetector mGestureDetector = null;
    HorizontalScrollView mHorizontalScrollView;
    ArrayList<View> mSimilarItemsList = new ArrayList<>();
    int mWidth;
    int mCurrPosition, mPrevPosition;

    public ImageSelector mDefaultImageView;

    public LinearLayout mInfoProgreeBarContainer;

    public RelativeLayout mOnErrorRetry;

    @Override
    public String getTitle(Context context) {
        return "مشاهده محصول";
    }

    public synchronized static ItemPageFragment getInstance(long itemId) {
        ItemPageFragment itemPageFragment = new ItemPageFragment();
        Bundle args = new Bundle();
        args.putLong("itemId", itemId);
        itemPageFragment.setArguments(args);
//        shopPageFragment.setOwner(owner);
        return itemPageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = HonarnamaBrowseApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("ItemPageFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onSelectedTabClick() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_item_page, container, false);
        mItemId = getArguments().getLong("itemId");

        mScrollView = (ObservableScrollView) rootView.findViewById(R.id.fragment_scroll_view);
        mScrollView.setOnScrollChangedListener(this);
        mBannerFrameLayout = rootView.findViewById(R.id.item_banner_frame_layout);
        mDefaultImageView = (ImageSelector) rootView.findViewById(R.id.item_default_image_view);

        mNameTextView = (TextView) rootView.findViewById(R.id.item_name_text_view);
        mPriceTextView = (TextView) rootView.findViewById(R.id.price);
        mDescTextView = (TextView) rootView.findViewById(R.id.item_desc_text_view);
        mPlaceTextView = (TextView) rootView.findViewById(R.id.item_place_text_view);
        mShare = (RelativeLayout) rootView.findViewById(R.id.item_share_container);
        mShare.setOnClickListener(this);
        rootView.findViewById(R.id.share_item_icon).setOnClickListener(this);
        rootView.findViewById(R.id.share_item_text).setOnClickListener(this);

        mBookmarkImageView = (IconicsImageView) rootView.findViewById(R.id.bookmark);
        mBookmarkImageView.setOnClickListener(this);

        mRemoveBoomarkImageView = (IconicsImageView) rootView.findViewById(R.id.remove_bookmark);
        mRemoveBoomarkImageView.setOnClickListener(this);

        mShopContainer = (RelativeLayout) rootView.findViewById(R.id.item_shop_container);
        mShopNameTextView = (TextView) rootView.findViewById(R.id.shop_name_text_view);
        mShopLogo = (ImageSelector) rootView.findViewById(R.id.store_logo_image_view);

        mSimilarTitleContainer = (RelativeLayout) rootView.findViewById(R.id.similar_title_container);
        mImageAdapter = new ImageAdapter(HonarnamaBrowseApp.getInstance());
        mDotsLayout = (LinearLayout) rootView.findViewById(R.id.image_dots_container);
        mInnerLayout = (LinearLayout) rootView.findViewById(R.id.innerLayout);

        mInfoProgreeBarContainer = (LinearLayout) rootView.findViewById(R.id.item_info_progress_bar_container);

        mOnErrorRetry = (RelativeLayout) rootView.findViewById(R.id.on_error_retry_container);
        mOnErrorRetry.setOnClickListener(this);

        mBookmarkBack = (IconicsImageView) rootView.findViewById(R.id.bookmark_back);
        mFab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        mDeletedItemMsg = (RelativeLayout) rootView.findViewById(R.id.deleted_item_msg);
        mInfoContainer = (RelativeLayout) rootView.findViewById(R.id.item_info_container);
        mSimilarItemsContainer = (RelativeLayout) rootView.findViewById(R.id.similar_items_container);

        new getItemAsync().execute();

        //here we create the gallery and set our adapter created before
        Gallery gallery = (Gallery) rootView.findViewById(R.id.gallery);
        gallery.setAdapter(mImageAdapter);

        //when we scroll the images we have to set the dot that corresponds to the image to White and the others
        //will be Gray
        gallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView adapterView, View view, int pos, long l) {
                if (mDotsText != null) {
                    for (int i = 0; i < mDotsCount; i++) {
                        if (mDotsText[i] != null) {
                            mDotsText[i].setTextSize(8);
                        }
                    }
                    if (mDotsText[pos] != null) {
                        mDotsText[pos].setTextSize(12);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView adapterView) {

            }
        });

        mPrev = (LinearLayout) rootView.findViewById(R.id.prev);
        mNext = (LinearLayout) rootView.findViewById(R.id.next);

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        mHorizontalScrollView.smoothScrollTo(
                                (int) mHorizontalScrollView.getScrollX()
                                        + mSimilarItemViewWidth,
                                (int) mHorizontalScrollView.getScrollY());
                    }
                }, 100L);
            }
        });
        mPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        mHorizontalScrollView.smoothScrollTo(
                                (int) mHorizontalScrollView.getScrollX()
                                        - mSimilarItemViewWidth,
                                (int) mHorizontalScrollView.getScrollY());
                    }
                }, 100L);
            }
        });

        mHorizontalScrollView = (HorizontalScrollView) rootView.findViewById(R.id.similar_items_hsv);
        mGestureDetector = new GestureDetector(new MyGestureDetector());


        Display display = getActivity().getWindowManager().getDefaultDisplay();
        mWidth = display.getWidth(); // deprecated
        mSimilarItemViewWidth = mWidth / 3;


        mHorizontalScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mGestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        });

        return rootView;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.item_share_container || v.getId() == R.id.share_item_icon || v.getId() == R.id.share_item_text) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, mNameTextView.getText().toString());
            sendIntent.putExtra(Intent.EXTRA_TEXT, "سلام،" + "\n" + "این محصول هنری تو برنامه‌ی هنرنما رو می‌خواستم بهت پیشنهاد بدم." + "\n" + mNameTextView.getText() +
                    "\n" + HonarnamaBaseApp.WEB_ADDRESS + "/item/" + mItemId);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }

        if (v.getId() == R.id.bookmark) {
            try {
                Bookmark.bookmarkItem(mItem);
                displayShortToast(getStringInFragment(R.string.item_got_bookmarked));
                mBookmarkImageView.setVisibility(View.GONE);
                mRemoveBoomarkImageView.setVisibility(View.VISIBLE);
            } catch (SQLException sqlEx) {
                displayShortToast(getStringInFragment(R.string.error_bookmarking_item));
            }
        }

        if (v.getId() == R.id.remove_bookmark) {
            final ConfirmationDialog confirmationDialog = new ConfirmationDialog(getActivity(),
                    getStringInFragment(R.string.remove_bookmark_dialog_title),
                    getStringInFragment(R.string.remove_bookmark_dialog_msg)
            );
            confirmationDialog.showDialog(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Bookmark.removeBookmark(mItem.id);
                        displayShortToast("نشان محصول حذف شد.");
                        mBookmarkImageView.setVisibility(View.VISIBLE);
                        mRemoveBoomarkImageView.setVisibility(View.GONE);
                    } catch (Exception ex) {
                        logE("Error removing bookmar. ex: ", ex);
                        displayShortToast("خطا در حذف نشان محصول.");
                    }
                    confirmationDialog.dismiss();
                }
            });
        }

        if (v.getId() == R.id.on_error_retry_container) {
            if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                return;
            }
            ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
            controlPanelActivity.refreshTopFragment();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        int scrollY = mScrollView.getScrollY();
        // Add parallax effect
        mBannerFrameLayout.setTranslationY(scrollY * 0.5f);

    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            if (e1.getX() < e2.getX()) {
                mCurrPosition = getVisibleViews("left");
            } else {
                mCurrPosition = getVisibleViews("right");
            }

            mHorizontalScrollView.smoothScrollTo(mSimilarItemsList.get(mCurrPosition)
                    .getLeft(), 0);
            return true;
        }

    }

    public int getVisibleViews(String direction) {
        Rect hitRect = new Rect();
        int position = 0;
        int rightCounter = 0;
        for (int i = 0; i < mSimilarItemsList.size(); i++) {
            if (mSimilarItemsList.get(i).getLocalVisibleRect(hitRect)) {
                if (direction.equals("left")) {
                    position = i;
                    break;
                } else if (direction.equals("right")) {
                    rightCounter++;
                    position = i;
                    if (rightCounter == 2)
                        break;
                }
            }
        }
        return position;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    public void addSimilarItems(net.honarnama.nano.Item[] items) {

        for (int i = 0; i < items.length; i++) {
            final Item item = items[i];

            View similarItemLayout = getActivity().getLayoutInflater().inflate(R.layout.similar_item_layout, null);

            TextView similarItemTitle = (TextView) similarItemLayout.findViewById(R.id.item_title);
            ImageSelector similarItemImage = (ImageSelector) similarItemLayout.findViewById(R.id.item_image);
            TextView similarPostPrice = (TextView) similarItemLayout.findViewById(R.id.similar_post_price);

            similarItemImage.setSource(item.images[0], null);
            similarItemTitle.setText(TextUtil.convertEnNumberToFa(item.name));

            NumberFormat formatter = TextUtil.getPriceNumberFormmat(Locale.ENGLISH);
            String formattedPrice = formatter.format(item.price);
            String price = TextUtil.convertEnNumberToFa(formattedPrice);

            similarPostPrice.setText(price + " " + getString(R.string.toman));

            mLayoutParams = new LayoutParams(mSimilarItemViewWidth, LayoutParams.WRAP_CONTENT);
            if ((i % 3) == 0) {
            }
            similarItemLayout.setLayoutParams(mLayoutParams);
            similarItemLayout.requestLayout();

            final ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
            similarItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controlPanelActivity.displayItemPage(item.id, false);
                }
            });

            mSimilarItemsList.add(similarItemLayout);
            mInnerLayout.addView(similarItemLayout);
        }


        mHorizontalScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mHorizontalScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        }, 10);

        if (mSimilarItemsList.size() > 3) {
            mPrev.setVisibility(View.VISIBLE);
            mNext.setVisibility(View.VISIBLE);
        }
    }


    public class getItemAsync extends AsyncTask<Void, Void, BrowseItemReply> {
        BrowseItemRequest browseItemRequest;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (isAdded()) {
                mSimilarItemsContainer.setVisibility(View.GONE);
                mDefaultImageView.setVisibility(View.VISIBLE);
                mInfoProgreeBarContainer.setVisibility(View.VISIBLE);
                mOnErrorRetry.setVisibility(View.GONE);
            }
        }

        @Override
        protected BrowseItemReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            browseItemRequest = new BrowseItemRequest();
            browseItemRequest.requestProperties = rp;

            browseItemRequest.id = mItemId;

            BrowseItemReply getItemReply;
            if (BuildConfig.DEBUG) {
                logD("Request for getting single item is: " + browseItemRequest);
            }
            try {
                BrowseServiceGrpc.BrowseServiceBlockingStub stub = GRPCUtils.getInstance().getBrowseServiceGrpc();
                getItemReply = stub.getItem(browseItemRequest);
                return getItemReply;
            } catch (Exception e) {
                logE("Error running getItem request. request: " + browseItemRequest + ". Error: " + e, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(BrowseItemReply browseItemReply) {
            super.onPostExecute(browseItemReply);

            mInfoProgreeBarContainer.setVisibility(View.GONE);

            Activity activity = getActivity();

            if (browseItemReply != null) {
                switch (browseItemReply.replyProperties.statusCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:
                        if (activity != null) {
                            ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) activity);
                            controlPanelActivity.displayUpgradeRequiredDialog();
                        } else {
                            displayLongToast(getStringInFragment(R.string.upgrade_to_new_version));
                        }
                        break;
                    case ReplyProperties.CLIENT_ERROR:
                        if (browseItemReply.errorCode == BrowseItemReply.ITEM_NOT_FOUND) {
                            if (isVisible()) {
                                displayLongToast(getStringInFragment(R.string.error_item_no_longer_exists));
                            }
                            mDeletedItemMsg.setVisibility(View.VISIBLE);
                        }
                        break;
                    case ReplyProperties.SERVER_ERROR:
                        if (isAdded()) {
                            mOnErrorRetry.setVisibility(View.VISIBLE);
                            displayLongToast(getStringInFragment(R.string.server_error_try_again));
                        }
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        break;

                    case ReplyProperties.OK:
                        if (isAdded()) {
                            net.honarnama.nano.Item item = browseItemReply.item;
                            net.honarnama.nano.Store store = browseItemReply.store;
                            net.honarnama.nano.Item[] similarItems = browseItemReply.similarItems;
                            loadItemInfo(item, store, similarItems);
                        }
                        break;
                }

            } else {
                if (isAdded()) {
                    mOnErrorRetry.setVisibility(View.VISIBLE);
                }

            }
        }

    }

    private void loadItemInfo(net.honarnama.nano.Item item, final net.honarnama.nano.Store store, net.honarnama.nano.Item[] similarItems) {

        mFab.setVisibility(View.VISIBLE);
        mDefaultImageView.setVisibility(View.GONE);
        mInfoContainer.setVisibility(View.VISIBLE);
        mOnErrorRetry.setVisibility(View.GONE);
        mShare.setVisibility(View.VISIBLE);
        mItem = item;

        boolean isBookmarked = new Bookmark().isBookmarkedAlready(mItem.id);

        if (isBookmarked) {
            mBookmarkImageView.setVisibility(View.GONE);
            mRemoveBoomarkImageView.setVisibility(View.VISIBLE);
        } else {
            mBookmarkImageView.setVisibility(View.VISIBLE);
            mRemoveBoomarkImageView.setVisibility(View.GONE);
        }
        mBookmarkBack.setVisibility(View.VISIBLE);

        mNameTextView.setText(TextUtil.convertEnNumberToFa(mItem.name));
        NumberFormat formatter = TextUtil.getPriceNumberFormmat(Locale.ENGLISH);
        String formattedPrice = formatter.format(mItem.price);
        String price = TextUtil.convertEnNumberToFa(formattedPrice);
        mPriceTextView.setText(price + " ");

        mDescTextView.setText(TextUtil.convertEnNumberToFa(mItem.description));

        String provinceName = Province.getProvinceById(store.locationCriteria.provinceId).getName();
        String cityName = City.getCityById(store.locationCriteria.cityId).getName();

        mPlaceTextView.setText(provinceName + "،" + " " + cityName);
        mShopNameTextView.append(store.name);

        mShopContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
                controlPanelActivity.displayShopPage(store.id, false);
            }
        });

        Picasso.with(mContext).load(R.drawable.default_logo_hand)
                .error(R.drawable.camera_insta)
                .into(mShopLogo, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                    }
                });

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContactDialog contactDialog = new ContactDialog();
                contactDialog.showDialog(getActivity(), store.publicPhoneNumber, store.publicCellNumber,
                        getResources().getString(R.string.item_contact_dialog_warning_msg));

            }
        });

        String[] images = mItem.images;
        List<String> nonNullImages = new ArrayList<>();
        for (int i = 0; i < net.honarnama.base.model.Item.NUMBER_OF_IMAGES; i++) {
            if (images[i].trim().length() > 0) {
                nonNullImages.add(images[i]);
            }
        }
        mImageAdapter.setImages(nonNullImages);
        mImageAdapter.notifyDataSetChanged();

        mDotsCount = mImageAdapter.getCount();
        if (mDotsCount > 1) {
            mDotsText = new IconicsTextView[mDotsCount];
            for (int i = 0; i < mDotsCount; i++) {
                mDotsText[i] = new IconicsTextView(HonarnamaBrowseApp.getInstance());
                mDotsText[i].setText("{gmd-brightness-1}");
                mDotsText[i].setTextSize(8);
                if (i == 0) {
                    mDotsText[i].setPadding(0, 10, 0, 0);
                } else {
                    mDotsText[i].setPadding(0, 10, 10, 0);
                }
                mDotsText[i].setTypeface(null, Typeface.BOLD);
                mDotsText[i].setTextColor(getResources().getColor(R.color.amber_primary_dark));
                mDotsLayout.addView(mDotsText[i]);
            }
        }

        if (similarItems.length == 0) {
            mSimilarItemsContainer.setVisibility(View.GONE);
        } else {
            mSimilarItemsContainer.setVisibility(View.VISIBLE);
            addSimilarItems(similarItems);
        }
    }

}


