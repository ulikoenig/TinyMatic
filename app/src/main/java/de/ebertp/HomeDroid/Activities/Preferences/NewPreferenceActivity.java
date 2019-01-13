package de.ebertp.HomeDroid.Activities.Preferences;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import de.ebertp.HomeDroid.Communication.Sync.SyncJobIntentService;
import de.ebertp.HomeDroid.Connection.BroadcastHelper;
import de.ebertp.HomeDroid.Connection.IpAdressHelper;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.Util;
import timber.log.Timber;

import static de.ebertp.HomeDroid.Communication.DbRefreshManager.EXTRA_SYNC_RES;

public class NewPreferenceActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        if (PreferenceHelper.isDarkTheme(this)) {
            setTheme(R.style.HomeDroidDark);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                getWindow().setStatusBarColor(getResources().getColor(R.color.homedroid_primary));
            }
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configuration_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new NewPreferenceFragment()).commit();

        if (PreferenceHelper.isChildProtectionOn(this)) {
            showChildProtectDialog();
        }

        Button refreshAllButton = (Button) findViewById(R.id.refreshAllButton);
        refreshAllButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (SyncJobIntentService.isSyncRunning()) {
                    Toast.makeText(getBaseContext(), getString(R.string.wait_for_refresh), Toast.LENGTH_LONG).show();
                } else {
                    doFullSync();

                }
            }


        });
    }

    private void doFullSync() {
        String serverAdressWithoutLogin = IpAdressHelper.getServerAdress(this, true, true, false);
        final String serverMessage = getString(R.string.db_refresh) + "\n\n" + serverAdressWithoutLogin + "\n";

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(serverMessage);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setProgress(0);
        pd.setMax(100);
        pd.setCanceledOnTouchOutside(false);

        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Timber.d("Received %s", intent.getAction());

                if (intent.getAction().equals(BroadcastHelper.SYNC_UPDATE_PROGRESS)) {
                    int res = intent.getIntExtra(EXTRA_SYNC_RES, 0);
                    if (res == 100) {
                        try {
                            pd.dismiss();
                        } catch (Exception e) {
                            // nothing
                        }
                    } else {
                        pd.setMessage(serverMessage + "\n" + (getString(res)));
                        pd.setProgress(pd.getProgress() + 8);
                    }
                } else {
                    throw new IllegalArgumentException("Unknown Action " + intent.getAction());
                }
            }
        };

        pd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                BroadcastHelper.cancelSync();
                LocalBroadcastManager.getInstance(NewPreferenceActivity.this).unregisterReceiver(receiver);
            }
        });

        pd.show();

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(BroadcastHelper.SYNC_UPDATE_PROGRESS));
        SyncJobIntentService.enqueueSyncAll(this);
    }

    private void showChildProtectDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this, Util.getDialogTheme(this));
        b.setTitle(getString(R.string.password_quest));
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        b.setView(input);
        b.setCancelable(false);
        b.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (input.getText().toString().equals(PreferenceHelper.getChildProtectPW(NewPreferenceActivity.this))) {
                    dialog.dismiss();
                } else {
                    Toast.makeText(NewPreferenceActivity.this,
                            NewPreferenceActivity.this.getString(R.string.password_wrong), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
        b.setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        b.create().show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
