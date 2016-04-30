package com.parse;

import com.crashlytics.android.Crashlytics;
import com.makeramen.roundedimageview.RoundedImageView;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.R;
import net.honarnama.core.utils.FileUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


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

    private Uri mSelectedImageUri;
    private Uri mTempImageUriCrop;
    private Uri mFinalImageUri;

    private boolean mChanged = false;
    private boolean mLoaded = false;

    private static boolean announced = false;

    public boolean mDeleted;

    public boolean isFileSet() {
        return mFileSet;
    }

    public void setFileSet(boolean fileSet) {
        mFileSet = fileSet;
    }

    private boolean mFileSet;


    public boolean getLoaded() {
        return mLoaded;
    }

    public void setLoaded(boolean loaded) {
        mLoaded = loaded;
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
                new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.AlertDialogCustom));
        changeImageOptionsDialog.setTitle(
                mContext.getString(R.string.select_national_card_image_dialog_title));

        String[] imageSourceProviders;
        if (mIncludeRemoveImage && isFileSet()) {
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
                            mSelectedImageUri = createImageFile();
                            if (mSelectedImageUri != null) {
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mSelectedImageUri);
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
        setFileSet(false);
        if (mOnImageSelectedListener != null) {
            mOnImageSelectedListener.onImageRemoved();
        }
        if (mDefaultDrawable != null) {
            setImageDrawable(mDefaultDrawable);
        }
        //TODO test below commenting
//        mChanged = true;
        mDeleted = true;
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
            mDeleted = false;
            setFileSet(true);
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
                    imageSelected(mSelectedImageUri, false);
                } // else: crop will handle
            } else {
                if (BuildConfig.DEBUG) {
                    Log.i(LOG_TAG, "onActivityResult::mIntentCodeCapture resultCode= " + resultCode);
                }
                mOnImageSelectedListener.onImageSelectionFailed();
            }
        } else if (requestCode == mIntentCodeSelect) {
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImageURI = intent.getData();
                String filePath = null;
                Log.d("", "URI = " + selectedImageURI);
                if (selectedImageURI != null && "content".equals(selectedImageURI.getScheme())) {
                    filePath = FileUtil.getRealPathFromURI(HonarnamaBaseApp.getInstance(), selectedImageURI);

                    mSelectedImageUri = Uri.fromFile(new File(filePath));
                } else {
                    mSelectedImageUri = selectedImageURI;
                }


//                String filePath = null;
//                Uri _uri = intent.getData();
//                Log.d("", "URI = " + _uri);
//                if (_uri != null && "content".equals(_uri.getScheme())) {
//                    Cursor cursor = mContext.getContentResolver().query(_uri,
//                            new String[]{android.provider.MediaStore.Images.ImageColumns.DATA},
//                            null, null, null);
//                    cursor.moveToFirst();
//                    filePath = cursor.getString(0);
//                    cursor.close();
//                    mSelectedImageUri = Uri.fromFile(new File(filePath));
//                } else {
//                    mSelectedImageUri = _uri;
//                }
//
                if (BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, "Converted mIntentCodeSelect selectedImageURI= " + selectedImageURI
                            + " to mSelectedImageUri= " + mSelectedImageUri);
                }

                if (!(mCropNeeded && crop())) {
                    imageSelected(mSelectedImageUri, false);
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
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Honarnama");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
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
                ex.printStackTrace();
            } else {
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                String stackTrace = sw.toString();
                Crashlytics.log(Log.ERROR, LOG_TAG, "While preparing for takePicture " + ex + ". stackTrace: " + stackTrace);
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

        cropIntent.setData(mSelectedImageUri);

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
//        if (mSelectedImageUri != null) {
//            File f = new File(mSelectedImageUri.getPath());
//            if (f.canWrite()) {
//                f.delete();
//            }
//        }

        if (mTempImageUriCrop != null) {
            File f = new File(mTempImageUriCrop.getPath());
            if (f.canWrite()) {
                f.delete();
            }
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        String prefix = "ImageSelector_" + mImageSelectorIndex;

        if (mSelectedImageUri != null) {
            outState.putString(prefix + "_mTempImageUriCapture", mSelectedImageUri.toString());
        }

        if (mTempImageUriCrop != null) {
            outState.putString(prefix + "_mTempImageUriCrop", mTempImageUriCrop.toString());
        }

        if (mFinalImageUri != null) {
            outState.putString(prefix + "_mFinalImageUri", mFinalImageUri.toString());
        }

        outState.putBoolean(prefix + "_mChanged", mChanged);
        outState.putBoolean(prefix + "_mDeleted", mDeleted);
    }

    public void restore(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String prefix = "ImageSelector_" + mImageSelectorIndex;
//            mChanged = true;

            mChanged = savedInstanceState.getBoolean(prefix + "_mChanged");
            mDeleted = savedInstanceState.getBoolean(prefix + "_mDeleted");

            String _mTempImageUriCapture = savedInstanceState.getString(prefix + "_mTempImageUriCapture");
            if (_mTempImageUriCapture != null) {
                mSelectedImageUri = Uri.parse(_mTempImageUriCapture);
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

    public int getImageSelectorIndex() {
        return mImageSelectorIndex;
    }

    public interface OnImageSelectedListener {
        boolean onImageSelected(Uri selectedImage, boolean cropped);

        void onImageRemoved();

        void onImageSelectionFailed();
    }


    public boolean isChanged() {
        return mChanged;
    }

    public void setChanged(boolean changed) {
        mChanged = changed;
    }

    public boolean isDeleted() {
        return mDeleted;
    }

    public void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

}
