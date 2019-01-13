package de.ebertp.HomeDroid.Utils;

import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.ViewAdapter.CursorToObjectHelper;
import timber.log.Timber;

public class UnsupportedUtil {

    public static String getDiagnostics(int channelId) {
        StringBuilder builder = new StringBuilder();

        builder.append("\n");
        builder.append("\n");
        builder.append("\n");

        builder.append("Diagnostic Data");
        builder.append("+++++++++++++++++++++++++++++++++++++++++\n");

        builder.append("Build ").append(Util.getVersionInfo());
        builder.append("\n");

        builder.append("Current channel\n");

        Cursor channel = HomeDroidApp.db().channelsDbAdapter.fetchItem(channelId);
        if (channel == null) {
            return null;
        }

        String deviceName = channel.getString(channel.getColumnIndex("device_name"));

        builder.append(cursorDataToString(getCursorData(channel)));
        builder.append("\n");
        channel.close();

        builder.append("All channels\n");
        builder.append("*****************START**********************");
        builder.append("\n");

        Cursor allChannels = HomeDroidApp.db().channelsDbAdapter.getChannelsByDeviceName(PrefixHelper.getPrefix(HomeDroidApp.getContext()), deviceName);
        if (allChannels == null || !allChannels.moveToFirst()) {
            return null;
        }

        Map<Integer, String> allChannelDate = new HashMap<>();
        do {
            allChannelDate.put(allChannels.getInt(allChannels.getColumnIndex("_id")), cursorDataToString(getCursorData(allChannels)));
        } while (allChannels.moveToNext());

        SortedSet<Integer> keys = new TreeSet<>(allChannelDate.keySet());

        for (Integer key : keys) {
            Timber.d("Adding channel");
            builder.append( allChannelDate.get(key));

            builder.append("-------------Datapoints---------------\n");

            Cursor datapoints = HomeDroidApp.db().datapointDbAdapter.fetchItemsByChannel(key);
            if (datapoints == null || !datapoints.moveToFirst()) {
                continue;
            }

            do {
                Timber.d("Adding datapoint");
                builder.append(cursorDataToString(getCursorData(datapoints)));
            } while (datapoints.moveToNext());
            datapoints.close();

            builder.append("---------------------------------------");
            builder.append("\n");
        }
        builder.append("****************END***********************");

        return builder.toString();
    }

    private static String cursorDataToString(List<String> cursorData) {
        StringBuilder builder = new StringBuilder();
        for (String cursorRow : cursorData) {
            builder.append(cursorRow);
        }
        builder.append("\n");
        return builder.toString();
    }

    private static List<String> getCursorData(Cursor cursor) {
        if (cursor == null) {
            return Collections.emptyList();
        }

        ArrayList<String> cursorData = new ArrayList<>();

        for (int i = 0; i < cursor.getColumnCount(); i++) {
            String data;
            if (cursor.isNull(i)) {
                data = "---";
            } else {
                data = cursor.getString(i);
            }

            String column_name = cursor.getColumnName(i);
            cursorData.add(column_name + "=" + data + (i == cursor.getCount() - 2 ? "" : ",  "));
        }

        return cursorData;
    }
}
