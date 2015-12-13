package net.honarnama.browse.activity;

import net.honarnama.browse.R;
import net.honarnama.core.activity.HonarnamaBaseActivity;

import android.os.Bundle;
import android.view.View;

/**
 * Created by elnaz on 12/13/15.
 */
public class LoginActivity extends HonarnamaBaseActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    @Override
    public void onClick(View v) {

    }
}
