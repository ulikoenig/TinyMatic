package de.ebertp.HomeDroid.Activities.Wizard;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import de.ebertp.HomeDroid.Activities.HelpActivity;
import de.ebertp.HomeDroid.Activities.Preferences.LicenceActivity;
import de.ebertp.HomeDroid.R;

public class WizardTutorialFinishedFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_wizard_tutorial_finished, container, false);

        rootView.findViewById(R.id.btn_help).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), HelpActivity.class));
            }
        });

        rootView.findViewById(R.id.text_licence).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), LicenceActivity.class));
            }
        });

        Button leftButton = rootView.findViewById(R.id.btn_left);
        leftButton.setOnClickListener(v -> ((WizardActivity) getActivity()).previous());

        Button rightButton = rootView.findViewById(R.id.btn_right);
        rightButton.setText(R.string.done);
        rightButton.setOnClickListener(v -> ((WizardActivity) getActivity()).next());

        return rootView;
    }
}
