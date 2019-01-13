package de.ebertp.HomeDroid.ViewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.ebertp.HomeDroid.Model.HMRoom;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.CustomImageHelper;
import de.ebertp.HomeDroid.Utils.IconUtil;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.Util;

public class GridViewAdapter extends BaseAdapter {

    private LayoutInflater li;
    private CustomImageHelper customImageHelper;

    private Integer selectedId = null;
    private boolean isDualPane = false;
    private boolean isLargeScreen;
    private boolean isDarkTheme;
    private Context ctx;
    private ArrayList<HMRoom> rooms;

    public GridViewAdapter(Context ctx, ArrayList<HMRoom> rooms, boolean isDualPane) {
        li = LayoutInflater.from(ctx);
        customImageHelper = new CustomImageHelper(ctx);
        this.isDualPane = isDualPane;

        this.rooms = rooms;
        this.ctx = ctx;

        isLargeScreen = ctx.getResources().getBoolean(R.bool.isTablet);
        isDarkTheme = PreferenceHelper.isDarkTheme(ctx);
    }

    @Override
    public int getCount() {
        return rooms.size();
    }

    public Object getItem(int position) {
        return rooms.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public Integer getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(Integer selectedId) {
        this.selectedId = selectedId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = li.inflate(R.layout.grid_item, null);

            if (isDarkTheme) {
                convertView.setBackgroundResource(R.drawable.bg_card_dark_selektor);
            }
        }

        convertView.setId(rooms.get(position).getRowId());

        String name = rooms.get(position).getName();
        convertView.setTag(name);
        // set text convertView
        TextView tv = (TextView) convertView.findViewById(R.id.item_name);
        tv.setText(name);

        // set image view
        ImageView iv = (ImageView) convertView.findViewById(R.id.item_icon);
        iv.setImageResource(R.drawable.standart_icon3);

        if (isDualPane) {

            if (selectedId != null && rooms.get(position).getRowId() == selectedId) {
                if (isDarkTheme) {
                    convertView.setBackgroundResource(R.drawable.bg_card_grid_dark_selected);
                } else {
                    convertView.setBackgroundResource(R.drawable.bg_card_grid_selected);
                }
            } else {

                if (isDarkTheme) {
                    convertView.setBackgroundResource(R.drawable.bg_card_dark_selektor);
                } else {
                    convertView.setBackgroundResource(R.drawable.bg_card_grid_selektor);
                }

            }
        }

        switch (customImageHelper.setCustomImage(rooms.get(position).getRowId(), iv)) {
            case CustomImageHelper.EXTERNAL:
                break;
            case CustomImageHelper.INTERNAL:
                if (!isLargeScreen) {
                    iv.setPadding(15, 15, 15, 15);
                }
                Util.setIconColor(isDarkTheme, iv);
                break;
            default:
                if (IconUtil.setDefaultIcon(rooms.get(position), iv)) {
                    if (!isLargeScreen) {
                        iv.setPadding(15, 15, 15, 15);
                    }
                } else {
                    iv.setPadding(0, 0, 0, 0);
                }
                Util.setIconColor(isDarkTheme, iv);
                break;
        }

        return convertView;

    }

    public void setData(ArrayList<HMRoom> rooms) {
        this.rooms = rooms;
    }

}
