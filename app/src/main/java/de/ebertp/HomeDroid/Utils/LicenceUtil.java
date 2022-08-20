package de.ebertp.HomeDroid.Utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import de.ebertp.HomeDroid.BuildConfig;
import de.ebertp.HomeDroid.R;
import timber.log.Timber;

public class LicenceUtil {

    public static void checkLicence(Context ctx, boolean notifiyUserOnError, boolean notifiyUserOnSuccess) {
        if (!BuildConfig.LICENSE_CHECK) {
            PreferenceHelper.setIsUnlocked(ctx, true);
            return;
        }

        // This may fail on some newer phones with Android 11+ for whatever reason
        int challenge = 4221;

        Uri auth = Uri.parse("content://de.ebertp.HomeDroid.Donate.Auth");
        String[] projection = {"value"};
        String selection = Integer.toString(challenge);
        Cursor cursor = ctx.getContentResolver().query(auth, projection, selection, null, null);

        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            int rValue = cursor.getInt(0);

            if (rValue / 24 == challenge) {
                Timber.i("Activation successful");
                PreferenceHelper.setIsUnlocked(ctx, true);
                if (notifiyUserOnSuccess) {
                    Toast.makeText(ctx, ctx.getString(R.string.activation_successful),
                            Toast.LENGTH_LONG).show();
                }
                de.ebertp.HomeDroid.Utils.Util.closeCursor(cursor);
                return;
            }
        }
        de.ebertp.HomeDroid.Utils.Util.closeCursor(cursor);

        // fallbback check
        PackageManager pm = ctx.getPackageManager();
        boolean isInstalled = isPackageInstalled("de.ebertp.HomeDroid.Donate", pm);

        if(isInstalled) {
            Timber.i("Activation successful");
            PreferenceHelper.setIsUnlocked(ctx, true);
            if (notifiyUserOnSuccess) {
                Toast.makeText(ctx, ctx.getString(R.string.activation_successful),
                        Toast.LENGTH_LONG).show();
            }
            return;
        }

        if (notifiyUserOnError) {
            Toast.makeText(ctx, ctx.getString(R.string.activation_not_successful),
                    Toast.LENGTH_LONG).show();
        }

        PreferenceHelper.setIsUnlocked(ctx, false);
        Timber.i("Activation not successful");
    }

    private static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

}
