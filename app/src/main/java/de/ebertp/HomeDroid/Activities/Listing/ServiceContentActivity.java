package de.ebertp.HomeDroid.Activities.Listing;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;

import de.ebertp.HomeDroid.Activities.HelpActivity;
import de.ebertp.HomeDroid.Activities.Preferences.LicenceActivity;
import de.ebertp.HomeDroid.Activities.Preferences.NewPreferenceActivity;
import de.ebertp.HomeDroid.Activities.Preferences.ProfilePreferencesActivity;
import de.ebertp.HomeDroid.Communication.Sync.SyncJobIntentService;
import de.ebertp.HomeDroid.Communication.Utils;
import de.ebertp.HomeDroid.Connection.BroadcastHelper;
import de.ebertp.HomeDroid.DbAdapter.BaseDbAdapter;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.SpeechUtil;
import de.ebertp.HomeDroid.Utils.Util;

public abstract class ServiceContentActivity extends AppCompatActivity {

    boolean isReceiverRegistered;

    @Override
    protected void onResume() {
        if (HomeDroidApp.Instance().isAppInBackground()) {
            HomeDroidApp.Instance().setAppInBackground(false);

            if (PreferenceHelper.isSyncInForegroundOnly(this)) {
                SyncJobIntentService.enqueueSyncPeriod(this);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            handleImmersive();
        }

        refreshFooter();
        refreshSyncMenuItem();

        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(BroadcastHelper.REFRESH_UI));
            isReceiverRegistered = true;
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        if (isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
            isReceiverRegistered = false;
        }

        super.onPause();
    }

    protected Handler mHideHandler;

    final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hideSystemUi();
        }
    };

    private void handleImmersive() {
        if (PreferenceHelper.isImmersiveModeEnabled(this)) {
            hideSystemUi();
            mHideHandler = new Handler();

            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if (visibility == 0) {
                        // the navigation bar re-appears, let's hide it after 2 seconds
                        mHideHandler.postDelayed(mHideRunnable, 3000);
                    }
                }
            });
        }
        // immersive mode was just disabled
        else if (getWindow().getDecorView().getSystemUiVisibility() == hideSystemUIFlags) {
            showSystemUI();
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(null);
            getWindow().getDecorView().findViewById(android.R.id.content).invalidate();
        }

    }

    int hideSystemUIFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE;

    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(0);
    }

    public void hideSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(hideSystemUIFlags);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Util.REQUEST_VOICE_RECOGINTION && resultCode == RESULT_OK) {
            SpeechUtil.onSpeedDataReceived(this, data);

        } else if (resultCode == Util.REQUEST_RESULT_RESTART) {
            if (this.getParent() != null) {
                this.getParent().finish();
            }
            finish();
            Util.restart2(this);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    protected BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            refreshFooter();
            refreshData();
            refreshSyncMenuItem();
        }

    };

    protected abstract void refreshData();


    private MenuItem syncMenuItem;

    public boolean onCreateOptionsMenu(Menu menu) {
        if (PreferenceHelper.isChildProtectionOn(this)) {
            getMenuInflater().inflate(R.menu.options_child_protect, menu);
        } else {
            getMenuInflater().inflate(R.menu.options_main, menu);
        }

        if (PreferenceHelper.isUnlocked(this)) {
            menu.removeItem(R.id.unlock);
        }

        if (!Util.isSpeechSupported(this)) {
            menu.removeItem(R.id.speech);
        }

        return true;
    }

    private void refreshSyncMenuItem() {
        if (syncMenuItem != null) {
            if (SyncJobIntentService.isSyncRunning()) {
                syncMenuItem.setTitle(getString(R.string.sync_running));
                syncMenuItem.setIcon(R.drawable.ic_action_menu_sync_cancel);
            } else {
                syncMenuItem.setTitle(getString(R.string.refresh_db));
                syncMenuItem.setIcon(R.drawable.ic_action_menu_sync);
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mHideHandler != null) {
            mHideHandler.removeCallbacks(mHideRunnable);
            mHideHandler.postDelayed(mHideRunnable, 4000);
        }

        syncMenuItem = menu.findItem(R.id.refresh);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                final View view = findViewById(R.id.refresh);

                if (view != null) {
                    view.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (SyncJobIntentService.isSyncRunning()) {
                                showCancelSyncDialog();
                            } else {
                                PreferenceHelper.setFullSyncTried(ServiceContentActivity.this, true);
                                SyncJobIntentService.enqueueSyncAll(ServiceContentActivity.this);
                            }
                            return true;
                        }
                    });
                }
            }
        });

        refreshSyncMenuItem();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help:
                Intent intent = new Intent(this, HelpActivity.class);
                startActivity(intent);
                return true;
            case R.id.unlock:
                Intent i2 = new Intent(this, LicenceActivity.class);
                startActivity(i2);
                return true;
            case R.id.refresh:
                if (SyncJobIntentService.isSyncRunning()) {
                    showCancelSyncDialog();
                } else {
                    if (!PreferenceHelper.isFullSyncTried(ServiceContentActivity.this)) {
                        Toast.makeText(this, R.string.full_sync_tip, Toast.LENGTH_LONG).show();
                    }

                    SyncJobIntentService.enqueueSyncSmall(this);
                }
                return true;
            case R.id.configuration:
                Intent settingsActivity = new Intent(getBaseContext(), NewPreferenceActivity.class);
                startActivityForResult(settingsActivity, 0);
                return true;
            case R.id.profiles:
                Intent appstateActivity = new Intent(getBaseContext(), ProfilePreferencesActivity.class);
                startActivity(appstateActivity);
                return true;
            case R.id.protocol:
                Intent protocolActivity = new Intent(getBaseContext(), ListDataActivity.class);
                Bundle bun3 = new Bundle();
                bun3.putString(ListDataActivity.EXTRA_TYPE, ListDataActivity.TYPE_PROTOCOL);
                bun3.putInt(ListDataActivity.EXTRA_ID, PreferenceHelper.getPrefix(this) * BaseDbAdapter.PREFIX_OFFSET);
                bun3.putString(ListDataActivity.EXTRA_NAME, this.getString(R.string.protocol));
                protocolActivity.putExtras(bun3);
                startActivity(protocolActivity);
                return true;
            case R.id.speech:
                if (getSpeechCommandsSize() == 0) {
                    showSpeechCommandHint();
                } else {
                    startVoiceRecognitionActivity();
                }
                return true;
            case R.id.quit:
                showQuitDialog();
                return true;
            case R.id.vpn_settings:
                showVPNSettings();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showCancelSyncDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.refresh_cancel)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        BroadcastHelper.cancelSync();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showVPNSettings() {
        String action;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            action = android.provider.Settings.ACTION_VPN_SETTINGS;
        } else
            action = "android.net.vpn.SETTINGS";

        Intent intent = new Intent(action);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showSpeechCommandHint() {
        Toast.makeText(this, R.string.no_speech_commands_hint, Toast.LENGTH_LONG).show();
    }

    private long getSpeechCommandsSize() {
        try {
            return HomeDroidApp.db().getSpeechCommandDao().countOf();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Fire an intent to next the speech recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = SpeechUtil.getSpeechRecognitionIntent();
        startActivityForResult(intent, Util.REQUEST_VOICE_RECOGINTION);
    }


    protected void showQuitDialog() {

        if (!PreferenceHelper.isQuitDismissed(this)) {
            final View content = getLayoutInflater().inflate(R.layout.dialog_quit, null);

            new AlertDialog.Builder(this).setView(content)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {

                            if (((CheckBox) content.findViewById(R.id.check_show_again)).isChecked()) {
                                PreferenceHelper.setIsQuitDismissed(ServiceContentActivity.this, true);
                            }

                            quit();
                        }

                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            quit();
        }
    }

    protected void quit() {
        Utils.handleExit(this);

        finish();
        moveTaskToBack(true);
        // }

        Intent goHome = new Intent();
        goHome.setAction("android.intent.action.MAIN");
        goHome.addCategory("android.intent.category.HOME");
        startActivity(goHome);
    }

    protected void refreshFooter() {

        if (findViewById(R.id.syncTime) != null) {

            if (SyncJobIntentService.isSyncRunning()) {
                findViewById(R.id.syncTime).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.syncTimeTitle)).setText(this.getString(R.string.db_refresh));

            } else {
                findViewById(R.id.syncTime).setVisibility(View.VISIBLE);

                String title = this.getString(R.string.lastSync);
                String time = PreferenceHelper.getLastSuccessfulSync(this);

                if (!PreferenceHelper.getSyncSuccessful(this)) {
                    // title = " [X] " + title;
                    // // time = "[X] "+time;
                    findViewById(R.id.imageview_sync_error).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.imageview_sync_error).setVisibility(View.GONE);
                }

                ((TextView) findViewById(R.id.syncTime)).setText(time);
                ((TextView) findViewById(R.id.syncTimeTitle)).setText(title);
            }
        }
    }

}
