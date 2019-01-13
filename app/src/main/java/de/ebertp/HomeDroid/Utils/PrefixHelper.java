package de.ebertp.HomeDroid.Utils;

import android.content.Context;

import de.ebertp.HomeDroid.DbAdapter.BaseDbAdapter;

public class PrefixHelper {

    public static int getPrefix(Context ctx) {
        return PreferenceHelper.getPrefix(ctx);
    }

    public static int getFullPrefix(Context ctx) {
        return getPrefix(ctx) * BaseDbAdapter.PREFIX_OFFSET;
    }

    public static int removePrefix(Context ctx, int idWithPrefix) {
        return idWithPrefix % (Math.max(1, getPrefix(ctx)) * BaseDbAdapter.PREFIX_OFFSET);
    }


}
