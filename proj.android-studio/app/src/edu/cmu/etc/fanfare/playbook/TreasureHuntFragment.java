package edu.cmu.etc.fanfare.playbook;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
                section = which+1;

                ImageView image= (ImageView)view.findViewById(R.id.map);
                switch(section)
                {
                    case 1:
                        image.setImageResource(R.drawable.section1);
                        break;
                    case 2:
                        image.setImageResource(R.drawable.section2);
                        break;
                    case 3:
                        image.setImageResource(R.drawable.section3);
                        break;
                    case 4:
                        image.setImageResource(R.drawable.section3);
                        break;
                }

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

        Context myContext= getContext();
        Typeface typeface = Typeface.createFromAsset(myContext.getAssets(), "fonts/nova1.ttf");
        Button button_w = (Button) view.findViewById(R.id.warmer);
        button_w.setTypeface(typeface);
        button_w.setOnClickListener(this);
        Button button_c = (Button) view.findViewById(R.id.colder);
        button_c.setTypeface(typeface);
        button_c.setOnClickListener(this);
        Button button_f = (Button) view.findViewById(R.id.flagdown);
        button_f.setTypeface(typeface);
        button_f.setOnClickListener(this);

        fb= (FloatingActionButton) view.findViewById(R.id.helmet);
        timerHandler.postDelayed(timerRunnable,0);

        return view;
    }

    public void onClick(View v) {
        //int x= Cocos2dxBridge.getSection();
        //Log.v("int",Integer.toString(x));
        switch (v.getId()) {
            case R.id.warmer:
                BackgroundWorker backgroundWorker = new  BackgroundWorker(section);
                backgroundWorker.execute("warmer");
                break;
            case R.id.colder:
                BackgroundWorker backgroundWorker1 = new  BackgroundWorker(section);
                backgroundWorker1.execute("colder");
                break;
            case R.id.flagdown:
                BackgroundWorker backgroundWorker2 = new  BackgroundWorker(section);
                backgroundWorker2.execute("flag");
                break;
        }
    }

}
