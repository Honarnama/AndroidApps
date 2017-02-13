package net.honarnama.base.dialog;

import net.honarnama.base.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;


/**
 * Created by elnaz on 3/2/16.
 */
public class AlertDialog extends Dialog {

    TextView mTitleTV;
    TextView mMsgTV;
    Button mYesBtn;
    Button mNoBtn;

    public AlertDialog(Context context, String title, String msg, String yesBtnText, String noBtnText) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(true);
        setContentView(R.layout.dialog_alert);
        try {
            mTitleTV = (TextView) findViewById(R.id.confirm_title);
            mMsgTV = (TextView) findViewById(R.id.contact_warning);
            mYesBtn = (Button) findViewById(R.id.yes_btn);
            mNoBtn = (Button) findViewById(R.id.no_button);
            mTitleTV.setText(title);
            mMsgTV.setText(msg);
            mYesBtn.setText(yesBtnText);
            mNoBtn.setText(noBtnText);
        } catch (Exception ex) {

        }
    }

    public void showDialog(View.OnClickListener onYesClickListener) {
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        View closeButton = findViewById(R.id.no_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button yesButton = (Button) findViewById(R.id.yes_btn);
        yesButton.setOnClickListener(onYesClickListener);

        show();

    }

}
