package edu.cmu.etc.fanfare.playbook;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HomeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        // Open stadium selection activity.
        View stadiumSelectNotification = view.findViewById(R.id.stadium_select_notification);
        stadiumSelectNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SeatSelectActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
