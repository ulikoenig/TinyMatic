package de.ebertp.HomeDroid.Utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class ToastHandler extends Handler {

    private Context ctx;

    public ToastHandler(Context ctx) {
        super();
        this.ctx = ctx;
    }

    public void handleMessage(Message msg) {
        Toast.makeText(ctx, msg.getData().getString("trace"), msg.what).show();
    }
}
