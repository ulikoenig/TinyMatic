package de.ebertp.HomeDroid.Activities.Listing.Fragments;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import de.ebertp.HomeDroid.R;

public class ListDataFragmentRemote extends ListDataFragment {

    public void initDbHelper() {
        mRelationsHelper = dbManager.remoteRelationsDbAdapter;
    }

    @Override
    protected void addPullToRefreshListener(boolean isEnabled) {
        ptrLayout = (SwipeRefreshLayout) getView().findViewById(R.id.ptr_layout);
        ptrLayout.setEnabled(false);
    }

    public String getTitle() {
        return getResources().getString(R.string.virtual_remote);
    }

}
