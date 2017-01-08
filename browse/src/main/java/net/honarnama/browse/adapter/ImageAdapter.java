package net.honarnama.browse.adapter;

import com.parse.ImageSelector;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elnaz on 2/26/16.
 */
public class ImageAdapter extends PagerAdapter {

    private Context mContext;
    List<String> mImages = new ArrayList<>();
    private static LayoutInflater mInflater = null;

    public ImageAdapter(Context c) {
        mContext = c;
        mInflater = LayoutInflater.from(mContext);
    }

    public void setImages(List<String> images) {
        mImages = images;
    }

    @Override
    public int getCount() {
        if (mImages != null) {
            return mImages.size();
        }
        return 0;
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
//
        View imageLayout = mInflater.inflate(R.layout.gallery_row, view, false);

        if (imageLayout == null) {
            return null;
        }
        ImageSelector imageView = (ImageSelector) imageLayout
                .findViewById(R.id.gallery_item_image);

        ProgressBar progressBar = (ProgressBar) imageLayout.findViewById(R.id.progressBar1);
        progressBar.setVisibility(View.VISIBLE);

        if (BuildConfig.DEBUG) {
            Log.d(HonarnamaBaseApp.PRODUCTION_TAG, "instantiateItem: image address: " + mImages.get(position));
        }
        imageView.setSource(mImages.get(position), progressBar, R.drawable.party_flags);
//        imageView.setAdjustViewBounds(true);
//        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        imageView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        view.addView(imageLayout, RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        return imageLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
