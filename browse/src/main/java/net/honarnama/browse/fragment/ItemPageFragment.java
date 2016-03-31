package net.honarnama.browse.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.mikepenz.iconics.view.IconicsImageView;
import com.mikepenz.iconics.view.IconicsTextView;
import com.parse.GetDataCallback;
import com.parse.ImageSelector;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.adapter.ImageAdapter;
import net.honarnama.browse.dialog.ConfirmationDialog;
import net.honarnama.browse.dialog.ContactDialog;
import net.honarnama.browse.model.Item;
import net.honarnama.core.model.Bookmark;
import net.honarnama.core.model.City;
import net.honarnama.core.model.Province;
import net.honarnama.core.model.Store;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.core.utils.ObservableScrollView;
import net.honarnama.core.utils.TextUtil;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.Display;
import android.view.LayoutInflater;
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
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by elnaz on 2/15/16.
 */
public class ItemPageFragment extends HonarnamaBrowseFragment implements View.OnClickListener, AdapterView.OnItemClickListener, ObservableScrollView.OnScrollChangedListener {
    public static ShopPageFragment mShopPageFragment;
    private Tracker mTracker;
//    public ProgressBar mBannerProgressBar;

    public TextView mNameTextView;
    public TextView mPriceTextView;
    public TextView mDescTextView;
    public TextView mPlaceTextView;
    public TextView mShopNameTextView;
    public ImageSelector mShopLogo;
    private ParseUser mOwner;
    private LinearLayout mDotsLayout;
    public LinearLayout mInnerLayout;
    public RelativeLayout mSimilarTitleContainer;


    static TextView mDotsText[];
    private int mDotsCount;

    private ObservableScrollView mScrollView;
    private View mBannerFrameLayout;
    private RelativeLayout mShare;
    public IconicsImageView mBookmarkImageView;
    public IconicsImageView mRemoveBoomarkImageView;

    public String mItemId;

    ImageAdapter mImageAdapter;

    public RelativeLayout mShopContainer;
    public Store mShop;

    public Item mItem;
    LayoutParams mLayoutParams;
    LinearLayout mNext, mPrev;
    int mSimilarItemViewWidth;
    //    GestureDetector mGestureDetector = null;
    HorizontalScrollView mHorizontalScrollView;
    ArrayList<View> mSimilarItemsList = new ArrayList<>();
    int mWidth;
    int mCurrPosition, mPrevPosition;

    public ImageSelector mDefaultImageView;

    public LinearLayout mInfoProgreeBarContainer;
    public ProgressBar mSimilarItemsProgressBar;

    public RelativeLayout mOnErrorRetry;

    @Override
    public String getTitle(Context context) {
        return "مشاهده محصول";
    }

    public synchronized static ItemPageFragment getInstance(String itemId) {
        ItemPageFragment itemPageFragment = new ItemPageFragment();
        Bundle args = new Bundle();
        args.putString("itemId", itemId);
        itemPageFragment.setArguments(args);
//        shopPageFragment.setOwner(owner);
        return itemPageFragment;
    }

    private void setOwner(ParseUser owner) {
        mOwner = owner;
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
        mItemId = getArguments().getString("itemId");

        mScrollView = (ObservableScrollView) rootView.findViewById(R.id.fragment_scroll_view);
        mScrollView.setOnScrollChangedListener(this);
        mBannerFrameLayout = rootView.findViewById(R.id.item_banner_frame_layout);
        mDefaultImageView = (ImageSelector) rootView.findViewById(R.id.item_default_image_view);

//        mBannerProgressBar = (ProgressBar) rootView.findViewById(R.id.banner_progress_bar);

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
        mShopContainer.setOnClickListener(this);
        mShopNameTextView = (TextView) rootView.findViewById(R.id.shop_name_text_view);
        mShopLogo = (ImageSelector) rootView.findViewById(R.id.store_logo_image_view);

        mSimilarTitleContainer = (RelativeLayout) rootView.findViewById(R.id.similar_title_container);
        mImageAdapter = new ImageAdapter(HonarnamaBrowseApp.getInstance());
        mDotsLayout = (LinearLayout) rootView.findViewById(R.id.image_dots_container);
        mInnerLayout = (LinearLayout) rootView.findViewById(R.id.innerLayout);

        mInfoProgreeBarContainer = (LinearLayout) rootView.findViewById(R.id.item_info_progress_bar_container);
        mSimilarItemsProgressBar = (ProgressBar) rootView.findViewById(R.id.similar_items_progress_bar);

        mOnErrorRetry = (RelativeLayout) rootView.findViewById(R.id.on_error_retry_container);
        mOnErrorRetry.setOnClickListener(this);

        final IconicsImageView bookmarkBack = (IconicsImageView) rootView.findViewById(R.id.bookmark_back);
        final FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        final RelativeLayout deletedItemMsg = (RelativeLayout) rootView.findViewById(R.id.deleted_item_msg);
        final RelativeLayout infoContainer = (RelativeLayout) rootView.findViewById(R.id.item_info_container);
        final RelativeLayout similarItemsContainer = (RelativeLayout) rootView.findViewById(R.id.similar_items_container);

        similarItemsContainer.setVisibility(View.GONE);
        mSimilarItemsProgressBar.setVisibility(View.VISIBLE);
        mDefaultImageView.setVisibility(View.VISIBLE);
        mInfoProgreeBarContainer.setVisibility(View.VISIBLE);
        mOnErrorRetry.setVisibility(View.GONE);
        Item.getItemById(mItemId).continueWith(new Continuation<ParseObject, Object>() {
            @Override
            public Object then(Task<ParseObject> task) throws Exception {
                mInfoProgreeBarContainer.setVisibility(View.GONE);
                if (task.isFaulted()) {
                    if (((ParseException) task.getError()).getCode() == ParseException.OBJECT_NOT_FOUND) {
                        if (isVisible()) {
                            Toast.makeText(getActivity(), getActivity().getString(R.string.error_item_no_longer_exists), Toast.LENGTH_SHORT).show();
                        }
                        deletedItemMsg.setVisibility(View.VISIBLE);

                    } else {
                        logE("Getting item with id " + mItemId + " for item page failed. Error: " + task.getError(), "", task.getError());
                        mOnErrorRetry.setVisibility(View.VISIBLE);
                    }
                    return null;
                } else {
                    fab.setVisibility(View.VISIBLE);
                    mDefaultImageView.setVisibility(View.GONE);
                    infoContainer.setVisibility(View.VISIBLE);
                    mOnErrorRetry.setVisibility(View.GONE);
                    mShare.setVisibility(View.VISIBLE);
                    mItem = (Item) task.getResult();

                    Bookmark.isBookmarkedAlready(mItem).continueWith(new Continuation<Boolean, Object>() {
                        @Override
                        public Object then(Task<Boolean> task) throws Exception {
                            if (task.isFaulted()) {

                            } else {
                                boolean isBookmarked = task.getResult();
                                if (isBookmarked) {
                                    mBookmarkImageView.setVisibility(View.GONE);
                                    mRemoveBoomarkImageView.setVisibility(View.VISIBLE);
                                } else {
                                    mBookmarkImageView.setVisibility(View.VISIBLE);
                                    mRemoveBoomarkImageView.setVisibility(View.GONE);
                                }
                                bookmarkBack.setVisibility(View.VISIBLE);
                            }
                            return null;
                        }
                    });

                    mNameTextView.setText(TextUtil.convertEnNumberToFa(mItem.getName()));
                    NumberFormat formatter = TextUtil.getPriceNumberFormmat(Locale.ENGLISH);
                    String formattedPrice = formatter.format(mItem.getPrice());
                    String price = TextUtil.convertEnNumberToFa(formattedPrice);
                    mPriceTextView.setText(price + " ");

                    mDescTextView.setText(TextUtil.convertEnNumberToFa(mItem.getDescription()));

                    mShop = mItem.getStore();
                    mPlaceTextView.setText(mShop.getProvince().getString(Province.NAME) + "، " + mShop.getCity().getString(City.NAME));
                    mShopNameTextView.append(TextUtil.convertEnNumberToFa(mShop.getName()));
                    mShopLogo.loadInBackground(mShop.getLogo(), new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException e) {
                        }
                    });

                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ContactDialog contactDialog = new ContactDialog();
                            contactDialog.showDialog(getActivity(), mShop.getPhoneNumber(), mShop.getCellNumber(),
                                    getResources().getString(R.string.item_contact_dialog_warning_msg));

                        }
                    });

//                    mBannerProgressBar.setVisibility(View.VISIBLE);

                    ParseFile[] images = mItem.getImages();
                    List<ParseFile> nonNullImages = new ArrayList<ParseFile>();
                    for (int i = 0; i < net.honarnama.core.model.Item.NUMBER_OF_IMAGES; i++) {
                        if (images[i] != null) {
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
                    similarItemsContainer.setVisibility(View.VISIBLE);
                    Item.getSimilarItemsByCategory(mItem.getCategory(), mItemId).continueWith(new Continuation<List<Item>, Object>() {
                        @Override
                        public Object then(Task<List<Item>> task) throws Exception {
                            mSimilarItemsProgressBar.setVisibility(View.GONE);
                            if (task.isFaulted()) {
                                logE("Finding similar items failed. " + task.getError());
                                similarItemsContainer.setVisibility(View.GONE);
                            } else {
                                List<Item> similarItems = task.getResult();
                                if (similarItems.size() > 0) {
//                                    mSimilarTitleContainer.setVisibility(View.VISIBLE);
                                    addSimilarItems(similarItems);
                                } else {
                                    similarItemsContainer.setVisibility(View.GONE);
                                }
                            }
                            return null;
                        }
                    });
                }

                return null;
            }
        });

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
//        mGestureDetector = new GestureDetector(new MyGestureDetector());


        Display display = getActivity().getWindowManager().getDefaultDisplay();
        mWidth = display.getWidth(); // deprecated
        mSimilarItemViewWidth = mWidth / 3;


//        mHorizontalScrollView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (mGestureDetector.onTouchEvent(event)) {
//                    return true;
//                }
//                return false;
//            }
//        });


        return rootView;

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.item_share_container || v.getId() == R.id.share_item_icon || v.getId() == R.id.share_item_text) {

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, mNameTextView.getText().toString());
            sendIntent.putExtra(Intent.EXTRA_TEXT, "سلام،" + "\n" + "این محصول هنری تو برنامه‌ی هنرنما رو می‌خواستم بهت پیشنهاد بدم." + "\n" + mNameTextView.getText() +
                    "\n" + "http://www.honarnama.net/item/" + mItemId);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }

        if (v.getId() == R.id.bookmark) {
            Bookmark.bookmarkItem(mItem).continueWith(new Continuation<Boolean, Object>() {
                @Override
                public Object then(Task<Boolean> task) throws Exception {
                    if (task.isFaulted()) {

                    } else {

                        if (task.getResult() == true) {
                            if (isVisible()) {
                                Toast.makeText(getActivity(), "محصول نشان شد.", Toast.LENGTH_SHORT).show();
                            }
                            mBookmarkImageView.setVisibility(View.GONE);
                            mRemoveBoomarkImageView.setVisibility(View.VISIBLE);
                        }
                    }
                    return null;
                }
            });
        }

        if (v.getId() == R.id.remove_bookmark) {
            final ConfirmationDialog confirmationDialog = new ConfirmationDialog(getActivity());
            confirmationDialog.showDialog(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bookmark.removeBookmark(mItem).continueWith(new Continuation<Boolean, Object>() {
                        @Override
                        public Object then(Task<Boolean> task) throws Exception {
                            confirmationDialog.dismiss();
                            if (task.isFaulted()) {

                            } else {
                                if (task.getResult() == true) {
                                    if (isVisible()) {
                                        Toast.makeText(getActivity(), "نشان محصول حذف شد.", Toast.LENGTH_SHORT).show();
                                    }
                                    mBookmarkImageView.setVisibility(View.VISIBLE);
                                    mRemoveBoomarkImageView.setVisibility(View.GONE);
                                }
                            }
                            return null;
                        }
                    });
                }
            });

        }

        if (v.getId() == R.id.item_shop_container) {
            ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
            controlPanelActivity.displayShopPage(mShop.getObjectId(), false);
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

//    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
//        @Override
//        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
//                               float velocityY) {
//            if (e1.getX() < e2.getX()) {
//                mCurrPosition = getVisibleViews("left");
//            } else {
//                mCurrPosition = getVisibleViews("right");
//            }
//
//            mHorizontalScrollView.smoothScrollTo(mSimilarItemsList.get(mCurrPosition)
//                    .getLeft(), 0);
//            return true;
//        }
//
//    }

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

    public void addSimilarItems(List<Item> items) {

        for (int i = 0; i < items.size(); i++) {
            final Item item = items.get(i);

            View similarItemLayout = getActivity().getLayoutInflater().inflate(R.layout.similar_item_layout, null);

            TextView similarItemTitle = (TextView) similarItemLayout.findViewById(R.id.item_title);
            ImageSelector similarItemImage = (ImageSelector) similarItemLayout.findViewById(R.id.item_image);
            TextView similarPostPrice = (TextView) similarItemLayout.findViewById(R.id.similar_post_price);

            similarItemImage.loadInBackground(item.getParseFile(Item.IMAGE_1));
            similarItemTitle.setText(TextUtil.convertEnNumberToFa(item.getName()));

            NumberFormat formatter = TextUtil.getPriceNumberFormmat(Locale.ENGLISH);
            String formattedPrice = formatter.format(item.getPrice());
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
                    controlPanelActivity.displayItemPage(item.getObjectId(), false);
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

}


