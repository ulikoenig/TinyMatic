package de.ebertp.HomeDroid.Activities.Listing.Fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.ebertp.HomeDroid.Communication.RefreshStateHelper;
import de.ebertp.HomeDroid.EventTracker;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PrefixHelper;
import de.ebertp.HomeDroid.ViewAdapter.CursorToObjectHelper;

public class ListDataFragmentVar extends SortableListDataFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRoomId = PrefixHelper.getFullPrefix(getContext()) + SortableListDataFragment.GROUP_ID_SYSVAR;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        shrinkList(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventTracker.trackScreen(getActivity(), "Variables");
    }

    public void initDbHelper() {
        mRelationsHelper = dbManager.varsDbAdapter;
    }

    @Override
    public ArrayList<HMObject> getHMObjects(Cursor c) {
        ArrayList<HMObject> hmObjects = CursorToObjectHelper.convertCursorToVariables(c);
        Collections.sort(hmObjects, new SortOrderComparator());
        return hmObjects;
    }

    public String getTitle() {
        return getResources().getString(R.string.variables);
    }

    protected void refreshBatch(FragmentActivity fragmentActivity, List<HMObject> arrayList, Handler toastHandler) {
        RefreshStateHelper.refreshVariables(fragmentActivity, toastHandler);
    }
}
