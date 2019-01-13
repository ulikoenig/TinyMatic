package de.ebertp.HomeDroid;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class InterceptViewGroup extends LinearLayout {

    boolean isEditMode = false;

    public InterceptViewGroup(Context context) {
        super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isEditMode) {
            onTouchEvent(ev);
            return true;
        }
        return false;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
    }

}
