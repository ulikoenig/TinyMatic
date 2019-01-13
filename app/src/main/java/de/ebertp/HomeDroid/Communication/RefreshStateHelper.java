package de.ebertp.HomeDroid.Communication;

import android.content.Context;
import android.os.Handler;

import androidx.fragment.app.FragmentActivity;

import java.util.List;

import de.ebertp.HomeDroid.Communication.XmlToDbParser.CgiFile;
import de.ebertp.HomeDroid.Connection.BroadcastHelper;
import de.ebertp.HomeDroid.Model.HMObject;

public class RefreshStateHelper {

    public static void refreshChannel(Context ctx, String channelId, Handler toastHandler) {
        new XmlToDbParser(ctx, toastHandler).parseState(channelId);
        BroadcastHelper.refreshUI();
    }

    public static void refreshVariable(Context ctx, String rowId, Handler toastHandler) {
        new XmlToDbParser(ctx, toastHandler).parseVarState(rowId);
        BroadcastHelper.refreshUI();
    }

    public static void refreshBatch(Context ctx, List<HMObject> objects, Handler toastHandler) {
        new XmlToDbParser(ctx, toastHandler).parseStateBatch(objects);
        BroadcastHelper.refreshUI();
    }

    public static void refreshNotifications(Context ctx, Handler toastHandler) {
        new XmlToDbParser(ctx, toastHandler).parseStuff(CgiFile.systemNotification);
        BroadcastHelper.refreshUI();
    }

    public static void refreshProtocol(Context ctx, Handler toastHandler) {
        new XmlToDbParser(ctx, toastHandler).parseStuff(CgiFile.protocol);
        BroadcastHelper.refreshUI();
    }

    public static void refreshVariables(Context ctx, Handler toastHandler) {
        new XmlToDbParser(ctx, toastHandler).parseStuff(CgiFile.sysvarlist);
        BroadcastHelper.refreshUI();
    }

    public static void refreshScripts(Context ctx, Handler toastHandler) {
        new XmlToDbParser(ctx, toastHandler).parseStuff(CgiFile.programlist);
        BroadcastHelper.refreshUI();
    }
}
