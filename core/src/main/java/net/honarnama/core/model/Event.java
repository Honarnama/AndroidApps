package net.honarnama.core.model;

import com.crashlytics.android.Crashlytics;
import com.parse.FindCallback;
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

import java.util.Date;
import java.util.List;

import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * Created by elnaz on 1/5/16.
 */
public class Event {

    public Event() {
        super();
    }

    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/eventModel";

    public static String OBJECT_NAME = "Event";

    public String mName;
    public String mDescription;
    public String mPhoneNumber;
    public String mCellNumber;
    public String mBanner;
    public int mOwnerId;
    public EventCategory mCategory;
    public Province mProvince;
    public City mCity;
    public int mStatus;
    public boolean mValidityChecked;
    public int mId;
    public boolean mActive;
    public String mAddress;
    public Date mStartDate;
    public Date mEndDate;


    public static Number STATUS_CODE_CONFIRMATION_WAITING = 0;
    public static Number STATUS_CODE_NOT_VERIFIED = -1;
    public static Number STATUS_CODE_VERIFIED = 1;

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

    public EventCategory getCategory() {
        return mCategory;
    }

    public void setCategory(EventCategory category) {
        mCategory = category;
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

    public boolean isActive() {
        return mActive;
    }

    public void setActive(boolean active) {
        mActive = active;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public Date getStartDate() {
        return mStartDate;
    }

    public void setStartDate(Date startDate) {
        mStartDate = startDate;
    }

    public Date getEndDate() {
        return mEndDate;
    }

    public void setEndDate(Date endDate) {
        mEndDate = endDate;
    }


}
