//package de.ebertp.HomeDroid.Communication;
//
//import android.app.Service;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Binder;
//import android.os.IBinder;
//import android.support.annotation.Nullable;
//import android.support.v4.content.LocalBroadcastManager;
//import android.widget.Toast;
//
//import de.ebertp.HomeDroid.Communication.Sync.SyncJobIntentService;
//import de.ebertp.HomeDroid.Connection.BroadcastHelper;
//import de.ebertp.HomeDroid.Connection.InitHelper;
//import de.ebertp.HomeDroid.Location.HDLocationManager;
//import de.ebertp.HomeDroid.Utils.NotificationHelper;
//import de.ebertp.HomeDroid.Utils.PermissionUtil;
//import de.ebertp.HomeDroid.Utils.PreferenceHelper;
//import de.ebertp.HomeDroid.Widget.StatusWidgetProvider;
//import timber.log.Timber;
//
//public class LocationBackgroundService extends Service {
//
//    private HDLocationManager mHdLocationManager;
//
//    @Override
//    public void onCreate() {
//        Timber.d("onCreate()");
//        super.onCreate();
//
//        if (PreferenceHelper.isLocationEnabled(this)) {
//            if (!PermissionUtil.hasLocationPermissions(this)) {
//                Toast.makeText(this, "Location permission is missing - Please set home location again", Toast.LENGTH_LONG).show();
//            } else {
//                mHdLocationManager = new HDLocationManager(this);
//                mHdLocationManager.start();
//            }
//        }
//    }
//
//    @Override
//    public void onDestroy() {
//        Timber.d("onDestroy()");
//        if (mHdLocationManager != null) {
//            mHdLocationManager.stop();
//        }
//
//        super.onDestroy();
//    }
//
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//
//}
