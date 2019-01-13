package de.ebertp.HomeDroid.Activities.Preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.EditTextPreference;
import android.preference.Preference;
import androidx.appcompat.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;


import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;

public class PasswordProtectDialog implements Preference.OnPreferenceClickListener, DialogInterface.OnDismissListener {

    Context ctx;
    Preference pref;

    public PasswordProtectDialog(Context ctx, Preference preference) {
        this.ctx = ctx;
        this.pref = preference;
    }

    public boolean onPreferenceClick(Preference preference) {
        AlertDialog.Builder b = new AlertDialog.Builder(ctx);
        b.setTitle(ctx.getString(R.string.password_quest));
        final EditText input = new EditText(ctx);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        b.setView(input);
        b.setCancelable(false);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (input.getText().toString().equals(PreferenceHelper.getChildProtectPW(ctx))) {

                } else {
                    if (pref instanceof EditTextPreference) {
                        ((EditTextPreference) pref).getDialog().cancel();
                    }
                    Toast.makeText(ctx, "Falsches Passwort", Toast.LENGTH_SHORT).show();
                }
            }
        });
        b.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (pref instanceof EditTextPreference) {
                    ((EditTextPreference) pref).getDialog().cancel();
                }
            }
        });
        b.create().show();
        return true;
    }


    public void onDismiss(DialogInterface dialog) {
        if (pref instanceof EditTextPreference) {
            ((EditTextPreference) pref).getDialog().cancel();
        }
    }


}
