package de.ebertp.HomeDroid.Utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;

import de.ebertp.HomeDroid.DbAdapter.DataBaseAdapterManager;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Utils.Compatibility.ThumbNailGetter;
import de.ebertp.HomeDroid.ViewAdapter.ImageAdapter;

public class CustomImageHelper {

    public static final int NONE = 0;
    public static final int INTERNAL = 1;
    public static final int EXTERNAL = 2;

    protected DataBaseAdapterManager dbM;
    protected ImageAdapter mImageAdapter;
    protected Context ctx;

    public CustomImageHelper(Context ctx) {
        dbM = HomeDroidApp.db();
        mImageAdapter = new ImageAdapter(ctx);
        this.ctx = ctx;
    }

    public Bitmap getCustomImage(int rowId) {
        Cursor ice = dbM.externalIconDbAdapter.fetchItem(rowId);
        if (ice.getCount() != 0) {
            if (ice.moveToFirst()) {

                try {
                    int imageId = ice.getInt(ice.getColumnIndex("icon"));

                    return ThumbNailGetter.getThumbnail(ctx, imageId);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    de.ebertp.HomeDroid.Utils.Util.closeCursor(ice);
                }

            }
        } else {
            Cursor ici = dbM.iconDbAdapter.fetchItem(rowId);
            if (ici.getCount() != 0) {
                if (ici.moveToFirst()) {
                    try {
                        int imageId = mImageAdapter.getIconId(ici.getInt(ici.getColumnIndex("icon")));
                        return BitmapFactory.decodeResource(ctx.getResources(), imageId);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.v("HomeDroid", "Found old Style Custom Icon, deleting Table");
                        dbM.deletePrefixFromTable(dbM.iconDbAdapter.getTableName(), PreferenceHelper.getPrefix(ctx));
                    }
                }
            }
            de.ebertp.HomeDroid.Utils.Util.closeCursor(ici);
        }
        de.ebertp.HomeDroid.Utils.Util.closeCursor(ice);
        return null;
    }

    public int setCustomImage(int rowId, ImageView iv) {
        Cursor iconExternalCursor = dbM.externalIconDbAdapter.fetchItem(rowId);
        if (iconExternalCursor.getCount() != 0) {
            if (iconExternalCursor.moveToFirst()) {

                Bitmap thumb;

                try {
                    int imageId = iconExternalCursor.getInt(iconExternalCursor.getColumnIndex("icon"));
                    thumb = ThumbNailGetter.getThumbnail(ctx, imageId);
                    iv.setImageBitmap(thumb);

                    return EXTERNAL;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    de.ebertp.HomeDroid.Utils.Util.closeCursor(iconExternalCursor);
                }

            }
        } else {
            Cursor iconInternalCursor = dbM.iconDbAdapter.fetchItem(rowId);
            if (iconInternalCursor.getCount() != 0) {
                if (iconInternalCursor.moveToFirst()) {
                    try {
                        int imageId = mImageAdapter.getIconId(iconInternalCursor.getInt(iconInternalCursor.getColumnIndex("icon")));
                        iv.setImageResource(imageId);
                        return INTERNAL;
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.v("HomeDroid", "Found old Style Custom Icon, deleting Table");
                        dbM.deletePrefixFromTable(dbM.iconDbAdapter.getTableName(), PreferenceHelper.getPrefix(ctx));
                    }
                }
            }
            de.ebertp.HomeDroid.Utils.Util.closeCursor(iconInternalCursor);
        }
        de.ebertp.HomeDroid.Utils.Util.closeCursor(iconExternalCursor);
        return NONE;
    }

    public int setCustomImage(int rowId, RemoteViews remoteViews, int viewId) {
        Cursor iconExternalCursor = dbM.externalIconDbAdapter.fetchItem(rowId);
        if (iconExternalCursor.getCount() != 0) {
            if (iconExternalCursor.moveToFirst()) {

                Bitmap thumb;

                try {
                    int imageId = iconExternalCursor.getInt(iconExternalCursor.getColumnIndex("icon"));
                    thumb = ThumbNailGetter.getThumbnail(ctx, imageId);
                    remoteViews.setImageViewBitmap(viewId, thumb);

                    return EXTERNAL;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    de.ebertp.HomeDroid.Utils.Util.closeCursor(iconExternalCursor);
                }

            }
        } else {
            Cursor iconInternalCursor = dbM.iconDbAdapter.fetchItem(rowId);
            if (iconInternalCursor.getCount() != 0) {
                if (iconInternalCursor.moveToFirst()) {
                    try {
                        int imageId = mImageAdapter.getIconId(iconInternalCursor.getInt(iconInternalCursor.getColumnIndex("icon")));
                        remoteViews.setImageViewResource(viewId, imageId);
                        return INTERNAL;
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.v("HomeDroid", "Found old Style Custom Icon, deleting Table");
                        dbM.deletePrefixFromTable(dbM.iconDbAdapter.getTableName(), PreferenceHelper.getPrefix(ctx));
                    }
                }
            }
            de.ebertp.HomeDroid.Utils.Util.closeCursor(iconInternalCursor);
        }
        de.ebertp.HomeDroid.Utils.Util.closeCursor(iconExternalCursor);
        return NONE;
    }
}