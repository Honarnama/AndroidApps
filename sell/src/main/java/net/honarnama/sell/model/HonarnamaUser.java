package net.honarnama.sell.model;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.sell.activity.LoginActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Created by elnaz on 7/26/15.
 */
public class HonarnamaUser {
    private static String mToken;
    private static String Name;
    private static int Gender;
    private static long Id;
    private static String TelegramToken;

    public static void login(String token) {
        SharedPreferences.Editor editor = HonarnamaBaseApp.getCommonSharedPref().edit();
        editor.putString(HonarnamaBaseApp.PREF_KEY_LOGIN_TOKEN, token);
        editor.commit();
        mToken = token;
    }

    public static void logout(Activity activity) {
        SharedPreferences.Editor editor = HonarnamaBaseApp.getCommonSharedPref().edit();
        //TODO is there something else to clear?
        editor.putString(HonarnamaBaseApp.PREF_KEY_LOGIN_TOKEN, "");
        editor.commit();
        mToken = null;

        if (activity != null) {
            Intent intent = new Intent(activity, LoginActivity.class);
            activity.finish();
            activity.startActivity(intent);
        }
    }

    public static String getName() {
        return Name;
    }

    public static void setName(String name) {
        Name = name;
    }

    public static int getGender() {
        return Gender;
    }

    public static void setGender(int gender) {
        Gender = gender;
    }

    public static long getId() {
        return Id;
    }

    public static void setId(long id) {
        Id = id;
    }

    public static void setTelegramToken(String telegramToken) {
        TelegramToken = telegramToken;
    }

//    public static ActivationMethod getActivationMethod() {
//        ParseUser user = getCurrentUser();
//        String activationMethod = "";
//        if (user != null) {
//            activationMethod = user.getString("activationMethod");
//        }
//        if ("email".equals(activationMethod)) {
//            return ActivationMethod.EMAIL;
//        } else if ("mobileNumber".equals(activationMethod)) {
//            return ActivationMethod.MOBILE_NUMBER;
//        } else {
//            return ActivationMethod.UNKNOWN;
//        }
//        return ActivationMethod.MOBILE_NUMBER;
//    }

    public static boolean isLoggedIn() {
        if (!TextUtils.isEmpty(mToken)) {
            return true;
        } else {
            String loginToken = HonarnamaBaseApp.getCommonSharedPref().getString(HonarnamaBaseApp.PREF_KEY_LOGIN_TOKEN, "");
            if (TextUtils.isEmpty(loginToken)) {
                return false;
            } else {
                mToken = loginToken;
                return true;
            }
        }
    }

    public static enum ActivationMethod {
        EMAIL("emailVerified"),
        MOBILE_NUMBER("telegramVerified"),
        UNKNOWN(null);

        private final String verificationFieldName;

        ActivationMethod(String verificationFieldName) {
            this.verificationFieldName = verificationFieldName;
        }
//
//        public boolean isUserVerified(ParseUser user) {
//            if (verificationFieldName != null) {
//                return user.getBoolean(verificationFieldName);
//            }
//            return false;
//        }
    }

}
