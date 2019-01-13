package de.ebertp.HomeDroid.Utils;

import android.content.Context;
import android.graphics.Color;

import de.ebertp.HomeDroid.R;

public class LedStatusIconHelper {

    private static final int OFF = 0;
    private static final int RED = 1;
    private static final int ORANGE = 3;
    private static final int GREEN = 2;

    public static int getIcon(Context ctx, int ledStatus) {
        switch (ledStatus) {
            case OFF:
                return R.drawable.led_background_grey;
            case RED:
                return R.drawable.led_background_red;
            case ORANGE:
                return R.drawable.led_background_orange;
            case GREEN:
                return R.drawable.led_background_green;
            default:
                return R.drawable.led_background_grey;
        }
    }
}
