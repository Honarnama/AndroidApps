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
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.adapter.ItemsAdapter;
import net.honarnama.browse.dialog.ContactDialog;
import net.honarnama.browse.model.Item;
import net.honarnama.browse.model.Shop;
import net.honarnama.core.model.City;
import net.honarnama.core.model.Provinces;
import net.honarnama.core.model.Store;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.core.utils.ObservableScrollView;
import net.honarnama.core.utils.WindowUtil;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
    private Tracker mTracker;
    private ItemsAdapter mItemsAdapter;
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
    private RelativeLayout mShare;
    public String mShopId;

    public RelativeLayout mOnErrorRetry;

    @Override
    public String getTitle(Context context) {
        return getString(R.string.art_shop);
    }

    public synchronized static ShopPageFragment getInstance(String shopId) {
        ShopPageFragment shopPageFragment = new ShopPageFragment();
        Bundle args = new Bundle();
        args.putString("shopId", shopId);
        shopPageFragment.setArguments(args);
//        shopPageFragment.setOwner(owner);
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

    @Override
    public void onSelectedTabClick() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_shop_page, container, false);
        mShopId = getArguments().getString("shopId");

        mListView = (ListView) rootView.findViewById(R.id.shop_items_listView);

        final RelativeLayout emptyListContainer = (RelativeLayout) rootView.findViewById(R.id.empty_list_container);

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
        mShare = (RelativeLayout) rootView.findViewById(R.id.shop_share_container);
        mShare.setOnClickListener(this);

        mOnErrorRetry = (RelativeLayout) rootView.findViewById(R.id.on_error_retry_container);
        mOnErrorRetry.setOnClickListener(this);

        final RelativeLayout deletedShopMsg = (RelativeLayout) rootView.findViewById(R.id.deleted_shop_msg);
        final RelativeLayout infoContainer = (RelativeLayout) rootView.findViewById(R.id.store_info_container);

        final LinearLayout itemsloadingCircle = (LinearLayout) rootView.findViewById(R.id.shop_items_loading_circle);
        emptyListContainer.setVisibility(View.GONE);
        itemsloadingCircle.setVisibility(View.VISIBLE);

        final FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        mItemsAdapter = new ItemsAdapter(getActivity());
        mOnErrorRetry.setVisibility(View.GONE);
        Shop.getShopById(mShopId).continueWith(new Continuation<ParseObject, Boolean>() {
            @Override
            public Boolean then(Task<ParseObject> task) throws Exception {
                if (task.isFaulted()) {
                    if (((ParseException) task.getError()).getCode() == ParseException.OBJECT_NOT_FOUND) {
                        if (isVisible()) {
                            Toast.makeText(getActivity(), getActivity().getString(R.string.error_shop_no_longer_exists), Toast.LENGTH_SHORT).show();
                        }
                        deletedShopMsg.setVisibility(View.VISIBLE);
                    } else {
                        logE("Getting Shop  with id " + mShopId + " failed. Error: " + task.getError(), "", task.getError());
                        if (isVisible()) {
                            Toast.makeText(getActivity(), getActivity().getString(R.string.error_displaying_shop) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                        }
                        mOnErrorRetry.setVisibility(View.VISIBLE);
                    }
                    itemsloadingCircle.setVisibility(View.GONE);
                    return false;
                } else {
                    fab.setVisibility(View.VISIBLE);
                    infoContainer.setVisibility(View.VISIBLE);
                    mOnErrorRetry.setVisibility(View.GONE);
                    mShare.setVisibility(View.VISIBLE);
                    final ParseObject shop = task.getResult();
                    mOwner = shop.getParseUser(Store.OWNER);
                    mShopName.setText(shop.getString(Store.NAME));
                    mShopDesc.setText(shop.getString(Store.DESCRIPTION));

                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ContactDialog contactDialog = new ContactDialog();
                            contactDialog.showDialog(getActivity(), shop.getString(Store.PHONE_NUMBER), shop.getString(Store.CELL_NUMBER),
                                    getResources().getString(R.string.item_contact_dialog_warning_msg));

                        }
                    });

                    String province = shop.getParseObject(Store.PROVINCE).getString(Provinces.NAME);
                    String city = shop.getParseObject(Store.CITY).getString(City.NAME);

                    mShopPlace.setText(province + "، " + city);

                    mLogoProgressBar.setVisibility(View.VISIBLE);
                    mLogoImageView.loadInBackground(shop.getParseFile(Store.LOGO), new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException e) {
                            mLogoProgressBar.setVisibility(View.GONE);
                            if (e != null) {
                                logE("Getting  logo image for shop " + mShopId + " failed. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error:" + e, "", e);
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
                                logE("Getting  banner image for shop " + mShopId + " failed. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e, "", e);
                                if (isVisible()) {
                                    Toast.makeText(getActivity(), getString(R.string.error_displaying_store_banner) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
                }
                return true;
            }
        }).continueWithTask(new Continuation<Boolean, Task<List<Item>>>() {
            @Override
            public Task<List<Item>> then(Task<Boolean> task) throws Exception {
                if (task.getResult() == true && mOwner != null) {
                    return Item.getItemsByOwner(mOwner);
                } else {
                    return Task.forError(new ParseException(ParseException.OBJECT_NOT_FOUND, "Shop is not verified or no shop found"));
                }
            }
        }).continueWith(new Continuation<List<Item>, Object>() {
            @Override
            public Object then(Task<List<Item>> task) throws Exception {
                itemsloadingCircle.setVisibility(View.GONE);
                if (task.isFaulted() && ((ParseException) task.getError()).getCode() != ParseException.OBJECT_NOT_FOUND) {
                    logE("Getting items for shop " + mShopId + " failed. Error: " + task.getError(), "", task.getError());
                    if (isVisible()) {
                        Toast.makeText(getActivity(), HonarnamaBrowseApp.getInstance().getString(R.string.error_getting_items_list) + getString(R.string.please_check_internet_connection), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    List<Item> shopItems = task.getResult();
                    if (shopItems.size() == 0) {
                        mListView.setEmptyView(emptyListContainer);
                        emptyListContainer.setVisibility(View.VISIBLE);
                    }
                    mItemsAdapter.setItems(shopItems);
                    mItemsAdapter.notifyDataSetChanged();
                    WindowUtil.setListViewHeightBasedOnChildren(mListView);
                }
                return null;
            }
        });

        mListView.setAdapter(mItemsAdapter);
        mListView.setOnItemClickListener(this);

        return rootView;

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.shop_share_container) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT,
                    getString(R.string.art_shop) + " " + mShopName.getText());
            sendIntent.putExtra(Intent.EXTRA_TEXT, "سلام،" + "\n" + "غرفه هنری " + mShopName.getText() + " تو برنامه هنرما رو ببین: " +
                    "\n" + "http://www.honarnama.net/shop/" + mShopId);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
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
        ParseObject selectedItem = (ParseObject) mItemsAdapter.getItem(position);
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        controlPanelActivity.displayItemPage(selectedItem.getObjectId(), false);
    }

    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        int scrollY = mScrollView.getScrollY();
        // Add parallax effect
        mBannerFrameLayout.setTranslationY(scrollY * 0.5f);

    }

}
