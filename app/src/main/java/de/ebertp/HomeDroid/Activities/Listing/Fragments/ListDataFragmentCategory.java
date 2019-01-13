package de.ebertp.HomeDroid.Activities.Listing.Fragments;

import android.database.Cursor;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;

import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.SortableRelations;
import de.ebertp.HomeDroid.EventTracker;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.ViewAdapter.CursorToObjectHelper;
import de.ebertp.HomeDroid.ViewAdapter.HMObjectViewAdapter;
import de.ebertp.HomeDroid.ViewAdapter.StatusListViewAdapter;


public class ListDataFragmentCategory extends SortableListDataFragment {

    @Override
    public void onResume() {
        super.onResume();
        EventTracker.trackScreen(getActivity(), "Category");
    }

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
