package de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.ebertp.HomeDroid.Activities.Listing.Fragments.SortableListDataFragment;
import de.ebertp.HomeDroid.DbAdapter.BaseRelationsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.DbUtil;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Utils.PrefixHelper;

public class CCUFavRelationsDbAdapter extends BaseRelationsDbAdapter {

    public static final String KEY_FAVLIST = "favlist";
    public static final String KEY_TYPE = "type";
    public static final String KEY_NCU = "not_can_use";
    public static final String KEY_FAVLISTID = "favlist";

    public CCUFavRelationsDbAdapter(SQLiteDatabase mDb) {
        super(mDb);

        this.tableName = "ccufavrelations";
        this.KEY_NAME = "name";

        this.createTableCommand = "create table if not exists ccufavrelations (_id integer not null, favlist integer not null,"
                + " name text not null, type text not null, not_can_use text not null);";
    }

    public long createItem(int id, String name, String type, String notCanUse, int listId) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_NCU, notCanUse);
        initialValues.put(KEY_FAVLIST, listId);

        return mDb.insert(tableName, null, initialValues);
    }

    public long replaceItem(int id, String name, String type, String notCanUse, int listId) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_NCU, notCanUse);
        initialValues.put(KEY_FAVLIST, listId);

        return mDb.replace(tableName, null, initialValues);
    }

    public static final String TYPE_CHANNEL = "CHANNEL";
    public static final String TYPE_SYSVAR = "SYSVAR";
    public static final String TYPE_PROGRAM = "PROGRAM";

    public Cursor fetchItemsByRoomAndType(int groupId, String type) {

        String objectsTable;
        if (type.equals(TYPE_PROGRAM)) {
            objectsTable = "programs";
        } else if (type.equals(TYPE_SYSVAR)) {
            objectsTable = "system_variables";
        } else {
            objectsTable = "channels";
        }

        String query = "SELECT " + objectsTable + ".*, s.sort_order as sort_order FROM " +
                "(SELECT DISTINCT _id FROM " + tableName + " where favlist = ?  and type = ?) AS filter " +
                "INNER JOIN " + objectsTable + " ON filter._id = " + objectsTable + "._id " +
                "LEFT JOIN (SELECT * from grouped_settings where group_id = ?) AS s ON " + objectsTable + "._id = s._id " +
                DbUtil.getOrderByClause(objectsTable);

        String[] select = {Integer.toString(groupId), type, Integer.toString(groupId)};
        Cursor mCursor = mDb.rawQuery(query, select);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    @Override
    public Cursor fetchItemsByGroup(int mRoomId) {
        String query = ("SELECT DISTINCT _id, name, type FROM ccufavrelations WHERE favlist = ?");
        String[] select = {Integer.toString(mRoomId)};
        Cursor mCursor = mDb.rawQuery(query, select);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

}
