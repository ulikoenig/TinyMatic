package de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.ebertp.HomeDroid.DbAdapter.BaseDbAdapter;
import timber.log.Timber;

public class GroupedItemSettingsDbAdapter extends BaseDbAdapter {

    private static final String KEY_COMBINED = "combined";
    private static final String KEY_SORT_ORDER = "sort_order";
    private static final String KEY_GROUP_ID = "group_id";
    private static final String KEY_HIDDEN = "hidden";

    /**
     * Does more that sorting now: Stores all settings for objects that are related to groupings
     */
    public GroupedItemSettingsDbAdapter(SQLiteDatabase mDb) {
        super(mDb);

        tableName = "grouped_settings";
        createTableCommand = "create table if not exists grouped_settings (combined text primary key, _id integer, "
                + "group_id text not null, sort_order integer, hidden integer);";
    }

    public boolean updateOrder(int rowId, int groupId, Integer sortOrder) {
        String combined = groupId + "." + rowId;

        ContentValues values = new ContentValues();
        values.put(KEY_COMBINED, combined);
        values.put(KEY_ROWID, rowId);
        values.put(KEY_GROUP_ID, Integer.toString(groupId));
        values.put(KEY_SORT_ORDER, sortOrder);

        if (mDb.update(tableName, values, KEY_COMBINED + "=" + "?", new String[]{combined}) > 0) {
            Timber.d("Updating entry");
            return true;
        }
        Timber.d("Creating entry");
        return mDb.insert(tableName, null, values) > 0;
    }

    public boolean updateHiddenState(int rowId, int groupId, int hidden) {
        String combined = groupId + "." + rowId;

        ContentValues values = new ContentValues();
        values.put(KEY_COMBINED, combined);
        values.put(KEY_ROWID, rowId);
        values.put(KEY_GROUP_ID, Integer.toString(groupId));
        values.put(KEY_HIDDEN, hidden);

        if (mDb.update(tableName, values, KEY_COMBINED + "=" + "?", new String[]{combined}) > 0) {
            Timber.d("Updating entry");
            return true;
        }
        Timber.d("Creating entry");
        return mDb.insert(tableName, null, values) > 0;
    }

    public boolean deleteSettingsForGroup(int groupId) {
        if (mDb.delete(tableName, KEY_GROUP_ID + "=" + "?", new String[]{Integer.toString(groupId)}) > 0) {
            Timber.d("Settings deleted");
            return true;
        }

        return false;
    }
}
