package de.ebertp.HomeDroid.Activities.Listing.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.mobeta.android.dslv.DragSortListView;

import java.util.Comparator;
import java.util.List;

import de.ebertp.HomeDroid.FixedDragSortController;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.NumberAwareStringComparator;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.ViewAdapter.StatusListViewAdapter;
import timber.log.Timber;

public abstract class SortableListDataFragment extends ListDataFragment {

    public static final int GROUP_ID_DEVICE_FAVS = 101;
    public static final int GROUP_ID_SYSVAR = 102;
    public static final int GROUP_ID_SCRIPTS = 103;

    private boolean mIsDragAndDropEnabled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.generic_list, container, false);
        inflater.inflate(R.layout.sortable_devices_list, (ViewGroup) view.findViewById(R.id.ptr_layout), true);
        return view;
    }

    @Override
    protected void initList() {
        super.initList();
        sanitizeSorting();
        initDragAndDropSort();
    }

    void initDragAndDropSort() {
        DragSortListView dragSortListView = (DragSortListView) getView().findViewById(R.id.list_view);

        FixedDragSortController dragSortController = new FixedDragSortController(dragSortListView);
        dragSortController.setDragHandleId(R.id.indicator_drag);

        dragSortListView.setDropListener((DragSortListView.DropListener) listViewAdapter);
        dragSortListView.setFloatViewManager(dragSortController);
        dragSortListView.setOnTouchListener(dragSortController);
        toggleDragAndDropMode(false);
    }

    private void toggleDragAndDropMode(boolean isEnabled) {
        mIsDragAndDropEnabled = isEnabled;
        ptrLayout.setEnabled(!isEnabled);
        ((StatusListViewAdapter) listViewAdapter).setClickable(!isEnabled);

        if (isEnabled) {
            unregisterForContextMenu(listView);
        } else {
            registerForContextMenu(listView);
        }
    }

    void sanitizeSorting() {
        List<HMObject> objects = listViewAdapter.getObjects();

        int countUnsorted = 0;
        for (HMObject hmObject : objects) {
            if (hmObject.getSortOrder() == null) {
                countUnsorted++;
            }
        }

        if (countUnsorted == objects.size()) {
            Timber.d("No custom sorting for this list");
            return;
        }

        Timber.d("Custom order detected");

        if (countUnsorted == 0) {
            Timber.d("Custom order is valid");
            return;
        }

        Timber.d("New entries with custom order, resorting");
        //assume list is ordered by sort order
        for (int i = objects.size() - 1; i >= 0; i--) {
            HMObject hmObject = objects.get(i);
            if (hmObject.getSortOrder() == null) {
                // put new objects to the top of the list
                Timber.d("Moving entry " + i + " to top");
                objects.add(0, objects.remove(i));
            }
        }

        // write new list order to
        for (int i = 0; i < objects.size(); i++) {
            HomeDroidApp.db().groupedItemSettingsDbAdatper.updateOrder(objects.get(i).getRowId(), mRoomId, i);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!PreferenceHelper.isChildProtectionOn(getContext())) {
            getActivity().getMenuInflater().inflate(R.menu.options_sort_favs, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_sort_list) {
            toggleDragAndDropMode(!mIsDragAndDropEnabled);
            listViewAdapter.notifyDataSetChanged();
            item.setIcon(mIsDragAndDropEnabled ? R.drawable.ic_action_menu_sort_off : R.drawable.ic_action_menu_sort);
            return true;
        } else if (item.getItemId() == R.id.menu_sort_reset) {
            showResetSortDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, R.id.hide_object, 0, R.string.hide);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        final HMObject hmObject = getItem(info.position);
        final int rowId = hmObject.getRowId();

        switch (item.getItemId()) {
            case R.id.hide_object: {
                showHideDialog(rowId);
            }
            default:
                return super.onContextItemSelected(item);
        }
    }

    public class SortOrderComparator implements Comparator<HMObject> {
        @Override
        public int compare(HMObject o1, HMObject o2) {
            if (o1.getSortOrder() == null && o2.getSortOrder() == null) {
                int compareResult = NumberAwareStringComparator.INSTANCE.compare(o1.getName(), o2.getName());
                if (compareResult < 0) {
                    return -1;
                } else if (compareResult > 0) {
                    return 1;
                }

                return 0;
            }

            if (o1.getSortOrder() == null) {
                return 1;
            }

            if (o2.getSortOrder() == null) {
                return -1;
            }

            return o1.getSortOrder() - o2.getSortOrder();
        }
    }

    protected void showResetSortDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.yousure).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                HomeDroidApp.db().groupedItemSettingsDbAdatper.deleteSettingsForGroup(mRoomId);
                refreshData();
            }
        }).setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    private void showHideDialog(final int rowId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.yousure).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                HomeDroidApp.db().groupedItemSettingsDbAdatper.updateHiddenState(rowId, mRoomId, 1);
                refreshData();
            }
        }).setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

}
