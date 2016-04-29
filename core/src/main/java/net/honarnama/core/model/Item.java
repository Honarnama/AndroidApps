package net.honarnama.core.model;

import net.honarnama.HonarnamaBaseApp;

import java.io.File;

public class Item {
    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/itemModel";
    public final static int NUMBER_OF_IMAGES = 4;


    //Defining fields
    public String mName;
    public String mDescription;
    public ArtCategory mCategory;
    public Number mPrice;
    public File mImage1;
    public File mImage2;
    public File mImage3;
    public File mImage4;
    public boolean mValidityChecked;
    public int mStatus;
    public int mOwnerId;
    public Store mStore;
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

    public ArtCategory getCategory() {
        return mCategory;
    }

    public void setCategory(ArtCategory category) {
        mCategory = category;
    }

    public Number getPrice() {
        return mPrice;
    }

    public void setPrice(Number price) {
        mPrice = price;
    }

    public File getImage1() {
        return mImage1;
    }

    public void setImage1(File image1) {
        mImage1 = image1;
    }

    public File getImage2() {
        return mImage2;
    }

    public void setImage2(File image2) {
        mImage2 = image2;
    }

    public File getImage3() {
        return mImage3;
    }

    public void setImage3(File image3) {
        mImage3 = image3;
    }

    public File getImage4() {
        return mImage4;
    }

    public void setImage4(File image4) {
        mImage4 = image4;
    }

    public boolean isValidityChecked() {
        return mValidityChecked;
    }

    public void setValidityChecked(boolean validityChecked) {
        mValidityChecked = validityChecked;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public int getOwnerId() {
        return mOwnerId;
    }

    public void setOwnerId(int ownerId) {
        mOwnerId = ownerId;
    }

    public Store getStore() {
        return mStore;
    }

    public void setStore(Store store) {
        mStore = store;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public Item(int ownerId, String name, String description, ArtCategory category, Number price, Store store) {
        super();
        setName(name);
        setDescription(description);
        setOwnerId(ownerId);
        setCategory(category);
        setPrice(price);
        if (store != null) {
            setStore(store);
        }
    }

    private void update(String name, String description, ArtCategory category, Number price, Store store) {
        setName(name);
        setDescription(description);
        setCategory(category);
        setPrice(price);
        if (store != null) {
            setStore(store);
        }
    }

    public File[] getImages() {
        File[] res = new File[NUMBER_OF_IMAGES];
        File imageFile;

        int count = 0;
        if (getImage1() != null) {
            imageFile = getImage1();
            res[count] = imageFile;
            count++;
        }
        if (getImage2() != null) {
            imageFile = getImage2();
            res[count] = imageFile;
            count++;
        }

        if (getImage3() != null) {
            imageFile = getImage3();
            res[count] = imageFile;
            count++;
        }

        if (getImage4() != null) {
            imageFile = getImage4();
            res[count] = imageFile;
            count++;
        }

        return res;
    }


}
