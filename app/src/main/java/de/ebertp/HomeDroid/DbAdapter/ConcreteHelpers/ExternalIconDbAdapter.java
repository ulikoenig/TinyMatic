package de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers;

import android.database.sqlite.SQLiteDatabase;

public class ExternalIconDbAdapter extends IconDbAdapter {

    public ExternalIconDbAdapter(SQLiteDatabase mDb) {
        super(mDb);

        this.tableName = "room_icons_external";

        this.createTableCommand = "create table if not exists room_icons_external (_id integer primary key, "
                + "icon integer not null);";
    }

}
