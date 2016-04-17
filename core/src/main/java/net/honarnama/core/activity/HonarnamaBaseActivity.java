package net.honarnama.core.activity;

import com.crashlytics.android.Crashlytics;

import net.honarnama.GRPCUtils;
import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.R;
import net.honarnama.core.model.ArtCategory;
import net.honarnama.core.utils.CommonUtil;
import net.honarnama.nano.EventCategory;
import net.honarnama.nano.Location;
import net.honarnama.nano.MetaReply;
import net.honarnama.nano.MetaServiceGrpc;
import net.honarnama.nano.ReplyProperties;
import net.honarnama.nano.RequestProperties;
import net.honarnama.nano.SimpleRequest;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import bolts.Continuation;
import bolts.Task;
import io.fabric.sdk.android.services.concurrency.AsyncTask;

/**
 * Created by reza on 7/23/15.
 */
public abstract class HonarnamaBaseActivity extends AppCompatActivity {

    private boolean announced = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG && !announced) {
            Log.d(HonarnamaBaseApp.PRODUCTION_TAG, "Activity created,\tadb catlog tag:   'Honarnama/" + getLocalClassName() + ":V'");
            announced = true;
        }
    }

    String getDebugTag() {
        if (BuildConfig.DEBUG) {
            return HonarnamaBaseApp.PRODUCTION_TAG + "/" + getLocalClassName();
        } else {
            return HonarnamaBaseApp.PRODUCTION_TAG;
        }
    }

    String getMessage(String sharedMsg, String debugMsg) {
        if ((debugMsg != null) && BuildConfig.DEBUG) {
            String message;
            if (sharedMsg != null) {
                message = sharedMsg + " //  debugMsg: " + debugMsg;
            } else {
                message = debugMsg;
            }
            return message;
        } else if (sharedMsg != null) {
            return sharedMsg;
        }
        return null;
    }

    public void logE(String sharedMsg, String debugMsg, Throwable throwable) {
        if (BuildConfig.DEBUG) {
            Log.e(getDebugTag(), getMessage(sharedMsg, debugMsg), throwable);
        } else if (sharedMsg != null) {
            Crashlytics.log(Log.ERROR, getDebugTag(), sharedMsg);
        }
    }

    public void logE(String sharedMsg, String debugMsg) {
        logE(sharedMsg, debugMsg, null);
    }

    public void logE(String sharedMsg) {
        logE(sharedMsg, null, null);
    }

    public void logI(String sharedMsg, String debugMsg) {
        Log.i(getDebugTag(), getMessage(sharedMsg, debugMsg));
    }

    public void logD(String sharedMsg, String debugMsg) {
        if (BuildConfig.DEBUG) {
            Log.d(getDebugTag(), getMessage(sharedMsg, debugMsg));
        }
    }

    public void logD(String debugMsg) {
        if (BuildConfig.DEBUG) {
            Log.d(getDebugTag(), getMessage(null, debugMsg));
        }
    }

    public void askToRate(final String callingApp) {
        final Dialog dialog = new Dialog(this, R.style.CustomDialogTheme);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.ask_for_starts_dialog);
        Button letsRateBtn = (Button) dialog.findViewById(R.id.lets_rate);
        Button rateLaterBtn = (Button) dialog.findViewById(R.id.rate_later);
        letsRateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBazaarRatingIntent(callingApp);

                SharedPreferences.Editor editor = HonarnamaBaseApp.getCommonSharedPref().edit();
                if (callingApp == HonarnamaBaseApp.PREF_NAME_SELL_APP) {
                    editor.putBoolean(HonarnamaBaseApp.PREF_KEY_SELL_APP_RATED, true);
                } else {
                    editor.putBoolean(HonarnamaBaseApp.PREF_KEY_BROWSE_APP_RATED, true);
                }
                editor.commit();

                dialog.dismiss();
                finish();
            }
        });
        rateLaterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });
        dialog.show();
    }

    public void callBazaarRatingIntent(final String callingApp) {
        if (CommonUtil.isPackageInstalled("com.farsitel.bazaar", this)) {
            Intent intent = new Intent(Intent.ACTION_EDIT);
            if (HonarnamaBaseApp.PACKAGE_NAME == HonarnamaBaseApp.SELL_PACKAGE_NAME) {
                intent.setData(Uri.parse("bazaar://details?id=" + HonarnamaBaseApp.SELL_PACKAGE_NAME));
            } else {
                intent.setData(Uri.parse("bazaar://details?id=" + HonarnamaBaseApp.BROWSE_PACKAGE_NAME));
            }
            intent.setPackage("com.farsitel.bazaar");
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.bazaar_is_not_installed, Toast.LENGTH_LONG).show();
        }
    }

    public void callBazaarViewAppPageIntent() {
        if (CommonUtil.isPackageInstalled("com.farsitel.bazaar", this)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (HonarnamaBaseApp.PACKAGE_NAME == HonarnamaBaseApp.SELL_PACKAGE_NAME) {
                intent.setData(Uri.parse("bazaar://details?id=" + HonarnamaBaseApp.SELL_PACKAGE_NAME));
            } else {
                intent.setData(Uri.parse("bazaar://details?id=" + HonarnamaBaseApp.BROWSE_PACKAGE_NAME));
            }
            intent.setPackage("com.farsitel.bazaar");
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.bazaar_is_not_installed, Toast.LENGTH_LONG).show();
        }
    }

    public void displayUpgradeRequiredDialog() {
        final Dialog dialog = new Dialog(this, R.style.CustomDialogTheme);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.upgrade_required_dialog);
        Button letsRateBtn = (Button) dialog.findViewById(R.id.lets_rate);
        Button rateLaterBtn = (Button) dialog.findViewById(R.id.rate_later);
        letsRateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBazaarViewAppPageIntent();
                dialog.dismiss();
            }
        });
        rateLaterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public class MetaUpdater extends AsyncTask<Void, Void, MetaReply> {
        public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/metaUpdater";
        String callingApp;

        public MetaUpdater(String callingApp) {
            this.callingApp = callingApp;
        }

        @Override
        protected MetaReply doInBackground(Void... param) {

            long metaVersion = HonarnamaBaseApp.getCommonSharedPref().getLong(HonarnamaBaseApp.PREF_KEY_META_VERSION, 0);

            if (BuildConfig.DEBUG) {
                logD("Current meta version is: " + metaVersion);
            }

            MetaReply metaReply = getMetaData(metaVersion);
            return metaReply;
        }


        protected void onPostExecute(final MetaReply reply) {
            if (reply != null) {
                logD("Meta reply:" + reply.replyProperties);
                switch (reply.replyProperties.statusCode) {
                    case ReplyProperties.OK:
                        logD("Updating Meta to: " + reply.replyProperties.etag);
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
                                    logE("Error while trying to update meta data. // Error: " + task.getError());
                                } else {
                                    SharedPreferences.Editor editor = HonarnamaBaseApp.getCommonSharedPref().edit();
                                    editor.putLong(HonarnamaBaseApp.PREF_KEY_META_VERSION, reply.replyProperties.etag);
                                    editor.commit();
                                    return null;
                                }
                                return null;
                            }
                        });
                        break;

                    case ReplyProperties.NOT_MODIFIED:
                        logD("Meta is not modified.");
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        logD("Server error occured. trying .");
                        break;

                    case ReplyProperties.UPGRADE_REQUIRED:
                        displayUpgradeRequiredDialog();
                        break;
                }

            } else {
                logD("Meta reply was null");
            }
        }

        // Not to be run in the UI thread
        public MetaReply getMetaData(long currentMetaVersion) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            rp.ifNotMatchEtag = currentMetaVersion;
            SimpleRequest req = new SimpleRequest();
            req.requestProperties = rp;

            Log.d("GRPC-HN", "updateMetaData :: rp= " + rp);
            try {
                MetaServiceGrpc.MetaServiceBlockingStub stub = GRPCUtils.getInstance().getMetaServiceGrpc();
                Log.d("GRPC-HN", "updateMetaData :: stub= " + stub);
                MetaReply reply = stub.meta(req);
                Log.d("GRPC-HN", "updateMetaData :: Got Reply");
                if (reply.replyProperties != null) {
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
            } catch (InterruptedException e) {
                Log.d("GRPC-HN", "Error getting meta. Error: " + e);
                return null;
            }
        }

    }
}
