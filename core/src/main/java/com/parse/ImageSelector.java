package com.parse;

import com.makeramen.roundedimageview.RoundedImageView;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import bolts.Continuation;
import bolts.Task;


public class ImageSelector extends RoundedImageView implements View.OnClickListener {

    private static final String LOG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/"
            + ImageSelector.class.getName();

    final private Context mContext;
    private boolean mIncludeRemoveImage;
    private int mImageSelectorIndex;
    private boolean mCropNeeded;
    private int mOutputX, mOutputY, mAspectX, mAspectY;
    private Drawable mDefaultDrawable;

    private int mIntentCodeCapture;
    private int mIntentCodeSelect;
    private int mIntentCodeCrop;

    private OnImageSelectedListener mOnImageSelectedListener;
    private Activity mActivity;

    private Uri mTempImageUriCapture;
    private Uri mTempImageUriCrop;
    private Uri mFinalImageUri;

    private ParseFile mParseFile;
    private boolean mChanged = false;
    private boolean mImageIsLoaded = false;

    private static boolean announced = false;

    public boolean mIsDeleted;


    public boolean getImageIsLoaded() {
        return mImageIsLoaded;
    }

    public void setImageIsLoaded(boolean imageIsLoaded) {
        mImageIsLoaded = imageIsLoaded;
    }

    public ImageSelector(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init(attrs);
        if (BuildConfig.DEBUG && !announced) {
            Log.d(HonarnamaBaseApp.PRODUCTION_TAG, "View created,\tadb catlog tag:   '" + LOG_TAG + ":V'");
            announced = true;
        }
    }

    public ImageSelector(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageSelector(Context context) {
        this(context, null, 0);
    }

    protected void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.ImageSelector);
            try {
                mIncludeRemoveImage = a.getBoolean(R.styleable.ImageSelector_removable, false);
                mImageSelectorIndex = a.getInt(R.styleable.ImageSelector_imageSelectorIndex, 0);
                mIntentCodeCapture = HonarnamaBaseApp.INTENT_IMAGE_SELECTOR_CODE_RANGE_START + (mImageSelectorIndex * 3);
                mIntentCodeSelect = mIntentCodeCapture + 1;
                mIntentCodeCrop = mIntentCodeCapture + 2;
                mOutputX = a.getInt(R.styleable.ImageSelector_outputX, -1);
                mOutputY = a.getInt(R.styleable.ImageSelector_outputY, -1);
                mAspectX = a.getInt(R.styleable.ImageSelector_aspectX, -1);
                mAspectY = a.getInt(R.styleable.ImageSelector_aspectY, -1);
                mCropNeeded = (mOutputX >= 0) || (mOutputY >= 0) ||
                        (mAspectX >= 0) || (mAspectY >= 0);
            } finally {
                a.recycle();
            }
        }
        mDefaultDrawable = getDrawable();
    }

    public void setActivity(Activity activity) {
        mActivity = activity;
        setOnClickListener(this);
    }

    public void setOnImageSelectedListener(OnImageSelectedListener onImageSelectedListener) {
        mOnImageSelectedListener = onImageSelectedListener;
    }

    @Override
    public void setOnClickListener(View.OnClickListener l) {
        if (mActivity == null) {
            throw new RuntimeException("call setActivity instead");
        }
        super.setOnClickListener(l);
    }

    @Override
    public void onClick(View view) {
        selectPhoto();
    }

    public void selectPhoto() {
        final AlertDialog.Builder changeImageOptionsDialog =
                new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.DialogStyle));
        changeImageOptionsDialog.setTitle(
                mContext.getString(R.string.select_national_card_image_dialog_title));

        String[] imageSourceProviders;
        if (mIncludeRemoveImage && (mFinalImageUri != null || !isDeleted())) {
            imageSourceProviders = new String[3];
            imageSourceProviders[2] = mContext.getString(R.string.image_selector_option_text_remove);
        } else {
            imageSourceProviders = new String[2];
        }
        imageSourceProviders[0] = mContext.getString(R.string.image_selector_option_text_capture);
        imageSourceProviders[1] = mContext.getString(R.string.image_selector_option_text_select);

        changeImageOptionsDialog.setItems(imageSourceProviders, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
                            mTempImageUriCapture = createImageFile();
                            if (mTempImageUriCapture != null) {
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mTempImageUriCapture);
                                mActivity.startActivityForResult(
                                        takePictureIntent, mIntentCodeCapture);
                            }
                        } else {
                            Log.w(LOG_TAG, "No activity for IMAGE_CAPTURE");
                            Toast.makeText(mContext, R.string.image_selector_error_no_camera,
                                    Toast.LENGTH_LONG).show();
                        }
                        break;

                    case 1:
                        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        mActivity.startActivityForResult(
                                pickPhotoIntent, mIntentCodeSelect);
                        break;

                    case 2:
                        removeSelectedImage();
                        mChanged = true;
                        if (BuildConfig.DEBUG) {
                            Log.d(LOG_TAG, "Image is removed");
                        }
                }
                dialog.dismiss();
            }
        });
        changeImageOptionsDialog.show();
    }

    public void removeSelectedImage() {
        mFinalImageUri = null;
        if (mOnImageSelectedListener != null) {
            mOnImageSelectedListener.onImageRemoved();
        }
        if (mDefaultDrawable != null) {
            setImageDrawable(mDefaultDrawable);
        }
        mChanged = false;
        mIsDeleted = true;
    }

    public boolean isDeleted() {
        return mIsDeleted;
    }

    protected void imageSelected(Uri selectedImage, boolean cropped) {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "imageSelected selectedImage= " + selectedImage +
                    " , cropped= " + cropped);
        }

        if (selectedImage == null) {
            mOnImageSelectedListener.onImageSelectionFailed();
        }

        File selectedImageFile = new File(selectedImage.getPath());
        if (!selectedImageFile.canRead()) {
            Log.e(LOG_TAG, "File not readable. exists=" + selectedImageFile.exists());
            if (mOnImageSelectedListener != null) {
                mOnImageSelectedListener.onImageSelectionFailed();
            }
        }

        if ((mOnImageSelectedListener == null) ||
                (mOnImageSelectedListener.onImageSelected(selectedImage, cropped))) {
            mFinalImageUri = selectedImage;
            mChanged = true;
            mIsDeleted = false;
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "Image is set (through imageSelected)");
            }
            setImageURI(selectedImage);
        }
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
        if ((requestCode < mIntentCodeCapture) || (requestCode > mIntentCodeCrop)) {
            return false;
        }

        if (requestCode == mIntentCodeCapture) {
            if (resultCode == Activity.RESULT_OK) {
                if (!(mCropNeeded && crop())) {
                    imageSelected(mTempImageUriCapture, false);
                } // else: crop will handle
            } else {
                if (BuildConfig.DEBUG) {
                    Log.i(LOG_TAG, "onActivityResult::mIntentCodeCapture resultCode= " + resultCode);
                }
                mOnImageSelectedListener.onImageSelectionFailed();
            }
        } else if (requestCode == mIntentCodeSelect) {
            if (resultCode == Activity.RESULT_OK) {
                String filePath = null;
                Uri _uri = intent.getData();
                Log.d("", "URI = " + _uri);
                if (_uri != null && "content".equals(_uri.getScheme())) {
                    Cursor cursor = mContext.getContentResolver().query(_uri,
                            new String[]{android.provider.MediaStore.Images.ImageColumns.DATA},
                            null, null, null);
                    cursor.moveToFirst();
                    filePath = cursor.getString(0);
                    cursor.close();
                    mTempImageUriCapture = Uri.fromFile(new File(filePath));
                } else {
                    mTempImageUriCapture = _uri;
                }

                if (BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, "Converted mIntentCodeSelect _uri= " + _uri
                            + " to mTempImageUriCapture= " + mTempImageUriCapture);
                }

                if (!(mCropNeeded && crop())) {
                    imageSelected(mTempImageUriCapture, false);
                } // else: crop will handle
            } else {
                if (BuildConfig.DEBUG) {
                    Log.i(LOG_TAG, "onActivityResult::mIntentCodeSelect resultCode= " + resultCode);
                }
                mOnImageSelectedListener.onImageSelectionFailed();
            }
        } else if (requestCode == mIntentCodeCrop) {
            if (resultCode == Activity.RESULT_OK) {
                imageSelected(mTempImageUriCrop, false);
            } else {
                if (BuildConfig.DEBUG) {
                    Log.i(LOG_TAG, "onActivityResult::mIntentCodeCrop resultCode= " + resultCode);
                }
                mOnImageSelectedListener.onImageSelectionFailed();
            }
        } else {
            throw new RuntimeException("Unexpected requestCode:" + requestCode);
        }

        return true;
    }

    public Uri createImageFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(new Date());
        String imageFileName = "Honarnama_" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image;
        try {

            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException ex) {
            if (BuildConfig.DEBUG) {
                Log.e(LOG_TAG, "While preparing for takePicture", ex);
            } else {
                Log.e(LOG_TAG, "Exception: " + ex);
            }
            return null;
        }
        // Save a file: path for use with ACTION_VIEW intents
        return Uri.fromFile(image);
    }

    public boolean crop() {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setType("image/*");

        List<ResolveInfo> resolveInfos = mContext.getApplicationContext().getPackageManager().queryIntentActivities(cropIntent, 0);

        if (resolveInfos.size() == 0) {
            return false;
        }

        mTempImageUriCrop = createImageFile();

        if (mTempImageUriCrop == null) {
            return false;
        }

        cropIntent.setData(mTempImageUriCapture);

        if (mOutputX >= 0) {
            cropIntent.putExtra("outputX", mOutputX);
        }
        if (mOutputX >= 0) {
            cropIntent.putExtra("outputY", mOutputY);
        }
        if (mOutputX >= 0) {
            cropIntent.putExtra("aspectX", mAspectX);
        }
        if (mOutputX >= 0) {
            cropIntent.putExtra("aspectY", mAspectY);
        }

        cropIntent.putExtra("scale", true);
        cropIntent.putExtra("return-data", false);
        cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, mTempImageUriCrop);

        Intent intent = new Intent(cropIntent);
        ResolveInfo res = resolveInfos.get(0);
        intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
        mActivity.startActivityForResult(intent, mIntentCodeCrop);

        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mTempImageUriCapture != null) {
            File f = new File(mTempImageUriCapture.getPath());
            if (f.canWrite()) {
                f.delete();
            }
        }

        if (mTempImageUriCrop != null) {
            File f = new File(mTempImageUriCrop.getPath());
            if (f.canWrite()) {
                f.delete();
            }
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        String prefix = "ImageSelector_" + mImageSelectorIndex;

        if (mTempImageUriCapture != null) {
            outState.putString(prefix + "_mTempImageUriCapture", mTempImageUriCapture.toString());
        }

        if (mTempImageUriCrop != null) {
            outState.putString(prefix + "_mTempImageUriCrop", mTempImageUriCrop.toString());
        }

        if (mFinalImageUri != null) {
            outState.putString(prefix + "_mFinalImageUri", mFinalImageUri.toString());
        }
    }

    public void restore(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String prefix = "ImageSelector_" + mImageSelectorIndex;
            mChanged = true;

            String _mTempImageUriCapture = savedInstanceState.getString(prefix + "_mTempImageUriCapture");
            if (_mTempImageUriCapture != null) {
                mTempImageUriCapture = Uri.parse(_mTempImageUriCapture);
            }

            String _mTempImageUriCrop = savedInstanceState.getString(prefix + "_mTempImageUriCrop");
            if (_mTempImageUriCrop != null) {
                mTempImageUriCrop = Uri.parse(_mTempImageUriCrop);
            }

            String _mFinalImageUri = savedInstanceState.getString(prefix + "_mFinalImageUri");
            if (_mFinalImageUri != null) {
                mFinalImageUri = Uri.parse(_mFinalImageUri);
                setImageDrawable(mDefaultDrawable);
            }
        }
    }

    public Uri getFinalImageUri() {
        return mFinalImageUri;
    }

    public void setFinalImageUri(Uri imageUri) {
        super.setImageURI(imageUri);
        mFinalImageUri = imageUri;
        mChanged = true;
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "Image is set (through setFinalImageUri)");
        }
    }

    public int getImageSelectorIndex() {
        return mImageSelectorIndex;
    }

    public interface OnImageSelectedListener {
        boolean onImageSelected(Uri selectedImage, boolean cropped);

        void onImageRemoved();

        void onImageSelectionFailed();
    }

    /**
     * Kick off downloading of remote image. When the download is finished, the image data will be
     * displayed.
     *
     * @param parseFile The remote file on Parse's server.
     * @return A Task that is resolved when the image data is fetched and this View displays the
     * image.
     */
    public Task<byte[]> loadInBackground(final ParseFile parseFile) {
        if (parseFile == null) {
            return Task.forResult(null);
        }

        if (mParseFile != null) {
            mParseFile.cancel();
        }
        mParseFile = parseFile;

        return parseFile.getDataInBackground().onSuccessTask(new Continuation<byte[], Task<byte[]>>() {
            @Override
            public Task<byte[]> then(Task<byte[]> task) throws Exception {
                byte[] data = task.getResult();
                if (mParseFile != parseFile) {
                    // This prevents the very slim chance of the file's download finishing and the callback
                    // triggering just before this ImageView is reused for another ParseObject.
                    return Task.cancelled();
                }
                if (data != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    if (bitmap != null) {
                        setImageBitmap(bitmap);
                    }
                }
                mChanged = false;
                mIsDeleted = false;

                if (BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, "Image is loaded");
                }
                return task;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }


    /**
     * Kick off downloading of remote image. When the download is finished, the image data will be
     * displayed and the {@code completionCallback} will be triggered.
     *
     * @param parseFile          The remote file on Parse's server.
     * @param completionCallback A custom {@code GetDataCallback} to be called after the image data
     *                           is fetched and this
     *                           {@code ImageView} displays the image.
     */
    public void loadInBackground(final ParseFile parseFile, final GetDataCallback completionCallback) {
        ParseTaskUtils.callbackOnMainThreadAsync(loadInBackground(parseFile), completionCallback, true);
    }


    public boolean isChanged() {
        return mChanged;
    }

    public ParseFile getParseFile() {
        return mParseFile;
    }

    //for Square Imageview
//    @Override
//    protected void onMeasure(int widthMeasureSpec,
//                             int heightMeasureSpec) {
//        Drawable d = getDrawable();
//
//        if (d != null) {
//            // ceil not round - avoid thin vertical gaps along the left/right edges
//            int width = MeasureSpec.getSize(widthMeasureSpec);
//            int height = (int) Math.ceil((float) width * (float) d.getIntrinsicHeight() / (float) d.getIntrinsicWidth());
//            setMeasuredDimension(width, height);
//        } else {
//            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        }
////        int width = MeasureSpec.getSize(widthMeasureSpec);
////        setMeasuredDimension(width, width);
//    }
}
