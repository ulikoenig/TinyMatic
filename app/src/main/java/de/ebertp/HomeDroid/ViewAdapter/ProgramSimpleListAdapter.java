package de.ebertp.HomeDroid.ViewAdapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.ProgramsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.DataBaseAdapterManager;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;

public class ProgramSimpleListAdapter extends BaseAdapter {

    protected DataBaseAdapterManager dbM;
    private ProgramsDbAdapter progamsDbAdapter;
    private Cursor c;
    private LayoutInflater li;

    public ProgramSimpleListAdapter(Context ctx) {
//		this.ctx = ctx;
        dbM = HomeDroidApp.db();
        progamsDbAdapter = dbM.programsDbAdapter;
        c = progamsDbAdapter.fetchAllItems(PreferenceHelper.getPrefix(ctx));
        li = LayoutInflater.from(ctx);
    }

    public int getCount() {
        return c.getCount();
    }

    public Object getItem(int position) {
        c.moveToPosition(position);
        return c;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = li.inflate(R.layout.program_select, null);
        }
        if (c.moveToPosition(position)) {
            ((TextView) convertView.findViewById(R.id.name)).setText(c.getString(c.getColumnIndex("name")));
        }
        return convertView;
    }

}