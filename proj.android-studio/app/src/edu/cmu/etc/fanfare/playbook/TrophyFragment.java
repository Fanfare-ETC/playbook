package edu.cmu.etc.fanfare.playbook;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by yqi1 on 4/4/2017.
 */

public class TrophyFragment extends PlaybookFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view=inflater.inflate(R.layout.trophy_fragment, container, false);

        Toolbar mToolbar = (Toolbar) getActivity().findViewById(R.id.my_toolbar);
        mToolbar.setTitle("Trophy Case");

        trophyWorker backgroundWorker = new trophyWorker(this);
        backgroundWorker.execute("trophy");

        return view;
    }

}
