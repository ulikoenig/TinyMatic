package de.ebertp.HomeDroid.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import androidx.core.graphics.drawable.DrawableCompat;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Pattern;

import de.cketti.library.changelog.ChangeLog;
import de.ebertp.HomeDroid.Activities.Drawer.MainActivity;
import de.ebertp.HomeDroid.BuildConfig;
import de.ebertp.HomeDroid.DbAdapter.BaseDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.DataBaseAdapterManager;
import de.ebertp.HomeDroid.Model.HMLayerItem;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.Model.HMScript;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.ViewAdapter.CursorToObjectHelper;

public class Util {

    public static boolean isLandscape(Context ctx) {
        return ctx.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static Point getScreenDimenions(Activity ctx) {
        Display display = ctx.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static int getRandomNumber(int min, int max) {
        return min + (int) (Math.random() * ((max - min) + 1));
    }

    public static String getRoundUpString(String source, int minLength) {

        if (source.length() < minLength) {

            StringBuilder sb = new StringBuilder(minLength);
            sb.append(source);

            while (sb.length() < minLength) {
                sb.append(" ");
            }
            return sb.toString();
        }

        return source;
    }

    public static void setIconColor(boolean isDarkTheme, ImageView iv) {
        if (iv != null) {
            if (!isDarkTheme) {
                iv.setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
            }
        }
    }

    public static void resizeView(View view, int newWidthPx, int newHeightPx) {
        try {
            Constructor<? extends ViewGroup.LayoutParams> ctor = view.getLayoutParams().getClass().getDeclaredConstructor(int.class, int.class);
            view.setLayoutParams(ctor.newInstance(newWidthPx, newHeightPx));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void tintDark(ImageView iv) {
        tintDark(iv.getDrawable());
    }

    public static void tintDark(Drawable drawable) {
        Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        wrappedDrawable = wrappedDrawable.mutate();
        DrawableCompat.setTint(wrappedDrawable, Color.DKGRAY);
    }

    public static Drawable getColoredIcon(Context ctx, boolean isDarkTheme, int res) {
        Drawable drawable = ctx.getResources().getDrawable(res);
        if (isDarkTheme) {
            //iv.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        } else {
            drawable.setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
        }
        return drawable;
    }

    public static void setLLChildrenIconColor(boolean isDarkTheme, LinearLayout ll) {
        int childcount = ll.getChildCount();
        for (int i = 0; i < childcount; i++) {
            setIconColor(isDarkTheme, (ImageView) ll.getChildAt(i));
        }
    }

    public static void setTextColor(int colorRes, ViewGroup view) {
        int childcount = view.getChildCount();
        for (int i = 0; i < childcount; i++) {
            if (view.getChildAt(i) instanceof TextView) {
                ((TextView) view.getChildAt(i)).setTextColor(colorRes);
            } else if (view.getChildAt(i) instanceof ViewGroup) {
                setTextColor(colorRes, (ViewGroup) view.getChildAt(i));
            }
        }

    }

    private static Pattern doublePattern = Pattern.compile("-?\\d+(\\.\\d*)?");

    public static boolean isDouble(String string) {
        return doublePattern.matcher(string).matches();
    }

    public static final int TYPE_CHANNEL = 0;
    public static final int TYPE_SCRIPT = 1;
    public static final int TYPE_VARIABLE = 2;
    public static final int TYPE_DATAPOINT = 3;

    public static final float XML_API_MIN_VERSION = 1.10f;
    public static final String XML_API_MIN_VERSION_STRING = "1.10";

    public static float pixelsToSp(Context context, Float px) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return px / scaledDensity;
    }

    public static int spToPixels(Context context, Float sp) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * scaledDensity);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static final int SCREEN_XLARGE = 4;

    public static final String INTENT_NEWVALUE = "HomeDroid.newValue";
    public static final String INTENT_WIDGET_TYPE = "HomeDroid.type";
    public static final String INTENT_ISE_ID = "HomeDroid.iseId";

    public static final int REQUEST_REFRESH_NOTIFY = 123;
    public static final int REQUEST_RESULT_RESTART = 101;

    public static final int REQUEST_SELECT_ICON_INTERNAL = 11;
    public static final int REQUEST_SELECT_ICON_EXTERNAL = 12;

    public static final int REQUEST_SELECT_ITEM = 13;
    public static final int REQUEST_SET_BACKGROUND = 14;

    public static final int REQUEST_VOICE_RECOGINTION = 15;

    public static final int REQUEST_WIZARD = 16;


    public static final String SHARED_PREF_NAME = "de.ebertp.HomeDroid_preferences.xml";

    public static int getSecondsToNextMinute() {
        GregorianCalendar calendar = new GregorianCalendar();
        return (59 - calendar.get(Calendar.SECOND)) * 1000;
    }

    public static int getMinutesInMilis(int minutes) {
        return minutes * 60 * 1000;
    }

    public static String getSharedPrefName(Context ctx) {
        return SHARED_PREF_NAME;
    }

    public static void restart2(final Context ctx) {
        Intent intent = new Intent(ctx, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    public static void addRefreshNotify(View v) {
        if (v != null && v.findViewById(R.id.indicatorSendingCommand) != null) {
            v.findViewById(R.id.indicatorSendingCommand).setVisibility(View.VISIBLE);
        }
    }

    public static String getWindDirection(double d) {
        if (d < 23) {
            return "N";
        } else if (d < 68) {
            return "NO";
        } else if (d < 113) {
            return "O";
        } else if (d < 158) {
            return "S0";
        } else if (d < 203) {
            return "S";
        } else if (d < 248) {
            return "SW";
        } else if (d < 293) {
            return "W";
        } else if (d < 338) {
            return "NW";
        } else {
            return "N";
        }

    }

    public static void closeCursor(Cursor cursor) {
        if (cursor != null) {
            if (!cursor.isClosed()) {
                cursor.close();
            }
            cursor = null;
        }
    }

    public static int removePrefix(Context ctx, int ise_id) {
        return ise_id % ((Math.max(1, PreferenceHelper.getPrefix(ctx)) * BaseDbAdapter.PREFIX_OFFSET));
    }

    public static Bitmap getScaledPicture(Context ctx, int imageId) throws FileNotFoundException {
        Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(imageId));
        AssetFileDescriptor fileDescriptor = ctx.getContentResolver().openAssetFileDescriptor(uri, "r");

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);

        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        options.inSampleSize = calculateInSampleSize(options, display.getWidth(), display.getHeight());
        options.inJustDecodeBounds = false;

        Bitmap scaledBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);

        return scaledBitmap;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    public static String removePrefix(Context ctx, String ise_id) {
        return Integer.toString(removePrefix(ctx, Integer.parseInt(ise_id)));
    }

    public static Bitmap getPicture(Context ctx, int imageId) {
        Bitmap bmp = null;
        try {
            Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(imageId));
            bmp = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), uri);

            if (bmp == null) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;
    }

    public static Bitmap getPictureForScreenSize(Activity activity, int imageId) {
        try {
            return Util.getScaledPicture(activity, imageId);
        } catch (FileNotFoundException e) {
            PreferenceHelper.removeSplashScreen(activity);
            e.printStackTrace();
        }
        return null;
    }

    public static HMObject getHmObject(int iseId, int type, DataBaseAdapterManager dbM) {
        Cursor d = null;
        HMObject reference = null;

        switch (type) {
            case Util.TYPE_CHANNEL: {
                d = dbM.channelsDbAdapter.fetchItem(iseId);
                if (d != null && d.moveToFirst()) {
                    reference = CursorToObjectHelper.convertCursorToChannels(d).get(0);
                }
                break;
            }
            case Util.TYPE_SCRIPT: {
                d = dbM.programsDbAdapter.fetchItem(iseId);
                if (d != null && d.moveToFirst()) {
                    reference = CursorToObjectHelper.convertCursorToPrograms(d).get(0);
                }
                break;
            }
            case Util.TYPE_VARIABLE: {
                d = dbM.varsDbAdapter.fetchItem(iseId);
                if (d != null && d.moveToFirst()) {
                    reference = CursorToObjectHelper.convertCursorToVariables(d).get(0);
                }
                break;
            }
        }
        de.ebertp.HomeDroid.Utils.Util.closeCursor(d);

        return reference;

    }

    public static HMLayerItem getLayerItem(DataBaseAdapterManager dbM, Cursor layerItem) {
        int rowId = layerItem.getInt(layerItem.getColumnIndex("_id"));
        int iseId = layerItem.getInt(layerItem.getColumnIndex("ise_id"));
        int type = layerItem.getInt(layerItem.getColumnIndex("type"));
        Integer posX = layerItem.getInt(layerItem.getColumnIndex("pos_x"));
        Integer posY = layerItem.getInt(layerItem.getColumnIndex("pos_y"));
        String name = layerItem.getString(layerItem.getColumnIndex("name"));

        return new HMLayerItem(rowId, iseId, type, posX, posY, name);
    }

    // public static String getFloor(String name){
    // if(name.split(".").length>1){
    // return name.split(".")[0];
    // }
    // return "";
    // }

    public static boolean hasIcon(DataBaseAdapterManager dbM, int rowId) {
        Cursor ice = dbM.externalIconDbAdapter.fetchItem(rowId);
        if (ice.getCount() != 0) {
            ice.close();
            return true;
        }
        ice.close();

        Cursor ici = dbM.iconDbAdapter.fetchItem(rowId);
        if (ici.getCount() != 0) {
            ici.close();
            return true;
        }
        ici.close();

        return false;

    }

    public static void removeIcon(DataBaseAdapterManager dbM, int rowId) {
        dbM.externalIconDbAdapter.deleteItem(rowId);
        dbM.iconDbAdapter.deleteItem(rowId);
    }

    public static final String DATE_FORMAT_LONG = "dd.MM.yyyy HH:mm";


    public static String getTimeStamp(HMScript hms) {
        return getTimeStamp(hms.getTimestamp());
    }

    public static String getTimeStamp(String timestampRaw) {
        try {
            Date date = new Date(Long.parseLong(((timestampRaw))) * 1000);
            return new SimpleDateFormat(Util.DATE_FORMAT_LONG).format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isSpeechSupported(Context ctx) {
        // Check to see if a recognition activity is present
        PackageManager pm = ctx.getPackageManager();
        List<ResolveInfo> activities =
                pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0) {
            return false;
        }
        return true;

    }

    public static ChangeLog getChangelog(Context ctx) {
        if (PreferenceHelper.isDarkTheme(ctx)) {
            return new Util.DarkThemeChangeLog(ctx);
        } else {
            return new ChangeLog(ctx);
        }
    }

    public static int getDialogTheme(Context ctx) {
        if (PreferenceHelper.isDarkTheme(ctx)) {
            return R.style.DialogThemeDark;
        }
        return R.style.DialogThemeLight;
    }

    public static String getVersionInfo() {
        return BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ")";
    }

    /**
     * Example that shows how to create a themed dialog.
     */
    public static class DarkThemeChangeLog extends ChangeLog {
        public static final String DARK_THEME_CSS =
                "body { color: #ffffff; background-color: #282828; }" + "\n" + DEFAULT_CSS;

        public DarkThemeChangeLog(Context context) {
            super(new ContextThemeWrapper(context, R.style.HomeDroidDark), DARK_THEME_CSS);
        }
    }

    // http://stackoverflow.com/questions/26794275/how-do-i-ignore-case-when-using-startswith-and-endswith-in-java
    public static boolean startsWithIgnoreCase(String str, String prefix) {
        return str.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
