package de.ebertp.HomeDroid.Connection;

import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import de.ebertp.HomeDroid.Communication.XmlToDbParser;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import timber.log.Timber;

public class InputStreamHelper {

    public static HttpURLConnection getUrlConnectionFromUrl(Context ctx, String urlString, boolean shortWait) throws IOException {
        Log.i(ctx.getClass().getSimpleName(), "Sending Request to " + urlString);

        URL url = new URL(urlString);
        HttpURLConnection urlConnection;

        if (PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("httpsOn", false)) {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            try {
                httpsURLConnection.setSSLSocketFactory(getSSLAuth(ctx));
            } catch (Exception e) {
                //meh.
                e.printStackTrace();
            }
            httpsURLConnection.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            urlConnection = httpsURLConnection;
        } else {
            urlConnection = (HttpURLConnection) url.openConnection();
        }

        urlConnection.setRequestMethod("GET");
        urlConnection.setConnectTimeout(5000);
        urlConnection.setReadTimeout(2000000);

        AuthHelper.setHttpAuth(ctx, urlConnection);

        try {
            urlConnection.getResponseCode();
        } catch (Exception e) {
            Timber.e(e);
            throw new SocketException(ctx.getString(R.string.connection_failed));
        }

        if (!(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK)) {
            int responseCode = urlConnection.getResponseCode();
            String msg = ctx.getString(R.string.error_code) + " " + responseCode + ": " + urlConnection.getResponseMessage();

            Log.e("HomeDroid", msg);

            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                msg = msg + "\n" + ctx.getString(R.string.installed_question);
            }

            if(responseCode == HttpURLConnection.HTTP_BAD_GATEWAY && PreferenceHelper.isMHEnabled(ctx)) {
                msg = msg + "\n" + ctx.getString(R.string.ccu_cloudmatic_erroc);
            }

            throw new IOException(msg);
        }

        return urlConnection;
    }

    public static InputStream getInputStream(HttpURLConnection urlConnection) throws IOException {
        return new BufferedInputStream(urlConnection.getInputStream(), XmlToDbParser.BUFFER_SIZE);
    }

    private static SSLSocketFactory getSSLAuth(Context ctx) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {

        TrustManager[] trustMangers;

        if(PreferenceHelper.isSslCheckEnabled(ctx)) {
            trustMangers = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).getTrustManagers();
        } else {
            X509TrustManager  easyTrustManager = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                    // Oh, I am easy!
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                    // Oh, I am easy!
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            // Create a trust manager that does not validate certificate chains
            trustMangers = new TrustManager[]{easyTrustManager};
        }

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("TLS");

        String pKeyFile = PreferenceHelper.getClientCertPath(ctx);
        String pKeyPassword = PreferenceHelper.getClientAuthPwd(ctx);

        if (PreferenceHelper.isClientAuthEnable(ctx) && pKeyFile != null) {
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance((KeyManagerFactory.getDefaultAlgorithm()));
            KeyStore keyStore = KeyStore.getInstance("PKCS12");

            File input = new File(Environment.getExternalStorageDirectory(), pKeyFile);
            InputStream keyInput = new FileInputStream(input);
            keyStore.load(keyInput, pKeyPassword.toCharArray());
            keyInput.close();

            keyManagerFactory.init(keyStore, null);

            sc.init(keyManagerFactory.getKeyManagers(), trustMangers, new SecureRandom());
        } else {
            sc.init(null, trustMangers, new java.security.SecureRandom());
        }
        return sc.getSocketFactory();
    }

}