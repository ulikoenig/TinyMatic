package de.ebertp.HomeDroid.Communication.Sync;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.widget.Toast;

import de.ebertp.HomeDroid.Communication.DbRefreshManager;
import de.ebertp.HomeDroid.Connection.BroadcastHelper;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.NotificationHelper;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.ToastHandler;
import timber.log.Timber;

public class SyncJobIntentService extends JobIntentService {

    public static final String SYNC_SMALL = "SYNC_SMALL";
    public static final String SYNC_PERIOD = "SYNC_PERIOD";
    public static final String SYNC_ALL = "SYNC_ALL";

    private Context ctx;
    private ToastHandler toastHandler;
    private DbRefreshManager dbRefreshManager;
    private BroadcastReceiver broadcastReceiver;

    private static boolean isJobRunning;

    public static void enqueueSyncAll(Context context) {
        enqueueWork(context, new Intent(SyncJobIntentService.SYNC_ALL));
    }

    public static void enqueueSyncSmall(Context context) {
        if (isJobRunning) {
            Toast.makeText(context, context.getString(R.string.wait_for_refresh), Toast.LENGTH_LONG).show();
        } else {
            enqueueWork(context, new Intent(SyncJobIntentService.SYNC_SMALL));
        }
    }

    public static void enqueueSyncPeriod(Context context) {
        enqueueWork(context, new Intent(SyncJobIntentService.SYNC_PERIOD));
    }


    static void enqueueWork(Context context, Intent work) {
        Timber.d("enqueueWork()%s", work.getAction());
        enqueueWork(context, SyncJobIntentService.class, 1234, work);
    }

    public static boolean isSyncRunning() {
        return isJobRunning;
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Timber.d("onHandleWork() %s", intent.getAction());
        isJobRunning = true;

        NotificationHelper.getNotificationHelperSingleton().setAppStateNotification();
        PreferenceHelper.setSyncSuccessful(this, false);
        BroadcastHelper.syncStarted();

        switch (intent.getAction()) {
            case SYNC_SMALL:
                syncSmall();
                break;
            case SYNC_PERIOD:
                syncPeriod();
                break;
            case SYNC_ALL:
                syncAll();
                break;
            default:
                throw new IllegalArgumentException("Unknown action" + intent.getAction());
        }

        if (dbRefreshManager.isRefreshSuccessful()) {
            PreferenceHelper.setSyncSuccessful(this, true);
        }

        if (!PreferenceHelper.getSyncSuccessful(this)) {
            PreferenceHelper.setWriteCount(this, 0);
        }

        isJobRunning = false;
        NotificationHelper.getNotificationHelperSingleton().setAppStateNotification();
        BroadcastHelper.syncFinished();
        Timber.d("onHandleWork() done");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("onCreate()");
        isJobRunning = true;

        ctx = this;
        toastHandler = new ToastHandler(this);
        dbRefreshManager = new DbRefreshManager(ctx.getApplicationContext(), toastHandler);

        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Timber.d("Received %s", intent.getAction());

                if (intent.getAction().equals(BroadcastHelper.SYNC_CANCEL)) {
                    cancelSync();
                } else {
                    throw new IllegalArgumentException("Unknown Action " + intent.getAction());
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(BroadcastHelper.SYNC_CANCEL));
    }

    @Override
    public void onDestroy() {
        Timber.d("onDestroy");
        isJobRunning = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    public void syncPeriod() {
        Timber.i("Periodic refresh with interval %s", PeriodSyncManager.getPeriod());
        try {
            dbRefreshManager.refreshPeriod();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void syncSmall() {
        if (HomeDroidApp.db().roomsDbAdapter.isEmpty(PreferenceHelper.getPrefix(this))) {
            Timber.i("Database for this profile is empty, do a full refresh");
            syncAll();
        } else {
            PreferenceHelper.setIsClosed(ctx, false);
            try {
                dbRefreshManager.refreshSmall();
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    public void syncAll() {
        try {
            dbRefreshManager.refreshAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cancelSync() {
        Timber.i("cancelSync()");
        dbRefreshManager.cancelSyncHard();
    }
}
