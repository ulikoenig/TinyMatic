package de.ebertp.HomeDroid.Location;


import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collections;

import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Utils.PermissionUtil;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import timber.log.Timber;

public class GeoFencingManager {

    private static String KEY_HOME = "HOME";
    private static float GEOFENCE_RADIUS_IN_METERS = 150;

    private static PendingIntent mGeofencePendingIntent;

    public static void init() {
        Timber.v("init()");

        if (!PreferenceHelper.isLocationEnabled(HomeDroidApp.getContext())) {
            Timber.v("Location disabled");
            return;
        }

        if (!PermissionUtil.hasLocationPermissions(HomeDroidApp.getContext())) {
            Toast.makeText(HomeDroidApp.getContext(), "Location permission is missing - Please set home location again", Toast.LENGTH_LONG).show();
            return;
        }

        //unregister();

        String lngString = PreferenceHelper.getLongitude(HomeDroidApp.getContext());
        String latString = PreferenceHelper.getLatitude(HomeDroidApp.getContext());
        Timber.v("Setting new Home " + latString + "/" + lngString);

        if (lngString == null || latString == null) {
            Timber.d("Home coordinates data missing");
            return;
        }

        register(Double.valueOf(latString), Double.valueOf(lngString));
    }

    @SuppressLint("MissingPermission")
    public static void register(double lat, double lng) {
        Timber.v("register()");

        ArrayList<Geofence> geofenceList = new ArrayList<>();

        geofenceList.add(new Geofence.Builder()
                .setRequestId(KEY_HOME)
                .setCircularRegion(
                        lat,
                        lng,
                        GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);

        GeofencingRequest request = builder.build();
        GeofencingClient geofencingClient = LocationServices.getGeofencingClient(HomeDroidApp.getContext());
        Task<Void> task = geofencingClient.addGeofences(request, getGeofencePendingIntent());

        task.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Timber.d("onComplete()");
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Timber.w("onFailure()");
            }
        });
    }

    public static void unregister() {
        Timber.v("unregister()");
        GeofencingClient geofencingClient = LocationServices.getGeofencingClient(HomeDroidApp.getContext());
        geofencingClient.removeGeofences(Collections.singletonList(KEY_HOME));
    }

    private static PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(HomeDroidApp.getContext(), GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getBroadcast(HomeDroidApp.getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }
}
