package de.ebertp.HomeDroid.Activities.Preferences;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.ProfileDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.DataBaseAdapterManager;
import de.ebertp.HomeDroid.EventTracker;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.ProfileHelper;
import de.ebertp.HomeDroid.ViewAdapter.ProfileAdapter;


public class ProfilePreferencesActivity extends AppCompatActivity {

    private DataBaseAdapterManager dbM;
    private ProfileDbAdapter profilesDbAdapter;
    private Cursor profilesCursor;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (PreferenceHelper.isDarkTheme(this)) {
            setTheme(R.style.HomeDroidDark);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_list);

        if (PreferenceHelper.isDarkTheme(this)) {
            findViewById(R.id.content).setBackgroundColor(getResources().getColor(R.color.homedroid_primary));
        }

        dbM = HomeDroidApp.db();
        profilesDbAdapter = dbM.profilesDbAdapter;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initContent();
        initButtons();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventTracker.trackScreen(this, "Profiles");
    }

    private void initButtons() {
        Button addProfile = (Button) findViewById(R.id.add_profile_button);
        addProfile.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                showNewProfileDialog();
            }
        });

    }

    private void createFirstProfile() {
        ProfileHelper.createOrUpdateProfile(this, 0, this.getString(R.string.default_profile));
        Toast.makeText(this, this.getString(R.string.profile_saved) + " " + this.getString(R.string.default_profile), Toast.LENGTH_LONG).show();
    }

    private void initContent() {
        profilesCursor = profilesDbAdapter.getAllProfiles();

        if (profilesCursor.getCount() == 0) {
            createFirstProfile();
        }

        profilesCursor = profilesDbAdapter.getAllProfiles();

        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(new ProfileAdapter(this, profilesCursor));
        listView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showLoadProfileDialog(position);
            }
        });

        registerForContextMenu(listView);
    }


    protected void showLoadProfileDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(this.getString(R.string.activate_profile))
                .setPositiveButton(this.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        ProfileHelper.createOrUpdateProfile(ProfilePreferencesActivity.this,
                                PreferenceHelper.getPrefix(ProfilePreferencesActivity.this), null);

                        profilesCursor.moveToPosition(position);
                        int profileId = profilesCursor.getInt(profilesCursor.getColumnIndex(ProfileDbAdapter.KEY_ROWID));
                        ProfileHelper.loadProfile(ProfilePreferencesActivity.this, profileId);
                        initContent();
                    }
                })
                .setNegativeButton(this.getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    protected void showDeleteProfileDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(this.getString(R.string.delete_profile_question))
                .setPositiveButton(this.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        ProfileHelper.deleteProfile(ProfilePreferencesActivity.this, position);
                        if (profilesCursor.getCount() == 0) {
                            createFirstProfile();
                        } else {
                            if (PreferenceHelper.getPrefix(ProfilePreferencesActivity.this) == position) {
                                ProfileHelper.loadProfile(ProfilePreferencesActivity.this, getSmallestPrefix());
                            }
                        }
                        initContent();
                    }
                })
                .setNegativeButton(this.getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    protected void showNewProfileDialog() {
        if (profilesCursor.getCount() < 10) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            final EditText editText = new EditText(this);
            editText.setText(this.getResources().getString(R.string.profile) + " #" + (profilesCursor.getCount() + 1));

            String msg = this.getString(R.string.create_new_profile);
            builder.setView(editText);
            builder.setMessage(msg)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            int newPrefix = getSmallestUnusedPrefix();

                            dialog.dismiss();

                            ProfileHelper.createOrUpdateProfile(ProfilePreferencesActivity.this, newPrefix, editText.getText().toString());
                            ProfileHelper.createOrUpdateProfile(ProfilePreferencesActivity.this,
                                    PreferenceHelper.getPrefix(ProfilePreferencesActivity.this), null);

                            ProfileHelper.loadProfile(ProfilePreferencesActivity.this, newPrefix);

                            Toast.makeText(ProfilePreferencesActivity.this, ProfilePreferencesActivity.this.getString(R.string.profile_created), Toast.LENGTH_LONG).show();
                            initContent();
                        }
                    })
                    .setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            Toast.makeText(ProfilePreferencesActivity.this, ProfilePreferencesActivity.this.getString(R.string.max_number_of_profiles), Toast.LENGTH_LONG).show();
        }

    }

    protected void showRenameProfileDialog(final int profileId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText editText = new EditText(this);
        editText.setHint(this.getString(R.string.new_profile_name));

        String msg = this.getString(R.string.rename);
        builder.setView(editText);
        builder.setMessage(msg)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();

                        DataBaseAdapterManager dbM = HomeDroidApp.db();
                        ProfileDbAdapter profileDb = dbM.profilesDbAdapter;
                        profileDb.createOrUpdatePrefs(profileId, editText.getText().toString());
                        initContent();
                    }
                })
                .setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        android.view.MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_profile, menu);
        menu.setHeaderTitle(this.getString(R.string.profile_settings));
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e("", "bad menuInfo", e);
            return false;
        }

        long id = listView.getAdapter().getItemId(info.position);
        profilesCursor.moveToPosition((int) id);
        int profileId = profilesCursor.getInt(profilesCursor.getColumnIndex(ProfileDbAdapter.KEY_ROWID));

        switch (item.getItemId()) {
            case R.id.delete: {
                showDeleteProfileDialog(profileId);
                break;
            }
            case R.id.rename:
                showRenameProfileDialog(profileId);
                break;
            default:
                break;
        }

        return true;
    }

    private int getSmallestPrefix() {
        int min = 0;
        profilesCursor.moveToFirst();
        while (!profilesCursor.isAfterLast()) {
            min = Math.min(min, profilesCursor.getInt(0));
            profilesCursor.moveToNext();
        }
        return min;
    }

    private int getSmallestUnusedPrefix() {
        int max = 0;
        int maxNew;
        profilesCursor.moveToFirst();
        while (!profilesCursor.isAfterLast()) {
            maxNew = Math.max(max, profilesCursor.getInt(profilesCursor.getColumnIndex(ProfileDbAdapter.KEY_ROWID)));

            if (maxNew - max > 1) {
                return max + 1;
            }
            max = maxNew;
            profilesCursor.moveToNext();
        }
        return max + 1;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return true;
        }
    }
}
