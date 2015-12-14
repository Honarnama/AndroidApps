package net.honarnama.browse.activity;

import net.honarnama.browse.R;
import net.honarnama.core.activity.HonarnamaBaseActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by elnaz on 12/13/15.
 */
public class LoginActivity extends HonarnamaBaseActivity implements View.OnClickListener {

    TextView mRegisterAsCustomerTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mRegisterAsCustomerTextView = (TextView) findViewById(R.id.register_as_customer_text_view);
        mRegisterAsCustomerTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.register_as_customer_text_view:
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                break;
        }
    }
}
