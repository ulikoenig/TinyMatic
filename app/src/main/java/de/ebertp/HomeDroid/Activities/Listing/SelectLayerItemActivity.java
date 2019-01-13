package de.ebertp.HomeDroid.Activities.Listing;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

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

public class SelectLayerItemActivity extends ListActivity {

    private DataBaseAdapterManager dbM;
    private int type;
    private BaseDbAdapter adapter;
    private EditText searchTxt;
    private SimpleListAdapter listAdapter;

    public static final String INTENT_EXTRA_TYPE = "type";
    public static final String INTENT_EXTRA_ISE_ID = "iseId";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (PreferenceHelper.isDarkTheme(this)) {
            setTheme(R.style.DialogThemeDark);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_picker_list);
        dbM = HomeDroidApp.db();

        final CharSequence[] items = {this.getString(R.string.channels), this.getString(R.string.scripts),
                this.getString(R.string.variables)};

        type = getIntent().getExtras().getInt(INTENT_EXTRA_TYPE);
        loadData();

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
    }

    private void loadData() {

        Cursor cursor = null;

        switch (type) {
            case Util.TYPE_CHANNEL: {
                // hotfix for internal channels (index 0)
                adapter = dbM.channelsDbAdapter;
                cursor = dbM.channelsDbAdapter.fetchAllItemsFilterInternal(PreferenceHelper.getPrefix(this));
                break;
            }
            case Util.TYPE_SCRIPT: {
                adapter = dbM.programsDbAdapter;
                cursor = dbM.programsDbAdapter.fetchAllItemsFilterInvisible(PreferenceHelper.getPrefix(this));
                break;
            }
            case Util.TYPE_VARIABLE: {
                adapter = dbM.varsDbAdapter;
                cursor = dbM.varsDbAdapter.fetchAllItemsFilterInvisible(PreferenceHelper.getPrefix(this));
                break;
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
        Intent intent = new Intent();
        intent.putExtra(INTENT_EXTRA_ISE_ID, listAdapter.getItem(position).getRowId());
        intent.putExtra(INTENT_EXTRA_TYPE, type);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
