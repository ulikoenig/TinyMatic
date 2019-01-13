package de.ebertp.HomeDroid.Communication.Rpc;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import org.xmlrpc.android.MethodCall;
import org.xmlrpc.android.XMLRPCServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.ebertp.HomeDroid.Connection.BroadcastHelper;
import de.ebertp.HomeDroid.DbAdapter.DataBaseAdapterManager;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import timber.log.Timber;

public class RPCListeningThread extends Thread {

    private int port;
    private ServerSocket socket;
    private boolean isStopped;
    private RPCEventParser eventParser;

    public RPCListeningThread(Context ctx, DataBaseAdapterManager dbM, int port) {
        this.port = port;
        this.eventParser = new RPCEventParser(ctx, dbM);
    }

    public void pauseListening(boolean isStopped) {
        this.isStopped = isStopped;
    }

    public void interrupt() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.interrupt();
    }

    public void run() {
        try {
            String portString = Integer.toString(port);
            Timber.i("Listening on  Port %s", portString);
            socket = new ServerSocket(port);
            XMLRPCServer server = new XMLRPCServer();
            while (true) {

                Socket client = socket.accept();
                MethodCall call = server.readMethodCall(client);
                String name = call.getMethodName();
                ArrayList<Object> params = call.getParams();

                Timber.i("Receiving XML-RPC Call [" + name + "] on Port " + port);
                // Log.i("rpc call",name);
                if (!isStopped) {
                    eventParser.parseEvent(name, params, port);
                    BroadcastHelper.refreshUI();
                }

                switch (name) {
                    case "system.multicall":
                        Object[] children = (Object[]) params.get(0);
                        List<Object> response = new ArrayList<Object>();

                        for (Object aChildren : children) {
                            response.add(new Object[]{""});
                        }
                        server.respond(client, response.toArray());
                        break;
                    case "event": {
                        String i0 = (String) params.get(0);
                        String i1 = (String) params.get(1);
                        server.respond(client, new Object[]{i0 + i1});
                        break;
                    }
                    case "listDevices":
                        server.respond(client, new Object[]{""});
                        break;
                    case "newDevices":
                        server.respond(client, new Object[]{""});
                        break;
                    case "system.listMethods": {
                        String i0 = "event";
                        server.respond(client, new Object[]{i0});
                        break;
                    }
                    default:
                        server.respond(client, null);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopThread() {
        if (socket != null) {
            // This is supposed to interrupt the waiting read.
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class RPCEventParser {

        private int writeCount = 0;
        private Context ctx;
        private DataBaseAdapterManager dbM;
        private int prefix;

        public RPCEventParser(Context ctx, DataBaseAdapterManager dbM) {
            this.ctx = ctx;
            this.dbM = dbM;

            prefix = PreferenceHelper.getPrefix(ctx);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        synchronized void parseEvent(String name, ArrayList<Object> params, int port) {

            writeCount++;
            setWritesNotification();

            switch (name) {
                case "system.multicall":
                    Timber.i("Multicall");
                    Object[] children = (Object[]) params.get(0);

                    for (Object aChildren : children) {
                        Map m = (Map) aChildren;
                        String childName = (String) m.get("methodName");
                        Object[] cOfC = (Object[]) m.get("params");
                        ;
                        ArrayList<Object> childParams = new ArrayList(Arrays.asList(cOfC));
                        parseEvent(childName, childParams, port);
                    }
                    break;
                case "event":
                    String channel = (String) params.get(1);
                    String datapoint = (String) params.get(2);
                    Object rawValue = params.get(3);
                    String value = "not recognized";
                    if (params.get(3) instanceof Boolean) {
                        if ((Boolean) rawValue) {
                            value = "true";
                        } else
                            value = "false";
                    } else if (params.get(3) instanceof Double) {
                        value = Double.toString((Double) rawValue);
                    } else if (params.get(3) instanceof Integer) {
                        value = Integer.toString((Integer) rawValue);
                    }

                    Timber.i("event: " + channel + "/" + datapoint + "/" + value);

                    String serial_name;

                    if (port == 2001)
                        serial_name = "BidCos-RF." + channel + "." + datapoint;
                    else
                        serial_name = "BidCos-Wired." + channel + "." + datapoint;

                    writeEventToDb(serial_name, value);
                    break;
                case "listDevices":
                case "system.listMethods":
                case "newDevices":
                    // no need to do anything, just respond later
                    break;
                default:
                    Timber.i("Unknown MethodRespone: %s", name);
                    break;
            }
        }

        private void setWritesNotification() {
            PreferenceHelper.setWriteCount(ctx, writeCount);
        }

        private synchronized void writeEventToDb(String serial_name, String value) {
            Cursor c = dbM.datapointDbAdapter.fetchItemBySerial(serial_name, prefix);
            if (c.getCount() != 0) {
                //TODO FIX THIS FOR RPC
                if (dbM.datapointDbAdapter.updateItem(c.getLong(0), value, null))
                    ;
                Timber.i(value + " saved to" + serial_name);
            } else
                Timber.i("Update not saved, Datapoint " + serial_name + " not found");
            de.ebertp.HomeDroid.Utils.Util.closeCursor(c);
        }
    }

}