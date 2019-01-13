package de.ebertp.HomeDroid.Activities.Listing.Fragments;


import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.sql.SQLException;
import java.util.List;

import de.ebertp.HomeDroid.EventTracker;
import de.ebertp.HomeDroid.Model.HmSpeechCommand;
import de.ebertp.HomeDroid.R;

public class SpeechCommandsFragment extends CommandFragment {

    private List<HmSpeechCommand> mSpeechCommandList;
    private ArrayAdapter<HmSpeechCommand> mListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ListView listView = new ListView(getActivity());

        try {
            mSpeechCommandList = HmSpeechCommand.getCommandsForCurrentPrefix(getActivity());

            mListAdapter = new ArrayAdapter<HmSpeechCommand>(getActivity(), android.R.layout.simple_list_item_1,
                    android.R.id.text1, mSpeechCommandList);
            // Assign adapter to ListView
            listView.setAdapter(mListAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    HmSpeechCommand speechCommand = mListAdapter.getItem(i);
                    showCrudDialog(speechCommand);
                }
            });

            registerForContextMenu(listView);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return listView;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventTracker.trackScreen(getActivity(), "Speech_Commands");
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.clear();
        menu.add(R.string.delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        if (item.getTitle().equals(this.getResources().getString(R.string.delete))) {
            HmSpeechCommand command = mSpeechCommandList.get(info.position);
            deleteSpeechCommand(command);
            return true;
        }

        return super.onContextItemSelected(item);
    }

    private void deleteSpeechCommand(HmSpeechCommand command) {
        try {
            mSpeechCommandDao.delete(command);
            refreshData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getTitle() {
        return getResources().getString(R.string.speech_commands);
    }

    @Override
    public void refreshData() {
        if (getView() != null) {
            try {
                mSpeechCommandList.clear();
                mSpeechCommandList.addAll(HmSpeechCommand.getCommandsForCurrentPrefix(getActivity()));
                mListAdapter.notifyDataSetChanged();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.options_speech, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.add_speech_command) {
            showCommandHistoryDialog();
        }

        return super.onOptionsItemSelected(item);
    }


}
