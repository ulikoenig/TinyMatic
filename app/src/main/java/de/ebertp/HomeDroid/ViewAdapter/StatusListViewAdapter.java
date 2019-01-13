package de.ebertp.HomeDroid.ViewAdapter;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.Nullable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortListView;

import java.util.List;

import de.ebertp.HomeDroid.DbAdapter.DataBaseAdapterManager;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Model.HMChannel;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.Model.WebCam;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.Util;
import de.ebertp.HomeDroid.Utils.WebcamHelper;
import timber.log.Timber;

public class StatusListViewAdapter extends HMObjectViewAdapter implements DragSortListView.DropListener {

    private final boolean isDarkTheme;
    private final Integer mGroupId;
    protected List<HMObject> objects;
    protected DataBaseAdapterManager dbM;

    protected Context ctx;
    private final Activity activity;
    private ListViewGenerator listViewGenerator;
    private boolean mIsClickable = true;

    public StatusListViewAdapter(Context context, Activity activity, List<HMObject> objects) {
        this(context, activity, objects, null);
    }

    public StatusListViewAdapter(Context context, Activity activity, List<HMObject> objects, @Nullable Integer groupId) {
        this.objects = objects;
        this.ctx = context;
        this.activity = activity;
        dbM = HomeDroidApp.db();
        listViewGenerator = getListViewGenerator(true);
        isDarkTheme = PreferenceHelper.isDarkTheme(ctx);
        mGroupId = groupId;
    }

    public void setClickable(boolean isClickable) {
        mIsClickable = isClickable;
        listViewGenerator = getListViewGenerator(isClickable);
    }

    private ListViewGenerator getListViewGenerator(boolean isClickable) {
        return new ListViewGenerator(ctx, activity, R.layout.list_item, R.layout.list_item_value, false, PreferenceHelper.isDarkTheme(ctx), isClickable, true);
    }

    public List<HMObject> getObjects() {
        return objects;
    }

    public void setObjects(List<HMObject> objects) {
        this.objects = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (objects.get(position) instanceof WebCam) {
            final WebCam webcam = (WebCam) objects.get(position);
            convertView = LayoutInflater.from(ctx).inflate(R.layout.list_item, null);

            if (isDarkTheme) {
                convertView.findViewById(R.id.head).setBackgroundResource(R.drawable.bg_card_dark_selektor);
            }

            ListViewGenerator.setIcon(convertView.findViewById(R.id.icon), R.drawable.icon1);
            Util.setIconColor(isDarkTheme, (ImageView) convertView.findViewById(R.id.icon));

            TextView webcamTextView = (TextView) convertView.findViewById(R.id.name);
            webcamTextView.setText(webcam.getName());

            webcamTextView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    WebcamHelper.showCrudDialog(ctx, webcam);
                    return true;
                }
            });

            webcamTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    WebcamHelper.showWebCam(activity, webcam, activity.getWindow().getDecorView().getHandler());
                }
            });
        } else {
            convertView = listViewGenerator.getView((HMChannel) objects.get(position));
        }

        if (mIsClickable) {
            convertView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                }
            });
        }

        View dragIndicator = convertView.findViewById(R.id.indicator_drag);
        if (dragIndicator != null) {
            convertView.findViewById(R.id.indicator_drag).setVisibility(mIsClickable ? View.GONE : View.VISIBLE);
            if (!mIsClickable) {
                Util.setIconColor(PreferenceHelper.isDarkTheme(ctx), (ImageView) convertView.findViewById(R.id.indicator_drag));
            }
        }

        return convertView;
    }

    public int getCount() {
        return objects.size();
    }

    public HMObject getItem(int position) {
        return objects.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void drop(int from, int to) {
        Timber.d("drop() from " + from + " to " + to);
        if (from != to) {
            HMObject data = objects.remove(from);
            objects.add(to, data);

            for (int i = 0; i < objects.size(); i++) {
                HomeDroidApp.db().groupedItemSettingsDbAdatper.updateOrder(objects.get(i).getRowId(), mGroupId, i);
            }

            notifyDataSetChanged();
        }
    }
}