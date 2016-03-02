package net.honarnama.browse.widget;

import com.mikepenz.iconics.view.IconicsImageView;
import com.parse.ImageSelector;

import net.honarnama.browse.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by elnaz on 3/2/16.
 */
public class ContactDialog {

    public void showDialog(final Activity activity, String phoneNumber, String cellNumber, String warnMsg) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.contact_dialog);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        final TextView phoneTextView = (TextView) dialog.findViewById(R.id.phone_text_view);
        if (!TextUtils.isEmpty(phoneNumber)) {
            phoneTextView.setText(phoneNumber);
            phoneTextView.setTextColor(activity.getResources().getColor(R.color.text_color));
            phoneTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:+" + phoneTextView.getText().toString().trim()));
                    activity.startActivity(callIntent);
                }
            });

        }


        final TextView cellTextView = (TextView) dialog.findViewById(R.id.cell_text_view);
        if (!TextUtils.isEmpty(cellNumber)) {
            cellTextView.setText(cellNumber);
            cellTextView.setTextColor(activity.getResources().getColor(R.color.text_color));
            cellTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:+" + cellTextView.getText().toString().trim()));
                    activity.startActivity(callIntent);
                }
            });
        }

        TextView contactWarningTextView = (TextView) dialog.findViewById(R.id.contact_warning);
        contactWarningTextView.setText(warnMsg);

        IconicsImageView closeButton = (IconicsImageView) dialog.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();

    }
}

