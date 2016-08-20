package net.honarnama.base.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Created by elnaz on 7/15/15.
 */
public class PersianTextWatcher implements TextWatcher {

    public EditText mEditText;
    public int mOriginalInputType;

    public PersianTextWatcher(EditText editText) {
        this.mEditText = editText;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        mOriginalInputType = mEditText.getInputType();
    }

    @Override
    public void afterTextChanged(Editable s) {
        mEditText.removeTextChangedListener(this);
        try {
            String rawString = mEditText.getText().toString();
            Character lastChar = rawString.charAt(rawString.length() - 1);
            String formattedString = TextUtil.convertEnNumberToFa(rawString);

//            s.replace(0, rawString.length(), formattedString, 0, formattedString.length());
            mEditText.setText(formattedString);
            mEditText.setSelection(mEditText.getText().length());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mEditText.addTextChangedListener(this);
    }

}





