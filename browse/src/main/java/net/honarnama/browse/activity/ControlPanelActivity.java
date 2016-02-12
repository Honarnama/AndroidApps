package net.honarnama.browse.activity;

import net.honarnama.browse.R;
import net.honarnama.browse.fragment.ChildFragment;
import net.honarnama.browse.model.MainFragmentAdapter;
import net.honarnama.browse.widget.MainTabBar;
import net.honarnama.browse.widget.NonSwipeableViewPager;
import net.honarnama.core.activity.HonarnamaBaseActivity;
import net.honarnama.core.utils.WindowUtil;

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

public class ControlPanelActivity extends HonarnamaBaseActivity implements MainTabBar.OnTabItemClickListener {

    public static Button btnRed; // Works as a badge
    //Declared static; so it can be accessed from all other Activities

    MainFragmentAdapter mMainFragmentAdapter;
    NonSwipeableViewPager mViewPager;
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

        mViewPager = (NonSwipeableViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(mMainFragmentAdapter);

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
//        mToolbar.setTitle(R.string.toolbar_title);
        setSupportActionBar(mToolbar);
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
    public void onTabSelect(Object tabTag, boolean byUser) {
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
}