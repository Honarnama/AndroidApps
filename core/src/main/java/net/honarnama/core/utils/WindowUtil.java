package net.honarnama.core.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by elnaz on 1/28/16.
 */
public class WindowUtil {
    public static void hideKeyboard(Context ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
