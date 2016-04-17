package net.honarnama.sell.utils;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import net.honarnama.HonarnamaBaseApp;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import bolts.Task;
import bolts.TaskCompletionSource;


/**
 * Created by elnaz on 4/8/16.
 */
public class Uploader {
    File mFile;
    String mUploadUrl;
    TaskCompletionSource<Void> mTcs;

    public Uploader(File file, String uploadUrl) {
        mFile = file;
        mUploadUrl = uploadUrl;

        Log.e("inja", "mUploadUrl is: " + mUploadUrl);
        mTcs = new TaskCompletionSource<>();
    }

    public Task<Void> upload() {

        try {
            new UploadAsync().execute();
        } catch (Exception e) {
            //TODO log
        }
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
                Log.e("inja", "requestBody=" + requestBody.toString());

                final Request request = new Request.Builder()
                        .url(mUploadUrl)
                        .put(requestBody)
                        .header("Content-Type", MEDIA_TYPE_JPEG.toString())
                        .header("Host", Uri.parse(mUploadUrl).getHost())
                        .build();
                Log.e("inja", "request=" + request.toString());
                Log.e("inja", "headers=" + request.headers());

                OkHttpClient okHttpClient = new OkHttpClient();
                okHttpClient.setCache(null);
                Call call = okHttpClient.newCall(request);
                Log.e("inja", "call=" + call.toString());
                Response response = call.execute();
                Log.e("inja", "Uploading image response. " + response.toString());

                if (response.isSuccessful()) {
                    Picasso.with(HonarnamaBaseApp.getInstance()).invalidate(mFile);
                    mTcs.trySetResult(null);
                } else {
                    //TODO log
                    mTcs.trySetError(new IOException("Unexpected code " + response));
                }
            } catch (Exception e) {
                //TODO log
                Log.e("inja", "Error Uploading image. ", e);
                mTcs.trySetError(e);
            }

            return null;
        }

    }

}
