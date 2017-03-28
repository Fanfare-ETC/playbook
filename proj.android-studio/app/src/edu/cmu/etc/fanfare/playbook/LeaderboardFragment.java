package edu.cmu.etc.fanfare.playbook;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by yqi1 on 3/24/2017.
 */

public class LeaderboardFragment extends Fragment {

    LeaderboardFragment currActivity = this;
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view=inflater.inflate(R.layout.leaderboard_activity, container, false);

        LeaderboardWorker backgroundWorker = new LeaderboardWorker(currActivity);
        backgroundWorker.execute("0");
        //0 is default, 1 is sortiing by prediction, 2 is sorting by collection
        //move to a variable after the soring buttons are implemented
       // backgroundWorker.execute(Integer.toString(flag));

        selfScoreWorker backgroundWorkerSelf = new selfScoreWorker(this);
        backgroundWorkerSelf.execute("self");

        Button button = (Button) view.findViewById(R.id.totalButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LeaderboardWorker backgroundWorker = new LeaderboardWorker(currActivity);
                backgroundWorker.execute("0");
                Log.i("Button", "Order by total");

            }
        });

        Button button1 = (Button) view.findViewById(R.id.predictButton);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LeaderboardWorker backgroundWorker = new LeaderboardWorker(currActivity);
                backgroundWorker.execute("1");
                Log.i("Button", "Order by prediction");

            }
        });

        Button button2 = (Button) view.findViewById(R.id.collectButton);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LeaderboardWorker backgroundWorker = new LeaderboardWorker(currActivity);
                backgroundWorker.execute("2");
                Log.i("Button", "Order by collection");
            }
        });

        return view;
    }

}
