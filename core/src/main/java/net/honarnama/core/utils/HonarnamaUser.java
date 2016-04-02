package net.honarnama.core.utils;

import com.parse.FunctionCallback;
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.HashMap;

/**
 * Created by elnaz on 7/26/15.
 */
public class HonarnamaUser {

    private static String token;

    public static String getUserEnteredEmailAddress() {
//        return getCurrentUser().get("userEnteredEmailAddress").toString();
        //TODO
        return "mojahedi.elnaz@gmail.com";
    }

    public static String getName() {
        //return getCurrentUser().get("name").toString();
        //TODO
        return "Elnaz";
    }

    public static void telegramLogInInBackground(String token, final LogInCallback logInCallback) {
        HashMap<String, Object> params = new HashMap<String, Object>();
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
//        if (getCurrentUser() == null) {
//            return false;
//        }
//        return getActivationMethod().isUserVerified(getCurrentUser());
        //TODO
        return false;
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

    public static int getUserId() {
        //TODO
        return 1;
    }
}
