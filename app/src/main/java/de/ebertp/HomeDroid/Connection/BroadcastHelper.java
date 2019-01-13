package de.ebertp.HomeDroid.Connection;

import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import de.ebertp.HomeDroid.Communication.DbRefreshManager;
import de.ebertp.HomeDroid.HomeDroidApp;

public class BroadcastHelper {

    public static final String REFRESH_UI = "de.ebertp.HomeDroid.Broadcast.RefreshUI";
    public static final String SYNC_STARTED = "de.ebertp.HomeDroid.Broadcast.SyncStarted";
    public static final String SYNC_FINISHED = "de.ebertp.HomeDroid.Broadcast.SyncFinished";
    public static final String SYNC_CANCEL = "de.ebertp.HomeDroid.Broadcast.SyncCancel";
    public static final String SYNC_UPDATE_PROGRESS = "de.ebertp.HomeDroid.Broadcast.SyncUpdateProgress";

    private static void sendBroadcast(Intent intent) {
        LocalBroadcastManager.getInstance(HomeDroidApp.getContext()).sendBroadcast(intent);
    }

    public static void syncStarted() {
        sendBroadcast(new Intent(SYNC_STARTED));
    }

    public static void syncFinished() {
        sendBroadcast(new Intent(SYNC_FINISHED));
    }

    public static void cancelSync() {
        sendBroadcast(new Intent(SYNC_CANCEL));
    }

    public static void refreshUI() {
        sendBroadcast(new Intent(REFRESH_UI));
    }

    public static void sendSyncUpdateProgress(int progress) {
        Intent intent = new Intent(SYNC_UPDATE_PROGRESS);
        intent.putExtra(DbRefreshManager.EXTRA_SYNC_RES, progress);
        sendBroadcast(intent);
    }
}
