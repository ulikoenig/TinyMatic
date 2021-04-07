package de.ebertp.HomeDroid.Activities.Listing.Fragments;

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
import de.ebertp.HomeDroid.ViewAdapter.NotificationListViewAdapter;

public class ListDataFragmentNotifications extends ListDataFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notifications_list, container, false);

        shrinkList(view);

        if (PreferenceHelper.isDarkTheme(getActivity())) {
            view.findViewById(R.id.btn_clear).setBackgroundResource(R.drawable.bg_card_dark_selektor);
        }

        view.findViewById(R.id.btn_clear).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                ControlHelper.clearNotifications(getActivity());
            }
        });

        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        //no context menu
    }

    public void initDbHelper() {
        mRelationsHelper = dbManager.notificationDbAdapter;
    }

    public HMObjectViewAdapter getListViewAdapter(ArrayList<HMObject> hmObjects) {
        return new NotificationListViewAdapter(getActivity(), getActivity(), hmObjects);
    }

    @Override
    protected Cursor fetchRoomItems() {
        return mRelationsHelper.fetchAllItems(PreferenceHelper.getPrefix(getActivity()), null);
    }

    @Override
    protected void refreshBatch(FragmentActivity fragmentActivity, List<HMObject> arrayList, Handler toastHandler) {
        RefreshStateHelper.refreshNotifications(fragmentActivity, toastHandler);
    }

    @Override
    public ArrayList<HMObject> getHMObjects(Cursor c) {
        return CursorToObjectHelper.convertCursorToNotifications(c);
    }

    public String getTitle() {
        return getResources().getString(R.string.notifications);
    }

}
