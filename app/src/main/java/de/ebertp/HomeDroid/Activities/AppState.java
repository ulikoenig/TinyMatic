package de.ebertp.HomeDroid.Activities;

import android.database.Cursor;
import android.widget.CheckBox;
import android.widget.TextView;

import de.ebertp.HomeDroid.Communication.Rpc.RpcForegroundService;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.CategoriesDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.ChannelsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.DatapointDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.ProgramsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.RoomsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.DataBaseAdapterManager;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;

public class AppState extends ThemedDialogActivity {

    private RoomsDbAdapter mRoomsHelper;
    private CategoriesDbAdapter mCategoriesHelper;
    private ChannelsDbAdapter mChannelsHelper;
    private DatapointDbAdapter mDatapointsHelper;
    private ProgramsDbAdapter mProgramsHelper;

    protected DataBaseAdapterManager dbM;

    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appstate);
        dbM = HomeDroidApp.db();
    }

    public void onResume() {
        super.onResume();
        getTableCount();
    }

    private void getTableCount() {
        TextView t;
        int count;
        Cursor c;

        CheckBox cb = (CheckBox) findViewById(R.id.SyncStatusContent);
        boolean syncStatus = PreferenceHelper.getSyncSuccessful(this);
        cb.setChecked(syncStatus);

        cb = (CheckBox) findViewById(R.id.RPCStatusContent);
        boolean rpcStatus = RpcForegroundService.isRunning();
        cb.setChecked(rpcStatus);

        t = (TextView) findViewById(R.id.RPCUpdatesCount);
        int writeCount = PreferenceHelper.getWriteCount(this);
        t.setText(Integer.toString(writeCount));

        t = (TextView) findViewById(R.id.LastSyncString);
        String lastSync = PreferenceHelper.getLastSync(this);
        t.setText(lastSync);

        t = (TextView) findViewById(R.id.RoomsContent);
        mRoomsHelper = dbM.roomsDbAdapter;
        c = mRoomsHelper.fetchAllItems(PreferenceHelper.getPrefix(this));
        count = c.getCount();
        de.ebertp.HomeDroid.Utils.Util.closeCursor(c);
        t.setText(Integer.toString(count));

        t = (TextView) findViewById(R.id.GewerkeContent);
        mCategoriesHelper = dbM.categoriesDbAdapter;
        c = mCategoriesHelper.fetchAllItems(PreferenceHelper.getPrefix(this));
        count = c.getCount();
        de.ebertp.HomeDroid.Utils.Util.closeCursor(c);
        t.setText(Integer.toString(count));

        t = (TextView) findViewById(R.id.ChannelsContent);
        mChannelsHelper = dbM.channelsDbAdapter;
        c = mChannelsHelper.fetchAllItems(PreferenceHelper.getPrefix(this));
        count = c.getCount();
        c.close();
        t.setText(Integer.toString(count));

        t = (TextView) findViewById(R.id.DatapointsContent);
        mDatapointsHelper = dbM.datapointDbAdapter;
        c = mDatapointsHelper.fetchAllItems(PreferenceHelper.getPrefix(this));
        count = c.getCount();
        de.ebertp.HomeDroid.Utils.Util.closeCursor(c);
        t.setText(Integer.toString(count));

        t = (TextView) findViewById(R.id.ProgramsContent);
        mProgramsHelper = dbM.programsDbAdapter;
        c = mProgramsHelper.fetchAllItems(PreferenceHelper.getPrefix(this));
        count = c.getCount();
        de.ebertp.HomeDroid.Utils.Util.closeCursor(c);
        t.setText(Integer.toString(count));

        t = (TextView) findViewById(R.id.VariablesContent);
        c = dbM.varsDbAdapter.fetchAllItems(PreferenceHelper.getPrefix(this));
        count = c.getCount();
        de.ebertp.HomeDroid.Utils.Util.closeCursor(c);
        t.setText(Integer.toString(count));
    }
}
