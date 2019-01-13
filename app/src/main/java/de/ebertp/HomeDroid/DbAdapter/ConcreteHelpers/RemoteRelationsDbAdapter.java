package de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.ebertp.HomeDroid.DbAdapter.BaseRelationsDbAdapter;

public class RemoteRelationsDbAdapter extends BaseRelationsDbAdapter {

    public RemoteRelationsDbAdapter(SQLiteDatabase mDb) {
        super(mDb);

    }

    @Override
    public Cursor fetchItemsByGroup(int mRoomId) {
        int lower = mRoomId + 1010;
        int upper = lower + 200;

        Cursor mCursor =

                mDb.query(true, "channels", new String[]{"_id", "name", "device_type", "c_index", "address", "isvisible", "isoperate"},
                        "_id" + " BETWEEN " + lower + " AND " + upper, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        // System.out.println(mCursor.getCount()+"#################"+lower+"/"+upper);
        return mCursor;
    }
}
