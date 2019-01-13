package de.ebertp.HomeDroid.Activities.Listing.Fragments;

import android.os.Bundle;

import de.ebertp.HomeDroid.Activities.Drawer.ContentFragment;
import de.ebertp.HomeDroid.DbAdapter.DataBaseAdapterManager;
import de.ebertp.HomeDroid.HomeDroidApp;

public abstract class DataFragment extends ContentFragment {

    public abstract void refreshData();

    protected DataBaseAdapterManager dbManager;

    public void onPageSelected() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbManager = HomeDroidApp.db();
    }
}
