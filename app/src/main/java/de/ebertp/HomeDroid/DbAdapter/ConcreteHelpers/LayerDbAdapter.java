package de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import de.ebertp.HomeDroid.DbAdapter.BaseDbAdapter;

public class LayerDbAdapter extends BaseDbAdapter {

    public LayerDbAdapter(SQLiteDatabase mDb) {
        super(mDb);

        this.tableName = "layers";
        this.createTableCommand = "create table if not exists layers (_id integer primary key, name text not null, background text);";
    }

    protected String KEY_BACKGROUND = "background";

    public long createItem(int id, String name, String background) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_BACKGROUND, background);

        return mDb.insert(tableName, null, initialValues);
    }

    public boolean deleteItem(long rowId) {
        return mDb.delete(tableName, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllItems(int prefix) {
        return mDb.query(tableName, new String[]{KEY_ROWID, KEY_NAME, KEY_BACKGROUND}, getPrefixCondition(prefix), null, null,
                null, null);
    }

    public Integer getHighestRowId(int prefix) {
        Cursor cursor = mDb.query(true, "layers", new String[]{KEY_ROWID}, "_id" + " BETWEEN " + prefix + " AND "
                + (prefix + BaseDbAdapter.PREFIX_OFFSET), null, null, null, KEY_ROWID + " DESC", null);

        if (cursor != null && cursor.moveToFirst()) {
            int rowId = cursor.getInt(cursor.getColumnIndex(KEY_ROWID));
            cursor.close();
            return rowId;
        }

        cursor.close();
        return null;
    }

    public Cursor fetchItem(long rowId) throws SQLException {

        Cursor cursor =

                mDb.query(true, tableName, new String[]{KEY_ROWID, KEY_NAME, KEY_BACKGROUND}, KEY_ROWID + "=" + "?",
                        new String[]{Long.toString(rowId)}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;

    }

    public boolean updateItem(long rowId, String name, String background) {
        ContentValues args = new ContentValues();
        if (name != null) {
            args.put(KEY_NAME, name);
        }
        if (background != null) {
            args.put(KEY_BACKGROUND, background);
        }
        return mDb.update(tableName, args, KEY_ROWID + "=" + "?", new String[]{Long.toString(rowId)}) > 0;
    }

}
