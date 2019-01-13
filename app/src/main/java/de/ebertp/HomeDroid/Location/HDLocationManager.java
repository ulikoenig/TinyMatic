//package de.ebertp.HomeDroid.Location;
//
//import android.content.Context;
//import android.location.Criteria;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.os.Bundle;
//import android.util.Log;
//
//import de.ebertp.HomeDroid.Communication.Control.ControlHelper;
//import de.ebertp.HomeDroid.Utils.PreferenceHelper;
//
//public class HDLocationManager implements LocationListener {
//
//    private static final long DELAY_BEFORE_CHANGE_VALID = 2 * 60 * 1000;
//    private static final long MIN_UPDATE_INTERVAL = 20000;
//    private static final int DEFAULT_PROXIMITY = 500;
//
//    private LocationManager locationManager;
//    private String bestProvider;
//
//    private Location home;
//    private Location currentLocation;
//    private Context ctx;
//
//    private boolean deviceIsHome = false;
//    private boolean timerIsRunning = false;
//
//
//    public HDLocationManager(Context ctx) {
//        this.ctx = ctx;
//        setNewHome();
//    }
//
//    public int getProximity() {
//        try {
//            return Integer.parseInt(PreferenceHelper.getHomeProximity(ctx));
//        } catch (NumberFormatException e) {
//            e.printStackTrace();
//            PreferenceHelper.setHomeProximity(ctx, Integer.toString(DEFAULT_PROXIMITY));
//            return DEFAULT_PROXIMITY;
//        }
//
//    }
//
//    public void setNewHome() {
//        String longitudeString = PreferenceHelper.getLongitude(ctx);
//        String latitudeString = PreferenceHelper.getLatitude(ctx);
//
//        Log.v(getClass().getName(), "Setting new Home" + longitudeString + "/" + latitudeString);
//
//        if (longitudeString != null && latitudeString != null) {
//            locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
//            home = new Location("");
//            home.setLongitude(Double.valueOf(longitudeString));
//            home.setLatitude(Double.valueOf(latitudeString));
//
//            Criteria criteria = new Criteria();
//            bestProvider = locationManager.getBestProvider(criteria, false);
//            currentLocation = locationManager.getLastKnownLocation(bestProvider);
//        }
//    }
//
//    public void start() {
//        if (locationManager != null) {
//            locationManager.requestLocationUpdates(bestProvider, MIN_UPDATE_INTERVAL, 1, this);
//        }
//    }
//
//    public void stop() {
//        if (locationManager != null) {
//            locationManager.removeUpdates(this);
//        }
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//        if (home != null) {
//            currentLocation = location;
//            if (isHome(currentLocation) && !deviceIsHome) {
//                Log.v(getClass().getName(), "Device may be arriving.");
//                startWaiting();
//            } else if (!isHome(currentLocation) && deviceIsHome) {
//                Log.v(getClass().getName(), "Device may be departing!");
//                startWaiting();
//            }
//        }
//    }
//
//    private synchronized void startWaiting() {
//        if (!timerIsRunning) {
//            new Thread(new Runnable() {
//                public void run() {
//                    timerIsRunning = true;
//
//                    try {
//                        Thread.sleep(DELAY_BEFORE_CHANGE_VALID);
//
//                        if (isHome(currentLocation) && deviceIsHome == false) {
//                            handleArrival();
//                        } else if (!isHome(currentLocation) && deviceIsHome == true) {
//                            handleDeparture();
//                        }
//
//                        timerIsRunning = false;
//
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//        } else {
//            Log.v(getClass().getName(), "Task is already running");
//        }
//    }
//
//    private void handleDeparture() {
//        //should be false
//        deviceIsHome = false;
//
//        int progId = PreferenceHelper.getStartOnDeparture(ctx);
//        if (progId != 0) {
//            ControlHelper.runProgram(ctx, progId, null);
//        }
//    }
//
//    private void handleArrival() {
//        //should be true
//        deviceIsHome = true;
//
//        int progId = PreferenceHelper.getStartOnArrival(ctx);
//        if (progId != 0) {
//            ControlHelper.runProgram(ctx, progId, null);
//        }
//    }
//
//
//    private boolean isHome(Location location) {
//        float distanceInM = location.distanceTo(home);
//        if (distanceInM < getProximity()) {
////				System.out.println("HDLocationManager.isHome()"+getProximity());
//            return true;
//        }
//        return false;
//
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//        // let okProvider be bestProvider
//        // re-register for updates
////			Log.v(TAG, "Provider Disabled: " + provider);
////			output="\n\nProvider Disabled: " + provider);
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//        // is provider better than bestProvider?
//        // is yes, bestProvider = provider
////			Log.v(TAG, "Provider Enabled: " + provider);
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
////			Log.v(TAG, "Provider Status Changed: " + provider + ", Status="
////					+ S[status] + ", Extras=" + extras);
//    }
//
////		private void printProvider(String provider) {
////			LocationProvider info = locationManager.getProvider(provider);
//////			Log.v(TAG, info.toString());
////		}
//}
