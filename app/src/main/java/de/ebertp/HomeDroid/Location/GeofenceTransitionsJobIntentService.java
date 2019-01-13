/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.ebertp.HomeDroid.Location;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import android.text.TextUtils;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import de.ebertp.HomeDroid.Communication.Control.ControlHelper;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import timber.log.Timber;

public class GeofenceTransitionsJobIntentService extends JobIntentService {

    private static final int JOB_ID = 573;

    public static void enqueueWork(Context context, Intent intent) {
        Timber.d("enqueueWork()");
        enqueueWork(context, GeofenceTransitionsJobIntentService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(Intent intent) {
        Timber.w("onHandleWork()");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Timber.e(errorMessage);
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
//            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
//            String geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition,
//                    triggeringGeofences);
//
//            Timber.i(geofenceTransitionDetails);
//            sendNotification(geofenceTransitionDetails);

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                handleArrival();
            } else {
                handleDeparture();
            }
        } else {
            // Log the error.
            Timber.e("Invalid type %s", geofenceTransition);
        }
    }

    private void handleArrival() {
        Timber.d("handleArrival()");
        int progId = PreferenceHelper.getStartOnArrival(HomeDroidApp.getContext());
        if (progId != 0) {
            ControlHelper.runProgram(HomeDroidApp.getContext(), progId, null);
        }
    }

    private void handleDeparture() {
        Timber.d("handleDeparture()");
        int progId = PreferenceHelper.getStartOnDeparture(HomeDroidApp.getContext());
        if (progId != 0) {
            ControlHelper.runProgram(HomeDroidApp.getContext(), progId, null);
        }
    }

    private String getGeofenceTransitionDetails(
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "GEOFENCE_TRANSITION_ENTER";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "GEOFENCE_TRANSITION_EXIT";
            default:
                return "UNKNOWN TRANSITION " + transitionType;
        }
    }

    private void sendNotification(String notificationDetails) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel =
                    new NotificationChannel("geo", "Geofencing", NotificationManager.IMPORTANCE_DEFAULT);

            mNotificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setSmallIcon(R.drawable.icon1)
                .setColor(Color.RED)
                .setContentTitle(notificationDetails);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("geo"); // Channel ID
        }

        builder.setAutoCancel(true);

        mNotificationManager.cancel(1);
        mNotificationManager.notify(1, builder.build());
    }
}
