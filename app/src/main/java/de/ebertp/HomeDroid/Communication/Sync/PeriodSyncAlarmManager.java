package de.ebertp.HomeDroid.Communication.Sync;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

import de.ebertp.HomeDroid.Communication.Sync.SyncJobIntentService;
import de.ebertp.HomeDroid.Connection.BroadcastHelper;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import timber.log.Timber;

/* Legacy for API level < 23*/
@Deprecated
public class PeriodSyncAlarmManager {

    private static PendingIntent pendingIntent;

    public static void start(Context context, int period) {
        Timber.d("start()");
        android.app.AlarmManager alarmMgr = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, AlarmReceiver.class), 0);
        alarmMgr.set(android.app.AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + period, pendingIntent);
    }

    public static void stop(Context context) {
        Timber.d("stop()");
        if (pendingIntent != null) {
            android.app.AlarmManager alarmMgr = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmMgr.cancel(pendingIntent);
            pendingIntent = null;
        }
    }

    public static boolean hasPendingAlarm() {
        return pendingIntent != null;
    }
}
