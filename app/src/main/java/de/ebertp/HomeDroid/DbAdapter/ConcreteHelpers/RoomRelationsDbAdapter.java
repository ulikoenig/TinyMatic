package de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.ebertp.HomeDroid.DbAdapter.BaseRelationsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.DbUtil;

public class RoomRelationsDbAdapter extends BaseRelationsDbAdapter {

    public RoomRelationsDbAdapter(SQLiteDatabase mDb) {
        super(mDb);

        this.tableName = "relations";
        this.KEY_NAME = "room";

        this.createTableCommand = "create table if not exists relations (dummyId text primary key, _id integer, "
                + "room text not null);";
    }

    public long createItem(int id, int roomId) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_NAME, roomId);

        return mDb.insert(tableName, null, initialValues);
    }

    public boolean deleteItem(long rowId) {
        return mDb.delete(tableName, KEY_ROWID + "=" + "?", new String[]{Long.toString(rowId)}) > 0;
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
