package de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.ebertp.HomeDroid.DbAdapter.BaseRelationsDbAdapter;

public class NotificationsDbAdapter extends BaseRelationsDbAdapter {

    public static final String KEY_TYPE = "type";
    public static final String KEY_TIMESTAMP = "timestamp";

    public NotificationsDbAdapter(SQLiteDatabase mDb) {
        super(mDb);

        this.tableName = "notifications";
        this.KEY_NAME = "name";

        this.createTableCommand = "create table if not exists notifications (dummyId integer primary key, _id integer not null,  name text not null, type text not null, timestamp text not null);";
    }

    public long createItem(int id, String name, String type, String timestamp) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_TIMESTAMP, timestamp);

        return mDb.insert(tableName, null, initialValues);
    }

    public Cursor fetchAllItems(int prefix, String orderBy) {
        return mDb.query(tableName, new String[]{KEY_ROWID, KEY_NAME, KEY_TYPE, KEY_TIMESTAMP}, getPrefixCondition(prefix), null,
                null, null, orderBy);
    }

    @Override
    public Cursor fetchItemsByGroup(int mRoomId) {
        // TODO Auto-generated method stub
        return null;
    }

}
