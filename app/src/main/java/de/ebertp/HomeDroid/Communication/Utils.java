package de.ebertp.HomeDroid.Communication;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.ebertp.HomeDroid.Communication.Rpc.RpcForegroundService;
import de.ebertp.HomeDroid.Communication.Sync.PeriodSyncManager;
import de.ebertp.HomeDroid.Communication.Sync.SyncJobIntentService;
import de.ebertp.HomeDroid.Connection.BroadcastHelper;
import de.ebertp.HomeDroid.Connection.IpAdressHelper;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Location.GeoFencingManager;
import de.ebertp.HomeDroid.Utils.NotificationHelper;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Widget.StatusWidgetProvider;
import timber.log.Timber;

public class Utils {

    public static void resetPeriodUpdateTimer(Context context) {
        Timber.d("resetPeriodUpdateTimer()");
        if (!SyncJobIntentService.isSyncRunning()) {
            PeriodSyncManager.stop(context);
        }
        PeriodSyncManager.next(context);
        NotificationHelper.getNotificationHelperSingleton().setAppStateNotification();
    }

    public static void handleExit(Context context) {
        NotificationHelper.getNotificationHelperSingleton().removeNotification();
        PreferenceHelper.setIsClosed(context, true);
        StatusWidgetProvider.updateAllWidgets(context);
        context.stopService(new Intent(context, RpcForegroundService.class));
        GeoFencingManager.unregister();
        PeriodSyncManager.stop(context);
        BroadcastHelper.cancelSync();
    }
}
