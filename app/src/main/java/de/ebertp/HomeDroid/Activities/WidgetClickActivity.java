package de.ebertp.HomeDroid.Activities;

import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import de.ebertp.HomeDroid.Activities.Drawer.ContentFragment;
import de.ebertp.HomeDroid.Activities.Listing.Fragments.ListDataFragmentVar;
import de.ebertp.HomeDroid.DbAdapter.DataBaseAdapterManager;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Model.HMChannel;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.Util;
import de.ebertp.HomeDroid.ViewAdapter.ListViewGenerator;
import de.ebertp.HomeDroid.Widget.StatusWidgetProvider;

public class WidgetClickActivity extends AppCompatActivity  implements ContentFragment.ContentFragmentCallback {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (PreferenceHelper.isDarkTheme(this)) {
            setTheme(R.style.DialogThemeDark);
        }
        super.onCreate(savedInstanceState);

        DataBaseAdapterManager dbM = HomeDroidApp.db();
        int iseId = getIntent().getExtras().getInt(Util.INTENT_ISE_ID);
        int widgetType = getIntent().getExtras().getInt(Util.INTENT_WIDGET_TYPE);

        switch (widgetType) {
            case StatusWidgetProvider.TYPE_CHANNEL:
            case StatusWidgetProvider.TYPE_VARIABLE:
            case StatusWidgetProvider.TYPE_SCRIPT: {
                HMObject reference = Util.getHmObject(iseId, widgetType, dbM);
                View v = new ListViewGenerator(this, this, R.layout.list_item, R.layout.list_item_value, true, PreferenceHelper.isDarkTheme(this)).getView((HMChannel) reference);
                v.performClick();
                break;
            }
            case StatusWidgetProvider.TYPE_LOCAL_FAVS: {
                setContentView(R.layout.activity_widget_click);
                final FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().add(R.id.container, new ListDataFragmentVar()).commit();
                Toast.makeText(this, "Quick access!", Toast.LENGTH_LONG).show();

                findViewById(R.id.top).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
                break;
            }
            default: {
                throw new IllegalStateException("Unknown widget type " + widgetType);
            }
        }

    }

    @Override
    public void setTitleOfFragment(String title) {
        // Not required
    }
}
