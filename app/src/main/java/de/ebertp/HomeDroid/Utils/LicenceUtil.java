package de.ebertp.HomeDroid.Utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import de.ebertp.HomeDroid.R;

public class LicenceUtil {

    public static boolean checkLicence(Context ctx, boolean notifiyUserOnError, boolean notifiyUserOnSuccess) {

        int challenge = 4221;

        Uri auth = Uri.parse("content://de.ebertp.HomeDroid.Donate.Auth");
        String[] projection = {"value"};
        String selection = Integer.toString(challenge);
        Cursor cursor = ctx.getContentResolver().query(auth, projection, selection, null, null);

        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            int rValue = cursor.getInt(0);

            if (rValue / 24 == challenge) {
                Log.i(ctx.getClass().getName(), "Activation successful");
                PreferenceHelper.setIsUnlocked(ctx, true);
                if (notifiyUserOnSuccess) {
                    Toast.makeText(ctx, ctx.getString(R.string.activation_successful),
                            Toast.LENGTH_LONG).show();
                }
                de.ebertp.HomeDroid.Utils.Util.closeCursor(cursor);
                return true;
            }
        }

        if (notifiyUserOnError) {
            Toast.makeText(ctx, ctx.getString(R.string.activation_not_successful),
                    Toast.LENGTH_LONG).show();
        }

        PreferenceHelper.setIsUnlocked(ctx, false);
        Log.i(ctx.getClass().getName(), "Activation not successful");

        de.ebertp.HomeDroid.Utils.Util.closeCursor(cursor);
        return false;

    }

}
