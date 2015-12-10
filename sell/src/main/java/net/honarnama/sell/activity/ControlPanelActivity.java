package net.honarnama.sell.activity;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import net.honarnama.HonarnamaBaseActivity;
import net.honarnama.HonarnamaBaseApp;
import net.honarnama.HonarnamaBaseFragment;
import net.honarnama.base.BuildConfig;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;
import net.honarnama.sell.fragments.EditItemFragment;
import net.honarnama.sell.fragments.ItemsFragment;
import net.honarnama.sell.fragments.SellerAccountFragment;
import net.honarnama.sell.fragments.StoreInfoFragment;
import net.honarnama.utils.HonarnamaUser;
import net.honarnama.utils.NetworkManager;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.List;

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
    private EditItemFragment mEditItemFragment;

    Drawer mResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!HonarnamaUser.isAuthenticatedUser() || !HonarnamaUser.isShopOwner()) {
            return;
        }
        setContentView(R.layout.activity_control_panel);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.toolbar_title);
        setSupportActionBar(mToolbar);

        mResult = new DrawerBuilder().withActivity(this)
                .withDrawerGravity(Gravity.RIGHT)
                .withRootView(R.id.drawer_container)
                .withToolbar(mToolbar)
                .withActionBarDrawerToggleAnimated(true)
                .withSelectedItem(-1)
                .withTranslucentStatusBar(false)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.nav_title_seller_account).
                                withIcon(GoogleMaterial.Icon.gmd_account_circle).withIdentifier(DRAWER_ITEM_IDENTIFIER_ACCOUNT),
                        new DividerDrawerItem().withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.nav_title_store_info).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_STORE_INFO).withIcon(GoogleMaterial.Icon.gmd_store),
                        new SecondaryDrawerItem().withName(R.string.nav_title_items).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_ITEMS).withIcon(GoogleMaterial.Icon.gmd_view_list),
                        new SecondaryDrawerItem().withName(R.string.nav_title_new_item).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_EDIT_ITEM).withIcon(GoogleMaterial.Icon.gmd_edit),
                        new SecondaryDrawerItem().withName(R.string.nav_title_orders).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_ORDERS).withIcon(GoogleMaterial.Icon.gmd_collection_item),
                        new DividerDrawerItem().withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.nav_title_exit_app).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_EXIT).withIcon(GoogleMaterial.Icon.gmd_power_off)
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

        mEditItemFragment = EditItemFragment.getInstance();
        processIntent(getIntent());

        //
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ControlPanelActivity.this);
        if (!sharedPref.getBoolean(HonarnamaSellApp.PREF_LOCAL_DATA_STORE_FOR_CATEGORIES_SYNCED, false)) {
            cacheArtCategories();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    private void processIntent(Intent intent) {
        Uri data = intent.getData();

        logI(null, "processIntent :: data= " + data);

        if (data != null) {
            final String itemId = data.getQueryParameter("itemId");
            if (itemId != null) {
                if (mEditItemFragment.isDirty()) {
                    switchFragmentFromEdittingItem(new OnAcceptedListener() {
                        @Override
                        public void onAccepted() {
                            switchFragmentToEditItem(itemId);
                        }
                    });
                } else {
                    switchFragmentToEditItem(itemId);
                }
            }
        }
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

    private interface OnAcceptedListener {
        public void onAccepted();
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        HonarnamaBaseFragment fragment = null;
        switch (drawerItem.getIdentifier()) {
            case DRAWER_ITEM_IDENTIFIER_ACCOUNT:
                fragment = SellerAccountFragment.getInstance();
                break;
            case DRAWER_ITEM_IDENTIFIER_STORE_INFO:
                fragment = StoreInfoFragment.getInstance();
                break;
            case DRAWER_ITEM_IDENTIFIER_ITEMS:
                fragment = ItemsFragment.getInstance();
                break;
            case DRAWER_ITEM_IDENTIFIER_EDIT_ITEM:
                fragment = mEditItemFragment;
                break;
            case DRAWER_ITEM_IDENTIFIER_ORDERS:
                //mFragment = EditItemFragment.getInstance();
                break;
            case DRAWER_ITEM_IDENTIFIER_EXIT:
                //sign user out
                HonarnamaUser.logOut();
                Intent intent = new Intent(ControlPanelActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
        }
        // Not null && (Another section || Maybe editing but wants to create new item)
        if ((fragment != null) && ((fragment != mFragment) || (fragment == mEditItemFragment))) {
            if (mEditItemFragment.isDirty()) {
                final HonarnamaBaseFragment finalFragment = fragment;
                switchFragmentFromEdittingItem(new OnAcceptedListener() {
                    @Override
                    public void onAccepted() {
                        mEditItemFragment.reset(true);
                        switchFragment(finalFragment);
                    }
                });
            } else {
                if (fragment == mEditItemFragment) {
                    mEditItemFragment.reset(true);
                }
                switchFragment(fragment);
            }
        }
        return false;
    }

    private void switchFragmentFromEdittingItem(final OnAcceptedListener onAcceptedListener) {
        final AlertDialog.Builder exitEditingDialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.DialogStyle));
        exitEditingDialog.setTitle(getString(R.string.exit_from_editing_dialog_title));
        exitEditingDialog.setItems(new String[]{getString(R.string.exit_from_editing_option_dont_exit), getString(R.string.exit_from_editing_option_exit)},
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 1) {
                            onAcceptedListener.onAccepted();
                        }
                        dialog.dismiss();
                    }
                });
        exitEditingDialog.show();
    }

    public void switchFragment(final HonarnamaBaseFragment fragment) {
        mFragment = fragment;

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_container, fragment);
        fragmentTransaction.commit();

        getSupportActionBar().setTitle(fragment.getTitle(this));
    }

    public void switchFragmentToEditItem(String itemId) {
        mEditItemFragment.setItemId(itemId);
        switchFragment(mEditItemFragment);
    }

    private void cacheArtCategories() {
        if (!NetworkManager.getInstance().isNetworkEnabled(this, true)) {
            return;
        }
        final ProgressDialog syncingDataProgressDialog = new ProgressDialog(this);
        syncingDataProgressDialog.setCancelable(false);
        syncingDataProgressDialog.setMessage(getString(R.string.syncing_data));
        syncingDataProgressDialog.show();
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("art_categories");
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(final List<ParseObject> artCategories, ParseException e) {

                syncingDataProgressDialog.dismiss();
                if (e == null) {
                    ParseObject.unpinAllInBackground("artCategories", artCategories, new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            ParseObject.pinAllInBackground("artCategories", artCategories, new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ControlPanelActivity.this);
                                            SharedPreferences.Editor editor = sharedPref.edit();
                                            editor.putBoolean(HonarnamaSellApp.PREF_LOCAL_DATA_STORE_FOR_CATEGORIES_SYNCED, true);
                                            editor.commit();
                                        }
                                    }
                            );
                        }
                    });


                } else {
                    Toast.makeText(ControlPanelActivity.this, getString(R.string.syncing_data_failed), Toast.LENGTH_LONG).show();
                    if (BuildConfig.DEBUG) {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "Receiving categories list failed. Code: " + e.getCode() +
                                "//" + e.getMessage() + " // " + e);
                    } else {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Receiving categories list failed. Code: " + e.getCode() +
                                "//" + e.getMessage() + " // " + e);
                    }
                }
            }
        });
    }

}
