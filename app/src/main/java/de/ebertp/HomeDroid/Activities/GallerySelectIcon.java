package de.ebertp.HomeDroid.Activities;

import android.app.Activity;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.IconDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.DataBaseAdapterManager;
import de.ebertp.HomeDroid.EventTracker;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.ViewAdapter.ImageAdapter;

public class GallerySelectIcon extends AppCompatActivity {


    protected DataBaseAdapterManager dbM;
    private IconDbAdapter mIconsDb;
    private int mRowId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.HomeDroidDark);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.homedroid_primary));
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.icon_gallery);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRowId = extras.getInt("rowId");
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbM = HomeDroidApp.db();
        mIconsDb = dbM.iconDbAdapter;

        GridView gridView = (GridView) findViewById(R.id.gridview);
        gridView.setAdapter(new ImageAdapter(this));

        gridView.setOnItemClickListener(new OnItemClickListener() {
            @SuppressWarnings("rawtypes")
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                int icon = position;
                Cursor c = mIconsDb.fetchItem(mRowId);
                if (c.getCount() == 0) {
                    mIconsDb.createItem(mRowId, icon);
                } else {
                    mIconsDb.updateItem(mRowId, icon);
                }
                de.ebertp.HomeDroid.Utils.Util.closeCursor(c);

                dbM.externalIconDbAdapter.deleteItem(mRowId);

                setResult(Activity.RESULT_OK);
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        EventTracker.trackScreen(this, "Select_Icon");
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}