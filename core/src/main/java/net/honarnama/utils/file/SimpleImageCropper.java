package net.honarnama.utils.file;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;

import java.io.File;
import java.util.List;

/**
 * Created by elnaz on 8/1/15.
 */
public class SimpleImageCropper {

    Activity mActivity;
    Intent mCropIntent;
    Integer mAvailableCroppingAppCount;
    List<ResolveInfo> mResolveInfoList;

    public SimpleImageCropper(Activity activity) {
        mActivity = activity;
    }

    public boolean checkIfDeviceSupportsImageCrop() {
        mCropIntent = new Intent("com.android.camera.action.CROP");
        mCropIntent.setType("image/*");

        mResolveInfoList = mActivity.getApplicationContext().getPackageManager().queryIntentActivities(mCropIntent, 0);
        mAvailableCroppingAppCount = mResolveInfoList.size();
        if (mAvailableCroppingAppCount == 0) {
            return false;
        } else {
            return true;
        }
    }

    public void crop(Uri picUri, File imageFile, int intentCode, int width, int height, int aspectX, int aspectY) {
        mCropIntent.setData(picUri);
        mCropIntent.putExtra("outputX", width);
        mCropIntent.putExtra("outputY", height);
        mCropIntent.putExtra("aspectX", aspectX);
        mCropIntent.putExtra("aspectY", aspectY);
        mCropIntent.putExtra("scale", true);
        mCropIntent.putExtra("return-data", false);
        mCropIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageFile);

        mCropIntent.putExtra("output", Uri.fromFile(imageFile));

        if (mAvailableCroppingAppCount >= 1) {
            Intent intent = new Intent(mCropIntent);
            ResolveInfo res = mResolveInfoList.get(0);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            mActivity.startActivityForResult(intent, intentCode);

        }
    }

}
