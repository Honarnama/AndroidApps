package net.honarnama.core.loader;

import com.crashlytics.android.Crashlytics;

import net.honarnama.GRPCUtils;
import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.model.ArtCategory;
import net.honarnama.nano.EventCategory;
import net.honarnama.nano.Location;
import net.honarnama.nano.MetaReply;
import net.honarnama.nano.ReplyProperties;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import bolts.Continuation;
import bolts.Task;
import io.fabric.sdk.android.services.concurrency.AsyncTask;

/**
 * Created by elnaz on 3/28/16.
 */
public class MetaUpdater extends AsyncTask<Void, Void, MetaReply> {
    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/metaUpdater";

    public Context mContext;
    SharedPreferences mSharedPref;

    public MetaUpdater(Context context) {
        mContext = context;
        mSharedPref = HonarnamaBaseApp.getInstance().getSharedPreferences(HonarnamaBaseApp.PREF_NAME_COMMON, Context.MODE_PRIVATE);
    }

    @Override
    protected MetaReply doInBackground(Void... param) {

        long metaVersion = mSharedPref.getLong(HonarnamaBaseApp.PREF_KEY_META_VERSION, 0);
        Log.e("inja", "current meta version is: " + metaVersion);

        try {
            MetaReply metaReply = GRPCUtils.getInstance().getMetaData(metaVersion);
            return metaReply;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onProgressUpdate() {

    }

    protected void onPostExecute(final MetaReply reply) {
        if (reply != null) {
            Log.e("inja", "statusCode is" + reply.replyProperties.statusCode);

            if (reply.replyProperties.statusCode == ReplyProperties.OK) {
                Log.e("inja", "Update Meta Info for meta version: " + reply.replyProperties.etag);
                //TODO updateMeta

                //        // Test Part
//        StringBuilder sb = new StringBuilder();
//        for (EventCategory ev : reply.eventCategories) {
//            sb.append(ev.name);
//            sb.append(" / ");
//        }

                final net.honarnama.nano.ArtCategory[] artCategories = reply.artCategories;
                final EventCategory[] eventCategories = reply.eventCategories;
                final Location[] locations = reply.locations;

                ArtCategory.resetArtCategories(artCategories).onSuccessTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<Void> task) throws Exception {
                        return net.honarnama.core.model.EventCategory.resetEventCategories(eventCategories);
                    }
                }).onSuccessTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<Void> task) throws Exception {
                        return net.honarnama.core.model.Location.resetLocations(locations);
                    }
                }).continueWith(new Continuation<Void, Object>() {
                    @Override
                    public Object then(Task<Void> task) throws Exception {
                        if (task.isFaulted()) {
                            if (BuildConfig.DEBUG) {
                                Log.e(DEBUG_TAG, "Error while trying to update meta data. // Error: " + task.getError(), task.getError());
                            } else {
                                Crashlytics.log(Log.ERROR, DEBUG_TAG, "Error while trying to update meta data. // Error:  " + task.getError());
                            }
                        } else {
                            SharedPreferences.Editor editor = mSharedPref.edit();
                            editor.putLong(HonarnamaBaseApp.PREF_KEY_META_VERSION, reply.replyProperties.etag);
                            editor.commit();
                            return null;
                        }
                        return null;
                    }
                });
            }
            if (reply.replyProperties.statusCode == ReplyProperties.NOT_MODIFIED) {
                Log.e("inja", "Meta is the same");
            }
        }
    }
}
