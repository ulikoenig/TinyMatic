package de.ebertp.HomeDroid.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.widget.Toast;

import de.ebertp.HomeDroid.Communication.Sync.SyncJobIntentService;
import de.ebertp.HomeDroid.Communication.XmlToDbParser;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.ProfileDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.DataBaseAdapterManager;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.R;
import timber.log.Timber;

public class ProfileHelper {


    public static void createOrUpdateProfile(Context ctx, int profileId, String name) {
        DataBaseAdapterManager dbM = HomeDroidApp.db();
        ProfileDbAdapter profileDb = dbM.profilesDbAdapter;

        if (name != null) {
            profileDb.createOrUpdatePrefs(profileId, name);
        }
        profileDb.addProperty(profileId,
                "syncAtStart",
                PreferenceHelper.isSyncAtStart(ctx));

        profileDb.addProperty(profileId,
                "updateServiceEnabled",
                PreferenceHelper.isUpdateServiceEnabled(ctx));

        profileDb.addProperty(profileId,
                "periodicUpdatesEnabled ",
                PreferenceHelper.getPeriodicUpdatesEnabled(ctx));

        profileDb.addProperty(profileId,
                "per_datapoints",
                PreferenceHelper.isPerDatapointsEnabled(ctx));

        profileDb.addProperty(profileId,
                "per_variables ",
                PreferenceHelper.isPerVariablesEnabled(ctx));

        profileDb.addProperty(profileId,
                "per_programs ",
                PreferenceHelper.isPerProgramsEnabled(ctx));

        profileDb.addProperty(profileId,
                "periodicUpdateInterval ",
                PreferenceHelper.getPeriodicUpdateIntervalAsString(ctx));

        profileDb.addProperty(profileId,
                "server",
                PreferenceHelper.getServer(ctx));

        profileDb.addProperty(profileId,
                "meineHomematic ",
                PreferenceHelper.isMHEnabled(ctx));

        profileDb.addProperty(profileId,
                "mhUser",
                PreferenceHelper.getMHUser(ctx));

        profileDb.addProperty(profileId,
                "mhId",
                PreferenceHelper.getMHId(ctx));

        profileDb.addProperty(profileId,
                "mhPass",
                PreferenceHelper.getMHPass(ctx));

        profileDb.addProperty(profileId,
                "default_tab",
                Integer.toString(PreferenceHelper.getDefaultTab(ctx)));

        profileDb.addProperty(profileId,
                "showNotifications ",
                PreferenceHelper.isNotificationsEnabled(ctx));

        profileDb.addProperty(profileId,
                "httpsOn",
                PreferenceHelper.isHttpsOn(ctx));

        profileDb.addProperty(profileId,
                "useRemoteServer ",
                PreferenceHelper.isRemoteServerEnabled(ctx));

        profileDb.addProperty(profileId,
                "remoteServer",
                PreferenceHelper.getRemoteServer(ctx));

        profileDb.addProperty(profileId,
                "useRemotePort",
                PreferenceHelper.isRemotePortUsed(ctx));

        profileDb.addProperty(profileId,
                "serverPort",
                PreferenceHelper.getRemoteServerPort(ctx));

//        profileDb.addProperty(profileId,
//                "enableHttpAuth",
//                PreferenceHelper.getRemoteServerPort(ctx));
//
//        profileDb.addProperty(profileId,
//                "httpAuthUser",
//                PreferenceHelper.getRemoteServerPort(ctx));
//
//        profileDb.addProperty(profileId,
//                "httpAuthPw",
//                PreferenceHelper.getRemoteServerPort(ctx));
//
//        profileDb.addProperty(profileId,
//                "enableClientAuth",
//                PreferenceHelper.getRemoteServerPort(ctx));
//
//        profileDb.addProperty(profileId,
//                "clientCertPath",
//                PreferenceHelper.getRemoteServerPort(ctx));
//
//        profileDb.addProperty(profileId,
//                "clientAuthPwd",
//                PreferenceHelper.getRemoteServerPort(ctx));
//
//
//
//        profileDb.addProperty(profileId,
//                "clientAuthPwd",
//                PreferenceHelper.getRemoteServerPort(ctx));
    }

    public static void loadProfile(Context ctx, int profileId) {
        Timber.d("loadProfile() " + profileId);

        if (SyncJobIntentService.isSyncRunning()) {
            Toast.makeText(ctx, ctx.getString(R.string.wait_for_refresh), Toast.LENGTH_LONG).show();
        } else {
            DataBaseAdapterManager dbM = HomeDroidApp.db();
            ProfileDbAdapter profileDb = dbM.profilesDbAdapter;
            Cursor c = profileDb.getProfile(profileId);

            if (c.getCount() != 0) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("syncAtStart", c.getInt(c.getColumnIndex("syncAtStart")) > 0);
                editor.putBoolean("updateServiceEnabled", c.getInt(c.getColumnIndex("updateServiceEnabled")) > 0);
                editor.putBoolean("periodicUpdatesEnabled", c.getInt(c.getColumnIndex("periodicUpdatesEnabled")) > 0);
                editor.putBoolean("per_datapoints", c.getInt(c.getColumnIndex("per_datapoints")) > 0);
                editor.putBoolean("per_variables", c.getInt(c.getColumnIndex("per_variables")) > 0);
                editor.putBoolean("per_programs", c.getInt(c.getColumnIndex("per_programs")) > 0);
                editor.putString("periodicUpdateInterval", c.getString(c.getColumnIndex("periodicUpdateInterval")));
                editor.putString("server", c.getString(c.getColumnIndex("server")));
                editor.putBoolean("meineHomematic", c.getInt(c.getColumnIndex("meineHomematic")) > 0);
                editor.putString("mhUser", c.getString(c.getColumnIndex("mhUser")));
                editor.putString("mhId", c.getString(c.getColumnIndex("mhId")));
                editor.putString("mhPass", c.getString(c.getColumnIndex("mhPass")));
                editor.putString("default_tab", c.getString(c.getColumnIndex("default_tab")));
                editor.putBoolean("showNotifications", c.getInt(c.getColumnIndex("showNotifications")) > 0);
                editor.putBoolean("httpsOn", c.getInt(c.getColumnIndex("httpsOn")) > 0);
                editor.putBoolean("useRemoteServer", c.getInt(c.getColumnIndex("useRemoteServer")) > 0);
                editor.putString("remoteServer", c.getString(c.getColumnIndex("remoteServer")));
                editor.putBoolean("useRemotePort", c.getInt(c.getColumnIndex("useRemotePort")) > 0);
                editor.putString("serverPort", c.getString(c.getColumnIndex("serverPort")));
//                editor.putBoolean("enableHttpAuth", c.getInt(c.getColumnIndex("enableHttpAuth")) > 0);
//                editor.putString("httpAuthUser", c.getString(c.getColumnIndex("httpAuthUser")));
//                editor.putString("httpAuthPw", c.getString(c.getColumnIndex("httpAuthPw")));
//                editor.putBoolean("enableClientAuth", c.getInt(c.getColumnIndex("enableClientAuth")) > 0);
//                editor.putString("clientCertPath", c.getString(c.getColumnIndex("clientCertPath")));
//                editor.putString("clientAuthPwd", c.getString(c.getColumnIndex("clientAuthPwd")));

                editor.putString("prefix", Integer.toString(profileId));
                editor.commit();
            }

            de.ebertp.HomeDroid.Utils.Util.closeCursor(c);
        }
    }

    public static void deleteProfile(Context ctx, int id) {

        new XmlToDbParser(ctx, null).dropCompleteProfile();
        HomeDroidApp.db().profilesDbAdapter.deleteItem(id);

    }


}
