package de.ebertp.HomeDroid.Activities.Preferences;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;

import com.anggrayudi.storage.SimpleStorageHelper;
import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.ebertp.HomeDroid.Backup.BackupHelper;
import de.ebertp.HomeDroid.Backup.LegacyBackupHelper;
import de.ebertp.HomeDroid.Communication.Rpc.RpcForegroundService;
import de.ebertp.HomeDroid.Communication.Utils;
import de.ebertp.HomeDroid.Connection.IpAdressHelper;
import de.ebertp.HomeDroid.DbAdapter.DataBaseAdapterManager;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Location.GeoFencingManager;
import de.ebertp.HomeDroid.Model.HMDrawerItem;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.DrawerHelper;
import de.ebertp.HomeDroid.Utils.PermissionUtil;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.Util;
import de.ebertp.HomeDroid.ViewAdapter.ProgramSimpleListAdapter;
import de.ebertp.HomeDroid.Widget.StatusWidgetProvider;
import timber.log.Timber;

public class NewPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int REQUEST_PERMISSION_LOCATION_FIX = 0;
    private static final int REQUEST_PERMISSION_FILESYSTEM_EXPORT = 1;
    private static final int REQUEST_PERMISSION_FILESYSTEM_IMPORT = 2;
    private static final int REQUEST_PERMISSION_LOCATION_HOME_WIFI = 4;
    private static final int REQUEST_PERMISSION_LOCATION_HOME_WIFI_SELECT = 5;
    private static final int REQUEST_PERMISSION_LOCATION_WIFI_SWITCH = 6;
    private static final int REQUEST_PERMISSION_LOCATION_HOME = 7;

    private SimpleStorageHelper storageHelper = new SimpleStorageHelper(this);

    private EditTextPreference mServerAddressPreference;
    private Preference mSelectWifiPref;
    private BackupHelper mBackupHelper;
    private LegacyBackupHelper mLegacyBackupHelper;
    private CheckBoxPreference syncOnHomeWifiOnlyPref;
    private CheckBoxPreference autoSwitchModePref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.new_prefs);

        initUIPrefs();
        initExperimentalPrefs();
        initParentalPrefs();
        initNetworkPrefs();
        initUpdatePrefs();
        initBackupPrefs();
        initMainPrefs();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);

        // If the user has clicked on a preference screen, set up the screen
        if (preference instanceof PreferenceScreen) {
            setUpNestedScreen((PreferenceScreen) preference);
        }

        return false;
    }

    public void setUpNestedScreen(PreferenceScreen preferenceScreen) {
        final Dialog dialog = preferenceScreen.getDialog();

        if (dialog == null) {
            Timber.e("Weird edge case, giving up");
            return;
        }

        View listView = dialog.findViewById(android.R.id.list);
        if (listView == null) {
            Timber.e("Weird edge case, giving up");
            return;
        }

        View rootCandidate = (View) listView.getParent();
        if (!(rootCandidate instanceof LinearLayout)) {
            rootCandidate = (View) rootCandidate.getParent();
        }

        if (!(rootCandidate instanceof LinearLayout)) {
            Timber.e("Can not find ViewGroup, giving up");
            return;
        }

        LinearLayout root = (LinearLayout) rootCandidate;

        Toolbar bar = (Toolbar) LayoutInflater.from(getActivity()).inflate(R.layout.settings_toolbar, root, false);
        bar.setTitle(preferenceScreen.getTitle());
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        root.addView(bar, 0); // insert at top
    }


    private void initBackupPrefs() {
        mBackupHelper = new BackupHelper(getActivity());
        mLegacyBackupHelper = new LegacyBackupHelper(getActivity());

        Preference backupFolderPref = findPreference("backup_folder");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            PreferenceScreen backupPreferences = (PreferenceScreen) findPreference("backup_preferences");
            backupPreferences.removePreference(backupFolderPref);
        } else {
            backupFolderPref.setOnPreferenceClickListener(preference -> {
                storageHelper.setOnFolderSelected((requestCode, folder) -> {
                    PreferenceHelper.setBackupAddress(getContext(), folder.getUri().toString());
                    return null;
                });
                storageHelper.openFolderPicker();
                return true;
            });
        }


        Preference backupImport = findPreference("backup_import");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            backupImport.setSummary("");
        }

        backupImport.setOnPreferenceClickListener(preference -> {
            importBackup();
            return true;
        });

        Preference backupExport = findPreference("backup_export");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            backupExport.setSummary("");
        }

        backupExport.setOnPreferenceClickListener(preference -> {
            exportBackup();
            return true;
        });
    }

    private DocumentFile getBackupFolder() {
        try {
            String uri = PreferenceHelper.getBackupAddress(getContext());
            return DocumentFile.fromTreeUri(getContext(), Uri.parse(uri));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void importBackup() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !PermissionUtil.hasFilePermissions(getActivity())) {
            PermissionUtil.requestFilePermission(this, REQUEST_PERMISSION_FILESYSTEM_IMPORT);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), getDialogTheme());
        builder.setMessage(NewPreferenceFragment.this.getString(R.string.backup_import_quest))
                .setCancelable(true)
                .setPositiveButton(NewPreferenceFragment.this.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            mLegacyBackupHelper.importAll();
                        } else {
                            DocumentFile backupFolder = getBackupFolder();
                            if (backupFolder == null) {
                                Toast.makeText(getContext(), getContext().getString(R.string.backup_folder_error), Toast.LENGTH_LONG).show();
                            } else {
                                mBackupHelper.importAll(backupFolder);
                            }
                        }
                    }
                })
                .setNegativeButton(NewPreferenceFragment.this.getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    public void exportBackup() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !PermissionUtil.hasFilePermissions(getActivity())) {
            PermissionUtil.requestFilePermission(this, REQUEST_PERMISSION_FILESYSTEM_EXPORT);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), getDialogTheme());
        builder.setMessage(NewPreferenceFragment.this.getString(R.string.backup_export_quest))
                .setCancelable(true)
                .setPositiveButton(NewPreferenceFragment.this.getString(R.string.yes), (dialog, id) -> {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        mLegacyBackupHelper.exportAll();
                        Util.restart2(getActivity());
                    } else {
                        DocumentFile backupFolder = getBackupFolder();
                        if (backupFolder == null) {
                            Toast.makeText(getContext(), getContext().getString(R.string.backup_folder_error), Toast.LENGTH_LONG).show();
                        } else {
                            mBackupHelper.exportAll(backupFolder);
                            Util.restart2(getActivity());
                        }
                    }
                })
                .setNegativeButton(NewPreferenceFragment.this.getString(R.string.no), (dialog, id) -> dialog.cancel());
        builder.create().show();
    }

    private void initMainPrefs() {
        mServerAddressPreference = (EditTextPreference) getPreferenceScreen().findPreference("server");

        findPreference("resetSettings").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), getDialogTheme());
                builder.setMessage(R.string.yousure)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                long commandCount = PreferenceHelper.getCommandCount(getActivity());
                                boolean isUnlocked = PreferenceHelper.isUnlocked(getActivity());
                                int versionCOde = PreferenceHelper.getVersionCode(getActivity());

                                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().clear().commit();

                                PreferenceHelper.setCommandCount(getActivity(), commandCount);
                                PreferenceHelper.setDisclaimerAccepted(getActivity(), true);
                                PreferenceHelper.setIsUnlocked(getActivity(), isUnlocked);
                                PreferenceHelper.setVersionCode(getActivity(), versionCOde);
                                Util.getChangelog(getActivity()).skipLogDialog();

                                restartApp();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                builder.create().show();
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mServerAddressPreference.setSummary(PreferenceHelper.getServer(getActivity()));

        List<String> homeWifis = PreferenceHelper.getHomeWifi(getActivity());
        if (homeWifis != null && !homeWifis.isEmpty()) {
            String homemWifis = getHomeWifisAsString();
            if (homemWifis != null) {
                mSelectWifiPref.setSummary(homemWifis);
            }
        }
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        if (PreferenceHelper.isSyncOnHomeWifiOnly(getContext())) {
            updateHomeWifiConnectionIcon();
        }
    }

    private void updateHomeWifiConnectionIcon() {
        Preference selectWifi = findPreference("select_wifi");

        if (IpAdressHelper.isHomeWifiConnected(getContext())) {
            selectWifi.setIcon(R.drawable.metro_83);
        } else {
            selectWifi.setIcon(R.drawable.metro_41);
        }
    }

    private String getHomeWifisAsString() {
        List<String> homeWifis = PreferenceHelper.getHomeWifi(getActivity());

        if (homeWifis == null || homeWifis.isEmpty()) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < homeWifis.size(); i++) {
            stringBuilder.append(homeWifis.get(i).replace("\"", ""));

            if (i != homeWifis.size() - 1) {
                stringBuilder.append(", ");
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("server")) {
            mServerAddressPreference.setSummary(PreferenceHelper.getServer(getActivity()));
        }
    }

    private void initUIPrefs() {
        Preference resetFavs = findPreference("reset_favs");
        resetFavs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity(), getDialogTheme()).create();

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getActivity().getResources().getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                SQLiteDatabase myDB = getActivity().openOrCreateDatabase("data", 0, null);
                                myDB.execSQL("drop table if exists favrelations");
                                myDB.execSQL("create table if not exists favrelations (_id integer primary key, "
                                        + "room text not null);");
                                myDB.close();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getActivity().getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                alertDialog.setMessage(getResources().getString(R.string.yousure));
                alertDialog.show();
                return true;
            }

        });

        Preference resetIcons = findPreference("reset_icons");
        resetIcons.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity(), getDialogTheme()).create();

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getActivity().getResources().getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                DataBaseAdapterManager dBm = HomeDroidApp.db();
                                int prefix = PreferenceHelper.getPrefix(getActivity());
                                dBm.deletePrefixFromTable(dBm.iconDbAdapter.getTableName(), prefix);
                                dBm.deletePrefixFromTable(dBm.externalIconDbAdapter.getTableName(), prefix);
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getActivity().getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                alertDialog.setMessage(getResources().getString(R.string.yousure));
                alertDialog.show();
                return true;
            }

        });

        Preference legacyLayout = findPreference("legacy_layout");
        legacyLayout.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            public boolean onPreferenceChange(Preference arg0, Object arg1) {
                restartApp();
                return true;
            }
        });


        (findPreference("dark_theme")).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            public boolean onPreferenceChange(Preference arg0, Object arg1) {
                restartApp();
                return true;
            }
        });

        (findPreference("grid_break_limit")).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            public boolean onPreferenceChange(Preference arg0, Object arg1) {
                restartApp();
                return true;
            }
        });

        ListPreference widgetTextSizePreference = (ListPreference) findPreference("widgetTextSize");
        widgetTextSizePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            public boolean onPreferenceChange(Preference preference, Object newValue) {
                StatusWidgetProvider.updateAllWidgets(getContext());
                return true;
            }
        });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            getPreferenceScreen().removePreference((findPreference("is_immersive")));
        }

        getPreferenceScreen().removePreference((findPreference("layer_is_minimalmode")));

        Preference drawerItemsPref = findPreference("visibleDrawerItems");
        drawerItemsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                if (PreferenceHelper.isChildProtectionOn(getContext())) {
                    Toast.makeText(getActivity(), R.string.disable_child_protection, Toast.LENGTH_LONG).show();
                    return true;
                }

                final List<HMDrawerItem> allItems = DrawerHelper.getDrawerItems(getContext());
                List<Integer> hiddenItems = PreferenceHelper.getHiddenDrawerItems(getContext());

                CharSequence[] names = new CharSequence[allItems.size()];
                final boolean[] visibles = new boolean[allItems.size()];

                for (int i = 0; i < allItems.size(); i++) {
                    names[i] = getString(allItems.get(i).getNameRes());
                    visibles[i] = !hiddenItems.contains(allItems.get(i).getId());
                }

                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), getDialogTheme());
                builder.setMultiChoiceItems(names, visibles, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        visibles[which] = isChecked;
                    }
                });

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<Integer> hiddenItems = new ArrayList<>();
                        int defaultItemId = PreferenceHelper.getDefaultTab(getContext());

                        for (int i = 0; i < allItems.size(); i++) {
                            if (!visibles[i]) {
                                if (allItems.get(i).getId() == defaultItemId) {
                                    Toast.makeText(getContext(), R.string.error_hiding_default_item, Toast.LENGTH_LONG).show();
                                    return;
                                }
                                hiddenItems.add(allItems.get(i).getId());
                            }
                        }

                        PreferenceHelper.setHiddenDrawerItems(getContext(), hiddenItems);
                    }
                });

                builder.setNegativeButton(R.string.cancel, null);

                builder.create().show();
                return true;
            }
        });
    }

    private void initExperimentalPrefs() {
        Preference homeLocation = findPreference("homeLocation");
        homeLocation.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                storeLocation();
                return true;
            }
        });

        Preference enableLocation = findPreference("enableLocation");
        enableLocation.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, final Object o) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if ((boolean) o) {
                            GeoFencingManager.init();
                        } else {
                            GeoFencingManager.unregister();
                        }
                    }
                });
                return true;
            }
        });


        Preference startOnArrival = findPreference("startOnArrical");
        startOnArrival.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity(), getDialogTheme());

                ListView selectProgramListView = new ListView(getActivity());
                final ProgramSimpleListAdapter progAdapter = new ProgramSimpleListAdapter(getActivity());
                selectProgramListView.setAdapter(progAdapter);

                alertDialog.setView(selectProgramListView);
                final AlertDialog dialog = alertDialog.create();

                selectProgramListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Cursor c = (Cursor) progAdapter.getItem(position);
                        int progId = c.getInt(c.getColumnIndex("_id"));
                        PreferenceHelper.setStartOnArrival(getActivity(), progId);

                        de.ebertp.HomeDroid.Utils.Util.closeCursor(c);
                        dialog.dismiss();
                    }
                });

                dialog.show();
                return true;
            }
        });

        Preference startOnDeparture = findPreference("startOnDeparture");
        startOnDeparture.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity(), getDialogTheme());

                ListView selectProgramListView = new ListView(getActivity());
                final ProgramSimpleListAdapter progAdapter = new ProgramSimpleListAdapter(getActivity());
                selectProgramListView.setAdapter(progAdapter);

                alertDialog.setView(selectProgramListView);
                final AlertDialog dialog = alertDialog.create();

                selectProgramListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Cursor c = (Cursor) progAdapter.getItem(position);
                        int progId = c.getInt(c.getColumnIndex("_id"));
                        PreferenceHelper.setStartOnDeparture(getActivity(), progId);

                        de.ebertp.HomeDroid.Utils.Util.closeCursor(c);
                        dialog.dismiss();
                    }
                });

                dialog.show();
                return true;
            }
        });

    }

    public void storeLocation() {
        if (!PermissionUtil.hasLocationPermissions(getActivity())) {
            PermissionUtil.requestLocationPermission(this, REQUEST_PERMISSION_LOCATION_HOME);
            return;
        }

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity(), getDialogTheme()).create();

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            @SuppressLint("MissingPermission")
            public void onClick(DialogInterface dialog, int which) {
                LocationServices.getFusedLocationProviderClient(getContext()).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            PreferenceHelper.setLocationHome(getContext(), location.getLatitude(), location.getLongitude());
                            GeoFencingManager.init();
                        } else {
                            Toast.makeText(getContext(), "No Location Data available", Toast.LENGTH_LONG).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "No Location Data available", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.setMessage(getResources().getString(R.string.yousure));
        alertDialog.show();
    }

    private void initParentalPrefs() {
        EditTextPreference exitPw = (EditTextPreference) getPreferenceScreen().findPreference("childProtectPW");
        exitPw.setOnPreferenceClickListener(new PasswordProtectDialog(getActivity(), exitPw));

        Preference childProtectState = findPreference("enableChildProtection");
        if (PreferenceHelper.isChildProtectionOn(getActivity())) {
            childProtectState.setSummary(getString(R.string.child_protect_state_on));
            exitPw.setEnabled(true);
        } else {
            childProtectState.setSummary(getString(R.string.child_protect_state_off));
            exitPw.setEnabled(false);
        }

        childProtectState.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder b = new AlertDialog.Builder(getActivity(), getDialogTheme());
                b.setTitle(getString(R.string.password_quest));
                final EditText input = new EditText(getActivity());
                b.setView(input);
                b.setCancelable(false);
                input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                b.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (input.getText().toString().equals(PreferenceHelper.getChildProtectPW(getActivity()))) {
                            PreferenceHelper.setChildProtectionOn(getActivity(), !PreferenceHelper.isChildProtectionOn(getActivity()));

                            // Restart
                            Util.restart2(getActivity());
                        } else {
                            Toast.makeText(getActivity(), getActivity().getString(R.string.password_wrong),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                b.setNegativeButton(getActivity().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                b.create().show();
                return true;
            }

        });

    }

    private int getDialogTheme() {
        return Util.getDialogTheme(getActivity());
    }

    private void initNetworkPrefs() {
        syncOnHomeWifiOnlyPref = (CheckBoxPreference) findPreference("syncOnHomeWifiOnly");
        syncOnHomeWifiOnlyPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            public boolean onPreferenceChange(Preference preference, final Object newValue) {
                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    return true;
                }

                if (((Boolean) newValue) && !PermissionUtil.hasLocationPermissions(getActivity())) {
                    PermissionUtil.requestLocationPermission(NewPreferenceFragment.this, REQUEST_PERMISSION_LOCATION_HOME_WIFI);
                    return false;
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (((Boolean) newValue)) {
                            HomeDroidApp.Instance().registerWifiReceiver();
                        } else {
                            HomeDroidApp.Instance().unregisterWifiReceiver();
                        }
                    }
                });
                return true;
            }
        });

        autoSwitchModePref = (CheckBoxPreference) findPreference("autoSwitchMode");
        autoSwitchModePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    return true;
                }

                if (((Boolean) newValue) && !PermissionUtil.hasLocationPermissions(getActivity())) {
                    PermissionUtil.requestLocationPermission(NewPreferenceFragment.this, REQUEST_PERMISSION_LOCATION_WIFI_SWITCH);
                    return false;
                }
                return true;
            }
        });

        mSelectWifiPref = findPreference("select_wifi");
        mSelectWifiPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !PermissionUtil.hasLocationPermissions(getActivity())) {
                    PermissionUtil.requestLocationPermission(NewPreferenceFragment.this, REQUEST_PERMISSION_LOCATION_HOME_WIFI_SELECT);
                    return false;
                }

                return selectWifi();
            }
        });

        findPreference("requestLocation").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                if (!PermissionUtil.hasLocationPermissions(getActivity())) {
                    PermissionUtil.requestLocationPermission(NewPreferenceFragment.this, REQUEST_PERMISSION_LOCATION_FIX);
                    return false;
                } else {
                    Toast.makeText(getActivity(), R.string.permission_granted, Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
    }

    private boolean selectWifi() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            showLegacyWifiDialog();
        } else {
            showWifiDialog();
        }
        return true;
    }

    private void showWifiDialog() {
        final List<String> oldWifiNames = PreferenceHelper.getHomeWifi(getContext());

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), getDialogTheme());

        StringBuilder flattened = new StringBuilder();

        for (String wifiName : oldWifiNames) {
            boolean isLast = oldWifiNames.indexOf(wifiName) == oldWifiNames.size() - 1;
            flattened.append(wifiName);
            if (!isLast) {
                flattened.append(",");
            }
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_wifi_input, null);
        EditText wifiNamesEditText = dialogView.findViewById(R.id.wifiNamesEditText);
        builder.setView(dialogView);

        if (flattened.length() >= 0) {
            wifiNamesEditText.setText(flattened);
        }

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = wifiNamesEditText.getText().toString();
                List<String> items = Arrays.asList(value.split(","));
                List<String> trimmed = new ArrayList();
                for (String item : items) {
                    trimmed.add(item.trim());
                }
                PreferenceHelper.setHomeWifi(getContext(), trimmed);
                mSelectWifiPref.setSummary(getHomeWifisAsString());
                updateHomeWifiConnectionIcon();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void showLegacyWifiDialog() {
        final WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(getActivity(), R.string.enable_wifi, Toast.LENGTH_LONG).show();
            return;
        }

        @SuppressLint("MissingPermission") final List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();

        final List<String> ssids = new ArrayList<>();

        for (WifiConfiguration config : configs) {
            if (!config.SSID.equals("")) {
                ssids.add(config.SSID);
            }
        }

        final boolean[] selecteds = new boolean[ssids.size()];
        String[] wifiNames = new String[ssids.size()];

        final List<String> oldWifiNames = PreferenceHelper.getHomeWifi(getContext());

        for (int i = 0; i < ssids.size(); i++) {
            String wifiName = ssids.get(i);
            wifiNames[i] = wifiName.replaceAll("\"", "");
            selecteds[i] = oldWifiNames.contains(wifiName);
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), getDialogTheme());
        builder.setMultiChoiceItems(wifiNames, selecteds, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                selecteds[which] = isChecked;
            }
        });

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<String> newSelectedWifis = new ArrayList<>();

                for (int i = 0; i < wifiNames.length; i++) {
                    if (selecteds[i]) {
                        newSelectedWifis.add(ssids.get(i));
                    }
                }

                PreferenceHelper.setHomeWifi(getContext(), newSelectedWifis);
                mSelectWifiPref.setSummary(getHomeWifisAsString());

                updateHomeWifiConnectionIcon();
            }
        });

        builder.create().show();
    }

    private void initUpdatePrefs() {
        ListPreference periodicUpdateInterval = (ListPreference) findPreference("periodicUpdateInterval");
        periodicUpdateInterval.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (PreferenceHelper.getPeriodicUpdateInterval(getActivity()) != newValue) {
                    Utils.resetPeriodUpdateTimer(getContext());
                }
                return true;
            }
        });

        CheckBoxPreference enableUpdateService = (CheckBoxPreference) findPreference("updateServiceEnabled");
        enableUpdateService.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            public boolean onPreferenceChange(Preference preference, final Object newValue) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    public void run() {
                        if (((Boolean) newValue)) {
                            getActivity().startService(new Intent(HomeDroidApp.getContext(), RpcForegroundService.class));
                        } else {
                            getActivity().stopService(new Intent(HomeDroidApp.getContext(), RpcForegroundService.class));
                        }
                    }
                });
                return true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION_FIX: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    Toast.makeText(getActivity(), getString(R.string.permission_granted), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
                }
                break;
            }
            case REQUEST_PERMISSION_LOCATION_HOME: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    storeLocation();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
                }
                break;
            }
            case REQUEST_PERMISSION_LOCATION_HOME_WIFI: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    HomeDroidApp.Instance().registerWifiReceiver();
                    syncOnHomeWifiOnlyPref.setChecked(true);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
                }
                break;
            }
            case REQUEST_PERMISSION_LOCATION_HOME_WIFI_SELECT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    selectWifi();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
                }
                break;
            }
            case REQUEST_PERMISSION_LOCATION_WIFI_SWITCH: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    autoSwitchModePref.setChecked(true);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
                }
                break;
            }
            case REQUEST_PERMISSION_FILESYSTEM_EXPORT: {
                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    exportBackup();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
                }
                break;
            }
            case REQUEST_PERMISSION_FILESYSTEM_IMPORT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    importBackup();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    public void restartApp() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                getActivity().setResult(Util.REQUEST_RESULT_RESTART);
                getActivity().finish();
            }
        }, 100);
    }
}
