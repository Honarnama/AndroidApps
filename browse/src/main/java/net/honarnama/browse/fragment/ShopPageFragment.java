package net.honarnama.browse.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.parse.GetDataCallback;
import com.parse.ImageSelector;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.adapter.ShopItemsAdapter;
import net.honarnama.browse.model.Item;
import net.honarnama.browse.model.Shop;
import net.honarnama.core.model.City;
import net.honarnama.core.model.Provinces;
import net.honarnama.core.model.Store;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.core.utils.ObservableScrollView;
import net.honarnama.core.utils.WindowUtil;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by elnaz on 2/15/16.
 */
public class ShopPageFragment extends HonarnamaBrowseFragment implements View.OnClickListener, AdapterView.OnItemClickListener, ObservableScrollView.OnScrollChangedListener {
    public static ShopPageFragment mShopPageFragment;
    public ImageView mRetryIcon;
    private Tracker mTracker;
    private ShopItemsAdapter mAdapter;
    private ImageSelector mLogoImageView;
    private ImageSelector mBannerImageView;
    public ProgressBar mBannerProgressBar;
    public ProgressBar mLogoProgressBar;

    public TextView mShopName;
    public TextView mShopDesc;
    public TextView mShopPlace;
    private ParseUser mOwner;
    public ListView mListView;

    private ObservableScrollView mScrollView;
    private View mBannerFrameLayout;

    @Override
    public String getTitle(Context context) {
        return "صفحه‌ی اختصاصی غرفه";
    }

    public synchronized static ShopPageFragment getInstance(String shopId, ParseUser owner) {
        ShopPageFragment shopPageFragment = new ShopPageFragment();
        Bundle args = new Bundle();
        args.putString("shopId", shopId);
        shopPageFragment.setArguments(args);
        shopPageFragment.setOwner(owner);
        return shopPageFragment;
    }

    private void setOwner(ParseUser owner) {
        mOwner = owner;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = HonarnamaBrowseApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("ShopPageFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
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
        final View rootView = inflater.inflate(R.layout.fragment_shop_page, container, false);
        final String shopId = getArguments().getString("shopId");


        mListView = (ListView) rootView.findViewById(R.id.shop_items_listView);

        final TextView emptyListTextVie = (TextView) rootView.findViewById(R.id.empty_items_list_view);
        mListView.setEmptyView(emptyListTextVie);

        mScrollView = (ObservableScrollView) rootView.findViewById(R.id.store_fragment_scroll_view);
        mScrollView.setOnScrollChangedListener(this);
        mBannerFrameLayout = rootView.findViewById(R.id.store_banner_frame_layout);

        mLogoImageView = (ImageSelector) rootView.findViewById(R.id.store_logo_image_view);
        mBannerImageView = (ImageSelector) rootView.findViewById(R.id.store_banner_image_view);

        mBannerProgressBar = (ProgressBar) rootView.findViewById(R.id.banner_progress_bar);
        mLogoProgressBar = (ProgressBar) rootView.findViewById(R.id.logo_progress_bar);

        mShopName = (TextView) rootView.findViewById(R.id.store_name_text_view);
        mShopDesc = (TextView) rootView.findViewById(R.id.store_desc_text_view);
        mShopPlace = (TextView) rootView.findViewById(R.id.shop_place_text_view);

        final LinearLayout loadingCircle = (LinearLayout) rootView.findViewById(R.id.loading_circle_container);
        emptyListTextVie.setVisibility(View.GONE);
        loadingCircle.setVisibility(View.VISIBLE);
        Item.getItemsByOwner(mOwner).continueWith(new Continuation<List<Item>, Object>() {
            @Override
            public Object then(Task<List<Item>> task) throws Exception {
                loadingCircle.setVisibility(View.GONE);
                emptyListTextVie.setVisibility(View.VISIBLE);
                emptyListTextVie.setText(HonarnamaBrowseApp.getInstance().getString(R.string.shop_has_no_item));
                if (task.isFaulted()) {
                    logE("Getting Shop items for owner " + mOwner.getObjectId() + " failed. Error: " + task.getError(), "", task.getError());
                    if (isVisible()) {
                        Toast.makeText(getActivity(), HonarnamaBrowseApp.getInstance().getString(R.string.error_getting_items_list) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG);
                    }
                } else {
                    List<Item> shopItems = task.getResult();
                    mAdapter.addAll(shopItems);
                    mAdapter.notifyDataSetChanged();
                    WindowUtil.setListViewHeightBasedOnChildren(mListView);
                }
                return null;
            }
        }).continueWithTask(new Continuation<Object, Task<ParseObject>>() {
            @Override
            public Task<ParseObject> then(Task<Object> task) throws Exception {
                return Shop.getShopById(shopId);
            }
        }).continueWith(new Continuation<ParseObject, Object>() {
            @Override
            public Object then(Task<ParseObject> task) throws Exception {
                if (task.isFaulted()) {
                    logE("Getting Shop  with id " + shopId + " failed. Error: " + task.getError(), "", task.getError());
                    if (isVisible()) {
                        Toast.makeText(getActivity(), getActivity().getString(R.string.error_displaying_shop) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG);
                    }
                } else {
                    ParseObject shop = task.getResult();

                    mShopName.setText(shop.getString(Store.NAME));
                    mShopDesc.setText(shop.getString(Store.DESCRIPTION));
                    String province = shop.getParseObject(Store.PROVINCE).getString(Provinces.NAME);
                    String city = shop.getParseObject(Store.CITY).getString(City.NAME);

                    mShopPlace.setText(province + "، " + city);

                    mLogoProgressBar.setVisibility(View.VISIBLE);
                    mLogoImageView.loadInBackground(shop.getParseFile(Store.LOGO), new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException e) {
                            mLogoProgressBar.setVisibility(View.GONE);
                            if (e != null) {
                                logE("Getting  logo image for shop " + shopId + " failed. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error:" + e, "", e);
                                if (isVisible()) {
                                    Toast.makeText(getActivity(), getString(R.string.error_displaying_store_logo) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });

                    mBannerProgressBar.setVisibility(View.VISIBLE);
                    mBannerImageView.loadInBackground(shop.getParseFile(Store.BANNER), new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException e) {
                            mBannerProgressBar.setVisibility(View.GONE);
                            if (e != null) {
                                logE("Getting  banner image for shop " + shopId + " failed. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e, "", e);
                                if (isVisible()) {
                                    Toast.makeText(getActivity(), getString(R.string.error_displaying_store_banner) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
                }
                return null;
            }
        });

        mAdapter = new ShopItemsAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        return rootView;

    }

    @Override
    public void onClick(View v) {

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
