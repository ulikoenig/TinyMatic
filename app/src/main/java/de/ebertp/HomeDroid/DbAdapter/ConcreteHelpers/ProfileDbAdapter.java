package de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import de.ebertp.HomeDroid.DbAdapter.BaseDbAdapter;

public class ProfileDbAdapter extends BaseDbAdapter {

    public ProfileDbAdapter(SQLiteDatabase mDb) {
        super(mDb);

        this.tableName = "profiles";

        this.createTableCommand = "create table if not exists profiles " +
                "(_id integer primary key, "
                + "name text not null " +
                ",syncAtStart boolean " +
                //update preferences
                ",updateServiceEnabled boolean" +
                ",periodicUpdatesEnabled boolean " +
                ",per_datapoints boolean " +
                ",per_variables boolean " +
                ",per_programs boolean " +
                ",periodicUpdateInterval text " +
                //standard preferences
                ",server homematic text" +
                //mh_preferences
                ",meineHomematic boolean" +
                ",mhUser text" +
                ",mhId text" +
                ",mhPass text" +
                //app_preferences
                ",default_tab text " +
                ",showNotifications boolean " +
                //advanced_preferences
                ",httpsOn boolean " +
                ",useRemoteServer boolean " +
                ",remoteServer text " +
                ",useRemotePort boolean " +
                ",serverPort text" +
                ");";
    }

    public long createOrUpdatePrefs(int id, String name) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_NAME, name);
        return mDb.replace(tableName, null, initialValues);
    }

    public Cursor getAllProfiles() {
        String query = ("SELECT * FROM " + tableName);
        Cursor mCursor = mDb.rawQuery(query, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean addProperty(int rowId, String column, Object value) {
        ContentValues initialValues = new ContentValues();

        if (Boolean.class.isInstance(value)) {
            initialValues.put(column, (Boolean) value);
        } else if (String.class.isInstance(value)) {
            initialValues.put(column, (String) value);
        } else if (Integer.class.isInstance(value)) {
            initialValues.put(column, (Integer) value);
        }

        return mDb.update(tableName, initialValues, KEY_ROWID + "=" + "?", new String[]{Long.toString(rowId)}) > 0;
    }

    public Cursor getProfile(int rowId) {
        String query = ("SELECT * FROM " + tableName + " WHERE " + KEY_ROWID + "=?");
        String[] select = {Integer.toString(rowId)};
        Cursor mCursor = mDb.rawQuery(query, select);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

}
