package de.ebertp.HomeDroid.Activities.Wizard;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import de.ebertp.HomeDroid.R;

public class WizardTutorialApiFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_wizard_xml_api, container, false);

        Button leftButton = rootView.findViewById(R.id.btn_left);
        leftButton.setOnClickListener(v -> ((WizardActivity) getActivity()).previous());

        Button rightButton = rootView.findViewById(R.id.btn_right);
        rightButton.setOnClickListener(v -> ((WizardActivity) getActivity()).next());

        return rootView;
    }
}
