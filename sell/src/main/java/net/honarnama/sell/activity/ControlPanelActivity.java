package net.honarnama.sell.activity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.crashlytics.android.Crashlytics;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.context.IconicsLayoutInflater;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.parse.LogOutCallback;
import com.parse.ParseException;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.activity.HonarnamaBaseActivity;
import net.honarnama.core.fragment.ContactFragment;
import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.core.model.CacheData;
import net.honarnama.core.utils.CommonUtil;
import net.honarnama.core.utils.HonarnamaUser;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.core.utils.WindowUtil;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;
import net.honarnama.sell.fragments.AboutFragment;
import net.honarnama.sell.fragments.EditItemFragment;
import net.honarnama.sell.fragments.EventManagerFragment;
import net.honarnama.sell.fragments.ItemsFragment;
import net.honarnama.sell.fragments.NoNetworkFragment;
import net.honarnama.sell.fragments.StoreInfoFragment;
import net.honarnama.sell.fragments.UserAccountFragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;

import bolts.Continuation;
import bolts.Task;

public class ControlPanelActivity extends HonarnamaBaseActivity implements Drawer.OnDrawerItemClickListener {

    public static final int DRAWER_ITEM_IDENTIFIER_ACCOUNT = 1;
    public static final int DRAWER_ITEM_IDENTIFIER_STORE_INFO = 2;
    public static final int DRAWER_ITEM_IDENTIFIER_ITEMS = 3;
    public static final int DRAWER_ITEM_IDENTIFIER_ADD_ITEM = 4;
    public static final int DRAWER_ITEM_IDENTIFIER_EVENT_MANAGER = 5;

    public static final int DRAWER_ITEM_IDENTIFIER_CONTACT = 6;
    public static final int DRAWER_ITEM_IDENTIFIER_RULES = 7;
    public static final int DRAWER_ITEM_IDENTIFIER_ABOUT = 8;
    public static final int DRAWER_ITEM_IDENTIFIER_SUPPORT = 9;
    public static final int DRAWER_ITEM_IDENTIFIER_SHARE = 10;
    public static final int DRAWER_ITEM_IDENTIFIER_SWITCH_APP = 11;
    public static final int DRAWER_ITEM_IDENTIFIER_EXIT = 12;


    private Toolbar mToolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private Fragment mFragment;
    private EditItemFragment mEditItemFragment;
    private ProgressDialog mWaitingProgressDialog;

    Tracker mTracker;
    Drawer mResult;
    TextView mAboutTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));

        super.onCreate(savedInstanceState);
        if (!HonarnamaUser.isAuthenticatedUser()) {
            if (BuildConfig.DEBUG) {
                logD("User was not authenticated!");
            }
            return;
        }

        if (getIntent().hasExtra(HonarnamaSellApp.EXTRA_KEY_UNCAUGHT_EXCEPTION)) {
            Toast.makeText(ControlPanelActivity.this, R.string.uncaught_exception_msg, Toast.LENGTH_SHORT).show();
            logE(getIntent().getStringExtra(HonarnamaSellApp.EXTRA_KEY_UNCAUGHT_EXCEPTION));
        }

        mTracker = HonarnamaSellApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("ControlPanel");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        WindowUtil.hideKeyboard(ControlPanelActivity.this);
        mWaitingProgressDialog = new ProgressDialog(ControlPanelActivity.this);
        setContentView(R.layout.activity_control_panel);

        mAboutTextView = (TextView) findViewById(R.id.about_us_text_view);
        try {
            Resources res = getResources();
            InputStream in_s = res.openRawResource(R.raw.about_us);
            byte[] b = new byte[in_s.available()];
            in_s.read(b);
            mAboutTextView.setText(new String(b));
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, HonarnamaBaseApp.PRODUCTION_TAG, "Error setting about us text in main page: " + e);
        }


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
                        new SecondaryDrawerItem().withName(R.string.register_new_item).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_ADD_ITEM).withIcon(GoogleMaterial.Icon.gmd_edit),
                        new SecondaryDrawerItem().withName(R.string.art_event).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_EVENT_MANAGER).withIcon(GoogleMaterial.Icon.gmd_event),
                        new DividerDrawerItem().withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.contact_us).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_CONTACT).withIcon(GoogleMaterial.Icon.gmd_email),
                        new SecondaryDrawerItem().withName(R.string.rules).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_RULES).withIcon(GoogleMaterial.Icon.gmd_gavel).withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.about_us).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_ABOUT).withIcon(GoogleMaterial.Icon.gmd_info_outline),
                        new SecondaryDrawerItem().withName(R.string.share_us).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_SHARE).withIcon(GoogleMaterial.Icon.gmd_share).withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.support_us).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_SUPPORT).withIcon(GoogleMaterial.Icon.gmd_stars).withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.switch_app).withSelectable(false).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_SWITCH_APP).withIcon(GoogleMaterial.Icon.gmd_swap_horiz),
                        new DividerDrawerItem().withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.nav_title_exit_app).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_EXIT).withIcon(GoogleMaterial.Icon.gmd_exit_to_app)

                ).withFooter(R.layout.footer)
                .withOnDrawerItemClickListener(this)
                .withShowDrawerOnFirstLaunch(true)
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
//        mResult.addStickyFooterItem(new SecondaryDrawerItem().withName("StickyFooterItem").with);
        this.mDrawerToggle.syncState();

        mEditItemFragment = EditItemFragment.getInstance();

        processIntent(getIntent());

//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ControlPanelActivity.this);
        final SharedPreferences sharedPref = HonarnamaBaseApp.getInstance().getSharedPreferences(HonarnamaUser.getCurrentUser().getUsername(), Context.MODE_PRIVATE);
//        final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        if (!sharedPref.getBoolean(HonarnamaSellApp.PREF_LOCAL_DATA_STORE_SYNCED, false)) {
            if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                HonarnamaBaseFragment fragment = NoNetworkFragment.getInstance();
                switchFragment(fragment);
                return;
            }
            new CacheData(ControlPanelActivity.this).startSyncing().continueWith(new Continuation<Void, Object>() {
                @Override
                public Object then(Task<Void> task) throws Exception {
                    if (task.isFaulted()) {
                        HonarnamaBaseFragment fragment = NoNetworkFragment.getInstance();
                        switchFragment(fragment);
                        Toast.makeText(ControlPanelActivity.this, R.string.syncing_data_failed, Toast.LENGTH_LONG).show();
                    }
                    return null;
                }
            });
        }

        mResult.getFooter().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.honarnama.net")));
            }
        });

        mResult.openDrawer();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    private void processIntent(Intent intent) {
        Uri data = intent.getData();
        if (BuildConfig.DEBUG) {
            logD("processIntent :: data= " + data);
        }

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
//            getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item_row clicks here. The action bar will
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
        if (id == R.id.add_item_action) {
            mEditItemFragment.reset(ControlPanelActivity.this, true);
            mResult.setSelection(DRAWER_ITEM_IDENTIFIER_ADD_ITEM);
            switchFragment(mEditItemFragment);

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("AddItem")
                    .build());
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
                fragment = UserAccountFragment.getInstance();
                break;
            case DRAWER_ITEM_IDENTIFIER_STORE_INFO:
                fragment = StoreInfoFragment.getInstance();
                break;
            case DRAWER_ITEM_IDENTIFIER_ITEMS:
                fragment = ItemsFragment.getInstance();
                break;
            case DRAWER_ITEM_IDENTIFIER_ADD_ITEM:
                fragment = EditItemFragment.getInstance();
                break;
            case DRAWER_ITEM_IDENTIFIER_EVENT_MANAGER:
                fragment = EventManagerFragment.getInstance();
                break;
            case DRAWER_ITEM_IDENTIFIER_ABOUT:
                fragment = AboutFragment.getInstance();
                break;
            case DRAWER_ITEM_IDENTIFIER_CONTACT:
                fragment = ContactFragment.getInstance(HonarnamaBaseApp.SELL_APP_KEY);
                break;
            case DRAWER_ITEM_IDENTIFIER_RULES:
                String url = "http://www.honarnama.net/terms";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            case DRAWER_ITEM_IDENTIFIER_SUPPORT:
                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setData(Uri.parse("bazaar://details?id=" + HonarnamaSellApp.getInstance().getPackageName()));
                intent.setPackage("com.farsitel.bazaar");
                startActivity(intent);
                break;

            case DRAWER_ITEM_IDENTIFIER_SWITCH_APP:
                try {
                    if (CommonUtil.isPackageInstalled("net.honarnama.browse", ControlPanelActivity.this)) {
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("net.honarnama.browse");
                        startActivity(launchIntent);
                    } else {
                        Intent browseAppIntent = new Intent(Intent.ACTION_VIEW);
                        browseAppIntent.setData(Uri.parse("bazaar://details?id=" + "net.honarnama.browse"));
                        browseAppIntent.setPackage("com.farsitel.bazaar");
                        startActivity(browseAppIntent);
                    }
                } catch (Exception e) {
                    logE("Error switching from sell app to browse. Error: " + e);
                }
                break;

            case DRAWER_ITEM_IDENTIFIER_SHARE:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "سلام،" + "\n" + "برنامه‌ٔ هنرنما برای فروشندگان رو از کافه بازار دانلود کن. اینم لینکش:" +
                        "\n" + "http://cafebazaar.ir/app/" + HonarnamaSellApp.getInstance().getPackageName() + "/");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;
            case DRAWER_ITEM_IDENTIFIER_EXIT:
                //sign user out
                mWaitingProgressDialog.setMessage(getString(R.string.please_wait));
                mWaitingProgressDialog.setCancelable(false);
                mWaitingProgressDialog.show();
                HonarnamaUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            logE("Error logging user out." + " Error Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e, "", e);
                        }
                        if (mWaitingProgressDialog.isShowing()) {
                            mWaitingProgressDialog.dismiss();
                        }

                        Intent intent = new Intent(ControlPanelActivity.this, LoginActivity.class);
                        finish();
                        startActivity(intent);
                    }
                });

                break;
        }
        // Not null && (Another section || Maybe editing but wants to create new item_row)
//        if ((fragment != null) && ((fragment != mFragment) || (fragment == mEditItemFragment))) {
        if ((fragment != null)) {
            if (mFragment == mEditItemFragment) {
                if (mEditItemFragment.isDirty()) {
                    final HonarnamaBaseFragment finalFragment = fragment;
                    switchFragmentFromEdittingItem(new OnAcceptedListener() {
                        @Override
                        public void onAccepted() {
                            mEditItemFragment.reset(ControlPanelActivity.this, true);
                            switchFragment(finalFragment);
                        }
                    });
                } else {
                    mEditItemFragment.reset(ControlPanelActivity.this, true);
                    switchFragment(fragment);
                }
            } else {
                mEditItemFragment.reset(ControlPanelActivity.this, true);
                switchFragment(fragment);
            }
        }

        return false;
    }

    private void switchFragmentFromEdittingItem(final OnAcceptedListener onAcceptedListener) {
        final AlertDialog.Builder exitEditingDialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
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
        View includedAbout = findViewById(R.id.about_included_in_control_panel);
        if (includedAbout != null) {
            includedAbout.setVisibility(View.GONE);
        }
        WindowUtil.hideKeyboard(ControlPanelActivity.this);
        mFragment = fragment;

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_container, fragment);
        fragmentTransaction.commit();

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("SwitchFragment")
                .build());

        getSupportActionBar().setTitle(fragment.getTitle(this));

    }

    public void switchFragmentToEditItem(String itemId) {
        mEditItemFragment.setItemId(ControlPanelActivity.this, itemId);
        switchFragment(mEditItemFragment);
    }

    @Override
    protected void onStop() {
        if (mWaitingProgressDialog != null) {
            if (mWaitingProgressDialog.isShowing()) {
                mWaitingProgressDialog.dismiss();
            }
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (mResult.isDrawerOpen()) {
            mResult.closeDrawer();
        } else if (mFragment == mEditItemFragment) {
            if (mEditItemFragment.isDirty()) {
                switchFragmentFromEdittingItem(new OnAcceptedListener() {
                    @Override
                    public void onAccepted() {
                        mEditItemFragment.reset(ControlPanelActivity.this, true);
                        switchFragment(ItemsFragment.getInstance());
                        mResult.setSelection(DRAWER_ITEM_IDENTIFIER_ITEMS);
                    }
                });
            } else {
                switchFragment(ItemsFragment.getInstance());
                mResult.setSelection(DRAWER_ITEM_IDENTIFIER_ITEMS);
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

}
