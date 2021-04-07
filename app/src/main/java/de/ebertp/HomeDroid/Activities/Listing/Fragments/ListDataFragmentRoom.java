package de.ebertp.HomeDroid.Activities.Listing.Fragments;

import android.database.Cursor;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;

import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.ViewAdapter.CursorToObjectHelper;

public class ListDataFragmentRoom extends SortableListDataFragment {

    public static ListDataFragmentRoom newInstance(Bundle bun) {
        ListDataFragmentRoom f = new ListDataFragmentRoom();
        f.setArguments(bun);
        return f;
    }

    @Override
    public java.util.ArrayList<? extends de.ebertp.HomeDroid.Model.HMObject> getSpinnerItems() {
        return CursorToObjectHelper.convertCursorToRooms(dbManager.roomsDbAdapter.fetchAllItems(PreferenceHelper.getPrefix(getActivity()),
                PreferenceHelper.getOrderBy(getActivity(), dbManager.roomsDbAdapter.KEY_NAME)));
    }

    @Override
    public ArrayList<HMObject> getHMObjects(Cursor c) {
        ArrayList<HMObject> hmObjects = CursorToObjectHelper.convertCursorToChannels(c);
        Collections.sort(hmObjects, new SortOrderComparator());
        return hmObjects;
    }

    @Override
    public void initDbHelper() {
        mRelationsHelper = dbManager.roomRelationsDbAdapter;
    }

    public String getTitle() {
        return getResources().getString(R.string.rooms);
    }

}
