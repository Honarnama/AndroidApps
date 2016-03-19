package net.honarnama.browse.activity;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.adapter.MainFragmentAdapter;
import net.honarnama.browse.fragment.BookmarksFragment;
import net.honarnama.browse.fragment.ChildFragment;
import net.honarnama.browse.fragment.EventPageFragment;
import net.honarnama.browse.fragment.ItemPageFragment;
import net.honarnama.browse.fragment.NoNetFragment;
import net.honarnama.browse.fragment.SearchFragment;
import net.honarnama.browse.fragment.ShopPageFragment;
import net.honarnama.browse.widget.LockableViewPager;
import net.honarnama.browse.widget.MainTabBar;
import net.honarnama.core.fragment.AboutFragment;
import net.honarnama.core.fragment.ContactFragment;
import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.core.utils.CommonUtil;
import net.honarnama.core.utils.HonarnamaUser;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.core.utils.WindowUtil;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
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
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import static net.honarnama.browse.widget.MainTabBar.TAB_EVENTS;
import static net.honarnama.browse.widget.MainTabBar.TAB_ITEMS;
import static net.honarnama.browse.widget.MainTabBar.TAB_SEARCH;
import static net.honarnama.browse.widget.MainTabBar.TAB_SHOPS;

public class ControlPanelActivity extends HonarnamaBrowseActivity implements MainTabBar.OnTabItemClickListener, View.OnClickListener {

    public static Button btnRed; // Works as a badge
    //Declared static; so it can be accessed from all other Activities

    MainFragmentAdapter mMainFragmentAdapter;
    LockableViewPager mViewPager;
    private Toolbar mToolbar;
    private int mActiveTab;
    private MainTabBar mMainTabBar;
    private String mShopId;
    private String mEventId;
    private String mItemId;
    public TextView mTitle;

    private DrawerLayout mDrawer;
    public NavigationView mNavigationView;

    public ContactFragment mContactFragment;
    public AboutFragment mAboutFragment;
    public BookmarksFragment mBookmarksFragment;

    public RelativeLayout mNavFooter;

    public static final int ITEM_IDENTIFIER_BOOKMARKS = 0;
    public static final int ITEM_IDENTIFIER_CONTACT = 1;
    public static final int ITEM_IDENTIFIER_RULES = 2;
    public static final int ITEM_IDENTIFIER_ABOUT = 3;
    public static final int ITEM_IDENTIFIER_SHARE = 4;
    public static final int ITEM_IDENTIFIER_SUPPORT = 5;
    public static final int ITEM_IDENTIFIER_SWAP = 6;
    public static final int ITEM_IDENTIFIER_EXIT = 7;

    SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
//        LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPreferences = getSharedPreferences(HonarnamaBaseApp.BROWSE_APP_KEY, Context.MODE_PRIVATE);

        mContactFragment = ContactFragment.getInstance(HonarnamaBaseApp.BROWSE_APP_KEY);
        mAboutFragment = AboutFragment.getInstance(HonarnamaBaseApp.BROWSE_APP_KEY);
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

        handleExternalIntent(getIntent());

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
        mMainTabBar.deselectAllTabs();
        switch (menuItem.getItemId()) {

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
                    ContactFragment contactFragment = ContactFragment.getInstance(HonarnamaBaseApp.BROWSE_APP_KEY);
                    switchFragment(contactFragment, false, contactFragment.getTitle(ControlPanelActivity.this));
                    break;
                } else {
                    menuItem.setChecked(false);
                }
            case R.id.item_rules:
                String url = "http://www.honarnama.net/terms";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
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
                    AboutFragment aboutFragment = AboutFragment.getInstance(HonarnamaBaseApp.BROWSE_APP_KEY);
                    switchFragment(aboutFragment, false, aboutFragment.getTitle(ControlPanelActivity.this));
                    break;
                } else {
                    menuItem.setChecked(false);
                }

            case R.id.item_share_us:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "سلام،" + "\n" + "برای خرید محصولات دست‌ساز و دیدن رویدادهای هنری برنامه هنرنما رو از کافه بازار دانلود کن. اینم لینکش:" +
                        "\n" + "http://cafebazaar.ir/app/" + HonarnamaBrowseApp.getInstance().getPackageName() + "/");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;

            case R.id.item_support_us:
                callBazaarRateIntent();
                break;

            case R.id.item_switch_app:
                try {
                    if (CommonUtil.isPackageInstalled("net.honarnama.sell", ControlPanelActivity.this)) {
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("net.honarnama.sell");
                        startActivity(launchIntent);
                    } else {
                        Intent sellAppIntent = new Intent(Intent.ACTION_VIEW);
                        sellAppIntent.setData(Uri.parse("bazaar://details?id=" + "net.honarnama.sell"));
                        sellAppIntent.setPackage("com.farsitel.bazaar");
                        startActivity(sellAppIntent);
                    }
                } catch (Exception e) {
                    logE("Error switching from browse app to sell. Error: " + e);
                }
                break;

            case R.id.item_nav_title_exit_app:
                if (!mSharedPreferences.getBoolean(HonarnamaBaseApp.EXTRA_KEY_BROWSE_APP_RATED, false)) {
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
                    logE("no net , top frag is no net");
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
            logE("Exception While Switching Fragments in CPA." + e);
        }

    }

    public void displayShopPage(String shopId, boolean isExternal) {
        setShopId(shopId);
//        mMainTabBar.deselectAllTabs();
        switchFragment(ShopPageFragment.getInstance(shopId), isExternal, getResources().getString(R.string.art_shop));
    }

    public void displayEventPage(String eventId, boolean isExternal) {
        setEventId(eventId);
//        mMainTabBar.deselectAllTabs();
        switchFragment(EventPageFragment.getInstance(eventId), isExternal, getResources().getString(R.string.art_event));
    }

    public void displayItemPage(String itemId, boolean isExternal) {
        setItemId(itemId);
        switchFragment(ItemPageFragment.getInstance(itemId), isExternal, "مشاهده محصول");
        //TODO SET ITEM CATEGORY AS TITLE
//        mTitle.setText();
    }

    public void setShopId(String shopId) {
        mShopId = shopId;
    }

    public void setEventId(String eventId) {
        mEventId = eventId;
    }

    public void setItemId(String itemId) {
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
                if (!mSharedPreferences.getBoolean(HonarnamaBaseApp.EXTRA_KEY_BROWSE_APP_RATED, false)) {
                    askToRate();
                } else {
                    finish();
                }
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
                    String shopId = segments.get(1).replace("/", "");
                    mMainTabBar.setSelectedTab(TAB_SHOPS);
                    displayShopPage(shopId, true);
                    return;
                }

                if (segments.size() > 1 && segments.get(0).equals("event")) {
                    String eventId = segments.get(1).replace("/", "");
                    mMainTabBar.setSelectedTab(TAB_EVENTS);
                    displayEventPage(eventId, true);
                    return;
                }

                if (segments.size() > 1 && segments.get(0).equals("item")) {
                    String itemId = segments.get(1).replace("/", "");
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
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.honarnama.net")));
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
                    logE("inja " + e);
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


    public void askToRate() {
        final Dialog dialog = new Dialog(ControlPanelActivity.this, R.style.CustomDialogTheme);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.ask_for_starts);
        Button letsRateBtn = (Button) dialog.findViewById(R.id.lets_rate);
        Button rateLaterBtn = (Button) dialog.findViewById(R.id.rate_later);
        letsRateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBazaarRateIntent();
                //TODO inja set intent ke ray dade dige neshon nade dialogo

                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean(HonarnamaBaseApp.EXTRA_KEY_BROWSE_APP_RATED, true);
                editor.commit();

                dialog.dismiss();
                finish();
            }
        });
        rateLaterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });
        dialog.show();
    }

    public void callBazaarRateIntent() {
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setData(Uri.parse("bazaar://details?id=" + HonarnamaBrowseApp.getInstance().getPackageName()));
        intent.setPackage("com.farsitel.bazaar");
        startActivity(intent);
    }
}