package de.ebertp.HomeDroid.Activities.Listing.Fragments;


import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.software.shell.fab.ActionButton;

import java.sql.SQLException;
import java.util.List;

import de.ebertp.HomeDroid.EventTracker;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Model.WebCam;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.WebcamHelper;

public class WebCamFragment extends DataFragment {

    private ArrayAdapter<WebCam> mListAdapter;
    private List<WebCam> mWebCamList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.fragment_webcams, container, false);

        ListView listView = (ListView) content.findViewById(R.id.list_view);
        ActionButton actionButton = (ActionButton) content.findViewById(R.id.action_button);

        actionButton.setButtonColor(getResources().getColor(R.color.fab_material_lime_500));
        actionButton.setImageDrawable(getResources().getDrawable(R.drawable.fab_plus_icon));
        actionButton.setButtonColorPressed(getResources().getColor(R.color.fab_material_lime_900));
        actionButton.removeShadow();
        actionButton.setRippleEffectEnabled(true);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebcamHelper.showCrudDialog(getActivity(), null);
            }
        });

        try {
            mWebCamList = getItems();

            mListAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,
                    android.R.id.text1, mWebCamList);
            // Assign adapter to ListView
            listView.setAdapter(mListAdapter);

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    WebcamHelper.showCrudDialog(getActivity(), mListAdapter.getItem(position));
                    return true;
                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    WebcamHelper.showWebCam(getContext(), mListAdapter.getItem(position), getActivity().getWindow().getDecorView().getHandler());
                }
            });

            registerForContextMenu(listView);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return content;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventTracker.trackScreen(getActivity(), "Webcams");
    }

    public List<WebCam> getItems() throws SQLException {
        return HomeDroidApp.db().getWebCamDao().queryForAll();
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.clear();
        menu.add(R.string.delete);
    }

    @Override
    public String getTitle() {
        return getResources().getString(R.string.webcams);
    }

    @Override
    public void refreshData() {
        if (getView() != null) {
            try {
                mWebCamList.clear();
                mWebCamList.addAll(getItems());
                mListAdapter.notifyDataSetChanged();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}
