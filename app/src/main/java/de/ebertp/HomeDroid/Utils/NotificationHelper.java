package de.ebertp.HomeDroid.Utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;

import java.text.SimpleDateFormat;
import java.util.Locale;

import de.ebertp.HomeDroid.Activities.Drawer.MainActivity;
import de.ebertp.HomeDroid.Communication.Sync.SyncJobIntentService;
import de.ebertp.HomeDroid.Communication.Sync.SyncSmallReceiver;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.R;
import timber.log.Timber;

public class NotificationHelper {

    public static final String NOTIFICATION_CHANNEL = "default";
    private NotificationManager mNotificationManager;
    private Context mContext;
    private SimpleDateFormat simpleDateFormat;
    private Intent notificationIntent;
    private Builder notificationBuilder;

    private static NotificationHelper notificationHelperSingleton;

    protected NotificationHelper(Context ctx) {
        mContext = ctx.getApplicationContext();
        simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        initChannel(mNotificationManager);
        notificationBuilder = new NotificationCompat.Builder(ctx, NOTIFICATION_CHANNEL);
    }

    public static NotificationHelper getNotificationHelperSingleton() {
        if (notificationHelperSingleton == null) {
            notificationHelperSingleton = new NotificationHelper(HomeDroidApp.getContext());
            return notificationHelperSingleton;
        } else
            return notificationHelperSingleton;
    }

    public static void initChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL) != null) {
            Timber.d("Notification channel already created");
            return;
        }

        NotificationChannel channel = new NotificationChannel("default",
                "TinyMatic",
                NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("TinyMatic");
        notificationManager.createNotificationChannel(channel);
    }

    public void setIntent(Intent intent) {
        this.notificationIntent = intent;
    }

    boolean isFirstTime = true;

    public synchronized void setNotification(CharSequence contentTitle, CharSequence contentText) {
        if (PreferenceHelper.isNotificationsEnabled(mContext)) {

            if (isFirstTime) {
                notificationBuilder.setOngoing(PreferenceHelper.getPeriodicUpdatesEnabled(mContext)).setOnlyAlertOnce(true)
                        .setTicker(contentTitle).setWhen(System.currentTimeMillis());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    notificationBuilder.setSmallIcon(R.drawable.system_icon).setColor(mContext.getResources().getColor(R.color.homedroid_primary));
                } else {
                    notificationBuilder.setSmallIcon(R.mipmap.icon);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    notificationBuilder.setPriority(Notification.PRIORITY_LOW);
                }

                isFirstTime = false;

                PendingIntent refreshIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(mContext, SyncSmallReceiver.class), 0);
                notificationBuilder.addAction(android.R.drawable.ic_menu_rotate, mContext.getResources().getString(R.string.refresh_db),
                        refreshIntent);
            }

            if (notificationIntent == null) {
                notificationIntent = new Intent(mContext, MainActivity.class);
            }

            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);

            notificationBuilder.setContentTitle(contentTitle).setContentText(contentText).setContentIntent(contentIntent)
                    .setWhen(System.currentTimeMillis());

            mNotificationManager.notify(0, notificationBuilder.build());
        }
    }

    public synchronized void removeNotification() {
        mNotificationManager.cancel(0);
    }

    public synchronized void setAppStateNotification() {
        String contentText = "";
        String contentTitle;

        if (SyncJobIntentService.isSyncRunning()) {
            contentTitle = mContext.getString(R.string.db_refresh);
        } else {

            if (PreferenceHelper.getSyncSuccessful(mContext)) {
//                if (PreferenceHelper.isUpdateServiceEnabled(mContext)) {
//                    if (rpcStatus) {
//                        contentText = mContext.getString(R.string.update_service_enabled);
//                    } else {
//                        contentText = mContext.getString(R.string.update_service_disabled);
//                    }
//                } else if (PreferenceHelper.getPeriodicUpdatesEnabled(mContext)) {
//                    long period = Util.getMinutesInMilis(PreferenceHelper.getPeriodicUpdateInterval(mContext));
//                    contentText = mContext.getString(R.string.next_update) + " "
//                            + simpleDateFormat.format(System.currentTimeMillis() + period + Util.getSecondsToNextMinute());
//                } else {
//                    contentText = mContext.getString(R.string.lastSync) + " " + PreferenceHelper.getLastSuccessfulSync(mContext);
//                }
                contentTitle = mContext.getString(R.string.refresh_successful);

            } else {
                contentTitle = mContext.getString(R.string.refresh_failed);
                contentText = mContext.getString(R.string.lastSync) + " " + PreferenceHelper.getLastSuccessfulSync(mContext);
            }

        }

        setNotification(contentTitle, contentText);
    }

}
