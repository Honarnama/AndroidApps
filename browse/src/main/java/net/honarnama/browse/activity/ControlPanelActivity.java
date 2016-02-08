package net.honarnama.browse.activity;

import net.honarnama.browse.R;
import net.honarnama.core.activity.HonarnamaBaseActivity;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class ControlPanelActivity extends HonarnamaBaseActivity {


    public static final int DRAWER_ITEM_IDENTIFIER_ACCOUNT = 1;
    public static final int DRAWER_ITEM_IDENTIFIER_STORE_INFO = 2;
    public static final int DRAWER_ITEM_IDENTIFIER_ITEMS = 3;
    public static final int DRAWER_ITEM_IDENTIFIER_EDIT_ITEM = 4;
    public static final int DRAWER_ITEM_IDENTIFIER_ORDERS = 5;
    public static final int DRAWER_ITEM_IDENTIFIER_EXIT = 6;

    private Toolbar mToolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private Fragment mFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        mToolbar = (Toolbar) findViewById(R.id.browse_toolbar);
        mToolbar.setTitle(R.string.toolbar_title);
        setSupportActionBar(mToolbar);
    }

}
