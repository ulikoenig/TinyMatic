package de.ebertp.HomeDroid;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import de.ebertp.HomeDroid.Utils.SpeechUtil;


public class SpeechService extends Service{

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SpeechUtil.onSpeedDataReceived(this, intent);
        return Service.START_NOT_STICKY;
    }
}
