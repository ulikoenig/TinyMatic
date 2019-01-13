package de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import de.ebertp.HomeDroid.DbAdapter.BaseDbAdapter;

public class LayerItemDbAdapter extends BaseDbAdapter {

    public LayerItemDbAdapter(SQLiteDatabase mDb) {
        super(mDb);

        this.tableName = "layer_items";
        this.createTableCommand = "create table if not exists layer_items"
                + " (_id integer primary key AUTOINCREMENT, ise_id integer not null, layer_id integer not null, type integer not null, "
                + "pos_x integer, pos_y integer, name text);";
    }

    protected String KEY_ISE_ID = "ise_id";
    protected String KEY_LAYER_ID = "layer_id";
    protected String KEY_TYPE = "type";
    protected String KEY_POS_X = "pos_x";
    protected String KEY_POS_Y = "pos_y";

    public long createItem(int id, int layerId, int type, Integer posX, Integer posY) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ISE_ID, id);
        initialValues.put(KEY_LAYER_ID, layerId);
        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_POS_X, posX);
        initialValues.put(KEY_POS_Y, posY);

        return mDb.insert(tableName, null, initialValues);
    }

    public int deleteLayerById(int layerId) {
        return mDb.delete(tableName, KEY_ISE_ID + "=" + layerId, null);
    }

    public boolean deleteItem(long rowId) {
        return mDb.delete(tableName, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllItems(int prefix) {
        return mDb.query(tableName, new String[]{KEY_ROWID, KEY_ISE_ID}, getPrefixCondition(prefix), null, null, null, null);
    }

    public Cursor fetchItem(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, tableName, new String[]{KEY_ROWID, KEY_ISE_ID, KEY_LAYER_ID, KEY_TYPE, KEY_POS_X, KEY_POS_Y, KEY_NAME}, KEY_ROWID
                        + "=" + "?", new String[]{Long.toString(rowId)}, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public Cursor fetchItemsByLayer(int layerId) {
        return mDb.query(tableName, new String[]{KEY_ROWID, KEY_ISE_ID, KEY_LAYER_ID, KEY_TYPE, KEY_POS_X, KEY_POS_Y, KEY_NAME}, KEY_LAYER_ID
                + " = " + layerId, null, null, null, null);
    }

    public boolean deleteItemsByLayer(int layerId) {
        return mDb.delete(tableName, KEY_LAYER_ID + "=" + layerId, null) > 0;
    }

    public boolean updateItem(int rowId, Integer posX, Integer posY, String name) {
        ContentValues args = new ContentValues();

        if (posX != null) {
            args.put(KEY_POS_X, posX);
        }

        if (posY != null) {
            args.put(KEY_POS_Y, posY);
        }

        if (name != null) {
            args.put(KEY_NAME, name);
        }

        return mDb.update(tableName, args, KEY_ROWID + "=" + "?", new String[]{Long.toString(rowId)}) > 0;
    }

}
