package de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;

import de.ebertp.HomeDroid.Communication.Control.HmDatapoint;
import de.ebertp.HomeDroid.DbAdapter.BaseDbAdapter;

public class DatapointDbAdapter extends BaseDbAdapter {

    public DatapointDbAdapter(SQLiteDatabase mDb) {
        super(mDb);

        this.tableName = "datapoints";
        this.KEY_NAME = "name";

        this.createTableCommand = "create table if not exists datapoints (_id integer primary key, "
                + "name text not null, point_type text not null , channel_id int not null,"
                + "value text not null, value_type int not null, timestamp text not null);";
    }

    protected String KEY_PTYPE = "point_type";
    protected String KEY_CHANNEL = "channel_id";
    protected String KEY_VALUE = "value";
    protected String KEY_VALUETYPE = "value_type";
    protected String KEY_VALUEUNIT = "value_unit";
    protected String KEY_OPERATIONS = "operations";
    protected String KEY_TIMESTAMP = "timestamp";

    public long createItem(int id, String name, String point_type, int channel_id, String value, int value_type, String value_unit, Integer operations, String timestamp) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_PTYPE, point_type);
        initialValues.put(KEY_CHANNEL, channel_id);
        initialValues.put(KEY_VALUE, value);
        initialValues.put(KEY_VALUEUNIT, value_unit);
        initialValues.put(KEY_OPERATIONS, operations);
        initialValues.put(KEY_VALUETYPE, value_type);
        initialValues.put(KEY_TIMESTAMP, timestamp);


        return mDb.insert(tableName, null, initialValues);
    }

    public long replaceItem(int id, String name, String point_type, int channel_id, String value, int value_type, String value_unit, int operations, String timestamp) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_PTYPE, point_type);
        initialValues.put(KEY_CHANNEL, channel_id);
        initialValues.put(KEY_VALUE, value);
        initialValues.put(KEY_VALUEUNIT, value_unit);
        initialValues.put(KEY_OPERATIONS, operations);
        initialValues.put(KEY_VALUETYPE, value_type);
        initialValues.put(KEY_TIMESTAMP, timestamp);

        if (mDb.update(tableName, initialValues, KEY_ROWID + "=" + id, null) != 0) {
            return id;
        } else {
            return mDb.insert(tableName, null, initialValues);
        }
    }

    public boolean updateIfDifferent(int id, String name, String point_type, int channel_id, String value, int value_type, String value_unit, Integer operations, String timestamp) {

        Cursor mCursor = mDb.query(true, tableName, new String[]{KEY_VALUE}, KEY_ROWID + "=" + "?",
                new String[]{Long.toString(id)}, null, null, null, null);

        if (!mCursor.moveToFirst()) {
            createItem(id, name, point_type, channel_id, value, value_type, value_unit, operations, timestamp);
            mCursor.close();
            return true;
        }

        if (value.equals(mCursor.getString(0))) {
            mCursor.close();
            return false;
        }
        mCursor.close();
        return updateItem(id, value, timestamp);
    }

    public Cursor fetchItem(long rowId) throws SQLException {
        Cursor mCursor =

                mDb.query(true, tableName, new String[]{KEY_ROWID, KEY_NAME, KEY_CHANNEL}, KEY_ROWID + "=" + rowId, null, null, null, null,
                        null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public Cursor fetchItemsByChannelAndType(int channel, String pt) {

        String query = ("SELECT DISTINCT _id, value FROM datapoints WHERE channel_id = ? AND point_type=?");
        String[] select = {Integer.toString(channel), pt};
        Cursor mCursor = mDb.rawQuery(query, select);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor fetchItemsByChannelAndName(int channel, String name) {
        String query = ("SELECT DISTINCT _id, value FROM datapoints WHERE channel_id = ? AND name=?");
        String[] select = {Integer.toString(channel), name};
        Cursor mCursor = mDb.rawQuery(query, select);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor fetchItemBySerial(String name, int prefix) {

        String query = ("SELECT DISTINCT _id FROM datapoints WHERE name = ? " + "AND " + getPrefixCondition(prefix));
        String[] select = {name};
        Cursor mCursor = mDb.rawQuery(query, select);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    private static String sql = "INSERT OR REPLACE INTO datapoints (_id, name, point_type, channel_id, value, value_type, value_unit, operations, timestamp) VALUES (?,?,?,?,?,?,?,?,?)";

    public void updateBulk(ArrayList<HmDatapoint> data) {
        SQLiteStatement st = mDb.compileStatement(sql);
        try {
            mDb.beginTransaction();

            for (int i = 0; i < data.size(); i++) {

                st.clearBindings();
                st.bindLong(1, data.get(i).getIse_id());
                st.bindString(2, data.get(i).getName());
                st.bindString(3, data.get(i).getPoint_type());
                st.bindLong(4, data.get(i).getChannel_id());
                st.bindString(5, data.get(i).getValue());
                st.bindLong(6, data.get(i).getValue_type());
                st.bindString(7, data.get(i).getValue_unit());
                st.bindLong(8, data.get(i).getOperations());
                st.bindString(9, data.get(i).getTimestamp());
                st.executeInsert();

            }

            mDb.setTransactionSuccessful();

        } catch (Exception e) {
        } finally {
            mDb.endTransaction();
        }
    }

    public boolean updateItem(long rowId, String value, String timestamp) {
        ContentValues args = new ContentValues();
        args.put(KEY_VALUE, value);
        args.put(KEY_TIMESTAMP, timestamp);

        return mDb.update(tableName, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public boolean updateItemByChannelAndType(int channel, String pt, String value, int prefix) {

        String query = ("UPDATE datapoints SET value = ? WHERE channel_id = ? AND point_type=? " + "AND " + getPrefixCondition(prefix));
        String[] select = {value, Integer.toString(channel), pt,};

        mDb.rawQuery(query, select);

        return true;
    }

    public Cursor fetchItemsByChannel(int channel) {
        String query = ("SELECT * FROM datapoints WHERE channel_id = ?");
        String[] select = {Integer.toString(channel)};
        Cursor mCursor = mDb.rawQuery(query, select);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
}
