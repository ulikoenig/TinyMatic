package de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;

import de.ebertp.HomeDroid.DbAdapter.BaseRelationsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.DbUtil;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Model.HMScript;
import de.ebertp.HomeDroid.Utils.PrefixHelper;

public class ProgramsDbAdapter extends BaseRelationsDbAdapter {

    public ProgramsDbAdapter(SQLiteDatabase mDb) {
        super(mDb);

        this.tableName = "programs";
        this.KEY_NAME = "name";

        this.createTableCommand = "create table if not exists programs (_id integer primary key, "
                + "name text not null, value text not null, isvisible integer, isoperate integer, timestamp text );";

    }

    protected String KEY_VALUE = "value";
    protected String KEY_ISVISIBLE = "isvisible";
    protected String KEY_ISOPERATE = "isoperate";
    protected String KEY_TIMESTAMP = "timestamp";

    private static String sql = "INSERT OR REPLACE INTO programs (_id, name, value, isvisible, isoperate, timestamp) VALUES (?,?,?,?,?,?)";

    public void updateBulk(ArrayList<HMScript> data) {
        SQLiteStatement st = mDb.compileStatement(sql);
        try {
            mDb.beginTransaction();

            for (int i = 0; i < data.size(); i++) {

                st.clearBindings();
                st.bindLong(1, data.get(i).getRowId());
                st.bindString(2, data.get(i).getName());
                st.bindString(3, data.get(i).getValue());
                st.bindLong(4, data.get(i).isVisible() ? 1 : 0);
                st.bindLong(5, data.get(i).isOperate() ? 1 : 0);
                st.bindString(6, data.get(i).getTimestamp());
                st.executeInsert();
            }

            mDb.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDb.endTransaction();
        }
    }

    public long createItem(int id, String name, String value, boolean isVisible, boolean isOperate) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_VALUE, value);
        initialValues.put(KEY_ISVISIBLE, isVisible ? 1 : 0);
        initialValues.put(KEY_ISOPERATE, isOperate ? 1 : 0);

        return mDb.insert(tableName, null, initialValues);
    }

    public long replaceItem(int id, String name, String value, boolean isVisible, boolean isOperate) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_VALUE, value);
        initialValues.put(KEY_ISVISIBLE, isVisible ? 1 : 0);
        initialValues.put(KEY_ISOPERATE, isOperate ? 1 : 0);

        if (mDb.update(tableName, initialValues, KEY_ROWID + "=" + id, null) != 0) {
            return id;
        } else {
            return mDb.insert(tableName, null, initialValues);
        }
    }

    public Cursor fetchAllItems(int prefix, String orderBy) {
        Cursor mCursor = mDb.query(tableName, new String[]{KEY_ROWID, KEY_NAME, KEY_VALUE, KEY_ISVISIBLE, KEY_ISOPERATE, KEY_TIMESTAMP}, getPrefixCondition(prefix), null, null,
                null, orderBy);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor fetchAllItemsFilterInvisible(int prefix) {
        return mDb.query(tableName, null,
                getPrefixCondition(prefix) + " AND " + KEY_ISVISIBLE + " !='false'", null,
                null, null, null);
    }

    public Cursor fetchItem(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, tableName, new String[]{KEY_ROWID, KEY_NAME, KEY_VALUE, KEY_ISVISIBLE, KEY_ISOPERATE, KEY_TIMESTAMP}, KEY_ROWID + "=" + "?",
                        new String[]{Long.toString(rowId)}, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    @Override
    public Cursor fetchItemsByGroup(int mRoomId) {
        String query = "SELECT filter.*, s.sort_order as sort_order FROM " +
                "(SELECT * FROM programs where " + getPrefixCondition(PrefixHelper.getPrefix(HomeDroidApp.getContext())) + ") AS filter " +
                "LEFT JOIN (SELECT * from grouped_settings where group_id = ?) AS s ON filter._id = s._id " +
                DbUtil.getOrderByClause("filter");

        String[] select = {Integer.toString(mRoomId)};
        Cursor mCursor = mDb.rawQuery(query, select);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
}
