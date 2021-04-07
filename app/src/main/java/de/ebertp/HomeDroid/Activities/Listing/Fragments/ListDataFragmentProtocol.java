package de.ebertp.HomeDroid.Activities.Listing.Fragments;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;

import androidx.fragment.app.FragmentActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.ebertp.HomeDroid.Communication.Control.ControlHelper;
import de.ebertp.HomeDroid.Communication.RefreshStateHelper;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.ViewAdapter.CursorToObjectHelper;
import de.ebertp.HomeDroid.ViewAdapter.HMObjectViewAdapter;
import de.ebertp.HomeDroid.ViewAdapter.ProtocolListViewAdapter;

public class ListDataFragmentProtocol extends ListDataFragment {

    private boolean isFirstResume = true;
    private ProgressDialog pd;

    @Override
    public void onResume() {
        super.onResume();
        if (isFirstResume) {
            isFirstResume = false;
            doRefreshWithProgress();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.protocol_list, container, false);

        shrinkList(view);

        if (PreferenceHelper.isDarkTheme(getActivity())) {
            view.findViewById(R.id.btn_clear).setBackgroundResource(R.drawable.bg_card_dark_selektor);
            view.findViewById(R.id.btn_refresh).setBackgroundResource(R.drawable.bg_card_dark_selektor);
        }

        view.findViewById(R.id.btn_clear).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                ControlHelper.clearProtocol(getActivity());
            }
        });

        view.findViewById(R.id.btn_refresh).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                doRefreshWithProgress();
            }
        });

        pd = new ProgressDialog(getContext());
        pd.setMessage(getString(R.string.db_refresh));

        return view;
    }

    private void doRefreshWithProgress()  {
        pd.show();
        doRefresh();
    }

    @Override
    public void refreshData() {
        super.refreshData();
        pd.hide();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        //no context menu
    }

    public void initDbHelper() {
        mRelationsHelper = dbManager.protocolDbAdapter;
    }

    public HMObjectViewAdapter getListViewAdapter(ArrayList<HMObject> hmObjects) {
        return new ProtocolListViewAdapter(getActivity(), hmObjects);
    }

    @Override
    protected Cursor fetchRoomItems() {
        return mRelationsHelper.fetchAllItems(PreferenceHelper.getPrefix(getActivity()), null);
    }

    @Override
    protected void refreshBatch(FragmentActivity fragmentActivity, List<HMObject> arrayList, Handler toastHandler) {
        RefreshStateHelper.refreshProtocol(fragmentActivity, toastHandler);
    }

    @Override
    public ArrayList<HMObject> getHMObjects(Cursor c) {
        return CursorToObjectHelper.convertCursorToProtocol(c);
    }

    public String getTitle() {
        return getResources().getString(R.string.protocol);
    }

}
