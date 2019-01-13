package de.ebertp.HomeDroid.DbAdapter;

import android.database.sqlite.SQLiteDatabase;

public abstract class BaseCategoryDbAdapter extends BaseDbAdapter {

    public BaseCategoryDbAdapter(SQLiteDatabase mDb) {
        super(mDb);
    }
}
