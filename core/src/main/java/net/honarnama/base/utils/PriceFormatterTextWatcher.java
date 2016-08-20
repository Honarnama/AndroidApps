package net.honarnama.base.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.Locale;

/**
 * Created by elnaz on 3/25/16.
 */
public class PriceFormatterTextWatcher implements TextWatcher {
    private String current = "";

    public EditText mEditText;

    public PriceFormatterTextWatcher(EditText editText) {
        this.mEditText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        mEditText.setCursorVisible(false);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!s.toString().equals(current) && !s.toString().isEmpty()) {
            mEditText.removeTextChangedListener(this);
            try {
                Long longPrice = new Long(TextUtil.normalizePrice(s.toString()));
                String new_s = TextUtil.getPricePart(longPrice,
                        Locale.ENGLISH);
                mEditText.setText(new_s);
                mEditText.setSelection(new_s.length());
                current = new_s;
            } catch (Exception e) {

            }

            mEditText.addTextChangedListener(this);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() > 0) {
            mEditText.setCursorVisible(true);
        }

    }
}
