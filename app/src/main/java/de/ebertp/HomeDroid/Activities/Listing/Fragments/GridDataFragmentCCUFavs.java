package de.ebertp.HomeDroid.Activities.Listing.Fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;
import android.view.View;

import de.ebertp.HomeDroid.Activities.Listing.ListDataActivity;
import de.ebertp.HomeDroid.DbAdapter.BaseDbAdapter;
import de.ebertp.HomeDroid.EventTracker;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;

public class GridDataFragmentCCUFavs extends GridDataFragment {

    @Override
    public void onResume() {
        super.onResume();
        EventTracker.trackScreen(getActivity(), "CCU_Favorite_Grid");
    }

    @Override
    public Intent getIntentForList() {
        Intent intent = new Intent(getActivity(), ListDataActivity.class);
        intent.putExtra(ListDataActivity.EXTRA_TYPE, ListDataActivity.TYPE_CCU_FAV);
        return intent;
    }

    @Override
    public int getDetailsViewId() {
        return R.id.details_favs;
    }

    @Override
    protected boolean isAllowed() {
        return true;
    }

    @Override
    public void initDbHelper() {
        // Log.v("TabView","Loading Rooms");
        mDbHelper = dbManager.ccuFavListsAdapter;
    }

    @Override
    public ListDataFragment getListDataFragment(Bundle bun) {
        return ListDataFragmentCCUFavs.newInstance(bun);
    }

    public String getTitle() {
        return getResources().getString(R.string.favs);
    }

    @Override
    protected void showDetails(int index, View v) {
        mCurCheckPosition = index;

        Bundle bun = new Bundle();
        bun.putInt(ListDataActivity.EXTRA_ID, v.getId());
        bun.putString(ListDataActivity.EXTRA_NAME, (String) v.getTag());

        Intent i;
        if (v.getId() == 1 + PreferenceHelper.getPrefix(getActivity()) * BaseDbAdapter.PREFIX_OFFSET) {
            i = new Intent(getActivity(), ListDataActivity.class);
            bun.putString(ListDataActivity.EXTRA_TYPE, ListDataActivity.TYPE_DEVICE_FAV);
        } else {
            i = getIntentForList();
        }

        i.putExtras(bun);

        if (isDualPane && !PreferenceHelper.isLegacyLayout(getActivity())) {
            v.setSelected(true);

            gridViewAdapter.setSelectedId(v.getId());
            gridViewAdapter.notifyDataSetChanged();

            if (v.getId() == 1 + PreferenceHelper.getPrefix(getActivity()) * BaseDbAdapter.PREFIX_OFFSET) {
                listFragment = ListDataFragmentDeviceFavs.newInstance(bun);
            } else {
                listFragment = getListDataFragment(bun);
            }

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right);
            ft.replace(getDetailsViewId(), listFragment);
            ft.commit();

            listFragment.setArguments(bun);

        } else {
            startActivity(i);
            getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
        }
    }

}
