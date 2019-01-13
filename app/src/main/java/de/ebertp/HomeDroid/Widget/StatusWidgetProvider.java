package de.ebertp.HomeDroid.Widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

import de.ebertp.HomeDroid.Activities.WidgetClickActivity;
import de.ebertp.HomeDroid.Communication.Control.HMCheckable;
import de.ebertp.HomeDroid.Communication.Control.HMControllable;
import de.ebertp.HomeDroid.Communication.Control.HmType;
import de.ebertp.HomeDroid.Connection.BroadcastHelper;
import de.ebertp.HomeDroid.DbAdapter.DataBaseAdapterManager;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Model.HMChannel;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.CustomImageHelper;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.Util;
import de.ebertp.HomeDroid.ViewAdapter.ListViewGenerator;

import static de.ebertp.HomeDroid.Communication.Control.HmType.DIMMER_IP_COLOR;
import static de.ebertp.HomeDroid.Communication.Control.HmType.PROGRAM;

public class StatusWidgetProvider extends AppWidgetProvider {

    public static final int TYPE_CHANNEL = 0;
    public static final int TYPE_SCRIPT = 1;
    public static final int TYPE_VARIABLE = 2;
    public static final int TYPE_LOCAL_FAVS = 3;
    public static final int TYPE_ROOM = 3;
    public static final int TYPE_FUNCTION = 3;
    private static final String TAG = "StatusWidgetProvider";

    public static final int CONTROL_MODE_FULL = 0;
    public static final int CONTROL_MODE_DIALOG_ONLY = 1;
    public static final int CONTROL_MODE_SHORTCUT_ONLY = 2;
    public static final int CONTROL_MODE_DIALOG_PASSIVE = 3;

    private static final int MIN_FOR_TWO_COLUMNS = 3;

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateWidgets(context, appWidgetManager, appWidgetIds);
    }

    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            HomeDroidApp.db().widgetDbAdapter.deleteItem(widgetId);
        }
    }

    public static void updateAllWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, StatusWidgetProvider.class));
        int[] allWidgetIdsMed = appWidgetManager.getAppWidgetIds(new ComponentName(context, StatusWidgetProviderMed.class));
        int[] allWidgetIdsBig = appWidgetManager.getAppWidgetIds(new ComponentName(context, StatusWidgetProviderBig.class));

        updateWidgets(context, appWidgetManager, allWidgetIds);
        updateWidgets(context, appWidgetManager, allWidgetIdsMed);
        updateWidgets(context, appWidgetManager, allWidgetIdsBig);

        Intent i = new Intent(context, SyncWidgetProvider.class);
        i.setAction(SyncWidgetProvider.SYNC_STATE_CHANGED);
        context.sendBroadcast(i);
    }

    private static void updateWidgets(Context context, AppWidgetManager appWidgetManager, int[] widgetIds) {
        DataBaseAdapterManager dbM = HomeDroidApp.db();
        ListViewGenerator listViewGenerator = new ListViewGenerator(context, null, R.layout.list_item, R.layout.list_item_value, true, PreferenceHelper.isDarkTheme(context));
        CustomImageHelper customImageHelper = new CustomImageHelper(context);

        for (int widgetId : widgetIds) {
            RemoteViews remoteViews = getRemoteView(context, listViewGenerator, customImageHelper, dbM, appWidgetManager, widgetId);
            if (remoteViews != null) {
                appWidgetManager.updateAppWidget(widgetId, remoteViews);
            }
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        Log.d(TAG, "onAppWidgetOptionsChanged");

        DataBaseAdapterManager dbM = HomeDroidApp.db();
        ListViewGenerator listViewGenerator = new ListViewGenerator(context, null, R.layout.list_item, R.layout.list_item_value, false, PreferenceHelper.isDarkTheme(context));
        CustomImageHelper customImageHelper = new CustomImageHelper(context);

        RemoteViews remoteViews = getRemoteView(context, listViewGenerator, customImageHelper, dbM, appWidgetManager, appWidgetId);
        if (remoteViews != null) {
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,
                newOptions);
    }

    private static RemoteViews getRemoteView(Context context, ListViewGenerator listViewGenerator, CustomImageHelper customImageHelper, DataBaseAdapterManager dbM, AppWidgetManager appWidgetManager, int widgetId) {

        Cursor c = dbM.widgetDbAdapter.fetchItem(widgetId);
        if (c == null || c.getCount() == 0) {
            return null;
        }

        int iseId = c.getInt(1);
        int widgetType = c.getInt(2);
        String name = c.getString(3);
        Integer controlMode = c.isNull(4) ? CONTROL_MODE_FULL : c.getInt(c.getColumnIndex("control_mode"));
        Util.closeCursor(c);

        switch (widgetType) {
            case TYPE_CHANNEL:
            case TYPE_VARIABLE:
            case TYPE_SCRIPT: {
                HMObject hmObject = Util.getHmObject(iseId, widgetType, dbM);
                return getControllableRemoteView(context, listViewGenerator, appWidgetManager, widgetId, widgetType, name, controlMode, hmObject);
            }
            case TYPE_LOCAL_FAVS: {
                //TODO use different path for local favs
                return getShortcutRemoteView(context, customImageHelper, iseId, name, widgetType);
            }
            default: {
                Log.e(TAG, "Unknown widget type" + widgetType);
                throw new IllegalStateException("Unknown widget type " + widgetType);
            }
        }
    }

    private static RemoteViews getShortcutRemoteView(Context context, CustomImageHelper customImageHelper, int iseId, String name, int widgetType) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_shortcut_layout);
        customImageHelper.setCustomImage(iseId, remoteViews, R.id.icon);
        remoteViews.setTextViewText(R.id.name, name);

        Intent intent = new Intent(context, WidgetClickActivity.class);
        intent.putExtra(Util.INTENT_ISE_ID, iseId);
        intent.putExtra(Util.INTENT_WIDGET_TYPE, widgetType);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, iseId, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.ll_as_button, pendingIntent);
        return remoteViews;
    }

    private static RemoteViews getControllableRemoteView(Context context, ListViewGenerator listViewGenerator, AppWidgetManager appWidgetManager, int widgetId, int widgetType, String name, Integer controlMode, HMObject hmObject) {
        int numOfColumns;
        int numOfRows;

        // 4.1+ only
        int widgetContentTextSize = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Bundle options = appWidgetManager.getAppWidgetOptions(widgetId);
            numOfColumns = getCellsForSize(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH));
            numOfRows = getCellsForSize(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT));
        } else {
            //set big values on older OS versions
            numOfColumns = 10;
            numOfRows = 1;
        }

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_status_layout_normal);
        remoteViews.removeAllViews(R.id.widgetLL);
        remoteViews.removeAllViews(R.id.widgetLLColumnLeft);
        remoteViews.removeAllViews(R.id.widgetLLColumnRight);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            int widgetTitleTextSize;
            int widgetTextSizePreference = PreferenceHelper.getWidgetTextSize(context);
            switch (widgetTextSizePreference) {
                case 1: {
                    widgetTitleTextSize = R.dimen.widget_textsize_content_small;
                    widgetContentTextSize = R.dimen.widget_textsize_content_tiny;
                    break;
                }
                case 2: {
                    widgetTitleTextSize = R.dimen.widget_textsize_content_medium;
                    widgetContentTextSize = R.dimen.widget_textsize_content_small;
                    break;
                }
                case 3: {
                    widgetTitleTextSize = R.dimen.widget_textsize_content_big;
                    widgetContentTextSize = R.dimen.widget_textsize_content_medium;
                    break;
                }
                case 4: {
                    widgetTitleTextSize = R.dimen.widget_textsize_content_very_big;
                    widgetContentTextSize = R.dimen.widget_textsize_content_big;
                    break;
                }
                case 5: {
                    widgetTitleTextSize = R.dimen.widget_textsize_content_huge;
                    widgetContentTextSize = R.dimen.widget_textsize_content_very_big;
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown widget size " + widgetTextSizePreference);
            }

            remoteViews.setTextViewTextSize(R.id.name, TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(widgetTitleTextSize));
        }

        if (hmObject == null) {
            remoteViews.setTextViewText(R.id.name, "Error loading data");
            return null;
        }

        View v = listViewGenerator.getView((HMChannel) hmObject);
        if (v == null) {
            return null;
        }

        try {
            HMControllable hmc = (HMControllable) v.getTag();
            if (hmc != null && hmc.getType() != HmType.PASSIV) {
                if (controlMode == CONTROL_MODE_FULL || controlMode == CONTROL_MODE_DIALOG_ONLY) {
                    Intent intent = new Intent(context, WidgetClickActivity.class);
                    intent.putExtra(Util.INTENT_ISE_ID, hmObject.getRowId());
                    intent.putExtra(Util.INTENT_WIDGET_TYPE, widgetType);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, hmObject.getRowId(), intent, 0);
                    remoteViews.setOnClickPendingIntent(R.id.ll_as_button, pendingIntent);
                }
            }

            if (name == null || name.equals("")) {
                remoteViews.setViewVisibility(R.id.name, View.GONE);
                remoteViews.setViewVisibility(R.id.divider, View.GONE);
            } else {
                remoteViews.setTextViewText(R.id.name, name);
            }

            ViewGroup children = v.findViewById(R.id.valueLL);
            int childCount = children.getChildCount();
            if (childCount >= MIN_FOR_TWO_COLUMNS && numOfColumns > 1) {
                remoteViews.setViewVisibility(R.id.widgetLL, View.GONE);
                remoteViews.setViewVisibility(R.id.widgetLLTwoColumns, View.VISIBLE);
            } else {
                remoteViews.setViewVisibility(R.id.widgetLL, View.VISIBLE);
                remoteViews.setViewVisibility(R.id.widgetLLTwoColumns, View.GONE);
            }

            for (int i = 0; i < childCount; i++) {
                if (i >= 3 && numOfRows < 2 && numOfColumns < 2) {
                    //if the content really does not fit in one cell, cut it off
                    break;
                }

                if (i >= 2 && numOfRows < 2 && numOfColumns < 2) {
                    //this content might fit, if the name is short or empty
                    if (!(TextUtils.isEmpty(name) || name.length() <= 7)) {
                        break;
                    }
                }

                View child = children.getChildAt(i);
                if (child == null) {
                    continue;
                }

                RemoteViews remoteViewsChild = new RemoteViews(context.getPackageName(), R.layout.widget_item);

                TextView tv = child.findViewById(R.id.value);
                CheckedTextView cb = child.findViewById(R.id.check);
                Button btn = child.findViewById(R.id.button);
                ImageView icon = child.findViewById(R.id.value_icon);
                ImageView iconBig = child.findViewById(R.id.value_icon_big);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    remoteViewsChild.setTextViewTextSize(R.id.label, TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(widgetContentTextSize));
                    remoteViewsChild.setTextViewTextSize(R.id.value, TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(widgetContentTextSize));
                    remoteViewsChild.setTextViewTextSize(R.id.boolValue, TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(widgetContentTextSize));
                }

                if (tv.getVisibility() == View.VISIBLE) {
                    String value = "";

                    if (tv.getText() != null) {
                        value = tv.getText().toString();
                    }

                    remoteViewsChild.setTextViewText(R.id.value, value);
                    remoteViewsChild.setViewVisibility(R.id.value, View.VISIBLE);
                }

                if (cb.getVisibility() == View.VISIBLE) {

                    remoteViewsChild.setViewVisibility(R.id.boolImage, View.VISIBLE);

                    HMCheckable hms = cb.getTag() == null ? null : (HMCheckable) cb.getTag();

                    HMChannel hmChannel = ((HMChannel) hmObject);
                    if (hmChannel.isOperate() && cb.isEnabled() && hmChannel.type != "HM-Program") {

                        if (controlMode == CONTROL_MODE_FULL || controlMode == CONTROL_MODE_SHORTCUT_ONLY) {
                            Intent intent = new Intent(context, WidgetFlipSwitchReceiver.class);

                            if (hms.getDatapointId() == null) {
                                intent.putExtra(Util.INTENT_ISE_ID, hms.getRowId());
                                intent.putExtra(Util.INTENT_WIDGET_TYPE, widgetType);
                            } else {
                                // Very dirty hack to send commands to datapoints but still refresh the channel
                                intent.putExtra(Util.INTENT_ISE_ID, hms.getDatapointId());
                                intent.putExtra(Util.INTENT_WIDGET_TYPE, Util.TYPE_DATAPOINT);
                            }

                            String newValue;

                            if (hms.type == HmType.DIMMER || hms.type.equals(HmType.DIMMER_IP) || hms.type == DIMMER_IP_COLOR || hms.type == HmType.BLIND || hms.type == HmType.BLIND_WITH_LAMELLA_IP || hms.type == HmType.BLIND_WITH_LAMELLA) {
                                if (cb.isChecked()) {
                                    newValue = "0";
                                } else {
                                    newValue = "1";
                                }
                            } else {
                                if (cb.isChecked()) {
                                    newValue = "false";
                                } else {
                                    newValue = "true";
                                }
                            }

                            intent.putExtra(Util.INTENT_NEWVALUE, newValue);

                            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, hmObject.getRowId(), intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);

                            if (controlMode == CONTROL_MODE_SHORTCUT_ONLY) {
                                remoteViews.setOnClickPendingIntent(R.id.ll_as_button, pendingIntent);
                            } else {
                                remoteViewsChild.setOnClickPendingIntent(R.id.boolImage, pendingIntent);
                            }
                        }
                    }


                    if (cb.getText() != "") {
                        if (childCount == 1) {
                            remoteViewsChild.setViewVisibility(R.id.boolValueBelow, View.VISIBLE);
                            remoteViewsChild.setTextViewText(R.id.boolValueBelow, cb.getText().toString());
                        } else {
                            remoteViewsChild.setViewVisibility(R.id.boolValue, View.VISIBLE);
                            remoteViewsChild.setTextViewText(R.id.boolValue, cb.getText().toString());
                        }
                    }


                    if (cb.isChecked()) {
                        if (hms != null && hms.getTrueIcon() != null) {
                            remoteViewsChild.setImageViewResource(R.id.boolImage, hms.getTrueIcon());
                        } else {
                            remoteViewsChild.setImageViewResource(R.id.boolImage, R.drawable.btn_radio_on_holo_dark_hm_old);
                        }
                    } else {
                        if ((hms != null && hms.getFalseIcon() != null)) {
                            remoteViewsChild.setImageViewResource(R.id.boolImage, hms.getFalseIcon());
                        } else {
                            remoteViewsChild.setImageViewResource(R.id.boolImage, R.drawable.btn_radio_off_focused_holo_dark_hm_old);
                        }
                    }
                }

                if (btn.getVisibility() == View.VISIBLE) {
                    remoteViewsChild.setViewVisibility(R.id.boolImage, View.VISIBLE);
                    remoteViewsChild.setInt(R.id.boolImage, "setBackgroundResource", R.drawable.btn_default_holo_dark_hm);


                    if (controlMode == CONTROL_MODE_FULL || controlMode == CONTROL_MODE_SHORTCUT_ONLY) {
                        Intent intent = new Intent(context, WidgetFlipSwitchReceiver.class);

                        if (hmc != null) {
                            intent.putExtra(Util.INTENT_ISE_ID, hmc.getRowId());
                        } else {
                            intent.putExtra(Util.INTENT_ISE_ID, hmObject.getRowId());
                        }

                        intent.putExtra(Util.INTENT_WIDGET_TYPE, widgetType);
                        intent.putExtra(Util.INTENT_NEWVALUE, "1");

                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, hmObject.getRowId(), intent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                        if (controlMode == CONTROL_MODE_SHORTCUT_ONLY) {
                            remoteViews.setOnClickPendingIntent(R.id.ll_as_button, pendingIntent);
                        } else {
                            remoteViewsChild.setOnClickPendingIntent(R.id.boolImage, pendingIntent);
                        }
                    }
                }

                if (icon.getVisibility() == View.VISIBLE) {
                    remoteViewsChild.setViewVisibility(R.id.value_icon, View.VISIBLE);
                    remoteViewsChild.setImageViewResource(R.id.value_icon, (Integer) icon.getTag());
                }

                if (iconBig.getVisibility() == View.VISIBLE) {
                    remoteViewsChild.setViewVisibility(R.id.value_icon_big, View.VISIBLE);
                    remoteViewsChild.setImageViewResource(R.id.value_icon_big, (Integer) iconBig.getTag());
                }

                if (childCount >= MIN_FOR_TWO_COLUMNS && numOfColumns > 1) {
                    if (i % 2 == 0) {
                        remoteViews.addView(R.id.widgetLLColumnLeft, remoteViewsChild);
                    } else {
                        remoteViews.addView(R.id.widgetLLColumnRight, remoteViewsChild);
                    }
                } else {
                    remoteViews.addView(R.id.widgetLL, remoteViewsChild);
                }
            }
            return remoteViews;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns number of cells needed for given size of the widget.
     *
     * @param size Widget size in dp.
     * @return Size in number of cells.
     */
    private static int getCellsForSize(int size) {
        int n = 2;
        while (70 * n - 30 < size) {
            ++n;
        }
        return n - 1;
    }
}
