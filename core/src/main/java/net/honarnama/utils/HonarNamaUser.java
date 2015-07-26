package net.honarnama.utils;

import com.parse.ParseUser;

import android.view.View;

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
        if (!isVerified) {
            return false;
        }
        return false;
    }
}
