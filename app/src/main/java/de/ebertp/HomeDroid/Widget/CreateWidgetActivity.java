package de.ebertp.HomeDroid.Widget;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.Toast;


import java.util.List;

import de.ebertp.HomeDroid.DbAdapter.BaseDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.ChannelsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.ProgramsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.DataBaseAdapterManager;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.Util;
import de.ebertp.HomeDroid.ViewAdapter.CursorToObjectHelper;
import de.ebertp.HomeDroid.ViewAdapter.SimpleListAdapter;

public class CreateWidgetActivity extends ListActivity {

    private static final String TAG = "CreateWidgetActivity";
    private int mAppWidgetId;
    private DataBaseAdapterManager dbM;
    private int mSelectedType;
    private BaseDbAdapter adapter;
    private EditText searchTxt;
    private SimpleListAdapter listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (PreferenceHelper.isDarkTheme(this)) {
            setTheme(R.style.HomeDroidDark);
        } else {
            setTheme(R.style.HomeDroidLight);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_picker_list);
        dbM = HomeDroidApp.db();

        searchTxt = (EditText) findViewById(R.id.search_text);
        searchTxt.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            public void afterTextChanged(Editable s) {
                search(s.toString());
            }
        });

        if (PreferenceHelper.isChildProtectionOn(this)) {
            showChildProtectDialog();
        } else {
            showTypeDialog();
        }


    }

    private void showTypeDialog() {
        final CharSequence[] items = {getString(R.string.channels), getString(R.string.scripts),
                getString(R.string.variables)};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                mSelectedType = item;
                loadData();
            }
        });

        AlertDialog alert = builder.create();

        alert.setOnCancelListener(new OnCancelListener() {

            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                finish();

            }

        });

        alert.show();
    }

    private void showChildProtectDialog() {
        androidx.appcompat.app.AlertDialog.Builder b = new androidx.appcompat.app.AlertDialog.Builder(this, Util.getDialogTheme(this));
        b.setTitle(getString(R.string.password_quest));
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        b.setView(input);
        b.setCancelable(false);
        b.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (input.getText().toString().equals(PreferenceHelper.getChildProtectPW(CreateWidgetActivity.this))) {
                    dialog.dismiss();
                    showTypeDialog();
                } else {
                    Toast.makeText(CreateWidgetActivity.this,
                            CreateWidgetActivity.this.getString(R.string.password_wrong), Toast.LENGTH_SHORT).show();
                    showChildProtectDialog();
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

    private void loadData() {

        Cursor cursor = null;

        switch (mSelectedType) {
            case StatusWidgetProvider.TYPE_CHANNEL: {
                //hotfix for internal channels (index 0)
                adapter = dbM.channelsDbAdapter;
                cursor = dbM.channelsDbAdapter.fetchAllItemsFilterInternal(PreferenceHelper.getPrefix(this));
                break;
            }
            case StatusWidgetProvider.TYPE_SCRIPT: {
                adapter = dbM.programsDbAdapter;
                cursor = dbM.programsDbAdapter.fetchAllItemsFilterInvisible(PreferenceHelper.getPrefix(this));
                break;
            }
            case StatusWidgetProvider.TYPE_VARIABLE: {
                adapter = dbM.varsDbAdapter;
                cursor = dbM.varsDbAdapter.fetchAllItemsFilterInvisible(PreferenceHelper.getPrefix(this));
                break;
            }
            case StatusWidgetProvider.TYPE_LOCAL_FAVS: {
                promptForName(0, getResources().getString(R.string.quick_access));
                return;
            }
        }

        listAdapter = new SimpleListAdapter(this, getObjectFromCursor(cursor));
        setListAdapter(listAdapter);
    }

    public List<HMObject> getObjectFromCursor(Cursor cursor) {

        if (adapter instanceof ChannelsDbAdapter) {
            return CursorToObjectHelper.convertCursorToChannels(cursor);
        }

        if (adapter instanceof ProgramsDbAdapter) {
            return CursorToObjectHelper.convertCursorToPrograms(cursor);
        }

        return CursorToObjectHelper.convertCursorToVariables(cursor);
    }

    private void search(String text) {
        if (adapter != null && text != null) {
            Cursor cursor = adapter.searchByName(PreferenceHelper.getPrefix(this), text);
            listAdapter.setItems(getObjectFromCursor(cursor));
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        promptForName(listAdapter.getItem(position).getRowId(), listAdapter.getItem(position).getName());
    }

    private void promptForName(final int rowId, String name) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_widget_options, null);

        final Spinner spinner = (Spinner) dialogView.findViewById(R.id.spinner_control_mode);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.widget_control_modes, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        final EditText editTitle = (EditText) dialogView.findViewById(R.id.edit_title);
        editTitle.setText(name);

        alert.setTitle(R.string.options);
        alert.setView(dialogView);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String name = editTitle.getText().toString();
                int controlMode = spinner.getSelectedItemPosition();
                createWidget(rowId, name, mSelectedType, controlMode);
            }
        });

        alert.setNegativeButton(CreateWidgetActivity.this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        alert.show();

    }

    private void createWidget(int rowId, String name, int type, int controlMode) {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras == null) {
            Log.e(TAG, "Extras null");
        }

        mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        dbM.widgetDbAdapter.createItem(mAppWidgetId, rowId, type, name, controlMode);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.widget_status_layout_normal);

        appWidgetManager.updateAppWidget(mAppWidgetId, views);

        StatusWidgetProvider.updateAllWidgets(this);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }
}
