package net.honarnama.sell.activity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.honarnama.core.activity.HonarnamaBaseActivity;
import net.honarnama.core.utils.GenericGravityTextWatcher;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ForgotPasswordActivity extends HonarnamaBaseActivity {
    private Button mForgotPasswordButton;
    private EditText mForgotPasswordEmailEditEext;
    private ProgressDialog mWaitingProgressDialog;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTracker = HonarnamaSellApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("ForgotPasswordActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        setContentView(R.layout.activity_forgot_password);

        mForgotPasswordEmailEditEext = (EditText) findViewById(R.id.forgot_password_email_edit_text);
        mForgotPasswordEmailEditEext.addTextChangedListener(new GenericGravityTextWatcher(mForgotPasswordEmailEditEext));
        mForgotPasswordEmailEditEext.requestFocus();

        mForgotPasswordButton = (Button) findViewById(R.id.forgot_password_button);
        mForgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                    return;
                }
                String email = mForgotPasswordEmailEditEext.getText().toString().trim();

                if (email.length() == 0) {
                    mForgotPasswordEmailEditEext.requestFocus();
                    mForgotPasswordEmailEditEext.setError(getString(R.string.error_email_field_can_not_be_empty));
                    return;
                } else {
                    boolean isOK = android.util.Patterns.EMAIL_ADDRESS.matcher(mForgotPasswordEmailEditEext.getText().toString()).matches();
                    if (!isOK) {
                        mForgotPasswordEmailEditEext.requestFocus();
                        mForgotPasswordEmailEditEext.setError(getString(net.honarnama.base.R.string.error_email_address_is_not_valid));
                        return;
                    }
                }
                mWaitingProgressDialog = new ProgressDialog(ForgotPasswordActivity.this);
                mWaitingProgressDialog.setMessage(getString(R.string.please_wait));
                mWaitingProgressDialog.setCancelable(false);
                mWaitingProgressDialog.show();

//                ParseUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {
//                    @Override
//                    public void done(ParseException e) {
//                        mWaitingProgressDialog.dismiss();
//                        if (e == null) {
//                            Toast.makeText(ForgotPasswordActivity.this, getString(R.string.password_reset_link_sent), Toast.LENGTH_LONG).show();
//                            kill_activity();
//                        } else {
//                            if (e.getCode() == ParseException.EMAIL_NOT_FOUND) {
//                                Toast.makeText(ForgotPasswordActivity.this, getString(R.string.no_user_found_matching_email), Toast.LENGTH_LONG).show();
//                            } else {
//                                Toast.makeText(ForgotPasswordActivity.this, getString(R.string.error_sending_reset_pass_link) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
//                                logE("Error sending request for forgot password link" + " // Error Code: " + e.getCode() + "// Error Message: " + e.getMessage(), "", e);
//                            }
//                        }
//                    }
//                });
            }
        });
    }

    void kill_activity() {
        finish();
    }
}
