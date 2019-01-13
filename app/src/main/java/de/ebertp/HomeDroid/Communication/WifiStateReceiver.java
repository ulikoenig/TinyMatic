package de.ebertp.HomeDroid.Communication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import de.ebertp.HomeDroid.Communication.Sync.PeriodSyncAlarmManager;
import de.ebertp.HomeDroid.Communication.Sync.PeriodSyncManager;
import de.ebertp.HomeDroid.Communication.Sync.SyncJobIntentService;
import de.ebertp.HomeDroid.Connection.IpAdressHelper;
import de.ebertp.HomeDroid.Utils.NotificationHelper;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import timber.log.Timber;

public class WifiStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Timber.d("onReceive() " + intent.getAction());

        if (PreferenceHelper.isClosed(context)) {
            return;
        }

        if (!PreferenceHelper.isSyncOnHomeWifiOnly(context)) {
            Timber.d("Home Wifi detection disabled");
            return;
        }

        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (info != null) {
            if (info.getState() == NetworkInfo.State.DISCONNECTED) {
                NotificationHelper.getNotificationHelperSingleton().removeNotification();

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    Timber.d("Home Wifi disconnected, stopping period sync");
                    PeriodSyncManager.stop(context);
                }
                return;
            }

            if (!info.isConnected()) {
                return;
            }

            if (!IpAdressHelper.isHomeWifi()) {
                return;
            }
            Timber.v("Home, Sweet Home");

            if (PeriodSyncManager.isPeriodSyncRunning()) {
                NotificationHelper.getNotificationHelperSingleton().setAppStateNotification();
                Timber.d("Period sync already running");
                return;
            }

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    SyncJobIntentService.enqueueSyncPeriod(context);
                }
            }, 1000);
        }
    }
}
