package de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import de.ebertp.HomeDroid.DbAdapter.BaseDbAdapter;

public class IconDbAdapter extends BaseDbAdapter {

    public IconDbAdapter(SQLiteDatabase mDb) {
        super(mDb);

        this.tableName = "room_icons";

        this.createTableCommand = "create table if not exists room_icons (_id integer primary key, "
                + "icon integer not null);";
    }

    protected String KEY_ICON = "icon";

    public long createItem(int id, int icon) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_ICON, icon);

        return mDb.insert(tableName, null, initialValues);
    }

    public boolean deleteItem(long rowId) {

        return mDb.delete(tableName, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllItems(int prefix) {
        return mDb.query(tableName, new String[]{KEY_ROWID, KEY_ICON}
                , getPrefixCondition(prefix), null, null, null, null);
    }

    public Cursor fetchItem(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, tableName, new String[]{KEY_ROWID,
                                KEY_ICON}, KEY_ROWID + "=" + "?", new String[]{Long.toString(rowId)},
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public boolean updateItem(long rowId, int icon) {
        ContentValues args = new ContentValues();
        args.put(KEY_ICON, icon);

        return mDb.update(tableName, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public long replaceItem(int id, int icon) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_ICON, icon);

        return mDb.replace(tableName, null, initialValues);
    }


}
