package de.ebertp.HomeDroid.Activities.Drawer;


import android.app.Activity;
import androidx.fragment.app.Fragment;

/**
 * Created by Philipp on 19.02.14.
 * <p/>
 * To be used in a drawer based UI in combination with an NavigationDrawerFragment
 */
public abstract class ContentFragment extends Fragment {


    private ContentFragmentCallback mCallbacks;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (ContentFragmentCallback) activity;
            mCallbacks.setTitleOfFragment(getTitle());
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement ContentFragmentCallback");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public abstract String getTitle();

    public interface ContentFragmentCallback {

        public void setTitleOfFragment(String title);
    }
}
