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
import net.honarnama.browse.adapter.ItemsAdapter;
import net.honarnama.browse.model.Item;
import net.honarnama.browse.model.Shop;
import net.honarnama.core.model.City;
import net.honarnama.core.model.Provinces;
import net.honarnama.core.model.Store;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.core.utils.ObservableScrollView;
import net.honarnama.core.utils.WindowUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
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
public class ItemPageFragment extends HonarnamaBrowseFragment implements View.OnClickListener, AdapterView.OnItemClickListener, ObservableScrollView.OnScrollChangedListener {
    public static ShopPageFragment mShopPageFragment;
    public ImageView mRetryIcon;
    private Tracker mTracker;
    private ItemsAdapter mAdapter;
    private ImageSelector mLogoImageView;
    private ImageSelector mBannerImageView;
    public ProgressBar mBannerProgressBar;
    public ProgressBar mLogoProgressBar;

    public TextView mItemName;
    public TextView mItemDesc;
    public TextView mItemPlace;
    private ParseUser mOwner;

    private ObservableScrollView mScrollView;
    private View mBannerFrameLayout;
    private RelativeLayout mShare;
    public String mItemId;

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

        mBannerImageView = (ImageSelector) rootView.findViewById(R.id.item_banner_image_view);

        mBannerProgressBar = (ProgressBar) rootView.findViewById(R.id.banner_progress_bar);

        mItemName = (TextView) rootView.findViewById(R.id.item_name_text_view);
        mItemDesc = (TextView) rootView.findViewById(R.id.item_desc_text_view);
        mItemPlace = (TextView) rootView.findViewById(R.id.item_place_text_view);
        mShare = (RelativeLayout) rootView.findViewById(R.id.item_share_container);
        mShare.setOnClickListener(this);

        final RelativeLayout infoContainer = (RelativeLayout) rootView.findViewById(R.id.item_info_container);

        Item.getItemById(mItemId).continueWith(new Continuation<ParseObject, Object>() {
            @Override
            public Object then(Task<ParseObject> task) throws Exception {
                if (task.isFaulted()) {
                    logE("Getting Item  with id " + mItemId + " failed. Error: " + task.getError(), "", task.getError());
                    if (isVisible()) {
                        Toast.makeText(getActivity(), getActivity().getString(R.string.error_displaying_item) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG);
                    }
                } else {
                    infoContainer.setVisibility(View.VISIBLE);
                    mShare.setVisibility(View.VISIBLE);
                    ParseObject item = task.getResult();
                    mOwner = item.getParseUser(Item.OWNER);
                    mItemName.setText(item.getString(Store.NAME));
                    mItemDesc.setText(item.getString(Store.DESCRIPTION));

                    mBannerProgressBar.setVisibility(View.VISIBLE);
                    mBannerImageView.loadInBackground(item.getParseFile(Item.IMAGE_1), new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException e) {
                            mBannerProgressBar.setVisibility(View.GONE);
                            if (e != null) {
                                logE("Getting  banner image for item " + mItemId + " failed. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e, "", e);
                                if (isVisible()) {
                                    Toast.makeText(getActivity(), getString(R.string.error_displaying_image) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
                }
                return null;
            }
        });
        return rootView;

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.item_share_container) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, mItemName.getText());
            sendIntent.putExtra(Intent.EXTRA_TEXT, "سلام،" + "\n" + "این محصول هنری تو برنامه‌ی هنرنما رو می‌خواستم بهت پیشنهاد بدم." + "\n" + mItemName.getText() +
                    "\n" + "http://www.honarnama.net/item/" + mItemId);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
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
