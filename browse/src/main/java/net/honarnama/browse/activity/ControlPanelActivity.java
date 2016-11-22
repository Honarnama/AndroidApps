package net.honarnama.browse.activity;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.context.IconicsLayoutInflater;
import com.mikepenz.iconics.view.IconicsImageView;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.browse.BuildConfig;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.adapter.MainFragmentAdapter;
import net.honarnama.browse.dialog.ConfirmationDialog;
import net.honarnama.browse.fragment.BookmarksFragment;
import net.honarnama.browse.fragment.ChildFragment;
import net.honarnama.browse.fragment.EventPageFragment;
import net.honarnama.browse.fragment.ItemPageFragment;
import net.honarnama.browse.fragment.NoNetFragment;
import net.honarnama.browse.fragment.SearchFragment;
import net.honarnama.browse.fragment.ShopPageFragment;
import net.honarnama.browse.model.Bookmark;
import net.honarnama.browse.widget.LockableViewPager;
import net.honarnama.browse.widget.MainTabBar;
import net.honarnama.base.adapter.CityAdapter;
import net.honarnama.base.adapter.ProvincesAdapter;
import net.honarnama.base.fragment.AboutFragment;
import net.honarnama.base.fragment.ContactFragment;
import net.honarnama.base.fragment.HonarnamaBaseFragment;
import net.honarnama.base.model.City;
import net.honarnama.base.model.Province;
import net.honarnama.base.utils.CommonUtil;
import net.honarnama.base.utils.NetworkManager;
import net.honarnama.base.utils.WindowUtil;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import bolts.Continuation;
import bolts.Task;

import static net.honarnama.browse.widget.MainTabBar.TAB_EVENTS;
import static net.honarnama.browse.widget.MainTabBar.TAB_ITEMS;
import static net.honarnama.browse.widget.MainTabBar.TAB_SEARCH;
import static net.honarnama.browse.widget.MainTabBar.TAB_SHOPS;

public class ControlPanelActivity extends HonarnamaBrowseActivity implements MainTabBar.OnTabItemClickListener, View.OnClickListener {

    //TODO add crashlytics
    //TODO add Analytics

    public static Button btnRed; // Works as a badge
    //Declared static; so it can be accessed from all other Activities

    MainFragmentAdapter mMainFragmentAdapter;
    LockableViewPager mViewPager;
    private Toolbar mToolbar;
    private int mActiveTab;
    private MainTabBar mMainTabBar;
    private long mShopId;
    private long mEventId;
    private long mItemId;
    public TextView mTitle;

    private DrawerLayout mDrawer;
    public NavigationView mNavigationView;

    public ContactFragment mContactFragment;
    public AboutFragment mAboutFragment;
    public BookmarksFragment mBookmarksFragment;

    public RelativeLayout mNavFooter;

    public static final int ITEM_IDENTIFIER_LOCATION = 0;
    public static final int ITEM_IDENTIFIER_BOOKMARKS = 1;
    public static final int ITEM_IDENTIFIER_CONTACT = 2;
    public static final int ITEM_IDENTIFIER_RULES = 3;
    public static final int ITEM_IDENTIFIER_ABOUT = 4;
    public static final int ITEM_IDENTIFIER_SHARE = 5;
    public static final int ITEM_IDENTIFIER_SUPPORT = 6;
    public static final int ITEM_IDENTIFIER_SWAP = 7;
    public static final int ITEM_IDENTIFIER_EXIT = 8;

    public Dialog mSetDefaultLocationDialog;

    public TreeMap<Number, Province> mProvincesTreeMap = new TreeMap();
    public HashMap<Integer, String> mProvincesHashMap = new HashMap<>();

    public int mDefaultProvinceId;
    public String mDefaultProvinceName;
    public int mSelectedProvinceId = -1;
    public String mSelectedProvinceName;

    public EditText mDefaultProvinceEditText;
    private Province mSelectedProvince;

    public TreeMap<Number, HashMap<Integer, String>> mCityTreeMap = new TreeMap<>();
    public HashMap<Integer, String> mCityHashMap = new HashMap<>();
    public int mDefaultCityId;
    public String mDefaultCityName;
    public int mSelectedCityId = -1;
    public String mSelectedCityName;
    public EditText mDefaultCityEditText;

    IconicsImageView mRefetchProvinces;
    IconicsImageView mRefetchCities;

    @Override
    public void onCreate(Bundle savedInstanceState) {
//        LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContactFragment = ContactFragment.getInstance();
        mAboutFragment = AboutFragment.getInstance();
        mBookmarksFragment = BookmarksFragment.getInstance();

        mMainFragmentAdapter =
                new MainFragmentAdapter(
                        getSupportFragmentManager());

        mViewPager = (LockableViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(mMainFragmentAdapter);
        mViewPager.setSwipeable(false);
//        mViewPager.setOffscreenPageLimit(4);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mActiveTab = position;
                ChildFragment childFragment = mMainFragmentAdapter.getItem(position);
                if (!childFragment.hasContent()) {
                    switchFragment(mMainFragmentAdapter.getDefaultFragmentForTab(position), false, getResources().getString(R.string.hornama));
                } else {
                    FragmentManager childFragmentManager = mMainFragmentAdapter.getItem(mActiveTab)
                            .getChildFragmentManager();
                    childFragmentManager.executePendingTransactions();
                    HonarnamaBaseFragment topFragment = (HonarnamaBaseFragment) childFragmentManager.findFragmentById(R.id.child_fragment_root);

                    if (topFragment instanceof NoNetFragment) {
                        if (NetworkManager.getInstance().isNetworkEnabled(false)) {
                            refreshNoNetFragment();
                            return;
                        }
                    }

                    mTitle.setText(topFragment.getTitle(HonarnamaBrowseApp.getInstance()));
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mMainTabBar = (MainTabBar) findViewById(R.id.tab_bar);
        mMainTabBar.setOnTabItemClickListener(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setDefaultTab();
        setSupportActionBar(mToolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        getSupportActionBar().setHomeButtonEnabled(false);
//        getSupportActionBar().setLogo(new IconicsDrawable(ControlPanelActivity.this)
//                .icon(GoogleMaterial.Icon.gmd_menu)
//                .color(Color.WHITE)
//                .sizeDp(20));

        mTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(new IconicsDrawable(ControlPanelActivity.this)
                    .icon(GoogleMaterial.Icon.gmd_menu)
                    .color(Color.WHITE)
                    .sizeDp(20));
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navView);
        resetMenuIcons();
        setupDrawerContent();
        mNavFooter = (RelativeLayout) findViewById(R.id.footer_container);
        mNavFooter.setOnClickListener(this);

        mSelectedProvinceId = mDefaultProvinceId = getUserLocationProvinceId();
        mSelectedCityId = mDefaultCityId = getUserLocationCityId();
        mSelectedProvinceName = mDefaultProvinceName = getUserLocationProvinceName();
        mSelectedCityName = mDefaultCityName = getUserLocationCityName();

        changeLocationTitle();

        handleExternalIntent(getIntent());

        checkAndUpdateMeta();

    }


    public void changeLocationTitle() {
        if (BuildConfig.DEBUG) {
            logD("changeLocationTitle");
        }
        if (!TextUtils.isEmpty(mDefaultProvinceName) && !TextUtils.isEmpty(mDefaultCityName)) {
            Menu menu = mNavigationView.getMenu();
            menu.getItem(ITEM_IDENTIFIER_LOCATION).setTitle(mDefaultProvinceName + "، " + mDefaultCityName);
        }
    }

    private void setupDrawerContent() {
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        resetMenuIcons();
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void resetMenuIcons() {
        Menu menu = mNavigationView.getMenu();

        IconicsDrawable locationDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .icon(GoogleMaterial.Icon.gmd_place);
        menu.getItem(ITEM_IDENTIFIER_LOCATION).setIcon(locationDrawable);
        menu.getItem(ITEM_IDENTIFIER_LOCATION).setChecked(false);

        IconicsDrawable bookmarksDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .icon(GoogleMaterial.Icon.gmd_stars);
        menu.getItem(ITEM_IDENTIFIER_BOOKMARKS).setIcon(bookmarksDrawable);
        menu.getItem(ITEM_IDENTIFIER_BOOKMARKS).setChecked(false);

        IconicsDrawable contactDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .icon(GoogleMaterial.Icon.gmd_email);
        menu.getItem(ITEM_IDENTIFIER_CONTACT).setIcon(contactDrawable);
        menu.getItem(ITEM_IDENTIFIER_CONTACT).setChecked(false);

        IconicsDrawable gavelDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .icon(GoogleMaterial.Icon.gmd_gavel);
        menu.getItem(ITEM_IDENTIFIER_RULES).setIcon(gavelDrawable);
        menu.getItem(ITEM_IDENTIFIER_RULES).setChecked(false);

        IconicsDrawable aboutDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .icon(GoogleMaterial.Icon.gmd_info_outline);
        menu.getItem(ITEM_IDENTIFIER_ABOUT).setIcon(aboutDrawable);
        menu.getItem(ITEM_IDENTIFIER_ABOUT).setChecked(false);

        IconicsDrawable shareDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .icon(GoogleMaterial.Icon.gmd_share);
        menu.getItem(ITEM_IDENTIFIER_SHARE).setIcon(shareDrawable);
        menu.getItem(ITEM_IDENTIFIER_SHARE).setChecked(false);

        IconicsDrawable supportDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .icon(GoogleMaterial.Icon.gmd_star);
        menu.getItem(ITEM_IDENTIFIER_SUPPORT).setIcon(supportDrawable);
        menu.getItem(ITEM_IDENTIFIER_SUPPORT).setChecked(false);

        IconicsDrawable swapDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .icon(GoogleMaterial.Icon.gmd_swap_horiz);
        menu.getItem(ITEM_IDENTIFIER_SWAP).setIcon(swapDrawable);
        menu.getItem(ITEM_IDENTIFIER_SWAP).setChecked(false);

        IconicsDrawable exitDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_exit_to_app);
        menu.getItem(ITEM_IDENTIFIER_EXIT).setIcon(exitDrawable);
        menu.getItem(ITEM_IDENTIFIER_EXIT).setChecked(false);
    }

    public void selectDrawerItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.item_location:
                displaySetDefaultLocationDialog();
                break;
            case R.id.item_bookmarks:
                if (NetworkManager.getInstance().isNetworkEnabled(true)) {
                    refreshNoNetFragment();
                    menuItem.setChecked(true);
                    IconicsDrawable bookmarksDrawable =
                            new IconicsDrawable(ControlPanelActivity.this)
                                    .color(getResources().getColor(R.color.dark_cyan))
                                    .icon(GoogleMaterial.Icon.gmd_stars);
                    menuItem.setIcon(bookmarksDrawable);
                    BookmarksFragment bookmarksFragment = BookmarksFragment.getInstance();
                    switchFragment(bookmarksFragment, false, bookmarksFragment.getTitle(ControlPanelActivity.this));
                    mMainTabBar.deselectAllTabs();
                } else {
                    menuItem.setChecked(false);
                }
                break;

            case R.id.item_contact_us:
                if (NetworkManager.getInstance().isNetworkEnabled(true)) {
                    refreshNoNetFragment();
                    menuItem.setChecked(true);
                    IconicsDrawable contactDrawable =
                            new IconicsDrawable(ControlPanelActivity.this)
                                    .color(getResources().getColor(R.color.dark_cyan))
                                    .icon(GoogleMaterial.Icon.gmd_email);
                    menuItem.setIcon(contactDrawable);
                    ContactFragment contactFragment = ContactFragment.getInstance();
                    switchFragment(contactFragment, false, contactFragment.getTitle(ControlPanelActivity.this));
                    mMainTabBar.deselectAllTabs();
                } else {
                    menuItem.setChecked(false);
                }
                break;

            case R.id.item_rules:
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(HonarnamaBaseApp.WEB_ADDRESS));
                startActivity(i);
                break;

            case R.id.item_about_us:
                if (NetworkManager.getInstance().isNetworkEnabled(true)) {
                    refreshNoNetFragment();
                    menuItem.setChecked(true);
                    IconicsDrawable aboutDrawable =
                            new IconicsDrawable(ControlPanelActivity.this)
                                    .color(getResources().getColor(R.color.dark_cyan))
                                    .icon(GoogleMaterial.Icon.gmd_info_outline);
                    menuItem.setIcon(aboutDrawable);
                    AboutFragment aboutFragment = AboutFragment.getInstance();
                    switchFragment(aboutFragment, false, aboutFragment.getTitle(ControlPanelActivity.this));
                    mMainTabBar.deselectAllTabs();
                } else {
                    menuItem.setChecked(false);
                }
                break;

            case R.id.item_share_us:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "سلام،" + "\n" + "برای خرید محصولات دست‌ساز و دیدن رویدادهای هنری برنامه هنرنما رو از کافه بازار دانلود کن. اینم لینکش:" +
                        "\n" + "http://cafebazaar.ir/app/" + HonarnamaBrowseApp.getInstance().getPackageName() + "/");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;

            case R.id.item_support_us:
                callBazaarRatingIntent();
                break;

            case R.id.item_switch_app:
                try {
                    if (CommonUtil.isPackageInstalled(HonarnamaBaseApp.SELL_PACKAGE_NAME)) {
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("net.honarnama.sell");
                        startActivity(launchIntent);
                    } else {
                        Intent sellAppIntent = new Intent(Intent.ACTION_VIEW);
                        sellAppIntent.setData(Uri.parse("bazaar://details?id=" + "net.honarnama.sell"));
                        sellAppIntent.setPackage("com.farsitel.bazaar");
                        startActivity(sellAppIntent);
                    }
                } catch (Exception e) {
                    logE("Error switching from browse app to sell. Error: " + e, e);
                }
                break;

            case R.id.item_nav_title_exit_app:
                if (!mSharedPreferences.getBoolean(HonarnamaBaseApp.PREF_KEY_BROWSE_APP_RATED, false)) {
                    askToRate();
                } else {
                    finish();
                }
                break;

        }
        mDrawer.closeDrawer(Gravity.RIGHT);
    }

    public void removeActiveTabTopNavMenuFragment() {
        FragmentManager childFragmentManager = mMainFragmentAdapter.getItem(mActiveTab)
                .getChildFragmentManager();
        Fragment topFragment = childFragmentManager.findFragmentById(R.id.child_fragment_root);

        if (topFragment instanceof NoNetFragment) {
            return;
        }

        if (childFragmentManager.getBackStackEntryCount() > 0) {
            List<Fragment> fragments = childFragmentManager.getFragments();
//            Fragment topFragment = fragments.get(fragments.size() - 1);

            if (topFragment != null) {

                if (topFragment.getClass().getName() == mContactFragment.getClass().getName() ||
                        topFragment.getClass().getName() == mAboutFragment.getClass().getName() ||
                        topFragment.getClass().getName() == mBookmarksFragment.getClass().getName()
                        ) {
                    FragmentTransaction fragmentTransaction = childFragmentManager.beginTransaction();
                    fragmentTransaction.remove(topFragment);
                    fragmentTransaction.commitAllowingStateLoss();
                    childFragmentManager.popBackStackImmediate();
                    childFragmentManager.executePendingTransactions();
                    mTitle.setText(getString(R.string.hornama));
                }
            }
        }
    }

    public boolean isNavMenuFragment(Fragment fragment) {
        if (fragment.getClass().getName() == mContactFragment.getClass().getName() ||
                fragment.getClass().getName() == mAboutFragment.getClass().getName() ||
                fragment.getClass().getName() == mBookmarksFragment.getClass().getName()) {
            return true;
        }
        return false;
    }

    public void switchFragment(Fragment fragment, boolean isExternal, String toolbarTitle) {
        WindowUtil.hideKeyboard(ControlPanelActivity.this);
        try {

            FragmentManager childFragmentManager = mMainFragmentAdapter.getItem(mActiveTab)
                    .getChildFragmentManager();
            Fragment topFragment = childFragmentManager.findFragmentById(R.id.child_fragment_root);


            if (!(NetworkManager.getInstance().isNetworkEnabled(true))) {
                if (topFragment instanceof NoNetFragment) {
                    resetMenuIcons();
                    return;
                }
            }
            removeActiveTabTopNavMenuFragment();

            if (fragment.isAdded()) {
                return;
            }

            FragmentTransaction fragmentTransaction = childFragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.child_fragment_root, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commitAllowingStateLoss();
            if (childFragmentManager != null) {
                childFragmentManager.executePendingTransactions();
            }
            if (!TextUtils.isEmpty(toolbarTitle)) {
                mTitle.setText(toolbarTitle);
            }
            if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                if (!(fragment instanceof NoNetFragment)) {
                    switchToNoNetFragment();
                }
            }
        } catch (Exception e) {
            logE("Exception While Switching Fragments in CPA." + e, e);
        }

    }

    public void displayShopPage(long shopId, boolean isExternal) {
        setShopId(shopId);
//        mMainTabBar.deselectAllTabs();
        switchFragment(ShopPageFragment.getInstance(shopId), isExternal, getResources().getString(R.string.art_shop));
    }

    public void displayEventPage(long eventId, boolean isExternal) {
        setEventId(eventId);
//        mMainTabBar.deselectAllTabs();
        switchFragment(EventPageFragment.getInstance(eventId), isExternal, getResources().getString(R.string.art_event));
    }

    public void displayItemPage(long itemId, boolean isExternal) {
        setItemId(itemId);
        switchFragment(ItemPageFragment.getInstance(itemId), isExternal, "مشاهده محصول");
        //TODO SET ITEM CATEGORY AS TITLE
//        mTitle.setText();
    }

    public void setShopId(long shopId) {
        mShopId = shopId;
    }

    public void setEventId(long eventId) {
        mEventId = eventId;
    }

    public void setItemId(long itemId) {
        mItemId = itemId;
    }


    @Override
    public void onTabSelect(Object tabTag, boolean userTriggered) {
        WindowUtil.hideKeyboard(ControlPanelActivity.this);
        int tag = (Integer) tabTag;
        removeActiveTabTopNavMenuFragment();
        if (mActiveTab == Integer.valueOf(TAB_SEARCH) && tag != Integer.valueOf(TAB_SEARCH)) {
            SearchFragment searchFragment = (SearchFragment) mMainFragmentAdapter.getDefaultFragmentForTab(TAB_SEARCH);
            searchFragment.resetFields();
        }
        resetMenuIcons();
        mActiveTab = tag;

        switch (tag) {
            case TAB_ITEMS:
                mViewPager.setCurrentItem(TAB_ITEMS, false);
                break;
            case TAB_EVENTS:
                mViewPager.setCurrentItem(TAB_EVENTS, false);
                break;
            case TAB_SHOPS:
                mViewPager.setCurrentItem(TAB_SHOPS, false);
                break;
            case TAB_SEARCH:
                mViewPager.setCurrentItem(TAB_SEARCH, false);
                break;
            default:
                mViewPager.setCurrentItem(TAB_ITEMS, false);
                break;
        }
        mMainFragmentAdapter.getItem(tag).onTabClick();
    }


    @Override
    public void onSelectedTabClick(Object tabTag, boolean byUser) {
        FragmentManager childFragmentManager = mMainFragmentAdapter.getItem(mActiveTab)
                .getChildFragmentManager();
        if (childFragmentManager.getBackStackEntryCount() > 0) {
            HonarnamaBaseFragment topFragment = (HonarnamaBaseFragment) childFragmentManager.findFragmentById(R.id.child_fragment_root);
            if (topFragment instanceof NoNetFragment) {
                if (NetworkManager.getInstance().isNetworkEnabled(false)) {
                    refreshNoNetFragment();
                    return;
                } else {
                    return;
                }
            }

        }

        removeActiveTabTopNavMenuFragment();
        resetMenuIcons();

        mMainTabBar.selectTabViewWithTabTag(tabTag);
        WindowUtil.hideKeyboard(ControlPanelActivity.this);
        int tag = (int) tabTag;
        mMainFragmentAdapter.getItem(tag).onSelectedTabClick();

    }

    public void setDefaultTab() {
        // Fetch the selected tab index with default
        int selectedTabIndex = getIntent().getIntExtra(SELECTED_TAB_EXTRA_KEY, TAB_ITEMS);
        // Switch to page based on index
        mViewPager.setCurrentItem(selectedTabIndex);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
//        menu.findItem(R.id.search).setIcon(new IconicsDrawable(ControlPanelActivity.this)
//                .icon(GoogleMaterial.Icon.gmd_search)
//                .color(Color.WHITE)
//                .sizeDp(20)).setTitleCondensed(getString(R.string.search));
        return true;
    }


    @Override
    public void onBackPressed() {

        if (mDrawer.isDrawerOpen(Gravity.RIGHT)) {
            mDrawer.closeDrawer(Gravity.RIGHT);
            return;
        }

        mMainTabBar.selectTabViewWithTabTag(mActiveTab);
        resetMenuIcons();
        if (!mMainFragmentAdapter.getItem(mActiveTab).back()) {
            if (mActiveTab != TAB_ITEMS) {
                mMainTabBar.setSelectedTab(TAB_ITEMS);
            } else {

                final ConfirmationDialog confirmationDialog = new ConfirmationDialog(ControlPanelActivity.this,
                        getString(R.string.exit_app_title),
                        getString(R.string.exit_app_confirmation)
                );
                confirmationDialog.showDialog(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!HonarnamaBaseApp.getAppSharedPref().getBoolean(HonarnamaBaseApp.PREF_KEY_BROWSE_APP_RATED, false)) {
                            askToRate();
                        } else {
                            finish();
                        }
                        confirmationDialog.dismiss();
                    }
                });

            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
        handleExternalIntent(intent);
    }

    private void processIntent(Intent intent) {
        Uri data = intent.getData();
        if (BuildConfig.DEBUG) {
            logD("processIntent :: data= " + data);
        }

        if (data != null) {
            if (intent.getAction().equals(Intent.ACTION_VIEW)) {
                List<String> segments = data.getPathSegments();
                if (segments.size() > 1 && segments.get(0).equals("shop")) {
                    int shopId = Integer.valueOf(segments.get(1).replace("/", ""));
                    mMainTabBar.setSelectedTab(TAB_SHOPS);
                    displayShopPage(shopId, true);
                    return;
                }

                if (segments.size() > 1 && segments.get(0).equals("event")) {
                    int eventId = Integer.valueOf(segments.get(1).replace("/", ""));
                    mMainTabBar.setSelectedTab(TAB_EVENTS);
                    displayEventPage(eventId, true);
                    return;
                }

                if (segments.size() > 1 && segments.get(0).equals("item")) {
                    int itemId = Integer.valueOf(segments.get(1).replace("/", ""));
                    mMainTabBar.setSelectedTab(TAB_ITEMS);
                    displayItemPage(itemId, true);
                    return;
                }
            }
        }
    }

    public void handleExternalIntent(final Intent intent) {
        findViewById(R.id.tab_bar).getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @SuppressLint("NewApi")
                    @SuppressWarnings("deprecation")
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            findViewById(R.id.tab_bar).getViewTreeObserver()
                                    .removeGlobalOnLayoutListener(this);
                        } else {
                            findViewById(R.id.tab_bar).getViewTreeObserver()
                                    .removeOnGlobalLayoutListener(this);
                        }
                        processIntent(intent);
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawer.isDrawerOpen(Gravity.RIGHT)) {
                    mDrawer.closeDrawer(Gravity.RIGHT);
                } else {
                    mDrawer.openDrawer(Gravity.RIGHT);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.footer_container:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(HonarnamaBaseApp.WEB_ADDRESS)));
                break;

            case R.id.province_edit_text:
                displayProvinceDialog();
                break;

            case R.id.city_edit_text:
                displayCityDialog();
                break;

            case R.id.refetchProvinces:
            case R.id.refetchCities:
                if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                    break;
                }
                checkAndUpdateMeta();
                mRefetchProvinces.setVisibility(View.GONE);
                mRefetchCities.setVisibility(View.GONE);
                fetchProvincesAndCities();
                break;

        }
    }

    public void switchToNoNetFragment() {
        NoNetFragment noNetFragment = NoNetFragment.getInstance();
        switchFragment(noNetFragment, false, noNetFragment.getTitle(ControlPanelActivity.this));
    }

    public void refreshNoNetFragment() {

        FragmentManager childFragmentManager = mMainFragmentAdapter.getItem(mActiveTab)
                .getChildFragmentManager();
        if (childFragmentManager != null) {
            childFragmentManager.executePendingTransactions();
        }


        HonarnamaBaseFragment topFragment = (HonarnamaBaseFragment) childFragmentManager.findFragmentById(R.id.child_fragment_root);

        if (childFragmentManager.getBackStackEntryCount() > 0) {
//            fragmentTransaction.remove(noNetFragment);
//            fragmentTransaction.commit();
            if (topFragment instanceof NoNetFragment) {
                try {
                    childFragmentManager.popBackStack();
//                    fragmentTransaction.remove(topFragment);
//                    fragmentTransaction.commitAllowingStateLoss();
                    if (childFragmentManager != null) {
                        childFragmentManager.executePendingTransactions();
                    }
                } catch (Exception e) {
                    logE("Error refreshing NoNetFragment fragment while popping back stack. Error: " + e, e);
                }

            }
        }

        refreshTopFragment();
    }

    public void refreshTopFragment() {
        FragmentManager childFragmentManager = mMainFragmentAdapter.getItem(mActiveTab)
                .getChildFragmentManager();
        FragmentTransaction fragmentTransaction = childFragmentManager.beginTransaction();
        HonarnamaBaseFragment topFragment = (HonarnamaBaseFragment) childFragmentManager.findFragmentById(R.id.child_fragment_root);

        if (topFragment != null && !(topFragment instanceof NoNetFragment)) {
            fragmentTransaction.detach(topFragment);
            fragmentTransaction.attach(topFragment);
            fragmentTransaction.commitAllowingStateLoss();
            mTitle.setText(topFragment.getTitle(ControlPanelActivity.this));
            if (childFragmentManager != null) {
                childFragmentManager.executePendingTransactions();
            }
        }
    }


    public void setTitle(String title) {
        mTitle.setText(title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void displaySetDefaultLocationDialog() {

        mSetDefaultLocationDialog = new Dialog(ControlPanelActivity.this, R.style.CustomDialogTheme);
        mSetDefaultLocationDialog.setCancelable(true);
        mSetDefaultLocationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSetDefaultLocationDialog.setContentView(R.layout.set_default_location);

        mDefaultProvinceEditText = (EditText) mSetDefaultLocationDialog.findViewById(R.id.province_edit_text);
        mDefaultProvinceEditText.setOnClickListener(this);
        mDefaultProvinceEditText.setKeyListener(null);

        mDefaultCityEditText = (EditText) mSetDefaultLocationDialog.findViewById(R.id.city_edit_text);
        mDefaultCityEditText.setOnClickListener(this);
        mDefaultCityEditText.setKeyListener(null);

        mRefetchProvinces = (IconicsImageView) mSetDefaultLocationDialog.findViewById(R.id.refetchProvinces);
        mRefetchProvinces.setOnClickListener(this);

        mRefetchCities = (IconicsImageView) mSetDefaultLocationDialog.findViewById(R.id.refetchCities);
        mRefetchCities.setOnClickListener(this);

        fetchProvincesAndCities();

        Button registerLocationBtn = (Button) mSetDefaultLocationDialog.findViewById(R.id.register_location_btn);
        Button bikhialBtn = (Button) mSetDefaultLocationDialog.findViewById(R.id.bikhial_btn);
        registerLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                    return;
                }
                if ((mSelectedProvinceId > 0) && mSelectedCityId > 0) {
                    mDefaultProvinceId = mSelectedProvinceId;
                    mDefaultProvinceName = mSelectedProvinceName;
                    mDefaultCityId = mSelectedCityId;
                    mDefaultCityName = mSelectedCityName;
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putInt(HonarnamaBaseApp.PREF_KEY_DEFAULT_LOCATION_PROVINCE_ID, mDefaultProvinceId);
                    editor.putString(HonarnamaBaseApp.PREF_KEY_DEFAULT_LOCATION_PROVINCE_NAME, mDefaultProvinceName);
                    editor.putInt(HonarnamaBaseApp.PREF_KEY_DEFAULT_LOCATION_CITY_ID, mDefaultCityId);
                    editor.putString(HonarnamaBaseApp.PREF_KEY_DEFAULT_LOCATION_CITY_NAME, mDefaultCityName);
                    editor.commit();
                    changeLocationTitle();
                    mSetDefaultLocationDialog.dismiss();
                } else {
                    Toast.makeText(ControlPanelActivity.this, "استان یا شهر انتخاب نشده است!", Toast.LENGTH_LONG).show();
                }
            }
        });
        bikhialBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSetDefaultLocationDialog.dismiss();
            }
        });

        // TODO:
        // GRPCUtils.getInstance().MakeSureMetaExists()

        mSetDefaultLocationDialog.show();
    }

    private void displayProvinceDialog() {

        ListView provincesListView;
        ProvincesAdapter provincesAdapter;

        final Dialog provinceDialog = new Dialog(ControlPanelActivity.this, R.style.DialogStyle);

        provinceDialog.setContentView(R.layout.choose_province);

        provincesListView = (ListView) provinceDialog.findViewById(net.honarnama.base.R.id.provinces_list_view);
        provincesAdapter = new ProvincesAdapter(ControlPanelActivity.this, mProvincesTreeMap);
        provincesListView.setAdapter(provincesAdapter);

        provincesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedProvince = mProvincesTreeMap.get(position + 1);
                mSelectedProvinceId = mSelectedProvince.getId();
                mSelectedProvinceName = mSelectedProvince.getName();
                mDefaultProvinceEditText.setText(mSelectedProvinceName);
                rePopulateCityList();
                if (provinceDialog.isShowing()) {
                    provinceDialog.dismiss();
                }
            }
        });
        provinceDialog.setCancelable(true);
        provinceDialog.setTitle(getString(R.string.select_province));
        provinceDialog.show();
    }

    private void rePopulateCityList() {
        City city = new City();
        city.getAllCitiesSorted(mSelectedProvinceId).continueWith(new Continuation<TreeMap<Number, HashMap<Integer, String>>, Object>() {
            @Override
            public Object then(Task<TreeMap<Number, HashMap<Integer, String>>> task) throws Exception {
                if (task.isFaulted()) {

                    mRefetchProvinces.setVisibility(View.VISIBLE);
                    mRefetchCities.setVisibility(View.VISIBLE);
                    mDefaultCityEditText.setHint(ControlPanelActivity.this.getString(R.string.error_getting_info));

                    if ((mSetDefaultLocationDialog.isShowing())) {
                        Toast.makeText(ControlPanelActivity.this, getString(R.string.error_getting_city_list) + getString(R.string.check_net_connection), Toast.LENGTH_LONG).show();
                    }
                } else {
                    mCityTreeMap = task.getResult();
                    for (HashMap<Integer, String> cityMap : mCityTreeMap.values()) {
                        for (Map.Entry<Integer, String> citySet : cityMap.entrySet()) {
                            mCityHashMap.put(citySet.getKey(), citySet.getValue());
                        }
                    }

                    Set<Integer> tempSet = mCityTreeMap.get(1).keySet();
                    for (int key : tempSet) {
                        mSelectedCityId = key;
                        mSelectedCityName = mCityHashMap.get(key);
                        mDefaultCityEditText.setText(mSelectedCityName);
                    }
                }
                return null;
            }
        });
    }


    private void displayCityDialog() {
        ListView cityListView;
        final CityAdapter cityAdapter;

        final Dialog cityDialog = new Dialog(ControlPanelActivity.this, R.style.DialogStyle);
        cityDialog.setContentView(R.layout.choose_city);
        cityListView = (ListView) cityDialog.findViewById(net.honarnama.base.R.id.city_list_view);

        cityAdapter = new CityAdapter(ControlPanelActivity.this, mCityTreeMap);
        cityListView.setAdapter(cityAdapter);
        cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                HashMap<Integer, String> selectedCity = mCityTreeMap.get(position + 1);
                for (int key : selectedCity.keySet()) {
                    mSelectedCityId = key;
                }
                for (String value : selectedCity.values()) {
                    mSelectedCityName = value;
                    mDefaultCityEditText.setText(mSelectedCityName);
                }

                if (cityDialog.isShowing()) {
                    cityDialog.dismiss();
                }
            }
        });
        cityDialog.setCancelable(true);
        cityDialog.setTitle(getString(R.string.select_city));
        cityDialog.show();
    }


    public void fetchProvincesAndCities() {

        final Province provinces = new Province();
        final City city = new City();

        mDefaultProvinceEditText.setHint(getString(R.string.select));
        mDefaultCityEditText.setHint(City.ALL_CITY_NAME);

        provinces.getAllProvincesSorted().
                continueWith(new Continuation<TreeMap<Number, Province>, Object>() {
                    @Override
                    public Object then(Task<TreeMap<Number, Province>> task) throws Exception {
                        if (task.isFaulted()) {
                            mRefetchProvinces.setVisibility(View.VISIBLE);
                            mRefetchCities.setVisibility(View.VISIBLE);
                            mDefaultProvinceEditText.setHint(ControlPanelActivity.this.getString(R.string.error_getting_info));
                            logE("Getting Province Task Failed. Msg: " + task.getError().getMessage() + " // Error: " + task.getError(), task.getError());
                            Toast.makeText(ControlPanelActivity.this, getString(R.string.error_getting_province_list) + getString(R.string.check_net_connection), Toast.LENGTH_SHORT).show();
                        } else {
                            mProvincesTreeMap = task.getResult();
                            for (Province province : mProvincesTreeMap.values()) {
                                mProvincesHashMap.put(province.getId(), province.getName());
                            }
                            mDefaultProvinceEditText.setText(mProvincesHashMap.get(mDefaultProvinceId));
                        }
                        return null;
                    }
                }).continueWithTask(new Continuation<Object, Task<TreeMap<Number, HashMap<Integer, String>>>>() {
            @Override
            public Task<TreeMap<Number, HashMap<Integer, String>>> then(Task<Object> task) throws Exception {
                return city.getAllCitiesSorted(mDefaultProvinceId);
            }
        }).continueWith(new Continuation<TreeMap<Number, HashMap<Integer, String>>, Object>() {
            @Override
            public Object then(Task<TreeMap<Number, HashMap<Integer, String>>> task) throws Exception {

                if (task.isFaulted() && mSelectedProvinceId > 0) {
                    mRefetchProvinces.setVisibility(View.VISIBLE);
                    mRefetchCities.setVisibility(View.VISIBLE);
                    mDefaultCityEditText.setHint(ControlPanelActivity.this.getString(R.string.error_getting_info));
                    logE("Getting City List Task Failed. Msg: " + task.getError().getMessage() + "//  Error: " + task.getError(), task.getError());
                    Toast.makeText(ControlPanelActivity.this, getString(R.string.error_getting_city_list) + getString(R.string.check_net_connection), Toast.LENGTH_SHORT).show();
                } else {
                    mCityTreeMap = task.getResult();

                    for (HashMap<Integer, String> cityMap : mCityTreeMap.values()) {
                        for (Map.Entry<Integer, String> citySet : cityMap.entrySet()) {
                            mCityHashMap.put(citySet.getKey(), citySet.getValue());
                        }
                    }

                    mDefaultCityEditText.setText(mCityHashMap.get(mDefaultCityId));

                }

                return null;
            }
        });

    }

}