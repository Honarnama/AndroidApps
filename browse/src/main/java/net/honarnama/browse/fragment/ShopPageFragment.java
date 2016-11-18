package net.honarnama.browse.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.parse.ImageSelector;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.adapter.ItemsAdapter;
import net.honarnama.browse.dialog.ContactDialog;
import net.honarnama.browse.model.Item;
import net.honarnama.browse.model.Shop;
import net.honarnama.base.model.Store;
import net.honarnama.base.utils.NetworkManager;
import net.honarnama.base.utils.ObservableScrollView;
import net.honarnama.base.utils.TextUtil;

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

    public synchronized static ShopPageFragment getInstance(long shopId) {
        ShopPageFragment shopPageFragment = new ShopPageFragment();
        Bundle args = new Bundle();
        args.putLong("shopId", shopId);
        shopPageFragment.setArguments(args);
//        shopPageFragment.setOwner(owner);
        return shopPageFragment;
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
        Shop.getShopById(mShopId).continueWith(new Continuation<Store, Boolean>() {
            @Override
            public Boolean then(Task<Store> task) throws Exception {
                if (task.isFaulted()) {
//                    if (((ParseException) task.getError()).getCode() == ParseException.OBJECT_NOT_FOUND) {
//                        if (isVisible()) {
//                            Toast.makeText(getActivity(), getActivity().getString(R.string.error_shop_no_longer_exists), Toast.LENGTH_SHORT).show();
//                        }
//                        deletedShopMsg.setVisibility(View.VISIBLE);
//                    } else {
//                        logE("Getting Shop  with id " + mShopId + " failed. Error: " + task.getError(), task.getError());
//                        if (isVisible()) {
//                            Toast.makeText(getActivity(), getActivity().getString(R.string.error_displaying_shop) + getString(R.string.check_net_connection), Toast.LENGTH_LONG).show();
//                        }
//                        mOnErrorRetry.setVisibility(View.VISIBLE);
//                    }
                    itemsloadingCircle.setVisibility(View.GONE);
                    return false;
                } else {
                    fab.setVisibility(View.VISIBLE);
                    infoContainer.setVisibility(View.VISIBLE);
                    mOnErrorRetry.setVisibility(View.GONE);
                    mShare.setVisibility(View.VISIBLE);
                    final Store shop = task.getResult();
                    //TODO
//                    mOwner = shop.getParseUser(Store.OWNER);
                    mShopName.setText(TextUtil.convertEnNumberToFa(shop.getName()));
                    mShopDesc.setText(TextUtil.convertEnNumberToFa(shop.getDescription()));

                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ContactDialog contactDialog = new ContactDialog();
                            contactDialog.showDialog(getActivity(), shop.getPhoneNumber(), shop.getCellNumber(),
                                    getResources().getString(R.string.item_contact_dialog_warning_msg));

                        }
                    });

                    String province = shop.getProvince().getName();
                    String city = shop.getCity().getName();

                    mShopPlace.setText(province + "، " + city);

                    mLogoProgressBar.setVisibility(View.VISIBLE);
                    //TODO
//                    mLogoImageView.loadInBackground(shop.getParseFile(Store.LOGO), new GetDataCallback() {
//                        @Override
//                        public void done(byte[] data, ParseException e) {
//                            mLogoProgressBar.setVisibility(View.GONE);
//                            if (e != null) {
//                                logE("Getting  logo image for shop " + mShopId + " failed. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error:" + e, "", e);
//                                if (isVisible()) {
//                                    Toast.makeText(getActivity(), getString(R.string.error_displaying_store_logo) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
//                                }
//                            }
//                        }
//                    });

                    mBannerProgressBar.setVisibility(View.VISIBLE);
                    //TODO
//                    mBannerImageView.loadInBackground(shop.getParseFile(Store.BANNER), new GetDataCallback() {
//                        @Override
//                        public void done(byte[] data, ParseException e) {
//                            mBannerProgressBar.setVisibility(View.GONE);
//                            if (e != null) {
//                                logE("Getting  banner image for shop " + mShopId + " failed. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e, "", e);
//                                if (isVisible()) {
//                                    Toast.makeText(getActivity(), getString(R.string.error_displaying_store_banner) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
//                                }
//                            }
//                        }
//                    });
                }
                return true;
            }
        }).continueWithTask(new Continuation<Boolean, Task<List<Item>>>() {
            @Override
            public Task<List<Item>> then(Task<Boolean> task) throws Exception {
//                if (task.getResult() == true && mOwner != null) {
//                    return Item.getItemsByOwner(mOwner);
//                } else {
//                    return Task.forError(new ParseException(ParseException.OBJECT_NOT_FOUND, "Shop is not verified or no shop found"));
//                }
                return null;
            }
        }).continueWith(new Continuation<List<Item>, Object>() {
            @Override
            public Object then(Task<List<Item>> task) throws Exception {
                itemsloadingCircle.setVisibility(View.GONE);
//                if (task.isFaulted() && ((ParseException) task.getError()).getCode() != ParseException.OBJECT_NOT_FOUND) {
//                    logE("Getting items for shop " + mShopId + " failed. Error: " + task.getError(), task.getError());
//                    if (isVisible()) {
//                        Toast.makeText(getActivity(), HonarnamaBrowseApp.getInstance().getString(R.string.error_getting_items_list) + getString(R.string.check_net_connection), Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    List<Item> shopItems = task.getResult();
//                    if (shopItems.size() == 0) {
//                        mListView.setEmptyView(emptyListContainer);
//                        emptyListContainer.setVisibility(View.VISIBLE);
//                    }
//                    mItemsAdapter.setItems(shopItems);
//                    mItemsAdapter.notifyDataSetChanged();
//                    WindowUtil.setListViewHeightBasedOnChildren(mListView);
//                }
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
                    "\n" + HonarnamaBaseApp.WEB_ADDRESS + "/shop/" + mShopId);
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
        net.honarnama.nano.Item selectedItem = mItemsAdapter.getItem(position);
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        controlPanelActivity.displayItemPage(selectedItem.id, false);
    }

    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        int scrollY = mScrollView.getScrollY();
        // Add parallax effect
        mBannerFrameLayout.setTranslationY(scrollY * 0.5f);

    }

}
