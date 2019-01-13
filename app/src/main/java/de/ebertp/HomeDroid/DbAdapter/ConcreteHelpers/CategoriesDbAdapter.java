package de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import de.ebertp.HomeDroid.DbAdapter.BaseCategoryDbAdapter;


public class CategoriesDbAdapter extends BaseCategoryDbAdapter {

    public CategoriesDbAdapter(SQLiteDatabase mDb) {
        super(mDb);

        this.tableName = "categories";
        this.KEY_NAME = "title";

        this.createTableCommand = "create table if not exists categories (_id integer primary key, "
                + "title text not null);";
    }

    public long createItem(int id, String title) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_NAME, title);

        return mDb.insert(tableName, null, initialValues);
    }

    public boolean updateItem(long rowId, String title) {
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, title);

        return mDb.update(tableName, args, KEY_ROWID + "=" + "?", new String[]{Long.toString(rowId)}) > 0;
    }


}
