package de.ebertp.HomeDroid.ViewAdapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.Util;

public class ProfileAdapter extends BaseAdapter {

    private Cursor c;
    private Context ctx;
    private LayoutInflater li;
    private int prefix;

    public ProfileAdapter(Context ctx, Cursor c) {
        this.ctx = ctx;
        this.c = c;
        this.prefix = PreferenceHelper.getPrefix(ctx);
        li = LayoutInflater.from(ctx);
    }

    public int getCount() {
        return c.getCount();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = li.inflate(R.layout.profile_row, null);
        }
        c.moveToPosition(position);

        int rowId = c.getInt(c.getColumnIndex("_id"));
        convertView.setId(rowId);

        String name = c.getString(c.getColumnIndex("name"));
        convertView.setTag(name);
        //set text view

        TextView textViewName = (TextView) convertView.findViewById(R.id.name);
        textViewName.setText(name);

        ImageView iconSpeech = (ImageView) convertView.findViewById(R.id.icon);
        Util.setIconColor(PreferenceHelper.isDarkTheme(ctx), iconSpeech);

        if (rowId == prefix) {
            textViewName.setTextColor(ctx.getResources().getColor(R.color.orange));
        } else {
            textViewName.setTextColor(textViewName.getTextColors().getDefaultColor());
        }

        return convertView;
    }
}

