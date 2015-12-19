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

    public Category() {
    }

    public static void cacheArtCategories(final Context context, final SharedPreferences sharedPref) {

        Toast.makeText(context, "cacheArtCategories", Toast.LENGTH_SHORT).show();

        final ProgressDialog syncingDataProgressDialog = new ProgressDialog(context);
        syncingDataProgressDialog.setCancelable(false);
        syncingDataProgressDialog.setMessage(context.getResources().getString(R.string.syncing_data));
        syncingDataProgressDialog.show();

        ParseQuery<Category> parseQuery = ParseQuery.getQuery(Category.class);
        parseQuery.findInBackground(new FindCallback<Category>() {
            public void done(final List<Category> artCategoriesList, ParseException e) {

                if (syncingDataProgressDialog != null) {
                    syncingDataProgressDialog.dismiss();
                }
                if (e == null) {
                    ParseObject.unpinAllInBackground(OBJECT_NAME, new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            ParseObject.pinAllInBackground(OBJECT_NAME, artCategoriesList, new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            SharedPreferences.Editor editor = sharedPref.edit();
                                            editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_FOR_CATEGORIES_SYNCED, true);
                                            editor.commit();
                                        }
                                    }
                            );
                        }
                    });


                } else {
                    Toast.makeText(context, context.getString(R.string.syncing_data_failed), Toast.LENGTH_LONG).show();
                    if (BuildConfig.DEBUG) {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getName(), "Receiving categories list failed. Code: " + e.getCode() +
                                "//" + e.getMessage() + " // " + e);
                    } else {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Receiving categories list failed. Code: " + e.getCode() +
                                "//" + e.getMessage() + " // " + e);
                    }
                }
            }
        });
    }

}
