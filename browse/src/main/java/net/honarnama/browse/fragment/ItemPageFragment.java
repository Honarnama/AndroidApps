package net.honarnama.browse.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

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
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Gallery;
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
    public List<ParseFile> mImageList;
    private LinearLayout mDotsLayout;


    static TextView mDotsText[];
    private int mDotsCount;

    private ObservableScrollView mScrollView;
    private View mBannerFrameLayout;
    private RelativeLayout mShare;
    public String mItemId;

    ImageAdapter mImageAdapter;

    public RelativeLayout mShopContainer;

    public Store mShop;


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

        mImageAdapter = new ImageAdapter(HonarnamaBrowseApp.getInstance());
        mDotsLayout = (LinearLayout) rootView.findViewById(R.id.image_dots_container);

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
//                    mBannerImageView.loadInBackground(item.getParseFile(Item.IMAGE_1), new GetDataCallback() {
//                        @Override
//                        public void done(byte[] data, ParseException e) {
//                            mBannerProgressBar.setVisibility(View.GONE);
//                            if (e != null) {
//                                logE("Getting  banner image for item " + mItemId + " failed. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e, "", e);
//                                if (isVisible()) {
//                                    Toast.makeText(getActivity(), getString(R.string.error_displaying_image) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
//                                }
//                            }
//                        }
//                    });

//                    if (item.has(Item.IMAGE_1)) {
//                        logE("inja 1");
////                        mImageList.add(item.getParseFile(Item.IMAGE_1));
//                    }
//                    if (item.has(Item.IMAGE_2)) {
//                        logE("inja 2");
//                        mImageList.add(item.getParseFile(Item.IMAGE_2));
//                    }
//                    if (item.has(Item.IMAGE_3)) {
//                        mImageList.add(item.getParseFile(Item.IMAGE_3));
//                    }
//                    if (item.has(Item.IMAGE_4)) {
//                        mImageList.add(item.getParseFile(Item.IMAGE_4));
//                    }

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
                        mDotsText = new TextView[mDotsCount];
                        for (int i = 0; i < mDotsCount; i++) {
                            mDotsText[i] = new TextView(HonarnamaBrowseApp.getInstance());
                            mDotsText[i].setText(".");
                            mDotsText[i].setTextSize(25);
                            mDotsText[i].setTypeface(null, Typeface.BOLD);
                            mDotsText[i].setTextColor(getResources().getColor(R.color.amber_dark));
                            mDotsLayout.addView(mDotsText[i]);
                        }
                    }
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
                        mDotsText[i].setTextColor(getResources().getColor(R.color.amber_dark));
                    }
                    mDotsText[pos].setTextColor(Color.WHITE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView adapterView) {

            }
        });

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

}