package net.honarnama.browse.activity;

import net.honarnama.browse.R;
import net.honarnama.browse.model.MainFragmentAdapter;
import net.honarnama.browse.widget.NonSwipeableViewPager;
import net.honarnama.core.activity.HonarnamaBaseActivity;

import android.app.Fragment;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost;

public class ControlPanelActivity extends HonarnamaBaseActivity {

    public static Button btnRed; // Works as a badge
    //Declared static; so it can be accessed from all other Activities

    MainFragmentAdapter mMainFragmentAdapter;
    NonSwipeableViewPager mViewPager;
    private Toolbar mToolbar;

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

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
//        mToolbar.setTitle(R.string.toolbar_title);
        setSupportActionBar(mToolbar);
    }
}