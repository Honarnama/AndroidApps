package net.honarnama.browse.activity;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.honarnama.browse.R;
import net.honarnama.browse.fragment.ChildFragment;
import net.honarnama.browse.model.MainFragmentAdapter;
import net.honarnama.browse.widget.MainTabBar;
import net.honarnama.browse.widget.LockableViewPager;
import net.honarnama.core.activity.HonarnamaBaseActivity;
import net.honarnama.core.utils.WindowUtil;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.widget.Button;


import static net.honarnama.browse.widget.MainTabBar.TAB_CATS;
import static net.honarnama.browse.widget.MainTabBar.TAB_FAVS;
import static net.honarnama.browse.widget.MainTabBar.TAB_HOME;
import static net.honarnama.browse.widget.MainTabBar.TAB_SHOPS;

public class ControlPanelActivity extends HonarnamaBrowseActivity implements MainTabBar.OnTabItemClickListener {

    public static Button btnRed; // Works as a badge
    //Declared static; so it can be accessed from all other Activities

    MainFragmentAdapter mMainFragmentAdapter;
    LockableViewPager mViewPager;
    private Toolbar mToolbar;
    private int mActiveTab;
    private MainTabBar mMainTabBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mMainFragmentAdapter =
                new MainFragmentAdapter(
                        getSupportFragmentManager());

        mViewPager = (LockableViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(mMainFragmentAdapter);
        mViewPager.setSwipeable(false);

        mViewPager.setOffscreenPageLimit(4);
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
        mToolbar.setTitle(R.string.hornama);

        setDefaultTab();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setLogo(new IconicsDrawable(ControlPanelActivity.this)
                .icon(GoogleMaterial.Icon.gmd_menu)
                .color(Color.WHITE)
                .sizeDp(24));

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
            logE("Exception While Switching Fragments in CPA.");
        }
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
}