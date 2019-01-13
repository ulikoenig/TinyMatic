package de.ebertp.HomeDroid;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.sql.SQLException;

import de.ebertp.HomeDroid.Utils.PreferenceHelper;

public class EventTracker {

    public static void logEvent(Context context, String event) {
        logEvent(context, event, null);
    }

    public static void logEvent(Context context, String event, Bundle bundle) {
        FirebaseAnalytics.getInstance(context).logEvent(event, bundle);
    }

    public static void trackScreen(Activity activity, String screenName) {
        FirebaseAnalytics.getInstance(activity).setCurrentScreen(activity, screenName, null);
    }

    public static void setUserProperties(Context context) {
        FirebaseAnalytics firebase = FirebaseAnalytics.getInstance(context);
        firebase.setUserProperty("App_Unlocked", PreferenceHelper.isUnlocked(context) ? "Unlocked" : "Trial");
        firebase.setUserProperty("Theme", PreferenceHelper.isDarkTheme(context) ? "Dark" : "Light");
        firebase.setUserProperty("Remote_Access", PreferenceHelper.isRemoteServerEnabled(context) ? "Enabled" : "Disabled");
        firebase.setUserProperty("CloudMatic", PreferenceHelper.isMHEnabled(context) ? "Enabled" : "Disabled");
        firebase.setUserProperty("Home_Network_Detection", PreferenceHelper.isLocalWifiDetectionOn(context) ? "Enabled" : "Disabled");
        firebase.setUserProperty("Home_Network_Only", PreferenceHelper.isSyncOnHomeWifiOnly(context) ? "Enabled" : "Disabled");
        firebase.setUserProperty("Num_of_Profiles", Long.toString(HomeDroidApp.db().profilesDbAdapter.getCount()));
        firebase.setUserProperty("Num_of_Quick_Access", Long.toString(HomeDroidApp.db().favRelationsDbAdapter.getCount()));
        firebase.setUserProperty("Num_of_Floor_Plan", Long.toString(HomeDroidApp.db().layerItemDbAdapter.getCount()));
        firebase.setUserProperty("Location_API", PreferenceHelper.isLocationEnabled(context) ? "Enabled" : "Disabled");
        firebase.setUserProperty("HTTP_Auth", PreferenceHelper.isHttpAuthUsed(context) ? "Enabled" : "Disabled");
        firebase.setUserProperty("Update_Service", PreferenceHelper.isUpdateServiceEnabled(context) ? "Enabled" : "Disabled");
        firebase.setUserProperty("Sync_at_Start", PreferenceHelper.isSyncAtStart(context) ? "Enabled" : "Disabled");
        firebase.setUserProperty("Legacy_Tablet_Layout", PreferenceHelper.isLegacyLayout(context) ? "Enabled" : "Disabled");
        firebase.setUserProperty("Foreground_Sync", PreferenceHelper.isSyncInForegroundOnly(context) ? "Enabled" : "Disabled");
        firebase.setUserProperty("Num_of_Widgets", Long.toString(HomeDroidApp.db().widgetDbAdapter.getCount()));
        try {
            firebase.setUserProperty("Num_of_Renames", Long.toString(HomeDroidApp.db().getHmOjectSettingsDao().countOf()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            firebase.setUserProperty("Num_of_Webcams", Long.toString(HomeDroidApp.db().getWebCamDao().countOf()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
