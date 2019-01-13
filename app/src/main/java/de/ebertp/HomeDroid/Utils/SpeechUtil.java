package de.ebertp.HomeDroid.Utils;


import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.ebertp.HomeDroid.Communication.Control.ControlHelper;
import de.ebertp.HomeDroid.Model.HMCommand;
import de.ebertp.HomeDroid.Model.HmSpeechCommand;
import de.ebertp.HomeDroid.R;

public class SpeechUtil {

    public static void onSpeedDataReceived(Context ctx, Intent data) {
        // Fill the list view with the strings the recognizer thought it could have heard
        ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

        try {
            List<HmSpeechCommand> commands = HmSpeechCommand.getCommandsForCurrentPrefix(ctx);

            for (HmSpeechCommand command : commands) {
                for (String match : matches) {
                    if (command.getCommand().equalsIgnoreCase(match)) {
                        ControlHelper.sendOrder(ctx,
                                new HMCommand(command.getRowId(), command.getType(), command.getValue(), new Date()),
                                new ToastHandler(ctx));
                        Toast.makeText(ctx, String.format(ctx.getResources().getString(R.string.speech_command_recognized), match), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }

            Toast.makeText(ctx,
                    String.format(ctx.getResources().getString(R.string.command_not_found), matches.get(0).toString()),
                    Toast.LENGTH_LONG).show();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Intent getSpeechRecognitionIntent() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        return intent;
    }

}
