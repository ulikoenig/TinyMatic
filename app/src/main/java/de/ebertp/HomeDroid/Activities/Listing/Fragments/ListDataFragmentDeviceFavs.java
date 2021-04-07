package de.ebertp.HomeDroid.Activities.Listing.Fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.ebertp.HomeDroid.DbAdapter.BaseDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.FavRelationsDbAdapter;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.PrefixHelper;
import de.ebertp.HomeDroid.ViewAdapter.CursorToObjectHelper;

public class ListDataFragmentDeviceFavs extends SortableListDataFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRoomId = PrefixHelper.getFullPrefix(getContext()) + SortableListDataFragment.GROUP_ID_DEVICE_FAVS;
    }

    public void initDbHelper() {
        mRelationsHelper = dbManager.favRelationsDbAdapter;
    }

    @Override
    protected boolean isAllowed() {
        return true;
    }

    public static ListDataFragment newInstance(Bundle bun) {
        ListDataFragmentDeviceFavs f = new ListDataFragmentDeviceFavs();
        f.setArguments(bun);
        return f;
    }

    @Override
    protected void initList() {
        initDbHelper();

        Cursor favChanC = mRelationsHelper.fetchItemsByGroup(FavRelationsDbAdapter.CHANNELS_ID + PreferenceHelper.getPrefix(getActivity()) * BaseDbAdapter.PREFIX_OFFSET);
        Cursor favScrC = mRelationsHelper.fetchItemsByGroup(FavRelationsDbAdapter.PROGRAMS_ID + PreferenceHelper.getPrefix(getActivity()) * BaseDbAdapter.PREFIX_OFFSET);
        Cursor favVarC = mRelationsHelper.fetchItemsByGroup(FavRelationsDbAdapter.SYSTEM_VARIABLES_ID + PreferenceHelper.getPrefix(getActivity()) * BaseDbAdapter.PREFIX_OFFSET);

        List<HMObject> webCamList = ((FavRelationsDbAdapter) mRelationsHelper).getDaoObjects(FavRelationsDbAdapter.WEBCAMS_ID + PreferenceHelper.getPrefix(getActivity()) * BaseDbAdapter.PREFIX_OFFSET);

        listViewAdapter = getListViewAdapter(getHMObjects(favChanC, favScrC, favVarC, webCamList));
        listView = (ListView) getView().findViewById(R.id.list_view);
        listView.setAdapter(listViewAdapter);

        ((TextView) getView().findViewById(R.id.emptyView)).setText(getEmptyListViewText());

        de.ebertp.HomeDroid.Utils.Util.closeCursor(favChanC);
        de.ebertp.HomeDroid.Utils.Util.closeCursor(favScrC);
        de.ebertp.HomeDroid.Utils.Util.closeCursor(favVarC);

        sanitizeSorting();
        initDragAndDropSort();
    }

    public ArrayList<HMObject> getHMObjects(Cursor favChanC, Cursor favScrC, Cursor favVarC, List<HMObject> favWebcamC) {
        ArrayList<HMObject> hmObjects = CursorToObjectHelper.mergeChanAndScriptFavs(favChanC, favScrC, favVarC);
        hmObjects.addAll(favWebcamC);
        Collections.sort(hmObjects, new SortOrderComparator());
        return hmObjects;
    }

    @Override
    public void refreshData() {
        if (listViewAdapter != null) {

            Cursor favChanC = mRelationsHelper
                    .fetchItemsByGroup(PreferenceHelper.getPrefix(getContext()) * BaseDbAdapter.PREFIX_OFFSET);
            Cursor favScrC = mRelationsHelper.fetchItemsByGroup(1 + PreferenceHelper.getPrefix(getContext()) * BaseDbAdapter.PREFIX_OFFSET);
            Cursor favVarC = mRelationsHelper.fetchItemsByGroup(2 + PreferenceHelper.getPrefix(getContext()) * BaseDbAdapter.PREFIX_OFFSET);
            List<HMObject> webCamList = ((FavRelationsDbAdapter) mRelationsHelper).getDaoObjects(FavRelationsDbAdapter.WEBCAMS_ID + PreferenceHelper.getPrefix(getActivity()) * BaseDbAdapter.PREFIX_OFFSET);

            listViewAdapter.setObjects(getHMObjects(favChanC, favScrC, favVarC, webCamList));
            listViewAdapter.notifyDataSetChanged();

            de.ebertp.HomeDroid.Utils.Util.closeCursor(favChanC);
            de.ebertp.HomeDroid.Utils.Util.closeCursor(favScrC);
            de.ebertp.HomeDroid.Utils.Util.closeCursor(favVarC);
        }
    }

    @Override
    public String getEmptyListViewText() {
        return this.getResources().getString(R.string.nofavs);
    }

    public String getTitle() {
        return getResources().getString(R.string.quick_access);
    }

}
