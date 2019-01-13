package de.ebertp.HomeDroid.ViewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.Util;

/**
 * Created by Philipp on 02.11.2014.
 */
public class ViewAdder {

    public enum IconSize {
        SMALL,
        BIG
    }

    Context ctx;
    boolean isDarkTheme;
    LayoutInflater inflater;
    int layoutResItem;

    public ViewAdder(Context ctx, boolean isDarkTheme, LayoutInflater inflater, int layoutResItem) {
        this.ctx = ctx;
        this.isDarkTheme = isDarkTheme;
        this.inflater = inflater;
        this.layoutResItem = layoutResItem;
    }

    public View addNewValue(View parentView, String value) {
        return addNewValue(parentView, null, null, null, value);
    }

    public View addNewValue(View parentView, Integer icon, IconSize iconSize) {
        return addNewValue(parentView, icon, iconSize, null, null);
    }

    public View addNewValue(View parentView, Integer icon, String value) {
        return addNewValue(parentView, icon, IconSize.SMALL, value);
    }

    public View addNewValue(View parentView, Integer icon, IconSize iconSize, String value) {
        return addNewValue(parentView, icon, iconSize, null, value);
    }

    public View addNewValue(View parentView, Integer icon, int labelRes, String value) {
        return addNewValue(parentView, icon, IconSize.SMALL, ctx.getResources().getString(labelRes), value);
    }

    public View addNewValue(View parentView, Integer icon, String label, String value) {
        return addNewChildView(parentView, icon, IconSize.SMALL, label, value, false, false);
    }

    public View addNewValue(View parentView, Integer icon, IconSize iconSize, String label, String value) {
        return addNewChildView(parentView, icon, iconSize, label, value, false, false);
    }

    public View addNewCheckedView(View parentView) {
        return addNewChildView(parentView, null, null, null, null, true, false);
    }

    public View addNewButtonView(View parentView) {
        return addNewChildView(parentView, null, IconSize.SMALL, null, null, false, true);
    }


    public View addNewChildView(View parentView, Integer icon, IconSize iconSize, String label, String value, boolean showCheckbox, boolean showSwitch) {
        View view = addNewView(parentView);

        if (icon != null) {

            ImageView iv;
            if (iconSize == IconSize.BIG) {
                iv = (ImageView) view.findViewById(R.id.value_icon_big);
            } else {
                iv = (ImageView) view.findViewById(R.id.value_icon);
            }

            iv.setImageResource(icon);
            iv.setTag(icon);
            iv.setVisibility(View.VISIBLE);
            Util.setIconColor(isDarkTheme, iv);
        }

        if (label != null) {
            TextView labelView = (TextView) view.findViewById(R.id.label);
            labelView.setVisibility(View.VISIBLE);
            labelView.setText(label);
        }

        TextView valueView = (TextView) view.findViewById(R.id.value);
        if (value != null) {
            valueView.setVisibility(View.VISIBLE);
            valueView.setText(value);
        }

        if (showCheckbox) {
            view.findViewById(R.id.check).setVisibility(View.VISIBLE);
        }

        if (showSwitch) {
            view.findViewById(R.id.button).setVisibility(View.VISIBLE);
        }

        return view;

    }

    public View addNewView(View parentView) {
        View childView = inflater.inflate(layoutResItem, null);
        ((ViewGroup) parentView.findViewById(R.id.valueLL)).addView(childView);
        return childView;
    }


}
