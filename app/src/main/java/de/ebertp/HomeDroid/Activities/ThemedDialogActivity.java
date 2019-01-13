package de.ebertp.HomeDroid.Activities;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;

import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;

public class ThemedDialogActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (PreferenceHelper.isDarkTheme(this)) {
            setTheme(R.style.DialogThemeDark);
        }
        super.onCreate(savedInstanceState);
    }
}
