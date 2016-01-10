package net.honarnama.core.model;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

@ParseClassName("art_categories")
public class Category extends ParseObject {

    public static String OBJECT_NAME = "art_categories";
    public static String NAME = "name";
    public static String ORDER = "order";

    public Category() {
    }

    
}
