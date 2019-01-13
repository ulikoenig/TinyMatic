package de.ebertp.HomeDroid.Activities.Drawer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import de.ebertp.HomeDroid.Model.HMDrawerItem;
import de.ebertp.HomeDroid.R;

public class DrawerArrayAdapter extends ArrayAdapter<HMDrawerItem> {

    private final LayoutInflater inflater;
    Context context;
    int layoutResourceId;
    HMDrawerItem data[] = null;

    public DrawerArrayAdapter(Context context, int layoutResourceId, HMDrawerItem[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HMDrawerItemHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(layoutResourceId, parent, false);

            holder = new HMDrawerItemHolder();
            holder.imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
            holder.divider = convertView.findViewById(R.id.divider);

            if (convertView.findViewById(R.id.txtTitle) != null) {
                holder.txtTitle = (TextView) convertView.findViewById(R.id.txtTitle);
            }

            convertView.setTag(holder);
        } else {
            holder = (HMDrawerItemHolder) convertView.getTag();
        }

        HMDrawerItem drawerItem = data[position];

        if (holder.txtTitle != null) {
            holder.txtTitle.setText(drawerItem.getNameRes());
        }
        holder.imgIcon.setImageResource(drawerItem.getPicRes());
        holder.divider.setVisibility(drawerItem.isDrawBorderBelow() ? View.VISIBLE : View.GONE);

        return convertView;
    }

    static class HMDrawerItemHolder {
        ImageView imgIcon;
        TextView txtTitle;
        View divider;
    }
}
