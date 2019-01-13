//package de.ebertp.HomeDroid.Communication.Wifi;
//
//import android.annotation.TargetApi;
//import android.app.job.JobInfo;
//import android.app.job.JobParameters;
//import android.app.job.JobScheduler;
//import android.app.job.JobService;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.IntentFilter;
//import android.os.Build;
//import android.provider.SyncStateContract;
//import android.widget.Toast;
//
//import de.ebertp.HomeDroid.Communication.WifiStateReceiver;
//import de.ebertp.HomeDroid.Utils.PreferenceHelper;
//import timber.log.Timber;
//
//@TargetApi(Build.VERSION_CODES.O)
//public class WifiJobService extends JobService {
//
//    private WifiStateReceiver mReceiver;
//
//    public static void start(Context context) {
//        Timber.d("start()");
//        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
//            return;
//        }
//
//        if (!PreferenceHelper.isSyncOnHomeWifiOnly(context)) {
//            Timber.d("Home wifi sync disabled");
//            return;
//        }
//        Timber.d("Enabling home wifi detection");
//
//        JobInfo jobInfo = new JobInfo.Builder(0, new ComponentName(context, WifiJobService.class))
//                .setMinimumLatency(1000)
//                .setOverrideDeadline(2000)
//                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
//                .build();
//
//        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
//        jobScheduler.schedule(jobInfo);
//    }
//
//    public static void stop(Context context) {
//        Timber.d("stop()");
//        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
//            return;
//        }
//
//        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
//        jobScheduler.cancelAll();
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        Timber.d("onCreate()");
//        Toast.makeText(this, "Wifi Job created", Toast.LENGTH_SHORT).show();
//        mReceiver = new WifiStateReceiver();
//    }
//
//    @Override
//    public boolean onStartJob(JobParameters jobParameters) {
//        Timber.d("onStartJob()");
//        Toast.makeText(this, "Wifi Job started", Toast.LENGTH_SHORT).show();
//        registerReceiver(mReceiver, new IntentFilter("android.net.wifi.STATE_CHANGE"));
//        registerReceiver(mReceiver, new IntentFilter("android.net.wifi.supplicant.CONNECTION_CHANGE"));
//        return true;
//    }
//
//    @Override
//    public boolean onStopJob(JobParameters jobParameters) {
//        Toast.makeText(this, "Wifi Job stopped", Toast.LENGTH_SHORT).show();
//        Timber.d("onStopJob()");
//        unregisterReceiver(mReceiver);
//        return true;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        Timber.d("onDestroy()");
//        Toast.makeText(this, "Wifi Job destroyed", Toast.LENGTH_SHORT).show();
//    }
//}
