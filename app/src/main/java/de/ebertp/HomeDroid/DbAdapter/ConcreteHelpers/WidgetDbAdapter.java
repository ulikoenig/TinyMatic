package de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import de.ebertp.HomeDroid.DbAdapter.BaseDbAdapter;

public class WidgetDbAdapter extends BaseDbAdapter {

    public WidgetDbAdapter(SQLiteDatabase mDb) {
        super(mDb);

        this.tableName = "widgets";

        this.createTableCommand = "create table if not exists widgets (_id integer primary key, " +
                "channel integer not null, type integer not null, name text not null, control_mode integer);";
    }

    public String KEY_CHANNEL = "channel";
    public String KEY_TYPE = "type";
    public String KEY_NAME = "name";
    public String KEY_CONTROL_MODE = "control_mode";

    public long createItem(int id, int channel, int type, String name, int controlMode) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_CHANNEL, channel);
        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_CONTROL_MODE, controlMode);

        return mDb.insert(tableName, null, initialValues);
    }

    public boolean deleteItem(long rowId) {
        return mDb.delete(tableName, KEY_ROWID + "=" + "?", new String[]{Long.toString(rowId)}) > 0;
    }

    public Cursor fetchAllItems(int prefix) {
        return mDb.query(tableName, null
                , getPrefixCondition(prefix), null, null, null, null);
    }

    public Cursor fetchItem(long rowId) throws SQLException {

        Cursor mCursor =
                mDb.query(true, tableName, null, KEY_ROWID + "=" + "?", new String[]{Long.toString(rowId)},
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public boolean updateItem(long rowId, int icon) {
        ContentValues args = new ContentValues();
        args.put(KEY_CHANNEL, icon);

        return mDb.update(tableName, args, KEY_ROWID + "=" + "?", new String[]{Long.toString(rowId)}) > 0;
    }

    public long replaceItem(int id, int icon) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_CHANNEL, icon);

        if (mDb.update(tableName, initialValues, KEY_ROWID + "=" + id, null) != 0) {
            return id;
        } else {
            return mDb.insert(tableName, null, initialValues);
        }
    }


}
