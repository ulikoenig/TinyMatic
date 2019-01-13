package de.ebertp.HomeDroid.Activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.ebertp.HomeDroid.Activities.Preferences.LicenceActivity;
import de.ebertp.HomeDroid.BuildConfig;
import de.ebertp.HomeDroid.EventTracker;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.Util;

public class HelpActivity extends ThemedDialogActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_help);

        initContent();
        initButtons();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventTracker.trackScreen(this, "Help");
    }

    private void initButtons() {
        findViewById(R.id.btn_help_center).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                // Set this up once when your application launches
//                Config config = new Config("tinymatic.uservoice.com");
//                config.setForumId(278592);
//                config.setShowContactUs(true);
//                UserVoice.init(config, HelpActivity.this);
//
//                // Call this wherever you want to launch UserVoice
//                UserVoice.launchUserVoice(HelpActivity.this);
                String url = "https://tinymatic.uservoice.com/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                EventTracker.trackScreen(HelpActivity.this, "Support_Center");
            }
        });

        findViewById(R.id.btn_licence).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(HelpActivity.this, LicenceActivity.class));
            }
        });

        findViewById(R.id.btn_changelog).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Util.getChangelog(HelpActivity.this).getFullLogDialog().show();
                EventTracker.trackScreen(HelpActivity.this, "Changelog");
            }
        });

        findViewById(R.id.btn_database).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                startActivity(new Intent(HelpActivity.this, AppState.class));
            }
        });


    }


    private void initContent() {
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(Util.getVersionInfo() + ", " + getString(R.string.display_author));
    }

}
