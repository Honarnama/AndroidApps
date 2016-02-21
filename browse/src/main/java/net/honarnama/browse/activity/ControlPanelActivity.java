package net.honarnama.browse.activity;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.honarnama.browse.R;
import net.honarnama.browse.adapter.MainFragmentAdapter;
import net.honarnama.browse.fragment.ChildFragment;
import net.honarnama.browse.fragment.ShopPageFragment;
import net.honarnama.browse.widget.MainTabBar;
import net.honarnama.browse.widget.LockableViewPager;
import net.honarnama.core.utils.WindowUtil;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;


import static net.honarnama.browse.widget.MainTabBar.TAB_CATS;
import static net.honarnama.browse.widget.MainTabBar.TAB_FAVS;
import static net.honarnama.browse.widget.MainTabBar.TAB_HOME;
import static net.honarnama.browse.widget.MainTabBar.TAB_SHOPS;
import static net.honarnama.browse.widget.MainTabBar.DUMMY_TAB_SHOP_PAGE;

public class ControlPanelActivity extends HonarnamaBrowseActivity implements MainTabBar.OnTabItemClickListener {

    public static Button btnRed; // Works as a badge
    //Declared static; so it can be accessed from all other Activities

    MainFragmentAdapter mMainFragmentAdapter;
    LockableViewPager mViewPager;
    private Toolbar mToolbar;
    private int mActiveTab;
    private MainTabBar mMainTabBar;
    private String mShopId;
    public  TextView mTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
//        LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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
                    switchFragment(mMainFragmentAdapter.getDefaultFragmentForTab(position));
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        getSupportActionBar().setLogo(new IconicsDrawable(ControlPanelActivity.this)
                .icon(GoogleMaterial.Icon.gmd_menu)
                .color(Color.WHITE)
                .sizeDp(20));

    }

    public void switchFragment(Fragment fragment) {
        FragmentManager childFragmentManager = mMainFragmentAdapter.getItem(mActiveTab)
                .getChildFragmentManager();
        WindowUtil.hideKeyboard(ControlPanelActivity.this);
        try {
            FragmentTransaction fragmentTransaction = childFragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.child_fragment_root, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commitAllowingStateLoss();
        } catch (Exception e) {
            logE("Exception While Switching Fragments in CPA." + e);
        }
    }

    public void displayShopPage(String shopId) {
        setShopId(shopId);
//        mMainTabBar.deselectAllTabs();
        switchFragment(ShopPageFragment.getInstance(shopId));
        mTitle.setText("غرفه هنری");
    }

    public void setShopId(String shopId) {
        mShopId = shopId;
    }

    public String getShopId() {
        return mShopId;
    }

    @Override
    public void onTabSelect(Object tabTag, boolean userTriggered) {
        WindowUtil.hideKeyboard(ControlPanelActivity.this);
        int tag = (Integer) tabTag;
        mActiveTab = tag;
        switch (tag) {
            case TAB_HOME:
                mViewPager.setCurrentItem(TAB_HOME, false);
                break;
            case TAB_CATS:
                mViewPager.setCurrentItem(TAB_CATS, false);
                break;
            case TAB_SHOPS:
                mViewPager.setCurrentItem(TAB_SHOPS, false);
                break;
            case TAB_FAVS:
                mViewPager.setCurrentItem(TAB_FAVS, false);
                break;
            default:
                mViewPager.setCurrentItem(TAB_HOME, false);
                break;
        }
        mMainFragmentAdapter.getItem(tag).onTabClick();
    }

    @Override
    public void onSelectedTabClick(Object tabTag, boolean byUser) {
        WindowUtil.hideKeyboard(ControlPanelActivity.this);
        int tag = (int) tabTag;
        mMainFragmentAdapter.getItem(tag).onSelectedTabClick();
    }

    public void setDefaultTab() {
        // Fetch the selected tab index with default
        int selectedTabIndex = getIntent().getIntExtra(SELECTED_TAB_EXTRA_KEY, TAB_HOME);
        // Switch to page based on index
        mViewPager.setCurrentItem(selectedTabIndex);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.search).setIcon(new IconicsDrawable(ControlPanelActivity.this)
                .icon(GoogleMaterial.Icon.gmd_search)
                .color(Color.WHITE)
                .sizeDp(20));
        return true;
    }


    @Override
    public void onBackPressed() {
        if (!mMainFragmentAdapter.getItem(mActiveTab).back()) {
            if (mActiveTab != TAB_HOME) {
                mMainTabBar.setSelectedTab(TAB_HOME);
            } else {
                finish();
            }
        }
    }
}