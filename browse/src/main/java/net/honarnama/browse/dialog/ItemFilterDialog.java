package net.honarnama.browse.dialog;

import com.mikepenz.iconics.view.IconicsImageView;

import net.honarnama.browse.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

/**
 * Created by elnaz on 3/2/16.
 */
public class ItemFilterDialog {

    public void showDialog(final Activity activity) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.item_filter_dialog);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

//
//        final TextView phoneTextView = (TextView) dialog.findViewById(R.id.phone_text_view);
//        final TextView cellTextView = (TextView) dialog.findViewById(R.id.cell_text_view);
//

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

