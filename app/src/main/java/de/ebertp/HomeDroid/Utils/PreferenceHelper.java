package de.ebertp.HomeDroid.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class PreferenceHelper {

    public static void setWriteCount(Context ctx, int writeCount) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("write_count", writeCount);
        editor.commit();
    }

    public static int getWriteCount(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getInt("write_count", 0);
    }

    public static synchronized void setSyncSuccessful(Context ctx, boolean b) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("sync_success", b);
        editor.commit();

        setLastSync(ctx, b);
    }

    public static boolean getSyncSuccessful(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("sync_success", false);
    }

    public static boolean isSyncAtStart(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        return prefs.getBoolean("syncAtStart", true);
    }

    public static boolean isUpdateServiceEnabled(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        return prefs.getBoolean("updateServiceEnabled", false);
    }

    public static void setLastSync(Context ctx, boolean successful) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM - HH:mm", Locale.getDefault());
        String currentDateTimeString = (String) simpleDateFormat.format(new Date());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();

        if (successful) {
            setLastSuccessfulSync(ctx);
            editor.putString("lastSync", currentDateTimeString);
        } else {
            editor.putString("lastSync", "[X]" + currentDateTimeString);
        }

        editor.commit();
    }

    public static void setLastSuccessfulSync(Context ctx) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM HH:mm", Locale.getDefault());
        String currentDateTimeString = simpleDateFormat.format(new Date());

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        editor.putString("lastSuccSync", currentDateTimeString).commit();
    }

    public static String getLastSuccessfulSync(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        return prefs.getString("lastSuccSync", "--");
    }

    public static void setLocationHome(Context ctx, Double latitude, Double longitude) {
        Timber.d("Setting as home: " + latitude + "/" + longitude);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("locationHomeLatitude", latitude.toString());
        editor.putString("locationHomeLongitude", longitude.toString());
        editor.commit();
    }

    public static String getLongitude(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("locationHomeLongitude", null);
    }

    public static String getLatitude(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("locationHomeLatitude", null);
    }

    public static String getLastSync(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        return prefs.getString("lastSync", "--");
    }

    public static boolean getPeriodicUpdatesEnabled(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        return prefs.getBoolean("periodicUpdatesEnabled", true);
    }

    public static Integer getPeriodicUpdateInterval(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        return Integer.parseInt(prefs.getString("periodicUpdateInterval", "5"));
    }

    public static String getPeriodicUpdateIntervalAsString(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        return prefs.getString("periodicUpdateInterval", "5");
    }

    public static boolean isLocationEnabled(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("enableLocation", false);
    }

    public static int getStartOnArrival(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getInt("startOnArrival", 0);
    }

    public static int getStartOnDeparture(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getInt("startOnDeparture", 0);
    }

    public static String getHomeProximity(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("homeProximity", "500");
    }

    public static void setHomeProximity(Context ctx, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("homeProximity", value);
        editor.commit();
    }

    public static List<String> getHomeWifi(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String itemsString = prefs.getString("homeWifi", null);

        if (itemsString == null || itemsString.isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.asList(itemsString.split((";")));
    }

    public static void setHomeWifi(Context ctx, List<String> items) {
        StringBuilder itemsString = new StringBuilder();

        for (String item : items) {
            itemsString.append(item).append(";");
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("homeWifi", itemsString.toString());
        editor.commit();
    }

    public static void setStartOnArrival(Context ctx, int progId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("startOnArrival", progId);
        editor.commit();
    }

    public static void setStartOnDeparture(Context ctx, int progId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("startOnDeparture", progId);
        editor.commit();
    }

    public static boolean isClientAuthEnable(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("enableClientAuth", false);
    }

    public static String getClientCertPath(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("clientCertPath", null);
    }

    public static String getClientAuthPwd(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("clientAuthPwd", null);
    }

    public static boolean isPerDatapointsEnabled(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("per_datapoints", false);
    }

    public static boolean isPerVariablesEnabled(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("per_variables", false);
    }

    public static boolean isPerProgramsEnabled(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("per_programs", false);
    }

    public static int getPrefix(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return Integer.parseInt(prefs.getString("prefix", "0"));
    }

    public static void setPrefix(Context ctx, int prefix) {
        if (prefix < 10) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("prefix", prefix);
            editor.commit();
        }
    }

    public static boolean isMHEnabled(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("meineHomematic", false);
    }

    public static String getMHUser(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("mhUser", "");
    }

    public static String getMHId(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("mhId", "");
    }

    public static String getMHPass(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("mhPass", "");
    }

    public static int getDefaultTab(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return Integer.valueOf(prefs.getString("default_tab", "0"));
    }

    public static boolean isNotificationsEnabled(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("showNotifications", true);
    }

    public static boolean isHttpsOn(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("httpsOn", false);
    }

    public static boolean isRemoteServerEnabled(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("useRemoteServer", false);
    }

    public static String getRemoteServer(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("remoteServer", "homematic-ccu2");
    }

    public static String getServer(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("server", "homematic-ccu3");
    }

    public static void setServer(Context ctx, String address) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("server", address);
        editor.commit();
    }


    public static boolean isRemotePortUsed(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("useRemotePort", false);
    }

    public static boolean isHttpAuthUsed(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("enableHttpAuth", false);
    }

    public static String getRemoteServerPort(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("serverPort", "");
    }

    // public static long getCommandCount(Context ctx) {
    // SharedPreferences prefs =
    // PreferenceManager.getDefaultSharedPreferences(ctx);
    // return Long.parseLong(prefs.getString("commandCount", "0"));
    // }
    //
    // public static void incrementCommandCount(Context ctx){
    // SharedPreferences prefs =
    // PreferenceManager.getDefaultSharedPreferences(ctx);
    // SharedPreferences.Editor editor = prefs.edit();
    // editor.putString("commandCount", Long.toString(getCommandCount(ctx)+1));
    // editor.commit();
    // }

    public static long getCommandCount(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getLong("commandCount", 0);
    }

    public static void setCommandCount(Context ctx, long commandCount) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("commandCount", commandCount);
        editor.commit();
    }

    public static void incrementCommandCount(Context ctx) {
        setCommandCount(ctx, (getCommandCount(ctx) + 1));
    }

    public static boolean isClosed(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("isClosed", false);
    }

    public static void setIsClosed(Context ctx, boolean isClosed) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isClosed", isClosed);
        editor.commit();
    }

    public static boolean isUnlocked(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("isUnlockedTest", false);
    }

    public static void setIsUnlocked(Context ctx, boolean unlocked) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isUnlockedTest", unlocked);
        editor.commit();
    }

    public static boolean showErrorMessages(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("showErrorMessages", true);
    }

    public static void setShowErrorMessages(Context ctx, boolean unlocked) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("showErrorMessages", unlocked);
        editor.commit();
    }

    public static void setChildProtectPW(Context ctx, String pw) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("childProtectPW", pw);
        editor.commit();
    }

    public static String getChildProtectPW(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String value = prefs.getString("childProtectPW", "test");

        if (value.equals("")) {
            return "test";
        }
        return value;
    }

    public static void setApiVersion(Context ctx, String version) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("xmlApiVersion", version);
        editor.commit();
    }

    public static String getApiVersion(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("xmlApiVersion", null);
    }

    public static boolean isChildProtectionOn(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("enableChildProtection", false);
    }

    public static void setChildProtectionOn(Context ctx, boolean bool) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("enableChildProtection", bool);
        editor.commit();
    }

    public static boolean isSyncProtocol(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("syncProtocol", false);
    }

    public static void setSyncProtocol(Context ctx, boolean bool) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("syncProtocol", bool);
        editor.commit();
    }

    public static boolean isDisclaimerAccepted(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("disclaimerAccepted", false);
    }

    public static void setDisclaimerAccepted(Context ctx, boolean bool) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("disclaimerAccepted", bool);
        editor.commit();
    }

    public static boolean isHideUnsupported(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("hideUnsupported", false);
    }

    public static void setIsLegacyLayout(Context ctx, boolean b) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("legacy_layout", b);
        editor.commit();
    }

    public static boolean isLegacyLayout(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("legacy_layout", false);
    }

    public static void setIsDarkTheme(Context ctx, boolean b) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("dark_theme", b);
        editor.commit();
    }

    public static boolean isDarkTheme(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("dark_theme", true);
    }

    public static void setHideLocalFavs(Context ctx, boolean b) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("hide_local_favs", b);
        editor.commit();
    }

    public static boolean isHideLocalFavs(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("hide_local_favs", false);
    }

    public static final int COMMANDS_SIZE = 3;

    public static synchronized void addSentCommand(Context ctx, String newCommand) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        ArrayList<String> commands = getLastCommands(ctx);

        if (commands.size() == COMMANDS_SIZE) {
            commands.remove(0);
        }
        commands.add(newCommand);

        int i = 0;
        String result = "";
        for (String s : commands) {
            result += s + ";";
            if (i == COMMANDS_SIZE) {
                break;
            }
            i++;
        }
        prefs.edit().putString("lastCommands", result).commit();
    }

    public static ArrayList<String> getLastCommands(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String commands = prefs.getString("lastCommands", null);

        if (commands == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(commands.split(";")));
    }

    public static String getOrderBy(Context ctx, String value) {
        if (isSortByName(ctx)) {
            return value;
        }
        return null;
    }

    public static boolean isSortByName(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("sort_by_name", true);
    }

    public static void setGridBreakLimit(Context ctx, int num) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("grid_break_limit", num);
        editor.commit();
    }

    public static int getGridBreakLimit(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String value = prefs.getString("grid_break_limit", "12");
        if (TextUtils.isEmpty(value)) {
            setGridBreakLimit(ctx, 12);
        }
        return Integer.parseInt(prefs.getString("grid_break_limit", "12"));
    }

    public static void setIsHideNotVisible(Context ctx, boolean b) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("hide_not_visible", b);
        editor.commit();
    }

    public static boolean isHideNotVisible(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("hide_not_visible", true);
    }

    public static void setIsDisableNotOperate(Context ctx, boolean b) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("disable_not_operate", b);
        editor.commit();
    }

    public static boolean isDisableNotOperate(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("disable_not_operate", true);
    }

    public static void setIsSyncOnFailure(Context ctx, boolean b) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("sync_on_failure", b);
        editor.commit();
    }

    public static boolean isSyncOnFailure(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("sync_on_failure", true);
    }

    public static void setSplashScreen(Context ctx, int imageId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt("replace_splash", imageId);
        editor.commit();
    }

    public static int getSplashScreen(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getInt("replace_splash", 0);
    }

    public static void removeSplashScreen(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();

        editor.remove("replace_splash");
        editor.commit();
    }

    public static void setIsMinimalMode(Context ctx, boolean b) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("layer_is_minimalmode", b);
        editor.commit();
    }

    public static boolean isMinimalMode(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("layer_is_minimalmode", false);
    }

    public static void setIsLayerHelpShown(Context ctx, boolean b) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("layer_help_shown", b);
        editor.commit();
    }

    public static boolean isLayerHelpShown(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("layer_help_shown", false);
    }

    public static void setDefaultLayer(Context ctx, int layerId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("default_layer", layerId);
        editor.commit();
    }

    public static int getDefaultLayer(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getInt("default_layer", -1);
    }

    public static void setIsImmersiveModeEnabled(Context ctx, boolean b) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("is_immersive", b);
        editor.commit();
    }

    public static boolean isImmersiveModeEnabled(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("is_immersive", false);
    }

    public static void setIsQuitDismissed(Context ctx, boolean b) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("is_quit_dismissed", b);
        editor.commit();
    }

    public static boolean isQuitDismissed(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("is_quit_dismissed", false);
    }


    public static boolean isLocalWifiDetectionOn(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("autoSwitchMode", false);
    }

    public static boolean isSyncOnHomeWifiOnly(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("syncOnHomeWifiOnly", false);
    }

    public static boolean isDetectUnrenamedChannels(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("detectUnrenamedChannels", true);
    }

    public static boolean isLayerOrientationSet(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.contains("isLayerOrientationLandscape");
    }

    public static boolean isLayerOrientationLandscape(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("isLayerOrientationLandscape", true);
    }

    public static void setOrientationLandscape(Context ctx, boolean isLandscape) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("isLayerOrientationLandscape", isLandscape);
        editor.commit();
    }

    public static boolean isSysVarQuickAccessEnabled(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("sysvarQuickAccess", true);
    }

    public static void setSysVarQuickAccessEnabled(Context ctx, boolean enabled) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("sysvarQuickAccess", enabled);
        editor.commit();
    }

    public static Integer getWidgetTextSize(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        //TODO store in profile backup!!
        return Integer.parseInt(prefs.getString("widgetTextSize", "2"));
    }

    public static boolean isSyncInForegroundOnly(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("foregroundOnly", false);
    }

    public static int getVersionCode(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getInt("versionCode", -1);
    }

    public static void setVersionCode(Context ctx, int versionCode) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt("versionCode", versionCode);
        editor.commit();
    }

    public static void setHiddenDrawerItems(Context ctx, List<Integer> items) {
        StringBuilder itemsString = new StringBuilder();

        for (Integer item : items) {
            itemsString.append(Integer.toString(item)).append(";");
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("visibleDrawerItems", itemsString.toString());
        editor.commit();
    }

    public static List<Integer> getHiddenDrawerItems(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String itemsString = prefs.getString("visibleDrawerItems", null);

        if (itemsString == null || itemsString.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> items = new ArrayList<>();
        for (String itemString : itemsString.split(";")) {
            items.add(Integer.parseInt(itemString));
        }

        return items;
    }

    public static boolean isFullSyncTried(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("isFullSyncTried", false);
    }

    public static void setFullSyncTried(Context ctx, boolean isClosed) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isFullSyncTried", isClosed);
        editor.commit();
    }

    public static boolean isSslCheckEnabled(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("sslCheckOn", false);
    }

    public static long getLaunchCount(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getLong("launchCount", 0);
    }

    public static void setLaunchCount(Context ctx, long count) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("launchCount", count);
        editor.commit();
    }

    public static void incrementLaunchCount(Context ctx) {
        setLaunchCount(ctx, (getLaunchCount(ctx) + 1));
    }

    public static boolean isUserHasGivenFeedback(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("isUserHasGivenFeedback", false);
    }

    public static void setUserHasGivenFeedback(Context ctx, boolean hasGivenFeedback) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("isUserHasGivenFeedback", hasGivenFeedback);
        editor.commit();
    }

    public static long getUserDimissedRatingAtSystemTimeMilis(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getLong("UserDimissedRatingAtSystemTimeMilis", -1);
    }

    public static void setUserDimissedRatingAtSystemTimeMilis(Context ctx, long systemTimeMilis) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("UserDimissedRatingAtSystemTimeMilis", systemTimeMilis);
        editor.commit();
    }

    public static void setIsBatterySavingNoteDismissed(Context ctx, boolean b) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("is_batter_note_dismissed", b);
        editor.commit();
    }

    public static boolean isBatterySavingNoteDismissed(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("is_batter_note_dismissed", false);
    }
}
