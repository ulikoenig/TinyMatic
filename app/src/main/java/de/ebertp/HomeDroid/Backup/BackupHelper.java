package de.ebertp.HomeDroid.Backup;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.ebertp.HomeDroid.Activities.Drawer.MainActivity;
import de.ebertp.HomeDroid.DbAdapter.DataBaseAdapterManager;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.Util;

public class BackupHelper {

    public String dbPath;
    public String appPath;
    private String prefPath;

    private Context context;
    private final DataBaseAdapterManager mDataBaseAdapterManager;

    private static String MIME_BINARY = "application/octet-stream";

    public BackupHelper(Context context) {
        this.context = context;
        mDataBaseAdapterManager = HomeDroidApp.db();
        appPath = "/data/" + this.context.getPackageName();
        dbPath = appPath + "/databases/" + DataBaseAdapterManager.DATABASE_NAME;
        prefPath = appPath + "/shared_prefs/" + Util.getSharedPrefName(this.context);
    }

    public boolean exportAll(DocumentFile folder) {
        File sourceDb = new File(Environment.getDataDirectory(), dbPath);
        DocumentFile oldBackupDB = folder.findFile(DataBaseAdapterManager.DATABASE_NAME);
        if (oldBackupDB != null) {
            oldBackupDB.delete();
        }
        DocumentFile backupDB = folder.createFile(MIME_BINARY, DataBaseAdapterManager.DATABASE_NAME);

        File sourcePrefs = new File(Environment.getDataDirectory(), prefPath);
        DocumentFile oldBackupPrefs = folder.findFile(Util.SHARED_PREF_NAME);
        if (oldBackupPrefs != null) {
            oldBackupPrefs.delete();
        }
        DocumentFile backupPrefs = folder.createFile(MIME_BINARY, Util.SHARED_PREF_NAME);

        try {
            if (exportDatabase(sourceDb, backupDB) && exportDatabase(sourcePrefs, backupPrefs)) {
                Toast.makeText(context, context.getString(R.string.backup_export_successful), Toast.LENGTH_LONG).show();
                HomeDroidApp.Instance().initDbHelper();
            } else {
                Toast.makeText(context, context.getString(R.string.backup_export_failed), Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean importAll(DocumentFile folder) {

        File destinationDB = new File(Environment.getDataDirectory(), dbPath);

        DocumentFile backupDB = folder.findFile(DataBaseAdapterManager.DATABASE_NAME);
        if (backupDB == null) {
            Toast.makeText(context, context.getString(R.string.backup_import_failed), Toast.LENGTH_LONG).show();
            return false;
        }

        DocumentFile backupPrefs = folder.findFile(Util.SHARED_PREF_NAME    );
        if (backupPrefs == null) {
            Toast.makeText(context, context.getString(R.string.backup_import_failed), Toast.LENGTH_LONG).show();
            return false;
        }

        try {
            if (importDatabase(backupDB, destinationDB) && importUserPrefs(backupPrefs)) {

                Toast.makeText(context, context.getString(R.string.backup_import_successful), Toast.LENGTH_LONG).show();

                // Restart
                AlarmManager alm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alm.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                        PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0));

                android.os.Process.sendSignal(android.os.Process.myPid(), android.os.Process.SIGNAL_KILL);
            } else {
                Toast.makeText(context, context.getString(R.string.backup_import_failed), Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public boolean exportDatabase(File source, DocumentFile backup) throws IOException {
        Log.i(this.getClass().getName(), "Exporting from " + source + " to " + backup);

        mDataBaseAdapterManager.close();

        try (InputStream in = new FileInputStream(source)) {
            try (OutputStream out = context.getContentResolver().openOutputStream(backup.getUri())) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
        return true;
    }

    /**
     * Copies the database file at the specified location over the current internal application database.
     */
    public boolean importDatabase(DocumentFile backup, File destination) throws IOException {
        Log.i(this.getClass().getName(), "Importing from " + backup + " to " + destination);
        // Close the SQLiteOpenHelper so it will commit the created empty
        // database to internal storage.
        mDataBaseAdapterManager.getWritableDatabase().close();


        try (InputStream in = context.getContentResolver().openInputStream(backup.getUri())) {
            try (OutputStream out = new FileOutputStream(destination)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
        return true;
    }

    @SuppressLint("NewApi")
    public boolean importUserPrefs(DocumentFile backup) {
        Log.i(this.getClass().getName(), "Importing Prefs from " + backup);

        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            Editor editor = sharedPreferences.edit();

            InputStream inputStream = context.getContentResolver().openInputStream(backup.getUri());

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

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
