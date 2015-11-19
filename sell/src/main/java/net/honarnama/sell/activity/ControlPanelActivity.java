package net.honarnama.sell.activity;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import net.honarnama.HonarnamaBaseActivity;
import net.honarnama.sell.R;
import net.honarnama.sell.fragments.EditItemFragment;
import net.honarnama.sell.fragments.ItemsFragment;
import net.honarnama.sell.fragments.SellerAccountFragment;
import net.honarnama.sell.fragments.StoreInfoFragment;
import net.honarnama.utils.HonarnamaUser;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class ControlPanelActivity extends HonarnamaBaseActivity implements Drawer.OnDrawerItemClickListener {
    private Toolbar mToolbar;
    //    private TextView mToolbarTitleTextView;
    private ActionBarDrawerToggle mDrawerToggle;
    Drawer mResult;
    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!HonarnamaUser.isAuthenticatedUser() || !HonarnamaUser.isShopOwner()) {
            return;
        }
        setContentView(R.layout.activity_control_panel);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
//        mToolbarTitleTextView = (TextView) findViewById(R.id.toolbar_title);

        setSupportActionBar(mToolbar);

        mResult = new DrawerBuilder().withActivity(this)
                .withDrawerGravity(Gravity.RIGHT)
                .withRootView(R.id.drawer_container)
                .withToolbar(mToolbar)
                .withActionBarDrawerToggleAnimated(true)
                .withSelectedItem(-1)
                .withTranslucentStatusBar(false)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.nav_title_seller_account).withIcon(GoogleMaterial.Icon.gmd_account_circle).withIdentifier(1),
                        new DividerDrawerItem().withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.nav_title_store_info).withIdentifier(2).withIcon(GoogleMaterial.Icon.gmd_store),
                        new SecondaryDrawerItem().withName(R.string.nav_title_items).withIdentifier(3).withIcon(GoogleMaterial.Icon.gmd_view_list),
                        new SecondaryDrawerItem().withName(R.string.nav_title_edit_item).withIdentifier(4).withIcon(GoogleMaterial.Icon.gmd_edit),
                        new SecondaryDrawerItem().withName(R.string.nav_title_orders).withIdentifier(5).withIcon(GoogleMaterial.Icon.gmd_collection_item),
                        new DividerDrawerItem().withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.nav_title_exit_app).withIdentifier(6).withIcon(GoogleMaterial.Icon.gmd_power_off)
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

        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (mResult.isDrawerOpen()) {
//                    mResult.closeDrawer();
//                } else {
//                    mResult.openDrawer();
//                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(false);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mResult.setActionBarDrawerToggle(mDrawerToggle);

        this.mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            if (mResult.isDrawerOpen()) {
                mResult.closeDrawer();
            } else {
                mResult.openDrawer();
            }
        }


        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (mFragment != null) {
            mFragment.onActivityResult(requestCode, resultCode, intent);
        }
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        String title = "";
        switch (drawerItem.getIdentifier()) {
            case 1:
                mFragment = SellerAccountFragment.getInstance();
                title = getString(R.string.nav_title_seller_account);
                break;
            case 2:
                mFragment = StoreInfoFragment.getInstance();
                title = getString(R.string.nav_title_store_info);
                break;
            case 3:
                mFragment = ItemsFragment.getInstance();
                title = getString(R.string.nav_title_items);
                break;
            case 4:
                mFragment = EditItemFragment.getInstance();
                title = getString(R.string.nav_title_edit_item);
                break;
            case 6:
                //sign user out
                HonarnamaUser.logOut();
                Intent intent = new Intent(ControlPanelActivity.this, LoginActivity.class);
                startActivity(intent);
                break;


        }
        if (mFragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_container, mFragment);
            fragmentTransaction.commit();

            getSupportActionBar().setTitle(title);
        }
        return false;
    }
}
