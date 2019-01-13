package de.ebertp.HomeDroid.Activities.Listing.Fragments;

import android.content.Intent;
import android.os.Bundle;

import de.ebertp.HomeDroid.Activities.Listing.ListDataActivity;
import de.ebertp.HomeDroid.EventTracker;
import de.ebertp.HomeDroid.R;

public class GridDataFragmentRooms extends GridDataFragment {

    @Override
    public void onResume() {
        super.onResume();
        EventTracker.trackScreen(getActivity(), "Room_Grid");
    }

    @Override
    public void initDbHelper() {
        mDbHelper = dbManager.roomsDbAdapter;
    }

    @Override
    public Intent getIntentForList() {
        Intent intent = new Intent(getActivity(), ListDataActivity.class);
        intent.putExtra(ListDataActivity.EXTRA_TYPE, ListDataActivity.TYPE_ROOM);
        return intent;
    }

    @Override
    public ListDataFragment getListDataFragment(Bundle bun) {
        return ListDataFragmentRoom.newInstance(bun);
    }

    @Override
    public int getDetailsViewId() {
        return R.id.details_rooms;
    }


    public String getTitle() {
        return getResources().getString(R.string.rooms);
    }

}
