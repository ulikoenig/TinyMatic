package de.ebertp.HomeDroid.Activities.Wizard;

import android.app.Activity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;

public class WizardActivity extends AppCompatActivity {

    private ViewPager mPager;
    private WizardPagerAdapter mPagerAdapter;
    private Button mBtnLeft;
    private Button mBtnRight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (PreferenceHelper.isDarkTheme(this)) {
            setTheme(R.style.DialogThemeDark);
        }
        super.onCreate(savedInstanceState);

        setFinishOnTouchOutside(false);

        setContentView(R.layout.activity_wizard_dialog);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new WizardPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        initButtons();
    }

    private void initButtons() {
        mBtnLeft = (Button) findViewById(R.id.btn_left);
        mBtnRight = (Button) findViewById(R.id.btn_right);

        mBtnLeft.setVisibility(View.GONE);
        mBtnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPager.getCurrentItem() - 1 == 0) {
                    mBtnLeft.setVisibility(View.GONE);
                }
                mPager.setCurrentItem(Math.max(mPager.getCurrentItem() - 1, 0));
            }
        });

        mBtnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnLeft.setVisibility(View.VISIBLE);
                if (mPager.getCurrentItem() + 1 == mPagerAdapter.getCount()) {
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    if (mPager.getCurrentItem() + 2 == mPagerAdapter.getCount()) {
                        mBtnRight.setText(R.string.done);
                    }
                    mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        mPager.setCurrentItem(Math.max(mPager.getCurrentItem() - 1, 0));
    }

    private class WizardPagerAdapter extends FragmentStatePagerAdapter {

        public WizardPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new WizardTermsFragment();
                case 1:
                    return new WizardTutorialFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}