package de.ebertp.HomeDroid.Communication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.ebertp.HomeDroid.Communication.XmlToDbParser.CgiFile;
import de.ebertp.HomeDroid.Connection.AuthHelper;
import de.ebertp.HomeDroid.Connection.BroadcastHelper;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import timber.log.Timber;

public class DbRefreshManager {

    private Context mContext;
    private XmlToDbParser mDbParser;
    private Handler mToastHandler;
    private List<Thread> parseThreads;

    public static final String EXTRA_SYNC_RES = "syncRes";

    public DbRefreshManager(Context ctx, Handler toastHandler) {
        mContext = ctx;
        mToastHandler = toastHandler;
        mDbParser = new XmlToDbParser(ctx, toastHandler);
    }

    private Thread getParseDatapointsThread() {
        return new Thread(new Runnable() {
            public void run() {
                mDbParser.parseStuff(CgiFile.statelist);
            }
        });
    }

    private Thread getParseProgramsThread() {
        return new Thread(new Runnable() {
            public void run() {
                mDbParser.parseStuff(CgiFile.programlist);
            }
        });
    }

    private Thread getParseVariablesThread() {
        return new Thread(new Runnable() {
            public void run() {
                mDbParser.parseStuff(CgiFile.sysvarlist);
            }
        });
    }

    private Thread getParseCCUFavsThread() {
        return new Thread(new Runnable() {
            public void run() {
                mDbParser.parseStuff(CgiFile.favoritelist);
            }
        });
    }

    private Thread getParseNotificationsThread() {
        return new Thread(new Runnable() {
            public void run() {
                mDbParser.parseStuff(CgiFile.systemNotification);
            }
        });
    }

    private Thread getParseProtocolThread() {
        return new Thread(new Runnable() {
            public void run() {
                mDbParser.parseStuff(CgiFile.protocol);
            }
        });
    }

    public void refreshSmall() {
        Timber.d("refreshSmall()");

        mDbParser = new XmlToDbParser(mContext, mToastHandler);
        AuthHelper.setHttpAuth(mContext);

        mDbParser.parseVersion();
        if (mDbParser.refreshSuccessful) {
            parseThreads = new ArrayList<>();
            parseThreads.add(getParseDatapointsThread());
            parseThreads.add(getParseProgramsThread());
            parseThreads.add(getParseVariablesThread());

            for (Thread thread : parseThreads) {
                if (!thread.isAlive()) {
                    thread.start();
                }
            }

            for (Thread thread : parseThreads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            parseThreads = new ArrayList<>();
            parseThreads.add(getParseCCUFavsThread());
            parseThreads.add(getParseNotificationsThread());

            for (Thread thread : parseThreads) {
                if (!thread.isAlive()) {
                    thread.start();
                }
            }

            for (Thread thread : parseThreads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            mDbParser.setCancelSync(false);
        } else {
            Timber.v("Connection to CCCU could not be established");
        }
    }

    public void refreshPeriod() {
        Timber.d("refreshPeriod()");

        mDbParser = new XmlToDbParser(mContext, mToastHandler);
        AuthHelper.setHttpAuth(mContext);

        mDbParser.parseVersion();
        if (mDbParser.refreshSuccessful) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            Boolean per_datapoints = prefs.getBoolean("per_datapoints", true);
            Boolean per_variables = prefs.getBoolean("per_variables", true);
            Boolean per_programs = prefs.getBoolean("per_programs", true);
            Boolean per_ccufavs = prefs.getBoolean("per_ccufavs", true);

            ArrayList<Thread> ts = new ArrayList<Thread>();

            Timber.i("--------------Parsing Period Refresh from XML------------");

            if (per_datapoints)
                ts.add(getParseDatapointsThread());
            if (per_variables)
                ts.add(getParseVariablesThread());
            if (per_programs)
                ts.add(getParseProgramsThread());

            for (Thread thread : ts) {
                if (!thread.isAlive()) {
                    thread.start();
                }
            }

            for (Thread thread : ts) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            ts = new ArrayList<>();
            if (per_ccufavs)
                ts.add(getParseCCUFavsThread());

            ts.add(getParseNotificationsThread());

            for (Thread thread : ts) {
                if (!thread.isAlive()) {
                    thread.start();
                }
            }

            for (Thread thread : ts) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Timber.i("--------------Parsing Period Refresh finished------------");
        }
    }

    public boolean isRefreshSuccessful() {
        return mDbParser.refreshSuccessful;
    }

    public class RefreshAllThread extends Thread {
        public void run() {
            if (PreferenceHelper.isUpdateServiceEnabled(mContext)) {
                sendRefreshProgress(R.string.update_stop);
            }

            AuthHelper.setHttpAuth(mContext);
            mDbParser.dropDb();
            mDbParser.createDb();
            if (isInterrupted()) {
                return;
            }

            Timber.i("--------------Parsing from XML------------");

            sendRefreshProgress(R.string.connecting);
            mDbParser.parseVersion();
            if (isInterrupted()) {
                return;
            }

            if (mDbParser.refreshSuccessful) {

                sendRefreshProgress(R.string.rooms);
                mDbParser.parseStuff(CgiFile.roomlist);
                if (isInterrupted()) {
                    return;
                }

                if (mDbParser.refreshSuccessful) {
                    sendRefreshProgress(R.string.channels);
                    mDbParser.parseStuff(CgiFile.devicelist);
                    if (isInterrupted()) {
                        return;
                    }

                    sendRefreshProgress(R.string.categories);
                    mDbParser.parseStuff(CgiFile.functionlist);
                    if (isInterrupted()) {
                        return;
                    }

                    sendRefreshProgress(R.string.datapoints);
                    mDbParser.parseStuff(CgiFile.statelist);
                    if (isInterrupted()) {
                        return;
                    }

                    sendRefreshProgress(R.string.scripts);
                    mDbParser.parseStuff(CgiFile.programlist);
                    if (isInterrupted()) {
                        return;
                    }

                    sendRefreshProgress(R.string.variables);
                    mDbParser.parseStuff(CgiFile.sysvarlist);
                    if (isInterrupted()) {
                        return;
                    }

                    sendRefreshProgress(R.string.favs);
                    mDbParser.parseStuff(CgiFile.favoritelist);
                    if (isInterrupted()) {
                        return;
                    }

                    sendRefreshProgress(R.string.notifications);
                    mDbParser.parseStuff(CgiFile.systemNotification);
                }
            }
            Timber.i("--------------Parsing finished------------");
            sendRefreshProgress(100);
        }
    }

    public void refreshAll() {
        Timber.d("refreshAll()");

        mDbParser = new XmlToDbParser(mContext, mToastHandler);
        Thread background = new RefreshAllThread();
        parseThreads = Collections.singletonList(background);
        background.start();

        try {
            background.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendRefreshProgress(int progress) {
        BroadcastHelper.sendSyncUpdateProgress(progress);
    }

    public void cancelSyncHard() {
        mDbParser.setCancelSync(true);
        mDbParser.refreshSuccessful = false;

        if (parseThreads != null) {
            for (Thread t : parseThreads) {
                t.interrupt();
            }
        }
    }
}
