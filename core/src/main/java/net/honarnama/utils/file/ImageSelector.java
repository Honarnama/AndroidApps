package net.honarnama.utils.file;

import net.honarnama.HonarNamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextThemeWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by elnaz on 8/5/15.
 */
public class ImageSelector {

    public Context mContext;
    public String[] mImageSourceProvider;
    public String mImagePath;
    public Activity mActivity;

    public static final String INTENT_SELECTED_IMAGE_PATH = "net.honarnama.app.selected_image_path";

    public ImageSelector(Activity activity, Context context) {
        mActivity = activity;
        mContext = context;
        mImageSourceProvider = new String[]{mContext.getString(R.string.camera_option_text), mContext.getString(R.string.choose_from_gallery_option_text)};
    }

    public void selectPhoto(File photoFile) {
        final AlertDialog.Builder nationalCardImageOptionDialog = new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.DialogStyle));
        nationalCardImageOptionDialog.setTitle(mContext.getString(R.string.select_national_card_image_dialog_title));
        final File imageFile = photoFile;
        nationalCardImageOptionDialog.setItems(mImageSourceProvider, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
                        // Continue only if the File was successfully created
                        if (imageFile != null) {
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    Uri.fromFile(imageFile));
                            takePictureIntent.putExtra(INTENT_SELECTED_IMAGE_PATH, mImagePath);
                            mActivity.startActivityForResult(takePictureIntent, HonarNamaBaseApp.INTENT_CAPTURE_IMAGE_CODE);
                        }

                    }
                } else if (which == 1) {
                    Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    mActivity.startActivityForResult(pickPhotoIntent, HonarNamaBaseApp.INTENT_SELECT_IMAGE_CODE);
                }
                dialog.dismiss();
            }
        });
        nationalCardImageOptionDialog.show();

    }

    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Honarnama_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }

    public void removeTempImageFile(File tempFile)
    {
        if(tempFile.exists())
        {
            tempFile.delete();
        }
    }

    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
}
