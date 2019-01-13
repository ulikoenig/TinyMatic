package de.ebertp.HomeDroid.Widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.RemoteViews;

import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.SpeechService;
import de.ebertp.HomeDroid.Utils.SpeechUtil;

public class SpeechWidgetProvider extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Get all ids
        ComponentName thisWidget = new ComponentName(context, SpeechWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int widgetId : allWidgetIds) {

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_speech_layout);

            // this intent points to activity that should handle results
            Intent resultIntent = new Intent(context, SpeechService.class);
            // this intent wraps results activity intent
            PendingIntent resultsPendingIntent = PendingIntent.getService(context, 0, resultIntent, 0);

            // this intent calls the speech recognition
            Intent voiceIntent = SpeechUtil.getSpeechRecognitionIntent();
            voiceIntent.putExtra(RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT, resultsPendingIntent);

            // this intent wraps voice recognition intent
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, voiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            //PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, voiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.btn_speech, pendingIntent);

            appWidgetManager.updateAppWidget(widgetId, remoteViews);


        }
    }
}
