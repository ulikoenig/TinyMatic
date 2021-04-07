package de.ebertp.HomeDroid;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

import de.ebertp.HomeDroid.Communication.StuffEventReceiver;
import de.ebertp.HomeDroid.Communication.Sync.PeriodSyncManager;
import de.ebertp.HomeDroid.Communication.WifiStateReceiver;
import de.ebertp.HomeDroid.Connection.BroadcastHelper;
import de.ebertp.HomeDroid.DbAdapter.DataBaseAdapterManager;
import de.ebertp.HomeDroid.Location.GeoFencingManager;
import de.ebertp.HomeDroid.Utils.NotificationHelper;
import de.ebertp.HomeDroid.Utils.PermissionUtil;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Widget.StatusWidgetProvider;
import timber.log.Timber;


public class HomeDroidApp extends Application {

    private static HomeDroidApp sInstance;

    private DataBaseAdapterManager mDataBaseAdapterManager;
    private boolean mAppInBackground = true;

    private StuffEventReceiver mStuffReceiver;
    private WifiStateReceiver mWifiStateReceiver;

    public static DataBaseAdapterManager db() {
        return Instance().mDataBaseAdapterManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        Timber.plant(new Timber.DebugTree());
        Timber.d("onCreate()");

        PreferenceHelper.incrementLaunchCount(this);

        initDbHelper();
        initEventReceiver();
        initBackgroundDetection();
        GeoFencingManager.init();
        registerWifiReceiver();

        if (BuildConfig.DEBUG) {
            Stetho.initialize(
                    Stetho.newInitializerBuilder(this)
                            .enableDumpapp(
                                    Stetho.defaultDumperPluginsProvider(this))
                            .enableWebKitInspector(
                                    Stetho.defaultInspectorModulesProvider(this))
                            .build());
        }

        StatusWidgetProvider.updateAllWidgets(this);
    }


    public void registerWifiReceiver() {
        Timber.d("registerWifiReceiver()");
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        if (!PreferenceHelper.isSyncOnHomeWifiOnly(this)) {
            return;
        }

        if (!PermissionUtil.hasLocationPermissions(HomeDroidApp.getContext())) {
            Toast.makeText(HomeDroidApp.getContext(), R.string.location_permission_missing, Toast.LENGTH_LONG).show();
            return;
        }

        // This will not work once the app gets destroyed, but it might help to remove the notification
        mWifiStateReceiver = new WifiStateReceiver();
        registerReceiver(mWifiStateReceiver, new IntentFilter("android.net.wifi.STATE_CHANGE"));
        registerReceiver(mWifiStateReceiver, new IntentFilter("android.net.wifi.supplicant.CONNECTION_CHANGE"));
    }

    public void unregisterWifiReceiver() {
        try {
            unregisterReceiver(mWifiStateReceiver);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void initEventReceiver() {
        mStuffReceiver = new StuffEventReceiver();
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(mStuffReceiver, new IntentFilter(BroadcastHelper.REFRESH_UI));
        broadcastManager.registerReceiver(mStuffReceiver, new IntentFilter(BroadcastHelper.SYNC_STARTED));
        broadcastManager.registerReceiver(mStuffReceiver, new IntentFilter(BroadcastHelper.SYNC_FINISHED));
    }

    public void initDbHelper() {
        if (mDataBaseAdapterManager != null) {
            DataBaseAdapterManager.releaseHelper();
        }
        mDataBaseAdapterManager = DataBaseAdapterManager.init(getApplicationContext());
    }

    private void initBackgroundDetection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            registerComponentCallbacks(new ComponentCallbacks2() {
                @Override
                public void onTrimMemory(int level) {
                    if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
                        Timber.d("App in background");
                        mAppInBackground = true;
                        if (PreferenceHelper.isSyncInForegroundOnly(HomeDroidApp.this)) {
                            PeriodSyncManager.stop(HomeDroidApp.this);
                            NotificationHelper.getNotificationHelperSingleton().removeNotification();
                        }
                    }
                }

                @Override
                public void onConfigurationChanged(Configuration configuration) {
                }

                @Override
                public void onLowMemory() {
                }
            });
        }
    }

    public static Context getContext() {
        return sInstance;
    }

    public static HomeDroidApp Instance() {
        return sInstance;
    }

    public boolean isAppInBackground() {
        return mAppInBackground;
    }

    public void setAppInBackground(boolean isAppInBackground) {
        mAppInBackground = isAppInBackground;
    }
}
