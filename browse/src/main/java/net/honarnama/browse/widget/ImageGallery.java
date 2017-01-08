package net.honarnama.browse.widget;

import com.mikepenz.iconics.view.IconicsTextView;

import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by elnaz on 1/8/17.
 */

public class ImageGallery extends RelativeLayout {
    private ViewPager mPager;

    private int mDimen = -1;

    protected Context mContext;

    static TextView mCirclesArray[];
    private RelativeLayout mCirclesLayout;

    private static int CURRENT_IMAGE_PAGE = 0;
    private static int NUM_IMAGE_PAGES = 0;

    public ImageGallery(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public ImageGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public ImageGallery(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    private void init() {
        View galleryLayout = LayoutInflater.from(getContext()).inflate(R.layout.item_image_gallery, null);
        mPager = (ViewPager) galleryLayout.findViewById(R.id.image_pager);

        mCirclesLayout = (RelativeLayout) galleryLayout.findViewById(R.id.image_dots_container);
//        mPager.setPageTransformer(false, new ParallaxPagerTransformer(R.id.imageView1));
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        mDimen = display.getWidth();

        LayoutParams params = new LayoutParams(mDimen, mDimen);
        addView(galleryLayout, params);

    }

    public void setAdapter(PagerAdapter adapter) {
        mPager.setAdapter(adapter);
    }

    public void setCurrentItem(int pos) {
        mPager.setCurrentItem(pos, true);
    }

    public void setSwipeHandler(int numOfImages) {

        RelativeLayout.LayoutParams layoutParams;

        NUM_IMAGE_PAGES = numOfImages;

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                CURRENT_IMAGE_PAGE = position;

                if (mCirclesArray != null) {
                    for (int i = 0; i < NUM_IMAGE_PAGES; i++) {
                        if (mCirclesArray.length > i && mCirclesArray[i] != null) {
//                            mCirclesArray[i].setTextSize(8);
                            mCirclesArray[i].setText("{gmd-panorama-fish-eye}");
                        }
                    }
                    if (mCirclesArray.length > position && mCirclesArray[position] != null) {
//                        mCirclesArray[position].setTextSize(12);
                        mCirclesArray[position].setText("{gmd-brightness-1}");
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (NUM_IMAGE_PAGES > 1) {
            mCirclesArray = new IconicsTextView[NUM_IMAGE_PAGES];
            for (int i = 0; i < NUM_IMAGE_PAGES; i++) {
                layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                mCirclesArray[i] = new IconicsTextView(HonarnamaBrowseApp.getInstance());

                int circleId = i + 1;
                mCirclesArray[i].setId(circleId);
                mCirclesArray[i].setTypeface(null, Typeface.BOLD);
                mCirclesArray[i].setTextColor(getResources().getColor(R.color.amber_primary_dark));

                if (i == 0) {
                    mCirclesArray[i].setText("{gmd-brightness-1}");
                    mCirclesArray[i].setTextSize(10);
                    mCirclesArray[i].setPadding(0, 10, 5, 0);
                    layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                } else {
                    mCirclesArray[i].setText("{gmd-panorama-fish-eye}");
                    mCirclesArray[i].setTextSize(10);
                    mCirclesArray[i].setPadding(5, 10, 5, 0);
                    layoutParams.addRule(RelativeLayout.RIGHT_OF, i);
                    layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                }

                if (mCirclesLayout != null) {
                    mCirclesLayout.addView(mCirclesArray[i], layoutParams);
                }
            }
        }

//        // Auto start of viewpager
//        final Handler handler = new Handler();
//        final Runnable Update = new Runnable() {
//            public void run() {
//                if (CURRENT_IMAGE_PAGE == NUM_IMAGE_PAGES) {
//                    CURRENT_IMAGE_PAGE = 0;
//                }
//                setCurrentItem(CURRENT_IMAGE_PAGE++);
//            }
//        };
//        Timer swipeTimer = new Timer();
//        swipeTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                handler.post(Update);
//            }
//        }, 5000, 5000);

    }

}
