package net.honarnama.sell.activity;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

import net.honarnama.core.activity.HonarnamaBaseActivity;
import net.honarnama.core.utils.GenericGravityTextWatcher;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.sell.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ForgotPasswordActivity extends HonarnamaBaseActivity {
    private Button mForgotPasswordButton;
    private EditText mForgotPasswordEmailEditEext;
    private ProgressDialog mWaitingProgressDialog;

    //TODO check net
    //TODO Debug logs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mForgotPasswordEmailEditEext = (EditText) findViewById(R.id.forgot_password_email_edit_text);
        mForgotPasswordEmailEditEext.addTextChangedListener(new GenericGravityTextWatcher(mForgotPasswordEmailEditEext));
        mForgotPasswordEmailEditEext.requestFocus();

        mForgotPasswordButton = (Button) findViewById(R.id.forgot_password_button);
        mForgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkManager.getInstance().isNetworkEnabled(ForgotPasswordActivity.this, true)) {
                    return;
                }
                String email = mForgotPasswordEmailEditEext.getText().toString().trim();

                if(email.length() == 0)
                {
                    mForgotPasswordEmailEditEext.requestFocus();
                    mForgotPasswordEmailEditEext.setError(getString(R.string.error_email_field_can_not_be_empty));
                    return;
                }
                else
                {
                    mWaitingProgressDialog = new ProgressDialog(ForgotPasswordActivity.this);
                    mWaitingProgressDialog.setMessage(getString(R.string.please_wait));
                    mWaitingProgressDialog.setCancelable(false);
                    mWaitingProgressDialog.show();

                    ParseUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {
                        @Override
                        public void done(ParseException e) {
                            mWaitingProgressDialog.dismiss();
                            if(e== null)
                            {
                                Toast.makeText(ForgotPasswordActivity.this, "لینک بازنشانی رمز عبور برایتان ارسال شد.", Toast.LENGTH_LONG).show();
                                kill_activity();
                            }
                            else
                            {
                                if(e.getCode()==ParseException.EMAIL_NOT_FOUND)
                                {
                                    Toast.makeText(ForgotPasswordActivity.this, "کاربری با آدرس ایمیل داده شده پیدا نشد.", Toast.LENGTH_LONG).show();
                                }
                                else
                                {
                                    Toast.makeText(ForgotPasswordActivity.this, "متاسفانه خطایی رخ داد. لطفا مجددا تلاش کنید.", Toast.LENGTH_LONG).show();
                                    logE("Error sending request for forgot password link", "Error Code: " + e.getCode() + "// Error Message: "+e.getMessage(), e);
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    void kill_activity() {
        finish();
    }
}