package de.ebertp.HomeDroid.DbAdapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.ArrayList;

import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.CCUFavListsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.CCUFavRelationsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.CatRelationsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.CategoriesDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.ChannelsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.DatapointDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.ExternalIconDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.FavRelationsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.IconDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.LayerDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.LayerItemDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.NotificationsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.ProfileDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.ProgramsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.ProtocolDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.RemoteRelationsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.RoomRelationsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.RoomsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.GroupedItemSettingsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.VariableDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.WidgetDbAdapter;
import de.ebertp.HomeDroid.Model.HMCommand;
import de.ebertp.HomeDroid.Model.HMObjectSettings;
import de.ebertp.HomeDroid.Model.HmSpeechCommand;
import de.ebertp.HomeDroid.Model.WebCam;
import timber.log.Timber;

public class DataBaseAdapterManager extends OrmLiteSqliteOpenHelper {

    public static final String DATABASE_NAME = "data";
    public static final int DATABASE_VERSION = 35;
    private static final String TAG = "HomeDroid Database";

    private SQLiteDatabase mDb;
    private ArrayList<BaseDbAdapter> dbAdapterList;

    public CategoriesDbAdapter categoriesDbAdapter;
    public CatRelationsDbAdapter catRelationsDbAdapter;
    public ChannelsDbAdapter channelsDbAdapter;
    public DatapointDbAdapter datapointDbAdapter;
    public FavRelationsDbAdapter favRelationsDbAdapter;
    public IconDbAdapter iconDbAdapter;
    public ExternalIconDbAdapter externalIconDbAdapter;
    public ProgramsDbAdapter programsDbAdapter;
    public RoomRelationsDbAdapter roomRelationsDbAdapter;
    public RoomsDbAdapter roomsDbAdapter;
    public VariableDbAdapter varsDbAdapter;
    public ProfileDbAdapter profilesDbAdapter;
    public WidgetDbAdapter widgetDbAdapter;
    public RemoteRelationsDbAdapter remoteRelationsDbAdapter;

    public CCUFavListsDbAdapter ccuFavListsAdapter;
    public CCUFavRelationsDbAdapter ccuFavRelationsDbAdapter;

    public NotificationsDbAdapter notificationDbAdapter;
    public ProtocolDbAdapter protocolDbAdapter;

    public LayerDbAdapter layerDbAdapter;
    public LayerItemDbAdapter layerItemDbAdapter;

    public GroupedItemSettingsDbAdapter groupedItemSettingsDbAdatper;

    private Dao<HMCommand, Integer> hmCommandDao;
    private Dao<HmSpeechCommand, Integer> hmSpeechCommandDao;
    private Dao<WebCam, Integer> webCamDao;
    private Dao<HMObjectSettings, Integer> hmOjectSettingsDao;

    public static DataBaseAdapterManager init(Context ctx) {
        return OpenHelperManager.getHelper(ctx.getApplicationContext(), DataBaseAdapterManager.class);
    }

    public DataBaseAdapterManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        getWritableDatabase();
    }

    private void initDbAdapters() {
        Timber.d("initDbAdapters()");

        dbAdapterList = new ArrayList<>();

        categoriesDbAdapter = new CategoriesDbAdapter(mDb);
        catRelationsDbAdapter = new CatRelationsDbAdapter(mDb);
        channelsDbAdapter = new ChannelsDbAdapter(mDb);
        datapointDbAdapter = new DatapointDbAdapter(mDb);
        favRelationsDbAdapter = new FavRelationsDbAdapter(mDb);
        iconDbAdapter = new IconDbAdapter(mDb);
        externalIconDbAdapter = new ExternalIconDbAdapter(mDb);
        programsDbAdapter = new ProgramsDbAdapter(mDb);
        roomRelationsDbAdapter = new RoomRelationsDbAdapter(mDb);
        roomsDbAdapter = new RoomsDbAdapter(mDb);
        varsDbAdapter = new VariableDbAdapter(mDb);
        profilesDbAdapter = new ProfileDbAdapter(mDb);
        widgetDbAdapter = new WidgetDbAdapter(mDb);
        remoteRelationsDbAdapter = new RemoteRelationsDbAdapter(mDb);
        ccuFavRelationsDbAdapter = new CCUFavRelationsDbAdapter(mDb);
        ccuFavListsAdapter = new CCUFavListsDbAdapter(mDb);
        notificationDbAdapter = new NotificationsDbAdapter(mDb);
        protocolDbAdapter = new ProtocolDbAdapter(mDb);
        layerDbAdapter = new LayerDbAdapter(mDb);
        layerItemDbAdapter = new LayerItemDbAdapter(mDb);
        groupedItemSettingsDbAdatper = new GroupedItemSettingsDbAdapter(mDb);

        dbAdapterList.add(categoriesDbAdapter);
        dbAdapterList.add(catRelationsDbAdapter);
        dbAdapterList.add(channelsDbAdapter);
        dbAdapterList.add(datapointDbAdapter);
        dbAdapterList.add(favRelationsDbAdapter);
        dbAdapterList.add(iconDbAdapter);
        dbAdapterList.add(externalIconDbAdapter);
        dbAdapterList.add(programsDbAdapter);
        dbAdapterList.add(roomRelationsDbAdapter);
        dbAdapterList.add(roomsDbAdapter);
        dbAdapterList.add(varsDbAdapter);
        dbAdapterList.add(profilesDbAdapter);
        dbAdapterList.add(widgetDbAdapter);
        dbAdapterList.add(ccuFavListsAdapter);
        dbAdapterList.add(ccuFavRelationsDbAdapter);
        dbAdapterList.add(notificationDbAdapter);
        dbAdapterList.add(protocolDbAdapter);
        dbAdapterList.add(layerDbAdapter);
        dbAdapterList.add(layerItemDbAdapter);
        dbAdapterList.add(groupedItemSettingsDbAdatper);

        hmCommandDao = null;
        hmSpeechCommandDao = null;
        webCamDao = null;
        hmOjectSettingsDao = null;
    }

    public void createDB() {
        onCreate(mDb);
    }

    public synchronized void deletePrefixFromTable(String table, int prefix) {
        deletePrefixFromTable(mDb, table, prefix);
    }

    public ArrayList<BaseDbAdapter> getDbAdapterList() {
        if (dbAdapterList == null) {
            initDbAdapters();
        }
        return dbAdapterList;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        mDb = db;
        initDbAdapters();
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        mDb = database;

        initDbAdapters();

        for (BaseDbAdapter adapter : dbAdapterList) {
            database.execSQL(adapter.getCreateTable());
        }

        createDaoTables();
    }

    private void createDaoTables() {
        try {
            Log.i(this.getClass().getName(), "onCreate");
            TableUtils.createTableIfNotExists(connectionSource, HMCommand.class);
            TableUtils.createTableIfNotExists(connectionSource, HmSpeechCommand.class);
            TableUtils.createTableIfNotExists(connectionSource, WebCam.class);
            TableUtils.createTableIfNotExists(connectionSource, HMObjectSettings.class);
        } catch (java.sql.SQLException e) {
            Log.e(this.getClass().getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        if (oldVersion <= 2) {
            createTable(database, new ProfileDbAdapter(database));
        }
        if (oldVersion <= 3) {
            createTable(database, new WidgetDbAdapter(database));
        }
        if (oldVersion <= 4) {
            if (!columnExists(database, "system_variables", "value_list")) {
                database.execSQL("ALTER TABLE " + "system_variables" + " ADD COLUMN " + "value_list" + " text");
            }
        }
        if (oldVersion == 5) {
            if (!columnExists(database, "widgets", "name")) {
                database.execSQL("ALTER TABLE " + "widgets" + " ADD COLUMN " + "name" + " text");
            }
        }
        if (oldVersion <= 6) {
            createTable(database, new CCUFavListsDbAdapter(database));
            createTable(database, new CCUFavRelationsDbAdapter(database));
        }

        if (oldVersion <= 7) {
            createTable(database, new NotificationsDbAdapter(database));
            createTable(database, new ProtocolDbAdapter(database));
        }

        if (oldVersion <= 8) {
            if (!columnExists(database, "channels", "address")) {
                database.execSQL("ALTER TABLE " + "channels" + " ADD COLUMN " + "address" + " text");
            }
        }
        if (oldVersion <= 12) {
            if (!columnExists(database, "channels", "isvisible")) {
                database.execSQL("ALTER TABLE " + "channels" + " ADD COLUMN " + "isvisible" + " integer");
            }
            if (!columnExists(database, "channels", "isoperate")) {
                database.execSQL("ALTER TABLE " + "channels" + " ADD COLUMN " + "isoperate" + " integer");
            }
            if (!columnExists(database, "system_variables", "isvisible")) {
                database.execSQL("ALTER TABLE " + "system_variables" + " ADD COLUMN " + "isvisible" + " integer");
            }
            if (!columnExists(database, "system_variables", "isoperate")) {
                database.execSQL("ALTER TABLE " + "system_variables" + " ADD COLUMN " + "isoperate" + " integer");
            }
            if (!columnExists(database, "programs", "isvisible")) {
                database.execSQL("ALTER TABLE " + "programs" + " ADD COLUMN " + "isvisible" + " integer");
            }
            if (!columnExists(database, "programs", "isoperate")) {
                database.execSQL("ALTER TABLE " + "programs" + " ADD COLUMN " + "isoperate" + " integer");
            }
        }
        if (oldVersion <= 13) {
            if (!columnExists(database, "system_variables", "value_name_0")) {
                database.execSQL("ALTER TABLE " + "system_variables" + " ADD COLUMN " + "value_name_0" + " text");
            }
            if (!columnExists(database, "system_variables", "value_name_1")) {
                database.execSQL("ALTER TABLE " + "system_variables" + " ADD COLUMN " + "value_name_1" + " text");
            }
        }

        if (oldVersion < 14) {
            createTable(database, new LayerDbAdapter(database));
            createTable(database, new LayerItemDbAdapter(database));
        }

        if (oldVersion < 15) {
            if (!columnExists(database, "layer_items", "name")) {
                database.execSQL("ALTER TABLE " + "layer_items" + " ADD COLUMN " + "name" + " text");
            }
        }
        if (oldVersion < 16) {
            if (!columnExists(database, "programs", "timestamp")) {
                database.execSQL("ALTER TABLE " + "programs" + " ADD COLUMN " + "timestamp" + " text");
            }
        }

        if (oldVersion < 18) {
            createDaoTables();
        }

        if (oldVersion == 19 || oldVersion == 20) {
            database.execSQL("DROP TABLE hmspeechcommand");
            database.execSQL("DROP TABLE hmcommand");
            createDaoTables();
        }

        if (oldVersion < 22) {
            if (!columnExists(database, "channels", "device_name")) {
                database.execSQL("ALTER TABLE " + "channels" + " ADD COLUMN " + "device_name" + " text");
            }
        }

        if (oldVersion < 23) {
            createDaoTables();
        }

        if (oldVersion < 25) {
            database.execSQL("ALTER TABLE " + "widgets" + " ADD COLUMN " + "control_mode" + " integer");
        }

        if (oldVersion < 27) {
            createDaoTables();
        }

        if (oldVersion < 28) {
            createTable(database, new GroupedItemSettingsDbAdapter(database));
        }

        if (oldVersion < 31) {
            if (tableExists(database, "sort_orders")) {
                database.execSQL("create table if not exists grouped_settings (combined text primary key, _id integer, "
                        + "group_id text not null, sort_order integer, hidden integer);");
                database.execSQL("INSERT INTO grouped_settings (combined, _id, group_id, sort_order)\n" +
                        "            SELECT combined, _id, group_id, sort_order FROM sort_orders;");
                database.execSQL("DROP TABLE sort_orders");
            }
        }

        if (oldVersion <= 32) {
            if (!columnExists(database, "hmobjectsettings", "hidden")) {
                database.execSQL("ALTER TABLE " + "hmobjectsettings" + " ADD COLUMN " + "hidden" + " integer");
            }
        }

        if(oldVersion < 35) {
            if (!columnExists(database, "datapoints", "timestamp")) {
                database.execSQL("ALTER TABLE " + "datapoints" + " ADD COLUMN " + "timestamp" + " text");
            }

            if (!columnExists(database, "system_variables", "timestamp")) {
                database.execSQL("ALTER TABLE " + "system_variables" + " ADD COLUMN " + "timestamp" + " text");
            }
        }
    }

    public void createTable(SQLiteDatabase db, BaseDbAdapter adapter) {
        db.execSQL(adapter.getCreateTable());
    }

    public void deletePrefixFromTable(SQLiteDatabase db, String table, int prefix) {
        db.delete(table, BaseDbAdapter.getPrefixCondition(prefix), null);
    }

    public static boolean columnExists(SQLiteDatabase db, String table, String column) {
        ;
        Cursor c = db.rawQuery("SELECT * FROM " + table + " LIMIT 0,1", null);
        boolean missingColum = c.getColumnIndex(column) == -1;
        c.close();
        if (missingColum) {
            return false;
        }
        return true;
    }

    public static boolean tableExists(SQLiteDatabase db, String name) {
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + name + "'", null);
        boolean hasTable = c.getCount() == 1;
        c.close();
        return hasTable;
    }

    public Dao<HMCommand, Integer> getCommandDao() throws java.sql.SQLException {
        if (hmCommandDao == null) {
            hmCommandDao = getDao(HMCommand.class);
        }
        return hmCommandDao;
    }

    public Dao<HmSpeechCommand, Integer> getSpeechCommandDao() throws java.sql.SQLException {
        if (hmSpeechCommandDao == null) {
            hmSpeechCommandDao = getDao(HmSpeechCommand.class);
        }
        return hmSpeechCommandDao;
    }

    public Dao<WebCam, Integer> getWebCamDao() throws SQLException {
        if (webCamDao == null) {
            webCamDao = getDao(WebCam.class);
        }
        return webCamDao;
    }

    public Dao<HMObjectSettings, Integer> getHmOjectSettingsDao() throws SQLException {
        if (hmOjectSettingsDao == null) {
            hmOjectSettingsDao = getDao(HMObjectSettings.class);
        }
        return hmOjectSettingsDao;
    }

    public static void releaseHelper() {
        OpenHelperManager.releaseHelper();
    }
}
