package edu.cmu.etc.fanfare.playbook;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by ramya on 3/29/17.
 */

public class TreasureHuntTutorialFragment extends Fragment implements View.OnClickListener {
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.treasurehunt_fragment_tutorial, container, false);

        ImageView button_continue= (ImageView)view.findViewById(R.id.trhnt_continue);
        button_continue.setOnClickListener(this);
        return view;
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.trhnt_continue:
                // Create new fragment and transaction
                Fragment newFragment = new TreasureHuntFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack
                transaction.replace(R.id.content_frame, newFragment);
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
                break;
        }
    }
}

