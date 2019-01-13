package de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import de.ebertp.HomeDroid.DbAdapter.BaseCategoryDbAdapter;

public class RoomsDbAdapter extends BaseCategoryDbAdapter {

    public RoomsDbAdapter(SQLiteDatabase mDb) {
        super(mDb);

        this.KEY_NAME = "title";
        this.tableName = "rooms";
        this.createTableCommand = "create table if not exists rooms (_id integer primary key, "
                + "title text);";
    }

    public long createItem(int id, String title) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_NAME, title);

        return mDb.insert(tableName, null, initialValues);
    }

//    public boolean updateItem(long rowId, String title, String body) {
//        ContentValues args = new ContentValues();
//        args.put(KEY_NAME, title);
//
//        return mDb.update(tableName, args,  KEY_ROWID + "=" + "?", new String[]{Long.toString(rowId)}) > 0;
//    }
}
