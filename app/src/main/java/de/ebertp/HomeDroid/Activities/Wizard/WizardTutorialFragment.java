package de.ebertp.HomeDroid.Activities.Wizard;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.ebertp.HomeDroid.Activities.HelpActivity;
import de.ebertp.HomeDroid.Activities.Preferences.LicenceActivity;
import de.ebertp.HomeDroid.R;

public class WizardTutorialFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_wizard_tutorial, container, false);

        rootView.findViewById(R.id.btn_install_guide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://www.tinymatic.de/installation/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        rootView.findViewById(R.id.btn_help).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), HelpActivity.class));
            }
        });

        rootView.findViewById(R.id.btn_licence).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), LicenceActivity.class));
            }
        });

        return rootView;
    }
}
