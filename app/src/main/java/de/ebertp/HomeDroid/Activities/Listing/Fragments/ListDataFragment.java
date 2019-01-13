package de.ebertp.HomeDroid.Activities.Listing.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.ebertp.HomeDroid.Activities.Listing.ListDataActivity;
import de.ebertp.HomeDroid.Communication.RefreshStateHelper;
import de.ebertp.HomeDroid.DbAdapter.BaseDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.BaseRelationsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.FavRelationsDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.DbUtil;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.Model.HMScript;
import de.ebertp.HomeDroid.Model.HMVariable;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.ToastHandler;
import de.ebertp.HomeDroid.Utils.Util;
import de.ebertp.HomeDroid.ViewAdapter.CursorToObjectHelper;
import de.ebertp.HomeDroid.ViewAdapter.HMObjectViewAdapter;
import de.ebertp.HomeDroid.ViewAdapter.StatusListViewAdapter;

public abstract class ListDataFragment extends HMObjectFragment implements SwipeRefreshLayout.OnRefreshListener {

    private FavRelationsDbAdapter mFavsHelper;
    protected HMObjectViewAdapter listViewAdapter;
    protected BaseRelationsDbAdapter mRelationsHelper;

    ListView listView;
    public int mRoomId;
    public String rowId;

    public ToastHandler toastHandler;
    protected SwipeRefreshLayout ptrLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toastHandler = new ToastHandler(getActivity());
        mFavsHelper = dbManager.favRelationsDbAdapter;

        Bundle bun = getArguments();

        if (bun != null) {
            mRoomId = bun.getInt(ListDataActivity.EXTRA_ID);
        } else {
            Log.d(this.getClass().getName(), "Bundle null");
        }
    }

    public ArrayList<? extends HMObject> getSpinnerItems() {
        return null;
    }

    public void doNavigationLayout() {
        final ArrayList<HMObject> spinnerItems = (ArrayList<HMObject>) getSpinnerItems();

        boolean isTabletLandscape = getResources().getBoolean(R.bool.isTabletLand);

        if (!isTabletLandscape && spinnerItems != null && spinnerItems.size() > 1) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
            activity.getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

            ArrayAdapter<HMObject> mSpinnerAdapter = new ArrayAdapter<HMObject>(activity.getSupportActionBar().getThemedContext(),
                    android.R.layout.simple_spinner_item, android.R.id.text1, spinnerItems);

            mSpinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

            ActionBar.OnNavigationListener mOnNavigationListener = new ActionBar.OnNavigationListener() {
                @Override
                public boolean onNavigationItemSelected(int position, long itemId) {
                    mRoomId = spinnerItems.get(position).getRowId();
                    initFragment();
                    return true;
                }
            };

            activity.getSupportActionBar().setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
            setSpinnerItem(spinnerItems, mRoomId);
        } else {
            initFragment();
        }
    }

    protected void setSpinnerItem(ArrayList<HMObject> spinnerItems, int layerId) {

        for (int i = 0; i < spinnerItems.size(); i++) {
            if (spinnerItems.get(i).getRowId() == layerId) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setSelectedNavigationItem(i);
                break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.generic_list, container, false);
        inflater.inflate(R.layout.devices_list, (ViewGroup) view.findViewById(R.id.ptr_layout));
        return view;
    }

    protected void shrinkList(View root) {
        if (getResources().getBoolean(R.bool.isTabletLand)) {
            root.findViewById(R.id.ptr_layout).setLayoutParams(
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.7f));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        doNavigationLayout();
    }

    public void initFragment() {

        if (isAllowed()) {
            if (listViewAdapter == null) {
                addPullToRefreshListener(!PreferenceHelper.isImmersiveModeEnabled(getActivity()));
                initList();
                showEmptyViewIfEmpty();
            } else {
                refreshData();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    protected boolean isAllowed() {
        return !PreferenceHelper.isChildProtectionOn(getActivity());
    }

    protected void initList() {
        initDbHelper();
        Cursor c = fetchRoomItems();
        listViewAdapter = getListViewAdapter(getHMObjects(c));
        ((TextView) getView().findViewById(R.id.emptyView)).setText(getEmptyListViewText());
        listView = (ListView) getView().findViewById(R.id.list_view);
        listView.setAdapter(listViewAdapter);

        registerForContextMenu(listView);
        de.ebertp.HomeDroid.Utils.Util.closeCursor(c);
    }

    protected void addPullToRefreshListener(boolean enabled) {
        ptrLayout = (SwipeRefreshLayout) getView().findViewById(R.id.ptr_layout);
        ptrLayout.setEnabled(enabled);
        ptrLayout.setOnRefreshListener(this);
    }

    protected void refreshBatch(FragmentActivity fragmentActivity, List<HMObject> arrayList, Handler toastHandler) {
        RefreshStateHelper.refreshBatch(fragmentActivity, arrayList, toastHandler);
    }

    @Override
    public void onRefresh() {
        doRefresh();
    }

    protected void doRefresh() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                refreshBatch(getActivity(), listViewAdapter.getObjects(), toastHandler);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                refreshData();
                ptrLayout.setRefreshing(false);
            }
        }.execute();
    }

    public void refreshData() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            if (listViewAdapter != null) {
                listViewAdapter.setObjects(getHMObjects(fetchRoomItems()));
                listViewAdapter.notifyDataSetChanged();
                showEmptyViewIfEmpty();
            }

            if (ptrLayout != null) {
                ptrLayout.setRefreshing(false);
            }
        }
    }

    protected void showEmptyViewIfEmpty() {
        //quickfix for weird error
        if (getView() == null) {
            return;
        }

        if (listViewAdapter.getCount() == 0) {
            getView().findViewById(R.id.list_view).setVisibility(View.GONE);
            getView().findViewById(R.id.emptyView).setVisibility(View.VISIBLE);
        } else {
            getView().findViewById(R.id.emptyView).setVisibility(View.GONE);
            getView().findViewById(R.id.list_view).setVisibility(View.VISIBLE);
        }
    }

    protected Cursor fetchRoomItems() {
        return mRelationsHelper.fetchItemsByGroup(mRoomId);
    }

    public String getEmptyListViewText() {
        return ((TextView) getView().findViewById(R.id.emptyView)).getText().toString();
    }

    public ArrayList<HMObject> getHMObjects(Cursor c) {
        return CursorToObjectHelper.convertCursorToChannels(c);
    }

    public HMObjectViewAdapter getListViewAdapter(ArrayList<HMObject> hmObjects) {
        return new StatusListViewAdapter(getActivity(), getActivity(), hmObjects, mRoomId);
    }

    public abstract void initDbHelper();

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public HMObject getItem(int position) {
        return (HMObject) listViewAdapter.getItem(position);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

        final HMObject hmObject = getItem(info.position);
        final int rowId = hmObject.getRowId();

        switch (item.getItemId()) {
            case R.id.add_favs:

                Cursor c = mFavsHelper.fetchItem(rowId);
                if (c.getCount() == 0) {
                    if (hmObject instanceof HMScript) {
                        mFavsHelper.createItem(rowId, 1 + PreferenceHelper.getPrefix(getActivity()) * BaseDbAdapter.PREFIX_OFFSET);
                    } else if (hmObject instanceof HMVariable) {
                        mFavsHelper.createItem(rowId, 2 + PreferenceHelper.getPrefix(getActivity()) * BaseDbAdapter.PREFIX_OFFSET);
                    } else {
                        mFavsHelper.createItem(rowId, 0 + PreferenceHelper.getPrefix(getActivity()) * BaseDbAdapter.PREFIX_OFFSET);
                    }
                }
                de.ebertp.HomeDroid.Utils.Util.closeCursor(c);
                break;
            case R.id.remove_favs:
                mFavsHelper.deleteItem(rowId);
                refreshData();
                break;
            case R.id.refresh_state:
                Util.addRefreshNotify(info.targetView);
                new Thread(new Runnable() {

                    public void run() {
                        if (hmObject instanceof HMVariable) {
                            RefreshStateHelper.refreshVariable(getActivity(), Integer.toString(rowId), toastHandler);
                        } else {
                            RefreshStateHelper.refreshChannel(getActivity(), Integer.toString(rowId), toastHandler);
                        }

                    }
                }).start();
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        int rowId = getItem(info.position).getRowId();

        Cursor c = mFavsHelper.fetchItem(rowId);

        if (c.getCount() != 0) {
            menu.add(0, R.id.remove_favs, 0, R.string.rem_favs);
        } else {
            menu.add(0, R.id.add_favs, 0, R.string.add_favs);
        }

        menu.add(0, R.id.refresh_state, 0, R.string.refresh_state);

        Long timestamp = DbUtil.getLastTimeStamp(rowId);
        if (timestamp != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM HH:mm", Locale.getDefault());
            String lastChangeFormatted = simpleDateFormat.format(new Date(timestamp * 1000));
            String message = getString(R.string.last_change) + "  " + lastChangeFormatted;
            menu.add(0, R.id.show_timestamp, 1, message);
        }

        de.ebertp.HomeDroid.Utils.Util.closeCursor(c);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Util.REQUEST_REFRESH_NOTIFY) {
            if (resultCode == Activity.RESULT_OK) {
                refreshNotifyHandler.sendMessageDelayed(refreshNotifyHandler.obtainMessage(data.getExtras().getInt("viewId")), 100);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    Handler refreshNotifyHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (getView() != null) {
                Util.addRefreshNotify(getView().findViewById(msg.what));
            }
        }
    };
}
