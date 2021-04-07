package de.ebertp.HomeDroid.Activities.Listing.Fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;

import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.CCUFavRelationsDbAdapter;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.ViewAdapter.CursorToObjectHelper;

public class ListDataFragmentCCUFavs extends SortableListDataFragment {

    public void initDbHelper() {
        mRelationsHelper = dbManager.ccuFavRelationsDbAdapter;
    }

    @Override
    protected boolean isAllowed() {
        return true;
    }

    public static ListDataFragment newInstance(Bundle bun) {
        ListDataFragmentCCUFavs f = new ListDataFragmentCCUFavs();
        f.setArguments(bun);
        return f;
    }

    @Override
    public java.util.ArrayList<? extends de.ebertp.HomeDroid.Model.HMObject> getSpinnerItems() {
        return CursorToObjectHelper.convertCursorToRooms(dbManager.ccuFavListsAdapter.fetchAllItems(PreferenceHelper.getPrefix(getActivity()),
                PreferenceHelper.getOrderBy(getActivity(), dbManager.ccuFavListsAdapter.KEY_NAME)));
    }

    @Override
    protected void initList() {
        initDbHelper();

        Cursor favChanC = ((CCUFavRelationsDbAdapter) mRelationsHelper).fetchItemsByRoomAndType(mRoomId,
                CCUFavRelationsDbAdapter.TYPE_CHANNEL);
        Cursor favScrC = ((CCUFavRelationsDbAdapter) mRelationsHelper).fetchItemsByRoomAndType(mRoomId,
                CCUFavRelationsDbAdapter.TYPE_PROGRAM);
        Cursor favVarC = ((CCUFavRelationsDbAdapter) mRelationsHelper).fetchItemsByRoomAndType(mRoomId,
                CCUFavRelationsDbAdapter.TYPE_SYSVAR);

        listViewAdapter = getListViewAdapter(getHMObjects(favChanC, favScrC, favVarC));
        listView = (ListView) getView().findViewById(R.id.list_view);
        listView.setAdapter(listViewAdapter);

        registerForContextMenu(listView);

        de.ebertp.HomeDroid.Utils.Util.closeCursor(favChanC);
        de.ebertp.HomeDroid.Utils.Util.closeCursor(favScrC);
        de.ebertp.HomeDroid.Utils.Util.closeCursor(favVarC);

        sanitizeSorting();
        initDragAndDropSort();
    }

    public ArrayList<HMObject> getHMObjects(Cursor favChanC, Cursor favScrC, Cursor favVarC) {
        ArrayList<HMObject> hmObjects = CursorToObjectHelper.mergeChanAndScriptFavs(favChanC, favScrC, favVarC);
        Collections.sort(hmObjects, new SortOrderComparator());
        return hmObjects;
    }

    @Override
    public void refreshData() {

        if (listViewAdapter != null) {

            Cursor favChanC = ((CCUFavRelationsDbAdapter) mRelationsHelper).fetchItemsByRoomAndType(mRoomId,
                    CCUFavRelationsDbAdapter.TYPE_CHANNEL);
            Cursor favScrC = ((CCUFavRelationsDbAdapter) mRelationsHelper).fetchItemsByRoomAndType(mRoomId,
                    CCUFavRelationsDbAdapter.TYPE_PROGRAM);
            Cursor favVarC = ((CCUFavRelationsDbAdapter) mRelationsHelper).fetchItemsByRoomAndType(mRoomId,
                    CCUFavRelationsDbAdapter.TYPE_SYSVAR);

            listViewAdapter.setObjects(getHMObjects(favChanC, favScrC, favVarC));
            listViewAdapter.notifyDataSetChanged();

            de.ebertp.HomeDroid.Utils.Util.closeCursor(favChanC);
            de.ebertp.HomeDroid.Utils.Util.closeCursor(favScrC);
            de.ebertp.HomeDroid.Utils.Util.closeCursor(favVarC);

            showEmptyViewIfEmpty();
        }
    }

    public String getTitle() {
        return getResources().getString(R.string.favs);
    }

}
