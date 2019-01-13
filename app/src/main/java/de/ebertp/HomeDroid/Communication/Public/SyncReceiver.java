package de.ebertp.HomeDroid.Communication.Public;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.ebertp.HomeDroid.Communication.Sync.SyncJobIntentService;

public class SyncReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent arg1) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SyncJobIntentService.enqueueSyncSmall(context);
            }
        }).start();

    }

}
