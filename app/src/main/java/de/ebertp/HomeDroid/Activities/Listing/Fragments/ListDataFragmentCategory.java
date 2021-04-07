package de.ebertp.HomeDroid.Activities.Listing.Fragments;

import android.database.Cursor;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;

import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.ViewAdapter.CursorToObjectHelper;


public class ListDataFragmentCategory extends SortableListDataFragment {

    public static ListDataFragmentCategory newInstance(Bundle bun) {
        ListDataFragmentCategory f = new ListDataFragmentCategory();
        f.setArguments(bun);
        return f;
    }

    @Override
    public ArrayList<HMObject> getHMObjects(Cursor c) {
        ArrayList<HMObject> hmObjects = CursorToObjectHelper.convertCursorToChannels(c);
        Collections.sort(hmObjects, new SortOrderComparator());
        return hmObjects;
    }

    @Override
    public java.util.ArrayList<? extends de.ebertp.HomeDroid.Model.HMObject> getSpinnerItems() {
        return CursorToObjectHelper.convertCursorToRooms(dbManager.categoriesDbAdapter.fetchAllItems(PreferenceHelper.getPrefix(getActivity()),
                PreferenceHelper.getOrderBy(getActivity(), dbManager.categoriesDbAdapter.KEY_NAME)));
    }

    public void initDbHelper() {
        mRelationsHelper = dbManager.catRelationsDbAdapter;
    }

    public String getTitle() {
        return getResources().getString(R.string.categories);
    }

}
