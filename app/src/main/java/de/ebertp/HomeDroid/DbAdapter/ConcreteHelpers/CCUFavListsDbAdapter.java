package de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import de.ebertp.HomeDroid.DbAdapter.BaseCategoryDbAdapter;

public class CCUFavListsDbAdapter extends BaseCategoryDbAdapter {

    public CCUFavListsDbAdapter(SQLiteDatabase mDb) {
        super(mDb);

        this.KEY_NAME = "title";
        this.tableName = "favlists";
        this.createTableCommand = "create table if not exists favlists (_id integer primary key, "
                + "title text not null);";
    }

    public long createItem(int id, String name) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_NAME, name);

        return mDb.insert(tableName, null, initialValues);
    }

    public long replaceItem(int id, String name) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_NAME, name);

        if (mDb.update(tableName, initialValues, KEY_ROWID + "=" + id, null) != 0) {
            return id;
        } else {
            return mDb.insert(tableName, null, initialValues);
        }
    }

    public boolean updateItem(long rowId, String title, String body) {
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, title);

        return mDb.update(tableName, args, KEY_ROWID + "=" + "?", new String[]{Long.toString(rowId)}) > 0;
    }
}
