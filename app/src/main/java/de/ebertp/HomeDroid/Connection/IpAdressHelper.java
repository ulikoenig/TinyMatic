package de.ebertp.HomeDroid.Connection;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PermissionUtil;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;

public class IpAdressHelper {

    public static String getServerAdress(Context ctx, boolean prefix, boolean port, boolean localOnly) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String urlString;

        boolean localWifi = PreferenceHelper.isLocalWifiDetectionOn(ctx) && isHomeWifiConnected(ctx);
        if (localWifi) {
            urlString = PreferenceHelper.getServer(ctx);
        } else {
            if (PreferenceHelper.isMHEnabled(ctx)) {
                urlString = prefs.getString("mhId", "Id") + ".meine-homematic.de";
            } else {

                if (!localOnly && prefs.getBoolean("useRemoteServer", false)) {
                    urlString = PreferenceHelper.getRemoteServer(ctx);
                } else {
                    urlString = PreferenceHelper.getServer(ctx);
                }
            }
        }

        urlString = urlString.replaceAll("http://", "");
        urlString = urlString.replaceAll("https://", "");

        if (prefix) {
            if (PreferenceHelper.isHttpsOn(ctx)) {
                urlString = "https://" + urlString;
            } else {
                urlString = "http://" + urlString;
            }
        }

        if (port && !localWifi) {
            if (PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("useRemoteServer", false)) {
                if (PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("useRemotePort", false)) {
                    if (PreferenceManager.getDefaultSharedPreferences(ctx).getString("serverPort", null) != null) {
                        urlString = urlString + ":" + PreferenceManager.getDefaultSharedPreferences(ctx).getString("serverPort", "");
                    }
                }
            }
        }

        // remove unwanted chars
        urlString = urlString.replace("\n", "").replace("\r", "").replace(" ", "");

        return urlString;
    }

    public static String getDeviceIp(Context ctx) {
        String deviceIp;
        if (PreferenceHelper.isMHEnabled(ctx)) {
            deviceIp = getInetIp();
        } else {
            deviceIp = getWifiOrInetIp(ctx);
        }
        return deviceIp;
    }

    public static boolean isHomeWifiConnected(Context ctx) {
        ConnectivityManager connManager = (ConnectivityManager) HomeDroidApp.getContext().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            return isHomeWifi();
        }
        return false;
    }

    public static boolean isHomeWifi() {
        if (!PermissionUtil.hasLocationPermissions(HomeDroidApp.getContext())) {
            showLocationErrorToast();
            return false;
        }

        final WifiManager wifiManager = (WifiManager) HomeDroidApp.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        if (connectionInfo != null && !(connectionInfo.getSSID().equals(""))) {
            List<String> savedHomeWifis = PreferenceHelper.getHomeWifi(HomeDroidApp.getContext());

            if (savedHomeWifis == null || savedHomeWifis.isEmpty()) {
                return false;
            }

            for (String homeWifi : savedHomeWifis) {
                String trimmedHomeWifi = homeWifi.replace("\"", "");
                String trimmedSSID = connectionInfo.getSSID().replace("\"", "");
                if (trimmedSSID.equals(trimmedHomeWifi)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void showLocationErrorToast() {
        new Handler(HomeDroidApp.getContext().getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(HomeDroidApp.getContext(), R.string.location_permission_missing, Toast.LENGTH_LONG).show();
            }
        });
    }

    private static String getWifiIp() {
        WifiManager wifiManager = (WifiManager) HomeDroidApp.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String wifiIp = intToIp(wifiInfo.getIpAddress());

        if (wifiInfo.getIpAddress() == 0) {
            return null;
        }

        return wifiIp;
    }

    public static String getInetIp() {
        String ip = null;

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                        ip = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            // Log.e("getDeviceIp", ex.toString());
        }
        return ip;
    }

    public static String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
    }

    private static String getWifiOrInetIp(Context ctx) {
        ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (wifi.isAvailable()) {
            return getWifiIp();
        } else {
            return getInetIp();
        }
    }

}
