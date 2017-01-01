package com.parse;

import com.crashlytics.android.Crashlytics;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.R;
import net.honarnama.base.activity.HonarnamaBaseActivity;
import net.honarnama.base.dialog.OptionDialog;
import net.honarnama.base.utils.FileUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ImageSelector extends RoundedImageView implements View.OnClickListener {

    private static final String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/"
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

    private String mLoadingURL;

    private boolean mChanged = false;

    private static boolean announced = false;

    public boolean mDeleted;

    public boolean isFileSet() {
        return mFileSet;
    }

    public void setFileSet(boolean fileSet) {
        mFileSet = fileSet;
    }

    private boolean mFileSet;

    public ImageSelector(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;
        init(attrs);
        if (BuildConfig.DEBUG && !announced) {
            Log.d(HonarnamaBaseApp.PRODUCTION_TAG, "View created,\tadb catlog tag:   '" + DEBUG_TAG + ":V'");
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

        if (mActivity != null) {
            if (!((HonarnamaBaseActivity) mActivity).checkAndAskStoragePermission(mActivity)) {
                Toast.makeText(mActivity, "دسترسی لازم به هنرنما را نداده‌اید.", Toast.LENGTH_LONG).show();
            }
        }

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
            imageSourceProviders[2] = mContext.getString(R.string.remove_photo);
        } else {
            imageSourceProviders = new String[2];
        }
        imageSourceProviders[0] = mContext.getString(R.string.capture_photo);
        imageSourceProviders[1] = mContext.getString(R.string.select_from_gallery);

        changeImageOptionsDialog.setItems(imageSourceProviders, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
                            mSelectedImageUri = createTempImageFile();
                            if (mSelectedImageUri != null && mActivity != null) {
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mSelectedImageUri);
                                mActivity.startActivityForResult(
                                        takePictureIntent, mIntentCodeCapture);
                            }
                        } else {
                            Log.w(DEBUG_TAG, "No activity for IMAGE_CAPTURE");
                            Toast.makeText(mContext, R.string.error_no_camera_found,
                                    Toast.LENGTH_LONG).show();
                        }
                        break;

                    case 1:
                        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        if (mActivity != null) {
                            mActivity.startActivityForResult(
                                    pickPhotoIntent, mIntentCodeSelect);
                        }
                        break;

                    case 2:
                        removeSelectedImage();
                        if (BuildConfig.DEBUG) {
                            Log.d(DEBUG_TAG, "Image is removed");
                        }
                }
                dialog.dismiss();
            }
        });
        changeImageOptionsDialog.show();
    }

    public void removeSelectedImage() {
        mFinalImageUri = null;
        mLoadingURL = null;
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
            Log.d(DEBUG_TAG, "imageSelected selectedImage= " + selectedImage +
                    " , cropped= " + cropped);
        }

        if (selectedImage == null && mOnImageSelectedListener != null) {
            mOnImageSelectedListener.onImageSelectionFailed();
            return;
        }

        File selectedImageFile = new File(selectedImage.getPath());
        if (!selectedImageFile.canRead()) {
            Log.e(DEBUG_TAG, "File not readable. exists=" + selectedImageFile.exists());
            if (mOnImageSelectedListener != null) {
                mOnImageSelectedListener.onImageSelectionFailed();
                return;
            }
        }

        if ((mOnImageSelectedListener == null) ||
                (mOnImageSelectedListener.onImageSelected(selectedImage, cropped))) {

            mLoadingURL = "";
            mFinalImageUri = selectedImage;
            mChanged = true;
            mDeleted = false;
            setFileSet(true);
            if (BuildConfig.DEBUG) {
                Log.d(DEBUG_TAG, "Image is set (through imageSelected)");
            }
            setImageURI(selectedImage);
        }
    }

    public void setFinalImageUri(Uri finalImageUri) {
        mFinalImageUri = finalImageUri;
    }

    public void setSelectedImageUri(Uri selectedImageUri) {
        mSelectedImageUri = selectedImageUri;
    }

    public void setTempImageUriCrop(Uri tempImageUriCrop) {
        mTempImageUriCrop = tempImageUriCrop;
    }

    public void setLoadingURL(String loadingURL) {
        mLoadingURL = loadingURL;
    }


    public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {

        if ((requestCode < mIntentCodeCapture) || (requestCode > mIntentCodeCrop)) {
            return false;
        }

        if (requestCode == mIntentCodeCapture) {
            String path = mSelectedImageUri.getPath();
            Log.d(DEBUG_TAG, "onActivityResult of IS. path: " + path);

            if (resultCode == Activity.RESULT_OK) {
                if (!(mCropNeeded && checkAndCrop(true))) {
                    imageSelected(mSelectedImageUri, false);
                } // else: checkAndCrop will handle
            } else {
                mOnImageSelectedListener.onImageSelectionFailed();
            }
        } else if (requestCode == mIntentCodeSelect) {

            if (resultCode == Activity.RESULT_OK && intent != null) {
                Uri selectedImageURI = intent.getData();
                String filePath = null;
                Log.d("", "selectedImageURI = " + selectedImageURI + ".");
                if (selectedImageURI != null && "content".equals(selectedImageURI.getScheme())) {
                    filePath = FileUtil.getRealPathFromURI(HonarnamaBaseApp.getInstance(), selectedImageURI);
                    mSelectedImageUri = Uri.fromFile(new File(filePath));
                } else {
                    mSelectedImageUri = selectedImageURI;
                }

                if (!(mCropNeeded && checkAndCrop(false))) {
                    imageSelected(mSelectedImageUri, false);
                    Log.d("", "!(mCropNeeded && checkAndCrop())");
                } // else: checkAndCrop will handle
            } else {
                if (BuildConfig.DEBUG) {
                    Log.i(DEBUG_TAG, "onActivityResult::mIntentCodeSelect resultCode= " + resultCode);
                }
                mOnImageSelectedListener.onImageSelectionFailed();
            }
        } else if (requestCode == mIntentCodeCrop) {

//            mTempImageUriCrop = intent.getData();

            if (resultCode == Activity.RESULT_OK) {
                imageSelected(mTempImageUriCrop, true);
            } else {
                if (BuildConfig.DEBUG) {
                    Log.i(DEBUG_TAG, "onActivityResult::mIntentCodeCrop resultCode= " + resultCode);
                }
                mOnImageSelectedListener.onImageSelectionFailed();
            }
        } else {
            throw new RuntimeException("Unexpected requestCode:" + requestCode);
        }

        return true;
    }

    public Uri createTempImageFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(new Date());
        String imageFileName = "Honarnama_" + timeStamp;
        //TODO make a key for path
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Honarnama/honarnama_temporary_files");
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
                Log.e(DEBUG_TAG, "While preparing for takePicture", ex);
                ex.printStackTrace();
            } else {
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                String stackTrace = sw.toString();
                Crashlytics.log(Log.ERROR, DEBUG_TAG, "While preparing for takePicture " + ex + ". stackTrace: " + stackTrace);
            }
            return null;
        }
        // Save a file: path for use with ACTION_VIEW intents
        return Uri.fromFile(image);
    }

    public boolean checkAndCrop(boolean delayed) {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setType("image/*");

        List<ResolveInfo> resolveInfos = mContext.getApplicationContext().getPackageManager().queryIntentActivities(cropIntent, 0);

        if (resolveInfos.size() == 0) {
            Toast.makeText(mActivity, "برنامه مناسبی برای برش عکس پیدا نشد!", Toast.LENGTH_LONG).show();
            return false;
        }

        mTempImageUriCrop = createTempImageFile();

        if (mTempImageUriCrop == null) {
            return false;
        }

        if (delayed) {
            callCropDelayed(mSelectedImageUri, mTempImageUriCrop);
        } else {
            crop(mSelectedImageUri, mTempImageUriCrop);
        }

        return true;

//        cropIntent.setData(mSelectedImageUri);
//
//        if (mOutputX >= 0) {
//            cropIntent.putExtra("outputX", mOutputX);
//        }
//        if (mOutputX >= 0) {
//            cropIntent.putExtra("outputY", mOutputY);
//        }
//        if (mOutputX >= 0) {
//            cropIntent.putExtra("aspectX", mAspectX);
//        }
//        if (mOutputX >= 0) {
//            cropIntent.putExtra("aspectY", mAspectY);
//        }
//
//        cropIntent.putExtra("scale", true);
//        cropIntent.putExtra("return-data", false);
//        cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
//        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, mTempImageUriCrop);
//
//        Intent intent = new Intent(cropIntent);
//        ResolveInfo res = resolveInfos.get(0);
//        intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
//
//        if (mActivity != null) {
//            Log.d(DEBUG_TAG, "Calling checkAndCrop intent in IS. ");
//            Log.d(DEBUG_TAG, "mSelectedImageUri:  " + mSelectedImageUri + ". in IS");
//            Log.d(DEBUG_TAG, "mTempImageUriCrop:  " + mTempImageUriCrop + ". in IS");
//            mActivity.startActivityForResult(intent, mIntentCodeCrop);
//        }
//        return true;
    }

    public void crop(Uri data, Uri output) {

        final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");

        List<ResolveInfo> list = mActivity.getApplicationContext().getPackageManager().queryIntentActivities(intent, 0);

        int size = list.size();

        intent.setData(data);
        intent.putExtra("outputX", mOutputX);
        intent.putExtra("outputY", mOutputY);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra("output", output);

        if (size == 1) {
            Intent i = new Intent(intent);
            ResolveInfo res = list.get(0);

            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            mActivity.startActivityForResult(i, mIntentCodeCrop);

        } else {
            for (ResolveInfo res : list) {
                final CropOption co = new CropOption();

                co.title = mActivity.getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
                co.icon = mActivity.getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
                co.appIntent = new Intent(intent);

                co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

                cropOptions.add(co);
            }

            CropOptionAdapter adapter = new CropOptionAdapter(mActivity.getApplicationContext(), cropOptions);
            final OptionDialog optionDialog = new OptionDialog(mActivity, adapter, R.string.no_crop_app_found);
            optionDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    mActivity.startActivityForResult(cropOptions.get(i).appIntent, mIntentCodeCrop);
                    optionDialog.dismiss();
                }
            });
            optionDialog.show();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//        if (BuildConfig.DEBUG) {
//            Log.d(DEBUG_TAG, "onDetachedFromWindow // mTempImageUriCrop: " + mTempImageUriCrop + ". DEBUG_CODE: STOPPED_ACTIVITY");
//        }

//        if (mTempImageUriCrop != null) {
//            File f = new File(mTempImageUriCrop.getPath());
//            if (f.canWrite()) {
//                f.delete();
//            }
//        }

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

        if (mLoadingURL != null) {
            outState.putString(prefix + "_mLoadingURL", mLoadingURL);
        }

        if (BuildConfig.DEBUG) {
            Log.d("STOPPED_ACTIVITY", "onSaveInstanceState of IS. mFinalImageUri: " + mFinalImageUri);
        }

        outState.putBoolean(prefix + "_mChanged", mChanged);
        outState.putBoolean(prefix + "_mDeleted", mDeleted);
        outState.putBoolean(prefix + "_mFileSet", mFileSet);
    }

    public void restore(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            String prefix = "ImageSelector_" + mImageSelectorIndex;
//            mChanged = true;

            mChanged = savedInstanceState.getBoolean(prefix + "_mChanged");
            mDeleted = savedInstanceState.getBoolean(prefix + "_mDeleted");
            mFileSet = savedInstanceState.getBoolean(prefix + "_mFileSet");

            String __mLoadingURL = savedInstanceState.getString(prefix + "_mLoadingURL");

            Log.d(DEBUG_TAG, "restore of IS: __mLoadingURL: " + __mLoadingURL);
            if (__mLoadingURL != null && __mLoadingURL.trim().length() > 0) {
                mLoadingURL = __mLoadingURL;
                Picasso.with(mContext).load(mLoadingURL)
                        .error(R.drawable.camera_insta)
                        .into(this, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                            }
                        });
            }

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
//                setImageDrawable(mDefaultDrawable);

                if (mTempImageUriCrop != null) {
                    imageSelected(mTempImageUriCrop, true);
                } else if (mSelectedImageUri != null) {
                    imageSelected(mSelectedImageUri, true);
                }
            }

            if (BuildConfig.DEBUG) {
                Log.d("STOPPED_ACTIVITY", "restore of IS. mFinalImageUri: " + mFinalImageUri);
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

    public void setSource(String imageUri, final ProgressBar progressbar, int drawable) {
        if (imageUri.trim().length() == 0) {
            return;
        }
        //R.drawable.party_flags
        Picasso.with(mContext).load(imageUri.toString())
                .error(drawable)
                .into(this, new Callback() {
                    @Override
                    public void onSuccess() {
                        if (progressbar != null) {
                            progressbar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError() {
                        if (progressbar != null) {
                            progressbar.setVisibility(View.GONE);
                        }
                    }
                });
    }


    class CropOption {
        public CharSequence title;
        public Drawable icon;
        public Intent appIntent;
    }

    public class CropOptionAdapter extends ArrayAdapter<CropOption> {

        private ArrayList<CropOption> mOptions;
        private LayoutInflater mInflater;

        private CropOptionAdapter(Context context, ArrayList<CropOption> options) {
            super(context, R.layout.view_crop_selector, options);
            mOptions = options;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup group) {
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.view_crop_selector, null);

            CropOption item = mOptions.get(position);
            if (item != null) {
                ((ImageView) convertView.findViewById(R.id.iv_icon)).setImageDrawable(item.icon);
                ((TextView) convertView.findViewById(R.id.tv_name)).setText(item.title);

                return convertView;
            }

            return null;
        }
    }

    private void callCropDelayed(final Uri data, final Uri croppedFile) {

        final ProgressDialog progressDialog = new ProgressDialog(mActivity);
        progressDialog.setMessage("در حال بارگیری تصویر");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Handler().postDelayed(new Runnable() { //wait a sec for fucking gallery/camera to save the image completely.
            @Override
            public void run() {
                progressDialog.dismiss();
                crop(data, croppedFile);
            }
        }, 5000);

    }
}
