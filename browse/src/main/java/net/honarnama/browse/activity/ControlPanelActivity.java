package net.honarnama.browse.activity;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import net.honarnama.browse.R;
import net.honarnama.core.activity.HonarnamaBaseActivity;
import net.honarnama.browse.fragments.HomeFragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;

public class ControlPanelActivity extends HonarnamaBaseActivity implements Drawer.OnDrawerItemClickListener {


    public static final int DRAWER_ITEM_IDENTIFIER_ACCOUNT = 1;
    public static final int DRAWER_ITEM_IDENTIFIER_STORE_INFO = 2;
    public static final int DRAWER_ITEM_IDENTIFIER_ITEMS = 3;
    public static final int DRAWER_ITEM_IDENTIFIER_EDIT_ITEM = 4;
    public static final int DRAWER_ITEM_IDENTIFIER_ORDERS = 5;
    public static final int DRAWER_ITEM_IDENTIFIER_EXIT = 6;

    private Toolbar mToolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private Fragment mFragment;

    private Drawer mResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        mToolbar = (Toolbar) findViewById(R.id.browse_toolbar);
        mToolbar.setTitle(R.string.toolbar_title);
        setSupportActionBar(mToolbar);

        mResult = new DrawerBuilder().withActivity(this)
                .withDrawerGravity(Gravity.RIGHT)
                .withRootView(R.id.browse_drawer_container)
                .withToolbar(mToolbar)
                .withActionBarDrawerToggleAnimated(true)
                .withSelectedItem(-1)
                .withTranslucentStatusBar(false)
                .addDrawerItems(
                        new SecondaryDrawerItem().withName(R.string.nav_title_main_page).
                                withIcon(GoogleMaterial.Icon.gmd_home).withIdentifier(DRAWER_ITEM_IDENTIFIER_ACCOUNT),
                        new SecondaryDrawerItem().withName(R.string.nav_title_login).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_STORE_INFO).withIcon(GoogleMaterial.Icon.gmd_sign_in),
                        new SecondaryDrawerItem().withName(R.string.nav_title_sign_up).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_STORE_INFO).withIcon(GoogleMaterial.Icon.gmd_account),

                        new DividerDrawerItem().withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.nav_title_switch_to_exhibitors_app).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_EXIT).withIcon(GoogleMaterial.Icon.gmd_search_in_page)
                )
                .withOnDrawerItemClickListener(this)
                .build();

        mDrawerToggle = new ActionBarDrawerToggle(this, mResult.getDrawerLayout(), null, R.string.drawer_open, R.string.drawer_close) {
        };

        mResult.getDrawerLayout().post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setElevation(0);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mResult.setActionBarDrawerToggle(mDrawerToggle);
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

        HomeFragment fragment = HomeFragment.getInstance();

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.browse_frame_container, fragment);
        fragmentTransaction.commit();

        getSupportActionBar().setTitle(fragment.getTitle(this));

        return false;
    }
}
