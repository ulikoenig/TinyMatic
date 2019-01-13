package de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;

import de.ebertp.HomeDroid.Communication.Control.HmDatapoint;
import de.ebertp.HomeDroid.DbAdapter.BaseRelationsDbAdapter;

public class ProtocolDbAdapter extends BaseRelationsDbAdapter {

    public static final String KEY_VALUE = "value";
    public static final String KEY_DATETIME = "datetime";

    public ProtocolDbAdapter(SQLiteDatabase mDb) {
        super(mDb);

        this.tableName = "protocol";
        this.KEY_NAME = "name";

        this.createTableCommand = "create table if not exists protocol (dummyId integer primary key, _id integer not null, name text not null, value text not null, datetime long not null);";
    }

    private static String sql = "INSERT OR REPLACE INTO datapoints (dummyId) VALUES (?,?,?,?,?,?)";

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
                st.executeInsert();

            }

            mDb.setTransactionSuccessful();

        } catch (Exception e) {
        } finally {
            mDb.endTransaction();
        }
    }

    public long createItem(int prefix, String name, String value, long timeMilis) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_VALUE, value);
        initialValues.put(KEY_DATETIME, timeMilis);
        initialValues.put(KEY_ROWID, prefix);

        return mDb.insert(tableName, null, initialValues);
    }

    public Cursor fetchAllItems(int prefix, String orderBy) {
        return mDb.query(tableName, new String[]{KEY_NAME, KEY_VALUE, KEY_DATETIME}, getPrefixCondition(prefix), null,
                null, null, orderBy);
    }

    @Override
    public Cursor fetchItemsByGroup(int mRoomId) {
        // TOO Auto-generated method stub
        return null;
    }

}
