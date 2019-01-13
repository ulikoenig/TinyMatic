package de.ebertp.HomeDroid.Communication;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.ebertp.HomeDroid.Communication.Control.HmDatapoint;
import de.ebertp.HomeDroid.Connection.InputStreamHelper;
import de.ebertp.HomeDroid.Connection.IpAdressHelper;
import de.ebertp.HomeDroid.DbAdapter.BaseDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.DataBaseAdapterManager;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Model.HMChannel;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.Model.HMScript;
import de.ebertp.HomeDroid.Model.HMVariable;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.IntToStringHelper;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.Util;
import timber.log.Timber;

import static de.ebertp.HomeDroid.Utils.Util.XML_API_MIN_VERSION;

public class XmlToDbParser {

    public static final int BUFFER_SIZE = 8 * 1024;

    private Context ctx;
    private String address;
    boolean refreshSuccessful = true;
    private Handler toastHandler;
    private boolean cancelSync = false;
    private float xmlApiVersion;
    private float XML_API_REMOTE_FIX = 100.16f; //TODO change version!

    public enum CgiFile {
        programlist,
        devicelist,
        roomlist,
        functionlist,
        sysvarlist,
        statelist,
        favoritelist,
        systemNotification,
        protocol
    }

    protected DataBaseAdapterManager dbM;
    private int fullPrefix;

    public XmlToDbParser(Context ctx, Handler toastHandler) {
        this.ctx = ctx;
        this.toastHandler = toastHandler;

        address = IpAdressHelper.getServerAdress(ctx, true, true, false) + "/addons/xmlapi/";
        dbM = HomeDroidApp.db();

        fullPrefix = PreferenceHelper.getPrefix(ctx) * BaseDbAdapter.PREFIX_OFFSET;
    }

    public void dropDb() {
        int prefix = PreferenceHelper.getPrefix(ctx);
        dbM.deletePrefixFromTable("relations", prefix);
        dbM.deletePrefixFromTable("rooms", prefix);
        dbM.deletePrefixFromTable("channels", prefix);
        dbM.deletePrefixFromTable("datapoints", prefix);
        dbM.deletePrefixFromTable("categories", prefix);
        dbM.deletePrefixFromTable("catrelations", prefix);
        dbM.deletePrefixFromTable("programs", prefix);
        dbM.deletePrefixFromTable("system_variables", prefix);
        dbM.deletePrefixFromTable("favlists", prefix);
        dbM.deletePrefixFromTable("ccufavrelations", prefix);
        dbM.deletePrefixFromTable("notifications", prefix);
        dbM.deletePrefixFromTable("protocol", prefix);
    }

    public void dropCompleteProfile() {
        int prefix = PreferenceHelper.getPrefix(ctx);

        for (BaseDbAdapter dbAdapter : dbM.getDbAdapterList()) {
            dbM.deletePrefixFromTable(dbAdapter.getTableName(), prefix);
        }
    }

    public void createDb() {
        dbM.createDB();
    }

    public void parseStuff(CgiFile fileType) {
        String fileName = fileType.toString();
        if (!isCancelSync()) {

            InputStream inputStream = null;
            HttpURLConnection conn = null;
            try {
                String name;
                int ise_id;
                int ise_id_room = 0;
                boolean isVisible, isOperate;
                String value;
                String timestamp;

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                try {
                    if (fileType == CgiFile.devicelist) {
                        conn = getUrlConnectionFromAddressUrl(fileName + ".cgi?show_internal=1&show_remote=1");
                    } else if (fileType == CgiFile.statelist) {
                        conn = getUrlConnectionFromAddressUrl(fileName + ".cgi?show_remote=1");
                    } else {
                        conn = getUrlConnectionFromAddressUrl(fileName + ".cgi");
                    }
                } catch (IOException e) {
                    return;
                }

                inputStream = InputStreamHelper.getInputStream(conn);

                xpp.setInput(new BufferedReader(new InputStreamReader(inputStream, "ISO-8859-1"), BUFFER_SIZE));

                Timber.i("Parsing %s", fileName);

                switch (fileType) {
                    case programlist:
                        ArrayList<HMScript> scripts = new ArrayList<HMScript>();

                        while (xpp.getEventType() != XmlPullParser.END_DOCUMENT && !isCancelSync()) {
                            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                                if (xpp.getName().equals("program")) {
                                    ise_id = Integer.parseInt(xpp.getAttributeValue(null, "id")) + fullPrefix;
                                    value = xpp.getAttributeValue(null, "active");
                                    name = xpp.getAttributeValue(null, "name");
                                    isVisible = Boolean.parseBoolean(xpp.getAttributeValue(null, "visible"));
                                    isOperate = Boolean.parseBoolean(xpp.getAttributeValue(null, "operate"));
                                    timestamp = xpp.getAttributeValue(null, "timestamp");

                                    scripts.add(
                                            new HMScript(ise_id, name, value, isVisible, isOperate, timestamp));
                                }
                            }
                            xpp.next();
                        }

                        dbM.programsDbAdapter.updateBulk(scripts);

                        break;
                    case roomlist:

                        while (xpp.getEventType() != XmlPullParser.END_DOCUMENT && !isCancelSync()) {
                            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                                if (xpp.getName().equals("room")) {
                                    name = xpp.getAttributeValue(null, "name");
                                    ise_id_room = Integer.parseInt(xpp.getAttributeValue(null, "ise_id"))
                                            + fullPrefix;

                                    dbM.roomsDbAdapter.createItem(ise_id_room, name);
                                }
                                if (xpp.getName().equals("channel")) {
                                    ise_id = Integer.parseInt(xpp.getAttributeValue(null, "ise_id"))
                                            + fullPrefix;
                                    dbM.roomRelationsDbAdapter.createItem(ise_id, ise_id_room);
                                }
                            }
                            xpp.next();
                        }
                        break;
                    case functionlist:
                        while (xpp.getEventType() != XmlPullParser.END_DOCUMENT && !isCancelSync()) {
                            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                                if (xpp.getName().equals("function")) {
                                    name = xpp.getAttributeValue(null, "name");
                                    ise_id_room = Integer.parseInt(xpp.getAttributeValue(null, "ise_id"))
                                            + fullPrefix;
                                    dbM.categoriesDbAdapter.createItem(ise_id_room, name);
                                }
                                if (xpp.getName().equals("channel")) {
                                    ise_id = Integer.parseInt(xpp.getAttributeValue(null, "ise_id"))
                                            + fullPrefix;
                                    dbM.catRelationsDbAdapter.createItem(ise_id, ise_id_room);
                                }
                            }
                            xpp.next();
                        }
                        break;
                    case devicelist:
                        String device_type = "";
                        String device_name = "";
                        String address;
                        int c_type;
                        while (xpp.getEventType() != XmlPullParser.END_DOCUMENT && !isCancelSync()) {
                            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                                if (xpp.getName().equals("device")) {
                                    device_type = xpp.getAttributeValue(null, "device_type");
                                    device_name = xpp.getAttributeValue(null, "name");
                                }
                                if (xpp.getName().equals("channel")) {
                                    name = xpp.getAttributeValue(0);
                                    c_type = Integer.parseInt(xpp.getAttributeValue(null, "index"));
                                    ise_id = Integer.parseInt(xpp.getAttributeValue(null, "ise_id"))
                                            + fullPrefix;
                                    address = xpp.getAttributeValue(null, "address");
                                    isVisible = Boolean.parseBoolean(xpp.getAttributeValue(null, "visible"));
                                    isOperate = Boolean.parseBoolean(xpp.getAttributeValue(null, "operate"));
                                    dbM.channelsDbAdapter.createItem(ise_id, name, device_type, c_type, address,
                                            isVisible, isOperate, device_name);
                                }
                            }
                            if (xpp.getEventType() == XmlPullParser.END_TAG) {
                                if (xpp.getName().equals("device")) {
                                }
                            }
                            xpp.next();
                        }
                        break;
                    case sysvarlist:
                        String variable,
                                value_list,
                                unit,
                                type,
                                subtype,
                                valueName0,
                                valueName1;
                        int min = -1,
                                max = -1;

                        ArrayList<HMVariable> variables = new ArrayList<>();

                        while (xpp.getEventType() != XmlPullParser.END_DOCUMENT && !isCancelSync()) {
                            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                                if (xpp.getName().equals("systemVariable")) {
                                    name = xpp.getAttributeValue(null, "name");
                                    variable = xpp.getAttributeValue(null, "variable");
                                    value = xpp.getAttributeValue(null, "value");
                                    value_list = xpp.getAttributeValue(null, "value_list");
                                    ise_id = Integer.parseInt(xpp.getAttributeValue(null, "ise_id"))
                                            + fullPrefix;

                                    type = xpp.getAttributeValue(null, "type");
                                    subtype = xpp.getAttributeValue(null, "subtype");

                                    isVisible = Boolean.parseBoolean(xpp.getAttributeValue(null, "visible"));

                                    if (type.equals("2")) {
                                        // dont parse min and max, could be boolean
                                    } else {
                                        min = IntToStringHelper.getIntFromString(
                                                xpp.getAttributeValue(null, "min"));
                                        max = IntToStringHelper.getIntFromString(
                                                xpp.getAttributeValue(null, "max"));
                                    }
                                    unit = IntToStringHelper.getNotNullString(
                                            xpp.getAttributeValue(null, "unit"));

                                    valueName0 = xpp.getAttributeValue(null, "value_name_0");
                                    valueName1 = xpp.getAttributeValue(null, "value_name_1");

                                    timestamp = xpp.getAttributeValue(null, "timestamp");

                                    variables.add(
                                            new HMVariable(ise_id, name, variable, value, value_list, min, max,
                                                    unit, type, subtype, isVisible, true, valueName0,
                                                    valueName1, timestamp));
                                }
                            }
                            xpp.next();
                        }

                        dbM.varsDbAdapter.updateBulk(variables);

                        break;
                    case statelist:
                        int channel_id = 0;
                        int value_type;
                        String channelName;
                        boolean gotChannel = false;

                        ArrayList<HmDatapoint> datapoints = new ArrayList<>();

                        while (xpp.getEventType() != XmlPullParser.END_DOCUMENT && !isCancelSync()) {
                            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                                if (xpp.getName().equals("channel")) {
                                    channel_id = Integer.parseInt(xpp.getAttributeValue(null, "ise_id"))
                                            + fullPrefix;
                                    channelName = xpp.getAttributeValue(null, "name");
                                    gotChannel = true;

                                    //workaround for legacy XML-API behaviour
                                    if (xmlApiVersion < XML_API_REMOTE_FIX && channel_id - fullPrefix >= 1012 && channel_id - fullPrefix <= 1210) {
                                        device_type = "HM-RC-12";
                                        c_type = 1;

                                        if (!xpp.getAttributeValue(null, "visible").equals("")) {
                                            isVisible = Boolean.parseBoolean(
                                                    xpp.getAttributeValue(null, "visible"));
                                            isOperate = Boolean.parseBoolean(
                                                    xpp.getAttributeValue(null, "operate"));

                                            dbM.channelsDbAdapter.replaceItem(channel_id, channelName,
                                                    device_type, c_type, "", isVisible, isOperate, channelName);
                                        }
                                    }
                                }
                                if (gotChannel) {
                                    if (xpp.getName().equals("datapoint")) {
                                        name = xpp.getAttributeValue(null, "name");
                                        type = xpp.getAttributeValue(null, "type");
                                        value = xpp.getAttributeValue(null, "value");

                                        ise_id = Integer.parseInt(xpp.getAttributeValue(null, "ise_id"))
                                                + fullPrefix;

                                        value_type = Integer.parseInt(xpp.getAttributeValue(null, "valuetype"));

                                        timestamp = xpp.getAttributeValue(null, "timestamp");
                                        datapoints.add(
                                                new HmDatapoint(ise_id, channel_id, value_type, name, type,
                                                        value, timestamp));
                                    }
                                }
                            }
                            if (xpp.getEventType() == XmlPullParser.END_TAG) {
                                if (xpp.getName().equals("channel")) {
                                    gotChannel = false;
                                }
                            }

                            xpp.next();
                        }

                        dbM.datapointDbAdapter.updateBulk(datapoints);

                        break;

                    case favoritelist:
                        int listId = 0;
                        String listName;

                        int favId;
                        String favType;
                        String favName;
                        String favNotCanUse;
                        boolean gotList = false;

                        dbM.deletePrefixFromTable(dbM.ccuFavListsAdapter.getTableName(),
                                PreferenceHelper.getPrefix(ctx));

                        while (xpp.getEventType() != XmlPullParser.END_DOCUMENT && !isCancelSync()) {
                            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                                if (xpp.getName().equals("favorite")) {
                                    listName = xpp.getAttributeValue(null, "name");
                                    listId = Integer.parseInt(xpp.getAttributeValue(null, "ise_id"));
                                    if (listId != 1408 && listId != 202 && listId != 203 && listId != 204) {
                                        if (!listName.contains("_USER")) {
                                            dbM.ccuFavListsAdapter.createItem(listId + fullPrefix, listName);
                                            gotList = true;
                                        }
                                    }
                                }
                                if (gotList) {
                                    if (xpp.getName().equals("channel")) {
                                        favId = Integer.parseInt(xpp.getAttributeValue(null, "ise_id"))
                                                + fullPrefix;
                                        favName = xpp.getAttributeValue(null, "name");
                                        favType = xpp.getAttributeValue(null, "type");
                                        favNotCanUse = xpp.getAttributeValue(null, "not_can_use");
                                        dbM.ccuFavRelationsDbAdapter.replaceItem(favId, favName, favType,
                                                favNotCanUse, listId + fullPrefix);
                                    }
                                }
                            }
                            if (xpp.getEventType() == XmlPullParser.END_TAG) {
                                if (xpp.getName().equals("favorite")) {
                                    gotList = false;
                                }
                            }

                            xpp.next();
                        }

                        break;
                    case systemNotification:
                        dbM.deletePrefixFromTable(dbM.notificationDbAdapter.getTableName(),
                                PreferenceHelper.getPrefix(ctx));

                        while (xpp.getEventType() != XmlPullParser.END_DOCUMENT && !isCancelSync()) {
                            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                                if (xpp.getName().equals("notification")) {
                                    name = xpp.getAttributeValue(null, "name");
                                    type = xpp.getAttributeValue(null, "type");
                                    timestamp = xpp.getAttributeValue(null, "timestamp");
                                    ise_id = Integer.parseInt(xpp.getAttributeValue(null, "ise_id"));
                                    dbM.notificationDbAdapter.createItem(ise_id + fullPrefix, name, type,
                                            timestamp);
                                }
                            }
                            xpp.next();
                        }
                        break;
                    case protocol:
                        long milis;

                        dbM.deletePrefixFromTable(dbM.protocolDbAdapter.getTableName(),
                                PreferenceHelper.getPrefix(ctx));
                        SimpleDateFormat dateFormat =
                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

                        while (xpp.getEventType() != XmlPullParser.END_DOCUMENT && !isCancelSync()) {
                            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                                if (xpp.getName().equals("row")) {
                                    name = xpp.getAttributeValue(null, "names");
                                    value = xpp.getAttributeValue(null, "values");
                                    milis = dateFormat.parse(xpp.getAttributeValue(null, "datetime")).getTime();
                                    dbM.protocolDbAdapter.createItem(fullPrefix + 1, name, value, milis);
                                }
                            }
                            xpp.next();
                        }

                        break;
                }
            } catch (Throwable t) {
                refreshSuccessful = false;
                handleThrowableInformation(t, "While parsing " + fileName);
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (conn != null) {
                        conn.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void parseState(String channelId) {
        if (!isCancelSync()) {

            InputStream inputStream = null;
            HttpURLConnection conn = null;
            try {
                String name;
                int ise_id;
                String value;
                String type;

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                try {
                    conn = getUrlConnectionFromAddressUrl("state.cgi?channel_id=" + Util.removePrefix(ctx, channelId));
                } catch (IOException e) {
                    return;
                }


                inputStream = InputStreamHelper.getInputStream(conn);

                xpp.setInput(new BufferedReader(new InputStreamReader(inputStream, "ISO-8859-1"), BUFFER_SIZE));

                Timber.d("Parsing Channel %s", channelId);

                int channel_id = 0;
                int value_type;
                boolean gotChannel = false;
                String timestamp;

                while (xpp.getEventType() != XmlPullParser.END_DOCUMENT && !isCancelSync()) {
                    if (xpp.getEventType() == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("channel")) {
                            channel_id = Integer.parseInt(xpp.getAttributeValue(null, "ise_id")) + fullPrefix;
                            gotChannel = true;
                        }
                        if (gotChannel) {
                            if (xpp.getName().equals("datapoint")) {
                                name = xpp.getAttributeValue(null, "name");
                                type = xpp.getAttributeValue(null, "type");
                                value = xpp.getAttributeValue(null, "value");
                                ise_id = Integer.parseInt(xpp.getAttributeValue(null, "ise_id")) + fullPrefix;
                                value_type = Integer.parseInt(xpp.getAttributeValue(null, "valuetype"));
                                timestamp = xpp.getAttributeValue(null, "timestamp");
                                dbM.datapointDbAdapter.updateIfDifferent(ise_id, name, type, channel_id, value,
                                        value_type, timestamp);
                            }
                        }
                    }
                    if (xpp.getEventType() == XmlPullParser.END_TAG) {
                        if (xpp.getName().equals("channel")) {
                            gotChannel = false;
                        }
                    }

                    xpp.next();
                }
            } catch (Throwable t) {
                handleThrowableInformation(t, "While parsing Channel " + channelId);
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (conn != null) {
                        conn.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void parseStateBatch(List<HMObject> objects) {
        InputStream inputStream = null;
        HttpURLConnection conn = null;

        StringBuilder sbChannel = new StringBuilder();
        sbChannel.append("state.cgi?channel_id=");

        StringBuilder sbVar = new StringBuilder();
        sbVar.append("state.cgi?datapoint_id=");

        boolean hasChan = false, hasVar = false;

        for (int i = 0; i < objects.size(); i++) {

            if (objects.get(i) instanceof HMVariable) {
                if (!hasVar) {
                    hasVar = true;
                }
                sbVar.append("").append(Util.removePrefix(ctx, objects.get(i).getRowId())).append(",");
            } else if (objects.get(i) instanceof HMChannel) {

                if (!hasChan) {
                    hasChan = true;
                }
                sbChannel.append("").append(Util.removePrefix(ctx, objects.get(i).getRowId())).append(",");
            }
        }

        if (hasChan) {

            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                try {
                    conn = getUrlConnectionFromAddressUrl(sbChannel.toString());
                } catch (IOException e) {
                    return;
                }

                inputStream = InputStreamHelper.getInputStream(conn);
                xpp.setInput(new BufferedReader(new InputStreamReader(inputStream, "ISO-8859-1"), BUFFER_SIZE));

                String name, type, value;
                int ise_id, channel_id = 0, value_type;
                boolean gotChannel = false;
                String timestamp;

                while (xpp.getEventType() != XmlPullParser.END_DOCUMENT && !isCancelSync()) {
                    if (xpp.getEventType() == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("channel")) {
                            channel_id = Integer.parseInt(xpp.getAttributeValue(null, "ise_id")) + fullPrefix;
                            gotChannel = true;
                        }
                        if (gotChannel) {
                            if (xpp.getName().equals("datapoint")) {
                                name = xpp.getAttributeValue(null, "name");
                                type = xpp.getAttributeValue(null, "type");
                                value = xpp.getAttributeValue(null, "value");
                                ise_id = Integer.parseInt(xpp.getAttributeValue(null, "ise_id")) + fullPrefix;
                                value_type = Integer.parseInt(xpp.getAttributeValue(null, "valuetype"));
                                timestamp = xpp.getAttributeValue(null, "timestamp");
                                dbM.datapointDbAdapter.updateIfDifferent(ise_id, name, type, channel_id, value,
                                        value_type, timestamp);
                            }
                        }
                    }
                    if (xpp.getEventType() == XmlPullParser.END_TAG) {
                        if (xpp.getName().equals("channel")) {
                            gotChannel = false;
                        }
                    }

                    xpp.next();
                }
            } catch (Throwable t) {
                handleThrowableInformation(t, "While parsing States");
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (conn != null) {
                        conn.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        if (hasVar) {

            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                conn = getUrlConnectionFromAddressUrl(sbVar.toString());
                if (conn != null) {
                    inputStream = InputStreamHelper.getInputStream(conn);

                    xpp.setInput(new BufferedReader(new InputStreamReader(inputStream, "ISO-8859-1"), BUFFER_SIZE));

                    String value, rowId;

                    while (xpp.getEventType() != XmlPullParser.END_DOCUMENT && !isCancelSync()) {

                        if (xpp.getEventType() == XmlPullParser.START_TAG) {
                            if (xpp.getName().equals("datapoint")) {
                                value = xpp.getAttributeValue(null, "value").replace("\n", "").replace("\r", "");
                                rowId = xpp.getAttributeValue(null, "ise_id");

                                dbM.varsDbAdapter.updateItem(Integer.parseInt(rowId) + fullPrefix, value);
                            }
                        }
                        xpp.next();
                    }
                }
            } catch (Throwable t) {
                handleThrowableInformation(t, "While parsing Variables");
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (conn != null) {
                        conn.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void parseVarState(String rowId) {
        if (!isCancelSync()) {

            InputStream inputStream = null;
            HttpURLConnection conn = null;
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                try {
                    conn = getUrlConnectionFromAddressUrl("sysvar.cgi?ise_id=" + rowId);
                } catch (IOException e) {
                    return;
                }

                inputStream = InputStreamHelper.getInputStream(conn);

                xpp.setInput(new BufferedReader(new InputStreamReader(inputStream, "ISO-8859-1"), BUFFER_SIZE));

                String value;

                while (xpp.getEventType() != XmlPullParser.END_DOCUMENT && !isCancelSync()) {
                    if (xpp.getEventType() == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("systemVariable")) {
                            value = xpp.getAttributeValue(null, "value").replace("\n", "").replace("\r", "");
                            rowId = xpp.getAttributeValue(null, "ise_id");

                            dbM.varsDbAdapter.updateItem(Integer.parseInt(rowId) + fullPrefix, value);
                        }
                    }
                    xpp.next();
                }
            } catch (Throwable t) {
                handleThrowableInformation(t, "While parsing Variable " + rowId);
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (conn != null) {
                        conn.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void parseVersion() {
        if (!isCancelSync()) {
            InputStream inputStream = null;
            HttpURLConnection conn = null;
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                try {
                    conn = getUrlConnectionFromAddressUrl("version.cgi");
                } catch (IOException e) {
                    return;
                }

                inputStream = InputStreamHelper.getInputStream(conn);
                xpp.setInput(new BufferedReader(new InputStreamReader(inputStream, "ISO-8859-1"), BUFFER_SIZE));
                Timber.i("parsing " + "XML-API Version");

                while (xpp.getEventType() != XmlPullParser.END_DOCUMENT && !isCancelSync()) {
                    if (xpp.getEventType() == XmlPullParser.TEXT) {
                        String version = xpp.getText();

                        try {
                            xmlApiVersion = Float.parseFloat(version);
                            if (xmlApiVersion < XML_API_MIN_VERSION) {
                                Timber.i("Expected XML-API Version " + Util.XML_API_MIN_VERSION_STRING + ", was "
                                        + version);

                                String versionError =
                                        ctx.getString(R.string.xml_version_error) + " " + Util.XML_API_MIN_VERSION_STRING + "!";
                                handleThrowableInformation(null, versionError);
                                refreshSuccessful = false;

                            } else {
                                Timber.i("XML-API is current, version " + xmlApiVersion + " installed");
                            }
                            PreferenceHelper.setApiVersion(ctx, version);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();

                            Timber.i("Expected XML-API Version " + Util.XML_API_MIN_VERSION_STRING + ", was "
                                    + version);

                            throw new IOException(ctx.getResources().getString(R.string.installed_question));
                        }
                    }
                    xpp.next();
                }
            } catch (Throwable t) {
                t.printStackTrace();
                refreshSuccessful = false;
                handleThrowableInformation(t, ctx.getResources().getString(R.string.installed_question));
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (conn != null) {
                        conn.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void handleThrowableInformation(Throwable t, String cause) {
        String s = t == null ? "" : t.toString();
        if (!cause.equals("")) {
            s += "\n" + cause;
        }
        formatErrorToast(s);
    }

    public void formatErrorToast(String trace) {
        if (PreferenceHelper.showErrorMessages(ctx)) {
            Message msg = Message.obtain();
            msg.what = Toast.LENGTH_LONG;
            Bundle b = new Bundle();
            b.putString("trace", ctx.getString(R.string.sync_error) + "\n\n" + trace + "\n\n" + ctx.getString(
                    R.string.sync_error_help));
            msg.setData(b);
            if (toastHandler != null) {
                toastHandler.sendMessage(msg);
            }
        }
    }

    public HttpURLConnection getUrlConnectionFromAddressUrl(String urlPostfix) throws IOException {
        try {
            return InputStreamHelper.getUrlConnectionFromUrl(ctx, address + urlPostfix, false);
        } catch (Exception e) {
            refreshSuccessful = false;
            Timber.e(e);

            String msg = "";
            if (e.getLocalizedMessage() != null) {
                msg = e.getLocalizedMessage();
            }
            msg = msg + "\n\nURL: " + address + urlPostfix;
            formatErrorToast(msg);
            throw e;
        }
    }

    public boolean isCancelSync() {
        return cancelSync;
    }

    public void setCancelSync(boolean cancelSync) {
        this.cancelSync = cancelSync;
    }

}
