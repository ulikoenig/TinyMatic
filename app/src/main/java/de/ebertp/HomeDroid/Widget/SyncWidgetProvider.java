package de.ebertp.HomeDroid.Widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import de.ebertp.HomeDroid.Communication.Sync.SyncJobIntentService;
import de.ebertp.HomeDroid.Communication.Sync.SyncSmallReceiver;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;

public class SyncWidgetProvider extends AppWidgetProvider {

    public static final String SYNC_STATE_CHANGED = " de.homedroid.DATABASE_CHANGED";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(SYNC_STATE_CHANGED)) {
            AppWidgetManager gm = AppWidgetManager.getInstance(context);
            int[] ids = gm.getAppWidgetIds(new ComponentName(context, SyncWidgetProvider.class));
            this.onUpdate(context, gm, ids);
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Get all ids
        ComponentName thisWidget = new ComponentName(context, SyncWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int widgetId : allWidgetIds) {

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_sync_layout);

            // Set the text
            if (SyncJobIntentService.isSyncRunning()) {
                remoteViews.setTextViewText(R.id.value, context.getString(R.string.sync_running));
            } else {
                String time = PreferenceHelper.getLastSuccessfulSync(context);
                remoteViews.setTextViewText(R.id.value, time);
            }

            if (PreferenceHelper.getSyncSuccessful(context)) {
                remoteViews.setImageViewResource(R.id.boolImage, R.drawable.btn_check_on_holo_dark_hm);
            } else {
                remoteViews.setImageViewResource(R.id.boolImage, R.drawable.btn_check_off_holo_dark_hm);
            }

            // Register an onClickListener
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, SyncSmallReceiver.class), 0);
            remoteViews.setOnClickPendingIntent(R.id.ll_as_button, pendingIntent);

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }
}
