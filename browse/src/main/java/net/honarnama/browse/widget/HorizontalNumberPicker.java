package net.honarnama.browse.widget;

import net.honarnama.browse.R;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HorizontalNumberPicker extends LinearLayout {

    private Button mMinusButton;
    private Button mPlusButton;
    private TextView mTextView;
    public Context mContext;

    private CharSequence[] mSpinnerValues = null;
    private int mSelectedIndex = -1;

    public HorizontalNumberPicker(Context context) {
        super(context);
        initializeViews(context);
        mContext = context;
    }

    public HorizontalNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
        mContext = context;
    }

    public HorizontalNumberPicker(Context context,
                                  AttributeSet attrs,
                                  int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
        mContext = context;
    }


    public void initializeViews(Context context) {

        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.horizontal_number_picker, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // Sets the images for the previous and next buttons. Uses
        // built-in images so you don't need to add images, but in
        // a real application your images should be in the
        // application package so they are always available.
        mMinusButton = (Button) this.findViewById(R.id.btn_minus);
        mPlusButton = (Button) this.findViewById(R.id.btn_plus);
        mTextView = (TextView) this.findViewById(R.id.text_view);


        mMinusButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (mSelectedIndex > 0) {
                    int newSelectedIndex = mSelectedIndex - 1;
                    setSelectedIndex(newSelectedIndex);
                }
            }
        });

        mPlusButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (mSpinnerValues != null
                        && mSelectedIndex < mSpinnerValues.length - 1) {
                    int newSelectedIndex = mSelectedIndex + 1;
                    setSelectedIndex(newSelectedIndex);
                }
            }
        });

        mMinusButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mMinusStartTime = System.currentTimeMillis();
                    mMinusTimerHandler.postDelayed(mMinusTimerRunnable, 0);
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mMinusTimerHandler.removeCallbacks(mMinusTimerRunnable);
                }

                return false;
            }
        });

        mPlusButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mPlusStartTime = System.currentTimeMillis();
                    mPlusTimerHandler.postDelayed(mPlusTimerRunnable, 0);
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mPlusTimerHandler.removeCallbacks(mPlusTimerRunnable);
                }

                return false;
            }
        });

        // Select the first value by default.
        setSelectedIndex(0);

    }

    /**
     * Sets the list of value in the spinner, selecting the first value
     * by default.
     *
     * @param values the values to set in the spinner.
     */
    public void setValues(CharSequence[] values) {
        mSpinnerValues = values;

        // Select the first item of the string array by default since
        // the list of value has changed.
        setSelectedIndex(0);
    }

    /**
     * Sets the selected index of the spinner.
     *
     * @param index the index of the value to select.
     */
    public void setSelectedIndex(int index) {
        // If no values are set for the spinner, do nothing.
        if (mSpinnerValues == null || mSpinnerValues.length == 0)
            return;

        // If the index value is invalid, do nothing.
        if (index < 0 || index >= mSpinnerValues.length)
            return;

        // Set the current index and display the value.
        mSelectedIndex = index;
        mTextView.setText(mSpinnerValues[index]);

        // If the first value is shown, hide the previous button.
        if (mSelectedIndex == 0) {
            mMinusButton.setBackgroundColor(mContext.getResources().getColor(R.color.cyan_extra_light));
        } else {
            mMinusButton.setBackgroundColor(mContext.getResources().getColor(R.color.dark_cyan));
        }
        // If the last value is shown, hide the next button.
        if (mSelectedIndex == mSpinnerValues.length - 1)
            mPlusButton.setBackgroundColor(mContext.getResources().getColor(R.color.cyan_extra_light));
        else
            mPlusButton.setBackgroundColor(mContext.getResources().getColor(R.color.dark_cyan));
    }

    /**
     * Gets the selected value of the spinner, or null if no valid
     * selected index is set yet.
     *
     * @return the selected value of the spinner.
     */
    public CharSequence getSelectedValue() {
        // If no values are set for the spinner, return an empty string.
        if (mSpinnerValues == null || mSpinnerValues.length == 0)
            return "";

        // If the current index is invalid, return an empty string.
        if (mSelectedIndex < 0 || mSelectedIndex >= mSpinnerValues.length)
            return "";

        return mSpinnerValues[mSelectedIndex];
    }

    /**
     * Gets the selected index of the spinner.
     *
     * @return the selected index of the spinner.
     */
    public int getSelectedIndex() {
        return mSelectedIndex;
    }


    long mMinusStartTime = 0;
    Handler mMinusTimerHandler = new Handler();
    Runnable mMinusTimerRunnable = new Runnable() {
        @Override
        public void run() {
//            long millis = System.currentTimeMillis() - mMinusStartTime;
//            int seconds = (int) (millis / 1000);
//            int minutes = seconds / 60;
//            seconds = seconds % 60;

            int newSelectedIndex = mSelectedIndex - 1;
            setSelectedIndex(newSelectedIndex);

            mMinusTimerHandler.postDelayed(this, 250);
        }
    };


    long mPlusStartTime = 0;
    Handler mPlusTimerHandler = new Handler();
    Runnable mPlusTimerRunnable = new Runnable() {
        @Override
        public void run() {
//            long millis = System.currentTimeMillis() - mMinusStartTime;
//            int seconds = (int) (millis / 1000);
//            int minutes = seconds / 60;
//            seconds = seconds % 60;

            int newSelectedIndex = mSelectedIndex + 1;
            setSelectedIndex(newSelectedIndex);

            mPlusTimerHandler.postDelayed(this, 250);
        }
    };

}