package net.honarnama.browse.adapter;

import com.parse.ImageSelector;

import net.honarnama.browse.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elnaz on 2/26/16.
 */
public class ImageAdapter extends BaseAdapter {

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
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final MyViewHolder mViewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.gallery_row, parent, false);
            mViewHolder = new MyViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        ImageSelector imageView = mViewHolder.imageSelector;

        mViewHolder.imageProgressBar.setVisibility(View.VISIBLE);
        //TODO
//        imageView.loadInBackground(mImages.get(position), new GetDataCallback() {
//            @Override
//            public void done(byte[] data, ParseException e) {
//                mViewHolder.imageProgressBar.setVisibility(View.GONE);
//            }
//        });
        imageView.setSource(mImages.get(position), mViewHolder.imageProgressBar);
        imageView.setLayoutParams(new Gallery.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        imageView.setAdjustViewBounds(true);
        return imageView;
    }

    private class MyViewHolder {
        ImageSelector imageSelector;
        ProgressBar imageProgressBar;

        public MyViewHolder(View view) {
            imageSelector = (ImageSelector) view.findViewById(R.id.gallery_item_image);
            imageProgressBar = (ProgressBar) view.findViewById(R.id.gallery_item_image_progress_bar);
        }

    }
}
