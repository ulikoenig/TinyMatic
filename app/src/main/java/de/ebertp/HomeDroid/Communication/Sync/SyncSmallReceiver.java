package de.ebertp.HomeDroid.Communication.Sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.ebertp.HomeDroid.Communication.Sync.SyncJobIntentService;
import timber.log.Timber;

public class SyncSmallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("onReceive()");
        SyncJobIntentService.enqueueSyncSmall(context);
    }
}
