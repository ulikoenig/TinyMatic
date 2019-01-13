package de.ebertp.HomeDroid.Widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.ebertp.HomeDroid.Communication.Control.ControlHelper;
import de.ebertp.HomeDroid.Utils.ToastHandler;
import de.ebertp.HomeDroid.Utils.Util;
import timber.log.Timber;

public class WidgetFlipSwitchReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("onReceive()");
        flipSwitch(context, intent);
    }

    private void flipSwitch(Context context, Intent intent) {
        ToastHandler toastHandler = new ToastHandler(context);

        int iseID = intent.getExtras().getInt(Util.INTENT_ISE_ID);
        String newValue = intent.getExtras().getString(Util.INTENT_NEWVALUE);

        switch (intent.getExtras().getInt(Util.INTENT_WIDGET_TYPE)) {
            case Util.TYPE_CHANNEL: {
                ControlHelper.sendOrder(context, iseID, newValue, toastHandler, false, false);
                break;
            }
            case Util.TYPE_SCRIPT: {
                ControlHelper.runProgram(context, iseID, toastHandler);
                break;
            }
            case Util.TYPE_VARIABLE: {
                ControlHelper.sendOrder(context, iseID, newValue, toastHandler, true, false);
                break;
            }
            case Util.TYPE_DATAPOINT: {
                ControlHelper.sendOrder(context, iseID, newValue, toastHandler, false, true);
            }
        }
    }
}
