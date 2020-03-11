package de.ebertp.HomeDroid.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.view.ContextThemeWrapper;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.triggertrap.seekarc.SeekArc;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.ebertp.HomeDroid.Communication.Control.ControlHelper;
import de.ebertp.HomeDroid.Communication.Control.HMControllableVar;
import de.ebertp.HomeDroid.Communication.Control.HmType;
import de.ebertp.HomeDroid.Connection.BroadcastHelper;
import de.ebertp.HomeDroid.DbAdapter.DataBaseAdapterManager;
import de.ebertp.HomeDroid.DbAdapter.DbUtil;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.ToastHandler;
import de.ebertp.HomeDroid.Utils.Util;
import timber.log.Timber;

import static de.ebertp.HomeDroid.Communication.Control.HmType.BLIND;
import static de.ebertp.HomeDroid.Communication.Control.HmType.BLIND_WITH_LAMELLA;
import static de.ebertp.HomeDroid.Communication.Control.HmType.BLIND_WITH_LAMELLA_IP;
import static de.ebertp.HomeDroid.Communication.Control.HmType.COLOR;
import static de.ebertp.HomeDroid.Communication.Control.HmType.DIMMER;
import static de.ebertp.HomeDroid.Communication.Control.HmType.DIMMER_IP;
import static de.ebertp.HomeDroid.Communication.Control.HmType.DIMMER_IP_COLOR;
import static de.ebertp.HomeDroid.Communication.Control.HmType.LAMELLA;
import static de.ebertp.HomeDroid.Communication.Control.HmType.LAMELLA_IP;
import static de.ebertp.HomeDroid.Communication.Control.HmType.LIGHTIFY_DIMMER;
import static de.ebertp.HomeDroid.Communication.Control.HmType.LIGHTIFY_DIMMER_RGBW;
import static de.ebertp.HomeDroid.Communication.Control.HmType.SYSVARIABLE;
import static de.ebertp.HomeDroid.Communication.Control.HmType.VARIABLECLIMATE;
import static de.ebertp.HomeDroid.Communication.Control.HmType.VARIABLECLIMATE_IP;
import static de.ebertp.HomeDroid.Communication.Control.HmType.VARIABLETEMP;
import static de.ebertp.HomeDroid.Communication.Control.HmType.VARIABLETHERMOSTATE_IP;
import static de.ebertp.HomeDroid.Communication.Control.HmType.WINDOW;

public class SeekBarWithButtonsDialog extends ThemedDialogActivity {

    private static final int NO_DECIMAL = 1;
    private static final int TWO_DECIMAL = 100;
    private static final int ONE_DECIMAL = 10;

    private ButtonRepeater mButtonRepeater = new ButtonRepeater(new Handler());
    private DataBaseAdapterManager dBm;
    private ToastHandler toastHandler;

    //10 = one decimal point
    private int mNumOfDecimalPoints;
    private double mIncrementStep = 1;

    public HMControllableVar hms;

    private int newValue;
    private int mClimateControlModeIndex;
    private boolean mIgnoreNextSeekbarUpdate = false;

    SeekAdapter seekAdapter;

    @BindView(R.id.seekBar)
    SeekBar mSeekBarNormal;

    @BindView(R.id.seekArc)
    SeekArc mSeekArc;

    @BindView(R.id.buttonMinus)
    ImageButton mButtonMinus;
    @BindView(R.id.buttonPlus)
    ImageButton mButtonPlus;

    @BindView(R.id.layout_climate_mode)
    LinearLayout mLayoutClimateMode;
    @BindView(R.id.buttonMode)
    Button mButtonMode;
    @BindView(R.id.buttonBoost)
    Button mButtonBoost;
    @BindView(R.id.buttonProfile)
    Button mButtonProfile;
    @BindView(R.id.buttonColorWhite)
    ImageButton mButtonColorWhite;

    @BindView(R.id.layout_up_down)
    LinearLayout mLayoutUpDown;
    @BindView(R.id.buttonMin)
    ImageButton mButtonMin;
    @BindView(R.id.buttonStop)
    ImageButton mButtonStop;
    @BindView(R.id.buttonMax)
    ImageButton mButtonMax;

    @BindView(R.id.layout_lamella)
    LinearLayout mLamellaLayout;
    @BindView(R.id.button_lamella)
    Button mButtonLamella;

    @BindView(R.id.layout_color)
    LinearLayout mColorLayout;
    @BindView(R.id.button_color)
    Button mButtonColor;

    @BindView(R.id.layout_open_close)
    LinearLayout mLayoutOpenClose;
    @BindView(R.id.buttonOpen)
    ImageButton mButtonOpen;
    @BindView(R.id.buttonReset)
    ImageButton mButtonReset;
    @BindView(R.id.buttonClose)
    ImageButton mButtonClose;

    @BindView(R.id.button_set)
    Button mButtonSet;

    private EditText mProgressText;
    private TextView mProgressUnit;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        hms = (HMControllableVar) extras.getSerializable("HMSeekable");

        dBm = HomeDroidApp.db();
        toastHandler = new ToastHandler(this);

        setContentView(R.layout.seekbar_layout);
        ButterKnife.bind(this);

        setTitle(hms.name);

        if (hms.type == VARIABLECLIMATE || hms.type == VARIABLETEMP || hms.type == VARIABLECLIMATE_IP || hms.type == VARIABLETHERMOSTATE_IP) {
            mProgressText = (EditText) findViewById(R.id.progressTextArc);
            mProgressUnit = (TextView) findViewById(R.id.progressUnitArc);
            findViewById(R.id.seekArcContainer).setVisibility(View.VISIBLE);
            findViewById(R.id.layout_progress).setVisibility(View.GONE);
            mSeekBarNormal.setVisibility(View.GONE);
            seekAdapter = new SeekAdapter(mSeekArc);
        } else {
            mProgressText = (EditText) findViewById(R.id.progressText);
            mProgressUnit = (TextView) findViewById(R.id.progressUnit);
            seekAdapter = new SeekAdapter(mSeekBarNormal);
        }

        mProgressUnit.setText(hms.unit);

        if (hms.type == COLOR || hms.type == BLIND || hms.type == BLIND_WITH_LAMELLA || hms.type == LAMELLA || hms.type == BLIND_WITH_LAMELLA_IP || hms.type == LAMELLA_IP
                || hms.type == DIMMER || hms.type == DIMMER_IP || hms.type == DIMMER_IP_COLOR || hms.type == LIGHTIFY_DIMMER || hms.type == LIGHTIFY_DIMMER_RGBW) {
            mNumOfDecimalPoints = NO_DECIMAL;
        } else if (hms.type == SYSVARIABLE) {
            mNumOfDecimalPoints = TWO_DECIMAL;
        } else {
            mNumOfDecimalPoints = ONE_DECIMAL;
        }

        hms.min = hms.min * mNumOfDecimalPoints;
        hms.max = hms.max * mNumOfDecimalPoints;

        if (hms.type == VARIABLECLIMATE || hms.type == VARIABLETEMP || hms.type == VARIABLECLIMATE_IP || hms.type == VARIABLETHERMOSTATE_IP) {
            mIncrementStep = 0.5;
        }

        setupSeekBar();

        if (hms.type == BLIND || hms.type == BLIND_WITH_LAMELLA || hms.type == BLIND_WITH_LAMELLA_IP) {
            mLayoutUpDown.setVisibility(View.VISIBLE);
            addBlindButtonHandling();
        }

        if (hms.type == BLIND_WITH_LAMELLA) {
            mLamellaLayout.setVisibility(View.VISIBLE);
            addLamellaButtonHandling();
        }

        if (hms.type == BLIND_WITH_LAMELLA_IP) {
            mLamellaLayout.setVisibility(View.VISIBLE);
            addLamellaIpButtonHandling();
        }

        if (hms.type == DIMMER_IP_COLOR) {
            mColorLayout.setVisibility(View.VISIBLE);
            addColorButtonHandling();
        }

        if (hms.type == WINDOW || hms.type == DIMMER || hms.type == DIMMER_IP || hms.type == DIMMER_IP_COLOR || hms.type == LIGHTIFY_DIMMER || hms.type == LIGHTIFY_DIMMER_RGBW) {
            mLayoutOpenClose.setVisibility(View.VISIBLE);
            addOpenCloseButtonHandling();

            if (hms.type != WINDOW) {
                mButtonPlus.setImageResource(R.drawable.flat_brighter);
                mButtonMinus.setImageResource(R.drawable.flat_darker);
            }
        }

        if (hms.type == DIMMER || hms.type == DIMMER_IP || hms.type == DIMMER_IP_COLOR) {
            mButtonReset.setVisibility(View.VISIBLE);
            addResetButtonHandling();
        }

        if (hms.type == LIGHTIFY_DIMMER_RGBW) {
            mButtonReset.setVisibility(View.VISIBLE);
            addResetAsRGBButtonHandling();
        }


        if (hms.type == VARIABLECLIMATE || hms.type == VARIABLECLIMATE_IP || hms.type == VARIABLETHERMOSTATE_IP) {
            mLayoutClimateMode.setVisibility(View.VISIBLE);
            setupClimateControlMode();

            if (hms.type == VARIABLECLIMATE_IP || hms.type == VARIABLETHERMOSTATE_IP) {
                mButtonBoost.setVisibility(View.VISIBLE);
                addBoostButtonHandling();

                mButtonProfile.setVisibility(View.VISIBLE);
                addProfileButtonHandling();
            }

        }

        if (hms.type == COLOR) {
            mButtonColorWhite.setVisibility(View.VISIBLE);
            addColorWhiteButtonHandling();
        }

        if (hms.type == COLOR) {
            setupColorSeekbar();
        }

        mButtonSet.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (hms.type == SYSVARIABLE) {
                    if (newValue < hms.min || newValue > hms.max) {
                        Toast.makeText(SeekBarWithButtonsDialog.this, getString(R.string.invalid_value),
                                Toast.LENGTH_LONG).show();
                    }
                }

                sendNewValue(getNewAdjustedValue());
            }
        });

        mButtonMinus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                seekAdapter.setProgress((int) (seekAdapter.getProgress() - mIncrementStep * mNumOfDecimalPoints));
                setNewValue(seekAdapter.getProgress() + hms.min);
                setProgressText();
            }
        });

        // Auto Decrement for a long click
        mButtonMinus.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View arg0) {
                startRepeating((int) (-mIncrementStep * mNumOfDecimalPoints));
                return false;
            }
        });

        // When the button is released, if we're auto decrementing, stop
        mButtonMinus.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    startRepeating(0);
                }
                return false;
            }
        });

        mButtonPlus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                seekAdapter.setProgress((int) (seekAdapter.getProgress() + mIncrementStep * mNumOfDecimalPoints));
                setNewValue(seekAdapter.getProgress() + hms.min);
                setProgressText();
            }
        });

        // Auto Decrement for a long click
        mButtonPlus.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View arg0) {
                startRepeating((int) (mIncrementStep * mNumOfDecimalPoints));
                return false;
            }
        });

        // When the button is released, if we're auto decrementing, stop
        mButtonPlus.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    startRepeating(0);
                }
                return false;
            }
        });

        if (!PreferenceHelper.isDarkTheme(this)) {
            Util.tintDark(mButtonReset);
            Util.tintDark(mButtonOpen);
            Util.tintDark(mButtonClose);
            Util.tintDark(mButtonMinus);
            Util.tintDark(mButtonPlus);
            Util.tintDark(mButtonStop);
            Util.tintDark(mButtonMin);
        }
    }

    private void addLamellaIpButtonHandling() {
        mButtonLamella.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SeekBarWithButtonsDialog.this, SeekBarWithButtonsDialog.this.getClass());

                Integer datapointId = DbUtil.getDatapointId(hms.rowId, "LEVEL_2");

                double value = 0;

                Double level = DbUtil.getDatapointDouble(hms.rowId, "LEVEL_2");
                if (level != null) {
                    value = Math.round(level * 100);
                }

                HMControllableVar lamellaVar = new HMControllableVar(hms.rowId, hms.name, "%", HmType.LAMELLA_IP, 0, 100, true, "%", value, datapointId);

                Bundle bun = new Bundle();
                bun.putSerializable("HMSeekable", lamellaVar);
                i.putExtra("viewId", getIntent().getExtras().getInt("viewId"));
                i.putExtras(bun);

                SeekBarWithButtonsDialog.this.startActivityForResult(i, Util.REQUEST_REFRESH_NOTIFY);
            }
        });
    }

    private void addColorButtonHandling() {
        mButtonColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String color = DbUtil.getDatapointString(hms.rowId, "COLOR");


                int selectedColor = -1;
                if (color != null) {
                    selectedColor = Integer.parseInt(color);
                }

                final AlertDialog.Builder dialog = new AlertDialog.Builder(new ContextThemeWrapper(SeekBarWithButtonsDialog.this, getTheme()));
                dialog.setTitle(R.string.color);
                dialog.setSingleChoiceItems(R.array.colors, selectedColor, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int datapointId = DbUtil.getDatapointId(hms.rowId, "COLOR");
                        ControlHelper.sendOrder(SeekBarWithButtonsDialog.this, datapointId, Integer.toString(i), toastHandler, false, true);
                    }
                });
                dialog.create().show();
            }
        });
    }

    private void addLamellaButtonHandling() {
        mButtonLamella.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SeekBarWithButtonsDialog.this, SeekBarWithButtonsDialog.this.getClass());

                Integer datapointId = DbUtil.getDatapointId(hms.rowId, "LEVEL_SLATS");

                double value = 0;

                Double level = DbUtil.getDatapointDouble(hms.rowId, "LEVEL_SLATS");
                if (level != null) {
                    value = Math.round(level * 100);
                }

                HMControllableVar lamellaVar = new HMControllableVar(hms.rowId, hms.name, "%", HmType.LAMELLA, 0, 100, true, "%", value, datapointId);

                Bundle bun = new Bundle();
                bun.putSerializable("HMSeekable", lamellaVar);
                i.putExtra("viewId", getIntent().getExtras().getInt("viewId"));
                i.putExtras(bun);

                SeekBarWithButtonsDialog.this.startActivityForResult(i, Util.REQUEST_REFRESH_NOTIFY);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Util.REQUEST_REFRESH_NOTIFY) {
            if (resultCode == Activity.RESULT_OK) {
                finishOnSent();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        BroadcastHelper.refreshUI();
    }

    private void addResetAsRGBButtonHandling() {
        mButtonReset.setImageResource(R.drawable.flat_pick_color);
        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PreferenceHelper.getApiVersion(SeekBarWithButtonsDialog.this).equals("1.10")) {
                    Toast.makeText(SeekBarWithButtonsDialog.this, R.string.xml_api_112_required, Toast.LENGTH_LONG).show();
                    return;
                }

                showRGBWDialog();
            }
        });
    }

    private void setupColorSeekbar() {
        ViewTreeObserver vto = seekAdapter.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                seekAdapter.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int width = seekAdapter.getMeasuredWidth();

                LinearGradient test = new LinearGradient(0.f, 0.f, width, 0.0f,

                        new int[]{
                                0xFFFF0000, // red
                                0xFFFFFF00, // yellow
                                0xFF00FF00, // green
                                0xFF00FFFF, // cyan
                                0xFF0000FF, // blue
                                0xFFFF00FF, // magenta
                                0xFFFF0000, // red
                        },

                        new float[]{
                                0.03f, // red
                                0.15f, // yellow
                                0.30f, // green
                                0.40f, // cyan
                                0.60f, // blue
                                0.80f, // magenta
                                0.90f, // red
                        },

                        Shader.TileMode.CLAMP);
                ShapeDrawable shape = new ShapeDrawable(new RectShape());
                shape.getPaint().setShader(test);

                seekAdapter.setProgressDrawable(shape);

            }
        });
    }


    private void setupSeekBar() {
        setNewValue(hms.doubleValue * mNumOfDecimalPoints);
        setProgressText();

        mProgressText.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {

                if (mIgnoreNextSeekbarUpdate) {
                    mIgnoreNextSeekbarUpdate = false;
                    return;
                }

                try {
                    setNewValue(Float.parseFloat(s.toString()) * mNumOfDecimalPoints);
                    seekAdapter.setProgress((int) (getNewValue() - (hms.min)));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    mProgressText.setText(Double.toString(getNewValue()));
                }
            }
        });

        seekAdapter.setMax((int) (hms.max - hms.min));
        seekAdapter.setProgress((int) (getNewValue() - hms.min));
        seekAdapter.setOnSeekBarChangeListener(new SeekAdapter.OnSeekThingChangedListener() {
            @Override
            public void onProgressChanged(int progress, boolean fromUser) {
                if (fromUser) {
                    setNewValue(progress + hms.min);
                    setProgressText();
                    //ignore next seekbar change in TextWatcher, otherwise we end up in a loop
                    mIgnoreNextSeekbarUpdate = true;
                }
            }
        });
    }

    private void addColorWhiteButtonHandling() {
        findViewById(R.id.buttonColorWhite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seekAdapter.setProgress(200);
                setNewValue(seekAdapter.getProgress());
                setProgressText();
            }
        });
    }

    private void addBoostButtonHandling() {
        mButtonBoost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor dData = dBm.datapointDbAdapter.fetchItemsByChannelAndType(hms.rowId, "BOOST_MODE");
                if (dData.getCount() != 0) {
                    int datapointId = dData.getInt(dData.getColumnIndex("_id"));
                    ControlHelper.sendOrder(SeekBarWithButtonsDialog.this, datapointId, "true", toastHandler, false, true);
                }

                finishOnSent();
            }
        });
    }

    private void addProfileButtonHandling() {
        mButtonProfile.setOnClickListener(view -> {
            ViewGroup dialogView = (ViewGroup) getLayoutInflater().inflate(R.layout.dialog_grid, null);
            final AlertDialog alertDialog = new AlertDialog.Builder(SeekBarWithButtonsDialog.this).setView(dialogView).create();

            int profileCount;
            if (hms.type == VARIABLECLIMATE_IP) {
                profileCount = 6;
            } else {
                profileCount = 3;
            }

            for (int i = 0; i < profileCount; i++) {
                int profile = i + 1;
                Button child = (Button) getLayoutInflater().inflate(R.layout.grid_button, dialogView, false);
                child.setOnClickListener(view1 -> {
                            int datapointId = DbUtil.getDatapointId(hms.rowId, "ACTIVE_PROFILE");
                            ControlHelper.sendOrder(SeekBarWithButtonsDialog.this, datapointId, Integer.toString(profile), toastHandler, false, true);
                            alertDialog.dismiss();
                        }
                );
                child.setText(Integer.toString(profile));
                dialogView.addView(child);
            }

            alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            alertDialog.show();
        });
    }

    private void setupClimateControlMode() {
        Cursor dData = dBm.datapointDbAdapter.fetchItemsByChannelAndType(hms.rowId, (hms.type == VARIABLECLIMATE_IP || hms.type == VARIABLETHERMOSTATE_IP) ? "SET_POINT_MODE" : "CONTROL_MODE");
        if (dData.getCount() != 0) {
            String controlMode = dData.getString(dData.getColumnIndex("value"));
            try {
                mClimateControlModeIndex = Integer.parseInt(controlMode);
            } catch (Exception e) {
                mClimateControlModeIndex = 0;
            }
        }

        mButtonMode.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SeekBarWithButtonsDialog.this);

                DialogInterface.OnClickListener listener = getClimateControlListener();
                builder.setTitle(getString(R.string.mode));
                builder.setSingleChoiceItems(getResources().getStringArray((hms.type == VARIABLECLIMATE_IP || hms.type == VARIABLETHERMOSTATE_IP) ? R.array.climate_mode_labels_ip : R.array.climate_mode_labels), mClimateControlModeIndex, listener)
                        .setCancelable(true).setPositiveButton("Ok", listener)
                        .setNegativeButton(getString(R.string.cancel), listener);
                builder.create().show();
            }
        });
    }

    private void addResetButtonHandling() {
        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hms.doubleValue == 0) {
                    if (hms.type == DIMMER) {
                        Cursor dData = dBm.datapointDbAdapter.fetchItemsByChannelAndType(hms.rowId, "OLD_LEVEL");
                        if (dData.getCount() != 0) {
                            int datapointId = dData.getInt(dData.getColumnIndex("_id"));
                            ControlHelper.sendOrder(SeekBarWithButtonsDialog.this, datapointId, "1", toastHandler, false, true);
                            finishOnSent();
                        }
                    } else {
                        sendNewValue(1d);
                    }
                } else {
                    sendNewValue(1d);
                }
            }
        });
    }

    private void addOpenCloseButtonHandling() {
        mButtonOpen.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Double openValue;
                if (hms.type == WINDOW) {
                    openValue = -0.005;
                } else {
                    openValue = 0 - hms.min;
                }
                sendNewValue(openValue);
            }
        });

        mButtonClose.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Double closeValue = (100 * mNumOfDecimalPoints) - hms.min;
                sendNewValue(closeValue);
            }
        });
    }

    private void addBlindButtonHandling() {
        mButtonMin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                sendNewValue(0d, false);
            }
        });

        mButtonMax.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                sendNewValue(1d, false);
            }
        });

        mButtonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor dData = dBm.datapointDbAdapter.fetchItemsByChannelAndType(hms.rowId, "STOP");
                if (dData.getCount() != 0) {
                    int datapointId = dData.getInt(dData.getColumnIndex("_id"));
                    ControlHelper.sendOrder(SeekBarWithButtonsDialog.this, datapointId, "true", toastHandler, false, true);
                }
            }
        });
    }

    private void showRGBWDialog() {
        ColorPickerDialogBuilder
                .with(this)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .noSliders()
                .setOnColorSelectedListener(new OnColorSelectedListener() {

                    @Override
                    public void onColorSelected(int selectedColor) {
                        Timber.d("onColorSelected: 0x" + Integer.toHexString(selectedColor));
                    }
                })
                .setPositiveButton(R.string.ok, new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int color, Integer[] allColors) {
                        Timber.d("Color picked" + Integer.toHexString(color));

                        float red = (color >> 16) & 0xFF;
                        float green = (color >> 8) & 0xFF;
                        float blue = (color >> 0) & 0xFF;

                        Integer datapointId = DbUtil.getDatapointId(hms.getRowId(), "RGBW");
                        String value = "rgb(" + (int) red + "," + (int) green + "," + (int) blue + ",255)";
                        ControlHelper.sendOrder(SeekBarWithButtonsDialog.this, datapointId, value, toastHandler, false, true);

                        dialog.dismiss();
                        finishOnSent();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .build()
                .show();
    }

    private DialogInterface.OnClickListener getClimateControlListener() {

        if (hms.type == VARIABLECLIMATE_IP || hms.type == VARIABLETHERMOSTATE_IP) {
            return new DialogInterface.OnClickListener() {

                int newIndex = mClimateControlModeIndex;

                public void onClick(DialogInterface dialog, int which) {
                    if (which == Dialog.BUTTON_POSITIVE) {

                        Cursor dData = dBm.datapointDbAdapter.fetchItemsByChannelAndType(hms.rowId, "CONTROL_MODE");
                        if (dData.getCount() != 0) {
                            int datapointId = dData.getInt(dData.getColumnIndex("_id"));
                            ControlHelper.sendOrder(SeekBarWithButtonsDialog.this, datapointId, Integer.toString(newIndex), toastHandler, false, true);
                        }

                        dialog.dismiss();

                        finishOnSent();
                    } else if (which == Dialog.BUTTON_NEUTRAL) {
                        dialog.cancel();
                    } else {
                        newIndex = which;
                    }
                }
            };
        }

        if (hms.type == VARIABLECLIMATE) {

            return new DialogInterface.OnClickListener() {

                int newIndex = mClimateControlModeIndex;

                public void onClick(DialogInterface dialog, int which) {
                    if (which == Dialog.BUTTON_POSITIVE) {

                        String modeName;

                        switch (newIndex) {
                            case 0:
                                modeName = "AUTO_MODE";
                                break;
                            case 1:
                                modeName = "MANU_MODE";
                                break;
                            case 2:
                                modeName = "LOWERING_MODE";
                                break;
                            case 3:
                                modeName = "BOOST_MODE";
                                break;
                            default:
                                modeName = "AUTO_MODE";
                                break;
                        }

                        Cursor dData = dBm.datapointDbAdapter.fetchItemsByChannelAndType(hms.rowId, modeName);
                        if (dData.getCount() != 0) {
                            int datapointId = dData.getInt(dData.getColumnIndex("_id"));
                            ControlHelper.sendOrder(SeekBarWithButtonsDialog.this, datapointId, Integer.toString(newIndex), toastHandler, false, true);
                        }

                        dialog.dismiss();

                        finishOnSent();
                    } else if (which == Dialog.BUTTON_NEUTRAL) {
                        dialog.cancel();
                    } else {
                        newIndex = which;
                    }
                }
            };
        }
        return null;
    }

    private void setProgressText() {
        Double printableNewValue = getNewValue() / mNumOfDecimalPoints;

        // Don't show decimal points for type COLOR
        mProgressText.setText(mNumOfDecimalPoints == NO_DECIMAL ? String.format("%d", printableNewValue.longValue()) : String.format("%s", printableNewValue));
    }

    private void startRepeating(int inc) {
        mButtonRepeater.startRepeating(inc);
    }

    private double getNewValue() {
        return newValue;
    }

    private void setNewValue(double newValue) {
        this.newValue = (int) newValue;
    }

    private class ButtonRepeater implements Runnable {

        private static final int DEF_REPINT = 200;
        private static final int MIN_REPINT = 40;

        private final Handler mRepeatHandler;

        private int mRepeatInterval = DEF_REPINT;
        private int mRepeatDefInt = DEF_REPINT;
        private int mRepeatAccel = 200;
        private int mRepeatValue = 0;

        ButtonRepeater(Handler handler) {
            mRepeatHandler = handler;
        }

        public void run() {
            if (mRepeatValue > 0) {
                seekAdapter.setProgress((int) (seekAdapter.getProgress() + mIncrementStep * mNumOfDecimalPoints));
                setNewValue(seekAdapter.getProgress() + hms.min);
                setProgressText();
            } else if (mRepeatValue < 0) {
                seekAdapter.setProgress((int) (seekAdapter.getProgress() - mIncrementStep * mNumOfDecimalPoints));
                setNewValue(seekAdapter.getProgress() + hms.min);
                setProgressText();
            }
            if (mRepeatValue != 0) {
                mRepeatHandler.postDelayed(mButtonRepeater, mRepeatInterval);
                mRepeatInterval -= mRepeatAccel;
                if (mRepeatInterval < MIN_REPINT) {
                    mRepeatInterval = MIN_REPINT;
                }
            } else { // Restore to default repeat rate
                mRepeatInterval = mRepeatDefInt;
            }
        }

        public void startRepeating(int inc) {
            mRepeatValue = inc;
            if (inc != 0) {
                mRepeatHandler.postDelayed(mButtonRepeater, mRepeatInterval);
            }
        }
    }

    private Double getNewAdjustedValue() {
        if (hms.isPercent) {
            return getNewValue() / mNumOfDecimalPoints / 100;
        } else {
            return getNewValue() / mNumOfDecimalPoints;
        }
    }

    private void sendNewValue(Double newFormattedValue) {
        sendNewValue(newFormattedValue, true);
    }

    private void sendNewValue(Double newFormattedValue, boolean closeWindow) {
        ControlHelper.sendOrder(this, hms.datapointId, newFormattedValue.toString(), toastHandler, hms.type == SYSVARIABLE, true);
        if (closeWindow) {
            finishOnSent();
        }
    }

    private void finishOnSent() {
        Intent i = new Intent();
        i.putExtra("viewId", getIntent().getExtras().getInt("viewId"));
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    private static class SeekAdapter {

        SeekBar mSeekbar;
        SeekArc mSeekArc;

        public SeekAdapter(SeekBar seekBar) {
            mSeekbar = seekBar;
        }

        public SeekAdapter(SeekArc seekArc) {
            mSeekArc = seekArc;
        }

        public double getProgress() {
            if (mSeekArc != null) {
                return mSeekArc.getProgress();
            }
            return mSeekbar.getProgress();
        }

        public void setProgress(int progress) {
            if (mSeekArc != null) {
                mSeekArc.setProgress(progress);
            } else {
                mSeekbar.setProgress(progress);
            }
        }

        public ViewTreeObserver getViewTreeObserver() {
            if (mSeekArc != null) {
                return mSeekArc.getViewTreeObserver();
            }
            return mSeekbar.getViewTreeObserver();
        }

        public int getMeasuredWidth() {
            if (mSeekArc != null) {
                return mSeekArc.getMeasuredWidth();
            }
            return mSeekbar.getMeasuredWidth();
        }

        public void setProgressDrawable(ShapeDrawable progressDrawable) {
            if (mSeekArc != null) {
                throw new IllegalStateException("Not supported by SeekArc!");
            } else {
                mSeekbar.setProgressDrawable(progressDrawable);
            }
        }

        public void setMax(int max) {
            if (mSeekArc != null) {
                mSeekArc.setMax(max);
            } else {
                mSeekbar.setMax(max);
            }
        }

        public void setOnSeekBarChangeListener(final OnSeekThingChangedListener listener) {
            if (mSeekArc != null) {
                mSeekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
                    @Override
                    public void onProgressChanged(SeekArc seekArc, int progress, boolean b) {
                        listener.onProgressChanged(progress, b);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekArc seekArc) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekArc seekArc) {

                    }
                });
            } else {
                mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                        listener.onProgressChanged(progress, b);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
            }
        }

        public interface OnSeekThingChangedListener {

            void onProgressChanged(int progress, boolean fromUser);

        }
    }

}
