package de.ebertp.HomeDroid.Activities.Wizard;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.PreferenceHelper;

public class WizardTutorialAddressFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_wizard_address, container, false);

        Button leftButton = rootView.findViewById(R.id.btn_left);
        leftButton.setOnClickListener(v -> ((WizardActivity) getActivity()).previous());

        Button rightButton = rootView.findViewById(R.id.btn_right);
        rightButton.setOnClickListener(v -> {
                    EditText addressEditText = rootView.findViewById(R.id.edit_address);
                    PreferenceHelper.setServer(getContext(), addressEditText.getText().toString());
                    ((WizardActivity) getActivity()).next();
                }
        );

        return rootView;
    }
}
