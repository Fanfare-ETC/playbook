package edu.cmu.etc.fanfare.playbook;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.Date;

public class TreasureHuntFragment extends Fragment implements View.OnClickListener{

    public static int section;
    long time = 0;
    FloatingActionButton fb;
    final Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            int sign= 0;
            Date dt = new Date();
            int seconds = dt.getSeconds();
            fb.setAlpha((float)seconds/60);
            timerHandler.postDelayed(this, 50);
        }
    };
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view=inflater.inflate(R.layout.treasurehunt_fragment, container, false);

        //create a list box to enter section

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add("1");
        arrayAdapter.add("2");
        arrayAdapter.add("3");
        arrayAdapter.add("4");

        // 2. Chain together various setter methods to set the dialog characteristics
        //builder.setMessage(R.string.dialog_message1)
        builder.setTitle("Select Section");
        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                section = which;

                ImageView image= (ImageView)view.findViewById(R.id.map);
                switch(section)
                {
                    case 1:
                        image.setImageResource(R.drawable.map2);
                        break;
                    case 2:
                        image.setImageResource(R.drawable.map2);
                        break;
                    case 3:
                        image.setImageResource(R.drawable.map2);
                        break;
                    case 4:
                        image.setImageResource(R.drawable.map2);
                        break;
                }

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();


        ImageView button_w= (ImageView)view.findViewById(R.id.warmer);
        button_w.setOnClickListener(this);
        ImageView button_c= (ImageView)view.findViewById(R.id.colder);
        button_c.setOnClickListener(this);
        //ImageView button_p= (ImageView)view.findViewById(R.id.plant);
        //button_p.setOnClickListener(this);

        //fb= (FloatingActionButton) view.findViewById(R.id.helmet);
        //timerHandler.postDelayed(timerRunnable,0);
        return view;

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.warmer:
                    BackgroundWorker backgroundWorker = new BackgroundWorker(section);
                    backgroundWorker.execute("warmer");
                break;
            case R.id.colder:
                BackgroundWorker backgroundWorker1 = new  BackgroundWorker(section);
                backgroundWorker1.execute("colder");
                break;
            case R.id.plant:
                BackgroundWorker backgroundWorker2 = new  BackgroundWorker(section);
                backgroundWorker2.execute("plant");
                break;
        }
    }

}
