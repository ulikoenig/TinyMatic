package de.ebertp.HomeDroid.Backup;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.ebertp.HomeDroid.Activities.Drawer.MainActivity;
import de.ebertp.HomeDroid.DbAdapter.DataBaseAdapterManager;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.Util;

public class LegacyBackupHelper {

    public String dbPath;
    public String appPath;
    private String prefPath;

    private Context mContext;
    private final DataBaseAdapterManager mDataBaseAdapterManager;

    public LegacyBackupHelper(Context context) {
        mContext = context;
        mDataBaseAdapterManager = HomeDroidApp.db();
        appPath = "/data/" + mContext.getPackageName();
        dbPath = appPath + "/databases/" + DataBaseAdapterManager.DATABASE_NAME;
        prefPath = appPath + "/shared_prefs/" + Util.getSharedPrefName(mContext);
    }

    public boolean exportAll() {

        if (Environment.getExternalStorageDirectory().canWrite()) {

            File sd = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/HomeDroidBackup/");
            sd.mkdirs();

            File sourceDb = new File(Environment.getDataDirectory(), dbPath);
            File backupDB = new File(sd, DataBaseAdapterManager.DATABASE_NAME);

            File sourcePrefs = new File(Environment.getDataDirectory(), prefPath);
            File backupPrefs = new File(sd, Util.SHARED_PREF_NAME);

            try {
                if (exportDatabase(sourceDb, backupDB) && exportDatabase(sourcePrefs, backupPrefs)) {
                    Toast.makeText(mContext, mContext.getString(R.string.backup_export_successful), Toast.LENGTH_LONG).show();
                    HomeDroidApp.Instance().initDbHelper();
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.backup_export_failed), Toast.LENGTH_LONG).show();
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.backup_card_error), Toast.LENGTH_LONG).show();
        }

        return true;
    }

    public boolean importAll() {
        if (Environment.getExternalStorageDirectory().canRead()) {
            File backupDir =
                    new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/HomeDroidBackup/");

            File destinationDB = new File(Environment.getDataDirectory(), dbPath);
            File backupDB = new File(backupDir, DataBaseAdapterManager.DATABASE_NAME);

            File backupPrefs = new File(backupDir, Util.SHARED_PREF_NAME);

            try {
                if (importDatabase(backupDB, destinationDB) && importUserPrefs(backupPrefs)) {

                    Toast.makeText(mContext, mContext.getString(R.string.backup_import_successful), Toast.LENGTH_LONG).show();

                    // Restart
                    AlarmManager alm = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                    alm.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                            PendingIntent.getActivity(mContext, 0, new Intent(mContext, MainActivity.class), 0));

                    android.os.Process.sendSignal(android.os.Process.myPid(), android.os.Process.SIGNAL_KILL);
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.backup_import_failed), Toast.LENGTH_LONG).show();
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.backup_card_error), Toast.LENGTH_LONG).show();
        }

        return true;
    }


    public boolean exportDatabase(File source, File backup) throws IOException {
        Log.i(this.getClass().getName(), "Exporting from " + source + " to " + backup);

        if (source.exists()) {
            mDataBaseAdapterManager.close();
            FileChannel src = new FileInputStream(source).getChannel();
            FileChannel dst = new FileOutputStream(backup).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();

            Log.i(this.getClass().getName(), "Database Export finished successfully");
            return true;
        } else {
            Log.i(this.getClass().getName(), "Database not found, URI:" + dbPath);
        }
        return true;
    }

    /**
     * Copies the database file at the specified location over the current internal application database.
     */
    public boolean importDatabase(File backup, File destination) throws IOException {
        // Close the SQLiteOpenHelper so it will commit the created empty
        // database to internal storage.
        mDataBaseAdapterManager.getWritableDatabase().close();

        Log.i(this.getClass().getName(), "Importing from " + backup + " to " + destination);

        if (backup.exists()) {
            copyFile(new FileInputStream(backup), new FileOutputStream(destination));
            mDataBaseAdapterManager.close();
            Log.i(this.getClass().getName(), "Database Import finished successfully");

            SQLiteDatabase newDb = mDataBaseAdapterManager.getReadableDatabase();
            Log.i(this.getClass().getName(), "DB version " + newDb.getVersion());
            newDb.close();
        } else {
            Log.i(this.getClass().getName(), "Database not found, URI:" + dbPath);
            return false;
        }

        return true;

    }

    public static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }

    }


    @SuppressLint("NewApi")
    public boolean importUserPrefs(File backup) {
        Log.i(this.getClass().getName(), "Importing Prefs from " + backup);
        String error = "";

        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

            Editor editor = sharedPreferences.edit();

            InputStream inputStream = new FileInputStream(backup);

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.parse(inputStream);
            Element root = doc.getDocumentElement();

            NodeList nodeList = root.getChildNodes();
            Node child;

            for (int i = 0; i < nodeList.getLength(); i++) {
                child = nodeList.item(i);

                if (child.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) child;

                    String type = element.getNodeName();
                    String name = element.getAttribute("name");

                    //		        System.out.println("BackupSqliteHelper.importUserPrefs()"+name+" / "+type+ " / "+element.getTextContent());

                    if (!name.equals("commandCount") && element.getFirstChild() != null) {
                        String value = element.getTextContent();

                        if (type.equals("string")) {
                            //		          String value =
                            editor.putString(name, value);
                        } else if (type.equals("boolean")) {
                            editor.putBoolean(name, value.equals("true"));
                        } else if (type.equals("int")) {
                            try {
                                editor.putInt(name, Integer.parseInt(value));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        } else if (type.equals("long")) {
                            try {
                                editor.putLong(name, Long.parseLong(value));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.e(this.getClass().getName(), "Prefs Import: Unknown type " + type);
                        }
                    }

                }

                editor.commit();
            }
            return true;

        } catch (FileNotFoundException e) {
            error = e.getMessage();
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            error = e.getMessage();
            e.printStackTrace();
        } catch (SAXException e) {
            error = e.getMessage();
            e.printStackTrace();
        } catch (IOException e) {
            error = e.getMessage();
            e.printStackTrace();
        }
        return false;
    }
}
