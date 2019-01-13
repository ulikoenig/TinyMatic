package de.ebertp.HomeDroid.Activities.Listing.Fragments;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.sql.SQLException;
import java.util.List;

import de.ebertp.HomeDroid.Communication.Control.ControlHelper;
import de.ebertp.HomeDroid.EventTracker;
import de.ebertp.HomeDroid.Model.HMCommand;
import de.ebertp.HomeDroid.Model.HmSpeechCommand;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.ToastHandler;
import de.ebertp.HomeDroid.Utils.Util;


public class HistoryFragment extends CommandFragment {

    private List<HMCommand> mCommands;
    private HistoryFragment.CommandHistoryArrayAdapter mListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ListView listView = new ListView(getActivity());

        try {
            mCommands = HMCommand.getCommandsForCurrentPrefix(getActivity());

            mListAdapter =
                    new CommandHistoryArrayAdapter(getActivity(), R.layout.command_history_item_detailed, mCommands);
            listView.setAdapter(mListAdapter);

            registerForContextMenu(listView);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return listView;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventTracker.trackScreen(getActivity(), "History");
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.clear();

        if (Util.isSpeechSupported(getActivity())) {
            menu.add(R.string.add_speech_command);
        }

        menu.add(R.string.send_again);
       // menu.add(R.string.show_command);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        HMCommand command = mCommands.get(info.position);
        if (item.getTitle().equals(this.getResources().getString(R.string.add_speech_command))) {
            showCrudDialog(new HmSpeechCommand(command.getRowId(), command.getType(), command.getValue(), null));
            return true;
        } else if (item.getTitle().equals(getResources().getString(R.string.send_again))) {
            ControlHelper.sendOrder(getActivity(), command, new ToastHandler(getActivity()));
        } else if(item.getTitle().equals(getResources().getString(R.string.show_command))) {

            //TODO maybe finish this later


//             AlertDialog.Builder builder = new  AlertDialog.Builder(getActivity());
//            builder.setMessage(getResources().getString(R.string.activate))
//                    .setView(new EditText())
//                    .setCancelable(true)
//                    .setNeutralButton(getString(R.string.close), new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            dialog.dismiss();
//                        }
//                    });
//
//            builder.show();
        }

        return super.onContextItemSelected(item);
    }


    @Override
    public void refreshData() {

    }

    @Override
    public String getTitle() {
        return getResources().getString(R.string.history);
    }
}
