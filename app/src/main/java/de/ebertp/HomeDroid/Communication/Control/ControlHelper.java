package de.ebertp.HomeDroid.Communication.Control;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Date;

import de.ebertp.HomeDroid.Communication.RefreshStateHelper;
import de.ebertp.HomeDroid.Communication.Sync.SyncJobIntentService;
import de.ebertp.HomeDroid.Communication.XmlToDbParser;
import de.ebertp.HomeDroid.Communication.XmlToDbParser.CgiFile;
import de.ebertp.HomeDroid.Connection.BroadcastHelper;
import de.ebertp.HomeDroid.Connection.InputStreamHelper;
import de.ebertp.HomeDroid.Connection.IpAdressHelper;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Model.HMCommand;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.PrefixHelper;
import de.ebertp.HomeDroid.Utils.ToastHandler;
import timber.log.Timber;

public class ControlHelper {

    public static final long UNLOCK_COMMAND_LIMIT = 50;

    public static final int COMMAND_WAIT_REFRESH = 2100;

    /**
     * legacy wrapper
     */
    @Deprecated
    public static void sendOrder(final Context ctx, final int rowId, final String value, final ToastHandler toastHandler,
                                 final boolean isVariable, final boolean isDatapoint) {

        int type;
        if (isVariable) {
            type = HMCommand.TYPE_VARIABLE;
        } else if (isDatapoint) {
            type = HMCommand.TYPE_DATAPOINT;
        } else {
            type = HMCommand.TYPE_CHANNEL;
        }

        sendOrder(ctx, new HMCommand(rowId, type, value, new Date()), toastHandler);
    }

    public static void runProgram(final Context ctx, final int ise_id, final Handler toastHandler) {
        final String ise_id1 = Integer.toString(PrefixHelper.removePrefix(ctx, ise_id));

        try {
            new Thread(new Runnable() {
                public void run() {
                    String url = IpAdressHelper.getServerAdress(ctx, true, true, false);

                    url = url + "/addons/xmlapi/runprogram.cgi?program_id=" + ise_id1;
                    Timber.i("Sending Program " + url);


                    if (sendCommand(ctx, url, toastHandler)) {
                        HMCommand.addCommandToHistory(ctx,
                                new HMCommand(ise_id, HMCommand.TYPE_SCRIPT, null, new Date()));
                    }

                    BroadcastHelper.refreshUI();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setProgramActive(final Context ctx, final int ise_id, final boolean active, final boolean visible, final Handler toastHandler) {
        final String ise_id1 = Integer.toString(PrefixHelper.removePrefix(ctx, ise_id));

        try {
            new Thread(new Runnable() {
                public void run() {
                    String url = IpAdressHelper.getServerAdress(ctx, true, true, false);

                    url = url + "/addons/xmlapi/programactions.cgi?program_id=" + ise_id + "&active=" + active + "&visible=" + visible;
                    Timber.i("Setting Program active " + url + " - " + active + " - " + visible);


                    if (sendCommand(ctx, url, toastHandler)) {
                        HMCommand.addCommandToHistory(ctx,
                                new HMCommand(ise_id, HMCommand.TYPE_SCRIPT, null, new Date()));

                        RefreshStateHelper.refreshScripts(ctx, toastHandler);
                    }

                    BroadcastHelper.refreshUI();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void sendOrder(final Context ctx, final HMCommand command, final Handler toastHandler) {

        if (command.getType() == HMCommand.TYPE_SCRIPT) {
            runProgram(ctx, command.getRowId(), toastHandler);
            return;
        }

        final String iseId = Integer.toString(PrefixHelper.removePrefix(ctx, command.getRowId()));


        try {
            final String newValue = command.getValue().replaceAll(" ", "%20");

            new Thread(new Runnable() {
                public void run() {
                    String url = IpAdressHelper.getServerAdress(ctx, true, true, false);
                    url = url + "/addons/xmlapi/statechange.cgi?ise_id=" + iseId + "&new_value=" + newValue;
                    url = url.replaceAll(" ", "%20");

                    Timber.i("Sending Command " + url);
                    if (sendCommand(ctx, url, toastHandler)) {

                        HMCommand.addCommandToHistory(ctx, command);

                        doRefresh(ctx, iseId, command, toastHandler);

                        try {
                            Thread.sleep(COMMAND_WAIT_REFRESH);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        doRefresh(ctx, iseId, command, toastHandler);
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void doRefresh(Context ctx, String iseId, HMCommand command, Handler toastHandler) {
        if (command.getType() == HMCommand.TYPE_VARIABLE) {
            RefreshStateHelper.refreshVariable(ctx, iseId, toastHandler);
        } else if (command.getType() == HMCommand.TYPE_DATAPOINT) {
            Cursor c = HomeDroidApp.db().datapointDbAdapter.fetchItem(command.getRowId());

            if (c.getCount() != 0) {
                int channelId = c.getInt(c.getColumnIndex("channel_id"));
                if (channelId != 0) {
                    RefreshStateHelper.refreshChannel(ctx, Integer.toString(PrefixHelper.removePrefix(ctx, channelId)),
                            toastHandler);
                }
            }

        } else {
            RefreshStateHelper.refreshChannel(ctx, iseId, toastHandler);
        }
    }

    public static void clearNotifications(final Context ctx) {
        try {
            new Thread(new Runnable() {
                public void run() {
                    String url = IpAdressHelper.getServerAdress(ctx, true, true, false);

                    url = url + "/addons/xmlapi/systemNotificationClear.cgi";
                    Log.i("TinyMatic", "Clearing Notifications " + url);

                    sendCommand(ctx, url, null);
                    new XmlToDbParser(ctx, null).parseStuff(CgiFile.systemNotification);
                    BroadcastHelper.refreshUI();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearProtocol(final Context ctx) {
        try {
            new Thread(new Runnable() {
                public void run() {
                    String url = IpAdressHelper.getServerAdress(ctx, true, true, false);

                    url = url + "/addons/xmlapi/protocol.cgi?clear=1";
                    Log.i("TinyMatic", "Clearing Protocol " + url);

                    sendCommand(ctx, url, null);
                    new XmlToDbParser(ctx, null).parseStuff(CgiFile.protocol);
                    BroadcastHelper.refreshUI();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean sendCommand(Context ctx, String url, Handler toastHandler1) {

        final Handler toastHandler = toastHandler1;

        PreferenceHelper.addSentCommand(ctx, url);

        if (!PreferenceHelper.isUnlocked(ctx) && PreferenceHelper.getCommandCount(ctx) >= UNLOCK_COMMAND_LIMIT) {
            if (toastHandler != null) {
                displayToast(toastHandler, ctx.getString(R.string.command_limit_reached), Toast.LENGTH_LONG);
            }
        } else {

            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp;

                boolean succ = false;

                xpp = factory.newPullParser();

                HttpURLConnection conn = InputStreamHelper.getUrlConnectionFromUrl(ctx, url, true);
                InputStream inputStream = InputStreamHelper.getInputStream(conn);

                int bufferSize = 1024;
                xpp.setInput(new BufferedReader(new InputStreamReader(inputStream, "ISO-8859-1"), bufferSize));

                while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                    if (xpp.getEventType() == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("result")) {
                            if (toastHandler != null) {

                                String msg = ctx.getString(R.string.send_successfully);
                                if (SyncJobIntentService.isSyncRunning()) {
                                    msg = msg + "\n" + ctx.getString(R.string.needs_refresh);
                                }
                                displayToast(toastHandler, msg);
                            }
                            PreferenceHelper.incrementCommandCount(ctx);
                            succ = true;
                        }
                    }
                    xpp.next();
                }
                if (!succ) {
                    displayToast(toastHandler, ctx.getString(R.string.not_send_successfully));
                }

                inputStream.close();
                conn.disconnect();

                return succ;
            } catch (Exception e) {
                displayToast(toastHandler, e.toString());
                e.printStackTrace();
            }
        }
        return false;
    }

    private static void displayToast(Handler toastHandler, String trace) {
        displayToast(toastHandler, trace, Toast.LENGTH_SHORT);
    }

    public static void displayToast(Handler toastHandler, String trace, int length) {
        Message msg = Message.obtain();
        msg.what = length;
        Bundle b = new Bundle();
        b.putString("trace", trace);
        msg.setData(b);
        if (toastHandler != null && msg != null) {
            toastHandler.sendMessage(msg);
        }
    }


}
