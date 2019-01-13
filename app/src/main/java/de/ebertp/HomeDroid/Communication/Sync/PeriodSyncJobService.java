package de.ebertp.HomeDroid.Communication.Sync;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.List;

import de.ebertp.HomeDroid.Communication.Rpc.RpcForegroundService;
import de.ebertp.HomeDroid.Connection.IpAdressHelper;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Utils.NotificationHelper;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import timber.log.Timber;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PeriodSyncJobService extends JobService {

    private static int JOB_ID = 1;

    public static void scheduleJob(Context context, int period) {
        //JobIntentService

        ComponentName serviceComponent = new ComponentName(context, PeriodSyncJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceComponent);
        builder.setMinimumLatency(period / 2); // wait at least
        builder.setOverrideDeadline(period * 2); // maximum delay
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require any network
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

    public static void cancelJob(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancelAll();
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        if (PreferenceHelper.isSyncOnHomeWifiOnly(this) && !IpAdressHelper.isHomeWifiConnected(this)) {
            Timber.d("Not in home wifi, going back to sleep");
            NotificationHelper.getNotificationHelperSingleton().removeNotification();
            stopService(new Intent(HomeDroidApp.getContext(), RpcForegroundService.class));
            PeriodSyncManager.next(this);
        } else {
            Timber.d("Queueing next period sync");
            SyncJobIntentService.enqueueSyncPeriod(HomeDroidApp.getContext());
        }

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    public static boolean isJobScheduled(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        List<JobInfo> jobs = jobScheduler.getAllPendingJobs();
        return jobs != null && !jobs.isEmpty();
    }
}
