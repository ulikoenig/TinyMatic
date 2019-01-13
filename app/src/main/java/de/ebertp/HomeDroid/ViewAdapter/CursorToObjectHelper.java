package de.ebertp.HomeDroid.ViewAdapter;

import android.database.Cursor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Model.HMChannel;
import de.ebertp.HomeDroid.Model.HMLayer;
import de.ebertp.HomeDroid.Model.HMNotification;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.Model.HMObjectSettings;
import de.ebertp.HomeDroid.Model.HMProtocol;
import de.ebertp.HomeDroid.Model.HMRoom;
import de.ebertp.HomeDroid.Model.HMScript;
import de.ebertp.HomeDroid.Model.HMVariable;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.TranslateUtil;

public class CursorToObjectHelper {

    public static ArrayList<HMObject> convertCursorToChannels(Cursor c) {
        ArrayList<HMObject> hmObjects = new ArrayList<>();

        boolean detectUnrenamedChannels = PreferenceHelper.isDetectUnrenamedChannels(HomeDroidApp.getContext());

        if (c.moveToFirst()) {
            do {
                hmObjects.add(convertCursorToChannel(detectUnrenamedChannels, c));
            } while (c.moveToNext());

            Collections.sort(hmObjects);
        }

        de.ebertp.HomeDroid.Utils.Util.closeCursor(c);
        return hmObjects;
    }

    public static HMObject convertCursorToChannel(boolean detectUnrenamedChannels, Cursor c) {
        int rowId, channelIndex;
        String name = null, type, address, deviceName;
        boolean isVisible, isOperate;

        rowId = c.getInt(0);

        HMObjectSettings settings = HMObjectSettings.getSettingsForId(rowId);
        if (settings != null) {
            name = settings.getCustomName();
        }

        if (name == null || name.isEmpty()) {
            name = c.getString(c.getColumnIndex("name"));
        }

        type = c.getString(c.getColumnIndex("device_type"));
        channelIndex = c.getInt(c.getColumnIndex("c_index"));
        address = c.getString(c.getColumnIndex("address"));
        isVisible = c.getInt(c.getColumnIndex("isvisible")) != 0;
        isOperate = c.getInt(c.getColumnIndex("isoperate")) != 0;
        deviceName = c.getColumnIndex("device_name") == -1 ? null : c.getString(c.getColumnIndex("device_name"));

        if (detectUnrenamedChannels && deviceName != null && name.matches("^.+?:\\d*$")) {
            name = deviceName + "(" + channelIndex + ")";
        }
        HMChannel hmChannel = new HMChannel(rowId, address, channelIndex, name, type, isVisible, isOperate);

        if (!c.isNull((c.getColumnIndex("sort_order")))) {
            hmChannel.setSortOrder(c.getInt(c.getColumnIndex("sort_order")));
        }

        if (!c.isNull((c.getColumnIndex("hidden")))) {
            hmChannel.setHidden(c.getInt(c.getColumnIndex("hidden")));
        }

        return hmChannel;
    }

    public static ArrayList<HMObject> convertCursorToPrograms(Cursor c) {
        ArrayList<HMObject> hmObjects = new ArrayList<>();

        if (c.moveToFirst()) {
            do {
                hmObjects.add(convertCursorToProgram(c));
            } while (c.moveToNext());
        }
        Collections.sort(hmObjects);

        de.ebertp.HomeDroid.Utils.Util.closeCursor(c);

        return hmObjects;
    }

    public static HMObject convertCursorToProgram(Cursor c) {
        int rowId;
        String name = null, value, timestamp;
        boolean isVisible, isOperate;

        rowId = c.getInt(c.getColumnIndex("_id"));

        HMObjectSettings settings = HMObjectSettings.getSettingsForId(rowId);
        if (settings != null) {
            name = settings.getCustomName();
        }

        if (name == null || name.isEmpty()) {
            name = c.getString(c.getColumnIndex("name"));
        }

        value = c.getString(c.getColumnIndex("value"));
        isVisible = c.getInt(c.getColumnIndex("isvisible")) != 0;
        isOperate = c.getInt(c.getColumnIndex("isoperate")) != 0;
        timestamp = c.getString(c.getColumnIndex("timestamp"));


        HMScript hmScript = new HMScript(rowId, name, value, isVisible, isOperate, timestamp);

        if (!c.isNull((c.getColumnIndex("sort_order")))) {
            hmScript.setSortOrder(c.getInt(c.getColumnIndex("sort_order")));
        }

        if (!c.isNull((c.getColumnIndex("hidden")))) {
            hmScript.setHidden(c.getInt(c.getColumnIndex("hidden")));
        }

        return hmScript;
    }

    public static ArrayList<HMObject> convertCursorToNotifications(Cursor c) {
        ArrayList<HMObject> hmObjects = new ArrayList<>();

        int rowId;
        String name, type, timestamp;

        if (c.moveToFirst()) {
            do {
                rowId = c.getInt(c.getColumnIndex("_id"));
                name = c.getString(c.getColumnIndex("name"));
                type = c.getString(c.getColumnIndex("type"));
                timestamp = c.getString(c.getColumnIndex("timestamp"));
                hmObjects.add(new HMNotification(rowId, name, type, new Date(Long.parseLong(timestamp) * 1000)));
            } while (c.moveToNext());
        }
        Collections.sort(hmObjects);

        de.ebertp.HomeDroid.Utils.Util.closeCursor(c);

        return hmObjects;
    }

    public static ArrayList<HMObject> convertCursorToProtocol(Cursor c) {
        ArrayList<HMObject> hmObjects = new ArrayList<>();

        String name, value;
        long datetime;

        if (c.moveToFirst()) {
            do {
                name = c.getString(c.getColumnIndex("name"));
                value = c.getString(c.getColumnIndex("value"));
                datetime = c.getLong(c.getColumnIndex("datetime"));
                hmObjects.add(new HMProtocol(name, value, datetime));
            } while (c.moveToNext());
        }
        Collections.sort(hmObjects);

        de.ebertp.HomeDroid.Utils.Util.closeCursor(c);

        return hmObjects;
    }

    public static ArrayList<HMObject> convertCursorToVariables(Cursor c) {
        ArrayList<HMObject> hmObjects = new ArrayList<>();

        if (c.moveToFirst()) {
            do {
                hmObjects.add(convertCursorToVariable(c));
            } while (c.moveToNext());
        }
        Collections.sort(hmObjects);

        de.ebertp.HomeDroid.Utils.Util.closeCursor(c);
        return hmObjects;
    }

    public static HMObject convertCursorToVariable(Cursor c) {
        int rowId;
        String name = null, variable, value, value_list, unit, type, subtype, valueName0, valueName1, timestamp;
        int min, max;
        boolean isVisible, isOperate;

        rowId = c.getInt(c.getColumnIndex("_id"));

        HMObjectSettings setttings = HMObjectSettings.getSettingsForId(rowId);
        if (setttings != null) {
            name = setttings.getCustomName();
        }

        if (name == null || name.isEmpty()) {
            name = TranslateUtil.getTranslatedString(c.getString(c.getColumnIndex("name")));
        }

        variable = c.getString(c.getColumnIndex("variable"));
        value = TranslateUtil.getTranslatedString(c.getString(c.getColumnIndex("value")));
        value_list = c.getString(c.getColumnIndex("value_list"));
        unit = c.getString(c.getColumnIndex("unit"));
        type = c.getString(c.getColumnIndex("type"));
        subtype = c.getString(c.getColumnIndex("subtype"));
        min = c.getInt(c.getColumnIndex("min"));
        max = c.getInt(c.getColumnIndex("max"));
        isVisible = c.getInt(c.getColumnIndex("isvisible")) != 0;
        isOperate = c.getInt(c.getColumnIndex("isoperate")) != 0;
        valueName0 = TranslateUtil.getTranslatedString(c.getString(c.getColumnIndex("value_name_0")));
        valueName1 = TranslateUtil.getTranslatedString(c.getString(c.getColumnIndex("value_name_1")));
        timestamp = c.getString(c.getColumnIndex("timestamp"));

        HMVariable hmVariable = new HMVariable(rowId, name, variable, value, value_list, min, max, unit, type, subtype, isVisible, isOperate, valueName0, valueName1, timestamp);

        if (!c.isNull((c.getColumnIndex("sort_order")))) {
            hmVariable.setSortOrder(c.getInt(c.getColumnIndex("sort_order")));
        }

        if (!c.isNull((c.getColumnIndex("hidden")))) {
            hmVariable.setHidden(c.getInt(c.getColumnIndex("hidden")));
        }

        return hmVariable;
    }

    public static ArrayList<HMObject> mergeChanAndScriptFavs(Cursor favChanC, Cursor favScrC, Cursor favVarC) {
        ArrayList<HMObject> mergedFavs = new ArrayList<>();
        mergedFavs.addAll(convertCursorToChannels(favChanC));
        mergedFavs.addAll(convertCursorToPrograms(favScrC));
        mergedFavs.addAll(convertCursorToVariables(favVarC));
        return mergedFavs;
    }

    public static ArrayList<HMRoom> convertCursorToRooms(Cursor c) {
        ArrayList<HMRoom> hmObjects = new ArrayList<>();

        if (c.moveToFirst()) {
            do {
                hmObjects.add(convertCursorToRoom(c));
            } while (c.moveToNext());
        }

        de.ebertp.HomeDroid.Utils.Util.closeCursor(c);
        return hmObjects;
    }

    public static HMRoom convertCursorToRoom(Cursor c) {
        int rowId = c.getInt(c.getColumnIndex("_id"));
        String name = null;

        HMObjectSettings settings = HMObjectSettings.getSettingsForId(rowId);
        if (settings != null) {
            name = settings.getCustomName();
        }

        if (name == null || name.isEmpty()) {
            name = TranslateUtil.getTranslatedString(c.getString(c.getColumnIndex("title")));
        }

        String rawName = c.getString(c.getColumnIndex("title"));
        HMRoom room = new HMRoom(rowId, name, rawName);
        return room;
    }

    public static ArrayList<HMLayer> convertCursorToLayers(Cursor c) {

        int id;
        String background, name;

        ArrayList<HMLayer> hmObjects = new ArrayList<>();

        while (c.moveToNext()) {
            id = c.getInt(c.getColumnIndex("_id"));
            name = c.getString(c.getColumnIndex("name"));
            background = c.getString(c.getColumnIndex("background"));
            hmObjects.add(new HMLayer(id, name, background));
        }

        de.ebertp.HomeDroid.Utils.Util.closeCursor(c);

        return hmObjects;
    }


}
