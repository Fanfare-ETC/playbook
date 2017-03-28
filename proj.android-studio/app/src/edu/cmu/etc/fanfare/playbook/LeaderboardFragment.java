package edu.cmu.etc.fanfare.playbook;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by yqi1 on 3/24/2017.
 */

public class LeaderboardFragment extends Fragment {

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view=inflater.inflate(R.layout.leaderboard_activity, container, false);


        LeaderboardWorker backgroundWorker = new LeaderboardWorker(this);
        //0 is default, 1 is sortiing by prediction, 2 is sorting by collection
        //move to a variable after the soring buttons are implemented
        backgroundWorker.execute("0");

        selfScoreWorker backgroundWorkerSelf = new selfScoreWorker(this);
        backgroundWorkerSelf.execute("self");


        return view;
    }
}
