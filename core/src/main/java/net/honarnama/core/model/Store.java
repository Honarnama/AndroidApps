package net.honarnama.core.model;

import com.parse.ImageSelector;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by elnaz on 1/5/16.
 */
@ParseClassName("Store")
public class Store extends ParseObject {

    public Store() {
        super();
    }

    public static String NAME = "name";
    public static String DESCRIPTION = "description";
    public static String PHONE_NUMBER = "phoneNumber";
    public static String CELL_NUMBER = "cellNumber";
    public static String LOGO = "logo";
    public static String BANNER = "banner";
    public static String OWNER = "owner";
    public static String PROVINCE_ID = "provinceId";
    public static String CITY_ID = "cityId";


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

    public String getProvinceId()
    {
        return getString(PROVINCE_ID);
    }
    public void setProvinceId(String provinceId)
    {
        put(PROVINCE_ID, provinceId);
    }

    public String getCityId()
    {
        return getString(CITY_ID);
    }

    public void setCityId(String cityId)
    {
        put(CITY_ID, cityId);
    }
}
