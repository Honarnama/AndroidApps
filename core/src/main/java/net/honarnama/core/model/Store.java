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
public class Store {

    public Store() {
        super();
    }

    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/storeModel";

    public static String OBJECT_NAME = "Store";

    public static int STATUS_CODE_CONFIRMATION_WAITING = 0;
    public static int STATUS_CODE_NOT_VERIFIED = -1;
    public static int STATUS_CODE_VERIFIED = 1;

    public String mName;
    public String mDescription;
    public String mPhoneNumber;
    public String mCellNumber;
    public String mLogo;
    public String mBanner;
    public int mOwnerId;
    public Province mProvince;
    public City mCity;
    public int mStatus;
    public boolean mValidityChecked;
    public int mId;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    public String getCellNumber() {
        return mCellNumber;
    }

    public void setCellNumber(String cellNumber) {
        mCellNumber = cellNumber;
    }

    public String getLogo() {
        return mLogo;
    }

    public void setLogo(String logo) {
        mLogo = logo;
    }

    public String getBanner() {
        return mBanner;
    }

    public void setBanner(String banner) {
        mBanner = banner;
    }

    public int getOwnerId() {
        return mOwnerId;
    }

    public void setOwnerId(int ownerId) {
        mOwnerId = ownerId;
    }

    public Province getProvince() {
        return mProvince;
    }

    public void setProvince(Province province) {
        mProvince = province;
    }

    public City getCity() {
        return mCity;
    }

    public void setCity(City city) {
        mCity = city;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public boolean isValidityChecked() {
        return mValidityChecked;
    }

    public void setValidityChecked(boolean validityChecked) {
        mValidityChecked = validityChecked;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public static Task<Boolean> checkIfUserHaveStore(Context context) {
        final TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();

        if (!NetworkManager.getInstance().isNetworkEnabled(false)) {
            tcs.setError(new NetworkErrorException("No network connection + Offline ddata not available for store"));
            return tcs.getTask();
        }

        // TODO ask server
        tcs.setResult(true);

        return tcs.getTask();
    }


    public static Task<Store> getStoreByOwner(final int userId) {
        final TaskCompletionSource<Store> tcs = new TaskCompletionSource<>();

//        final ParseQuery<Store> parseQuery = ParseQuery.getQuery(Store.class);
//        parseQuery.whereEqualTo(OWNER, parseUser);
//        final SharedPreferences sharedPref = HonarnamaBaseApp.getInstance().getSharedPreferences(HonarnamaUser.getCurrentUser().getUsername(), Context.MODE_PRIVATE);
//
//        if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
//            tcs.setError(new NetworkErrorException("Network connection failed"));
//            return tcs.getTask();
//        }
//
//        parseQuery.getFirstInBackground(new GetCallback<Store>() {
//            @Override
//            public void done(Store store, ParseException e) {
//                if (e == null) {
//                    tcs.trySetResult(store);
//                } else {
//                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
//                        tcs.trySetResult(null);
//                    } else {
//                        if (BuildConfig.DEBUG) {
//                            Log.e(DEBUG_TAG, "Finding store for owner " + parseUser.getObjectId() + " failed. Code: " + e.getCode() + " // " + e.getMessage(), e);
//                        } else {
//                            Crashlytics.log(Log.ERROR, DEBUG_TAG, "Finding store for owner " + parseUser.getObjectId() + " failed. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e);
//                        }
//                        tcs.trySetError(e);
//                    }
//                }
//            }
//        });
        //TODO
        tcs.trySetResult(null);
        return tcs.getTask();
    }
}
