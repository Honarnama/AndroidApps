package net.honarnama.core.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.EditText;

/**
 * Created by elnaz on 7/15/15.
 */
public class GenericGravityTextWatcher implements TextWatcher {

    public EditText editText;
    public GenericGravityTextWatcher(EditText editText) {
        this.editText = editText;
    }
    public void afterTextChanged(Editable s) {
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
            // position the text type in the left top corner
            editText.setGravity(Gravity.LEFT);
        } else {
            // no text entered. Center the hint text.
            editText.setGravity(Gravity.RIGHT);
        }
    }
}
