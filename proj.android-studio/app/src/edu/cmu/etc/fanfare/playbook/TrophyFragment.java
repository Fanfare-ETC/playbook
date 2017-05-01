package edu.cmu.etc.fanfare.playbook;

import android.content.DialogInterface;
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
import android.widget.TextView;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by yqi1 on 4/4/2017.
 */

public class TrophyFragment extends PlaybookFragment {

    TrophyFragment curr = this;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view=inflater.inflate(R.layout.trophy_fragment, container, false);
        setHasOptionsMenu(true);

        boolean first_load;

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

        Toolbar mToolbar = (Toolbar) getActivity().findViewById(R.id.my_toolbar);
        mToolbar.setTitle("Trophy Case");

        trophyWorker backgroundWorker = new trophyWorker(this);
        backgroundWorker.execute("trophy");

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
        Log.i("TROPHY", "First load of the app.");
        AlertDialog.Builder builder = new AlertDialog.Builder(curr.getActivity());

        final Typeface nameFont = Typeface.createFromAsset(curr.getActivity().getAssets(), "nova_excblack.otf");
        TextView title2 = new TextView(curr.getActivity());
        title2.setText(("Trophy Case").toUpperCase());
        title2.setTextSize(36);
        title2.setTypeface(nameFont);
        title2.setGravity(Gravity.CENTER);

        builder.setCustomTitle(title2);

        builder.setMessage("You can see all the trophies you've earned here. Scroll down for more.");

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
