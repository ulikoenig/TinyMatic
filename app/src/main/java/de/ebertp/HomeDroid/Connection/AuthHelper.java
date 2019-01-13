package de.ebertp.HomeDroid.Connection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Base64;

import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;

import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.timroes.axmlrpc.XMLRPCClient;

public class AuthHelper {

    public static void setHttpAuth(Context ctx) {
        if(PreferenceHelper.isLocalWifiDetectionOn(ctx) && IpAdressHelper.isHomeWifiConnected(ctx)) {
            Authenticator.setDefault(null);
        }

        if (PreferenceHelper.isMHEnabled(ctx)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

            final String user = prefs.getString("mhUser", "user");
            final String pass = prefs.getString("mhPass", "pass");

            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pass.toCharArray());
                }
            });

        } else if (PreferenceHelper.isHttpAuthUsed(ctx)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

            final String user = prefs.getString("httpAuthUser", "user");
            final String pass = prefs.getString("httpAuthPw", "pass");

            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pass.toCharArray());
                }
            });
        } else {
            Authenticator.setDefault(null);
        }
    }

    @SuppressLint("InlinedApi")
    public static void setHttpAuth(Context ctx, HttpURLConnection urlConnection) {
        if (PreferenceHelper.isMHEnabled(ctx)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

            final String user = prefs.getString("mhUser", "user");
            final String pass = prefs.getString("mhPass", "pass");

            String userpass = user + ":" + pass;
            String basicAuth;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                basicAuth = "Basic " + new String(Base64.encodeToString(userpass.getBytes(), Base64.NO_WRAP));
            } else {
                basicAuth = "Basic " + new String(de.ebertp.HomeDroid.Connection.Base64.encodeBytes(userpass.getBytes()));
            }
            urlConnection.setRequestProperty("Authorization", basicAuth);
        } else if (PreferenceHelper.isHttpAuthUsed(ctx)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

            final String user = prefs.getString("httpAuthUser", "user");
            final String pass = prefs.getString("httpAuthPw", "pass");

            String userpass = user + ":" + pass;
            String basicAuth;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                basicAuth = "Basic " + new String(Base64.encodeToString(userpass.getBytes(), Base64.NO_WRAP));
            } else {
                basicAuth = "Basic " + new String(de.ebertp.HomeDroid.Connection.Base64.encodeBytes(userpass.getBytes()));
            }

            urlConnection.setRequestProperty("Authorization", basicAuth);
            urlConnection.setUseCaches(false);
        }
    }

    public static void setHttpAuth(Context ctx, XMLRPCClient client) {
        if (PreferenceHelper.isMHEnabled(ctx)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            String user = prefs.getString("mhUser", "user");
            String pass = prefs.getString("mhPass", "pass");
            client.setLoginData(user, pass);
        } else if (PreferenceHelper.isHttpAuthUsed(ctx)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

            final String user = prefs.getString("httpAuthUser", "user");
            final String pass = prefs.getString("httpAuthPw", "pass");

            client.setLoginData(user, pass);
        }
    }
}
