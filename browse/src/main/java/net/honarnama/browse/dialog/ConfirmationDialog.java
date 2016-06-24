package net.honarnama.browse.dialog;

import net.honarnama.browse.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;

/**
 * Created by elnaz on 3/2/16.
 */
public class ConfirmationDialog extends Dialog {

    public ConfirmationDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(true);
        setContentView(R.layout.confirmation_dilaog);
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
