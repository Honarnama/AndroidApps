package net.honarnama.base.model;

import net.honarnama.HonarnamaBaseApp;

/**
 * Created by elnaz on 1/5/16.
 */
public class Store {

    public Store() {
        super();
    }

    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/storeModel";


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

}
