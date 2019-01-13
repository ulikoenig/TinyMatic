package de.ebertp.HomeDroid.Activities.Listing.Fragments;

import android.content.Intent;
import android.os.Bundle;

import de.ebertp.HomeDroid.Activities.Listing.ListDataActivity;
import de.ebertp.HomeDroid.EventTracker;
import de.ebertp.HomeDroid.R;

public class GridDataFragmentCategories extends GridDataFragment {

    @Override
    public void onResume() {
        super.onResume();
        EventTracker.trackScreen(getActivity(), "Category_Grid");
    }

    @Override
    public Intent getIntentForList() {
        Intent intent = new Intent(getActivity(), ListDataActivity.class);
        intent.putExtra(ListDataActivity.EXTRA_TYPE, ListDataActivity.TYPE_CATEGORY);
        return intent;
    }

    @Override
    public void initDbHelper() {
        mDbHelper = dbManager.categoriesDbAdapter;
    }

    @Override
    public ListDataFragment getListDataFragment(Bundle bun) {
        return ListDataFragmentCategory.newInstance(bun);
    }

    @Override
    public int getDetailsViewId() {
        return R.id.details_categories;
    }

    @Override
    public String getTitle() {
        return getResources().getString(R.string.categories);
    }

}
