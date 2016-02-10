package net.honarnama.browse.activity;

import net.honarnama.browse.R;
import net.honarnama.core.activity.HonarnamaBaseActivity;

import android.app.Fragment;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
    public static TabHost tabHost;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}