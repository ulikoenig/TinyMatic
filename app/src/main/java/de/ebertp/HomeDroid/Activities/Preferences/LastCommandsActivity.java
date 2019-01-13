package de.ebertp.HomeDroid.Activities.Preferences;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;

import de.ebertp.HomeDroid.Activities.ThemedDialogActivity;
import de.ebertp.HomeDroid.EventTracker;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;

public class LastCommandsActivity extends ThemedDialogActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auto_prefs);
        setCommands();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventTracker.trackScreen(this, "Last_Commands");
    }

    private void setCommands() {
        LinearLayout content = (LinearLayout) findViewById(R.id.ll_auto_commands);

        content.removeAllViews();

        for (String s : PreferenceHelper.getLastCommands(this)) {
            EditText foo = new EditText(this);
            foo.setText(s);
            content.addView(foo);
        }
    }

}
