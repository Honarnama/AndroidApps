package net.honarnama.browse.dialog;

import net.honarnama.browse.R;

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
public class ConfirmationDialog extends Dialog {

    TextView mTitleTV;
    TextView mMsgTV;

    public ConfirmationDialog(Context context, String title, String msg) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(true);
        setContentView(R.layout.confirmation_dilaog);
        try {
            mTitleTV = (TextView) findViewById(R.id.confirm_title);
            mMsgTV = (TextView) findViewById(R.id.contact_warning);
            mTitleTV.setText(title);
            mMsgTV.setText(msg);
        } catch (Exception ex) {

        }
    }

    public void showDialog(View.OnClickListener onYesClickListener) {
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        View closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button yesButton = (Button) findViewById(R.id.yes);
        yesButton.setOnClickListener(onYesClickListener);

        show();

    }

}
