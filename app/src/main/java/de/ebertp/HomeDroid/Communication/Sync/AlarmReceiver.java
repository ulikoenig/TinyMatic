package de.ebertp.HomeDroid.Communication.Sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SyncJobIntentService.enqueueSyncPeriod(context);
    }
}
