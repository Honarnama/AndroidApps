package net.honarnama.sell.utils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.UploadPartRequest;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.nano.UploadInfo;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * Created by elnaz on 4/8/16.
 */
public class AwsUploader {
    File mFile;
    UploadInfo mUploadInfo;
    TaskCompletionSource<Void> mTcs;

    public AwsUploader(File file, UploadInfo uploadInfo) {
        mFile = file;
        mUploadInfo = uploadInfo;
        mTcs = new TaskCompletionSource<>();
    }

    public Task<Void> upload() {

        try {
            new UploadAsync().execute();
//            PutObjectRequest putRequest = new PutObjectRequest(uploadInfo.bucketName, uploadInfo.key,
//                    fileToUpload);
//            PutObjectResult putResponse = s3Client.putObject(putRequest);
//
//            GetObjectRequest getRequest = new GetObjectRequest(uploadInfo.bucketName, uploadInfo.key);
//            S3Object getResponse = s3Client.getObject(getRequest);
//            InputStream myObjectBytes = getResponse.getObjectContent();
//            // Do what you want with the object....
//            myObjectBytes.close();
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

            Log.e("inja", "bucket name is: "+ mUploadInfo.bucketName);
            AmazonS3Client s3Client = new AmazonS3Client(new AnonymousAWSCredentials());
            s3Client.setS3ClientOptions(new S3ClientOptions().withPathStyleAccess(true));
            s3Client.setEndpoint("https://honarnama.net:9000"); // TODO: move to config

            s3Client.setRegion(Region.getRegion(Regions.US_EAST_1));

            ClientConfiguration configuration = new ClientConfiguration();
            configuration.setMaxErrorRetry(3);
            configuration.setConnectionTimeout(5 * 1000);
            configuration.setSocketTimeout(5 * 1000);
            configuration.setProtocol(Protocol.HTTPS);
            s3Client.setConfiguration(configuration);

            InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(
                    mUploadInfo.bucketName, mUploadInfo.key);
            List<PartETag> partETags = new ArrayList<PartETag>();
            InitiateMultipartUploadResult initResponse =
                    s3Client.initiateMultipartUpload(initRequest);
            long contentLength = mFile.length();
//            long partSize = 5 * 1024 * 1024; // Set part size to 5 MB.
            long partSize = contentLength;
            try {
//                // Last part can be less than 5 MB. Adjust part size.
//                partSize = Math.min(partSize, (contentLength));
//
//                // Create request to upload a part.
//                UploadPartRequest uploadRequest = new UploadPartRequest()
//                        .withBucketName(mUploadInfo.bucketName).withKey(mUploadInfo.key)
//                        .withUploadId(initResponse.getUploadId()).withPartNumber(1)
//                        .withFileOffset(0)
//                        .withFile(mFile)
//                        .withPartSize(partSize);
//
//                // Upload part and add response to our list.
//                partETags.add(s3Client.uploadPart(uploadRequest).getPartETag());
//
//                // Step 3: Complete.
//                CompleteMultipartUploadRequest compRequest = new
//                        CompleteMultipartUploadRequest(mUploadInfo.bucketName,
//                        mUploadInfo.key,
//                        initResponse.getUploadId(),
//                        partETags);
//
//                s3Client.completeMultipartUpload(compRequest);

                long filePosition = 0;
                for (int i = 1; filePosition < contentLength; i++) {
                    // Last part can be less than 5 MB. Adjust part size.
                    partSize = Math.min(partSize, (contentLength - filePosition));

                    // Create request to upload a part.
                    UploadPartRequest uploadRequest = new UploadPartRequest()
                            .withBucketName(mUploadInfo.bucketName).withKey(mUploadInfo.key)
                            .withUploadId(initResponse.getUploadId()).withPartNumber(i)
                            .withFileOffset(filePosition)
                            .withFile(mFile)
                            .withPartSize(partSize);

                    // Upload part and add response to our list.
                    partETags.add(s3Client.uploadPart(uploadRequest).getPartETag());

                    filePosition += partSize;
                }

                // Step 3: Complete.
                CompleteMultipartUploadRequest compRequest = new
                        CompleteMultipartUploadRequest(mUploadInfo.bucketName,
                        mUploadInfo.key,
                        initResponse.getUploadId(),
                        partETags);

                s3Client.completeMultipartUpload(compRequest);

            } catch (Exception e) {
                //TODO log
                s3Client.abortMultipartUpload(new AbortMultipartUploadRequest(
                        mUploadInfo.bucketName, mUploadInfo.key, initResponse.getUploadId()));
                mTcs.trySetError(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}
