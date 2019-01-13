package de.ebertp.HomeDroid.ViewAdapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.ebertp.HomeDroid.DbAdapter.DataBaseAdapterManager;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Model.HMNotification;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.TranslateUtil;

public class NotificationListViewAdapter extends HMObjectViewAdapter {
    protected List<HMObject> objects;
    protected DataBaseAdapterManager dbM;

    protected Context ctx;
    private LayoutInflater li;
    private SimpleDateFormat simpleDateFormat;

    public NotificationListViewAdapter(Context context, Activity activity, ArrayList<HMObject> objects) {
        this.objects = objects;
        this.ctx = context;
        dbM = HomeDroidApp.db();
        this.li = LayoutInflater.from(ctx);

        simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    }

    /**
     * @return the objects
     */
    public List<HMObject> getObjects() {
        return objects;
    }

    /**
     * @param objects the objects to set
     */
    public void setObjects(List<HMObject> objects) {
        this.objects = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        HMNotification hmc = (HMNotification) objects.get(position);

        if (convertView == null) {
            convertView = li.inflate(R.layout.notifications_item, null);

            if (PreferenceHelper.isDarkTheme(ctx)) {
                convertView.setBackgroundResource(R.drawable.bg_card_dark_selektor);
            }
        }

        ((TextView) convertView.findViewById(R.id.value)).setText(TranslateUtil.getNotificationTypeText(hmc.getType()));
        ((TextView) convertView.findViewById(R.id.time)).setText(simpleDateFormat.format(hmc.getTimestamp()));

        Cursor c = dbM.datapointDbAdapter.fetchItem(hmc.rowId);
        if (c.getCount() != 0) {
            int channelId = c.getInt(c.getColumnIndex("channel_id"));
            System.out.println("NotificationListViewAdapter.getView() " + channelId);
            Cursor d = dbM.channelsDbAdapter.fetchItem(channelId);
            if (d.getCount() != 0) {
                String name = d.getString(d.getColumnIndex("name"));
                System.out.println("NotificationListViewAdapter.getView() " + name);
                ((TextView) convertView.findViewById(R.id.name)).setText(name);
            } else {
                System.out.println("NotificationListViewAdapter.getView()" + " not found");
            }
            d.close();
        }
        c.close();
        return convertView;
    }

    public int getCount() {
        return objects.size();
    }

    public Object getItem(int position) {
        return objects.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

}