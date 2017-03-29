package edu.cmu.etc.fanfare.playbook;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.gcm.GcmReceiver;

/**
 * Created by yqi1 on 3/23/2017.
 */

public class LeaderboardActivity extends AppCompatActivity {
    /*@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leaderboard_activity);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setTitle(R.string.leader_board);

        LeaderboardWorker backgroundWorker = new LeaderboardWorker(this);
        //0 is default, 1 is sortiing by prediction, 2 is sorting by collection
        //move to a variable after the soring buttons are implemented
        backgroundWorker.execute("0");
    }*/
}
