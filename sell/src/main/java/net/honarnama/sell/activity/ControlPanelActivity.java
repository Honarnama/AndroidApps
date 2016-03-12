package net.honarnama.sell.activity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.crashlytics.android.Crashlytics;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.context.IconicsLayoutInflater;
import com.parse.LogOutCallback;
import com.parse.ParseException;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.activity.HonarnamaBaseActivity;
import net.honarnama.core.fragment.AboutFragment;
import net.honarnama.core.fragment.ContactFragment;
import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.core.model.CacheData;
import net.honarnama.core.utils.CommonUtil;
import net.honarnama.core.utils.HonarnamaUser;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.core.utils.WindowUtil;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;
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
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;

import bolts.Continuation;
import bolts.Task;

public class ControlPanelActivity extends HonarnamaBaseActivity implements View.OnClickListener {

    public static final int ITEM_IDENTIFIER_ACCOUNT = 0;

    public static final int ITEM_IDENTIFIER_STORE_INFO = 1;
    public static final int ITEM_IDENTIFIER_ITEMS = 2;
    public static final int ITEM_IDENTIFIER_ADD_ITEM = 3;
    public static final int ITEM_IDENTIFIER_EVENT_MANAGER = 4;

    public static final int ITEM_IDENTIFIER_CONTACT = 5;
    public static final int ITEM_IDENTIFIER_RULES = 6;
    public static final int ITEM_IDENTIFIER_ABOUT = 7;
    public static final int ITEM_IDENTIFIER_SHARE = 8;
    public static final int ITEM_IDENTIFIER_SUPPORT = 9;
    public static final int ITEM_IDENTIFIER_SWITCH_APP = 10;

    public static final int ITEM_IDENTIFIER_EXIT = 11;


    private Toolbar mToolbar;
    private Fragment mFragment;
    private EditItemFragment mEditItemFragment;
    private ProgressDialog mWaitingProgressDialog;

    Tracker mTracker;
    TextView mAboutTextView;

    private DrawerLayout mDrawer;
    public NavigationView mNavigationView;
    public RelativeLayout mNavFooter;


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

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(getString(R.string.toolbar_title));
        setSupportActionBar(mToolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(new IconicsDrawable(ControlPanelActivity.this)
                    .icon(GoogleMaterial.Icon.gmd_menu)
                    .color(Color.WHITE)
                    .sizeDp(20));
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navView);

        resetMenuIcons();
        setupDrawerContent();
        mNavFooter = (RelativeLayout) findViewById(R.id.footer_container);
        mNavFooter.setOnClickListener(this);

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
        mDrawer.openDrawer(Gravity.RIGHT);
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
            if (mDrawer.isDrawerOpen(Gravity.RIGHT)) {
                mDrawer.closeDrawer(Gravity.RIGHT);
            } else {
                mDrawer.openDrawer(Gravity.RIGHT);
            }
        }
        if (id == R.id.add_item_action) {
            if (mDrawer.isDrawerOpen(Gravity.RIGHT)) {
                mDrawer.closeDrawer(Gravity.RIGHT);
            }
            mEditItemFragment.reset(ControlPanelActivity.this, true);
            resetMenuIcons();
            selectDrawerItem(mNavigationView.getMenu().getItem(ITEM_IDENTIFIER_ADD_ITEM));
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.footer_container:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.honarnama.net")));
                break;
        }
    }

    private interface OnAcceptedListener {
        public void onAccepted();
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

        mToolbar.setTitle(fragment.getTitle(this));

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
        if (mDrawer.isDrawerOpen(Gravity.RIGHT)) {
            mDrawer.closeDrawer(Gravity.RIGHT);
        } else if (mFragment == mEditItemFragment) {
            if (mEditItemFragment.isDirty()) {
                switchFragmentFromEdittingItem(new OnAcceptedListener() {
                    @Override
                    public void onAccepted() {
                        mEditItemFragment.reset(ControlPanelActivity.this, true);
                        switchFragment(ItemsFragment.getInstance());
                        resetMenuIcons();
                        selectDrawerItem( mNavigationView.getMenu().getItem(ITEM_IDENTIFIER_ITEMS));
                    }
                });
            } else {
                switchFragment(ItemsFragment.getInstance());
                resetMenuIcons();
                selectDrawerItem(mNavigationView.getMenu().getItem(ITEM_IDENTIFIER_ITEMS));
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void resetMenuIcons() {
        Menu menu = mNavigationView.getMenu();

        IconicsDrawable accountDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_account_circle);
        menu.getItem(ITEM_IDENTIFIER_ACCOUNT).setIcon(accountDrawable);
        menu.getItem(ITEM_IDENTIFIER_ACCOUNT).setChecked(false);

        IconicsDrawable storeDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_store);
        menu.getItem(ITEM_IDENTIFIER_STORE_INFO).setIcon(storeDrawable);
        menu.getItem(ITEM_IDENTIFIER_STORE_INFO).setChecked(false);

        IconicsDrawable itemsDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_toc);
        menu.getItem(ITEM_IDENTIFIER_ITEMS).setIcon(itemsDrawable);
        menu.getItem(ITEM_IDENTIFIER_ITEMS).setChecked(false);

        IconicsDrawable newItemDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_edit);
        menu.getItem(ITEM_IDENTIFIER_ADD_ITEM).setIcon(newItemDrawable);
        menu.getItem(ITEM_IDENTIFIER_ADD_ITEM).setChecked(false);

        IconicsDrawable eventDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_event);
        menu.getItem(ITEM_IDENTIFIER_EVENT_MANAGER).setIcon(eventDrawable);
        menu.getItem(ITEM_IDENTIFIER_EVENT_MANAGER).setChecked(false);

        IconicsDrawable contactDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_email);
        menu.getItem(ITEM_IDENTIFIER_CONTACT).setIcon(contactDrawable);
        menu.getItem(ITEM_IDENTIFIER_CONTACT).setChecked(false);

        IconicsDrawable rulesDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_gavel);
        menu.getItem(ITEM_IDENTIFIER_RULES).setIcon(rulesDrawable);
        menu.getItem(ITEM_IDENTIFIER_RULES).setChecked(false);

        IconicsDrawable aboutDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_info_outline);
        menu.getItem(ITEM_IDENTIFIER_ABOUT).setIcon(aboutDrawable);
        menu.getItem(ITEM_IDENTIFIER_ABOUT).setChecked(false);

        IconicsDrawable shareDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_share);
        menu.getItem(ITEM_IDENTIFIER_SHARE).setIcon(shareDrawable);
        menu.getItem(ITEM_IDENTIFIER_SHARE).setChecked(false);

        IconicsDrawable supportDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_stars);
        menu.getItem(ITEM_IDENTIFIER_SUPPORT).setIcon(supportDrawable);
        menu.getItem(ITEM_IDENTIFIER_SUPPORT).setChecked(false);

        IconicsDrawable swapDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_swap_horiz);
        menu.getItem(ITEM_IDENTIFIER_SWITCH_APP).setIcon(swapDrawable);
        menu.getItem(ITEM_IDENTIFIER_SWITCH_APP).setChecked(false);

        IconicsDrawable exitDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_exit_to_app);
        menu.getItem(ITEM_IDENTIFIER_EXIT).setIcon(exitDrawable);
        menu.getItem(ITEM_IDENTIFIER_EXIT).setChecked(false);

    }

    public void selectDrawerItem(MenuItem menuItem) {

        HonarnamaBaseFragment fragment = null;

        switch (menuItem.getItemId()) {
            case R.id.item_account:
                menuItem.setChecked(true);
                IconicsDrawable accountDrawable =
                        new IconicsDrawable(ControlPanelActivity.this)
                                .color(getResources().getColor(R.color.amber_launcher_color))
                                .icon(GoogleMaterial.Icon.gmd_account_circle);
                menuItem.setIcon(accountDrawable);
                fragment = UserAccountFragment.getInstance();
                break;


            case R.id.item_store_info:
                menuItem.setChecked(true);
                IconicsDrawable storeDrawable =
                        new IconicsDrawable(ControlPanelActivity.this)
                                .color(getResources().getColor(R.color.amber_launcher_color))
                                .icon(GoogleMaterial.Icon.gmd_store);
                menuItem.setIcon(storeDrawable);
                fragment = StoreInfoFragment.getInstance();
                break;

            case R.id.item_items:
                menuItem.setChecked(true);
                IconicsDrawable itemsDrawable =
                        new IconicsDrawable(ControlPanelActivity.this)
                                .color(getResources().getColor(R.color.amber_launcher_color))
                                .icon(GoogleMaterial.Icon.gmd_view_list);
                menuItem.setIcon(itemsDrawable);
                fragment = ItemsFragment.getInstance();
                break;

            case R.id.item_new_item:
                menuItem.setChecked(true);
                IconicsDrawable newItemDrawable =
                        new IconicsDrawable(ControlPanelActivity.this)
                                .color(getResources().getColor(R.color.amber_launcher_color))
                                .icon(GoogleMaterial.Icon.gmd_edit);
                menuItem.setIcon(newItemDrawable);
                fragment = EditItemFragment.getInstance();
                break;

            case R.id.item_art_event:
                menuItem.setChecked(true);
                IconicsDrawable eventDrawable =
                        new IconicsDrawable(ControlPanelActivity.this)
                                .color(getResources().getColor(R.color.amber_launcher_color))
                                .icon(GoogleMaterial.Icon.gmd_event);
                menuItem.setIcon(eventDrawable);
                fragment = EventManagerFragment.getInstance();
                break;

            case R.id.item_about_us:
                menuItem.setChecked(true);
                IconicsDrawable aboutDrawable =
                        new IconicsDrawable(ControlPanelActivity.this)
                                .color(getResources().getColor(R.color.amber_launcher_color))
                                .icon(GoogleMaterial.Icon.gmd_info_outline);
                menuItem.setIcon(aboutDrawable);
                fragment = AboutFragment.getInstance(HonarnamaBaseApp.SELL_APP_KEY);
                break;

            case R.id.item_contact_us:
                menuItem.setChecked(true);
                IconicsDrawable contactDrawable =
                        new IconicsDrawable(ControlPanelActivity.this)
                                .color(getResources().getColor(R.color.amber_launcher_color))
                                .icon(GoogleMaterial.Icon.gmd_email);
                menuItem.setIcon(contactDrawable);
                fragment = ContactFragment.getInstance(HonarnamaBaseApp.SELL_APP_KEY);
                break;

            case R.id.item_rules:
                String url = "http://www.honarnama.net/terms";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;


            case R.id.item_share_us:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "سلام،" + "\n" + "برنامه‌ٔ هنرنما برای فروشندگان رو از کافه بازار دانلود کن. اینم لینکش:" +
                        "\n" + "http://cafebazaar.ir/app/" + HonarnamaSellApp.getInstance().getPackageName() + "/");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;

            case R.id.item_support_us:
                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setData(Uri.parse("bazaar://details?id=" + HonarnamaSellApp.getInstance().getPackageName()));
                intent.setPackage("com.farsitel.bazaar");
                startActivity(intent);
                break;

            case R.id.item_switch_app:
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

            case R.id.item_nav_title_exit_app:
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
        mDrawer.closeDrawer(Gravity.RIGHT);
    }

}
