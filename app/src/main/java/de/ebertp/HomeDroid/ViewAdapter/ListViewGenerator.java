package de.ebertp.HomeDroid.ViewAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.ebertp.HomeDroid.Activities.SeekBarWithButtonsDialog;
import de.ebertp.HomeDroid.Activities.SeekBarWithButtonsDialogWidget;
import de.ebertp.HomeDroid.Communication.Control.ControlHelper;
import de.ebertp.HomeDroid.Communication.Control.HMCheckable;
import de.ebertp.HomeDroid.Communication.Control.HMControllable;
import de.ebertp.HomeDroid.Communication.Control.HMControllableVar;
import de.ebertp.HomeDroid.Communication.Control.HmDatapoint;
import de.ebertp.HomeDroid.Communication.Control.HmType;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.DatapointDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.DataBaseAdapterManager;
import de.ebertp.HomeDroid.DbAdapter.DbUtil;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Model.HMChannel;
import de.ebertp.HomeDroid.Model.HMCommand;
import de.ebertp.HomeDroid.Model.HMHvac;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.Model.HMScript;
import de.ebertp.HomeDroid.Model.HMVariable;
import de.ebertp.HomeDroid.Model.WebCam;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.ColorSwitchIconHelper;
import de.ebertp.HomeDroid.Utils.CustomImageHelper;
import de.ebertp.HomeDroid.Utils.LedStatusIconHelper;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.ToastHandler;
import de.ebertp.HomeDroid.Utils.UnsupportedUtil;
import de.ebertp.HomeDroid.Utils.Util;
import de.ebertp.HomeDroid.Utils.WebcamHelper;
import timber.log.Timber;

public class ListViewGenerator {

    private final ViewAdder mViewAdder;
    private LayoutInflater inflater;
    private CustomImageHelper customImageHelper;
    private DataBaseAdapterManager dbM;
    private DatapointDbAdapter mDatapointHelper;
    private Context ctx;
    private Activity activity;

    private ToastHandler toastHandler;
    private boolean isDarkTheme;
    private boolean isWidget;
    private final boolean isDetailed;
    private int layoutRes;
    private int layoutResItem;
    private final boolean isClickable;


    public ListViewGenerator(Context ctx, Activity activity, int layoutRes, int layoutResItem, boolean isWidget,
                             boolean isDarkTheme) {
        this(ctx, activity, layoutRes, layoutResItem, isWidget, isDarkTheme, true, false);
    }

    public ListViewGenerator(Context ctx, Activity activity, int layoutRes, int layoutResItem, boolean isWidget,
                             boolean isDarkTheme, boolean isClickable, boolean isDetailed) {
        super();
        this.ctx = ctx;
        this.activity = activity;
        this.inflater = LayoutInflater.from(ctx);
        this.isWidget = isWidget;
        this.isClickable = isClickable;
        this.isDetailed = isDetailed;
        dbM = HomeDroidApp.db();
        mDatapointHelper = dbM.datapointDbAdapter;
        this.customImageHelper = new CustomImageHelper(ctx);

        this.layoutRes = layoutRes;
        this.layoutResItem = layoutResItem;

        if (activity == null) {
            toastHandler = null;
        } else {
            toastHandler = new ToastHandler(ctx);
        }

        this.isDarkTheme = isDarkTheme;

        mViewAdder = new ViewAdder(ctx, isDarkTheme, inflater, layoutResItem);
    }

    public View getView(HMChannel hmc) {
        if (hmc == null || hmc.getHidden() == 1) {
            return inflater.inflate(R.layout.devices_row_empty, null);
        }

        if (hmc.isVisible() || !PreferenceHelper.isHideNotVisible(ctx)) {
            View v = picViewType(hmc);
            if (v == null) {
                return inflater.inflate(R.layout.devices_row_empty, null);
            }

            if (isDarkTheme) {
                v.findViewById(R.id.head).setBackgroundResource(R.drawable.bg_card_dark_selektor);
            }

            ImageView iv = (ImageView) v.findViewById(R.id.icon);
            if (customImageHelper.setCustomImage(hmc.rowId, iv) != CustomImageHelper.EXTERNAL) {
                Util.setIconColor(isDarkTheme, iv);
            }

            return v;
        } else {
            return inflater.inflate(R.layout.devices_row_empty, null);
        }

    }

    private View picViewType(HMChannel hmc) {
        View v = inflater.inflate(layoutRes, null);
        v.setId(hmc.rowId);
        ((TextView) v.findViewById(R.id.name)).setText(hmc.name);

        boolean isCUXD = hmc.getAddress() != null && hmc.getAddress().startsWith("CUX");

        String type = hmc.type;

        if (type.equals("")) {
            if (hmc.getAddress().startsWith("CUX")) {
                UpdateCUXD(v);
            } else {
                RebootCCUView(v);
            }
        } else if (type.equalsIgnoreCase("HM-Program")) {
            ScriptView(v, hmc);
        } else if (type.equalsIgnoreCase("HM-Variable")) {
            SelectVariableView(v, (HMVariable) hmc);
        }
        // IntesisBox
        else if (isCUXD && hmc.getAddress().startsWith("CUX3603")) {
            if (hmc.channelIndex == 0) {
                v = null;
            } else if (hmc.channelIndex == 1) {
                TemperatureView2(v, hmc);
            } else if (hmc.channelIndex == 2) {
                TemperatureControlView(v, hmc, 0, 40, "°C");
            } else if (hmc.channelIndex == 3) {
                SwitchView(v, hmc);
            } else if (hmc.channelIndex == 4) {
                HVACView(v, hmc);
            } else if (hmc.channelIndex == 5) {
                ErrorView(v, hmc);
            }
            //PicoTracker
        } else if (isCUXD && hmc.getAddress().startsWith("CUX350")) {
            if (hmc.channelIndex == 1) {
                TasterView(v, hmc);
            } else if (hmc.channelIndex == 2) {
                StateView(v, hmc, R.drawable.flat_locked, R.drawable.flat_unlocked);
            } else if (hmc.channelIndex == 3) {
                VoltageView(v, hmc);
            }
//        } else if (isCUXD && hmc.getAddress().startsWith("CUX9002")) {
//            WeatherCuxdView(v, hmc);
        } else if (isCUXD && hmc.getAddress().startsWith("CUX2802")) {
            if (hmc.channelIndex <= 6) {
                VariableView(v, hmc, 0, 100, "%", HmType.DIMMER, R.drawable.flat_light_off_2, R.drawable.flat_light_on_2);
            } else {
                v = null;
            }
        } // switches
        else if (type.startsWith("HM-LC-Sw") || type.equalsIgnoreCase("HM-MOD-RC-8") || type.equalsIgnoreCase("ZEL STG RM FZS") || type.equalsIgnoreCase("HM-MOD-Re-8") || type.equals("HM-LC-DDC1-PCB")) {
            SwitchView(v, hmc);
            // statusanzeige
        } else if (type.equalsIgnoreCase("HM-Dis-TD-T")) {
            SwitchView(v, hmc);
        } else if (type.equals("HM-Dis-EP-WM55")) {
            if (hmc.channelIndex <= 2) {
                TasterView(v, hmc);
            } else {
                v = null;
            }
        } else if (type.startsWith("HM-ES-PMSw1") || type.equals("HM-ES-PMSwX")) {
            if (hmc.channelIndex == 1) {
                SwitchView(v, hmc);
            } else if (hmc.channelIndex == 2) {
                PowerMeterView(v, hmc);
            } else {
                v = null;
            }
        } else if (type.equals("HM-ES-TX-WM")) {
            GasMeterView(v, hmc);
        } else if (
            // sensoren wired
                type.equalsIgnoreCase("HMW-Sen-SC-12-DR") || type.equalsIgnoreCase("HMW-Sen-SC-12-FM")) {
            SensorView(v, hmc);
        }
        //
        else if (
            // funkgong
                type.equalsIgnoreCase("HM-OU-CF-Pl") || type.equalsIgnoreCase("HM-OU-CFM-Pl") || type.equalsIgnoreCase(
                        "HM-OU-CM-PCB") || type.equals("HM-OU-CFM-TW")
                        // alarmsirene
                        || type.equalsIgnoreCase("HM-Sec-SFA-SM")) {
            SwitchView(v, hmc, ctx.getResources().getString(R.string.active),
                    ctx.getResources().getString(R.string.inactive), null, null);
        } else if (
            // rauchmelder
                type.startsWith("HM-Sec-SD")) {
            if (hmc.value.equals("")) {
                hmc.value = "false";
            }
            StateView(v, hmc, R.drawable.flat_alarm, R.drawable.flat_no_smoke);
        } else if (
            // schliesskontakte
                type.equalsIgnoreCase("HM-SCI-3-FM") || type.equalsIgnoreCase("HM-Sec-SCI-3-FM")
                        || type.equalsIgnoreCase("HM-Sec-TiS")) {
            StateView(v, hmc, R.drawable.flat_unlocked, R.drawable.flat_locked);
        } else if (type.equalsIgnoreCase("HM-Sec-SCo") || type.equalsIgnoreCase("ZEL STG RM FFK") || type.equalsIgnoreCase("HM-Sec-SC") || type.startsWith("HM-Sec-SC-2")) {
            StateView(v, hmc, R.drawable.flat_unlocked, R.drawable.flat_locked);
        } else if (
            // dimmer
                type.startsWith("HM-LC-Dim") || type.startsWith("HMW-LC-Dim") || type.equalsIgnoreCase("HSS-DX") || type.equals("OLIGO.smart.iq.HM")) {
            VariableView(v, hmc, 0, 100, "%", HmType.DIMMER, R.drawable.flat_light_off_2, R.drawable.flat_light_on_2);
        } else if (
            // jalousieaktor
                type.equalsIgnoreCase("HM-LC-Bl1-SM") || type.equalsIgnoreCase("HM-LC-Bl1-FM") || type.equalsIgnoreCase(
                        "HM-LC-Bl1-PB-FM") || type.equalsIgnoreCase("HM-LC-BlX") || type.equalsIgnoreCase(
                        "HM-LC-Bl1PBU-FM") || type.equalsIgnoreCase("ZEL STG RM FEP 230V")) {
            VariableView(v, hmc, 0, 100, "%", HmType.BLIND, R.drawable.flat_blinds_closed, R.drawable.flat_blinds_up);
        } else if (type.equals("HM-LC-Ja1PBU-FM")) {
            VariableView(v, hmc, 0, 100, "%", HmType.BLIND_WITH_LAMELLA, R.drawable.flat_blinds_closed, R.drawable.flat_blinds_up);
        } else if (
            // jalousieaktor wired
                type.equalsIgnoreCase("HMW-LC-Bl1-DR")) {
            if (hmc.channelIndex <= 2) {
                TasterView(v, hmc);
            } else {
                VariableView(v, hmc, 0, 100, "%", HmType.BLIND, R.drawable.flat_blinds_closed, R.drawable.flat_blinds_up);
            }
        } else if (type.equalsIgnoreCase("HM-Sec-Key") || type.equalsIgnoreCase("HM-Sec-Key-S")
                || type.equalsIgnoreCase("HM-Sec-Key-O") || type.equalsIgnoreCase("HM-Sec-Key-Generic")) {
            SwitchKeyView(v, hmc);
        } else if (type.equals("HmIP-DLD")) {
            // hide all other channels
            if (hmc.channelIndex == 0) {
                BatteryView(v, hmc);
            } else if (hmc.channelIndex == 1) {
                LockIpView(v, hmc);
            } else if (hmc.channelIndex >= 2 && hmc.channelIndex <= 9) {
                StateView(v, hmc, R.drawable.btn_check_on_holo_dark_hm, R.drawable.btn_check_off_holo_dark_hm);
            }
        } else if (type.equals("HmIP-DLS")) {
            if (hmc.channelIndex == 0) {
                BatteryView(v, hmc);
            } else if (hmc.channelIndex == 1) {
                LockStateIpView(v, hmc);
            }
        } else if (type.equalsIgnoreCase("HM-RC-4") || type.equalsIgnoreCase("HM-RC-4-B") || type.equalsIgnoreCase("HM-RC-4-3") || type.equalsIgnoreCase(
                "HM-RC-8") || type.equalsIgnoreCase("HM-RC-P1") || type.equalsIgnoreCase("HM-RC-Sec3")
                || type.equalsIgnoreCase("HM-RC-Sec3-B") || type.equalsIgnoreCase("HM-RC-Key3")
                || type.equalsIgnoreCase("HM-RC-Key3-B") || type.equalsIgnoreCase("HM-RC-Sec4-2") || type.equalsIgnoreCase("HM-RC-Sec4-3")
                || type.equalsIgnoreCase("HM-RC-Key4-2") || type.equalsIgnoreCase("HM-RC-Key4-3") || type.equalsIgnoreCase("HM-PB-4-WM")
                || type.equalsIgnoreCase("HM-PB-4Dis-WM") || type.equalsIgnoreCase("HM-PB-4Dis-WM-2") || type.equalsIgnoreCase("ZEL STG RM DWT 10")
                || type.startsWith("HM-PB-2") || type.equalsIgnoreCase("HM-RC-X") || type.equalsIgnoreCase(
                "HM-PBI-4-FM") || type.equalsIgnoreCase("HM-RC-12") || type.equalsIgnoreCase("HM-RC-12-B")
                || type.equalsIgnoreCase("HM-RC-12-SW") || type.equalsIgnoreCase("HM-PBI-X") || type.equalsIgnoreCase(
                "HM-RC-4-2") || type.equalsIgnoreCase("HM-PB-6-WM55") || type.equalsIgnoreCase("ZEL STG RM WT 2")
                || type.equalsIgnoreCase("ZEL STG RM FST UP4") || type.equalsIgnoreCase("ZEL STG RM HS 4")
                || type.equalsIgnoreCase("HM-RC-SB-X") || type.equalsIgnoreCase("BRC-H") || type.equalsIgnoreCase("HM-Dis-WM55") || type.startsWith("HM-RC-2-PBU-FM")
                || type.equalsIgnoreCase("HM-RCV-50") || type.equals("HMW-RCV-50") || type.equals("HmIP-DSD-PCB") || type.startsWith("HmIPW-BRC")) {
            TasterView(v, hmc);
        } else if (type.startsWith("HmIP-FCI") || type.startsWith("HmIPW-DRI")) {
            if (hmc.channelIndex == 0) {
                BatteryView(v, hmc);
            } else {
                TasterAndStateView(v, hmc);
            }
        } else if (type.startsWith("HM-MOD-EM-8")) {
            ModeView(v, hmc);
        } else if (type.equalsIgnoreCase("HM-RC-19") || type.equalsIgnoreCase("HM-RC-19-B") || type.equalsIgnoreCase(
                "HM-RC-19-SW")) {
            if (hmc.channelIndex <= 18) {
                TasterView(v, hmc);
            } else {
                return NotSupportedView(v, hmc);
            }
        } else if (type.equals("HM-RC-Dis-H-x-EU")) {
            TasterView(v, hmc);
        } else if (type.equals("HM-Sen-DB-PCB")) {
            DoorBellView(v, hmc);
        } else if (type.equalsIgnoreCase("HM-CC-VD") || type.equalsIgnoreCase("ZEL STG RM FSA")) {
            if (hmc.channelIndex == 0) {
                v = null;
            } else if (hmc.channelIndex == 1) {
                Int99ViewVent(v, hmc);
            } else {
                v = null;
            }
        } else if (type.equalsIgnoreCase("HM-CC-TC") || type.equalsIgnoreCase("ZEL STG RM FWT")) {
            if (hmc.channelIndex == 1) {
                WeatherView(v, hmc);
            } else if (hmc.channelIndex == 2) {
                TemperatureControlView(v, hmc, 6, 30, "°C");
            } else {
                v = null;
            }
        } else if (type.equalsIgnoreCase("HM-CC-RT-DN")) {
            if (hmc.channelIndex == 4) {
                TemperatureControlView2(v, hmc, 4.5, 30.5, "°C");
            } else {
                v = null;
            }
        } else if (type.equalsIgnoreCase("HM-CC-VG-1")) {
            if (hmc.channelIndex == 1) {
                TemperatureControlView2(v, hmc, 4.5, 30.5, "°C");
            } else if (hmc.channelIndex == 2) {
                StateView(v, hmc, R.drawable.flat_window_open, R.drawable.flat_locked);
            } else {
                v = null;
            }
        } else if (type.equalsIgnoreCase("HM-TC-IT-WM-W-EU")) {
            if (hmc.channelIndex == 1) {
                if (hmc.getAddress().startsWith("CUX900201")) {
                    WeatherCuxdView(v, hmc);
                } else {
                    WeatherView(v, hmc);
                }
            } else if (hmc.channelIndex == 2) {
                TemperatureControlView3(v, hmc, 4.5, 30.5, "°C");
            } else {
                v = null;
            }
        } else if (type.equalsIgnoreCase("HM-Sen-MDIR-WM55")) {
            if (hmc.channelIndex <= 2) {
                TasterView(v, hmc);
            } else {
                MotionView(v, hmc);
            }
        } else if (type.startsWith("HM-Sec-MDIR") || type.startsWith("HM-Sen-MDIR") || type.equalsIgnoreCase("HM-Sec-MD") || type.equalsIgnoreCase("HM-MD") || type.equals("HmIP-STV")) {
            if (hmc.channelIndex == 0) {
                BatteryView(v, hmc);
            } else {
                MotionView(v, hmc);
            }
        } else if (type.equalsIgnoreCase("HM-Sec-RHS") || type.equalsIgnoreCase("HM-Sec-RHS-2") || type.equalsIgnoreCase("HM-Sec-XX") || type.equalsIgnoreCase(
                "ZEL STG RM FDK")) {
            WindowView(v, hmc);
        } else if (type.equalsIgnoreCase("HM-OU-LED16")) {
            LedDisplayView(v, hmc);
        } else if (type.equalsIgnoreCase("HMW-IO-12-FM") || type.equalsIgnoreCase("HMW-IO-4-FM")) {
            selectTasterOrSwitchView(v, hmc);
            // v = TasterView(hmc);
        } else if (type.equalsIgnoreCase("HM-SwI-3-FM") || type.equalsIgnoreCase("HM-SwI-X") || type.equalsIgnoreCase(
                "ZEL STG RM FSS UP3")) {
            TasterView(v, hmc);
        } else if (type.equalsIgnoreCase("HMW-IO-12-Sw7-DR")) {
            if (hmc.channelIndex <= 12) {
                TasterView(v, hmc);
            } else if (hmc.channelIndex > 12) {
                SwitchView(v, hmc);
            }
        } else if (type.equalsIgnoreCase("HMW-IO-12-Sw14-DR")) {
            if (hmc.channelIndex <= 14) {
                SwitchView(v, hmc);
            } else if (hmc.channelIndex <= 20) {
                StateView(v, hmc, R.drawable.flat_unlocked, R.drawable.flat_locked);
            } else if (hmc.channelIndex > 20) {
                Int1000ViewVent(v, hmc);
            }
        } else if (type.equalsIgnoreCase("ASH550I") || type.equalsIgnoreCase("ASH550") || type.equalsIgnoreCase(
                "HM-WDS10-TH-O") || type.equalsIgnoreCase("HM-WDS20-TH-O") || type.equalsIgnoreCase("HM-WDS40-TH-I")
                || type.equalsIgnoreCase("HM-WDS40-TH-I-2") || type.equalsIgnoreCase("IS-WDS-TH-OD-S-R3")) {
            if (hmc.getAddress().startsWith("CUX9002001")) {
                WeatherCuxdView(v, hmc);
            } else {
                WeatherView(v, hmc);
            }
        } else if (type.equalsIgnoreCase("WS550") || type.equalsIgnoreCase("WS888") || type.equalsIgnoreCase(
                "WS550Tech") || type.equalsIgnoreCase("WS550LCB") || type.equalsIgnoreCase("WS550LCW")
                || type.equalsIgnoreCase("HM-WDC7000")) {
            if (hmc.channelIndex == 10) {
                WirelessWeatherStationView(v, hmc);
            } else {
                v = null;
            }
        } else if (type.equalsIgnoreCase("HM-WDS100-C6-O") || type.equalsIgnoreCase("HM-WDS100-C6-O-2") || (type.equalsIgnoreCase("KS550") || type.equalsIgnoreCase(
                "KS888") || type.equalsIgnoreCase("KS550Tech") || type.equalsIgnoreCase("KS550LC"))) {
            WeatherStationView(v, hmc);
        } else if (type.equalsIgnoreCase("HB-UW-Sen-THPL-I") || type.equalsIgnoreCase("HB-UW-Sen-THPL-O")
                || type.equalsIgnoreCase("HB-UW-Sen-THPL-I")) {
            UniversalSensorView(v, hmc);
        } else if (type.equalsIgnoreCase("HM-Sen-EP")) {
            ActivateView(v, hmc);
        } else if (type.equalsIgnoreCase("HM-Sen-Wa-OD") || type.equalsIgnoreCase("HM-Sen-Wa-Od")) {
            FillingView(v, hmc);
        } else if (type.equalsIgnoreCase("HM-WDS30-T-O") || (type.equalsIgnoreCase("S550IA")) || (type.equalsIgnoreCase(
                "HM-WS550STH-I")) || (type.equalsIgnoreCase("HM-WDS30-OT2-SM")) || (type.equalsIgnoreCase("HM-WDS30-OT2-SM-2"))) {
            TemperatureView2(v, hmc);
        } else if (type.startsWith("HM-Sec-WDS")) {
            WaterView(v, hmc);
        } else if (type.equalsIgnoreCase("HM-CC-SCD")) {
            WaterView(v, hmc);
        } else if (type.equalsIgnoreCase("HM-Sen-RD-O")) {
            if (hmc.channelIndex == 1) {
                RainingView(v, hmc);
            } else if (hmc.channelIndex == 2) {
                SwitchView(v, hmc, null, null, null, null);
            } else {
                v = null;
            }
        } else if (type.equalsIgnoreCase("HM-Sec-Win")) {
            if (hmc.channelIndex == 1) {
                VariableView(v, hmc, 0, 100, "%", HmType.WINDOW, R.drawable.flat_locked, R.drawable.flat_unlocked);
            } else {
                VariableView(v, hmc, 0, 100, "% Bat", HmType.WINDOW, R.drawable.flat_battery, R.drawable.flat_battery);
                v.setClickable(false);
            }
        } else if (type.equalsIgnoreCase("HMW-LC-Sw2-DR")) {
            if (hmc.channelIndex <= 2) {
                TasterView(v, hmc);
                // v = SwitchView(cData, inflater, rowId);
            } else if (hmc.channelIndex > 2) {
                SwitchView(v, hmc);
            }
        } else if (type.equalsIgnoreCase("HM-CCU-1")) {
            if (hmc.channelIndex == 1) {
                CCUView(v, hmc);
            } else if (hmc.channelIndex == 2) {
                SwitchView(v, hmc, ctx.getResources().getString(R.string.active),
                        ctx.getResources().getString(R.string.inactive), null, null);
            }
        } else if (type.equalsIgnoreCase("HM-LC-RGBW-WM")) {
            if (hmc.channelIndex == 1) {
                v = VariableView(v, hmc, 0, 100, "%", HmType.DIMMER, R.drawable.flat_light_off_2, R.drawable.flat_light_on_2);
            } else if (hmc.channelIndex == 2) {
                v = ColorView(v, hmc);
            } else if (hmc.channelIndex == 3) {
                v = ColorProgramView(v, hmc);
            } else {
                v = null;
            }
        } else if (type.equalsIgnoreCase("HM-EM-CCM") || type.equals("HM-EM-CMM")) {
            FS20CounterView(v, hmc);
        } else if (type.equalsIgnoreCase("HM-Sys-sRP-Pl")) {
            v = null;
        } else if (Util.startsWithIgnoreCase(type, "HMIP-eTRV")) {
            if (hmc.channelIndex == 0) {
                BatteryView(v, hmc);
            } else if (hmc.channelIndex == 1) {
                ThermostatIpView(v, hmc, 5, 30, "°C");
            } else {
                v = null;
            }
        } else if (Util.startsWithIgnoreCase(type, "HmIP-WTH") || type.equalsIgnoreCase("HmIPW-WTH") || Util.startsWithIgnoreCase(type, "HmIP-BWTH") || Util.startsWithIgnoreCase(type, "HmIP-STH") || Util.startsWithIgnoreCase(type, "HmIPW-STH") || type.equals("ALPHA-IP-RBG")) {
            if (hmc.channelIndex == 0) {
                BatteryView(v, hmc);
            } else if (hmc.channelIndex == 1) {
                ClimateControlIpView(v, hmc, 5, 30, "°C");
            } else if (hmc.channelIndex >= 9) {
                StateView(v, hmc, R.drawable.btn_check_on_holo_dark_hm, R.drawable.btn_check_off_holo_dark_hm);
            } else {
                v = null;
            }
        } else if (type.equalsIgnoreCase("HmIP-HEATING")) {
            if (hmc.channelIndex == 1) {
                ClimateControlIpView(v, hmc, 5, 30, "°C");
            } else if (hmc.channelIndex == 3) {
                WindowView(v, hmc);
            } else if (hmc.channelIndex == 4) {
                StateView(v, hmc, R.drawable.btn_check_on_holo_dark_hm, R.drawable.btn_check_off_holo_dark_hm);
            }
        } else if (type.equalsIgnoreCase("HmIP-SCTH230")) {
            DynamicDatapoints(v, hmc);
        } else if (type.equalsIgnoreCase("HmIP-SFD")) {
            if (hmc.channelIndex == 1) {
                DustView(v, hmc);
            }
        } else if (type.equals("HmIP-FALMOT-C12") || type.equals("HmIPW-FALMOT-C12")) {
            if (hmc.channelIndex <= 12) {
                FloorHeatingLevel(v, hmc);
            }
        } else if (type.equalsIgnoreCase("HMIP-PS")) {
            if (hmc.channelIndex == 2) {
                StateView(v, hmc, R.drawable.btn_check_on_holo_dark_hm, R.drawable.btn_check_off_holo_dark_hm);
            } else if (hmc.channelIndex <= 5) {
                SwitchView(v, hmc);
            } else {
                IpWeekProgramView(v, hmc);
            }
        } else if (Util.startsWithIgnoreCase(type, "HmIP-PSM")) {
            DynamicDatapoints(v, hmc);
        } else if (Util.startsWithIgnoreCase(type, "HmIP-USBSM")) {
            DynamicDatapoints(v, hmc);
        } else if (type.equalsIgnoreCase("HmIP-BSM") || Util.startsWithIgnoreCase(type, "HmIP-FSM")) {
            selectIPPowerMeterView(v, hmc);
        } else if (type.equals("HmIP-BDT") || type.equals("HmIP-PDT")) {
            if (hmc.channelIndex < 3) {
                TasterView(v, hmc);
            } else {
                VariableView(v, hmc, 0, 100, "%", HmType.DIMMER_IP, R.drawable.flat_light_off_2, R.drawable.flat_light_on_2);
            }
        } else if (type.equals("HmIP-FDT")) {
            VariableView(v, hmc, 0, 100, "%", HmType.DIMMER_IP, R.drawable.flat_light_off_2, R.drawable.flat_light_on_2);
        } else if (type.equalsIgnoreCase("HmIP-SWD")) {
            if (hmc.channelIndex == 0) {
                BatteryView(v, hmc);
            } else {
                AlarmStateView(v, hmc);
            }
        } else if (Util.startsWithIgnoreCase(type, "HMIP-SWDO") || type.equalsIgnoreCase("HmIP-SCI") || type.startsWith("HmIP-SWDM")) {
            if (hmc.channelIndex == 0) {
                BatteryView(v, hmc);
            } else if (hmc.channelIndex == 1) {
                OpenClosedView(v, hmc);
            } else {
                v = null;
            }
        } else if (Util.startsWithIgnoreCase(type, "HmIP-WRC") || Util.startsWithIgnoreCase(type, "HmIPW-WRC")) {
            if (hmc.channelIndex == 0) {
                BatteryView(v, hmc);
            } else {
                TasterView(v, hmc);
            }
        } else if (type.equals("HmIP-SMI") || type.startsWith("HmIP-SMO")) {
            MotionIPView(v, hmc);
        } else if (type.equals("HmIP-SAM")) {
            if (hmc.channelIndex == 0) {
                BatteryView(v, hmc);
            } else {
                MotionIPOnlyView(v, hmc);
            }
        } else if (type.equals("HmIP-SMI55") || type.startsWith("HmIPW-SMI55")) {
            if (hmc.channelIndex == 0) {
                BatteryView(v, hmc);
            } else if (hmc.channelIndex <= 2) {
                TasterView(v, hmc);
            } else {
                MotionIPView(v, hmc);
            }
        } else if (type.equals("HmIP-SPI") || type.equals("HmIPW-SPI")) {
            if (hmc.channelIndex == 0) {
                BatteryView(v, hmc);
            } else {
                PresenceIPView(v, hmc);
            }
        } else if (type.equals("HmIP-SRH")) {
            if (hmc.channelIndex == 0) {
                BatteryView(v, hmc);
            } else {
                WindowView(v, hmc);
            }
        } else if (type.equals("HmIP-SWSD")) {
            SmokeIPView(v, hmc);
        } else if (type.equals("HmIP-SPDR")) {
            if (hmc.channelIndex == 0) {
                BatteryView(v, hmc);
            } else {
                DirectionIPView(v, hmc);
            }
        } else if (type.equals("HmIP-ASIR")) {
            if (hmc.channelIndex == 1) {
                TasterView(v, hmc);
            } else if (hmc.channelIndex == 3) {
                StateView(v, hmc, R.drawable.flat_alarm, R.drawable.flat_unlocked);
                setIcon(v, R.drawable.icon_gahle1);
            } else {
                v = null;
            }
        } else if (type.equals(" HmIP-ASIR-B1")) {
            if (hmc.channelIndex == 1) {
                TasterView(v, hmc);
            } else if (hmc.channelIndex == 2) {
                SwitchView(v, hmc);
            } else if (hmc.channelIndex == 3) {
                StateView(v, hmc, R.drawable.flat_alarm, R.drawable.flat_unlocked);
                setIcon(v, R.drawable.icon_gahle1);
            }
        } else if (type.equals("HmIP-ASIR-O") || type.equals("HmIP-ASIR-2")) {
            if (hmc.channelIndex == 0) {
                BatteryView(v, hmc);
            } else if (hmc.channelIndex == 3) {
                AlarmSireneStateView(v, hmc);
            } else {
                v = null;
            }
        } else if (type.equals("HmIP-MP3P")) {
            if (hmc.channelIndex == 1) {
                SoundLevelIpView(v, hmc);
            } else if (hmc.channelIndex == 2) {
                VariableView(v, hmc, 0, 100, "%", HmType.MP3_IP_SOUND_FILE, R.drawable.flat_sound_off, R.drawable.flat_sound_on);
            } else if (hmc.channelIndex <= 4) {
                VariableView(v, hmc, 0, 100, "%", HmType.MP3_IP_SOUND, R.drawable.flat_sound_off, R.drawable.flat_sound_on);
            } else if (hmc.channelIndex == 5) {
                ColorLevelIpView(v, hmc);
            } else if (hmc.channelIndex <= 8) {
                VariableView(v, hmc, 0, 100, "%", HmType.DIMMER_MP3_COLOR, R.drawable.flat_light_off_2, R.drawable.flat_light_on_2);
            } else {
                IpWeekProgramView(v, hmc);
            }
        } else if (Util.startsWithIgnoreCase(type, "HmIP-KRC")) {
            if (hmc.channelIndex == 0) {
                BatteryView(v, hmc);
            } else {
                TasterView(v, hmc);
            }
        } else if (Util.startsWithIgnoreCase(type, "HmIP-RC")) {
            if (hmc.channelIndex == 0) {
                BatteryView(v, hmc);
            } else {
                TasterView(v, hmc);
            }
        } else if (type.equals("HmIP-FBL") || type.equals("HmIP-BBL")) {
            if (hmc.channelIndex < 3) {
                TasterView(v, hmc);
            } else if (hmc.channelIndex == 3) {
                BlindHeightIpView(v, hmc);
            } else if (hmc.channelIndex < 7) {
                VariableView(v, hmc, 0, 100, "%", HmType.BLIND_WITH_LAMELLA_IP, R.drawable.flat_blinds_closed, R.drawable.flat_blinds_up);
            } else {
                IpWeekProgramView(v, hmc);
            }
        } else if (type.equals("HmIPW-DRD3")) {
            if (hmc.channelIndex == 13) {
                IpWeekProgramView(v, hmc);
            } else {
                VariableView(v, hmc, 0, 100, "%", HmType.DIMMER_IP, R.drawable.flat_light_off_2, R.drawable.flat_light_on_2);
            }
        } else if (type.equals("HmIPW-DRBL4")) {
            if (hmc.channelIndex == 17) {
                IpWeekProgramView(v, hmc);
            } else {
                VariableView(v, hmc, 0, 100, "%", HmType.BLIND_WITH_LAMELLA_IP, R.drawable.flat_blinds_closed, R.drawable.flat_blinds_up);
            }
        } else if (type.equals("HmIP-HDM1") || type.equals("HmIP-HDM2")) {
            if (hmc.channelIndex == 2) {
                IpWeekProgramView(v, hmc);
            } else {
                VariableView(v, hmc, 0, 100, "%", HmType.BLIND_WITH_LAMELLA_IP, R.drawable.flat_blinds_closed, R.drawable.flat_blinds_up);
            }
        } else if (type.equals("HmIP-FROLL") || type.equals("HmIP-BROLL")) {
            if (hmc.channelIndex < 3) {
                TasterView(v, hmc);
            } else if (hmc.channelIndex == 3) {
                BlindHeightIpView(v, hmc);
            } else if (hmc.channelIndex < 7) {
                VariableView(v, hmc, 0, 100, "%", HmType.BLIND, R.drawable.flat_blinds_closed, R.drawable.flat_blinds_up);
            } else {
                v = IpWeekProgramView(v, hmc);
            }
        } else if (type.equals("HmIP-PCBS") || type.equals("HmIP-PCBS-BAT")) {
            if (hmc.channelIndex == 0) {
                BatteryView(v, hmc);
            } else if (hmc.channelIndex == 1) {
                TasterView(v, hmc);
            }/* else if (hmc.channelIndex == 6) {
                IpWeekProgramView(v, hmc);
            }*/ else {
                SwitchView(v, hmc);
            }
        } else if (type.equals("HmIP-PCBS2")) {
            if (hmc.channelIndex <= 2) {
                TasterView(v, hmc);
            } else if (hmc.channelIndex <= 10) {
                SwitchView(v, hmc);
            } else {
                IpWeekProgramView(v, hmc);
            }
        } else if (type.equals("HmIP-BRC2") || type.equals("HmIP-DBB")) {
            if (hmc.channelIndex == 0) {
                BatteryView(v, hmc);
            } else {
                TasterView(v, hmc);
            }
        } else if (type.equals("HmIP-MOD-RC8")) {
            ModeView(v, hmc);
        } else if (type.equals("HmIP-MOD-OC8")) {
            if (hmc.channelIndex <= 8) {
                TasterView(v, hmc);
            } else if (hmc.channelIndex <= 40) {
                SwitchView(v, hmc);
            } else {
                v = IpWeekProgramView(v, hmc);
            }
            //channel 41 unsupported
        } else if (type.startsWith("HmIP-SWO")) {
            if (hmc.channelIndex == 1) {
                v = WeatherStationIPView(v, hmc);
            } else {
                // other channels have no datapoints
            }
        } else if (type.equals("HM-Sen-LI-O")) {
            LuxView(v, hmc);
        } else if (type.equals("HM-Sec-Sir-WM")) {
            if ((hmc.channelIndex == 4)) {
                AlarmView(v, hmc);
            } else {
                SwitchView(v, hmc, ctx.getString(R.string.on), ctx.getString(R.string.off), R.drawable.flat_alarm, null);
                setIcon(v, R.drawable.icon_gahle1);
            }
        } else if (type.equals("HM-LC-AO-SM") || type.equals("HM-LC-DW-WM") || type.equals("HM-DW-WM")) {
            VariableView(v, hmc, 0, 100, "%", HmType.DIMMER, R.drawable.flat_light_off_2, R.drawable.flat_light_on_2);
        } else if (type.equals("VIR-OL-GTW") || type.equals("VIR-HUE-GTW")) {
            //nothing to do here, just show the name
        } else if (type.equals("VIR-LG-ONOFF")) {
            LightifyPlugView(v, hmc);
        } else if (type.equals("VIR-LG-WHITE")) {
            SwitchView(v, hmc);
        } else if (type.equals("VIR-LG-WHITE-DIM") || type.equals("VIR-LG-DIM") || type.equals("VIR-OL-GTW")) {
            VariableView(v, hmc, 0, 100, "%", HmType.LIGHTIFY_DIMMER, R.drawable.flat_light_off_2, R.drawable.flat_light_on_2);
        } else if (type.equals("VIR-LG-RGBW-DIM") || type.equals("VIR-LG-RGB-DIM") || type.equals("VIR-LG-GROUP")) {
            VariableView(v, hmc, 0, 100, "%", HmType.LIGHTIFY_DIMMER_RGBW, R.drawable.flat_light_off_2, R.drawable.flat_light_on_2);
        } else if (type.startsWith("HmIP-FAL230-C") || type.startsWith("HmIPW-FAL230-C") || type.startsWith("HmIP-FAL24") || type.startsWith("HmIPW-FAL24")) {
            StateView(v, hmc, R.drawable.btn_check_on_holo_dark_hm, R.drawable.btn_check_off_holo_dark_hm);
        } else if (type.equals("HmIP-PMFS")) {
            PowerFailureView(v, hmc);
        } else if (type.equals("HmIP-MIOB")) {
            if (hmc.channelIndex <= 8) {
                SwitchView(v, hmc);
            } else if (hmc.channelIndex == 11) {
                VariableView(v, hmc, 0, 100, "%", HmType.DIMMER, R.drawable.flat_light_off_2, R.drawable.flat_light_on_2);
            }
        } else if (type.equals("HmIP-MIO16-PCB")) {
            if (hmc.channelIndex <= 8) {
                SwitchView(v, hmc);
            } else if (hmc.channelIndex == 11) {
                VariableView(v, hmc, 0, 100, "%", HmType.DIMMER, R.drawable.flat_light_off_2, R.drawable.flat_light_on_2);
            }
        } else if (type.equals("HmIP-BSL")) {
            if (hmc.channelIndex <= 2) {
                TasterView(v, hmc);
            } else if (hmc.channelIndex <= 6) {
                SwitchView(v, hmc);
            } else if (hmc.channelIndex == 7) {
                ColorLevelIpView(v, hmc);
            } else if (hmc.channelIndex <= 10) {
                VariableView(v, hmc, 0, 100, "%", HmType.DIMMER_IP_COLOR, R.drawable.flat_light_off_2, R.drawable.flat_light_on_2);
            } else if (hmc.channelIndex == 11) {
                ColorLevelIpView(v, hmc);
            } else if (hmc.channelIndex <= 14) {
                VariableView(v, hmc, 0, 100, "%", HmType.DIMMER_IP_COLOR, R.drawable.flat_light_off_2, R.drawable.flat_light_on_2);
            } else {
                IpWeekProgramView(v, hmc);
            }
        } else if (type.equals("HmIP-WGC")) {
            if (hmc.channelIndex == 1) {
                TasterView(v, hmc);
            } else {
                SwitchView(v, hmc);
            }
        } else if (type.equals("HmIP-MOD-TM") || type.equals("HmIP-MOD-HO")) {
            if (hmc.channelIndex == 1) {
                GarageDoorIpView(v, hmc);
            } else if (hmc.channelIndex == 2) {
                SwitchView(v, hmc, null, null, R.drawable.flat_light_on_2, R.drawable.flat_light_off_2);
            }
        } else if (type.equals("HmIP-ASIR-B1")) {
            if (hmc.channelIndex == 1) {
                TasterView(v, hmc);
            } else {
                SwitchView(v, hmc);
            }
        } else if (type.startsWith("HBW-SD6-Multikey")) {
            if (hmc.channelIndex <= 6) {
                TasterView(v, hmc);
            } else if (hmc.channelIndex <= 12) {
                VariableView(v, hmc, 0, 100, "%", HmType.DIMMER, R.drawable.flat_light_off_2, R.drawable.flat_light_on_2);
            } else if (hmc.channelIndex == 13) {
                TemperatureView2(v, hmc);
            } else if (hmc.channelIndex == 14) {
                Int1000ViewVent(v, hmc);
            } else {
                NotSupportedView(v, hmc);
            }
        } else if (type.startsWith("HBW-IO-12-1W-UP")) {
            if (hmc.channelIndex <= 12) {
                TasterView(v, hmc);
            } else if (hmc.channelIndex <= 24) {
                VariableView(v, hmc, 0, 100, "%", HmType.DIMMER, R.drawable.flat_light_off_2, R.drawable.flat_light_on_2);
            } else if (hmc.channelIndex <= 30) {
                TemperatureView2(v, hmc);
            } else if (hmc.channelIndex == 32) {
                Int1000ViewVent(v, hmc);
            } else {
                NotSupportedView(v, hmc);
            }
        } else if (type.equals("HmIP-SLO")) {
            LuxIPView(v, hmc);
        } else if (type.equals("HmIPW-DRS4")) {
            if (hmc.channelIndex == 17) {
                IpWeekProgramView(v, hmc);
            } else {
                SwitchView(v, hmc);
            }
        } else if (type.equals("HmIPW-DRS8")) {
            if (hmc.channelIndex == 33) {
                IpWeekProgramView(v, hmc);
            } else {
                SwitchView(v, hmc);
            }
        } else if (type.equals("HmIP-WHS2")) {
            if (hmc.channelIndex <= 8) {
                SwitchView(v, hmc);
            } else {
                IpWeekProgramView(v, hmc);
            }
        } else if (type.equals("HmIPW-FIO6")) {
            if (hmc.channelIndex <= 6) {
                TasterView(v, hmc);
            } else if (hmc.channelIndex <= 30) {
                SwitchView(v, hmc);
            } else {
                IpWeekProgramView(v, hmc);
            }
        } else if (type.equals("HmIP-DRSI4")) {
            if (hmc.channelIndex <= 4) {
                TasterView(v, hmc);
            } else if (hmc.channelIndex <= 20) {
                SwitchView(v, hmc);
            } else {
                IpWeekProgramView(v, hmc);
            }
        } else if (type.equals("HmIP-DRSI1")) {
            if (hmc.channelIndex == 1) {
                TasterView(v, hmc);
            } else if (hmc.channelIndex <= 5) {
                SwitchView(v, hmc);
            } else {
                IpWeekProgramView(v, hmc);
            }
        } else if (type.equals("HmIP-DRDI3")) {
            if (hmc.channelIndex <= 3) {
                TasterAndStateView(v, hmc);
            } else if (hmc.channelIndex <= 15) {
                VariableView(v, hmc, 0, 100, "%", HmType.DIMMER, R.drawable.flat_light_off_2, R.drawable.flat_light_on_2);
            } else {
                IpWeekProgramView(v, hmc);
            }
        } else if (type.equals("HmIP-DRBLI4")) {
            if (hmc.channelIndex <= 8) {
                TasterAndStateView(v, hmc);
            } else if (hmc.channelIndex <= 24) {
                VariableView(v, hmc, 0, 100, "%", HmType.BLIND_WITH_LAMELLA, R.drawable.flat_blinds_closed, R.drawable.flat_blinds_up);
            } else {
                IpWeekProgramView(v, hmc);
            }
        } else if (type.equals("HmIP-FSI16")) {
            if (hmc.channelIndex == 1) {
                TasterView(v, hmc);
            } else if (hmc.channelIndex < 6) {
                SwitchView(v, hmc);
            } else {
                IpWeekProgramView(v, hmc);
            }
        } else if (type.equals("HmIP-SRD")) {
            RainingIPView(v, hmc);
        } else if (type.equals("HmIP-STE2-PCB")) {
            TemperatureIPView(v, hmc);
        } else if (type.equals("EASYCam") || type.equals("Outdoor-EASYCam")) {
            EasyCamView(v, hmc);
        } else if (type.equals("EASYLed") || type.equals("EASYLed2")) {
            SwitchView(v, hmc);
        } else if (type.equals("EASYOpenWeatherMap")) {
            TemperatureView2(v, hmc);
        } else if (type.equals("10000")) {
            WeatherCuxdView(v, hmc);
        } else if (type.startsWith("HB-UNI-Sen-TEMP-")) {
            TemperatureView2(v, hmc);
        } else if (type.equals("HB-UNI-Sen-CAP-MOIST")) {
            HumidityView(v, hmc);
        } else if (type.equals("HB-UNI-DMX-Master")) {
            if (hmc.channelIndex == 1 || hmc.channelIndex == 3) {
                TasterView(v, hmc);
            }
        } else if (type.equals("RPI-RF-MOD") || type.equals("HmIP-HAP")) {
            v = null;
        } else {
            if (PreferenceHelper.isHideUnsupported(ctx)) {
                v = null;
            } else {
                NotSupportedView(v, hmc);
            }
        }

//        if (v != null) {
//            addConnectedVariables(v, hmc);
//        }

        return v;
    }

    private void addConnectedVariables(View v, HMChannel hmc) {
        for (HMObject hmObject : DbUtil.getConnectedVariables(hmc.rowId)) {

            HMVariable hmv = (HMVariable) hmObject;

            String name = isWidget ? null : hmv.name;
            switch (hmv.vartype) {
                case "2": {
                    CheckedTextView cb = mViewAdder.addNewChildView(v, null, null, name, null, true, false).findViewById(R.id.check);
                    if (hmv.value.equals("true")) {
                        cb.setChecked(true);
                    } else if (hmv.value.equals("false")) {
                        cb.setChecked(false);
                    } else {
                        cb.setChecked(false);
                    }
                    break;
                }
                case "4": {
                    String value = hmv.value;

                    if (Integer.parseInt(hmv.vartype) == 4) {
                        float d;
                        try {
                            d = Float.parseFloat(value);
                            value = Double.toString(Math.round(d * 100) / 100.);
                        } catch (Exception e) {
                            d = 0f;
                        }

                        if (hmv.min == -1) {
                            hmv.min = 0;
                            hmv.max = 100;
                        }

                        mViewAdder.addNewChildView(v, null, null, name, value + " " + hmv.unit, false, false);
                    } else {
                        mViewAdder.addNewChildView(v, null, null, name, value, false, false);
                    }
                    break;
                }
                case "16": {
                    try {
                        int index = Integer.parseInt(hmv.value.replaceAll("[^\\d.]", ""));
                        String[] values = hmv.value_list.split(";");

                        mViewAdder.addNewChildView(v, null, null, name, values[index] + " " + hmv.unit, false, false);
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                    break;
                }
                case "20": {
                    String value = Html.fromHtml(hmv.value).toString();
                    if (hmv.value.matches(".*\\<[^>]+>.*")) {
                        WebView webView = new WebView(ctx);
                        webView.loadDataWithBaseURL(null, hmv.value, "text/html", "utf-8", null);
                        ((LinearLayout) v.findViewById(R.id.valueLL)).addView(webView);
                    } else {
                        mViewAdder.addNewChildView(v, null, null, name, value + " " + hmv.unit, false, false);
                    }
                    break;
                }
            }
        }
    }

    private void HumidityView(View v, HMChannel hmc) {
        Double humidity = DbUtil.getDatapointDouble(hmc.rowId, "HUMIDITY");
        if (humidity != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_humidity, Double.toString(humidity) + "%");
        }

        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
    }

    private void EasyCamView(View v, HMChannel hmc) {
        if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {

            String snapshotUrl = DbUtil.getDatapointString(hmc.getRowId(), "SNAPSHOT_URL");
            final WebCam webCam = new WebCam("EasyCam", snapshotUrl, WebCam.TYPE_JPEG);

            setOnClickListener(v, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    WebcamHelper.showWebCam(ctx, webCam, new Handler(Looper.getMainLooper()));
                }
            });
        }
    }

    private void selectIPPowerMeterView(View v, HMChannel hmc) {
        if (DbUtil.hasDatapoint(hmc.rowId, "PRESS_SHORT")) {
            TasterView(v, hmc);
        } else if (DbUtil.hasDatapoint(hmc.rowId, "STATE")) {
            SwitchView(v, hmc);
        } else if (DbUtil.hasDatapoint(hmc.rowId, "ENERGY_COUNTER")) {
            PowerMeterView(v, hmc);
        } else if (DbUtil.hasDatapoint(hmc.rowId, "WEEK_PROGRAM_CHANNEL_LOCKS")) {
            IpWeekProgramView(v, hmc);
        }
    }

    /**
     * Try for handling all devices in a generic, dynamic way
     **/
    private void DynamicDatapoints(View view, HMChannel hmc) {
        ArrayList<HmDatapoint> datapoints = DbUtil.getDatapoints(hmc.getRowId());

        if (datapoints == null) {
            return;
        }

        for (HmDatapoint datapoint : datapoints) {
            switch (datapoint.getPoint_type()) {
                case "ACTUAL_TEMPERATURE":
                    double temperature = Double.parseDouble(datapoint.getValue());

                    // TODO: get valueunit from api, store it to database, extend the model and use here instead of fixed "°C"
                    mViewAdder.addNewValue(view, R.drawable.flat_temp, Math.round(temperature * 10) / 10. + "°C");
                    break;

                case "HUMIDITY":
                    int humidity = Integer.parseInt(datapoint.getValue());

                    // TODO: get valueunit from api, store it to database, extend the model and use here instead of fixed "%"
                    mViewAdder.addNewValue(view, R.drawable.flat_humidity, humidity + "%");
                    break;

                case "OPERATING_VOLTAGE":
                    double operatingVoltage = Double.parseDouble(datapoint.getValue());
                    if (operatingVoltage > 0) {
                        View setPointView = mViewAdder.addNewValue(view, R.drawable.flat_battery, Math.round(operatingVoltage * 10) / 10. + " V");

                        Boolean lowBattery = DbUtil.getDatapointBoolean(hmc.rowId, "LOW_BAT"); // TODO: Use datapoint from datapoints array list
                        if (lowBattery != null && lowBattery) {
                            ((TextView) setPointView.findViewById(R.id.value)).setTextColor(ctx.getResources().getColor(R.color.orange));
                        }
                    }
                    break;

                case "ENERGY_COUNTER":
                    double energyCounter = Double.parseDouble(datapoint.getValue());

                    // TODO: get valueunit from api, store it to database, extend the model and use here instead of fixed "Wh"
                    mViewAdder.addNewValue(view, R.drawable.flat_counter, Math.round(energyCounter * 10) / 10. + "Wh");
                    break;

                case "POWER":
                    double power = Double.parseDouble(datapoint.getValue());

                    // TODO: get valueunit from api, store it to database, extend the model and use here instead of fixed "W"
                    mViewAdder.addNewValue(view, R.drawable.flat_power, Math.round(power * 10) / 10. + "W");

                    setIcon(view, R.drawable.icon16);
                    break;

                case "CURRENT":
                    double current = Double.parseDouble(datapoint.getValue());

                    // TODO: get valueunit from api, store it to database, extend the model and use here instead of fixed "mA"
                    mViewAdder.addNewValue(view, R.drawable.flat_power, Math.round(current * 10) / 10. + "mA");
                    break;

                /*case "FREQUENCY":
                    double frequency = Double.parseDouble(datapoint.getValue());

                    // TODO: get valueunit from api, store it to database, extend the model and use here instead of fixed "Hz"
                    mViewAdder.addNewValue(view, R.drawable.flat_power, Math.round(frequency * 100) / 100. + "Hz");
                    break;*/

                /*case "VOLTAGE":
                    double voltage = Double.parseDouble(datapoint.getValue());

                    // TODO: get valueunit from api, store it to database, extend the model and use here instead of fixed "V"
                    mViewAdder.addNewValue(view, R.drawable.flat_power, Math.round(voltage * 100) / 100. + "V");
                    break;*/

                case "CONCENTRATION":
                    double concentration = Double.parseDouble(datapoint.getValue());

                    // TODO: get valueunit from api, store it to database, extend the model and use here instead of fixed "ppm"
                    mViewAdder.addNewValue(view, R.drawable.flat_humidity, concentration + "ppm");
                    break;

                case "LEVEL":
                    double level = Double.parseDouble(datapoint.getValue());

                    switch (datapoint.getOperations()) {
                        case 5: // show state only
                            mViewAdder.addNewValue(view, R.drawable.flat_counter, Math.round(level * 100) + "%");
                            break;

                        case 7: // switchable state
                            VariableView(view, hmc, 0, 100, "%", HmType.DIMMER_IP, R.drawable.flat_light_off_2, R.drawable.flat_light_on_2);
                            break;

                        default:
                            mViewAdder.addNewValue(view, R.drawable.flat_counter, Math.round(level * 100) + "%");
                            break;
                    }
                    break;

                case "STATE":
                    switch (datapoint.getOperations()) {
                        case 5: // show state only
                            String viewableState = DbUtil.getDatapointString(hmc.rowId, "STATE");

                            if (viewableState != null) {
                                switch (viewableState) {
                                    case "true":
                                        mViewAdder.addNewValue(view, R.drawable.btn_check_on_holo_dark_hm, ViewAdder.IconSize.BIG);
                                        break;
                                    case "false":
                                        mViewAdder.addNewValue(view, R.drawable.btn_check_off_holo_dark_hm, ViewAdder.IconSize.BIG);
                                        break;
                                }
                            }

                            setIcon(view, R.drawable.icon_new30);

                            view.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));

                            break;

                        case 7: // switchable state
                            SwitchView(view, hmc);
                            break;

                        default:
                            boolean state = Boolean.parseBoolean(datapoint.getValue());

                            if (state) {
                                mViewAdder.addNewValue(view, R.drawable.flat_unlocked, ViewAdder.IconSize.BIG);
                            } else {
                                mViewAdder.addNewValue(view, R.drawable.flat_locked, ViewAdder.IconSize.BIG);
                            }
                            break;
                    }
                    break;

                case "WEEK_PROGRAM_CHANNEL_LOCKS":
                    if (hmc.isIPDevice()) {
                        IpWeekProgramView(view, hmc);
                    }
                    break;
            }
        }
    }

    private View TemperatureView(View v, HMChannel hmc) {
        Double temperature = DbUtil.getDatapointDouble(hmc.rowId, "ACTUAL_TEMPERATURE");
        if (temperature != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_temp, Math.round(temperature * 10) / 10. + "°C");
        }

        Double humidity = DbUtil.getDatapointDouble(hmc.rowId, "HUMIDITY");
        if (humidity != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_humidity, humidity + "%");
        }

        return v;
    }

    private View ErrorView(View v, HMChannel hmc) {
        Integer error = DbUtil.getDatapointInt(hmc.rowId, "ERROR");
        if (error != null) {
            if (error == 0) {
                mViewAdder.addNewValue(v, "No Error");
            } else {
                mViewAdder.addNewValue(v, "Error " + error);
            }
        }

        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View SmokeIPView(View v, HMChannel hmc) {
        Integer error = DbUtil.getDatapointInt(hmc.rowId, "SMOKE_DETECTOR_ALARM_STATUS");
        if (error != null) {
            if (error == 0) {
                mViewAdder.addNewValue(v, R.drawable.flat_no_smoke, ViewAdder.IconSize.BIG);
            } else {
                mViewAdder.addNewValue(v, R.drawable.flat_alarm, ViewAdder.IconSize.BIG);
            }
        }

        setIcon(v, R.drawable.icon13);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View HVACView(View v, HMChannel hmc) {
        View hvacView = inflater.inflate(R.layout.hvac_layout, null);

        Integer mode = DbUtil.getDatapointInt(hmc.rowId, "MODE");
        if (mode != null) {
            if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
                setOnClickListener(hvacView.findViewById(R.id.hvac_mode), hvacHandler);
            }

            Util.setIconColor(isDarkTheme, (ImageView) hvacView.findViewById(R.id.hvac_mode));
            hvacView.findViewById(R.id.hvac_mode)
                    .setTag(new HMHvac(HMHvac.Type.MODE, mode, DbUtil.getDatapointId(hmc.rowId, "MODE")));

            switch (mode) {
                case 0:
                    ((ImageView) hvacView.findViewById(R.id.hvac_mode)).setImageResource(R.drawable.hvac_usermode_0);
                    break;
                case 1:
                    ((ImageView) hvacView.findViewById(R.id.hvac_mode)).setImageResource(R.drawable.hvac_usermode_1);
                    break;
                case 3:
                    ((ImageView) hvacView.findViewById(R.id.hvac_mode)).setImageResource(R.drawable.hvac_usermode_4);
                    break;
                case 9:
                    ((ImageView) hvacView.findViewById(R.id.hvac_mode)).setImageResource(R.drawable.hvac_usermode_3);
                    break;
                case 14:
                    ((ImageView) hvacView.findViewById(R.id.hvac_mode)).setImageResource(R.drawable.hvac_usermode_2);
                    break;
                default:
                    ((ImageView) hvacView.findViewById(R.id.hvac_mode)).setImageResource(R.drawable.hvac_unkown);
                    break;
            }
        }

        Integer vane = DbUtil.getDatapointInt(hmc.rowId, "VANE");
        if (vane != null) {
            Integer datapointId = DbUtil.getDatapointId(hmc.rowId, "VANE");

            Util.setIconColor(isDarkTheme, (ImageView) hvacView.findViewById(R.id.hvac_vane_left_right));
            if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
                hvacView.findViewById(R.id.hvac_vane_left_right).setOnClickListener(hvacHandler);
            }
            hvacView.findViewById(R.id.hvac_vane_left_right).setTag(new HMHvac(HMHvac.Type.VANE, vane, datapointId));

            switch (vane) {
                case 0:
                    ((ImageView) hvacView.findViewById(R.id.hvac_vane_left_right)).setImageResource(
                            R.drawable.hvac_dinamic_vanesh5_full);
                    break;
                case 1:
                    ((ImageView) hvacView.findViewById(R.id.hvac_vane_left_right)).setImageResource(
                            R.drawable.hvac_dinamic_vanesh5_1);
                    break;
                case 2:
                    ((ImageView) hvacView.findViewById(R.id.hvac_vane_left_right)).setImageResource(
                            R.drawable.hvac_dinamic_vanesh5_2);
                    break;
                case 3:
                    ((ImageView) hvacView.findViewById(R.id.hvac_vane_left_right)).setImageResource(
                            R.drawable.hvac_dinamic_vanesh5_3);
                    break;
                case 4:
                    ((ImageView) hvacView.findViewById(R.id.hvac_vane_left_right)).setImageResource(
                            R.drawable.hvac_dinamic_vanesh5_4);
                    break;
                default:
                    ((ImageView) hvacView.findViewById(R.id.hvac_vane_left_right)).setImageResource(
                            R.drawable.hvac_dinamic_vanesh5_4);
                    break;
            }

        }

        Integer speed = DbUtil.getDatapointInt(hmc.rowId, "SPEED");
        if (speed != null) {
            Integer datapointId = DbUtil.getDatapointId(hmc.rowId, "SPEED");

            Util.setIconColor(isDarkTheme, (ImageView) hvacView.findViewById(R.id.hvac_speed));
            if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
                hvacView.findViewById(R.id.hvac_speed).setOnClickListener(hvacHandler);
            }
            hvacView.findViewById(R.id.hvac_speed).setTag(new HMHvac(HMHvac.Type.SPEED, speed, datapointId));

            switch (speed) {
                case 0:
                    ((ImageView) hvacView.findViewById(R.id.hvac_speed)).setImageResource(R.drawable.hvac_usermode_0);
                    break;
                case 1:
                    ((ImageView) hvacView.findViewById(R.id.hvac_speed)).setImageResource(R.drawable.hvac_speed_1);
                    break;
                case 2:
                    ((ImageView) hvacView.findViewById(R.id.hvac_speed)).setImageResource(R.drawable.hvac_speed_2);
                    break;
                case 3:
                    ((ImageView) hvacView.findViewById(R.id.hvac_speed)).setImageResource(R.drawable.hvac_speed_3);
                    break;
                case 4:
                    ((ImageView) hvacView.findViewById(R.id.hvac_speed)).setImageResource(R.drawable.hvac_speed_4);
                    break;
                default:
                    ((ImageView) hvacView.findViewById(R.id.hvac_mode)).setImageResource(R.drawable.hvac_unkown);
                    break;
            }
        }

        Integer room = DbUtil.getDatapointInt(hmc.rowId, "ROOM");
        if (room != null) {
            Integer datapointId = DbUtil.getDatapointId(hmc.rowId, "ROOM");

            Util.setIconColor(isDarkTheme, (ImageView) hvacView.findViewById(R.id.hvac_room));
            if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
                hvacView.findViewById(R.id.hvac_room).setOnClickListener(hvacHandler);
            }
            hvacView.findViewById(R.id.hvac_room).setTag(new HMHvac(HMHvac.Type.ROOM, room, datapointId));

            switch (room) {
                case 0:
                    ((ImageView) hvacView.findViewById(R.id.hvac_room)).setImageResource(R.drawable.hvac_occup_0);
                    break;
                case 1:
                    ((ImageView) hvacView.findViewById(R.id.hvac_room)).setImageResource(R.drawable.hvac_occup_1);
                    break;
                case 2:
                    ((ImageView) hvacView.findViewById(R.id.hvac_room)).setImageResource(R.drawable.hvac_occup_2);
                    break;
                case 3:
                    ((ImageView) hvacView.findViewById(R.id.hvac_room)).setImageResource(R.drawable.hvac_occup_3);
                    break;
                default:
                    ((ImageView) hvacView.findViewById(R.id.hvac_room)).setImageResource(R.drawable.hvac_unkown);
                    break;
            }

        }

        ((LinearLayout) (v.findViewById(R.id.head))).addView(hvacView);

        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private void setOnClickListener(View view, OnClickListener onClickListener) {
        if (isClickable) {
            view.setOnClickListener(onClickListener);
        }
    }

    private void setOnLongClickListener(View view, OnLongClickListener onClickListener) {
        if (isClickable) {
            view.setOnLongClickListener(onClickListener);
        }
    }

    private View FillingView(View v, HMChannel hmc) {
        Double fillingLevel = DbUtil.getDatapointDouble(hmc.rowId, "FILLING_LEVEL");
        if (fillingLevel != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_filling, Double.toString(fillingLevel) + "%");
        }

        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    public View SelectVariableView(View v, HMVariable hmv) {
        int type = Integer.parseInt(hmv.vartype);

        if (type == 2) {
            return SwitchVarView(v, hmv);
        } else if (type == 20) {
            return StringVariableView(v, hmv);
        } else if (type == 16) {
            return ValueListVariableView(v, hmv);
        } else if (type == 4) {
            return SysVariableView(v, hmv);
        }
        return null;
    }

    private View GasMeterView(View v, HMChannel hmc) {
        Double gasEnergyCounter = DbUtil.getDatapointDouble(hmc.rowId, "GAS_ENERGY_COUNTER");
        if (gasEnergyCounter != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_counter,
                    Double.toString(Math.round(gasEnergyCounter * 1000) / 1000.) + "m³");
        }

        Double gasPower = DbUtil.getDatapointDouble(hmc.rowId, "GAS_POWER");
        if (gasPower != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_power, Double.toString(Math.round(gasPower * 1000) / 1000.) + "m³");
        }


        Double energyCounter = DbUtil.getDatapointDouble(hmc.rowId, "ENERGY_COUNTER");
        if (energyCounter != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_counter,
                    Double.toString(Math.round(energyCounter * 10) / 10.) + "Wh");
        }

        Double power = DbUtil.getDatapointDouble(hmc.rowId, "POWER");
        if (power != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_power, Double.toString(Math.round(power * 10) / 10.) + "W");
        }

        Double iecEnergyCounter = DbUtil.getDatapointDouble(hmc.rowId, "IEC_ENERGY_COUNTER");
        if (iecEnergyCounter != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_counter,
                    Double.toString(Math.round(iecEnergyCounter * 10) / 10.) + "Wh");
        }

        Double icePower = DbUtil.getDatapointDouble(hmc.rowId, "IEC_POWER");
        if (icePower != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_power, Double.toString(Math.round(icePower * 10) / 10.) + "W");
        }

        setIcon(v, R.drawable.icon_new33);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View PowerMeterView(View v, HMChannel hmc) {
        Double energyCounter = DbUtil.getDatapointDouble(hmc.rowId, "ENERGY_COUNTER");
        if (energyCounter != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_counter,
                    Double.toString(Math.round(energyCounter * 10) / 10.) + "Wh");
        }

        Double power = DbUtil.getDatapointDouble(hmc.rowId, "POWER");
        if (power != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_power, Double.toString(Math.round(power * 10) / 10.) + "W");
        }

        Double current = DbUtil.getDatapointDouble(hmc.rowId, "CURRENT");
        if (current != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_power, Math.round(current) + "mA");
        }

        setIcon(v, R.drawable.icon_new33);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View LuxView(View v, HMChannel hmc) {
        Double lux = DbUtil.getDatapointDouble(hmc.rowId, "LUX");
        if (lux != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_brightness, Double.toString(Math.round(lux * 100) / 100.) + " Lux");
        }

        setIcon(v, R.drawable.icon_gahle3);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View SysVariableView(View v, HMVariable hmv) {
        setOnClickListener(v, variableHandler);

        String value = hmv.value;

        if (Integer.parseInt(hmv.vartype) == 4) {
            Float d;
            try {
                d = Float.parseFloat(value);
                value = Double.toString(Math.round(d * 100) / 100.);
            } catch (Exception e) {
                d = 0f;
            }

            if (hmv.min == -1) {
                hmv.min = 0;
                hmv.max = 100;
            }

            mViewAdder.addNewValue(v, value + " " + hmv.unit);

            v.setTag(new HMControllableVar(hmv.rowId, hmv.name, hmv.unit, HmType.SYSVARIABLE, hmv.min, hmv.max, false,
                    hmv.unit, d, hmv.rowId));
        } else {
            mViewAdder.addNewValue(v, value);
            v.setTag(new HMControllable(hmv.rowId, hmv.name, hmv.value, HmType.SYSVARIABLE));
        }

        setIcon(v, R.drawable.icon18);
        return v;
    }

    private View ValueListVariableView(View v, HMVariable hmv) {
        if (!PreferenceHelper.isDisableNotOperate(ctx) || hmv.isOperate()) {
            setOnClickListener(v, valueListVariableHandler);
        }

        try {
            int index = Integer.parseInt(hmv.value.replaceAll("[^\\d.]", ""));
            String[] values = hmv.value_list.split(";");
            mViewAdder.addNewValue(v, values[index] + " " + hmv.unit);
        } catch (Exception e) {
            v.setOnClickListener(null);
        }

        setIcon(v, R.drawable.icon_new15);
        v.setTag(new HMControllable(hmv.rowId, hmv.name, hmv.value, hmv.value_list, HmType.SYSVARIABLE));
        return v;
    }

    private View StringVariableView(View v, HMVariable hmv) {
        if (!PreferenceHelper.isDisableNotOperate(ctx) || hmv.isOperate()) {
            setOnClickListener(v, stringVariableHandler);
        }

        String value = Html.fromHtml(hmv.value).toString();
        if (hmv.value.matches(".*\\<[^>]+>.*")) {
            WebView webView = new WebView(ctx);
            webView.loadDataWithBaseURL(null, hmv.value, "text/html", "utf-8", null);
            ((LinearLayout) v.findViewById(R.id.valueLL)).addView(webView);
        } else {
            mViewAdder.addNewValue(v, value + " " + hmv.unit);
        }

        setIcon(v, R.drawable.icon_new15);
        v.setTag(new HMControllable(hmv.rowId, hmv.name, value, HmType.SYSVARIABLE));
        return v;
    }

    private View ScriptView(View v, HMChannel hmc) {
        if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
            setOnClickListener(v, scriptDialogHandler);
        }

        View childView = mViewAdder.addNewChildView(v, null, null, null, null, true, true);
        CheckedTextView cb = (CheckedTextView) childView.findViewById(R.id.check);

        if (hmc.value.equals("true")) {
            cb.setCheckMarkDrawable(Util.getColoredIcon(ctx, isDarkTheme, R.drawable.btn_check_on_holo_dark_hm));
            cb.setChecked(true);
        } else {
            cb.setCheckMarkDrawable(Util.getColoredIcon(ctx, isDarkTheme, R.drawable.btn_check_off_holo_dark_hm));
            cb.setChecked(false);
        }
        cb.setEnabled(false);

        Button b = (Button) childView.findViewById(R.id.button);
        if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
            setOnClickListener(b, CheckedTextViewRunProgramHandler);

            cb.setEnabled(true);

            cb.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    ControlHelper.setProgramActive(ctx, hmc.getRowId(), !hmc.value.equals("true"), hmc.isVisible(), toastHandler);
                    Util.addRefreshNotify(((View) v.getParent().getParent().getParent().getParent()));
                }
            });

            //cb.setTag(new HMCheckable(hmc.rowId, hmc.name, levelString, hmType, null, null, lowerRes, upperRes, null));
        }
        b.setTag(new HMControllable(hmc.rowId, hmc.name, hmc.value, HmType.PROGRAM));

        setIcon(v, R.drawable.icon15);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, ((HMScript) hmc).getTimestamp(), HmType.PROGRAM));
        return v;
    }

    private View SoundLevelIpView(View v, HMChannel hmc) {
        Double level = DbUtil.getDatapointDouble(hmc.rowId, "LEVEL");
        if (level != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_sound_on,
                    Double.toString(Math.round(level * 100)) + "%");

            if (level > 0) {
                setIcon(v, (R.drawable.flat_sound_on));
            } else {
                setIcon(v, (R.drawable.flat_sound_off));
            }
        } else {
            setIcon(v, (R.drawable.flat_sound_off));
        }

        Integer soundFile = DbUtil.getDatapointInt(hmc.rowId, "SOUNDFILE");
        if (soundFile != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_list, soundFile.toString());
        }

        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View ColorLevelIpView(View v, HMChannel hmc) {
        String color = DbUtil.getDatapointString(hmc.rowId, "COLOR");
        if (color != null) {
            try {
                View view = mViewAdder.addNewValue(v, ColorSwitchIconHelper.getIcon(ctx, Integer.parseInt(color)), ViewAdder.IconSize.BIG);
                ImageView imageView = (ImageView) view.findViewById(R.id.value_icon_big);
                imageView.clearColorFilter();
            } catch (NumberFormatException e) {
                // foo
            }
        }

        Double level = DbUtil.getDatapointDouble(hmc.rowId, "LEVEL");
        if (level != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_light_on_2,
                    Double.toString(Math.round(level * 100)) + "%");

            if (level > 0) {
                setIcon(v, (R.drawable.icon_gahle3));
            } else {
                setIcon(v, (R.drawable.icon_gahle4));
            }
        } else {
            setIcon(v, (R.drawable.icon_gahle3));
        }

        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View LedDisplayView(View v, HMChannel hmc) {
        if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
            setOnClickListener(v, ledDialogHandler);
        }

        String ledStatus = DbUtil.getDatapointString(hmc.rowId, "LED_STATUS");
        if (ledStatus != null) {
            try {
                View view = mViewAdder.addNewValue(v, LedStatusIconHelper.getIcon(ctx, Integer.parseInt(ledStatus)), ViewAdder.IconSize.BIG);
                ImageView imageView = (ImageView) view.findViewById(R.id.value_icon_big);
                imageView.clearColorFilter();
            } catch (NumberFormatException e) {
                // foo
            }
        }

        Integer pressShortId = DbUtil.getDatapointId(hmc.rowId, "PRESS_SHORT");

        setIcon(v, (R.drawable.icon_new33));
        v.setTag(new HMControllable(pressShortId == null ? 0 : pressShortId, hmc.name, hmc.value, HmType.TASTER));
        return v;
    }

    private View WirelessWeatherStationView(View v, HMChannel hmc) {
        Double temperature = DbUtil.getDatapointDouble(hmc.rowId, "TEMPERATURE");
        if (temperature != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_temp, Double.toString(Math.round(temperature * 10) / 10.) + "°C");
        }

        Double humidity = DbUtil.getDatapointDouble(hmc.rowId, "HUMIDITY");
        if (humidity != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_humidity, Double.toString(humidity) + "%");
        }

        Integer airPressure = DbUtil.getDatapointInt(hmc.rowId, "AIR_PRESSURE");
        if (airPressure != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_pressure, Integer.toString(airPressure) + " hPa");
        }

        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View UniversalSensorView(View v, HMChannel hmc) {
        Double temperature = DbUtil.getDatapointDouble(hmc.rowId, "TEMPERATURE");
        if (temperature != null) {
            mViewAdder.addNewValue(v, null, R.string.temperature,
                    Double.toString(Math.round(temperature * 10) / 10.) + "°C");
        }

        Double humidity = DbUtil.getDatapointDouble(hmc.rowId, "HUMIDITY");
        if (humidity != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_humidity, Double.toString(humidity) + "%");
        }

        Integer airPressure = DbUtil.getDatapointInt(hmc.rowId, "AIR_PRESSURE");
        if (airPressure != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_pressure, Integer.toString(airPressure) + " hPa");
        }

        Double luminosity = DbUtil.getDatapointDouble(hmc.rowId, "LUMINOSITY");
        if (luminosity != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_luminosity, Double.toString(Math.round(luminosity)) + "%");
        }

        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View FS20CounterView(View v, HMChannel hmc) {
        Double counter = DbUtil.getDatapointDouble(hmc.rowId, "COUNTER");
        if (counter != null) {
            mViewAdder.addNewValue(v, null, "COUNTER", Double.toString(counter));
        }

        Double sum = DbUtil.getDatapointDouble(hmc.rowId, "SUM");
        if (sum != null) {
            mViewAdder.addNewValue(v, null, "SUM", Double.toString(sum));
        }

        Double mean5 = DbUtil.getDatapointDouble(hmc.rowId, "MEAN5MINUTES");
        if (mean5 != null) {
            mViewAdder.addNewValue(v, null, "MEAN5MINUTES", Double.toString(mean5));
        }

        Double max5 = DbUtil.getDatapointDouble(hmc.rowId, "MAX5MINUTES");
        if (max5 != null) {
            mViewAdder.addNewValue(v, null, "MAX5MINUTES", Double.toString(max5));
        }

        Double sum1H = DbUtil.getDatapointDouble(hmc.rowId, "SUM_1H");
        if (sum1H != null) {
            mViewAdder.addNewValue(v, null, "SUM_1H", Double.toString(sum1H));
        }

        Double max1H = DbUtil.getDatapointDouble(hmc.rowId, "MAX_1H");
        if (max1H != null) {
            mViewAdder.addNewValue(v, null, "MAX_1H", Double.toString(max1H));
        }

        Double sum24H = DbUtil.getDatapointDouble(hmc.rowId, "SUM_24H");
        if (sum24H != null) {
            mViewAdder.addNewValue(v, null, "SUM_24H", Double.toString(sum24H));
        }

        Double max24H = DbUtil.getDatapointDouble(hmc.rowId, "MAX_24H");
        if (max24H != null) {
            mViewAdder.addNewValue(v, null, "MAX_24H", Double.toString(max24H));
        }

        Double miss24H = DbUtil.getDatapointDouble(hmc.rowId, "MISS_24H");
        if (miss24H != null) {
            mViewAdder.addNewValue(v, null, "MISS_24H", Double.toString(miss24H));
        }

        Double meter = DbUtil.getDatapointDouble(hmc.rowId, "METER");
        if (meter != null) {
            mViewAdder.addNewValue(v, null, "METER", Double.toString(meter));
        }

        setIcon(v, R.drawable.icon19);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View LuxIPView(View v, HMChannel hmc) {
        Double illumination = DbUtil.getDatapointDouble(hmc.rowId, "CURRENT_ILLUMINATION");
        if (illumination != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_brightness,
                    Double.toString(Math.round(illumination * 10) / 10.) + " Lux");
        }

        setIcon(v, R.drawable.icon_gahle3);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View CO2View(View v, HMChannel hmc) {
        Double concentration = DbUtil.getDatapointDouble(hmc.rowId, "CONCENTRATION");
        if (concentration != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_humidity, (int) Math.round(concentration) + " ppm");
        }

        return v;
    }

    private View DustView(View v, HMChannel hmc) {
        TemperatureView(v, hmc);

        Double particleSize = DbUtil.getDatapointDouble(hmc.rowId, "TYPICAL_PARTICLE_SIZE");
        if (particleSize != null) {
            mViewAdder.addNewValue(v, Math.round(particleSize * 10) / 10. + "µm");
        }

        Double massConcentration25 = DbUtil.getDatapointDouble(hmc.rowId, "MASS_CONCENTRATION_PM_2_5");
        if (massConcentration25 != null) {
            mViewAdder.addNewValue(v, Math.round(massConcentration25 * 10) / 10. + "µg/m³");
        }

        Double numberConcentration25 = DbUtil.getDatapointDouble(hmc.rowId, "NUMBER_CONCENTRATION_PM_2_5");
        if (numberConcentration25 != null) {
            mViewAdder.addNewValue(v, Math.round(numberConcentration25 * 10) / 10. + "1/cm³");
        }

        Double massConcentration10 = DbUtil.getDatapointDouble(hmc.rowId, "MASS_CONCENTRATION_PM_10");
        if (massConcentration10 != null) {
            mViewAdder.addNewValue(v, Math.round(massConcentration10 * 10) / 10. + "µg/m³");
        }

        Double numberConcentration10 = DbUtil.getDatapointDouble(hmc.rowId, "NUMBER_CONCENTRATION_PM_10");
        if (numberConcentration10 != null) {
            mViewAdder.addNewValue(v, Math.round(numberConcentration10 * 10) / 10. + "1/cm³");
        }

        return v;
    }

    private View WeatherStationIPView(View v, HMChannel hmc) {
        Double temperature = DbUtil.getDatapointDouble(hmc.rowId, "ACTUAL_TEMPERATURE");
        if (temperature != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_temp, Double.toString(Math.round(temperature * 10) / 10.) + "°C");
        }

        Double humidity = DbUtil.getDatapointDouble(hmc.rowId, "HUMIDITY");
        if (humidity != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_humidity, Double.toString(humidity) + "%");
        }

        Double illumination = DbUtil.getDatapointDouble(hmc.rowId, "ILLUMINATION");
        if (illumination != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_brightness,
                    Double.toString(Math.round(illumination)));
        }

        String raining = DbUtil.getDatapointString(hmc.rowId, "RAINING");
        if (raining != null) {
            if (raining.equals("true")) {
                mViewAdder.addNewValue(v, R.drawable.flat_rain, ViewAdder.IconSize.BIG);
            } else if (raining.equals("false")) {
                mViewAdder.addNewValue(v, R.drawable.flat_sunny, ViewAdder.IconSize.BIG);
            }
        }

        Double rainCounter = DbUtil.getDatapointDouble(hmc.rowId, "RAIN_COUNTER");
        if (rainCounter != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_rain_counter,
                    Double.toString(Math.round(rainCounter * 10) / 10.) + "mm");
        }

        String sunshineDuration = DbUtil.getDatapointString(hmc.rowId, "SUNSHINEDURATION");
        if (sunshineDuration != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_sunshine, sunshineDuration);
        }

        Double windSpeed = DbUtil.getDatapointDouble(hmc.rowId, "WIND_SPEED");
        if (windSpeed != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_wind, Math.round(windSpeed * 10) / 10. + "km/h");
        }

        Double windDirection = DbUtil.getDatapointDouble(hmc.rowId, "WIND_DIR");
        Double windDirectionRange = DbUtil.getDatapointDouble(hmc.rowId, "WIND_DIR_RANGE");
        if (windDirection != null && windDirectionRange != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_wind_direction,
                    Double.toString(windDirection) + "° " + Util.getWindDirection(windDirection) + " / "
                            + Double.toString(windDirectionRange) + "°");
        }

        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View WeatherStationView(View v, HMChannel hmc) {
        String raining = DbUtil.getDatapointString(hmc.rowId, "RAINING");

        if (raining != null) {
            if (raining.equals("true")) {
                mViewAdder.addNewValue(v, R.drawable.flat_rain, ViewAdder.IconSize.BIG);
            } else if (raining.equals("false")) {
                mViewAdder.addNewValue(v, R.drawable.flat_sunny, ViewAdder.IconSize.BIG);
            }
        }

        Double temperature = DbUtil.getDatapointDouble(hmc.rowId, "TEMPERATURE");
        if (temperature != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_temp, Double.toString(Math.round(temperature * 10) / 10.) + "°C");
        }

        Double humidity = DbUtil.getDatapointDouble(hmc.rowId, "HUMIDITY");
        if (humidity != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_humidity, Double.toString(humidity) + "%");
        }

        Double windSpeed = DbUtil.getDatapointDouble(hmc.rowId, "WIND_SPEED");
        if (windSpeed != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_wind, Math.round(windSpeed * 10) / 10. + "km/h");
        }

        Double windDirection = DbUtil.getDatapointDouble(hmc.rowId, "WIND_DIRECTION");
        Double windDirectionRange = DbUtil.getDatapointDouble(hmc.rowId, "WIND_DIRECTION_RANGE");
        if (windDirection != null && windDirectionRange != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_wind_direction,
                    Double.toString(windDirection) + "° " + Util.getWindDirection(windDirection) + " / "
                            + Double.toString(windDirectionRange) + "°");
        }

        String sunshineDuration = DbUtil.getDatapointString(hmc.rowId, "SUNSHINEDURATION");
        if (sunshineDuration != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_sunshine, sunshineDuration);
        }

        Integer brightness = DbUtil.getDatapointInt(hmc.rowId, "BRIGHTNESS");
        if (brightness != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_sunny, Integer.toString(brightness));
        }

        Double rainCounter = DbUtil.getDatapointDouble(hmc.rowId, "RAIN_COUNTER");
        if (rainCounter != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_rain_counter,
                    Double.toString(Math.round(rainCounter * 10) / 10.) + "mm");
        }

        Double rainToday = DbUtil.getDatapointDoubleByName(hmc.rowId, "${sysVarRainToday}");
        if (rainToday != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_rain_counter, "Heute",
                    Double.toString(Math.round(rainToday * 10) / 10.) + "mm");
        }

        Double rainYesterday = DbUtil.getDatapointDoubleByName(hmc.rowId, "${sysVarRainYesterday}");
        if (rainYesterday != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_rain_counter, "Gestern",
                    Double.toString(Math.round(rainYesterday * 10) / 10.) + "mm");
        }

        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View ModeView(View v, HMChannel hmc) {
        if (DbUtil.getDatapointString(hmc.rowId, "PRESS_SHORT", "_id") != null) {
            return TasterView(v, hmc);
        } else if (DbUtil.getDatapointString(hmc.rowId, "PRESS") != null) {
            return SwitchView(v, hmc);
        } else {
            return StateView(v, hmc, R.drawable.flat_unlocked, R.drawable.flat_locked);
        }
    }

    private View BatteryView(View v, HMChannel hmc) {
        Boolean lowBattery = DbUtil.getDatapointBoolean(hmc.rowId, "LOW_BAT");

        Double battery = DbUtil.getDatapointDouble(hmc.rowId, "OPERATING_VOLTAGE");
        if (battery != null && battery > 0) {
            View setPointView = mViewAdder.addNewValue(v, R.drawable.flat_battery, Math.round(battery * 10) / 10. + " V");

            if (lowBattery != null && lowBattery) {
                ((TextView) setPointView.findViewById(R.id.value)).setTextColor(ctx.getResources().getColor(R.color.orange));
            }
        }

        return v;
    }

    private View TasterView(View v, HMChannel hmc) {
        Integer pressShortId = DbUtil.getDatapointId(hmc.rowId, "PRESS_SHORT");
        if (pressShortId == null) {
            pressShortId = 0;
        }

        Integer pressLongId = DbUtil.getDatapointId(hmc.rowId, "PRESS_LONG");
        if (pressLongId == null) {
            pressLongId = 0;
        }

        Button button = (Button) mViewAdder.addNewButtonView(v).findViewById(R.id.button);
        button.setVisibility(View.VISIBLE);
        if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
            setOnClickListener(v, tasterDialogHandler);

            final Integer finalPressShortId = pressShortId;

            setOnClickListener(button, new OnClickListener() {

                public void onClick(View arg0) {
                    ControlHelper.sendOrder(ctx, finalPressShortId, "1", toastHandler, false, true);
                }
            });

            final Integer finalPressLongId = pressLongId;
            setOnLongClickListener(button, new OnLongClickListener() {

                public boolean onLongClick(View arg0) {
                    ControlHelper.sendOrder(ctx, finalPressLongId, "1", toastHandler, false, true);
                    return true;
                }
            });

        }

        setIcon(v, R.drawable.icon15);
        v.setTag(new HMControllable(pressShortId, hmc.name, null, HmType.TASTER, pressLongId));
        return v;
    }

    private View TasterAndStateView(View v, HMChannel hmc) {
        Integer pressShortId = DbUtil.getDatapointId(hmc.rowId, "PRESS_SHORT");
        if (pressShortId == null) {
            pressShortId = 0;
        }

        Integer pressLongId = DbUtil.getDatapointId(hmc.rowId, "PRESS_LONG");
        if (pressLongId == null) {
            pressLongId = 0;
        }

        Button button = (Button) mViewAdder.addNewButtonView(v).findViewById(R.id.button);
        button.setVisibility(View.VISIBLE);
        if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
            setOnClickListener(v, tasterDialogHandler);

            final Integer finalPressShortId = pressShortId;

            setOnClickListener(button, new OnClickListener() {

                public void onClick(View arg0) {
                    ControlHelper.sendOrder(ctx, finalPressShortId, "1", toastHandler, false, true);
                }
            });

            final Integer finalPressLongId = pressLongId;
            setOnLongClickListener(button, new OnLongClickListener() {

                public boolean onLongClick(View arg0) {
                    ControlHelper.sendOrder(ctx, finalPressLongId, "1", toastHandler, false, true);
                    return true;
                }
            });
        }

        String state = DbUtil.getDatapointString(hmc.rowId, "STATE");
        if (state != null) {
            switch (state) {
                case "true":
                    mViewAdder.addNewValue(v, R.drawable.flat_unlocked, ViewAdder.IconSize.BIG);
                    break;
                case "false": {
                    mViewAdder.addNewValue(v, R.drawable.flat_locked, ViewAdder.IconSize.BIG);
                    break;
                }
                default: {
                    //don't show anything
                }
            }
        }

        setIcon(v, R.drawable.icon15);
        v.setTag(new HMControllable(pressShortId, hmc.name, null, HmType.TASTER, pressLongId));
        return v;
    }

    private View IpWeekProgramView(View v, final HMChannel hmc) {
        Integer weekWriteId = DbUtil.getDatapointId(hmc.rowId, "WEEK_PROGRAM_TARGET_CHANNEL_LOCK");
        if (weekWriteId == null) {
            weekWriteId = 0;
        }

        Integer weekWriteValue = DbUtil.getDatapointInt(hmc.rowId, "WEEK_PROGRAM_TARGET_CHANNEL_LOCK");
        if (weekWriteValue == null) {
            weekWriteValue = 0;
        }

        Integer weekReadId = DbUtil.getDatapointId(hmc.rowId, "WEEK_PROGRAM_CHANNEL_LOCKS");
        if (weekReadId == null) {
            weekReadId = 0;
        }

        Integer weekReadIdValue = DbUtil.getDatapointInt(hmc.rowId, "WEEK_PROGRAM_CHANNEL_LOCKS");
        if (weekReadIdValue == null) {
            weekReadIdValue = 0;
        }

        String status;
        switch (weekReadIdValue) {
            case 0:
                status = "MANU_MODE (0)";
                break;
            case 1:
                status = "AUTO_MODE_WITH_RESET(1)";
                break;
            case 2:
                status = "AUTO_MODE_WITHOUT_RESET(2)";
                break;
            default:
                status = "Unknown (" + weekReadIdValue + ")";
        }

        mViewAdder.addNewValue(v, status);

        if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
            final Integer finalWeekWriteId = weekWriteId;
            setOnClickListener(v, new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    View dialogView = inflater.inflate(R.layout.dialog_three_buttons_vertical, null);
                    final AlertDialog alertDialog = new AlertDialog.Builder(ctx).setView(dialogView).create();

                    Button buttonLeft = (Button) dialogView.findViewById(R.id.button_set);
                    buttonLeft.setText("MANU_MODE (0)");
                    buttonLeft.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ControlHelper.sendOrder(ctx, finalWeekWriteId, "0", toastHandler, false, true);
                            Util.addRefreshNotify(v);
                            alertDialog.dismiss();
                        }
                    });

                    Button buttonMiddle = (Button) dialogView.findViewById(R.id.button_middle);
                    buttonMiddle.setText("AUTO_MODE_WITH_RESET(1)");
                    buttonMiddle.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ControlHelper.sendOrder(ctx, finalWeekWriteId, "1", toastHandler, false, true);
                            Util.addRefreshNotify(v);
                            alertDialog.dismiss();
                        }
                    });

                    Button buttonRight = (Button) dialogView.findViewById(R.id.button_back);
                    buttonRight.setText("AUTO_MODE_WITHOUT_RESET(2)");
                    buttonRight.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ControlHelper.sendOrder(ctx, finalWeekWriteId, "2", toastHandler, false, true);
                            Util.addRefreshNotify(v);
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.setOnCancelListener(activityFinishCancelListener);
                    alertDialog.setOnDismissListener(activityFinishDimissListener);

                    alertDialog.setMessage(hmc.name);
                    alertDialog.show();
                    v.refreshDrawableState();
                }
            });
        }

        return v;
    }

    private View DoorBellView(View v, HMChannel hmc) {
        Integer pressShortId = DbUtil.getDatapointId(hmc.rowId, "PRESS_SHORT");
        if (pressShortId == null) {
            pressShortId = 0;
        }

        Button button = (Button) mViewAdder.addNewButtonView(v).findViewById(R.id.button);
        button.setVisibility(View.VISIBLE);
        if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
            setOnClickListener(v, doorbellDialogHandler);

            final Integer finalPressShortId = pressShortId;
            setOnClickListener(button, new OnClickListener() {

                public void onClick(View arg0) {
                    ControlHelper.sendOrder(ctx, finalPressShortId, "1", toastHandler, false, true);
                }
            });
        }

        setIcon(v, R.drawable.icon15);
        v.setTag(new HMControllable(pressShortId, hmc.name, null, HmType.DOORBELL));
        return v;
    }

    private View GarageDoorIpView(View v, final HMChannel hmc) {
        Integer doorState = DbUtil.getDatapointInt(hmc.rowId, "DOOR_STATE");
        if (doorState != null) {
            switch (doorState) {
                case 0:
                    mViewAdder.addNewValue(v, R.drawable.flat_locked, ViewAdder.IconSize.BIG);
                    break;
                case 1:
                    mViewAdder.addNewValue(v, R.drawable.flat_window_open, ViewAdder.IconSize.BIG, ctx.getString(R.string.opened));
                    break;
                case 2:
                    mViewAdder.addNewValue(v, R.drawable.flat_window_open, ViewAdder.IconSize.BIG, ctx.getString(R.string.tilted));
                    break;
                default:
                    mViewAdder.addNewValue(v, R.drawable.flat_window_open, ViewAdder.IconSize.BIG, "?");
                    break;
            }
        }

        if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
            setOnClickListener(v, new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final int datapointId = DbUtil.getDatapointId(hmc.rowId, "DOOR_COMMAND");

                    View dialogView = inflater.inflate(R.layout.dialog_garage_door, null);
                    final AlertDialog alertDialog = new AlertDialog.Builder(ctx).setView(dialogView).create();

                    dialogView.findViewById(R.id.button_open).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ControlHelper.sendOrder(ctx, datapointId, "1", toastHandler, false, true);
                            Util.addRefreshNotify(v);
                            alertDialog.dismiss();
                        }
                    });

                    dialogView.findViewById(R.id.button_stop).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ControlHelper.sendOrder(ctx, datapointId, "2", toastHandler, false, true);
                            Util.addRefreshNotify(v);
                            alertDialog.dismiss();
                        }
                    });

                    dialogView.findViewById(R.id.button_close).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ControlHelper.sendOrder(ctx, datapointId, "3", toastHandler, false, true);
                            Util.addRefreshNotify(v);
                            alertDialog.dismiss();
                        }
                    });

                    dialogView.findViewById(R.id.button_air).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ControlHelper.sendOrder(ctx, datapointId, "4", toastHandler, false, true);
                            Util.addRefreshNotify(v);
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.setOnCancelListener(activityFinishCancelListener);
                    alertDialog.setOnDismissListener(activityFinishDimissListener);

                    alertDialog.setMessage(hmc.name);
                    alertDialog.show();
                    v.refreshDrawableState();
                }
            });


        }

        setIcon(v, R.drawable.icon13);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.SWITCH));
        return v;
    }


    private View SwitchKeyView(View v, HMChannel hmc) {
        String value = DbUtil.getDatapointString(hmc.rowId, "STATE");

        if (value != null) {
            if (value.equals("true")) {
                mViewAdder.addNewValue(v, R.drawable.flat_unlocked, ViewAdder.IconSize.BIG);
            } else {
                mViewAdder.addNewValue(v, R.drawable.flat_locked, ViewAdder.IconSize.BIG);
            }

            if (isDetailed) {
                Double batteryState = DbUtil.getDatapointDouble(hmc.rowId, "BATTERY_STATE");
                if (batteryState != null) {
                    mViewAdder.addNewValue(v, R.drawable.flat_battery,
                            Double.toString(Math.round(batteryState * 100) / 100.) + " V");
                }
            }

            if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
                setOnClickListener(v, keyMaticHandler);
            }
        }

        Integer datapointId = DbUtil.getDatapointId(hmc.rowId, "OPEN");

        setIcon(v, R.drawable.icon14);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, value, HmType.SWITCH, datapointId));
        return v;
    }

    private View LockStateIpView(View v, HMChannel hmc) {
        Integer readValue = DbUtil.getDatapointInt(hmc.rowId, "LOCK_STATE");

        if (readValue != null) {
            switch (readValue) {
                case 0:
                    mViewAdder.addNewValue(v, R.drawable.hvac_unkown, ViewAdder.IconSize.BIG);
                    break;

                case 1:
                    mViewAdder.addNewValue(v, R.drawable.flat_locked, ViewAdder.IconSize.BIG);
                    break;

                case 2:
                    mViewAdder.addNewValue(v, R.drawable.flat_unlocked, ViewAdder.IconSize.BIG);
                    break;
            }

            if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
                setOnClickListener(v, lockIpHandler);
            }
        }

        setIcon(v, R.drawable.icon14);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View LockIpView(View v, HMChannel hmc) {
        Integer readValue = DbUtil.getDatapointInt(hmc.rowId, "LOCK_STATE");
        Integer writeDatapointId = DbUtil.getDatapointId(hmc.rowId, "LOCK_TARGET_LEVEL");

        if (readValue != null && writeDatapointId != null) {
            switch (readValue) {
                case 0:
                    mViewAdder.addNewValue(v, R.drawable.hvac_unkown, ViewAdder.IconSize.BIG);
                    break;

                case 1:
                    mViewAdder.addNewValue(v, R.drawable.flat_locked, ViewAdder.IconSize.BIG);
                    break;

                case 2:
                    mViewAdder.addNewValue(v, R.drawable.flat_unlocked, ViewAdder.IconSize.BIG);
                    break;
            }

            if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
                setOnClickListener(v, lockIpHandler);
            }
        }

        setIcon(v, R.drawable.icon14);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, "", HmType.SWITCH, writeDatapointId));
        return v;
    }

    private View OpenClosedView(View v, HMChannel hmc) {
        String state = DbUtil.getDatapointString(hmc.rowId, "STATE");

        if (state != null) {
            if (state.equals("1")) {
                mViewAdder.addNewValue(v, R.drawable.flat_unlocked, ViewAdder.IconSize.BIG);
            } else {
                mViewAdder.addNewValue(v, R.drawable.flat_locked, ViewAdder.IconSize.BIG);
            }
        }

        setIcon(v, R.drawable.icon13);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View WindowView(View v, HMChannel hmc) {
        String state = DbUtil.getDatapointString(hmc.rowId, "STATE");

        if (state != null) {
            switch (state) {
                case "2":
                    mViewAdder.addNewValue(v, R.drawable.flat_window_open, ViewAdder.IconSize.BIG, ctx.getString(R.string.opened));
                    break;
                case "1":
                    mViewAdder.addNewValue(v, R.drawable.flat_window_open, ViewAdder.IconSize.BIG, ctx.getString(R.string.tilted));
                    break;
                default:
                    mViewAdder.addNewValue(v, R.drawable.flat_locked, ViewAdder.IconSize.BIG);
                    break;
            }
        }

        setIcon(v, R.drawable.icon13);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View WaterView(View v, HMChannel hmc) {
        String state = DbUtil.getDatapointString(hmc.rowId, "STATE");
        switch (state) {
            case "2":
                mViewAdder.addNewValue(v, R.drawable.flat_water_full, ViewAdder.IconSize.BIG);
                break;
            case "1":
                mViewAdder.addNewValue(v, R.drawable.flat_water_half, ViewAdder.IconSize.BIG);
                break;
            default:
                mViewAdder.addNewValue(v, R.drawable.flat_water_empty, ViewAdder.IconSize.BIG);
                break;
        }

        setIcon(v, R.drawable.icon13);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, state, HmType.PASSIV));
        return v;
    }

    private View NotSupportedView(View v, HMChannel hmc) {
        setOnClickListener(v, notSupportedHandler);
        mViewAdder.addNewValue(v, ctx.getResources().getString(R.string.unsupported));
        setIcon(v, R.drawable.icon_new9);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, hmc.type, HmType.PASSIV));

        return v;
    }

    private void RebootCCUView(View v) {
        setOnClickListener(v, rebootCCUHandler);
        mViewAdder.addNewValue(v, ctx.getResources().getString(R.string.reboot_ccu));
    }

    private void UpdateCUXD(View v) {
        mViewAdder.addNewValue(v, ctx.getResources().getString(R.string.update_cuxd));
    }

    private View TemperatureView2(View v, HMChannel hmc) {
        Double temperature = DbUtil.getDatapointDouble(hmc.rowId, "TEMPERATURE");
        if (temperature != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_temp, Double.toString(Math.round(temperature * 10) / 10.) + "°C");
        }

        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View WeatherView(View v, HMChannel hmc) {
        Double temperature = DbUtil.getDatapointDouble(hmc.rowId, "TEMPERATURE");
        if (temperature != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_temp, Double.toString(Math.round(temperature * 10) / 10.) + "°C");
        }

        Double humidity = DbUtil.getDatapointDouble(hmc.rowId, "HUMIDITY");
        if (humidity != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_humidity, Double.toString(humidity) + "%");
        }

        Double dewPoint = DbUtil.getDatapointDouble(hmc.rowId, "DEW_POINT");
        if (dewPoint != null) {
            mViewAdder.addNewValue(v, null, "DEW_POINT", Double.toString(dewPoint));
        }

        Double absHumidity = DbUtil.getDatapointDouble(hmc.rowId, "ABS_HUMIDITY");
        if (absHumidity != null) {
            mViewAdder.addNewValue(v, null, "ABS_HUMIDITY", Double.toString(absHumidity));
        }

        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View WeatherCuxdView(View v, HMChannel hmc) {
        Double temperature = DbUtil.getDatapointDouble(hmc.rowId, "TEMPERATURE");
        if (temperature != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_temp, Double.toString(Math.round(temperature * 10) / 10.) + "°C");
        }

        Double humidity = DbUtil.getDatapointDouble(hmc.rowId, "HUMIDITY");
        if (humidity != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_humidity, Double.toString(humidity) + "%");
        }

        Double dewPoint = DbUtil.getDatapointDouble(hmc.rowId, "DEW_POINT");
        if (dewPoint != null) {
            mViewAdder.addNewValue(v, null, "DEW_POINT", Double.toString(dewPoint));
        }

        Double absHumidity = DbUtil.getDatapointDouble(hmc.rowId, "ABS_HUMIDITY");
        if (absHumidity != null) {
            mViewAdder.addNewValue(v, null, "ABS_HUMIDITY", Double.toString(absHumidity));
        }

        Double tempMin24H = DbUtil.getDatapointDouble(hmc.rowId, "TEMP_MIN_24H");
        if (tempMin24H != null) {
            mViewAdder.addNewValue(v, null, "TEMP_MIN_24H", Double.toString(tempMin24H));
        }

        Double tempMax24H = DbUtil.getDatapointDouble(hmc.rowId, "TEMP_MAX_24H");
        if (tempMax24H != null) {
            mViewAdder.addNewValue(v, null, "TEMP_MAX_24H", Double.toString(tempMax24H));
        }

        Double humMin24H = DbUtil.getDatapointDouble(hmc.rowId, "HUM_MIN_24H");
        if (humMin24H != null) {
            mViewAdder.addNewValue(v, null, "HUM_MIN_24H", Double.toString(humMin24H));
        }

        Double humMax24H = DbUtil.getDatapointDouble(hmc.rowId, "HUM_MAX_24H");
        if (humMax24H != null) {
            mViewAdder.addNewValue(v, null, "HUM_MAX_24H", Double.toString(humMax24H));
        }

        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View VoltageView(View v, HMChannel hmc) {
        Double batLevel = DbUtil.getDatapointDouble(hmc.rowId, "BAT_VOLTAGE");

        if (batLevel != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_battery, Double.toString(Math.round(batLevel * 100) / 100) + "V");
        }

        setIcon(v, R.drawable.icon15);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View CCUView(View v, HMChannel hmc) {
        Double batLevel = DbUtil.getDatapointDouble(hmc.rowId, "BAT_LEVEL");

        if (batLevel != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_battery, Double.toString(Math.round(batLevel)) + "%");
        }

        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View MotionView(View v, HMChannel hmc) {
        String brightness = DbUtil.getDatapointString(hmc.rowId, "BRIGHTNESS");

        if (brightness != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_brightness, brightness);
        }

        Boolean motion = DbUtil.getDatapointBoolean(hmc.rowId, "MOTION");
        if (motion != null && motion) {
            mViewAdder.addNewValue(v, R.drawable.flat_alarm, ViewAdder.IconSize.SMALL);
        }

        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View MotionIPView(View v, HMChannel hmc) {
        Double illumination = DbUtil.getDatapointDouble(hmc.rowId, "ILLUMINATION");
        if (illumination != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_brightness,
                    Double.toString(Math.round(illumination)) + " Lux");
        }

        Boolean motion = DbUtil.getDatapointBoolean(hmc.rowId, "MOTION");
        if (motion != null && motion) {
            mViewAdder.addNewValue(v, R.drawable.flat_alarm, ViewAdder.IconSize.SMALL);
        }

        Boolean lowBattery = DbUtil.getDatapointBoolean(hmc.rowId, "LOW_BAT");

        Double battery = DbUtil.getDatapointDouble(hmc.rowId, "OPERATING_VOLTAGE");
        if (battery != null) {
            View setPointView = mViewAdder.addNewValue(v, R.drawable.flat_battery, Math.round(battery * 10) / 10. + " V");

            if (lowBattery != null && lowBattery) {
                ((TextView) setPointView.findViewById(R.id.value)).setTextColor(ctx.getResources().getColor(R.color.orange));
            }
        }

        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View MotionIPOnlyView(View v, HMChannel hmc) {
        Boolean motion = DbUtil.getDatapointBoolean(hmc.rowId, "MOTION");
        if (motion != null && motion) {
            mViewAdder.addNewValue(v, R.drawable.flat_alarm, ViewAdder.IconSize.BIG);
        }

        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View PresenceIPView(View v, HMChannel hmc) {
        Double illumination = DbUtil.getDatapointDouble(hmc.rowId, "ILLUMINATION");
        if (illumination != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_brightness,
                    Double.toString(Math.round(illumination * 10) / 10.));
        }

        Boolean motion = DbUtil.getDatapointBoolean(hmc.rowId, "PRESENCE_DETECTION_STATE");
        if (motion != null && motion) {
            mViewAdder.addNewValue(v, R.drawable.flat_alarm, ViewAdder.IconSize.SMALL);
        }

        Boolean active = DbUtil.getDatapointBoolean(hmc.rowId, "PRESENCE_DETECTION_ACTIVE");
        if (active != null) {
            if (active) {
                mViewAdder.addNewValue(v, R.drawable.flat_sound_on, ViewAdder.IconSize.SMALL);
            } else {
                mViewAdder.addNewValue(v, R.drawable.flat_sound_off, ViewAdder.IconSize.SMALL);
            }
        }

        setIcon(v, R.drawable.icon_new30);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View AlarmStateView(View v, HMChannel hmc) {
        Boolean alarmstate = DbUtil.getDatapointBoolean(hmc.rowId, "ALARMSTATE");

        if (alarmstate != null) {
            if (alarmstate) {
                mViewAdder.addNewValue(v, R.drawable.flat_alarm, ViewAdder.IconSize.BIG);
            } else {
                mViewAdder.addNewValue(v, R.drawable.flat_locked, ViewAdder.IconSize.BIG);
            }
        }

        setIcon(v, R.drawable.icon_gahle1);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View AlarmSireneStateView(View v, HMChannel hmc) {
        Boolean accoustic = DbUtil.getDatapointBoolean(hmc.rowId, "ACOUSTIC_ALARM_ACTIVE");
        Boolean optical = DbUtil.getDatapointBoolean(hmc.rowId, "OPTICAL_ALARM_ACTIVE");

        if (accoustic != null && optical != null) {
            if (accoustic || optical) {
                mViewAdder.addNewValue(v, R.drawable.flat_alarm, ViewAdder.IconSize.SMALL);
            }
        }

        setIcon(v, R.drawable.icon_gahle1);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View DirectionIPView(View v, HMChannel hmc) {
        Boolean current = DbUtil.getDatapointBoolean(hmc.rowId, "CURRENT_PASSAGE_DIRECTION");
        if (current != null) {
            if (current) {
                mViewAdder.addNewValue(v, "→");
            } else {
                mViewAdder.addNewValue(v, "←");
            }
        }

        Boolean previous = DbUtil.getDatapointBoolean(hmc.rowId, "LAST_PASSAGE_DIRECTION");
        if (previous != null) {
            if (previous) {
                mViewAdder.addNewValue(v, "⧖ →");
            } else {
                mViewAdder.addNewValue(v, "⧖ ←");
            }
        }

        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }


    private View ColorView(View v, final HMChannel hmc) {
        if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
            setOnClickListener(v, variableHandler);
        }

        Integer color = DbUtil.getDatapointInt(hmc.rowId, "COLOR");
        if (color != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_settings, color.toString());
        }

        setIcon(v, R.drawable.icon18);

        v.setTag(new HMControllableVar(hmc.rowId, hmc.name, "", HmType.COLOR, 0, 200, false, "", color == null ? 0 : color.doubleValue(), DbUtil.getDatapointId(hmc.rowId, "COLOR")));
        return v;
    }

    private View ColorProgramView(View v, HMChannel hmc) {
        if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
            setOnClickListener(v, colorProgramListHandler);
        }

        Integer programId = DbUtil.getDatapointInt(hmc.rowId, "PROGRAM");
        if (programId != null) {
            mViewAdder.addNewValue(v, ctx.getResources().getStringArray(R.array.color_programs)[programId]);
        }

        setIcon(v, R.drawable.icon_new15);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.COLOR_PROGAM));
        return v;
    }

    private View LightifyPlugView(View v, HMChannel hmc) {
        Integer datapointId = DbUtil.getDatapointId(hmc.rowId, "LEVEL");
        if (datapointId != null) {
            if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
                setOnClickListener(v, LightifyPlugHandler);
            }
            v.setTag(new HMControllable(datapointId, hmc.name, null, HmType.LIGHTIFY_PLUG));
        }

        return v;
    }

    private View VariableView(View v, final HMChannel hmc, int min, int max, String unit, final HmType hmType,
                              Integer lowerRes, Integer upperRes) {
        if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
            setOnClickListener(v, variableHandler);
        }

        double value = 0;

        Double level = DbUtil.getDatapointDouble(hmc.rowId, "LEVEL");
        if (level != null) {
            value = Math.round(level * 100);
            final boolean isZero = value == min;
            String levelString;
            if (hmType == HmType.BLIND || hmType == HmType.BLIND_WITH_LAMELLA || hmType == HmType.BLIND_WITH_LAMELLA_IP) {
                levelString = (int) value + unit;
            } else {
                levelString = value + unit;
            }

            Integer iconRes = isZero ? lowerRes : upperRes;

            if (hmType == HmType.DIMMER || hmType.equals(HmType.DIMMER_IP) || hmType.equals(HmType.DIMMER_IP_COLOR) || hmType == HmType.BLIND || hmType == HmType.BLIND_WITH_LAMELLA_IP || hmType == HmType.BLIND_WITH_LAMELLA || hmType == HmType.MP3_IP_SOUND || hmType == HmType.MP3_IP_SOUND_FILE || hmType == HmType.DIMMER_MP3_COLOR) {
                CheckedTextView cb = mViewAdder.addNewCheckedView(v).findViewById(R.id.check);
                cb.setChecked(!isZero);
                cb.setText(levelString);
                cb.setCheckMarkDrawable(Util.getColoredIcon(ctx, isDarkTheme, iconRes));

                setOnClickListener(cb, new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isZero) {
                            if (hmType == HmType.DIMMER) {
                                Integer datapointId = DbUtil.getDatapointId(hmc.rowId, "OLD_LEVEL");
                                if (datapointId != null) {
                                    ControlHelper.sendOrder(ctx, datapointId, "1", toastHandler,
                                            false, true);
                                }
                            } else if (hmType == HmType.MP3_IP_SOUND || hmType == HmType.MP3_IP_SOUND_FILE) {
                                Integer datapointId = DbUtil.getDatapointId(hmc.rowId, "LEVEL");
                                if (datapointId != null) {
                                    ControlHelper.sendOrder(ctx, datapointId, "0.3", toastHandler, false, true);
                                }
                            } else if (hmType == HmType.DIMMER_MP3_COLOR) {
                                Integer datapointId = DbUtil.getDatapointId(hmc.rowId, "LEVEL");
                                if (datapointId != null) {
                                    ControlHelper.sendOrder(ctx, datapointId, "1", toastHandler, false, true);
                                }
                            } else {
                                ControlHelper.sendOrder(ctx, hmc.rowId, "1", toastHandler,
                                        false, true);
                            }
                        } else {
                            if (hmType == HmType.MP3_IP_SOUND || hmType == HmType.MP3_IP_SOUND_FILE || hmType == HmType.DIMMER_MP3_COLOR) {
                                Integer datapointId = DbUtil.getDatapointId(hmc.rowId, "LEVEL");
                                if (datapointId != null) {
                                    ControlHelper.sendOrder(ctx, datapointId, "0", toastHandler, false, true);
                                }
                            } else {
                                ControlHelper.sendOrder(ctx, hmc.rowId, "0", toastHandler, hmType == HmType.SYSVARIABLE,
                                        false);
                            }
                        }
                    }
                });

                cb.setTag(new HMCheckable(hmc.rowId, hmc.name, levelString, hmType, null, null, lowerRes, upperRes, null));
            } else if (hmType == HmType.LIGHTIFY_DIMMER || hmType == HmType.LIGHTIFY_DIMMER_RGBW) {
                // CCU does not provide value
            } else {
                View childView = mViewAdder.addNewValue(v, iconRes, levelString);
                if (value > min) {
                    ((TextView) childView.findViewById(R.id.value)).setTextColor(ctx.getResources().getColor(R.color.orange));
                }
            }
        }

        if (hmType == HmType.BLIND_WITH_LAMELLA_IP) {
            Double lamella = DbUtil.getDatapointDouble(hmc.rowId, "LEVEL_2");
            if (lamella != null) {
                long lamellaValue = Math.round(lamella * 100);
                mViewAdder.addNewValue(v, R.drawable.ic_action_menu_sort_off,
                        Math.round(lamellaValue) + "%");
            }
        }

        Integer datapointId = DbUtil.getDatapointId(hmc.rowId, "LEVEL");

        setIcon(v, R.drawable.icon18);
        v.setTag(new HMControllableVar(hmc.rowId, hmc.name, unit, hmType, min, max, true, unit, value, datapointId));
        return v;
    }

    private View TemperatureControlView(View v, HMChannel hmc, int min, int max, String unit) {
        if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
            setOnClickListener(v, variableHandler);
        }

        Double setpoint = DbUtil.getDatapointDouble(hmc.rowId, "SETPOINT");
        if (setpoint != null) {
            View childView = mViewAdder.addNewValue(v, R.drawable.flat_temp,
                    Double.toString(Math.round(setpoint * 10) / 10.) + unit);

            if (setpoint > min) {
                ((TextView) childView.findViewById(R.id.value)).setTextColor(ctx.getResources().getColor(R.color.orange));
            }
        }

        Integer datapointId = DbUtil.getDatapointId(hmc.rowId, "SETPOINT");

        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllableVar(hmc.rowId, hmc.name, unit, HmType.VARIABLETEMP, min, max, false, unit,
                setpoint == null ? min : setpoint, datapointId));
        return v;
    }

    private View TemperatureControlView2(View v, HMChannel hmc, double min, double max, String unit) {
        Double actualTemperature = DbUtil.getDatapointDouble(hmc.rowId, "ACTUAL_TEMPERATURE");
        if (actualTemperature != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_temp,
                    Double.toString(Math.round(actualTemperature * 10) / 10.) + unit);
        }

        Double setTemperature = DbUtil.getDatapointDouble(hmc.rowId, "SET_TEMPERATURE");
        if (setTemperature != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_counter,
                    Double.toString(Math.round(setTemperature * 10) / 10.) + unit);
        }

        String valveState = DbUtil.getDatapointString(hmc.rowId, "VALVE_STATE");
        if (valveState != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_valve, valveState + "%");
        }

        if (isDetailed) {
            Double batteryState = DbUtil.getDatapointDouble(hmc.rowId, "BATTERY_STATE");
            if (batteryState != null) {
                mViewAdder.addNewValue(v, R.drawable.flat_battery,
                        Double.toString(Math.round(batteryState * 100) / 100.) + " V");
            }
        }

        Integer controlMode = DbUtil.getDatapointInt(hmc.rowId, "CONTROL_MODE");
        if (controlMode != null) {
            Integer mode = null;
            switch (controlMode) {
                case 0:
                    mode = R.string.mode_auto;
                    break;
                case 1:
                    mode = R.string.mode_manu;
                    break;
                case 2:
                    mode = R.string.mode_lowering;
                    break;
                case 3:
                    mode = R.string.mode_boost;
                    break;
                case 4:
                    mode = R.string.mode_comfort;
                    break;
                default:
                    break;
            }

            if (mode != null) {
                mViewAdder.addNewValue(v, R.drawable.flat_settings, ctx.getResources().getString(mode));
            }
        }

        Integer datapointId = DbUtil.getDatapointId(hmc.rowId, "SET_TEMPERATURE");
        if (datapointId != null) {
            if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
                setOnClickListener(v, variableHandler);
            }
        }

        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllableVar(hmc.rowId, hmc.name, unit, HmType.VARIABLECLIMATE, min, max, false, unit,
                setTemperature == null ? 0 : setTemperature, datapointId));
        return v;
    }

    private View AlarmView(View v, HMChannel hmc) {
        if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
            setOnClickListener(v, alarmStateHandler);
        }

        Integer armstate = DbUtil.getDatapointInt(hmc.rowId, "ARMSTATE");
        if (armstate != null) {
            Integer stateString = null;
            switch (armstate) {
                case 0:
                    stateString = R.string.alarm_off;
                    break;
                case 1:
                    stateString = R.string.alarm_internal;
                    break;
                case 2:
                    stateString = R.string.alarm_external;
                    break;
                case 3:
                    stateString = R.string.alarm_blocked;
                    break;
                default:
                    break;
            }

            if (stateString != null) {
                mViewAdder.addNewValue(v, R.drawable.flat_settings, ctx.getResources().getString(stateString));
            }
        }

        setIcon(v, R.drawable.icon_gahle1);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.ALARM));
        return v;
    }

    private View TemperatureControlView3(View v, HMChannel hmc, double min, double max, String unit) {

        Double actualTemperature = DbUtil.getDatapointDouble(hmc.rowId, "ACTUAL_TEMPERATURE");
        if (actualTemperature != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_temp,
                    Double.toString(Math.round(actualTemperature * 10) / 10.) + unit);
        }

        Double setTemperature = DbUtil.getDatapointDouble(hmc.rowId, "SET_TEMPERATURE");
        if (setTemperature != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_counter,
                    Double.toString(Math.round(setTemperature * 10) / 10.) + unit);
        }

        Double actualHumidity = DbUtil.getDatapointDouble(hmc.rowId, "ACTUAL_HUMIDITY");
        if (actualHumidity != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_humidity, Double.toString(Math.round(actualHumidity)) + "%");
        }

        if (isDetailed) {
            Double batteryState = DbUtil.getDatapointDouble(hmc.rowId, "BATTERY_STATE");
            if (batteryState != null) {
                mViewAdder.addNewValue(v, R.drawable.flat_battery,
                        Double.toString(Math.round(batteryState * 100) / 100.) + " V");
            }
        }

        Integer controlMode = DbUtil.getDatapointInt(hmc.rowId, "CONTROL_MODE");
        if (controlMode != null) {
            Integer mode = null;
            switch (controlMode) {
                case 0:
                    mode = R.string.mode_auto;
                    break;
                case 1:
                    mode = R.string.mode_manu;
                    break;
                case 2:
                    mode = R.string.mode_lowering;
                    break;
                case 3:
                    mode = R.string.mode_boost;
                    break;
                default:
                    break;
            }

            if (mode != null) {
                mViewAdder.addNewValue(v, R.drawable.flat_settings, ctx.getResources().getString(mode));
            }
        }

        Integer datapointId = DbUtil.getDatapointId(hmc.rowId, "SET_TEMPERATURE");
        if (datapointId != null) {
            if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
                setOnClickListener(v, variableHandler);
            }
        }

        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllableVar(hmc.rowId, hmc.name, unit, HmType.VARIABLECLIMATE, min, max, false, unit,
                setTemperature == null ? 0 : setTemperature, datapointId));
        return v;
    }

    private View ClimateControlIpView(View v, HMChannel hmc, int min, int max, String unit) {
        Double actualTemperature = DbUtil.getDatapointDouble(hmc.rowId, "ACTUAL_TEMPERATURE");
        if (actualTemperature != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_temp,
                    Double.toString(Math.round(actualTemperature * 10) / 10.) + unit);
        }

        Double actualHumidity = DbUtil.getDatapointDouble(hmc.rowId, "HUMIDITY");
        if (actualHumidity != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_humidity, Double.toString(Math.round(actualHumidity)) + "%");
        }

        Double setTemperature = DbUtil.getDatapointDouble(hmc.rowId, "SET_POINT_TEMPERATURE");
        if (setTemperature != null) {
            View setPointView = mViewAdder.addNewValue(v, R.drawable.flat_counter,
                    Double.toString(Math.round(setTemperature * 10) / 10.) + unit);

            if (setTemperature > min) {
                ((TextView) setPointView.findViewById(R.id.value)).setTextColor(ctx.getResources().getColor(R.color.orange));
            }
        }

        Integer datapointId = DbUtil.getDatapointId(hmc.rowId, "SET_POINT_TEMPERATURE");
        if (datapointId != null) {
            if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
                setOnClickListener(v, variableHandler);
            }
        }

        Double level = DbUtil.getDatapointDouble(hmc.rowId, "LEVEL");
        if (level != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_valve,
                    Double.toString(Math.round(level * 100)) + "%");
        }


        Integer controlMode = DbUtil.getDatapointInt(hmc.rowId, "SET_POINT_MODE");
        if (controlMode != null) {
            Integer mode = null;
            switch (controlMode) {
                case 0:
                    mode = R.string.mode_auto;
                    break;
                case 1:
                    mode = R.string.mode_manu;
                    break;
                case 2:
                    mode = R.string.mode_lowering;
                    break;
                default:
                    break;
            }

            if (mode != null) {
                mViewAdder.addNewValue(v, R.drawable.flat_settings, ctx.getResources().getString(mode));
            }
        }

        Integer activeProfile = DbUtil.getDatapointInt(hmc.rowId, "ACTIVE_PROFILE");
        if (activeProfile != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_list, activeProfile.toString());
        }

        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllableVar(hmc.rowId, hmc.name, unit, HmType.VARIABLECLIMATE_IP, min, max, false, unit,
                setTemperature == null ? 0 : setTemperature, datapointId));
        return v;
    }

    private View ThermostatIpView(View v, HMChannel hmc, int min, int max, String unit) {
        Double actualTemperature = DbUtil.getDatapointDouble(hmc.rowId, "ACTUAL_TEMPERATURE");
        if (actualTemperature != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_temp,
                    Double.toString(Math.round(actualTemperature * 10) / 10.) + unit);
        }

        Double setTemperature = DbUtil.getDatapointDouble(hmc.rowId, "SET_POINT_TEMPERATURE");
        if (setTemperature != null) {
            View setPointView = mViewAdder.addNewValue(v, R.drawable.flat_counter,
                    Double.toString(Math.round(setTemperature * 10) / 10.) + unit);

            if (setTemperature > min) {
                ((TextView) setPointView.findViewById(R.id.value)).setTextColor(ctx.getResources().getColor(R.color.orange));
            }
        }

        Double level = DbUtil.getDatapointDouble(hmc.rowId, "LEVEL");
        if (level != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_valve,
                    Double.toString(Math.round(level * 100)) + "%");
        }

        Integer datapointId = DbUtil.getDatapointId(hmc.rowId, "SET_POINT_TEMPERATURE");
        if (datapointId != null) {
            if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
                setOnClickListener(v, variableHandler);
            }
        }

        Integer controlMode = DbUtil.getDatapointInt(hmc.rowId, "SET_POINT_MODE");
        if (controlMode != null) {
            Integer mode = null;
            switch (controlMode) {
                case 0:
                    mode = R.string.mode_auto;
                    break;
                case 1:
                    mode = R.string.mode_manu;
                    break;
                case 2:
                    mode = R.string.mode_lowering;
                    break;
                default:
                    break;
            }

            if (mode != null) {
                mViewAdder.addNewValue(v, R.drawable.flat_settings, ctx.getResources().getString(mode));
            }
        }

        Boolean boostMode = DbUtil.getDatapointBoolean(hmc.rowId, "BOOST_MODE");
        if (boostMode != null) {
            if (boostMode) {
                String boostStatus = ctx.getResources().getString(R.string.mode_boost);

                View setPointView = mViewAdder.addNewValue(v, R.drawable.flat_power, boostStatus);

                ((TextView) setPointView.findViewById(R.id.value)).setTextColor(ctx.getResources().getColor(R.color.orange));
            }
        }

        Integer activeProfile = DbUtil.getDatapointInt(hmc.rowId, "ACTIVE_PROFILE");
        if (activeProfile != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_list, activeProfile.toString());
        }

        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllableVar(hmc.rowId, hmc.name, unit, HmType.VARIABLETHERMOSTATE_IP, min, max, false, unit,
                setTemperature == null ? 0 : setTemperature, datapointId));
        return v;
    }


    private View ActivateView(View v, HMChannel hmc) {
        if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
            setOnClickListener(v, activateDialogHandler);
        }

        setIcon(v, R.drawable.icon_new30);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.ACTIVATE));
        return v;
    }

    private View selectTasterOrSwitchView(View v, HMChannel hmc) {
        if (mDatapointHelper.fetchItemsByChannelAndType(hmc.rowId, "STATE").getCount() > 0) {
            return SwitchView(v, hmc);
        } else {
            return TasterView(v, hmc);
        }
    }

    private View SwitchVarView(View v, HMVariable hmc) {
        CheckedTextView cb = (CheckedTextView) mViewAdder.addNewCheckedView(v).findViewById(R.id.check);

        if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
            setOnClickListener(v, CheckedTextViewDialogTextSwitchHandler);

            if (PreferenceHelper.isSysVarQuickAccessEnabled(ctx)) {
                setOnClickListener(cb, CheckedTextViewHandler);
            }
        }

        v.setTag(new HMCheckable(hmc.rowId, hmc.name, hmc.value, HmType.SYSVARIABLE, hmc.getValueName0(),
                hmc.getValueName1(), null, null, null));
        cb.setTag(new HMCheckable(hmc.rowId, hmc.name, hmc.value, HmType.SYSVARIABLE));

        if (hmc.value.equals("true")) {
            cb.setChecked(true);
            cb.setText(hmc.getValueName1());
        } else if (hmc.value.equals("false")) {
            cb.setChecked(false);
            cb.setText(hmc.getValueName0());
        } else {
            cb.setChecked(false);
        }

        setIcon(v, R.drawable.icon_new30);
        return v;
    }

    private View RainingView(View v, HMChannel hmc) {
        String state = DbUtil.getDatapointString(hmc.rowId, "STATE");
        if (state != null) {
            if (state.equals("1")) {
                mViewAdder.addNewValue(v, R.drawable.flat_rain, ViewAdder.IconSize.BIG);
            } else if (state.equals("0")) {
                mViewAdder.addNewValue(v, R.drawable.flat_sunny, ViewAdder.IconSize.BIG);
            }
        }

        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View RainingIPView(View v, HMChannel hmc) {
        String raining = DbUtil.getDatapointString(hmc.rowId, "RAINING");
        if (raining != null) {
            if (raining.equals("true")) {
                mViewAdder.addNewValue(v, R.drawable.flat_rain, ViewAdder.IconSize.BIG);
            } else if (raining.equals("false")) {
                mViewAdder.addNewValue(v, R.drawable.flat_sunny, ViewAdder.IconSize.BIG);
            }
        }

        Double temperature = DbUtil.getDatapointDouble(hmc.rowId, "ACTUAL_TEMPERATURE");
        if (temperature != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_temp, Double.toString(Math.round(temperature * 10) / 10.) + "°C");
        }


        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View TemperatureIPView(View v, HMChannel hmc) {
        Double temperature = DbUtil.getDatapointDouble(hmc.rowId, "ACTUAL_TEMPERATURE");
        if (temperature != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_temp, Double.toString(Math.round(temperature * 10) / 10.) + "°C");
        }

        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }


    private View BlindHeightIpView(View v, HMChannel hmc) {
        Double level = DbUtil.getDatapointDouble(hmc.rowId, "LEVEL");
        if (level != null) {
            mViewAdder.addNewValue(v, R.drawable.ic_action_menu_sort,
                    Double.toString(Math.round(level * 100)) + "%");
        }

        setIcon(v, R.drawable.icon_gahle25);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View SensorView(View v, HMChannel hmc) {
        String value = DbUtil.getDatapointString(hmc.rowId, "SENSOR");
        if (value.equals("true")) {
            mViewAdder.addNewValue(v, R.drawable.flat_unlocked, ViewAdder.IconSize.BIG);
        } else {
            mViewAdder.addNewValue(v, R.drawable.flat_locked, ViewAdder.IconSize.BIG);
        }

        setIcon(v, R.drawable.icon_new30);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, value, HmType.PASSIV));
        return v;
    }

    private View SwitchView(View v, HMChannel hmc) {
        return SwitchView(v, hmc, null, null, null, null);
    }

    /**
     * @param trueString  defaults to 'on'
     * @param falseString defaults to 'off'
     */
    private View SwitchView(View v, HMChannel hmc, String trueString, String falseString, Integer trueIcon,
                            Integer falseIcon) {
        if (trueString == null) {
            trueString = ctx.getResources().getString(R.string.on);
        }

        if (falseString == null) {
            falseString = ctx.getResources().getString(R.string.off);
        }

        CheckedTextView cb = mViewAdder.addNewCheckedView(v).findViewById(R.id.check);

        HMCheckable hms;
        String value = hmc.value;
        if (hmc.type.equalsIgnoreCase("HM-Variable")) {
            hms = new HMCheckable(hmc.rowId, hmc.name, hmc.value, HmType.SYSVARIABLE, falseString, trueString,
                    falseIcon, trueIcon, null);
        } else {
            value = DbUtil.getDatapointString(hmc.rowId, "STATE");
            Integer datapointId = DbUtil.getDatapointId(hmc.rowId, "STATE");
            if (value == null) {
                value = DbUtil.getDatapointString(hmc.rowId, "PRESS");
                if (value == null) {
                    value = "";
                }
            }
            hms = new HMCheckable(hmc.rowId, hmc.name, value, HmType.SWITCH, falseString, trueString, falseIcon,
                    trueIcon, datapointId);
        }

        if (value.equals("true")) {
            cb.setChecked(true);
            if (trueIcon != null) {
                cb.setCheckMarkDrawable(Util.getColoredIcon(ctx, isDarkTheme, trueIcon));
            }
        } else if (value.equals("false")) {
            cb.setChecked(false);
            if (falseIcon != null) {
                cb.setCheckMarkDrawable(Util.getColoredIcon(ctx, isDarkTheme, falseIcon));
            }
        } else {
            cb.setVisibility(View.GONE);
        }

        if (!PreferenceHelper.isDisableNotOperate(ctx) || hmc.isOperate()) {
            setOnClickListener(v, CheckedTextViewDialogTextSwitchHandler);
            setOnClickListener(cb, CheckedTextViewHandler);
        }

        cb.setTag(hms);
        v.setTag(hms);
        setIcon(v, R.drawable.icon_new30);
        return v;
    }

    private View PowerFailureView(View v, HMChannel hmc) {
        String state = DbUtil.getDatapointString(hmc.rowId, "POWER_MAINS_FAILURE");

        if (state != null) {
            switch (state) {
                case "true":
                    mViewAdder.addNewValue(v, R.drawable.btn_check_on_holo_dark_hm, ViewAdder.IconSize.BIG);
                    break;
                case "false": {
                    mViewAdder.addNewValue(v, R.drawable.btn_check_off_holo_dark_hm, ViewAdder.IconSize.BIG);
                    break;
                }
                default: {
                    //don't show anything
                }
            }
        }

        setIcon(v, R.drawable.icon13);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View StateView(View v, HMChannel hmc, int trueIcon, int falseIcon) {
        String state = DbUtil.getDatapointString(hmc.rowId, "STATE");

        if (state != null) {
            switch (state) {
                case "true":
                    mViewAdder.addNewValue(v, trueIcon, ViewAdder.IconSize.BIG);
                    break;
                case "false": {
                    mViewAdder.addNewValue(v, falseIcon, ViewAdder.IconSize.BIG);
                    break;
                }
                default: {
                    //don't show anything
                }
            }
        }

        setIcon(v, R.drawable.icon13);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View FloorHeatingLevel(View v, HMChannel hmc) {
        Double level = DbUtil.getDatapointDouble(hmc.rowId, "LEVEL");
        if (level != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_valve,
                    Double.toString(Math.round(level * 100)) + "%");
        }
        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View Int1000ViewVent(View v, HMChannel hmc) {
        String value = DbUtil.getDatapointString(hmc.rowId, "VALUE");
        if (value != null) {
            mViewAdder.addNewValue(v, value);
        }
        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    private View Int99ViewVent(View v, HMChannel hmc) {
        String value = DbUtil.getDatapointString(hmc.rowId, "VALVE_STATE");
        if (value != null) {
            mViewAdder.addNewValue(v, R.drawable.flat_valve, value + "%");
        }
        setIcon(v, R.drawable.icon10);
        v.setTag(new HMControllable(hmc.rowId, hmc.name, HmType.PASSIV));
        return v;
    }

    public OnClickListener keyMaticHandler = new OnClickListener() {

        public void onClick(final View v) {
            final int rowId = v.getId();
            final HMControllable hmc = (HMControllable) v.getTag();
            String name = hmc.name;
            final int openChannel = hmc.datapointId;

            View dialogView = inflater.inflate(R.layout.dialog_three_buttons, null);
            final AlertDialog alertDialog = new AlertDialog.Builder(ctx).setView(dialogView).create();

            Button buttonLeft = (Button) dialogView.findViewById(R.id.button_set);
            buttonLeft.setText(R.string.lock);
            buttonLeft.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    ControlHelper.sendOrder(ctx, rowId, "false", toastHandler, hmc.type == HmType.SYSVARIABLE, false);
                    Util.addRefreshNotify(v);
                    alertDialog.dismiss();
                }
            });

            Button buttonMiddle = (Button) dialogView.findViewById(R.id.button_middle);
            buttonMiddle.setText(R.string.unlock);
            buttonMiddle.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    ControlHelper.sendOrder(ctx, rowId, "true", toastHandler, hmc.type == HmType.SYSVARIABLE, false);
                    Util.addRefreshNotify(v);
                    alertDialog.dismiss();
                }
            });

            Button buttonRight = (Button) dialogView.findViewById(R.id.button_back);
            buttonRight.setText(R.string.open);
            buttonRight.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.setOnDismissListener(null);
                    alertDialog.dismiss();

                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setMessage(ctx.getResources().getString(R.string.yousure))
                            .setTitle(hmc.name)
                            .setCancelable(false)
                            .setPositiveButton(ctx.getString(R.string.yes), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ControlHelper.sendOrder(ctx, openChannel, "0", toastHandler, hmc.type == HmType.SYSVARIABLE, true);
                                    Util.addRefreshNotify(v);


                                }
                            })
                            .setNegativeButton(ctx.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog innerAlertDialog = builder.create();
                    innerAlertDialog.setOnDismissListener(activityFinishDimissListener);
                    innerAlertDialog.show();
                }
            });

            alertDialog.setOnCancelListener(activityFinishCancelListener);
            alertDialog.setOnDismissListener(activityFinishDimissListener);

            alertDialog.setMessage(name);
            alertDialog.show();
            v.refreshDrawableState();
        }
    };

    public OnClickListener lockIpHandler = new OnClickListener() {

        public void onClick(final View v) {
            final HMControllable hmc = (HMControllable) v.getTag();
            String name = hmc.name;
            final int writeDatapoint = hmc.datapointId;

            View dialogView = inflater.inflate(R.layout.dialog_three_buttons, null);
            final AlertDialog alertDialog = new AlertDialog.Builder(ctx).setView(dialogView).create();

            Button buttonLeft = (Button) dialogView.findViewById(R.id.button_set);
            buttonLeft.setText(R.string.lock);
            buttonLeft.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    ControlHelper.sendOrder(ctx, new HMCommand(writeDatapoint, HMCommand.TYPE_DATAPOINT, "0", new Date()), toastHandler);
                    Util.addRefreshNotify(v);
                    alertDialog.dismiss();
                }
            });

            Button buttonMiddle = (Button) dialogView.findViewById(R.id.button_middle);
            buttonMiddle.setText(R.string.unlock);
            buttonMiddle.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    ControlHelper.sendOrder(ctx, new HMCommand(writeDatapoint, HMCommand.TYPE_DATAPOINT, "1", new Date()), toastHandler);
                    Util.addRefreshNotify(v);
                    alertDialog.dismiss();
                }
            });

            Button buttonRight = (Button) dialogView.findViewById(R.id.button_back);
            buttonRight.setText(R.string.open);
            buttonRight.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.setOnDismissListener(null);
                    alertDialog.dismiss();

                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setMessage(ctx.getResources().getString(R.string.yousure))
                            .setTitle(hmc.name)
                            .setCancelable(false)
                            .setPositiveButton(ctx.getString(R.string.yes), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ControlHelper.sendOrder(ctx, new HMCommand(writeDatapoint, HMCommand.TYPE_DATAPOINT, "2", new Date()), toastHandler);
                                    Util.addRefreshNotify(v);
                                }
                            })
                            .setNegativeButton(ctx.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog innerAlertDialog = builder.create();
                    innerAlertDialog.setOnDismissListener(activityFinishDimissListener);
                    innerAlertDialog.show();
                }
            });

            alertDialog.setOnCancelListener(activityFinishCancelListener);
            alertDialog.setOnDismissListener(activityFinishDimissListener);

            alertDialog.setMessage(name);
            alertDialog.show();

            v.refreshDrawableState();
        }
    };


    OnDismissListener activityFinishDimissListener = new OnDismissListener() {

        @Override
        public void onDismiss(DialogInterface arg0) {
            finishWidgetActivity();
        }
    };

    OnCancelListener activityFinishCancelListener = new OnCancelListener() {

        @Override
        public void onCancel(DialogInterface arg0) {
            finishWidgetActivity();
        }
    };

    protected void finishWidgetActivity() {
        if (isWidget) {
            activity.finish();
        }
    }

    public OnClickListener activateDialogHandler = new OnClickListener() {

        public void onClick(final View v) {

            final HMControllable hmc = (HMControllable) v.getTag();

            Runnable leftButtonRunnable = new Runnable() {
                @Override
                public void run() {
                    ControlHelper.sendOrder(ctx, hmc.rowId, "true", toastHandler,
                            hmc.type == HmType.SYSVARIABLE, false);
                    Util.addRefreshNotify(v);
                }
            };

            AlertDialog alertDialog = getAlertDialogWithButtons(ctx.getString(R.string.yes), leftButtonRunnable, ctx.getString(R.string.cancel), null);
            alertDialog.setMessage(ctx.getResources().getString(R.string.activate));
            alertDialog.setTitle(hmc.name);
            alertDialog.setOnDismissListener(activityFinishDimissListener);
            alertDialog.setOnCancelListener(activityFinishCancelListener);
            alertDialog.show();
        }
    };

    public OnClickListener CheckedTextViewDialogTextSwitchHandler = new OnClickListener() {

        @SuppressWarnings("deprecation")
        public void onClick(final View v) {

            // String name = getDeviceName(rowId);
            final HMCheckable hmc = (HMCheckable) v.getTag();

            final int rowID = hmc.getDatapointId() == null ? hmc.rowId : hmc.getDatapointId();

            Runnable leftButtonRunnable = new Runnable() {
                @Override
                public void run() {
                    ControlHelper.sendOrder(ctx, rowID, "true", toastHandler, hmc.type == HmType.SYSVARIABLE,
                            hmc.getDatapointId() != null);
                    Util.addRefreshNotify(v);
                }
            };

            Runnable rightButtonRunnable = new Runnable() {
                @Override
                public void run() {
                    ControlHelper.sendOrder(ctx, rowID, "false", toastHandler, hmc.type == HmType.SYSVARIABLE,
                            hmc.getDatapointId() != null);
                    Util.addRefreshNotify(v);
                }
            };

            AlertDialog alertDialog = getAlertDialogWithButtons(hmc.getTrueValueName(), leftButtonRunnable, hmc.getFalseValueName(), rightButtonRunnable);
            alertDialog.setTitle(hmc.name);
            alertDialog.setOnDismissListener(activityFinishDimissListener);
            alertDialog.setOnCancelListener(activityFinishCancelListener);
            alertDialog.show();
        }
    };

    public OnClickListener LightifyPlugHandler = new OnClickListener() {

        @SuppressWarnings("deprecation")
        public void onClick(final View v) {

            final HMControllable hmc = (HMControllable) v.getTag();

            final int rowID = hmc.getRowId();

            Runnable leftButtonRunnable = new Runnable() {
                @Override
                public void run() {
                    ControlHelper.sendOrder(ctx, rowID, "1.0", toastHandler, false,
                            false);
                    Util.addRefreshNotify(v);
                }
            };

            Runnable rightButtonRunnable = new Runnable() {
                @Override
                public void run() {
                    ControlHelper.sendOrder(ctx, rowID, "0.0", toastHandler, false,
                            false);
                    Util.addRefreshNotify(v);
                }
            };

            AlertDialog alertDialog = getAlertDialogWithButtons(ctx.getString(R.string.on), leftButtonRunnable, ctx.getString(R.string.off), rightButtonRunnable);
            alertDialog.setTitle(hmc.name);
            alertDialog.setOnDismissListener(activityFinishDimissListener);
            alertDialog.setOnCancelListener(activityFinishCancelListener);
            alertDialog.show();
        }
    };

    public OnClickListener CheckedTextViewHandler = new OnClickListener() {

        public void onClick(final View v) {
            final HMControllable hmc = (HMControllable) v.getTag();

            CheckedTextView check = (CheckedTextView) v;

            String value = check.isChecked() ? "false" : "true";

            int rowID = hmc.getDatapointId() == null ? hmc.rowId : hmc.getDatapointId();

            ControlHelper.sendOrder(ctx, rowID, value, toastHandler, hmc.type == HmType.SYSVARIABLE, hmc.getDatapointId() != null);
            Util.addRefreshNotify(((View) v.getParent().getParent().getParent().getParent()));
            // check.toggle();
        }
    };

    public OnClickListener tasterDialogHandler = new OnClickListener() {

        @SuppressWarnings("deprecation")
        public void onClick(final View v) {

            final HMControllable hmc = (HMControllable) v.getTag();

            Runnable leftButtonRunnable = new Runnable() {
                @Override
                public void run() {
                    ControlHelper.sendOrder(ctx, hmc.rowId, "1", toastHandler, hmc.type == HmType.SYSVARIABLE, true);
                    Util.addRefreshNotify(v);
                }
            };

            Runnable rightButtonRunnable = new Runnable() {
                @Override
                public void run() {
                    ControlHelper.sendOrder(ctx, hmc.datapointId, "1", toastHandler, hmc.type == HmType.SYSVARIABLE,
                            true);
                    Util.addRefreshNotify(v);
                }
            };

            AlertDialog alertDialog = getAlertDialogWithButtons(ctx.getString(R.string.short_click), leftButtonRunnable, ctx.getString(R.string.long_click), rightButtonRunnable);
            alertDialog.setTitle(hmc.name);
            alertDialog.setOnDismissListener(activityFinishDimissListener);
            alertDialog.setOnCancelListener(activityFinishCancelListener);
            alertDialog.show();
        }
    };

    public OnClickListener doorbellDialogHandler = new OnClickListener() {

        @SuppressWarnings("deprecation")
        public void onClick(final View v) {

            final HMControllable hmc = (HMControllable) v.getTag();

            Runnable leftButtonRunnable = new Runnable() {
                @Override
                public void run() {
                    ControlHelper.sendOrder(ctx, hmc.rowId, "1", toastHandler, hmc.type == HmType.SYSVARIABLE, true);
                    Util.addRefreshNotify(v);
                }
            };

            AlertDialog alertDialog = getAlertDialogWithButtons(ctx.getString(R.string.short_click), leftButtonRunnable, ctx.getString(R.string.cancel), null);
            alertDialog.setTitle(hmc.name);
            alertDialog.setOnDismissListener(activityFinishDimissListener);
            alertDialog.setOnCancelListener(activityFinishCancelListener);
            alertDialog.show();
        }
    };

    public OnClickListener ledDialogHandler = new OnClickListener() {

        public void onClick(final View v) {

            final HMControllable hmc = (HMControllable) v.getTag();

            Runnable leftButtonRunnable = new Runnable() {
                @Override
                public void run() {
                    ControlHelper.sendOrder(ctx, hmc.rowId, "1", toastHandler, hmc.type == HmType.SYSVARIABLE,
                            false);
                    Util.addRefreshNotify(v);
                }
            };

            AlertDialog alertDialog = getAlertDialogWithButtons(ctx.getString(R.string.short_click), leftButtonRunnable, ctx.getString(R.string.cancel), null);
            alertDialog.setTitle(hmc.name);
            alertDialog.setOnDismissListener(activityFinishDimissListener);
            alertDialog.setOnCancelListener(activityFinishCancelListener);
            alertDialog.show();
        }
    };

    public OnClickListener CheckedTextViewRunProgramHandler = new OnClickListener() {

        public void onClick(View v) {
            final HMControllable hmc = (HMControllable) v.getTag();

            ControlHelper.runProgram(ctx, hmc.getRowId(), toastHandler);
            Util.addRefreshNotify(((View) v.getParent().getParent().getParent().getParent()));
        }
    };

    public OnClickListener lightifyInProgressHandler = new OnClickListener() {

        public void onClick(View v) {

            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setMessage(ctx.getString(R.string.lightify_in_progress))
                    .setPositiveButton("Ok", null);

            AlertDialog alert = builder.create();
            alert.setOnCancelListener(activityFinishCancelListener);
            alert.setOnDismissListener(activityFinishDimissListener);

            alert.show();
        }
    };

    public OnClickListener notSupportedHandler = new OnClickListener() {

        public void onClick(View v) {
            final HMControllable hmc = (HMControllable) v.getTag();
            final String name = hmc.name;
            final String type = hmc.value;

            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle(name);
            builder.setMessage(ctx.getString(R.string.unsupported_message))
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Email", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();

                            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                            intent.setType("plain/text");

                            String aEmailList[] = {"support@tinymatic.de"};

                            intent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);
                            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Support " + type);

                            String text = UnsupportedUtil.getDiagnostics(hmc.rowId);
                            Timber.d(text);
                            intent.putExtra(android.content.Intent.EXTRA_TEXT, text);

                            ctx.startActivity(
                                    Intent.createChooser(intent, ctx.getResources().getString(R.string.email)));
                        }
                    });

            AlertDialog alert = builder.create();
            alert.setOnCancelListener(activityFinishCancelListener);
            alert.setOnDismissListener(activityFinishDimissListener);

            alert.show();
        }
    };

    public OnClickListener rebootCCUHandler = new OnClickListener() {

        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setMessage(ctx.getString(R.string.reboot_ccu_explanation))
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.setOnCancelListener(activityFinishCancelListener);
            alert.setOnDismissListener(activityFinishDimissListener);

            alert.show();
        }
    };

    public OnClickListener hvacHandler = new OnClickListener() {

        protected final int[] hvacVaneValues = {0, 1, 2, 3, 4, 5, 11, 12, 13, 14};

        @Override
        public void onClick(View v) {
            final HMHvac hvac = (HMHvac) v.getTag();

            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

            // builder.setOnCancelListener(activityFinishCancelListener);
            // builder.setOnDismissListener(activityFinishDimissListener);

            // TODO Fix dismiss listener!

            // hotfix, shadowing
            boolean isDarkTheme = PreferenceHelper.isDarkTheme(ctx);

            switch (hvac.getType()) {
                case MODE:

                    View modeView = inflater.inflate(R.layout.hvac_mode_layout, null);
                    Util.setLLChildrenIconColor(isDarkTheme, (LinearLayout) modeView);
                    builder.setView(modeView);
                    builder.setTitle(R.string.mode);

                    final AlertDialog modeDialog = builder.create();
                    modeDialog.setOnDismissListener(activityFinishDimissListener);
                    modeDialog.setOnCancelListener(activityFinishCancelListener);

                    modeView.findViewById(R.id.hvac_mode_cool).setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            ControlHelper.sendOrder(ctx, hvac.getDatapointId(), "3", toastHandler, false, true);
                            modeDialog.cancel();
                        }
                    });

                    modeView.findViewById(R.id.hvac_mode_heat).setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            ControlHelper.sendOrder(ctx, hvac.getDatapointId(), "1", toastHandler, false, true);
                            modeDialog.cancel();
                        }
                    });

                    modeView.findViewById(R.id.hvac_mode_auto).setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            ControlHelper.sendOrder(ctx, hvac.getDatapointId(), "0", toastHandler, false, true);
                            modeDialog.cancel();
                        }
                    });

                    modeView.findViewById(R.id.hvac_mode_dry).setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            ControlHelper.sendOrder(ctx, hvac.getDatapointId(), "14", toastHandler, false, true);
                            modeDialog.cancel();
                        }
                    });

                    modeView.findViewById(R.id.hvac_mode_fan).setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            ControlHelper.sendOrder(ctx, hvac.getDatapointId(), "9", toastHandler, false, true);
                            modeDialog.cancel();
                        }
                    });

                    modeDialog.show();

                    break;

                case SPEED:

                    View speedView = inflater.inflate(R.layout.hvac_speed_layout, null);
                    Util.setLLChildrenIconColor(isDarkTheme, (LinearLayout) speedView);
                    builder.setView(speedView);
                    builder.setTitle(R.string.speed);

                    final AlertDialog speedDialog = builder.create();
                    speedDialog.setOnDismissListener(activityFinishDimissListener);
                    speedDialog.setOnCancelListener(activityFinishCancelListener);

                    speedView.findViewById(R.id.hvac_speed_1).setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            ControlHelper.sendOrder(ctx, hvac.getDatapointId(), "1", toastHandler, false, true);
                            speedDialog.cancel();
                        }
                    });

                    speedView.findViewById(R.id.hvac_speed_2).setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            ControlHelper.sendOrder(ctx, hvac.getDatapointId(), "2", toastHandler, false, true);
                            speedDialog.cancel();
                        }
                    });

                    speedView.findViewById(R.id.hvac_speed_3).setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            ControlHelper.sendOrder(ctx, hvac.getDatapointId(), "3", toastHandler, false, true);
                            speedDialog.cancel();
                        }
                    });

                    speedView.findViewById(R.id.hvac_speed_4).setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            ControlHelper.sendOrder(ctx, hvac.getDatapointId(), "4", toastHandler, false, true);
                            speedDialog.cancel();
                        }
                    });

                    speedView.findViewById(R.id.hvac_speed_auto).setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            ControlHelper.sendOrder(ctx, hvac.getDatapointId(), "0", toastHandler, false, true);
                            speedDialog.cancel();
                        }
                    });

                    speedView.findViewById(R.id.hvac_speed_1).setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            ControlHelper.sendOrder(ctx, hvac.getDatapointId(), "1", toastHandler, false, true);
                            speedDialog.cancel();
                        }
                    });

                    speedDialog.show();

                    break;

                case VANE:

                    int tmpIndex = -1;
                    for (int i = 0; i < hvacVaneValues.length; i++) {
                        if (hvacVaneValues[i] == hvac.getValue()) {
                            tmpIndex = i;
                        }
                    }
                    final int index = tmpIndex;

                    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

                        int newIndex = index;

                        public void onClick(DialogInterface dialog, int which) {
                            if (which == Dialog.BUTTON_POSITIVE) {
                                ControlHelper.sendOrder(ctx, hvac.getDatapointId(),
                                        Integer.toString(hvacVaneValues[newIndex]), toastHandler, false, true);
                                // SyncJobManager.addRefreshNotify(v);
                                dialog.dismiss();
                            } else if (which == Dialog.BUTTON_NEUTRAL) {
                                dialog.cancel();
                            } else {
                                newIndex = which;
                            }
                        }
                    };

                    builder.setTitle(R.string.vane_position);
                    builder.setSingleChoiceItems(ctx.getResources().getStringArray(R.array.hvac_vane_labels), index,
                            listener)
                            .setCancelable(true)
                            .setPositiveButton("Ok", listener)
                            .setNegativeButton(ctx.getString(R.string.cancel), listener);

                    AlertDialog vaneDialog = builder.create();
                    vaneDialog.setOnDismissListener(activityFinishDimissListener);
                    vaneDialog.setOnCancelListener(activityFinishCancelListener);
                    vaneDialog.show();

                    break;

                case ROOM:

                    View roomView = inflater.inflate(R.layout.hvac_room_layout, null);
                    Util.setLLChildrenIconColor(isDarkTheme, (LinearLayout) roomView);
                    builder.setView(roomView);
                    builder.setTitle(R.string.room_occupancy);

                    final AlertDialog roomDialog = builder.create();
                    roomDialog.setOnDismissListener(activityFinishDimissListener);
                    roomDialog.setOnCancelListener(activityFinishCancelListener);

                    roomView.findViewById(R.id.hvac_room_0).setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            ControlHelper.sendOrder(ctx, hvac.getDatapointId(), "0", toastHandler, false, true);
                            roomDialog.cancel();
                        }
                    });

                    roomView.findViewById(R.id.hvac_room_1).setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            ControlHelper.sendOrder(ctx, hvac.getDatapointId(), "1", toastHandler, false, true);
                            roomDialog.cancel();
                        }
                    });

                    roomView.findViewById(R.id.hvac_room_2).setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            ControlHelper.sendOrder(ctx, hvac.getDatapointId(), "2", toastHandler, false, true);
                            roomDialog.cancel();
                        }
                    });

                    roomView.findViewById(R.id.hvac_room_3).setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            ControlHelper.sendOrder(ctx, hvac.getDatapointId(), "3", toastHandler, false, true);
                            roomDialog.cancel();
                        }
                    });

                    roomDialog.show();
                default:
                    break;
            }

        }
    };

    public OnClickListener variableHandler = new OnClickListener() {

        public void onClick(View v) {
            if (v.isClickable()) {
                final HMControllableVar hmc = (HMControllableVar) v.getTag();
                Intent i;
                if (isWidget) {
                    i = new Intent(ctx, SeekBarWithButtonsDialogWidget.class);
                } else {
                    i = new Intent(ctx, SeekBarWithButtonsDialog.class);
                }

                Bundle bun = new Bundle();
                bun.putSerializable("HMSeekable", hmc);
                i.putExtra("viewId", v.getId());

                // SyncJobManager.addRefreshNotify(v);

                i.putExtras(bun);
                if (activity != null) {
                    activity.startActivityForResult(i, Util.REQUEST_REFRESH_NOTIFY);
                } else {
                    ctx.startActivity(i);
                }
                finishWidgetActivity();
            }
        }
    };

    public OnClickListener scriptDialogHandler = new OnClickListener() {

        public void onClick(final View v) {
            HMControllable hmc = (HMControllable) v.getTag();

            Runnable leftButtonRunnable = new Runnable() {
                @Override
                public void run() {
                    ControlHelper.runProgram(ctx, v.getId(), toastHandler);
                    Util.addRefreshNotify(v);
                }
            };

            AlertDialog alertDialog = getAlertDialogWithButtons(ctx.getString(R.string.run_program), leftButtonRunnable, ctx.getString(R.string.cancel), null);

            try {
                Date date = new Date(Long.parseLong(hmc.getValue()) * 1000);
                String message = ctx.getResources().getString(R.string.executed_last) + new SimpleDateFormat(
                        Util.DATE_FORMAT_LONG).format(date);
                alertDialog.setMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }

            alertDialog.setTitle(hmc.name);
            alertDialog.show();
            v.refreshDrawableState();
        }
    };

    private AlertDialog getAlertDialogWithButtons(@Nullable String buttonLeftText, @Nullable final Runnable leftButtonRunnable, @Nullable String buttonRightText, @Nullable final Runnable rightButtonRunnable) {
        View dialogView = inflater.inflate(R.layout.dialog_two_buttons, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(ctx).setView(dialogView).create();

        Button buttonLeft = (Button) dialogView.findViewById(R.id.button_set);
        buttonLeft.setText(buttonLeftText);
        buttonLeft.setOnClickListener(new OnClickListener() {
                                          @Override
                                          public void onClick(View view) {
                                              if (leftButtonRunnable != null) {
                                                  leftButtonRunnable.run();
                                              }
                                              alertDialog.dismiss();
                                          }
                                      }

        );

        Button buttonRight = (Button) dialogView.findViewById(R.id.button_back);
        buttonRight.setText(buttonRightText);
        buttonRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rightButtonRunnable != null) {
                    rightButtonRunnable.run();
                }
                alertDialog.dismiss();
            }
        });

        alertDialog.setOnCancelListener(activityFinishCancelListener);
        alertDialog.setOnDismissListener(activityFinishDimissListener);

        return alertDialog;
    }

    public OnClickListener stringVariableHandler = new OnClickListener() {

        public void onClick(final View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            final HMControllable hmc = (HMControllable) v.getTag();
            String name = hmc.name;
            builder.setMessage(name);

            // Set an EditText view to db user input
            final EditText input = new EditText(ctx);
            input.setText(hmc.value);
            builder.setView(input);

            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Editable value = input.getText();
                    ControlHelper.sendOrder(ctx, v.getId(), value.toString(), toastHandler,
                            hmc.type == HmType.SYSVARIABLE, false);
                    Util.addRefreshNotify(v);
                }
            });

            builder.setNegativeButton(ctx.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.setOnCancelListener(activityFinishCancelListener);
            alertDialog.setOnDismissListener(activityFinishDimissListener);

            builder.show();
        }
    };

    public OnClickListener alarmStateHandler = new OnClickListener() {

        public void onClick(final View v) {

            final HMControllable hmc = (HMControllable) v.getTag();

            int index = -1;
            Integer stateId = DbUtil.getDatapointInt(hmc.rowId, "ARMSTATE");
            if (stateId != null) {
                index = stateId;
            }

            final int finalIndex = index;
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

                int newIndex = finalIndex;

                public void onClick(DialogInterface dialog, int which) {
                    if (which == Dialog.BUTTON_POSITIVE) {
                        ControlHelper.sendOrder(ctx, DbUtil.getDatapointId(hmc.rowId, "ARMSTATE"), Integer.toString(newIndex), toastHandler, true, false);
                        Util.addRefreshNotify(v);
                        dialog.dismiss();
                    } else if (which == Dialog.BUTTON_NEUTRAL) {
                        dialog.cancel();
                    } else {
                        newIndex = which;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle(hmc.getName());
            builder.setSingleChoiceItems(ctx.getResources().getStringArray(R.array.alarm_states), index, listener)
                    .setCancelable(true)
                    .setPositiveButton("Ok", listener)
                    .setNegativeButton(ctx.getString(R.string.cancel), listener);

            AlertDialog dialog = builder.create();
            dialog.setOnDismissListener(activityFinishDimissListener);
            dialog.setOnCancelListener(activityFinishCancelListener);
            dialog.show();
        }
    };

    public OnClickListener colorProgramListHandler = new OnClickListener() {

        public void onClick(final View v) {

            final HMControllable hmc = (HMControllable) v.getTag();

            int index = -1;
            Integer programmId = DbUtil.getDatapointInt(hmc.rowId, "PROGRAM");
            if (programmId != null) {
                index = programmId;
            }

            final int finalIndex = index;
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

                int newIndex = finalIndex;

                public void onClick(DialogInterface dialog, int which) {
                    if (which == Dialog.BUTTON_POSITIVE) {
                        ControlHelper.sendOrder(ctx, DbUtil.getDatapointId(hmc.rowId, "PROGRAM"), Integer.toString(newIndex), toastHandler, true, false);
                        Util.addRefreshNotify(v);
                        dialog.dismiss();
                    } else if (which == Dialog.BUTTON_NEUTRAL) {
                        dialog.cancel();
                    } else {
                        newIndex = which;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle(hmc.getName());
            builder.setSingleChoiceItems(ctx.getResources().getStringArray(R.array.color_programs), index, listener)
                    .setCancelable(true)
                    .setPositiveButton("Ok", listener)
                    .setNegativeButton(ctx.getString(R.string.cancel), listener);

            AlertDialog dialog = builder.create();
            dialog.setOnDismissListener(activityFinishDimissListener);
            dialog.setOnCancelListener(activityFinishCancelListener);
            dialog.show();

        }
    };

    public OnClickListener valueListVariableHandler = new OnClickListener() {

        public void onClick(final View v) {

            final HMControllable hmc = (HMControllable) v.getTag();
            String[] items = hmc.value_list.split(";");

            int tempIndex;
            try {
                tempIndex = Integer.parseInt(hmc.value.replaceAll("[^\\d.]", ""));
            } catch (Exception e) {
                e.printStackTrace();
                tempIndex = 0;
            }
            final int index = tempIndex;

            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

                int newIndex = index;

                public void onClick(DialogInterface dialog, int which) {
                    if (which == Dialog.BUTTON_POSITIVE) {
                        ControlHelper.sendOrder(ctx, hmc.rowId, Integer.toString(newIndex), toastHandler, true, false);
                        Util.addRefreshNotify(v);
                        dialog.dismiss();
                    } else if (which == Dialog.BUTTON_NEUTRAL) {
                        dialog.cancel();
                    } else {
                        newIndex = which;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle(hmc.getName());
            builder.setSingleChoiceItems(items, index, listener)
                    .setCancelable(true)
                    .setPositiveButton("Ok", listener)
                    .setNegativeButton(ctx.getString(R.string.cancel), listener);

            AlertDialog dialog = builder.create();
            dialog.setOnDismissListener(activityFinishDimissListener);
            dialog.setOnCancelListener(activityFinishCancelListener);
            dialog.show();

        }
    };


    public static void setIcon(View view, int iconRes) {
        ((ImageView) view.findViewById(R.id.icon)).setImageResource(iconRes);
    }
}
