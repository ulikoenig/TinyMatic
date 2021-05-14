package de.ebertp.HomeDroid.Activities.Wizard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import de.ebertp.HomeDroid.R;

public class WizardTermsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_wizard_terms, container, false);

        rootView.findViewById(R.id.btn_terms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getString(R.string.disclaimer_text))
                        .setCancelable(false)
                        .setTitle(getString(R.string.disclaimer_title))
                        .setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                builder.show();
            }
        });

        Button leftButton = rootView.findViewById(R.id.btn_left);
        leftButton.setVisibility(View.GONE);

        Button rightButton = rootView.findViewById(R.id.btn_right);
        rightButton.setOnClickListener(v -> ((WizardActivity) getActivity()).next());

        return rootView;
    }

}
