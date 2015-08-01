package net.honarnama.utils;

import com.parse.FunctionCallback;
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.HashMap;

/**
 * Created by elnaz on 7/26/15.
 */
public class HonarNamaUser extends ParseUser {

    public static boolean isLoggedIn() {
        if (getCurrentUser() != null) {
            return true;
        }
        return false;
    }

    public static void telegramLogInInBackground(String token, final LogInCallback logInCallback) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("token", token);
        ParseCloud.callFunctionInBackground("telegramLogInInBackground", params, new FunctionCallback<String>() {
            @Override
            public void done(String sessionToken, ParseException e) {
                if (e == null) {
                    ParseUser.becomeInBackground(sessionToken, logInCallback);
                } else {
                    logInCallback.done(null, e);
                }
            }
        });
    }

    public static boolean isShopOwner() {
        if (getCurrentUser() == null) {
            return false;
        }
        boolean isShopOwner = getCurrentUser().getBoolean("isShopOwner");
        return isShopOwner;
    }


    public static boolean isVerified() {

        String activationMethod = getCurrentUser().getString("activationMethod");
        boolean isVerified = false;
        if ("email".equals(activationMethod)) {
            isVerified = getCurrentUser().getBoolean("emailVerified");
        } else if ("mobileNumber".equals(activationMethod)) {
            isVerified = getCurrentUser().getBoolean("telegramVerified");
        }
        return isVerified;
    }
}
