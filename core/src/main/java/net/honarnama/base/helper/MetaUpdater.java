package net.honarnama.base.helper;

import com.crashlytics.android.Crashlytics;

import net.honarnama.GRPCUtils;
import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.interfaces.MetaUpdateListener;
import net.honarnama.base.model.ArtCategory;
import net.honarnama.nano.EventCategory;
import net.honarnama.nano.Location;
import net.honarnama.nano.MetaReply;
import net.honarnama.nano.MetaServiceGrpc;
import net.honarnama.nano.ReplyProperties;
import net.honarnama.nano.RequestProperties;
import net.honarnama.nano.SimpleRequest;

import android.content.SharedPreferences;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

import bolts.Continuation;
import bolts.Task;
import io.fabric.sdk.android.services.concurrency.AsyncTask;

/**
 * Created by elnaz on 4/19/16.
 */
public class MetaUpdater extends AsyncTask<Void, Void, MetaReply> {
    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/metaUpdater";
    MetaUpdateListener mMetaCallback;
    long mMetaVersion = 0;

    public MetaUpdater(MetaUpdateListener metaCallback, long metaVersion) {
        mMetaCallback = metaCallback;
        mMetaVersion = metaVersion;
    }

    @Override
    protected MetaReply doInBackground(Void... param) {

        if (BuildConfig.DEBUG) {
            Log.d(DEBUG_TAG, "Current meta version is: " + mMetaVersion);
        }

        MetaReply metaReply = getMetaData(mMetaVersion);
        return metaReply;
    }


    protected void onPostExecute(final MetaReply metaReply) {
        if (metaReply != null) {
            if (BuildConfig.DEBUG) {
                Log.d(DEBUG_TAG, "meta reply: " + metaReply);
            }
            switch (metaReply.replyProperties.statusCode) {
                case ReplyProperties.OK:
                    if (BuildConfig.DEBUG) {
                        Log.d(DEBUG_TAG, "Updating Meta to: " + metaReply.replyProperties.etag);
                    }
                    updateLocalMeta(metaReply);
                    break;

                case ReplyProperties.NOT_MODIFIED:
                    if (mMetaCallback != null) {
                        mMetaCallback.onMetaUpdateDone(ReplyProperties.NOT_MODIFIED);
                    }
                    if (BuildConfig.DEBUG) {
                        Log.d(DEBUG_TAG, "Meta is not modified.");
                    }
                    break;

                case ReplyProperties.SERVER_ERROR:
                    if (mMetaCallback != null) {
                        mMetaCallback.onMetaUpdateDone(ReplyProperties.SERVER_ERROR);
                    }
                    if (BuildConfig.DEBUG) {
                        Log.e(DEBUG_TAG, "Server error occured trying to update meta.");
                    } else {
                        Crashlytics.logException(new Throwable("Server error occured trying to update meta."));
                    }
                    break;

                case ReplyProperties.UPGRADE_REQUIRED:
                    if (mMetaCallback != null) {
                        mMetaCallback.onMetaUpdateDone(ReplyProperties.UPGRADE_REQUIRED);
                    }
                    break;

                case ReplyProperties.CLIENT_ERROR:
                    if (mMetaCallback != null) {
                        mMetaCallback.onMetaUpdateDone(ReplyProperties.CLIENT_ERROR);
                    }
                    break;

                case ReplyProperties.NOT_AUTHORIZED:
                    if (mMetaCallback != null) {
                        mMetaCallback.onMetaUpdateDone(ReplyProperties.NOT_AUTHORIZED);
                    }
                    break;
            }

        } else {

            if (mMetaCallback != null) {
                mMetaCallback.onMetaUpdateDone(ReplyProperties.CLIENT_ERROR);
            }
            if (BuildConfig.DEBUG) {
                Log.d(DEBUG_TAG, "meta reply was null");
            }

        }
    }

    private void updateLocalMeta(final MetaReply metaReply) {
        final net.honarnama.nano.ArtCategory[] artCategories = metaReply.artCategories;
        final EventCategory[] eventCategories = metaReply.eventCategories;
        final Location[] locations = metaReply.locations;

        ArtCategory.resetArtCategories(artCategories).onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                return net.honarnama.base.model.EventCategory.resetEventCategories(eventCategories);
            }
        }).onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                return net.honarnama.base.model.Location.resetLocations(locations);
            }
        }).continueWith(new Continuation<Void, Object>() {
            @Override
            public Object then(Task<Void> task) throws Exception {
                if (mMetaCallback != null) {
                    mMetaCallback.onMetaUpdateDone(ReplyProperties.OK);
                }
                if (task.isFaulted()) {
                    if (BuildConfig.DEBUG) {
                        Log.e(DEBUG_TAG, "Error while trying to update meta data. // Error: " + task.getError());
                    } else {
                        Crashlytics.log(Log.ERROR, DEBUG_TAG, "Error while trying to update meta data. // Error: " + task.getError());
                    }
                } else {
                    SharedPreferences.Editor editor = HonarnamaBaseApp.getAppSharedPref().edit();
                    editor.putLong(HonarnamaBaseApp.PREF_KEY_META_VERSION, metaReply.replyProperties.etag);
                    editor.commit();
                    return null;
                }
                return null;
            }
        });
    }

    // Not to be run in the UI thread
    public MetaReply getMetaData(long currentMetaVersion) {
        RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
        rp.ifNotMatchEtag = currentMetaVersion;
        SimpleRequest req = new SimpleRequest();
        req.requestProperties = rp;
        if (BuildConfig.DEBUG) {
            Log.d("GRPC-HN", "updateMetaData :: rp= " + rp);
        }
        try {
            MetaServiceGrpc.MetaServiceBlockingStub stub = GRPCUtils.getInstance().getMetaServiceGrpc();
            if (BuildConfig.DEBUG) {
                Log.d("GRPC-HN", "updateMetaData :: stub= " + stub);
            }
            MetaReply reply = stub.meta(req);
            if (BuildConfig.DEBUG) {
                Log.d("GRPC-HN", "updateMetaData :: Got Reply");
            }
            if (reply.replyProperties != null && BuildConfig.DEBUG) {
                Log.d("GRPC-HN", "updateMetaData :: reply.statusCode= " + reply.replyProperties.statusCode);
                Log.d("GRPC-HN", "updateMetaData :: reply.serverVersion= " + reply.replyProperties.serverVersion);
            }
            return reply;
//        // Test Part
//        StringBuilder sb = new StringBuilder();
//        for (EventCategory ev : reply.eventCategories) {
//            sb.append(ev.name);
//            sb.append(" / ");
//        }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e("GRPC-HN", "Error getting meta. Error: " + e, e);
            } else {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String stackTrace = sw.toString();
                Crashlytics.log(Log.ERROR, "GRPC-HN", "Error getting meta. Error: " + e + ". stackTrace: " + stackTrace);
                Crashlytics.logException(e);
            }
            return null;
        }
    }

}