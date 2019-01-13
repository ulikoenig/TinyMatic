package de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import de.ebertp.HomeDroid.DbAdapter.BaseDbAdapter;

public class ChannelsDbAdapter extends BaseDbAdapter {

    public ChannelsDbAdapter(SQLiteDatabase mDb) {
        super(mDb);

        this.tableName = "channels";
        this.KEY_NAME = "name";

        this.createTableCommand = "create table if not exists channels (_id integer primary key, "
                + "name text not null, device_type text not null, c_index integer not null, address text, isvisible integer, isoperate integer, device_name text);";
    }

    protected String KEY_DEVTYPE = "device_type";
    protected String KEY_CINDEX = "c_index";
    protected String KEY_ADDRESS = "address";
    protected String KEY_ISVISIBLE = "isvisible";
    protected String KEY_ISOPERATE = "isoperate";
    protected String KEY_DEVICE_NAME = "device_name";

    public long createItem(int id, String name, String device_type, int cType, String address, boolean isVisible,
                           boolean isOperate, String deviceName) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_DEVTYPE, device_type);
        initialValues.put(KEY_CINDEX, cType);
        initialValues.put(KEY_ADDRESS, address);
        initialValues.put(KEY_ISVISIBLE, isVisible ? 1 : 0);
        initialValues.put(KEY_ISOPERATE, isOperate ? 1 : 0);
        initialValues.put(KEY_DEVICE_NAME, deviceName);

        return mDb.insert(tableName, null, initialValues);
    }

    public long replaceItem(int id, String name, String device_type, int cType, String address, boolean isVisible,
                            boolean isOperate, String deviceName) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_DEVTYPE, device_type);
        initialValues.put(KEY_CINDEX, cType);
        initialValues.put(KEY_ADDRESS, address);
        initialValues.put(KEY_ISVISIBLE, isVisible ? 1 : 0);
        initialValues.put(KEY_ISOPERATE, isOperate ? 1 : 0);
        initialValues.put(KEY_DEVICE_NAME, deviceName);

        if (mDb.update(tableName, initialValues, KEY_ROWID + "=" + id, null) != 0) {
            return id;
        } else {
            return mDb.insert(tableName, null, initialValues);
        }
    }

    public Cursor fetchItem(long rowId) throws SQLException {
        Cursor mCursor = mDb.query(true, tableName,
                null, KEY_ROWID + "=" + "?", new String[]{Long.toString(rowId)}, null,
                null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor fetchAllItemsFilterInternal(int prefix) {
        return mDb.query(tableName, null,
                getPrefixCondition(prefix) + " AND " + KEY_CINDEX + " !=0" + " AND " + KEY_ISVISIBLE + " !='false'", null,
                null, null, null);
    }

    public boolean updateItem(long rowId, int name) {
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, name);

        return mDb.update(tableName, args, KEY_ROWID + "=" + "?", new String[]{Long.toString(rowId)}) > 0;
    }

    public Cursor searchByName(int prefix, String name) {
        String query = "SELECT * FROM " +
                "(SELECT * from " + tableName + " WHERE " + getPrefixCondition(prefix) + " AND " + KEY_CINDEX + " !=0) AS objects " +
                "LEFT JOIN (SELECT rowid, customname FROM hmobjectsettings) AS custom ON objects._id=custom.rowid " +
                "WHERE " + KEY_NAME + " like '%" + name + "%' OR custom.customname like '%" + name + "%' " + "OR " + KEY_DEVICE_NAME + " like '%" + name + "%'";

        String[] select = {};
        Cursor mCursor = mDb.rawQuery(query, select);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor getChannelsByDeviceName(int prefix, String deviceName) {
        Cursor mCursor = mDb.query(tableName, null,
                getPrefixCondition(prefix) + " AND " + KEY_DEVICE_NAME + " = " + "\'" + deviceName + "\'", null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
}
