package de.ebertp.HomeDroid.Activities.Listing;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.ebertp.HomeDroid.Activities.GallerySelectIcon;
import de.ebertp.HomeDroid.Communication.Control.HMControllable;
import de.ebertp.HomeDroid.DbAdapter.DataBaseAdapterManager;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.InterceptViewGroup;
import de.ebertp.HomeDroid.Model.HMChannel;
import de.ebertp.HomeDroid.Model.HMLayer;
import de.ebertp.HomeDroid.Model.HMLayerItem;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.Model.HMRoom;
import de.ebertp.HomeDroid.Model.WebCam;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.BitmapUtil;
import de.ebertp.HomeDroid.Utils.Compatibility.ThumbNailGetter;
import de.ebertp.HomeDroid.Utils.CustomImageHelper;
import de.ebertp.HomeDroid.Utils.PermissionUtil;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.PrefixHelper;
import de.ebertp.HomeDroid.Utils.Util;
import de.ebertp.HomeDroid.Utils.WebcamHelper;
import de.ebertp.HomeDroid.ViewAdapter.CursorToObjectHelper;
import de.ebertp.HomeDroid.ViewAdapter.ListViewGenerator;

public class LayerActivity extends ServiceContentActivity {

    private static final int REQUEST_FILESYSTEM_ICON = 0;
    private static final int REQUEST_FILESYSTEM_BACKGROUND = 1;
    private DataBaseAdapterManager dbM;
    private RelativeLayout rlMain;
    private RelativeLayout rlContent;
    private int layerId;
    private CustomImageHelper customImageHelper;
    private ArrayList<HMLayer> spinnerItems;
    private int screenWidth;
    private int screenHeight;

    public static final int LAYER_ITEM_TYPE_LAYER = 0;
    public static final int LAYER_ITEM_TYPE_ROOM = 1;
    public static final int LAYER_ITEM_TYPE_CATEGORY = 2;
    public static final int LAYER_ITEM_TYPE_DEVICE = 3;
    public static final int LAYER_ITEM_TYPE_VARIABLE = 4;
    public static final int LAYER_ITEM_TYPE_SCRIPT = 5;
    public static final int LAYER_ITEM_TYPE_WEBCAM = 6;

    private static final int LAYER_ITEM_SIZE_TINY = 1;
    private static final int LAYER_ITEM_SIZE_SMALL = 2;
    private static final int LAYER_ITEM_SIZE_NORMAL = 3;
    private static final int LAYER_ITEM_SIZE_LARGE = 4;

    private int mItemSize = LAYER_ITEM_SIZE_SMALL;
    private boolean mShowIcon = false;
    private boolean mShowText = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (setOrientation()) {
            Log.d("TAG", "Changing orientation");
        }

        if (PreferenceHelper.isDarkTheme(this)) {
            setTheme(R.style.HomeDroidDark);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                getWindow().setStatusBarColor(getResources().getColor(R.color.homedroid_primary));
            }
        }
        setContentView(R.layout.activity_layer);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbM = HomeDroidApp.db();
        customImageHelper = new CustomImageHelper(this);

        rlMain = (RelativeLayout) findViewById(R.id.rl_main);
        rlMain.setOnDragListener(new ItemDragListener());
        rlContent = (RelativeLayout) findViewById(R.id.rl_content);
        layerId = PreferenceHelper.getDefaultLayer(this);

        if (layerId != -1) {
            Cursor c = dbM.layerDbAdapter.fetchItem(layerId);

            if (c.getCount() == 0) {
                PreferenceHelper.setDefaultLayer(this, -1);
                layerId = 1 + PrefixHelper.getFullPrefix(this);
            }

            c.close();
        } else {
            layerId = 1 + PrefixHelper.getFullPrefix(this);
        }

        DisplayMetrics display = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(display);
        screenWidth = display.widthPixels;
        screenHeight = display.heightPixels;

        if (dbM.layerDbAdapter.isEmpty(PrefixHelper.getPrefix(LayerActivity.this))) {
            Log.i(this.getClass().getName(), "Layer Db empty, creating base layer");
            createFirstLayer();
        }

        doHeaderLayout();
        doNavigationLayout();

        if (!PreferenceHelper.isLayerHelpShown(this)) {
            PreferenceHelper.setIsLayerHelpShown(this, true);

            AlertDialog.Builder alert = new AlertDialog.Builder(LayerActivity.this);

            alert.setTitle(getString(R.string.layer_welcome_title));
            alert.setMessage(getString(R.string.layer_welcome_message));
            alert.setPositiveButton(R.string.mode_compact, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    PreferenceHelper.setIsMinimalMode(LayerActivity.this, true);
                    doItemsLayout();
                    showSetOrientationDialog();
                }
            });
            alert.setNegativeButton(R.string.mode_normmal, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    showSetOrientationDialog();
                }
            });
            alert.show();
        } else if (!PreferenceHelper.isLayerOrientationSet(this)) {
            showSetOrientationDialog();
        }
    }

    private void showSetOrientationDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(LayerActivity.this);
        alert.setTitle(R.string.select_orientation);
        alert.setMessage(R.string.select_orientation_desc);
        alert.setPositiveButton(R.string.orientation_landscape, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                PreferenceHelper.setOrientationLandscape(LayerActivity.this, true);
                setOrientation();
            }
        });
        alert.setNegativeButton(R.string.orientation_portrait, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                PreferenceHelper.setOrientationLandscape(LayerActivity.this, false);
                setOrientation();
            }
        });
        alert.show();
    }

    private boolean setOrientation() {
        if (!PreferenceHelper.isLayerHelpShown(this)) {
            return false;
        }

        if (PreferenceHelper.isLayerOrientationLandscape(this)) {
            if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                return true;
            }
        } else {
            if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                return true;
            }
        }
        return false;
    }

    public void setCurrentLayer(int layerId) {
        this.layerId = layerId;
        doHeaderLayout();
        doItemsLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        doItemsLayout();
    }

    public void doNavigationLayout() {
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        spinnerItems = CursorToObjectHelper.convertCursorToLayers(dbM.layerDbAdapter.fetchAllItems(PrefixHelper.getPrefix(this)));

        ArrayAdapter<HMLayer> mSpinnerAdapter = new ArrayAdapter<HMLayer>(getSupportActionBar().getThemedContext(),
                android.R.layout.simple_spinner_item, android.R.id.text1, spinnerItems);

        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ActionBar.OnNavigationListener mOnNavigationListener = new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int position, long itemId) {
                setCurrentLayer(spinnerItems.get(position).getRowId());
                return true;
            }
        };

        setSpinnerItem(layerId);
        getSupportActionBar().setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
    }

    @SuppressWarnings("deprecation")
    public boolean doHeaderLayout() {
        Bitmap bmpIcon = customImageHelper.getCustomImage(layerId);
        if (bmpIcon != null) {
            BitmapDrawable icon = new BitmapDrawable(getResources(), bmpIcon);
            getSupportActionBar().setIcon(icon);
        } else {
            getSupportActionBar().setIcon(R.mipmap.icon);
        }

        Cursor layerCursor = dbM.layerDbAdapter.fetchItem(layerId);

        String background = layerCursor.getString(layerCursor.getColumnIndex("background"));

        if (background != null) {
            Bitmap bmpBg = null;

            try {
                bmpBg = BitmapUtil.getScaledBitmap(this, new File(background), screenWidth, screenHeight);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();

                System.gc();

                try {
                    bmpBg = BitmapUtil.getScaledBitmap(this, new File(background), screenWidth / 2, screenHeight / 2);
                } catch (OutOfMemoryError e2) {
                    e2.printStackTrace();
                }
            }

            if (bmpBg == null) {
                Toast.makeText(this, getResources().getString(R.string.error_background), Toast.LENGTH_LONG).show();
                if (Build.VERSION.SDK_INT >= 16) {
                    rlContent.setBackground(null);
                } else {
                    rlContent.setBackgroundDrawable(null);
                }
            } else {
                if (Build.VERSION.SDK_INT >= 16) {
                    rlContent.setBackground(new BitmapDrawable(getResources(), bmpBg));
                } else {
                    rlContent.setBackgroundDrawable(new BitmapDrawable(getResources(), bmpBg));
                }
            }
        } else {
            if (Build.VERSION.SDK_INT >= 16) {
                rlContent.setBackground(null);
            } else {
                rlContent.setBackgroundDrawable(null);
            }
        }

        if (layerCursor.getCount() > 0) {
            layerCursor.close();
            return true;
        }
        layerCursor.close();
        return false;
    }

    public void doItemsLayout() {
        rlMain.removeAllViews();

        if (isEditMode) {
            getLayoutInflater().inflate(R.layout.view_overlay, rlMain);
        }

        ListViewGenerator listViewGenerator = new ListViewGenerator(this, this, R.layout.layer_list_item, R.layout.list_item_value, false, true);

        Cursor layerItems = dbM.layerItemDbAdapter.fetchItemsByLayer(layerId);

        while (layerItems.moveToNext()) {
            HMLayerItem layerItem = Util.getLayerItem(dbM, layerItems);

            View view = null;

            if (layerItem.getType() == LAYER_ITEM_TYPE_LAYER) {
                view = getLayoutInflater().inflate(R.layout.layer_item_layer, null);

                if (PreferenceHelper.isMinimalMode(this)) {
                    view.findViewById(R.id.tv_name).setVisibility(View.GONE);
                } else {
                    if (layerItem.getName() != null) {
                        ((TextView) view.findViewById(R.id.tv_name)).setText(layerItem.getName());
                    } else {
                        Cursor c = dbM.layerDbAdapter.fetchItem(layerItem.getIseId());
                        String name;
                        if (c.getCount() > 0) {
                            name = c.getString(c.getColumnIndex("name"));
                        } else {
                            name = "----";
                        }
                        c.close();

                        ((TextView) view.findViewById(R.id.tv_name)).setText(name);
                    }
                }

                if (customImageHelper.setCustomImage(layerItem.getIseId(), (ImageView) view.findViewById(R.id.iv_icon)) == CustomImageHelper.NONE) {
                    ((ImageView) view.findViewById(R.id.iv_icon)).setImageResource(R.drawable.icon_new34);
                }

                //adjustLayerItemSize(view);

                view.setOnClickListener(new OnLayerClickListener());

            } else if (layerItem.getType() == LAYER_ITEM_TYPE_ROOM) {
                Cursor c = dbM.roomsDbAdapter.fetchItem(layerItem.getIseId());
                if (c.getCount() > 0) {

                    HMRoom room = CursorToObjectHelper.convertCursorToRoom(c);

                    view = getLayoutInflater().inflate(R.layout.layer_item_layer, null);

                    if (PreferenceHelper.isMinimalMode(this)) {
                        view.findViewById(R.id.tv_name).setVisibility(View.GONE);
                    } else {
                        if (layerItem.getName() != null) {
                            ((TextView) view.findViewById(R.id.tv_name)).setText(layerItem.getName());
                        } else {
                            ((TextView) view.findViewById(R.id.tv_name)).setText(room.getName());
                        }
                    }

                    if (customImageHelper.setCustomImage(layerItem.getIseId(), (ImageView) view.findViewById(R.id.iv_icon)) == CustomImageHelper.NONE) {
                        ((ImageView) view.findViewById(R.id.iv_icon)).setImageResource(R.drawable.standart_icon3);
                    }

                    view.setOnClickListener(new OnRoomClickListener());
                }
                c.close();
            } else if (layerItem.getType() == LAYER_ITEM_TYPE_CATEGORY) {
                Cursor c = dbM.categoriesDbAdapter.fetchItem(layerItem.getIseId());
                if (c.getCount() > 0) {
                    view = getLayoutInflater().inflate(R.layout.layer_item_layer, null);

                    HMRoom room = CursorToObjectHelper.convertCursorToRoom(c);

                    if (PreferenceHelper.isMinimalMode(this)) {
                        view.findViewById(R.id.tv_name).setVisibility(View.GONE);
                    } else {
                        if (layerItem.getName() != null) {
                            ((TextView) view.findViewById(R.id.tv_name)).setText(layerItem.getName());
                        } else {
                            ((TextView) view.findViewById(R.id.tv_name)).setText(room.getName());
                        }
                    }

                    if (customImageHelper.setCustomImage(layerItem.getIseId(), (ImageView) view.findViewById(R.id.iv_icon)) == CustomImageHelper.NONE) {
                        ((ImageView) view.findViewById(R.id.iv_icon)).setImageResource(R.drawable.standart_icon3);
                    }

                    view.setOnClickListener(new OnRoomClickListener());
                }
                c.close();

            } else if (layerItem.getType() == LAYER_ITEM_TYPE_DEVICE || layerItem.getType() == LAYER_ITEM_TYPE_VARIABLE
                    || layerItem.getType() == LAYER_ITEM_TYPE_SCRIPT) {

                HMObject reference = null;
                switch (layerItem.getType()) {
                    case LAYER_ITEM_TYPE_DEVICE:
                        reference = Util.getHmObject(layerItem.getIseId(), Util.TYPE_CHANNEL, dbM);
                        break;
                    case LAYER_ITEM_TYPE_VARIABLE:
                        reference = Util.getHmObject(layerItem.getIseId(), Util.TYPE_VARIABLE, dbM);
                        break;
                    case LAYER_ITEM_TYPE_SCRIPT:
                        reference = Util.getHmObject(layerItem.getIseId(), Util.TYPE_SCRIPT, dbM);
                        break;
                    default:
                        break;
                }

                if (reference != null) {

                    View itemView = listViewGenerator.getView((HMChannel) reference);

                    if (itemView.getTag() == null) {
                        // no tag, empty view
                        view = null;
                    } else {
                        Util.setTextColor(getResources().getColor(R.color.white), (ViewGroup) itemView);
                        itemView.setBackgroundResource(R.drawable.round_corners_layer_selectable);

                        if (Build.VERSION.SDK_INT >= 16) {
                            itemView.findViewById(R.id.head).setBackground(null);
                        } else {
                            itemView.findViewById(R.id.head).setBackgroundDrawable(null);
                        }
                        HMControllable hmc = (HMControllable) itemView.getTag();
                        hmc.setLayerItemId(layerItem.getRowId());

                        if (PreferenceHelper.isMinimalMode(this)) {
                            itemView.findViewById(R.id.name).setVisibility(View.GONE);
                            itemView.findViewById(R.id.icon).setVisibility(View.GONE);
                        } else {
                            if (layerItem.getName() != null) {
                                ((TextView) itemView.findViewById(R.id.name)).setText(layerItem.getName());
                            }
                        }

                        if (isEditMode) {
                            InterceptViewGroup wrapper = new InterceptViewGroup(this);
                            wrapper.addView(itemView);
                            wrapper.setEditMode(isEditMode);
                            view = wrapper;
                        } else {
                            view = itemView;
                        }

                    }

                }
            } else if (layerItem.getType() == LAYER_ITEM_TYPE_WEBCAM) {
                view = getLayoutInflater().inflate(R.layout.layer_item_layer, null);

                try {
                    WebCam item = HomeDroidApp.db().getWebCamDao().queryForId(layerItem.getIseId());

                    if (item == null) {
                        dbM.layerItemDbAdapter.deleteItem(layerItem.getRowId());
                        continue;
                    }

                    if (PreferenceHelper.isMinimalMode(this)) {
                        view.findViewById(R.id.tv_name).setVisibility(View.GONE);
                    } else {
                        ((TextView) view.findViewById(R.id.tv_name)).setText(item.getName());
                    }

                    if (customImageHelper.setCustomImage(layerItem.getIseId(), (ImageView) view.findViewById(R.id.iv_icon)) == CustomImageHelper.NONE) {
                        ((ImageView) view.findViewById(R.id.iv_icon)).setImageResource(R.drawable.menu_cam);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                view.setOnClickListener(new OnWebCamClickListener());
            }

            if (view != null) {

                view.setOnTouchListener(new ItemTouchListener());

                if (view.getTag() == null) {
                    view.setTag(layerItem);
                }

                registerForContextMenu(view);

                rlMain.addView(view);

                if (layerItem.getPosX() != null && layerItem.getPosY() != null) {
                    int relPosX = Math.min(screenWidth - (view.getWidth()), layerItem.getPosX() * screenWidth / 10000);
                    int relPosY = Math.min(screenHeight - (view.getHeight()), layerItem.getPosY() * screenHeight / 10000);

                    view.setX(relPosX);
                    view.setY(relPosY);
                }
            }
        }
        layerItems.close();
    }

    private void adjustLayerItemSize(View view) {
        TextView tvName = ((TextView) view.findViewById(R.id.tv_name));
        ImageView ivIcon = (ImageView) view.findViewById(R.id.iv_icon);

        float textSize = 13f;

        if (mItemSize == LAYER_ITEM_SIZE_SMALL) {
            textSize = 12f;
        }

        tvName.setTextSize(textSize);
        tvName.setVisibility(mShowText ? View.VISIBLE : View.GONE);
        ivIcon.setVisibility(mShowIcon ? View.VISIBLE : View.GONE);
    }

    class OnWebCamClickListener implements OnClickListener {

        @Override
        public void onClick(View view) {
            HMLayerItem layerItem = (HMLayerItem) view.getTag();
            try {
                WebCam item = HomeDroidApp.db().getWebCamDao().queryForId(layerItem.getIseId());
                WebcamHelper.showWebCam(LayerActivity.this, item, getWindow().getDecorView().getHandler());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    class OnLayerClickListener implements OnClickListener {

        @Override
        public void onClick(View view) {
            HMLayerItem layerItem = (HMLayerItem) view.getTag();
            setCurrentLayer(layerItem.getIseId());
            setSpinnerItem(layerItem.getIseId());
        }
    }

    protected void setSpinnerItem(int layerId) {

        for (int i = 0; i < spinnerItems.size(); i++) {
            if (spinnerItems.get(i).getRowId() == layerId) {
                getSupportActionBar().setSelectedNavigationItem(i);
                break;
            }
        }
    }

    class OnRoomClickListener implements OnClickListener {

        @Override
        public void onClick(View view) {
            HMLayerItem layerItem = (HMLayerItem) view.getTag();

            if (layerItem.getType() == LAYER_ITEM_TYPE_ROOM) {
                Cursor c = dbM.roomsDbAdapter.fetchItem(layerItem.getIseId());
                Intent intent = new Intent(LayerActivity.this, ListDataActivity.class);
                intent.putExtra(ListDataActivity.EXTRA_ID, layerItem.getIseId());
                intent.putExtra(ListDataActivity.EXTRA_NAME, c.getString(c.getColumnIndex("title")));
                intent.putExtra(ListDataActivity.EXTRA_TYPE, ListDataActivity.TYPE_ROOM);
                c.close();
                startActivity(intent);
            } else if (layerItem.getType() == LAYER_ITEM_TYPE_CATEGORY) {
                Cursor c = dbM.categoriesDbAdapter.fetchItem(layerItem.getIseId());
                Intent intent = new Intent(LayerActivity.this, ListDataActivity.class);
                intent.putExtra(ListDataActivity.EXTRA_ID, layerItem.getIseId());
                intent.putExtra(ListDataActivity.EXTRA_NAME, c.getString(c.getColumnIndex("title")));
                intent.putExtra(ListDataActivity.EXTRA_TYPE, ListDataActivity.TYPE_CATEGORY);
                c.close();
                startActivity(intent);
            }
        }
    }

    class ItemTouchListener implements OnTouchListener {

        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (isEditMode) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    ClipData data = ClipData.newPlainText("", "");
                    DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                    view.startDrag(data, shadowBuilder, view, 0);
                    view.setVisibility(View.INVISIBLE);
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    class ItemDragListener implements OnDragListener {

        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign View to ViewGroup
                    View view = (View) event.getLocalState();

                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int maxWidth = size.x - view.getWidth();
                    int maxHeight = size.y - view.getHeight();

                    int posX = Math.max(0, Math.min(maxWidth, (int) (event.getX() - (view.getWidth() / 2))));
                    int posY = Math.max(0, Math.min(maxHeight, (int) (event.getY() - (view.getHeight() / 2))));

                    int relX = posX * 10000 / screenWidth;
                    int relY = posY * 10000 / screenHeight;

                    HMLayerItem layerItem = getLayerItemTag(view);

                    dbM.layerItemDbAdapter.updateItem(layerItem.getRowId(), relX, relY, null);
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    doItemsLayout();
                default:
                    break;
            }
            return true;
        }
    }

    public HMLayerItem getLayerItemTag(View v) {
        if (v.getTag() instanceof HMControllable) {
            HMControllable hmc = (HMControllable) v.getTag();

            return Util.getLayerItem(dbM, dbM.layerItemDbAdapter.fetchItem(hmc.getLayerItemId()));

        } else {
            return (HMLayerItem) v.getTag();
        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        if (isEditMode) {
            menu.clear();
            getMenuInflater().inflate(R.menu.options_layer_edit, menu);
        } else {
            MenuItem toogleModeItem = menu.add("Toogle Creation Mode");
            MenuItemCompat.setShowAsAction(toogleModeItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
            toogleModeItem.setIcon(android.R.drawable.ic_menu_edit);
            toogleModeItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    toggleEditMode();
                    return true;
                }
            });
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_layer:
                showAddItemDialog();
                return true;
            case R.id.toggle_creation:
                toggleEditMode();
                return true;
            case R.id.set_orientation:
                showSetOrientationDialog();
                return true;
            case R.id.set_background:
                if (!PermissionUtil.hasFilePermissions(this)) {
                    PermissionUtil.requestFilePermission(this, REQUEST_FILESYSTEM_BACKGROUND);
                    return true;
                }

                pickBackground();
                return true;
            case R.id.rename_layer:
                showRenameLayerDialog();
                return true;
            case R.id.delete_layer:
                showDeleteLayerDialog();
                return true;
            case R.id.set_default_layer:
                setAsDefaultLayer();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void pickBackground() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, Util.REQUEST_SET_BACKGROUND);
    }

    private void setAsDefaultLayer() {
        PreferenceHelper.setDefaultLayer(this, layerId);
        Toast.makeText(this, R.string.saved, Toast.LENGTH_LONG).show();
    }

    public void showRenameLayerDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(LayerActivity.this);

        final EditText input = new EditText(LayerActivity.this);
        alert.setTitle(R.string.rename);
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                dbM.layerDbAdapter.updateItem(layerId, value, null);
                doHeaderLayout();
                doNavigationLayout();
                doItemsLayout();
            }
        });
        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();
    }

    protected void showDeleteLayerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.yousure).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dbM.layerDbAdapter.deleteItem(layerId);
                dbM.layerItemDbAdapter.deleteItemsByLayer(layerId);

                int defaultLayerId = 1 + PrefixHelper.getFullPrefix(LayerActivity.this);

                if (dbM.layerDbAdapter.isEmpty(PrefixHelper.getPrefix(LayerActivity.this))) {
                    Log.i(this.getClass().getName(), "Layer Db empty, creating base layer");
                    createFirstLayer();
                } else {
                    Cursor c = dbM.layerDbAdapter.fetchItem(defaultLayerId);

                    if (!(c.getCount() > 0)) {
                        createFirstLayer();
                    }

                    c.close();
                }

                layerId = defaultLayerId;
                doHeaderLayout();
                doNavigationLayout();
                doItemsLayout();
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        builder.create().show();
    }

    protected void showAddItemDialog() {

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                ;
                if (which == Dialog.BUTTON_NEUTRAL) {
                    dialog.cancel();
                } else {
                    dialog.dismiss();
                    try {
                        addItem(which);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.new_item);
        builder.setSingleChoiceItems(this.getResources().getStringArray(R.array.layer_items), -1, listener).setCancelable(true)
                .setNegativeButton(this.getString(R.string.cancel), listener);

        builder.create().show();
    }

    public void addItem(int type) throws SQLException {

        // new layer
        if (type == 0) {
            AlertDialog.Builder alert = new AlertDialog.Builder(LayerActivity.this);

            final EditText input = new EditText(LayerActivity.this);
            alert.setTitle(R.string.name);
            alert.setView(input);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @SuppressWarnings("unused")
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = input.getText().toString();

                    int newLayerId = dbM.layerDbAdapter.getHighestRowId(PrefixHelper.getFullPrefix(LayerActivity.this)) + 1;
                    int newLayerId2 = (int) dbM.layerDbAdapter.createItem(newLayerId, value, null);
                    int newItemId = (int) dbM.layerItemDbAdapter.createItem(newLayerId, layerId, 0, null, null);
                    doNavigationLayout();
                    doItemsLayout();
                }
            });
            alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();
            // existing layer
        } else if (type == 1) {
            int index = -1;

            final ArrayList<HMLayer> items = CursorToObjectHelper.convertCursorToLayers(dbM.layerDbAdapter.fetchAllItems(PrefixHelper
                    .getPrefix(this)));

            ArrayList<String> labels = new ArrayList<String>();

            for (HMLayer item : items) {
                labels.add(item.getName());
            }
            CharSequence[] cs = labels.toArray(new CharSequence[labels.size()]);

            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    if (which == Dialog.BUTTON_NEUTRAL || which == Dialog.BUTTON_NEGATIVE) {
                        dialog.cancel();
                    } else {
                        dialog.dismiss();
                        addLayer(which);
                    }
                }

                public void addLayer(int index) {
                    dbM.layerItemDbAdapter.createItem(items.get(index).getRowId(), layerId, LAYER_ITEM_TYPE_LAYER, null, null);
                    doItemsLayout();
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.new_item);
            builder.setSingleChoiceItems(cs, index, listener).setCancelable(true)
                    .setNegativeButton(this.getString(R.string.cancel), listener);

            builder.create().show();
        } else if (type == 2) {
            // room
            int index = -1;

            final ArrayList<HMRoom> items = CursorToObjectHelper.convertCursorToRooms(dbM.roomsDbAdapter.fetchAllItems(PrefixHelper
                    .getPrefix(this)));

            ArrayList<String> labels = new ArrayList<String>();

            for (HMRoom item : items) {
                labels.add(item.getName());
            }
            CharSequence[] cs = labels.toArray(new CharSequence[labels.size()]);

            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    if (which == Dialog.BUTTON_NEUTRAL || which == Dialog.BUTTON_NEGATIVE) {
                        dialog.cancel();
                    } else {
                        dialog.dismiss();
                        addLayer(which);
                    }
                }

                public void addLayer(int index) {
                    dbM.layerItemDbAdapter.createItem(items.get(index).getRowId(), layerId, LAYER_ITEM_TYPE_ROOM, null, null);
                    doItemsLayout();
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.new_item);
            builder.setSingleChoiceItems(cs, index, listener).setCancelable(true)
                    .setNegativeButton(this.getString(R.string.cancel), listener);

            builder.create().show();

        } else if (type == 3) {
            // category
            int index = -1;

            final ArrayList<HMRoom> items = CursorToObjectHelper.convertCursorToRooms(dbM.categoriesDbAdapter.fetchAllItems(PrefixHelper
                    .getPrefix(this)));

            ArrayList<String> labels = new ArrayList<String>();

            for (HMRoom item : items) {
                labels.add(item.getName());
            }
            CharSequence[] cs = labels.toArray(new CharSequence[labels.size()]);

            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    if (which == Dialog.BUTTON_NEUTRAL || which == Dialog.BUTTON_NEGATIVE) {
                        dialog.cancel();
                    } else {
                        dialog.dismiss();
                        addLayer(which);
                    }
                }

                public void addLayer(int index) {
                    dbM.layerItemDbAdapter.createItem(items.get(index).getRowId(), layerId, LAYER_ITEM_TYPE_CATEGORY, null, null);
                    doItemsLayout();
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.new_item);
            builder.setSingleChoiceItems(cs, index, listener).setCancelable(true)
                    .setNegativeButton(this.getString(R.string.cancel), listener);

            builder.create().show();

        } else if (type == 4) {
            Intent intent = new Intent(LayerActivity.this, SelectLayerItemActivity.class);
            intent.putExtra(SelectLayerItemActivity.INTENT_EXTRA_TYPE, Util.TYPE_CHANNEL);
            startActivityForResult(intent, Util.REQUEST_SELECT_ITEM);
        } else if (type == 5) {
            Intent intent = new Intent(LayerActivity.this, SelectLayerItemActivity.class);
            intent.putExtra(SelectLayerItemActivity.INTENT_EXTRA_TYPE, Util.TYPE_VARIABLE);
            startActivityForResult(intent, Util.REQUEST_SELECT_ITEM);
        } else if (type == 6) {
            Intent intent = new Intent(LayerActivity.this, SelectLayerItemActivity.class);
            intent.putExtra(SelectLayerItemActivity.INTENT_EXTRA_TYPE, Util.TYPE_SCRIPT);
            startActivityForResult(intent, Util.REQUEST_SELECT_ITEM);
        } else if (type == 7) {
            int index = -1;

            final List<WebCam> items = dbM.getWebCamDao().queryForAll();

            ArrayList<String> labels = new ArrayList<String>();

            for (WebCam item : items) {
                labels.add(item.getName());
            }
            CharSequence[] cs = labels.toArray(new CharSequence[labels.size()]);

            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    if (which == Dialog.BUTTON_NEUTRAL || which == Dialog.BUTTON_NEGATIVE) {
                        dialog.cancel();
                    } else {
                        dialog.dismiss();
                        addItem(which);
                    }
                }

                public void addItem(int index) {
                    dbM.layerItemDbAdapter.createItem(items.get(index).getId(), layerId, LAYER_ITEM_TYPE_WEBCAM, null, null);
                    doItemsLayout();
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.new_item);
            builder.setSingleChoiceItems(cs, index, listener).setCancelable(true)
                    .setNegativeButton(this.getString(R.string.cancel), listener);

            builder.create().show();
        }
    }

    protected boolean isEditMode;

    public void toggleEditMode() {
        if (isEditMode) {
            isEditMode = false;
            doItemsLayout();
        } else {
            isEditMode = true;
            doItemsLayout();
        }
        invalidateOptionsMenu();
    }

    protected void refreshData() {
        doItemsLayout();
    }

    HMLayerItem menuClickedItem;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.context_layer, menu);

        menuClickedItem = getLayerItemTag(v);

        if (Util.hasIcon(dbM, menuClickedItem.getRowId())) {
            menu.add(0, R.id.remove_icon, 0, R.string.remove_icon);
        }

        menu.add(0, R.id.rename, 0, R.string.rename);
        menu.add(0, R.id.remove_item, 0, R.string.remove);
    }

    int rowIdExternalIcon;

    public boolean onContextItemSelected(android.view.MenuItem item) {

        if (menuClickedItem != null) {

            switch (item.getItemId()) {
                case R.id.change_icon_external:
                    rowIdExternalIcon = menuClickedItem.getIseId();
                    if (!PermissionUtil.hasFilePermissions(this)) {
                        PermissionUtil.requestFilePermission(this, REQUEST_FILESYSTEM_ICON);
                        return true;
                    }
                    pickExternalIcon();
                    return true;
                case R.id.change_icon_internal:
                    Intent i = new Intent(this, GallerySelectIcon.class);
                    i.putExtra("rowId", menuClickedItem.getIseId());
                    startActivityForResult(i, Util.REQUEST_SELECT_ICON_INTERNAL);
                    return true;
                case R.id.remove_icon:
                    Util.removeIcon(dbM, menuClickedItem.getIseId());
                    return true;
                case R.id.remove_item:
                    dbM.layerItemDbAdapter.deleteItem(menuClickedItem.getRowId());
                    doItemsLayout();
                    return true;
                case R.id.rename:
                    showRenameDialog(menuClickedItem);
                default:
                    return true;
            }
        }
        return true;
    }

    private void pickExternalIcon() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, Util.REQUEST_SELECT_ICON_EXTERNAL);
    }

    public void showRenameDialog(final HMLayerItem layerItem) {
        AlertDialog.Builder alert = new AlertDialog.Builder(LayerActivity.this);

        final EditText input = new EditText(LayerActivity.this);

        if (layerItem.getName() != null) {
            input.setText(layerItem.getName());
        }

        alert.setTitle(R.string.rename);
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                dbM.layerItemDbAdapter.updateItem(layerItem.getRowId(), null, null, value);
                doItemsLayout();
            }
        });
        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();
    }

    public void onContextMenuClosed(Menu menu) {
        menuClickedItem = null;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Util.REQUEST_SELECT_ITEM) {
            if (resultCode == Activity.RESULT_OK) {
                int iseId = data.getExtras().getInt(SelectLayerItemActivity.INTENT_EXTRA_ISE_ID);
                int type = data.getExtras().getInt(SelectLayerItemActivity.INTENT_EXTRA_TYPE);

                switch (type) {
                    case Util.TYPE_CHANNEL:
                        dbM.layerItemDbAdapter.createItem(iseId, layerId, LAYER_ITEM_TYPE_DEVICE, null, null);
                        doItemsLayout();
                        break;
                    case Util.TYPE_SCRIPT:
                        dbM.layerItemDbAdapter.createItem(iseId, layerId, LAYER_ITEM_TYPE_SCRIPT, null, null);
                        doItemsLayout();
                        break;
                    case Util.TYPE_VARIABLE:
                        dbM.layerItemDbAdapter.createItem(iseId, layerId, LAYER_ITEM_TYPE_VARIABLE, null, null);
                        doItemsLayout();
                        break;
                }
            }
        } else if (requestCode == Util.REQUEST_SET_BACKGROUND && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String[] projection = {MediaStore.Images.Media.DATA};

            Cursor cursor = this.getContentResolver().query(uri, projection, null, null, null);
            cursor.moveToFirst();

            String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            de.ebertp.HomeDroid.Utils.Util.closeCursor(cursor);
            dbM.layerDbAdapter.updateItem(layerId, null, imagePath);
            doHeaderLayout();
        } else if (requestCode == Util.REQUEST_SELECT_ICON_INTERNAL) {
            if (resultCode == Activity.RESULT_OK) {
                refreshData();
            }
        } else if (requestCode == Util.REQUEST_SELECT_ICON_EXTERNAL) {
            if (resultCode == Activity.RESULT_OK) {
                // External icon change
                Uri uri = data.getData();
                String[] projection = {MediaStore.Images.Media._ID};

                Cursor cursor = this.getContentResolver().query(uri, projection, null, null, null);
                cursor.moveToFirst();
                int imageId = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));

                new ThumbNailGetter().getThumbnail(this, imageId);

                de.ebertp.HomeDroid.Utils.Util.closeCursor(cursor);

                HomeDroidApp.db().iconDbAdapter.deleteItem(imageId);
                HomeDroidApp.db().externalIconDbAdapter.replaceItem(rowIdExternalIcon, imageId);

                refreshData();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void createFirstLayer() {
        dbM.layerDbAdapter.createItem(layerId, getResources().getString(R.string.layer1), null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        if (requestCode == REQUEST_FILESYSTEM_ICON) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickExternalIcon();
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_FILESYSTEM_BACKGROUND) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickBackground();
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
            }
        }
    }

}
