package de.ebertp.HomeDroid.Activities.Listing.Fragments;

import android.widget.ListView;
import android.widget.TextView;

import com.j256.ormlite.dao.GenericRawResults;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.ebertp.HomeDroid.EventTracker;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Model.HMCommand;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.ViewAdapter.StatusListViewAdapter;

public class RecentsFragment extends ListDataFragment {

    @Override
    public void onResume() {
        super.onResume();
        EventTracker.trackScreen(getActivity(), "Recents");
    }

    @Override
    protected void initList() {
        initDbHelper();

        listViewAdapter = new StatusListViewAdapter(getActivity(), getActivity(), getItems());

        listView = (ListView) getView().findViewById(R.id.list_view);
        ((TextView) getView().findViewById(R.id.emptyView)).setText(getEmptyListViewText());
        listView.setAdapter(listViewAdapter);

        showEmptyViewIfEmpty();

        registerForContextMenu(listView);
    }

    public List<HMObject> getItems() {
        List<HMObject> hmObjects = new ArrayList<>();
        try {
            boolean detectUnrenamedChannels = PreferenceHelper.isDetectUnrenamedChannels(HomeDroidApp.getContext());

            GenericRawResults<String[]> commandInfo = HMCommand.getMostUsedForCurrentPrefix(getActivity());
            for (String[] rowInfo : commandInfo) {
                HMObject hmObject = HMCommand.getHMObject(getActivity(), Integer.parseInt(rowInfo[0]), Integer.parseInt(rowInfo[1]), detectUnrenamedChannels);
                if (hmObject != null) {
                    hmObjects.add(hmObject);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return hmObjects;
    }

    @Override
    public void refreshData() {
        if (listViewAdapter != null) {
            listViewAdapter.setObjects(getItems());
            listViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void initDbHelper() {
        //not required
    }

    @Override
    public String getTitle() {
        return getString(R.string.recents);
    }
}
