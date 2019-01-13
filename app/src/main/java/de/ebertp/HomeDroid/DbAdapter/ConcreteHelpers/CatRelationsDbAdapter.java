package de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.ebertp.HomeDroid.DbAdapter.BaseRelationsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.DbUtil;


public class CatRelationsDbAdapter extends BaseRelationsDbAdapter {

    public CatRelationsDbAdapter(SQLiteDatabase mDb) {
        super(mDb);

        this.tableName = "catrelations";
        this.KEY_NAME = "room";

        this.createTableCommand = "create table if not exists catrelations (dummyId integer primary key, _id integer, "
                + "room text not null);";
    }

    public long createItem(int id, int room) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_NAME, room);

        return mDb.insert(tableName, null, initialValues);
    }

    @Override
    public Cursor fetchItemsByGroup(int mRoomId) {
        return DbUtil.fetchSortedChannels(mDb, tableName, mRoomId);
    }

    public boolean updateItem(long rowId, int room) {
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, room);

        return mDb.update(tableName, args, KEY_ROWID + "=" + "?", new String[]{Long.toString(rowId)}) > 0;
    }
}
