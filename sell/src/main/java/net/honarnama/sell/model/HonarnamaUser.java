package net.honarnama.sell.model;

import com.parse.FunctionCallback;
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.sell.activity.LoginActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.HashMap;

/**
 * Created by elnaz on 7/26/15.
 */
public class HonarnamaUser {
    private static String mToken;
    private static String Name;
    private static int Gender;
    private static long Id;

    public static void login(String token) {
        SharedPreferences.Editor editor = HonarnamaBaseApp.getCommonSharedPref().edit();
        editor.putString(HonarnamaBaseApp.PREF_KEY_LOGIN_TOKEN, token);
        editor.commit();
        mToken = token;
    }

    public static void logout(Activity activity) {
        SharedPreferences.Editor editor = HonarnamaBaseApp.getCommonSharedPref().edit();
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


    public static void telegramLogInInBackground(String token, final LogInCallback logInCallback) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("token", token);
        ParseCloud.callFunctionInBackground("telegramLogin", params, new FunctionCallback<String>() {
            @Override
            public void done(String sessionToken, ParseException e) {
                if (e == null && (sessionToken != null)) {
                    ParseUser.becomeInBackground(sessionToken, logInCallback);
                } else {
                    logInCallback.done(null, e);
                }
            }
        });
    }

    public static ActivationMethod getActivationMethod() {
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
        //TODO
        return ActivationMethod.MOBILE_NUMBER;
    }

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

        public boolean isUserVerified(ParseUser user) {
            if (verificationFieldName != null) {
                return user.getBoolean(verificationFieldName);
            }
            return false;
        }
    }

    public static String getUsername() {
        //TODO
        return "mojahedi.elnaz@gmail.com";
    }


}
