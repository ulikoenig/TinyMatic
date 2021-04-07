package de.ebertp.HomeDroid.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.LicenceUtil;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;

public class DonationActivity extends ThemedDialogActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.donation);

        if (!PreferenceHelper.isUnlocked(this)) {
            LicenceUtil.checkLicence(this, false, true);
        }

        //getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        initButtons();
    }

    private void initButtons() {

        ((Button) findViewById(R.id.button_donation_market)).setOnClickListener(new OnClickListener() {

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

        ((Button) findViewById(R.id.button_donation_amazon)).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                String url = "http://www.amazon.de/Philipp-Ebert-HomeDroid-Unlocker/dp/B00HFWN9Q8";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

    }
}
