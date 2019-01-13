package de.ebertp.HomeDroid.Communication.Rpc;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;

import java.net.URL;

import de.ebertp.HomeDroid.Activities.Drawer.MainActivity;
import de.ebertp.HomeDroid.Connection.AuthHelper;
import de.ebertp.HomeDroid.Connection.BroadcastHelper;
import de.ebertp.HomeDroid.Connection.IpAdressHelper;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.NotificationHelper;
import de.timroes.axmlrpc.XMLRPCClient;
import timber.log.Timber;

import static de.timroes.axmlrpc.XMLRPCClient.FLAGS_DEFAULT_TYPE_STRING;

public class RpcForegroundService extends Service {

    private static boolean mIsRunning;

    private RpcPushListener rpcPushListener;
    private boolean mIsSubscribed;

    public static boolean isRunning() {
        return mIsRunning;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Timber.d("onCreate()");
        super.onCreate();
        mIsRunning = true;

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(receiver, new IntentFilter(BroadcastHelper.SYNC_STARTED));
        broadcastManager.registerReceiver(receiver, new IntentFilter(BroadcastHelper.SYNC_FINISHED));

        rpcPushListener = new RpcPushListener(this);
        rpcPushListener.start();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationHelper.initChannel(notificationManager);

        setNotification(R.string.update_service_started);
        subscribe();
    }

    private void setNotification(int stringRes) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationHelper.NOTIFICATION_CHANNEL)
                .setContentTitle(getText(R.string.update_service))
                .setContentText(getText(stringRes));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.system_icon).setColor(getResources().getColor(R.color.homedroid_primary));
        } else {
            builder.setSmallIcon(R.mipmap.icon);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setPriority(Notification.PRIORITY_LOW);
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        builder.setContentIntent(contentIntent);

        startForeground(2, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy()");
        mIsRunning = false;

        unsubscribe();

        try {
            if (receiver != null) {
                unregisterReceiver(receiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        rpcPushListener.stop();
        stopSelf();
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastHelper.SYNC_STARTED)) {
                rpcPushListener.pause();
            } else if (intent.getAction().equals(BroadcastHelper.SYNC_FINISHED)) {
                rpcPushListener.resume();

                if (!mIsSubscribed) {
                    subscribe();
                }
            }
        }
    };

    private class RpcPushListener {

        RPCListeningThread wired;
        RPCListeningThread rf;
        RPCListeningThread HmInternal;

        RpcPushListener(Context ctx) {
            wired = new RPCListeningThread(ctx, HomeDroidApp.db(), 2000);
            rf = new RPCListeningThread(ctx, HomeDroidApp.db(), 2001);
            HmInternal = new RPCListeningThread(ctx, HomeDroidApp.db(), 2002);
        }

        void start() {
            wired.start();
            rf.start();
            HmInternal.start();
        }

        void pause() {
            Timber.i("Suspending Background Service");

            if (wired != null) {
                wired.pauseListening(true);
                rf.pauseListening(true);
                HmInternal.pauseListening(true);
            }
        }

        void resume() {
            Timber.i("Resuming Background Service");

            if (wired != null) {
                wired.pauseListening(false);
                rf.pauseListening(false);
                HmInternal.pauseListening(false);
            }
        }

        public void stop() {
            wired.interrupt();
            rf.interrupt();
            HmInternal.interrupt();
        }
    }

    private void onSubscribe(boolean isSuccessful) {
        mIsSubscribed = isSuccessful;
        if (mIsSubscribed) {
            setNotification(R.string.update_service_active);
        } else {
            setNotification(R.string.update_service_error);
        }
    }

    private synchronized void subscribe() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isSuccessful = false;

                if (sendInitToCCU(RpcForegroundService.this, 2001, false)) {
                    isSuccessful = true;
                }
                if (sendInitToCCU(RpcForegroundService.this, 2002, false)) {
                    isSuccessful = true;
                }
                if (sendInitToCCU(RpcForegroundService.this, 2000, false)) {
                    isSuccessful = true;
                }

                final boolean finalIsSuccessful = isSuccessful;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        onSubscribe(finalIsSuccessful);
                    }
                });
            }
        }).start();
    }

    private synchronized void unsubscribe() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendInitToCCU(RpcForegroundService.this, 2001, true);
                sendInitToCCU(RpcForegroundService.this, 2002, true);
                sendInitToCCU(RpcForegroundService.this, 2000, true);
            }
        }).start();
    }

    private synchronized boolean sendInitToCCU(Context ctx, int port, boolean unregister) {

        String serverAddress = IpAdressHelper.getServerAdress(ctx, false, false, true);
        String deviceIp = IpAdressHelper.getDeviceIp(ctx);

        if (deviceIp == null) {
            return false;
        }

        deviceIp = deviceIp.replaceAll(" ", "");
        String eventId;

        if (unregister) {
            eventId = "";
        } else {
            String withoutDots = deviceIp.replaceAll("\\.", "");
            eventId = withoutDots.substring(withoutDots.length() - 4, withoutDots.length());
        }

        if (deviceIp == null) {
            return false;
        }

        try {
            String uriString = "http://" + serverAddress + ":" + Integer.toString(port);
            XMLRPCClient client = new XMLRPCClient(new URL(uriString), FLAGS_DEFAULT_TYPE_STRING);

            new XMLRPCClient(new URL("http://example.com/xmlrpc"));
            AuthHelper.setHttpAuth(ctx, client);

            Log.i("HomeDroid RPC-Server", "Sending Init from " + deviceIp + " to " + uriString + ", EventID " + eventId);

            client.call("init", deviceIp + ":" + port, eventId);
        } catch (Exception e) {
            Timber.e(e);
            return false;
        }
        return true;
    }
}
