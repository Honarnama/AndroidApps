package net.honarnama.browse.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

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
import net.honarnama.browse.model.Item;
import net.honarnama.core.model.City;
import net.honarnama.core.model.Provinces;
import net.honarnama.core.model.Store;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.core.utils.ObservableScrollView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

import android.widget.RelativeLayout.LayoutParams;

/**
 * Created by elnaz on 2/15/16.
 */
public class ItemPageFragment extends HonarnamaBrowseFragment implements View.OnClickListener, AdapterView.OnItemClickListener, ObservableScrollView.OnScrollChangedListener {
    public static ShopPageFragment mShopPageFragment;
    public ImageView mRetryIcon;
    private Tracker mTracker;
    public ProgressBar mBannerProgressBar;

    public TextView mNameTextView;
    public TextView mPriceTextView;
    public TextView mDescEditText;
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
    public String mItemId;

    ImageAdapter mImageAdapter;

    public RelativeLayout mShopContainer;

    public Store mShop;

    LayoutParams mLayoutParams;
    LinearLayout mNext, mPrev;
    int mSimilarItemViewWidth;
    //    GestureDetector mGestureDetector = null;
    HorizontalScrollView mHorizontalScrollView;
    ArrayList<View> mSimilarItemsList = new ArrayList<>();
    int mWidth;
    int mCurrPosition, mPrevPosition;

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

        if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
            final View rootView = inflater.inflate(R.layout.fragment_no_network, container, false);
            mRetryIcon = (ImageView) rootView.findViewById(R.id.no_network_fragment_retry_icon);
            mRetryIcon.setOnClickListener(this);
            return rootView;
        }
        final View rootView = inflater.inflate(R.layout.fragment_item_page, container, false);
        mItemId = getArguments().getString("itemId");


        mScrollView = (ObservableScrollView) rootView.findViewById(R.id.fragment_scroll_view);
        mScrollView.setOnScrollChangedListener(this);
        mBannerFrameLayout = rootView.findViewById(R.id.item_banner_frame_layout);

        mBannerProgressBar = (ProgressBar) rootView.findViewById(R.id.banner_progress_bar);

        mNameTextView = (TextView) rootView.findViewById(R.id.item_name_text_view);
        mPriceTextView = (TextView) rootView.findViewById(R.id.price);
        mDescEditText = (TextView) rootView.findViewById(R.id.item_desc_text_view);
        mPlaceTextView = (TextView) rootView.findViewById(R.id.item_place_text_view);
        mShare = (RelativeLayout) rootView.findViewById(R.id.item_share_container);
        mShare.setOnClickListener(this);

        mShopContainer = (RelativeLayout) rootView.findViewById(R.id.item_shop_container);
        mShopContainer.setOnClickListener(this);
        mShopNameTextView = (TextView) rootView.findViewById(R.id.shop_name_text_view);
        mShopLogo = (ImageSelector) rootView.findViewById(R.id.store_logo_image_view);

        mSimilarTitleContainer = (RelativeLayout) rootView.findViewById(R.id.similar_title_container);
        mImageAdapter = new ImageAdapter(HonarnamaBrowseApp.getInstance());
        mDotsLayout = (LinearLayout) rootView.findViewById(R.id.image_dots_container);
        mInnerLayout = (LinearLayout) rootView.findViewById(R.id.innerLayout);

        final RelativeLayout infoContainer = (RelativeLayout) rootView.findViewById(R.id.item_info_container);

        Item.getItemById(mItemId).continueWith(new Continuation<ParseObject, Object>() {
            @Override
            public Object then(Task<ParseObject> task) throws Exception {
                if (task.isFaulted()) {
                    logE("Getting item with id " + mItemId + " for item page failed. Error: " + task.getError(), "", task.getError());
                    if (isVisible()) {
                        Toast.makeText(getActivity(), getActivity().getString(R.string.error_displaying_item) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG);
                    }
                } else {
                    infoContainer.setVisibility(View.VISIBLE);
                    mShare.setVisibility(View.VISIBLE);
                    Item item = (Item) task.getResult();
                    mNameTextView.setText(item.getName());
                    mPriceTextView.setText(item.getPrice() + "");
                    mDescEditText.setText(item.getDescription());

                    mShop = item.getStore();
                    mPlaceTextView.setText(mShop.getProvince().getString(Provinces.NAME) + "، " + mShop.getCity().getString(City.NAME));
                    mShopNameTextView.append(mShop.getName());
                    mShopLogo.loadInBackground(mShop.getLogo(), new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException e) {
                        }
                    });

                    mBannerProgressBar.setVisibility(View.VISIBLE);

                    ParseFile[] images = item.getImages();
                    List<ParseFile> nonNullImages = new ArrayList<ParseFile>();
                    for (int i = 0; i < net.honarnama.core.model.Item.NUMBER_OF_IMAGES; i++) {
                        if (images[i] != null) {
                            nonNullImages.add(images[i]);
                        }
                    }
                    mImageAdapter.addAll(nonNullImages);
                    logE(mImageAdapter.getCount() + "");
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
                            mDotsText[i].setTextColor(getResources().getColor(R.color.amber_launcher_color));
                            mDotsLayout.addView(mDotsText[i]);
                        }
                    }
                    Item.getSimilarItemsByCategory(item.getCategory()).continueWith(new Continuation<List<Item>, Object>() {
                        @Override
                        public Object then(Task<List<Item>> task) throws Exception {

                            if (task.isFaulted()) {
                                logE("Finding similar items failed. " + task.getError());
                            } else {
                                List<Item> similarItems = task.getResult();
                                if (similarItems.size() > 0) {
                                    mSimilarTitleContainer.setVisibility(View.VISIBLE);
                                    addItems(task.getResult());
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

        mHorizontalScrollView = (HorizontalScrollView) rootView.findViewById(R.id.hsv);
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

        if (v.getId() == R.id.item_share_container) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, mNameTextView.getText().toString());
            sendIntent.putExtra(Intent.EXTRA_TEXT, "سلام،" + "\n" + "این محصول هنری تو برنامه‌ی هنرنما رو می‌خواستم بهت پیشنهاد بدم." + "\n" + mNameTextView.getText() +
                    "\n" + "http://www.honarnama.net/item/" + mItemId);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }

        if (v.getId() == R.id.item_shop_container) {
            ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
            controlPanelActivity.displayShopPage(mShop.getObjectId(), false);
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


    public void addItems(List<Item> items) {

        for (int i = 1; i < items.size(); i++) {
            Item item = items.get(i);

            View similarItemLayout = getActivity().getLayoutInflater().inflate(R.layout.similar_item_layout, null);

            TextView similarItemTitle = (TextView) similarItemLayout.findViewById(R.id.item_title);
            ImageSelector similarItemImage = (ImageSelector) similarItemLayout.findViewById(R.id.item_image);
            TextView similarPostPrice = (TextView) similarItemLayout.findViewById(R.id.similar_post_price);

            similarItemImage.loadInBackground(item.getParseFile(Item.IMAGE_1));
            similarItemTitle.setText(item.getName());
            similarPostPrice.setText(item.getPrice() + " " + getString(R.string.toman));

            mLayoutParams = new LayoutParams(mSimilarItemViewWidth, LayoutParams.WRAP_CONTENT);
            if ((i % 3) == 0) {
                logE("inja", item.getName());
                similarItemTitle.setPadding(5, 0, 5, 0);
            }
            similarItemLayout.setLayoutParams(mLayoutParams);
            similarItemLayout.requestLayout();

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


