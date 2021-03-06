package net.honarnama.sell.utils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

import bolts.Task;
import bolts.TaskCompletionSource;


/**
 * Created by elnaz on 4/8/16.
 */
public class Uploader {

    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/uploader";

    File mFile;
    String mUploadUrl;
    TaskCompletionSource<Void> mTcs;

    public Uploader(File file, String uploadUrl) {
        mFile = file;
        mUploadUrl = uploadUrl;

        if (BuildConfig.DEBUG) {
            Log.d(DEBUG_TAG, "mUploadUrl: " + mUploadUrl);
        }
        mTcs = new TaskCompletionSource<>();
    }

    public Task<Void> upload() {
        new UploadAsync().execute();
        return mTcs.getTask();
    }

    public class UploadAsync extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
                RequestBody requestBody = RequestBody.create(MEDIA_TYPE_JPEG, mFile);

                final Request request = new Request.Builder()
                        .url(mUploadUrl)
                        .put(requestBody)
                        .header("Content-Type", MEDIA_TYPE_JPEG.toString())
                        .header("Host", Uri.parse(mUploadUrl).getHost())
                        .build();

                if (BuildConfig.DEBUG) {
                    Log.d(DEBUG_TAG, "Upload request: " + request.toString());
                }

                OkHttpClient okHttpClient = new OkHttpClient();

                okHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
                okHttpClient.setReadTimeout(30, TimeUnit.SECONDS);
                okHttpClient.setWriteTimeout(30, TimeUnit.SECONDS);

                okHttpClient.setCache(null);
                Call call = okHttpClient.newCall(request);
                Response response = call.execute();

                if (BuildConfig.DEBUG) {
                    Log.d(DEBUG_TAG, "Upload response: " + response.toString());
                }

                if (response.isSuccessful()) {
//                    Picasso.with(HonarnamaBaseApp.getInstance()).invalidate(mFile);
                    Glide.with(HonarnamaBaseApp.getInstance()).load(mFile)
                            .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true);
                    mTcs.trySetResult(null);
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.e(DEBUG_TAG, "Upload failed. " + response.message());
                    } else {
                        Crashlytics.log(Log.ERROR, DEBUG_TAG, "Upload failed. " + response.message());
                    }
                    mTcs.trySetError(new Exception("Unexpected code: " + response));
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    Log.e(DEBUG_TAG, "Upload failed. ", e);
                } else {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    String stackTrace = sw.toString();
                    Crashlytics.log(Log.ERROR, DEBUG_TAG, "Upload failed. Error: " + e + ". stackTrace: " + stackTrace);
                }
                mTcs.trySetError(e);
            }

            return null;
        }

    }

}
