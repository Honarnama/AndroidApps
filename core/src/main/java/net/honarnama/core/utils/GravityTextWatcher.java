package net.honarnama.core.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;

/**
 * Created by elnaz on 7/15/15.
 */
public class GravityTextWatcher implements TextWatcher {

    public EditText editText;

    public GravityTextWatcher(EditText editText) {
        this.editText = editText;
    }

    public void afterTextChanged(Editable s) {
    }


    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
            // position the text type in the left top corner
            editText.setGravity(Gravity.LEFT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                editText.setTextDirection(View.TEXT_DIRECTION_LTR);
            }

        } else {
            // no text entered. Center the hint text.
            editText.setGravity(Gravity.RIGHT);
            editText.setTextDirection(View.TEXT_DIRECTION_RTL);
        }
    }
}
