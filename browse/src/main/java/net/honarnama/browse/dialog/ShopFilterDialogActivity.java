package net.honarnama.browse.dialog;

import com.mikepenz.iconics.view.IconicsImageView;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.HonarnamaBrowseActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * Created by elnaz on 3/2/16.
 */
public class ShopFilterDialogActivity extends HonarnamaBrowseActivity implements View.OnClickListener {

    public Activity mActivity;

    public EditText mSearchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shop_filter_dialog);

        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        mActivity = ShopFilterDialogActivity.this;

        Intent intent = getIntent();

        mSearchEditText = (EditText) findViewById(R.id.serach_term);

        if (intent.hasExtra(HonarnamaBrowseApp.EXTRA_KEY_SEARCH_TERM)) {
            mSearchEditText.setText(intent.getStringExtra(HonarnamaBrowseApp.EXTRA_KEY_SEARCH_TERM));
        } else {
            mSearchEditText.setText("");
        }

        findViewById(R.id.apply_filter).setOnClickListener(this);
        findViewById(R.id.remove_filter).setOnClickListener(this);

        IconicsImageView closeButton = (IconicsImageView) findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        checkAndUpdateMeta(false, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        setIntent(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.apply_filter:
                setFilters();
                break;

            case R.id.remove_filter:
                removeFilters();
                break;

        }
    }

    public void setFilters() {
        if (BuildConfig.DEBUG) {
            logD("setFilters for shopFilterDialog");
        }
        Intent data = new Intent();
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_SEARCH_TERM, mSearchEditText.getText().toString().trim());
        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_FILTER_APPLIED, true);
        setResult(RESULT_OK, data);
        finish();
    }

    public void removeFilters() {
        Intent data = new Intent();
        data.putExtra(HonarnamaBaseApp.EXTRA_KEY_FILTER_APPLIED, false);
        data.putExtra(HonarnamaBrowseApp.EXTRA_KEY_SEARCH_TERM, "");

        setResult(RESULT_OK, data);
        finish();
    }

}

