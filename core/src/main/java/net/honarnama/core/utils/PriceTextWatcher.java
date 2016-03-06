package net.honarnama.core.utils;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by elnaz on 7/15/15.
 */
public class PriceTextWatcher implements TextWatcher {

    public EditText editText;

    public PriceTextWatcher(EditText editText) {
        this.editText = editText;
    }

    public void afterTextChanged(Editable s) {

    }


    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
//        String val = editText.getText().toString();
//        if (!TextUtils.isEmpty(val)) {
////            NumberFormat formatter = TextUtil.getPriceNumberFormmat(Locale.ENGLISH);
////            String formattedPrice = formatter.format(val);
////            String price = TextUtil.convertEnNumberToFa(formattedPrice);
//            editText.setText(val+"1");
////            editText.setSelection(val.length()+1);
//        }

    }
}
