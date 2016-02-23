package net.honarnama.core.model;

import com.crashlytics.android.Crashlytics;
import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.utils.HonarnamaUser;
import net.honarnama.core.utils.NetworkManager;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * Created by elnaz on 1/5/16.
 */
@ParseClassName("Store")
public class Store extends ParseObject {

    public Store() {
        super();
    }

    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/storeModel";

    public static String OBJECT_NAME = "Store";

    public static String NAME = "name";
    public static String DESCRIPTION = "description";
    public static String PHONE_NUMBER = "phoneNumber";
    public static String CELL_NUMBER = "cellNumber";
    public static String LOGO = "logo";
    public static String BANNER = "banner";
    public static String OWNER = "owner";
    public static String PROVINCE = "province";
    public static String CITY = "city";
    public static String STATUS = "status";
    public static String VALIDITY_CHECKED = "validity_checked";
    public static String OBJECT_ID = "objectId";


    public static Number STATUS_CODE_CONFIRMATION_WAITING = 0;
    public static Number STATUS_CODE_NOT_VERIFIED = -1;
    public static Number STATUS_CODE_VERIFIED = 1;


    public String getName() {
        return getString(NAME);
    }

    public void setName(String value) {
        put(NAME, value);
    }

    public String getDescription() {
        return getString(DESCRIPTION);
    }

    public void setDescription(String value) {
        put(DESCRIPTION, value);
    }


    public String getPhoneNumber() {
        return getString(PHONE_NUMBER);
    }

    public void setPhoneNumber(String value) {
        put(PHONE_NUMBER, value);
    }

    public String getCellNumber() {
        return getString(CELL_NUMBER);
    }

    public void setCellNumber(String value) {
        put(CELL_NUMBER, value);
    }


    public ParseFile getLogo() {
        return getParseFile(LOGO);
    }

    public void setLogo(ParseFile parseFile) {
        put(LOGO, parseFile);
    }

    public Number getStatus() {
        return getNumber(STATUS);
    }


    public ParseFile getBanner() {
        return getParseFile(BANNER);
    }

    public void setBanner(ParseFile parseFile) {
        put(BANNER, parseFile);
    }

    public ParseUser getOwner() {
        return getParseUser(OWNER);
    }

    public void setOwner(ParseUser parseUser) {
        put(OWNER, parseUser);
    }

    public ParseObject getProvince() {
        return getParseObject(PROVINCE);
    }

    public void setProvince(Provinces province) {
        put(PROVINCE, province);
    }

    public ParseObject getCity() {
        return getParseObject(CITY);
    }

    public void setCity(City city) {
        put(CITY, city);
    }

    public static Task<Boolean> checkIfUserHaveStore(Context context) {
        final TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();
        ParseQuery<Store> query = ParseQuery.getQuery(Store.class);

        query.whereEqualTo(Store.OWNER, HonarnamaUser.getCurrentUser());

//        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences sharedPref = HonarnamaBaseApp.getInstance().getSharedPreferences(HonarnamaUser.getCurrentUser().getUsername(), Context.MODE_PRIVATE);

        if (!NetworkManager.getInstance().isNetworkEnabled(false)) {
            if (sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_STORE_SYNCED, false)) {
                if (BuildConfig.DEBUG) {
                    Log.d(DEBUG_TAG, "Getting store info from local datastore.");
                }
                query.fromLocalDatastore();
            } else {
                tcs.setError(new NetworkErrorException("No network connection + Offline ddata not available for store"));
                return tcs.getTask();
            }
        }

        query.getFirstInBackground(new GetCallback<Store>() {
            @Override
            public void done(Store object, ParseException e) {
                if (e == null) {
                    tcs.setResult(true);
                } else {
                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        tcs.setResult(false);
                    } else {
                        tcs.setError(e);
                    }
                }
            }
        });

        return tcs.getTask();
    }


    public static Task<Store> getStoreByOwner(final ParseUser parseUser) {
        final TaskCompletionSource<Store> tcs = new TaskCompletionSource<>();

        final ParseQuery<Store> parseQuery = ParseQuery.getQuery(Store.class);
        parseQuery.whereEqualTo(OWNER, parseUser);
        final SharedPreferences sharedPref = HonarnamaBaseApp.getInstance().getSharedPreferences(HonarnamaUser.getCurrentUser().getUsername(), Context.MODE_PRIVATE);
        if (sharedPref.getBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_STORE_SYNCED, false)) {
            if (BuildConfig.DEBUG) {
                Log.d(DEBUG_TAG, "Getting store for owner from local datastore");
            }
            parseQuery.fromLocalDatastore();
        } else {
            if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                tcs.setError(new NetworkErrorException("Network connection failed"));
                return tcs.getTask();
            }
        }

        parseQuery.getFirstInBackground(new GetCallback<Store>() {
            @Override
            public void done(Store store, ParseException e) {
                if (e == null) {
                    tcs.trySetResult(store);
                } else {
                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        tcs.trySetResult(null);
                    } else {
                        if (BuildConfig.DEBUG) {
                            Log.e(DEBUG_TAG, "Finding store for owner " + parseUser.getObjectId() + " failed. Code: " + e.getCode() + " // " + e.getMessage(), e);
                        } else {
                            Crashlytics.log(Log.ERROR, DEBUG_TAG, "Finding store for owner " + parseUser.getObjectId() + " failed. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e);
                        }
                        tcs.trySetError(e);
                    }
                }
            }
        });
        return tcs.getTask();
    }
}
