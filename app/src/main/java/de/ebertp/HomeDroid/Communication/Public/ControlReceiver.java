package de.ebertp.HomeDroid.Communication.Public;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import de.ebertp.HomeDroid.Communication.Control.ControlHelper;
import de.ebertp.HomeDroid.DbAdapter.BaseDbAdapter;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;

public class ControlReceiver extends BroadcastReceiver {


    //string value
    public static final String EXTRA_ISE_ID = "iseId";
    public static final String EXTRA_VALUE = "value";

    //int value
    public static final String EXTRA_TYPE = "type";

    public static final int TYPE_CHANNEL = 0;
    public static final int TYPE_VARIABLE = 1;
    public static final int TYPE_DATAPOINT = 2;
    public static final int TYPE_SCRIPT = 3;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Bundle b = intent.getExtras();

                if (b != null) {
                    int rowId = b.getInt(EXTRA_ISE_ID);

                    rowId = rowId + PreferenceHelper.getPrefix(context) * BaseDbAdapter.PREFIX_OFFSET;

                    int type = b.getInt(EXTRA_TYPE);

                    if (type != TYPE_SCRIPT) {

                        String value = b.getString(EXTRA_VALUE);
                        boolean isVariable = type == TYPE_VARIABLE ? true : false;
                        boolean isDatapoint = type == TYPE_DATAPOINT ? true : false;

                        ControlHelper.sendOrder(context, rowId, value, null, isVariable, isDatapoint);
                    } else {
                        ControlHelper.runProgram(context, rowId, null);
                    }
                }
            }
        }).start();

    }
}
