package de.ebertp.HomeDroid.Activities.Preferences;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import de.ebertp.HomeDroid.Activities.ThemedDialogActivity;
import de.ebertp.HomeDroid.Communication.Control.ControlHelper;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.LicenceUtil;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;

public class LicenceActivity extends ThemedDialogActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.licence_prefs_layout);

        initButtons();

        findViewById(R.id.btn_check_licence).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LicenceUtil.checkLicence(LicenceActivity.this, true, true);
                initLicenceData();
            }
        });
    }

    @Override
    public void onResume() {
        initLicenceData();
        super.onResume();
    }

    private void initLicenceData() {
        TextView countView = (TextView) findViewById(R.id.commandCount);
        countView.setText(this.getString(R.string.commands_sent) + ": " + PreferenceHelper.getCommandCount(this));

        if (PreferenceHelper.isUnlocked(this)) {
            findViewById(R.id.commandsLeft).setVisibility(View.GONE);
            findViewById(R.id.unlock_info).setVisibility(View.GONE);
            findViewById(R.id.btn_check_licence).setVisibility(View.GONE);
        } else {
            findViewById(R.id.btn_check_licence).setVisibility(View.VISIBLE);
            findViewById(R.id.commandsLeft).setVisibility(View.VISIBLE);

            long left = Math.max(0,
                    ControlHelper.UNLOCK_COMMAND_LIMIT - PreferenceHelper.getCommandCount(this));

            TextView leftView = (TextView) findViewById(R.id.commandsLeft);
            leftView.setText(this.getString(R.string.commands_left) + ": " + left + "/" + ControlHelper.UNLOCK_COMMAND_LIMIT);
        }

        TextView licenceState = (TextView) findViewById(R.id.licenceState);
        if (PreferenceHelper.isUnlocked(this)) {
            licenceState.setText(this.getString(R.string.licence_state) + ": " + this.getString(R.string.licence_unlocked));
        } else {
            licenceState.setText(this.getString(R.string.licence_state) + ": " + this.getString(R.string.licence_locked));
        }
    }

    private void initButtons() {

        findViewById(R.id.button_donation_market).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                try {
                    Intent search = new Intent(Intent.ACTION_VIEW);
                    search.setData(Uri.parse("market://search?q=de.ebertp.HomeDroid.Donate"));
                    search.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(search);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), "Android Marketplace not supported by this Device, opening Web-Version",
                            Toast.LENGTH_LONG).show();
                    String url = "http://market.android.com/details?id=de.ebertp.HomeDroid.Donate";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            }
        });

        findViewById(R.id.button_donation_amazon).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                String url = "http://www.amazon.de/Philipp-Ebert-HomeDroid-Unlocker/dp/B00HFWN9Q8";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }
}
