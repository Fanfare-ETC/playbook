package edu.cmu.etc.fanfare.playbook;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProfileFragment extends Fragment implements View.OnClickListener{

    public static int section;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view=inflater.inflate(R.layout.profile_fragment, container, false);

        //create a list box to enter section

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add("1");
        arrayAdapter.add("2");
        arrayAdapter.add("3");

        // 2. Chain together various setter methods to set the dialog characteristics
        //builder.setMessage(R.string.dialog_message1)
        builder.setTitle("Select Section");
        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                section = which+1;

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

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
        }
        Button button_w = (Button) view.findViewById(R.id.warmer);
        button_w.setOnClickListener(this);
        Button button_c = (Button) view.findViewById(R.id.colder);
        button_c.setOnClickListener(this);
        Button button_f = (Button) view.findViewById(R.id.flagdown);
        button_f.setOnClickListener(this);


        return view;
    }

    public void onClick(View v) {
        //do what you want to do when button is clicked

        //int x= Cocos2dxBridge.getSection();
        //Log.v("int",Integer.toString(x));
        switch (v.getId()) {
            case R.id.warmer:
                Background_worker backgroundWorker = new Background_worker(section);
                backgroundWorker.execute("warmer");
                break;
            case R.id.colder:
                Background_worker backgroundWorker1 = new Background_worker(section);
                backgroundWorker1.execute("colder");
                break;
            case R.id.flagdown:
                Background_worker backgroundWorker2 = new Background_worker(section);
                backgroundWorker2.execute("flag");
                break;
        }
    }

}
