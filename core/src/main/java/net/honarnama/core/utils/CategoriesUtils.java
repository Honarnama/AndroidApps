package net.honarnama.core.utils;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
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

/**
 * Created by reza on 12/12/15.
 */
public class CategoriesUtils {

    public static void cacheArtCategories(final Context context, final ProgressDialog syncingDataProgressDialog) {
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("art_categories");
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(final List<ParseObject> artCategories, ParseException e) {

                if (syncingDataProgressDialog != null) {
                    syncingDataProgressDialog.dismiss();
                }
                if (e == null) {
                    ParseObject.unpinAllInBackground("artCategories", artCategories, new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            ParseObject.pinAllInBackground("artCategories", artCategories, new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
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
