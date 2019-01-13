package de.ebertp.HomeDroid.Communication.Public;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.ebertp.HomeDroid.Communication.Utils;

public class ExitReceiver extends BroadcastReceiver {

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
                Utils.handleExit(context);
            }
        }).start();

    }

}
