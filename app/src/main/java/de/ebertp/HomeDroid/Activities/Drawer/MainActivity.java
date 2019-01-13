package de.ebertp.HomeDroid.Activities.Drawer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.fragment.app.FragmentManager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;

import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.cketti.library.changelog.ChangeLog;
import de.ebertp.HomeDroid.Activities.Listing.Fragments.DataFragment;
import de.ebertp.HomeDroid.Activities.Listing.Fragments.GridDataFragmentCCUFavs;
import de.ebertp.HomeDroid.Activities.Listing.Fragments.GridDataFragmentCategories;
import de.ebertp.HomeDroid.Activities.Listing.Fragments.GridDataFragmentRooms;
import de.ebertp.HomeDroid.Activities.Listing.Fragments.HistoryFragment;
import de.ebertp.HomeDroid.Activities.Listing.Fragments.ListDataFragmentDeviceFavs;
import de.ebertp.HomeDroid.Activities.Listing.Fragments.ListDataFragmentNotifications;
import de.ebertp.HomeDroid.Activities.Listing.Fragments.ListDataFragmentRemote;
import de.ebertp.HomeDroid.Activities.Listing.Fragments.ListDataFragmentScripts;
import de.ebertp.HomeDroid.Activities.Listing.Fragments.ListDataFragmentVar;
import de.ebertp.HomeDroid.Activities.Listing.Fragments.RecentsFragment;
import de.ebertp.HomeDroid.Activities.Listing.Fragments.SpeechCommandsFragment;
import de.ebertp.HomeDroid.Activities.Listing.Fragments.WebCamFragment;
import de.ebertp.HomeDroid.Activities.Listing.LayerActivity;
import de.ebertp.HomeDroid.Activities.Listing.ServiceContentActivity;
import de.ebertp.HomeDroid.Activities.Wizard.WizardActivity;
import de.ebertp.HomeDroid.BuildConfig;
import de.ebertp.HomeDroid.Communication.Sync.SyncJobIntentService;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Model.HMDrawerItem;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.DrawerHelper;
import de.ebertp.HomeDroid.Utils.LicenceUtil;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;
import de.ebertp.HomeDroid.Utils.Util;
import timber.log.Timber;

public class MainActivity extends ServiceContentActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, ContentFragment.ContentFragmentCallback {

    private static final int FIRST_TINYMATIC_VERSION_CODE = 2000000;
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private CharSequence mTitle;

    public DataFragment currentFragment;
    private DrawerLayout mDrawerLayout;
    private HMDrawerItem[] mDrawerItems;
    private Handler uiHandler = new Handler();

    //true if the user has just finished the wizard
    private boolean mIsReturningFromWizard;

    private HMDrawerItem[] getDrawerItems() {
        List<HMDrawerItem> allItems = DrawerHelper.getDrawerItems(this);
        List<Integer> hiddenItems = PreferenceHelper.getHiddenDrawerItems(this);
        List<HMDrawerItem> allowedItems = new ArrayList<>();

        if (hiddenItems.isEmpty()) {
            return allItems.toArray(new HMDrawerItem[allItems.size()]);
        }

        for (HMDrawerItem item : allItems) {
            if (hiddenItems.contains(item.getId())) {
                Timber.d(item.toString() + " is hidden");
            } else {
                allowedItems.add(item);
            }
        }

        return allowedItems.toArray(new HMDrawerItem[allowedItems.size()]);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        if (PreferenceHelper.isDisclaimerAccepted(this)) {
            initViews();

            if (PreferenceHelper.getVersionCode(this) < FIRST_TINYMATIC_VERSION_CODE) {
                showTinyMaticDialog();
                Util.getChangelog(this).skipLogDialog();
            } else {
                ChangeLog log = Util.getChangelog(MainActivity.this);
                if (log.isFirstRun()) {
                    log.getFullLogDialog().show();
                }
            }

            if (savedInstanceState != null) {
                Timber.d("Just rotating, skipping sync");
                return;
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!SyncJobIntentService.isSyncRunning() && PreferenceHelper.isSyncAtStart(MainActivity.this)) {
                        SyncJobIntentService.enqueueSyncSmall(MainActivity.this);
                    }
                }
            }, 200);

            LicenceUtil.checkLicence(MainActivity.this, false, false);

            maybeShowRateItDialog();
        } else {
            new Thread(new Runnable() {
                @Override

                public void run() {
                    HomeDroidApp.db();
                }
            }).start();
            Util.getChangelog(this).skipLogDialog();
            showWizard();
        }
        PreferenceHelper.setVersionCode(this, BuildConfig.VERSION_CODE);
    }


    private void maybeShowRateItDialog() {

        if (PreferenceHelper.isUserHasGivenFeedback(this)) {
            return;
        }

        if (PreferenceHelper.getUserDimissedRatingAtSystemTimeMilis(this) != -1) {
            return;
        }

        if (PreferenceHelper.getLaunchCount(this) < 10) {
            return;
        }

        new AlertDialog.Builder(this)
                .setMessage(R.string.rating_question)
                .setPositiveButton(R.string.rating_response_good, (dialogInterface, i) -> showRatingGoToPlayStoreDialog())
                .setNegativeButton(R.string.rating_response_bad, (dialogInterface, i) -> showRatingFeedbackDialog())
                .setNeutralButton(R.string.rating_response_dont_ask_again, (dialogInterface, i) -> dontShowRatingAgain())
                .show();
    }

    private void showRatingFeedbackDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.rating_negative_feedback_question)
                .setPositiveButton(R.string.rating_button_send_feedback, (dialogInterface, i) -> {
                    markUserHasGivenFeedback();
                    sendRatingEmailFeedback();
                })
                .setNeutralButton(R.string.rating_button_no, (dialogInterface, i) -> dontShowRatingAgain())
                .show();
    }

    private void showRatingGoToPlayStoreDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(R.string.rating_positive_rate_now_question)
                .setNeutralButton(R.string.rating_already_done, (dialogInterface, i) -> markUserHasGivenFeedback())
                .setPositiveButton(R.string.rating_button_go_to_playstore, (dialogInterface, i) -> {
                    markUserHasGivenFeedback();
                    goToPlayStore();
                })
                .setNegativeButton(R.string.rating_button_no, (dialogInterface, i) -> dontShowRatingAgain())
                .show();
    }


    private void sendRatingEmailFeedback() {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("plain/text");

        String[] aEmailList = {"support@tinymatic.de"};

        intent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback TinyMatic");

        startActivity(Intent.createChooser(intent, getResources().getString(R.string.email)));
    }

    private void goToPlayStore() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=de.ebertp.HomeDroid"));
        intent.setPackage("com.android.vending");
        startActivity(intent);
    }

    private void markUserHasGivenFeedback() {
        PreferenceHelper.setUserHasGivenFeedback(this, true);
    }

    private void dontShowRatingAgain() {
        PreferenceHelper.setUserDimissedRatingAtSystemTimeMilis(this, System.currentTimeMillis());
    }

    private void showTinyMaticDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.tinymatic_intro_title)
                .setMessage(R.string.tinymatic_intro_desc)
                .setPositiveButton(R.string.close, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mIsReturningFromWizard) {
            mIsReturningFromWizard = false;
            SyncJobIntentService.enqueueSyncSmall(this);
            initViews();
        }
    }

    private void showWizard() {
        startActivityForResult(new Intent(this, WizardActivity.class), Util.REQUEST_WIZARD);
    }

    private void initViews() {
        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerItems = getDrawerItems();

        if (mDrawerLayout != null) {
            mNavigationDrawerFragment =
                    (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
            mTitle = getTitle();
            // Set up the drawer.
            mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout),
                    mDrawerItems);
        } else {
            ListView mDrawerListView = (ListView) findViewById(R.id.drawer_listview);
            final DrawerArrayAdapter adapter =
                    new DrawerArrayAdapter(getSupportActionBar().getThemedContext(), R.layout.drawer_item_icon_only,
                            mDrawerItems);
            mDrawerListView.setAdapter(adapter);
            mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                // mDrawerListView.setItemChecked(0, true);
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    onNavigationDrawerItemSelected(adapter.getItem(i).getId());
                }
            });

            int defaultTab = (PreferenceHelper.isChildProtectionOn(this) ? 0 : PreferenceHelper.getDefaultTab(this));
            mDrawerListView.setItemChecked(defaultTab, true);
            onNavigationDrawerItemSelected(defaultTab);
        }

        if (PreferenceHelper.isDarkTheme(this)) {
            findViewById(R.id.content).setBackgroundColor(getResources().getColor(R.color.homedroid_primary));
            findViewById(R.id.footer).setBackgroundDrawable(null);
        }
    }

    @Override
    protected void refreshData() {
        if (currentFragment != null) {
            currentFragment.refreshData();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        final FragmentManager fragmentManager = getSupportFragmentManager();
        DataFragment newFragment = null;

        switch (position) {
            case 0:
                newFragment = new GridDataFragmentRooms();
                break;
            case 1:
                newFragment = new GridDataFragmentCCUFavs();
                break;
            case 2:
                newFragment = new ListDataFragmentVar();
                break;
            case 3:
                newFragment = new GridDataFragmentCategories();
                break;
            case 4:
                newFragment = new ListDataFragmentScripts();
                break;
            case 5:
                newFragment = new ListDataFragmentRemote();
                break;
            case 6:
                newFragment = new RecentsFragment();
                uiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, LayerActivity.class);
                        startActivity(intent);
                    }
                }, 100);
                break;
            case 7:
                newFragment = new ListDataFragmentNotifications();
                break;
            case 8:
                newFragment = new SpeechCommandsFragment();
                break;
            case 9:
                newFragment = new HistoryFragment();
                break;
            case 10:
                newFragment = new RecentsFragment();
                break;
            case 11:
                newFragment = new ListDataFragmentDeviceFavs();
                break;
            case 12:
                newFragment = new WebCamFragment();
                break;
            default:
                newFragment = EmptyFragment.newInstance();
        }


        final DataFragment finalNewFragment = newFragment;
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed()) {
                    return;
                }

                if (finalNewFragment == null || isFinishing()) {
                    return;
                }

                fragmentManager.beginTransaction().replace(R.id.container, finalNewFragment).commitAllowingStateLoss();
                currentFragment = finalNewFragment;
            }
        }, 100);
    }

    @Override
    public void setTitleOfFragment(String title) {
        mTitle = title;
        //if (mDrawerLayout == null) {
        setTitle(mTitle);
        //}
    }

    public void setTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
    }

    public void restoreActionBar() {
        setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mDrawerLayout != null) {
            if (!mNavigationDrawerFragment.isDrawerOpen()) {
                restoreActionBar();
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showQuitDialog();
            return true;
        } else {
            return false;
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Util.REQUEST_WIZARD && resultCode == RESULT_OK) {
            PreferenceHelper.setDisclaimerAccepted(this, true);
            mIsReturningFromWizard = true;
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
