package edu.cmu.etc.fanfare.playbook;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by yqi1 on 3/24/2017.
 */

public class LeaderboardFragment extends PlaybookFragment {

    LeaderboardFragment currActivity = this;
    ImageView sortTotal;
    ImageView sortPredict;
    ImageView sortCollect;
    boolean first_load;
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view=inflater.inflate(R.layout.leaderboard_fragment, container, false);
        setHasOptionsMenu(true);

        SharedPreferences settings = this.getActivity().getSharedPreferences("FANFARE_SHARED", 0);
        first_load = settings.getBoolean("first_load", true);
        //first_load = true; //for test purpose

        if(first_load == true){
            showTutorial();
            first_load = false;
            SharedPreferences.Editor editor = this.getActivity().getSharedPreferences("FANFARE_SHARED", MODE_PRIVATE).edit();
            editor.putBoolean("first_load", first_load);
            editor.apply();
        }

        sortTotal = (ImageView) view.findViewById(R.id.total);
        sortPredict = (ImageView) view.findViewById(R.id.prediction);
        sortCollect = (ImageView) view.findViewById(R.id.collection);

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
                backgroundWorker.execute("3");
                sortTotal.setImageResource(R.drawable.dots_yellow);
                sortPredict.setImageResource(R.drawable.predict_white);
                sortCollect.setImageResource(R.drawable.collect_white);
                Log.i("Button", "Order by total");

            }
        });

        Button button1 = (Button) view.findViewById(R.id.predictButton);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LeaderboardWorker backgroundWorker = new LeaderboardWorker(currActivity);
                backgroundWorker.execute("1");
                sortTotal.setImageResource(R.drawable.dots_white);
                sortPredict.setImageResource(R.drawable.predict_yellow);
                sortCollect.setImageResource(R.drawable.collect_white);
                Log.i("Button", "Order by prediction");

            }
        });

        Button button2 = (Button) view.findViewById(R.id.collectButton);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LeaderboardWorker backgroundWorker = new LeaderboardWorker(currActivity);
                backgroundWorker.execute("2");
                sortTotal.setImageResource(R.drawable.dots_white);
                sortPredict.setImageResource(R.drawable.predict_white);
                sortCollect.setImageResource(R.drawable.collect_yellow);
                Log.i("Button", "Order by collection");
            }
        });

        return view;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_collection, menu);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_collection_tutorial:
                showTutorial();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showTutorial(){
        Log.i("LEADERBOARD", "First load of the app.");
        AlertDialog.Builder builder = new AlertDialog.Builder(currActivity.getActivity());

        final Typeface nameFont = Typeface.createFromAsset(currActivity.getActivity().getAssets(), "nova_excblack.otf");
        TextView title2 = new TextView(currActivity.getActivity());
        title2.setText(("Leaderboard").toUpperCase());
        title2.setTextSize(36);
        title2.setTypeface(nameFont);
        title2.setGravity(Gravity.CENTER);

        builder.setCustomTitle(title2);

        builder.setMessage("You can see the heavy hitters in the crowd here. Tap the column headers to sort by game.");

        builder.setCancelable(false);

        builder.setPositiveButton(
                "Got it!",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();

        alert.show();
    }

}
