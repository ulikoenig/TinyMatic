package de.ebertp.HomeDroid.DbAdapter;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class BaseRelationsDbAdapter extends BaseDbAdapter {

    public BaseRelationsDbAdapter(SQLiteDatabase mDb) {
        super(mDb);
    }

    public abstract Cursor fetchItemsByGroup(int mRoomId);
}
