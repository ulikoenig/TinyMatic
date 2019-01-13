package de.ebertp.HomeDroid.ViewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.ebertp.HomeDroid.DbAdapter.DataBaseAdapterManager;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.Model.HMProtocol;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.TranslateUtil;

public class ProtocolListViewAdapter extends HMObjectViewAdapter {
    protected List<HMObject> objects;
    protected DataBaseAdapterManager dbM;

    protected Context ctx;
    private LayoutInflater li;
    private SimpleDateFormat simpleDateFormat;

    public ProtocolListViewAdapter(Context context, ArrayList<HMObject> objects) {
        this.objects = objects;
        this.ctx = context;
        dbM = HomeDroidApp.db();
        this.li = LayoutInflater.from(ctx);

        simpleDateFormat = new SimpleDateFormat("dd.MM. HH:mm:ss");
    }

    public List<HMObject> getObjects() {
        return objects;
    }

    public void setObjects(List<HMObject> objects) {
        this.objects = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        HMProtocol hmc = (HMProtocol) objects.get(position);

        if (convertView == null) {
            convertView = li.inflate(R.layout.notifications_item, null);

            if (PreferenceHelper.isDarkTheme(ctx)) {
                convertView.setBackgroundResource(R.drawable.bg_card_dark_selektor);
            }
        }

        ((TextView) convertView.findViewById(R.id.value)).setText(TranslateUtil.getTranslatedStringTable(hmc.getValue()));
        ((TextView) convertView.findViewById(R.id.time)).setText(simpleDateFormat.format(hmc.getTimestamp()));
        ((TextView) convertView.findViewById(R.id.name)).setText(hmc.getName());

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