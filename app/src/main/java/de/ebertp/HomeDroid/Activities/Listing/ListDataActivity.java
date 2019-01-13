package de.ebertp.HomeDroid.Activities.Listing;

import android.os.Build;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.MenuItem;
import android.view.WindowManager;

import de.ebertp.HomeDroid.Activities.Drawer.ContentFragment;
import de.ebertp.HomeDroid.Activities.Listing.Fragments.ListDataFragment;
import de.ebertp.HomeDroid.Activities.Listing.Fragments.ListDataFragmentCCUFavs;
import de.ebertp.HomeDroid.Activities.Listing.Fragments.ListDataFragmentCategory;
import de.ebertp.HomeDroid.Activities.Listing.Fragments.ListDataFragmentDeviceFavs;
import de.ebertp.HomeDroid.Activities.Listing.Fragments.ListDataFragmentNotifications;
import de.ebertp.HomeDroid.Activities.Listing.Fragments.ListDataFragmentProtocol;
import de.ebertp.HomeDroid.Activities.Listing.Fragments.ListDataFragmentRemote;
import de.ebertp.HomeDroid.Activities.Listing.Fragments.ListDataFragmentRoom;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;

public class ListDataActivity extends ServiceContentActivity implements SwipeRefreshLayout.OnRefreshListener, ContentFragment.ContentFragmentCallback {

    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_ID = "id";
    public static final String EXTRA_TYPE = "type";

    public static final String TYPE_CATEGORY = "cat";
    public static final String TYPE_DEVICE_FAV = "fav";
    public static final String TYPE_CCU_FAV = "ccu_fav";
    public static final String TYPE_REMOTE = "remote";
    public static final String TYPE_ROOM = "room";
    public static final String TYPE_NOTIFICATION = "notification";
    public static final String TYPE_PROTOCOL = "protocol";

    ListDataFragment listFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (PreferenceHelper.isDarkTheme(this)) {
            setTheme(R.style.HomeDroidDark);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                getWindow().setStatusBarColor(getResources().getColor(R.color.homedroid_primary));
            }
        } else {
            setTheme(R.style.HomeDroidLight);
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);

        if (PreferenceHelper.isDarkTheme(this)) {
            findViewById(R.id.content).setBackgroundColor(getResources().getColor(R.color.homedroid_primary));
        }

        Bundle extras = getIntent().getExtras();

        String roomName = extras.getString(EXTRA_NAME);
        String type = extras.getString(EXTRA_TYPE);

        getSupportActionBar().setTitle(roomName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {


            if (type.equals(TYPE_CATEGORY)) {
                listFragment = new ListDataFragmentCategory();
            } else if (type.equals(TYPE_CCU_FAV)) {
                listFragment = new ListDataFragmentCCUFavs();
            } else if (type.equals(TYPE_DEVICE_FAV)) {
                listFragment = new ListDataFragmentDeviceFavs();
            } else if (type.equals(TYPE_REMOTE)) {
                listFragment = new ListDataFragmentRemote();
            } else if (type.equals(TYPE_ROOM)) {
                listFragment = new ListDataFragmentRoom();
            } else if (type.equals(TYPE_NOTIFICATION)) {
                listFragment = new ListDataFragmentNotifications();
            } else if (type.equals(TYPE_PROTOCOL)) {
                listFragment = new ListDataFragmentProtocol();
            }

            listFragment.setArguments(extras);
            getSupportFragmentManager().beginTransaction().replace(R.id.ll_content, listFragment, "list_content").commit();
        } else {
            listFragment = (ListDataFragment) getSupportFragmentManager().findFragmentByTag("list_content");
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        listFragment.onPageSelected();
    }

    protected void refreshData() {
        listFragment.refreshData();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    @Override
    public void onRefresh() {
        refreshData();
    }

    @Override
    public void setTitleOfFragment(String title) {
        //don't need that here. bad class hierarchy, maybe fix this later
    }
}
