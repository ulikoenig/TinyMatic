package de.ebertp.HomeDroid.Activities.Listing.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Model.HMCommand;
import de.ebertp.HomeDroid.Model.HmSpeechCommand;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.PrefixHelper;
import de.ebertp.HomeDroid.Utils.SpeechUtil;
import de.ebertp.HomeDroid.Utils.Util;

public abstract class CommandFragment extends DataFragment {

    protected Dao<HmSpeechCommand, Integer> mSpeechCommandDao;
    private HmSpeechCommand tmpSpeechCommand;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mSpeechCommandDao = HomeDroidApp.db().getSpeechCommandDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void showCommandHistoryDialog() {
        try {
            final CommandHistoryArrayAdapter arrayAdapter =
                    new CommandHistoryArrayAdapter(getActivity(), R.layout.command_history_item_detailed,
                            HMCommand.getCommandsForCurrentPrefix(getActivity()));


             AlertDialog.Builder builder = new  AlertDialog.Builder(getActivity()).setTitle(R.string.select_command)
                    .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            HMCommand item = arrayAdapter.getItem(which);
                            showCrudDialog(new HmSpeechCommand(item.getRowId(), item.getType(), item.getValue(), null));
                            dialog.dismiss();
                        }
                    });
            builder.show();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    protected void showCrudDialog(final HmSpeechCommand command) {
        View content = getActivity().getLayoutInflater().inflate(R.layout.crud_speech_command, null);

        final EditText editCommand = ((EditText) content.findViewById(R.id.edit_command));
        final EditText editValue = ((EditText) content.findViewById(R.id.edit_value));

        if (command.getCommand() != null) {
            editCommand.setText(command.getCommand());
        }

        editValue.setText(command.getValue());
        if (command.getType() == HMCommand.TYPE_SCRIPT) {
            editValue.setVisibility(View.GONE);
            editValue.setText("1");
            content.findViewById(R.id.edit_value_label).setVisibility(View.GONE);
        }

        String title = HMCommand.getObjectName(getActivity(), command.getRowId(), command.getType());
        if (command.getType() == HMCommand.TYPE_DATAPOINT) {
            title += " (" + PrefixHelper.removePrefix(getActivity(), command.getRowId()) + ")";
        }


        final AlertDialog dialog = new  AlertDialog.Builder(getActivity()).setView(content)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (editCommand.getText().toString().equals("") || editValue.getText().toString().equals("")) {
                            Toast.makeText(getActivity(), R.string.imcomplete_input, Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            try {
                                HmSpeechCommand speechCommand =
                                        new HmSpeechCommand(command.getRowId(), command.getType(),
                                                editValue.getText().toString(), editCommand.getText().toString());

                                if (command.getId() != null) {
                                    speechCommand.setId(command.getId());
                                }

                                try {
                                    mSpeechCommandDao.createOrUpdate(speechCommand);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }

                                refreshData();
                                dialog.dismiss();

                            } catch (NumberFormatException e) {
                                Toast.makeText(getActivity(), R.string.invalid_input, Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setTitle(title)
                .create();

        ImageView iconSpeech = (ImageView) content.findViewById(R.id.button_speech);
        Util.setIconColor(PreferenceHelper.isDarkTheme(getActivity()), iconSpeech);

        iconSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                tmpSpeechCommand =
                        new HmSpeechCommand(command.getRowId(), command.getType(), editValue.getText().toString(),
                                editCommand.getText().toString());

                startVoiceRecognitionActivity();
            }
        });

        dialog.show();
    }

    /**
     * Fire an intent to start the speech recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = SpeechUtil.getSpeechRecognitionIntent();
        startActivityForResult(intent, Util.REQUEST_VOICE_RECOGINTION);
    }

    protected class CommandHistoryArrayAdapter extends ArrayAdapter<HMCommand> {

        private final int mResource;
        private final SimpleDateFormat dateFormat;

        public CommandHistoryArrayAdapter(Context context, int resource, List<HMCommand> objects) {
            super(context, resource, objects);
            mResource = resource;
            dateFormat = new SimpleDateFormat(Util.DATE_FORMAT_LONG);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(mResource, parent, false);
            HMCommand command = getItem(position);

            String readableName = HMCommand.getObjectName(getContext(), command);

            if (command.getType() == HMCommand.TYPE_DATAPOINT) {
                readableName +=
                        " (" + Integer.toString(PrefixHelper.removePrefix(getActivity(), command.getRowId())) + ")";
            }

            ((TextView) rowView.findViewById(R.id.text_name)).setText(readableName);
            ((TextView) rowView.findViewById(R.id.text_value)).setText(
                    getResources().getString(R.string.set_to) + " " + command.getValue());
            ((TextView) rowView.findViewById(R.id.text_timestamp)).setText(dateFormat.format(command.getTimestamp()));

            return rowView;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Util.REQUEST_VOICE_RECOGINTION && resultCode == Activity.RESULT_OK) {
            onSpeedDataReceived(data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onSpeedDataReceived(Intent data) {
        // Fill the list view with the strings the recognizer thought it could have heard
        ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

        if (matches.size() > 0) {
            tmpSpeechCommand.setCommand(matches.get(0));
        }
        showCrudDialog(tmpSpeechCommand);
    }
}
