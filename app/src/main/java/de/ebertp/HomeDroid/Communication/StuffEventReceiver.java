package de.ebertp.HomeDroid.Communication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import de.ebertp.HomeDroid.Communication.Rpc.RpcForegroundService;
import de.ebertp.HomeDroid.Communication.Sync.PeriodSyncManager;
import de.ebertp.HomeDroid.Communication.Sync.SyncJobIntentService;
import de.ebertp.HomeDroid.Connection.BroadcastHelper;
import de.ebertp.HomeDroid.Connection.IpAdressHelper;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Utils.NotificationHelper;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.Util;
import de.ebertp.HomeDroid.Widget.StatusWidgetProvider;
import timber.log.Timber;

public class StuffEventReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("Received %s", intent.getAction());
        if (intent.getAction().equals(BroadcastHelper.REFRESH_UI)) {
            StatusWidgetProvider.updateAllWidgets(context);
        } else if (intent.getAction().equals(BroadcastHelper.SYNC_STARTED)) {
            handleSyncStarted();
        } else if (intent.getAction().equals(BroadcastHelper.SYNC_FINISHED)) {
            handleSyncFinished(context);
        } else {
            throw new IllegalArgumentException("Unknown Action " + intent.getAction());
        }
    }

    private void handleSyncStarted() {
        BroadcastHelper.refreshUI();
    }

    private void handleSyncFinished(Context context) {
        BroadcastHelper.refreshUI();

        if (PreferenceHelper.isClosed(context)) {
            Timber.d("App is closed, Period sync off");
            return;
        }

        if (SyncJobIntentService.isSyncRunning()) {
            Timber.d("Sync is running, Period sync off");
            return;
        }

        if (!PreferenceHelper.getPeriodicUpdatesEnabled(context)) {
            Timber.d("Periodic updates disabled, Period sync off");
            return;
        }

        if (HomeDroidApp.Instance().isAppInBackground() && PreferenceHelper.isSyncInForegroundOnly(context)) {
            Timber.d("No background sync, Period sync off");
            return;
        }

        if (!PreferenceHelper.getSyncSuccessful(context) && !PreferenceHelper.isSyncOnFailure(context)) {
            Timber.d("Sync failed, Period sync off");
            return;
        }

        //resetting timer
        int currentUpdateTimer = PeriodSyncManager.getPeriod();
        int updateTimerSettings = Util.getMinutesInMilis(PreferenceHelper
                .getPeriodicUpdateInterval(context));

        if (currentUpdateTimer != 0 && currentUpdateTimer != updateTimerSettings) {
            Timber.d("Resetting Timer  " + currentUpdateTimer + " -> " + updateTimerSettings);
            Utils.resetPeriodUpdateTimer(context);
        }

        if (PreferenceHelper.isSyncOnHomeWifiOnly(context) && !IpAdressHelper.isHomeWifiConnected(context)) {
            Timber.d("Not in Home Wifi, removing notification");
            NotificationHelper.getNotificationHelperSingleton().removeNotification();
            context.stopService(new Intent(HomeDroidApp.getContext(), RpcForegroundService.class));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Timber.d("Not in Home Wifi, check again later");
                PeriodSyncManager.next(context);
            } else {
                Timber.d("Not in Home Wifi, Period sync off");
            }
            return;
        }

        NotificationHelper.getNotificationHelperSingleton().setAppStateNotification();

        if (PreferenceHelper.isUpdateServiceEnabled(context)) {
            if (RpcForegroundService.isRunning()) {
                Timber.v("RPC Service already running");
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(new Intent(context, RpcForegroundService.class));
                } else {
                    context.startService(new Intent(context, RpcForegroundService.class));
                }
            }
        }

        PeriodSyncManager.next(context);
    }
}
