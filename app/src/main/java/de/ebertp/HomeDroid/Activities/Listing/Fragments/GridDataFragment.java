package de.ebertp.HomeDroid.Activities.Listing.Fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import de.ebertp.HomeDroid.Activities.Listing.ListDataActivity;
import de.ebertp.HomeDroid.DbAdapter.BaseCategoryDbAdapter;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.Model.HMRoom;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.ViewAdapter.CursorToObjectHelper;
import de.ebertp.HomeDroid.ViewAdapter.GridViewAdapter;

public abstract class GridDataFragment extends HMObjectFragment {

    protected GridView gridView;
    public GridViewAdapter gridViewAdapter;
    protected BaseCategoryDbAdapter mDbHelper;
    private boolean isLegacyLayout;

    private Integer prefix = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.frame_grid, container, false);

        // Check to see if we have a frame in which to embed the details
        // fragment directly in the containing UI.
        if (view.getChildCount() > 1 && view.getChildAt(1).getVisibility() == View.VISIBLE) {
            isDualPane = true;
            view.getChildAt(1).setId(getDetailsViewId());
        }

        isLegacyLayout = PreferenceHelper.isLegacyLayout(getActivity());

        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
        }

        return view;
    }

    @Override
    public void onPageSelected() {
        if (listFragment != null) {
            listFragment.onPageSelected();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isAllowed() || !PreferenceHelper.isChildProtectionOn(getActivity())) {
            if (gridViewAdapter == null || (prefix != null && prefix != PreferenceHelper.getPrefix(getActivity()))) {
                initGrid();
            } else {
                refreshData();
            }
        }
    }

    protected void initGrid() {
        prefix = PreferenceHelper.getPrefix(getActivity());
        initDbHelper();
        ArrayList<HMRoom> rooms = CursorToObjectHelper.convertCursorToRooms(mDbHelper.fetchAllItems(prefix,
                PreferenceHelper.getOrderBy(getActivity(), mDbHelper.KEY_NAME)));

        gridView = (GridView) getView().findViewById(R.id.rooms_grid);

        if (isLegacyLayout) {
            gridView.setNumColumns(-1);
            if (isDualPane) {
                getView().findViewById(getDetailsViewId()).setVisibility(View.GONE);
            }
        }

        if (isDualPane && !isLegacyLayout && rooms.size() > PreferenceHelper.getGridBreakLimit(getActivity())) {
            gridView.setNumColumns(2);
        }

        gridViewAdapter = new GridViewAdapter(getActivity(), rooms, isDualPane);
        gridView.setAdapter(gridViewAdapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @SuppressWarnings("rawtypes")
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                showDetails(position, v);
            }
        });
        registerForContextMenu(gridView);

        if (isDualPane && !isLegacyLayout) {

            ViewTreeObserver viewTreeObserver = gridView.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        gridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                        if (gridView.getChildCount() > 0 && gridViewAdapter.getCount() > 0) {
                            showDetails(0, gridView.getChildAt(0));
                        }

                    }
                });
            }
        }

    }

    boolean isDualPane = false;
    protected ListDataFragment listFragment;
    int mCurCheckPosition = -1;

    protected void showDetails(int index, View v) {
        mCurCheckPosition = index;

        Intent i = getIntentForList();
        Bundle bun = i.getExtras();
        bun.putInt(ListDataActivity.EXTRA_ID, v.getId());
        bun.putString(ListDataActivity.EXTRA_NAME, (String) v.getTag());

        i.putExtras(bun);

        if (isDualPane && !isLegacyLayout) {
            v.setSelected(true);

            gridViewAdapter.setSelectedId(v.getId());

            listFragment = getListDataFragment(bun);
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right);
            ft.replace(getDetailsViewId(), listFragment);
            ft.commit();

            listFragment.setArguments(bun);
            gridViewAdapter.notifyDataSetChanged();
        } else {
            getActivity().startActivity(i);
            getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curChoice", mCurCheckPosition);
    }

    public void initDbHelper() {

    }

    public abstract ListDataFragment getListDataFragment(Bundle bun);

    public abstract Intent getIntentForList();

    public abstract int getDetailsViewId();

    @Override
    public void refreshData() {
        if (gridViewAdapter != null) {
            gridViewAdapter.setData(CursorToObjectHelper.convertCursorToRooms(mDbHelper.fetchAllItems(prefix,
                    PreferenceHelper.getOrderBy(getActivity(), mDbHelper.KEY_NAME))));
            gridViewAdapter.notifyDataSetChanged();

            if (listFragment != null) {
                listFragment.refreshData();
            }
        }
    }

    protected boolean isAllowed() {
        return false;
    }

    @Override
    public HMObject getItem(int position) {
        return (HMObject) gridViewAdapter.getItem(position);
    }

}
