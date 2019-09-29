package de.ebertp.HomeDroid.DbAdapter;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.Model.HMVariable;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.ViewAdapter.CursorToObjectHelper;
import timber.log.Timber;

public class DbUtil {

    public static Cursor fetchSortedObjects(SQLiteDatabase db, String groupTable, String objectsTable, int groupId) {
        return fetchSortedObjects(db, groupTable, objectsTable, groupId, groupId);
    }

    public static Cursor fetchSortedObjects(SQLiteDatabase db, String groupTable, String objectsTable, int groupId, int sortGroupId) {

        String query = "SELECT " + objectsTable + ".*, s.sort_order as sort_order, s.hidden as hidden FROM " +
                "(SELECT _id FROM " + groupTable + " where room = ?) AS filter " +
                "INNER JOIN " + objectsTable + " ON filter._id = " + objectsTable + "._id " +
                "LEFT JOIN (SELECT * from grouped_settings where group_id = ?) AS s ON " + objectsTable + "._id = s._id " +
                getOrderByClause(objectsTable);

        String[] select = {Integer.toString(groupId), Integer.toString(sortGroupId)};
        Cursor mCursor = db.rawQuery(query, select);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public static Cursor fetchSortedChannels(SQLiteDatabase db, String groupTable, int groupId) {
        return fetchSortedObjects(db, groupTable, "channels", groupId);
    }

    public static String getOrderByClause(String tableName) {
        if (PreferenceHelper.isSortByName(HomeDroidApp.getContext())) {
            return "ORDER BY " + tableName + ".name ASC";
        } else {
            return "";
        }
    }


    public static Double getDatapointDouble(int rowId, String type) {
        try {
            return Double.parseDouble(getDatapointString(rowId, type, "value"));
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer getDatapointInt(int rowId, String type) {
        try {
            return Integer.parseInt(getDatapointString(rowId, type, "value"));
        } catch (Exception e) {
            return null;
        }
    }

    public static String getDatapointString(int rowId, String type) {
        return getDatapointString(rowId, type, "value");
    }

    public static Boolean getDatapointBoolean(int rowId, String type) {
        String value = getDatapointString(rowId, type);

        if (value == null) {
            return null;
        }

        return value.equals("true");

    }

    public static Integer getDatapointId(int rowId, String type) {
        try {
            return Integer.parseInt(getDatapointString(rowId, type, "_id"));
        } catch (Exception e) {
            return null;
        }
    }

    public static String getDatapointString(int rowId, String type, String column) {
        Cursor cursor = HomeDroidApp.db().datapointDbAdapter.fetchItemsByChannelAndType(rowId, type);
        try {
            if (cursor.getCount() != 0) {
                return cursor.getString(cursor.getColumnIndex(column));
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public static ArrayList<HMObject> getConnectedVariables(int rowId) {
        Cursor cursor = HomeDroidApp.db().varsDbAdapter.fetchConnectedVariables(rowId);
        return CursorToObjectHelper.convertCursorToVariables(cursor);
    }

    public static Double getDatapointDoubleByName(int rowId, String name) {
        try {
            return Double.parseDouble(getDatapointStringByName(rowId, name, "value"));
        } catch (Exception e) {
            return null;
        }
    }

    public static String getDatapointStringByName(int rowId, String name, String column) {
        Cursor cursor = HomeDroidApp.db().datapointDbAdapter.fetchItemsByChannelAndName(rowId, name);
        try {
            if (cursor.getCount() != 0) {
                return cursor.getString(cursor.getColumnIndex(column));
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public static boolean hasDatapoint(int rowId, String type) {
        return getDatapointId(rowId, type) != null;
    }

    public static Long getLastTimeStamp(int rowId) {
        Cursor timeStampSource = HomeDroidApp.db().datapointDbAdapter.fetchItemsByChannel(rowId);
        if (timeStampSource == null || !timeStampSource.moveToFirst()) {
            timeStampSource = HomeDroidApp.db().varsDbAdapter.fetchItem(rowId);
        }

        if (timeStampSource == null || !timeStampSource.moveToFirst()) {
            timeStampSource = HomeDroidApp.db().programsDbAdapter.fetchItem(rowId);
        }

        if (timeStampSource == null || !timeStampSource.moveToFirst()) {
            Timber.d("No Data found");
            return null;
        }

        long maxTimeStamp = 0;
        do {
            String timestamp = timeStampSource.getString(timeStampSource.getColumnIndex("timestamp"));
            if (timestamp == null) {
                continue;
            }

            long timestampLong = Long.parseLong(timestamp);
            if (timestampLong > maxTimeStamp) {
                maxTimeStamp = timestampLong;
            }
        } while (timeStampSource.moveToNext());
        timeStampSource.close();

        if (maxTimeStamp == 0) {
            Timber.d("No timestamp found");
            return null;
        } else {
            return maxTimeStamp;
        }
    }
}
