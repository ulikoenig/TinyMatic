package de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;

import de.ebertp.HomeDroid.DbAdapter.BaseRelationsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.DbUtil;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Model.HMVariable;
import de.ebertp.HomeDroid.Utils.PrefixHelper;

public class VariableDbAdapter extends BaseRelationsDbAdapter {

    public VariableDbAdapter(SQLiteDatabase mDb) {
        super(mDb);
        KEY_NAME = "name";

        this.tableName = "system_variables";

        this.createTableCommand = "create table if not exists system_variables (" + "_id integer primary key," + " name text not null,"
                + " variable text not null," + " value text not null," + " value_list text not null," + " min integer not null,"
                + " max integer not null," + " unit text not null," + " type text not null," + " subtype text not null,"
                + " isvisible integer," + " isoperate integer," + " value_name_0 text," + " value_name_1 text," + " timestamp text" + ");";
    }

    protected String KEY_VARIABLE = "variable";
    protected String KEY_VALUE = "value";
    protected String KEY_MIN = "min";
    protected String KEY_MAX = "max";
    protected String KEY_UNIT = "unit";
    protected String KEY_TYPE = "type";
    protected String KEY_SUBTYPE = "subtype";
    protected String KEY_VALUE_LIST = "value_list";
    protected String KEY_ISVISIBLE = "isvisible";
    protected String KEY_ISOPERATE = "isoperate";
    protected String KEY_VALUE_NAME_0 = "value_name_0";
    protected String KEY_VALUE_NAME_1 = "value_name_1";
    protected String KEY_TIMESTAMP = "timestamp";

    public long createItem(int id, String name, String variable, String value, String value_list, int min, int max, String unit,
                           String type, String subtype, boolean isVisible, boolean isOperate, String valueName0, String valueName1, String timestamp) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_VARIABLE, variable);
        initialValues.put(KEY_VALUE, value);
        initialValues.put(KEY_VALUE_LIST, value_list);
        initialValues.put(KEY_MIN, min);
        initialValues.put(KEY_MAX, max);
        initialValues.put(KEY_UNIT, unit);
        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_SUBTYPE, subtype);
        initialValues.put(KEY_ISVISIBLE, isVisible ? 1 : 0);
        initialValues.put(KEY_ISOPERATE, isOperate ? 1 : 0);
        initialValues.put(KEY_VALUE_NAME_0, valueName0);
        initialValues.put(KEY_VALUE_NAME_1, valueName1);
        initialValues.put(KEY_TIMESTAMP, timestamp);

        return mDb.insert(tableName, null, initialValues);
    }

    public long replaceItem(int id, String name, String variable, String value, String value_list, int min, int max, String unit,
                            String type, String subtype, boolean isVisible, boolean isOperate, String valueName0, String valueName1, String timestamp) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_VARIABLE, variable);
        initialValues.put(KEY_VALUE, value);
        initialValues.put(KEY_VALUE_LIST, value_list);
        initialValues.put(KEY_MIN, min);
        initialValues.put(KEY_MAX, max);
        initialValues.put(KEY_UNIT, unit);
        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_SUBTYPE, subtype);
        initialValues.put(KEY_ISVISIBLE, isVisible ? 1 : 0);
        initialValues.put(KEY_ISOPERATE, isOperate ? 1 : 0);
        initialValues.put(KEY_VALUE_NAME_0, valueName0);
        initialValues.put(KEY_VALUE_NAME_1, valueName1);
        initialValues.put(KEY_TIMESTAMP, timestamp);

        if (mDb.update(tableName, initialValues, KEY_ROWID + "=" + id, null) != 0) {
            return id;
        } else {
            return mDb.insert(tableName, null, initialValues);
        }
    }

    private static String sql = "INSERT OR REPLACE INTO system_variables (_id, name, variable, value, value_list, min, max, unit, type, subtype, isvisible, isoperate, value_name_0, value_name_1, timestamp) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public void updateBulk(ArrayList<HMVariable> data) {
        SQLiteStatement st = mDb.compileStatement(sql);
        try {
            mDb.beginTransaction();

            for (int i = 0; i < data.size(); i++) {
                st.clearBindings();
                st.bindLong(1, data.get(i).getRowId());
                st.bindString(2, data.get(i).getName());
                st.bindString(3, data.get(i).getVariable());
                st.bindString(4, data.get(i).getValue());
                st.bindString(5, data.get(i).getValue_list());
                st.bindLong(6, data.get(i).getMin());
                st.bindLong(7, data.get(i).getMax());
                st.bindString(8, data.get(i).getUnit());
                st.bindString(9, data.get(i).getVartype());
                st.bindString(10, data.get(i).getSubtype());
                st.bindLong(11, data.get(i).isVisible() ? 1 : 0);
                st.bindLong(12, data.get(i).isOperate() ? 1 : 0);
                st.bindString(13, data.get(i).getValueName0());
                st.bindString(14, data.get(i).getValueName1());
                st.bindString(15, data.get(i).getTimestamp());
                st.executeInsert();
            }

            mDb.setTransactionSuccessful();

        } catch (Exception e) {
        } finally {
            mDb.endTransaction();
        }
    }

    public Cursor fetchAllItems(int prefix, String orderBy) {
        //TODO add join with sort_orders
        return mDb.query(tableName, new String[]{KEY_ROWID, KEY_NAME, KEY_VARIABLE, KEY_VALUE, KEY_VALUE_LIST, KEY_MIN, KEY_MAX,
                KEY_UNIT, KEY_TYPE, KEY_SUBTYPE, KEY_ISVISIBLE, KEY_ISOPERATE, KEY_VALUE_NAME_0, KEY_VALUE_NAME_1, KEY_TIMESTAMP}, getPrefixCondition(prefix), null, null, null, orderBy);
    }

    public Cursor fetchAllItemsFilterInvisible(int prefix) {
        return mDb.query(tableName, null,
                getPrefixCondition(prefix) + " AND " + KEY_ISVISIBLE + " !='false'", null,
                null, null, null);
    }

    public Cursor fetchItem(long rowId) throws SQLException {

        Cursor mCursor =
                mDb.query(true, tableName, new String[]{KEY_ROWID, KEY_NAME, KEY_VARIABLE, KEY_VALUE, KEY_VALUE_LIST, KEY_MIN, KEY_MAX,
                        KEY_UNIT, KEY_TYPE, KEY_SUBTYPE, KEY_ISVISIBLE, KEY_ISOPERATE, KEY_VALUE_NAME_0, KEY_VALUE_NAME_1, KEY_TIMESTAMP}, KEY_ROWID + "=" + "?", new String[]{Long.toString(rowId)}, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public Cursor fetchConnectedVariables(int channelId) {
//        String query = "SELECT * FROM system_variables, datapoints WHERE datapoints.channel_id = ? AND datapoints._id = system_variables._id";

        String query = "SELECT * FROM system_variables JOIN datapoints ON datapoints._id = system_variables._id  WHERE datapoints.channel_id = ? AND datapoints. point_type= ''";

        String[] select = {Integer.toString(channelId)};
        Cursor mCursor = mDb.rawQuery(query, select);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    @Override
    public Cursor fetchItemsByGroup(int mRoomId) {
        String query = "SELECT filter.*, s.sort_order as sort_order FROM " +
                "(SELECT * FROM system_variables where " + getPrefixCondition(PrefixHelper.getPrefix(HomeDroidApp.getContext())) + ") AS filter " +
                "LEFT JOIN (SELECT * from grouped_settings where group_id = ?) AS s ON filter._id = s._id " +
                DbUtil.getOrderByClause("filter");

        String[] select = {Integer.toString(mRoomId)};
        Cursor mCursor = mDb.rawQuery(query, select);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean updateItem(long rowId, String value) {
        ContentValues args = new ContentValues();
        args.put(KEY_VALUE, value);

        return mDb.update(tableName, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
