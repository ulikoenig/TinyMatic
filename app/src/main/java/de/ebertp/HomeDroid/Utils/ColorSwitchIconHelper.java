package de.ebertp.HomeDroid.Utils;

import android.content.Context;

import de.ebertp.HomeDroid.R;

public class ColorSwitchIconHelper {

    private static final int OFF = 0;
    private static final int BLUE = 1;
    private static final int GREEN = 2;
    private static final int TURQUOISE = 3;
    private static final int RED = 4;
    private static final int PURPLE = 5;
    private static final int YELLOW = 6;
    private static final int WHITE = 7;

    public static int getIcon(Context ctx, int ledStatus) {
        switch (ledStatus) {
            case OFF:
                return R.drawable.led_background_grey;
            case BLUE:
                return R.drawable.led_background_blue;
            case GREEN:
                return R.drawable.led_background_green;
            case TURQUOISE:
                return R.drawable.led_background_turquoise;
            case RED:
                return R.drawable.led_background_red;
            case PURPLE:
                return R.drawable.led_background_purple;
            case YELLOW:
                return R.drawable.led_background_yellow;
            case WHITE:
                return R.drawable.led_background_white;
            default:
                return R.drawable.led_background_grey;
        }
    }
}
