package de.ebertp.HomeDroid.Activities.Wizard;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class WizardViewPager extends ViewPager {

    private boolean mIsSwipeEnabled;

    public WizardViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mIsSwipeEnabled = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsSwipeEnabled) {
            return super.onTouchEvent(event);
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mIsSwipeEnabled) {
            return super.onInterceptTouchEvent(event);
        }
        return false;
    }

    /**
     * Custom method to enable or disable swipe
     *
     * @param isSwipeEnabled true to enable swipe, false otherwise
     *
     */
    public void setPagingEnabled(boolean isSwipeEnabled) {
        this.mIsSwipeEnabled = isSwipeEnabled;
    }
}

