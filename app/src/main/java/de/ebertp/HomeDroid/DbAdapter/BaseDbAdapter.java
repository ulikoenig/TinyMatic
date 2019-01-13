package de.ebertp.HomeDroid.DbAdapter;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public abstract class BaseDbAdapter {

    public static final String KEY_ROWID = "_id";
    public String KEY_NAME = "name";

    public static final int PREFIX_OFFSET = 1000000;

    protected SQLiteDatabase mDb;

    protected String tableName = "";
    protected String createTableCommand = "";

    public BaseDbAdapter(SQLiteDatabase mDb) {
        this.mDb = mDb;
    }

    public String getCreateTable() {
        return createTableCommand;
    }

    public String getTableName() {
        return tableName;
    }

    public boolean deleteItem(long rowId) {
        return mDb.delete(this.tableName, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public boolean isEmpty(int prefix) {
        Cursor cur = mDb.rawQuery("select (select _id from " +
                this.tableName + " where " + getPrefixCondition(prefix)
                + " limit 1) is not null", null);

        if (cur == null) {
            return true;
        }

        cur.moveToFirst();                       // Always one row returned.
        if (cur.getInt(0) == 0) {               // Zero count means empty table.
            cur.close();
            return true;
        }
        cur.close();
        return false;
    }

    public Cursor fetchAllItems(int prefix) {
        return fetchAllItems(prefix, null);
    }

    public Cursor fetchAllItems(int prefix, String orderBy) {
        return mDb.query(tableName, null
                , getPrefixCondition(prefix), null, null, null, orderBy);
    }

    public static String getPrefixCondition(int prefix) {
        return KEY_ROWID + " BETWEEN " + prefix * PREFIX_OFFSET + " AND " + (prefix + 1) * PREFIX_OFFSET;
    }

    public Cursor fetchItem(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, tableName, new String[]{KEY_ROWID,
                                KEY_NAME}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor searchByName(int prefix, String name) {
        String query = "SELECT * FROM " +
                "(SELECT * from " + tableName + " WHERE " + getPrefixCondition(prefix) + ") AS objects " +
                "LEFT JOIN (SELECT rowid, customname FROM hmobjectsettings) AS custom ON objects._id=custom.rowid " +
                "WHERE " + KEY_NAME + " like '%" + name + "%' OR custom.customname like '%" + name + "%' ";

        String[] select = {};
        Cursor mCursor = mDb.rawQuery(query, select);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public long getCount() {
        return DatabaseUtils.queryNumEntries(mDb, tableName);
    }
}
