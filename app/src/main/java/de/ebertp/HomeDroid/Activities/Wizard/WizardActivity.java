package de.ebertp.HomeDroid.Activities.Wizard;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import de.ebertp.HomeDroid.R;

public class WizardActivity extends AppCompatActivity {

    private ViewPager mPager;
    private WizardPagerAdapter mPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wizard);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new WizardPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }

    public void previous() {
        mPager.setCurrentItem(Math.max(mPager.getCurrentItem() - 1, 0));
    }

    public void next() {
        if (mPager.getCurrentItem() + 1 == mPagerAdapter.getCount()) {
            setResult(Activity.RESULT_OK);
            finish();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() + 1);
        }
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
                    return new WizardTutorialApiFragment();
                case 2:
                    return new WizardTutorialAddressFragment();
                case 3:
                    return new WizardTutorialFinishedFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    }
}
