package de.ebertp.HomeDroid.ViewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.R;

public class SimpleListAdapter extends BaseAdapter {

    private List<HMObject> c;
    private LayoutInflater li;

    public SimpleListAdapter(Context ctx, List<HMObject> c) {
        this.c = c;
        li = LayoutInflater.from(ctx);
    }

    public int getCount() {
        return c.size();
    }

    public void setItems(List<HMObject> c) {
        this.c = c;
    }

    public HMObject getItem(int position) {
        return c.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = li.inflate(R.layout.simple_list_layout, null);
        }
        ((TextView) convertView.findViewById(R.id.name)).setText(c.get(position).getName());
        return convertView;
    }

}