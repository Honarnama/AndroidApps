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
public class HonarnamaUser extends ParseUser {

    public static boolean isAuthenticatedUser() {
        ParseUser user = ParseUser.getCurrentUser();
        if ((user != null) && user.isAuthenticated()) {
            return true;
        }
        return false;
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
        ParseUser user = getCurrentUser();
        String activationMethod = user.getString("activationMethod");
        if ("email".equals(activationMethod)) {
            return ActivationMethod.EMAIL;
        } else if ("mobileNumber".equals(activationMethod)) {
            return ActivationMethod.MOBILE_NUMBER;
        } else {
            return ActivationMethod.UNKNOWN;
        }
    }

    public static boolean isVerified() {
        if(getCurrentUser() == null)
        {
            return false;
        }
        return getActivationMethod().isUserVerified(getCurrentUser());
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
}
