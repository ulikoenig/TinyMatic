package de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.ebertp.HomeDroid.Activities.Listing.Fragments.SortableListDataFragment;
import de.ebertp.HomeDroid.DbAdapter.BaseRelationsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.DbUtil;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.Utils.PrefixHelper;
import de.ebertp.HomeDroid.Utils.hL;

public class FavRelationsDbAdapter extends BaseRelationsDbAdapter {

    public static final int CHANNELS_ID = 0;
    public static final int PROGRAMS_ID = 1;
    public static final int SYSTEM_VARIABLES_ID = 2;
    public static final int WEBCAMS_ID = 3;

    public FavRelationsDbAdapter(SQLiteDatabase mDb) {
        super(mDb);

        this.tableName = "favrelations";
        this.KEY_NAME = "room";

        this.createTableCommand = "create table if not exists favrelations (dummyId integer primary key, _id integer, "
                + "room text not null);";
    }

    public long createItem(int id, int room) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_NAME, room);

        return (int) mDb.insert(tableName, null, initialValues);
    }

    public Cursor fetchItemsByGroup(int groupId) {
        String objectsTable;
        if (groupId % 10 == PROGRAMS_ID) {
            objectsTable = "programs";
        } else if (groupId % 10 == SYSTEM_VARIABLES_ID) {
            objectsTable = "system_variables";
        } else {
            objectsTable = "channels";
        }

        return DbUtil.fetchSortedObjects(mDb, tableName, objectsTable, groupId, PrefixHelper.getFullPrefix(HomeDroidApp.getContext()) + SortableListDataFragment.GROUP_ID_DEVICE_FAVS);
    }

    public List<HMObject> getDaoObjects(int roomId) {
        if (roomId % 10 != WEBCAMS_ID) {
            Log.e(hL.TAG, "Not a webcam");
            return null;
        }

        String query = ("SELECT _id FROM favrelations where room = ?");
        String[] select = {Integer.toString(roomId)};
        Cursor cursor = mDb.rawQuery(query, select);

        if (cursor == null) {
            return null;
        }

        try {
            ArrayList<HMObject> hmObjects = new ArrayList<>();
            if (cursor.moveToFirst()) {
                do {
                    HMObject hmObject = HomeDroidApp.db().getWebCamDao().queryForId(cursor.getInt(0));
                    if (hmObject == null) {
                        continue;
                    }

                    if (cursor.getColumnIndex("sort_order") != -1) {
                        hmObject.setSortOrder(cursor.getInt(cursor.getColumnIndex("sort_order")));
                    }
                    hmObjects.add(hmObject);
                } while (cursor.moveToNext());
            }
            return hmObjects;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            cursor.close();
        }
    }
}
