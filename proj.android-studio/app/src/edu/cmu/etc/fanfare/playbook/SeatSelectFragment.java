package edu.cmu.etc.fanfare.playbook;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;


public class SeatSelectFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    public static int section = 0;
    private ImageView mStadiumMap;
    private ImageView arrow;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view=inflater.inflate(R.layout.stadium_select_fragment, container, false);

        mStadiumMap = (ImageView) view.findViewById(R.id.stadium_map);

        final Spinner spinner = (Spinner)view.findViewById(R.id.sections_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(),
                R.array.sections_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        arrow = (ImageView) view.findViewById(R.id.btn_dropdown);
        arrow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                spinner.performClick();
            }
        });

        Button button = (Button) view.findViewById(R.id.proceed);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackgroundWorker backgroundWorker = new BackgroundWorker(section);
                backgroundWorker.execute("section");

                /*
                Fragment predictionFragment = new PredictionFragment();

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, predictionFragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack(null);
                ft.commit();
                */
                Intent intent = new Intent(v.getContext(), AppActivity.class);
                Bundle extras = new Bundle();

                extras.putString("EXTRA_MESSAGE", Integer.toString(section));

                intent.putExtras(extras);
                //intent.putExtra(EXTRA_MESSAGE, seatNo);
                //intent.putExtra(EXTRA_COLOR, colorController);
                startActivity(intent);
                getActivity().finish();
            }
        });

        Button button1 = (Button) view.findViewById(R.id.section1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Spinner spinner = (Spinner) v.findViewById(R.id.sections_spinner);
                spinner.setSelection(1);



            }
        });
        Button button2 = (Button) view.findViewById(R.id.section2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Spinner spinner = (Spinner) v.findViewById(R.id.sections_spinner);
                spinner.setSelection(2);



            }
        });
        Button button3 = (Button) view.findViewById(R.id.section3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Spinner spinner = (Spinner) v.findViewById(R.id.sections_spinner);
                spinner.setSelection(3);



            }
        });
        Button button4 = (Button) view.findViewById(R.id.section4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Spinner spinner = (Spinner) v.findViewById(R.id.sections_spinner);
                spinner.setSelection(4);



            }
        });


        return view;
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        Log.d("Test","Selected:"+ parent.getItemAtPosition(pos).toString());
         switch (parent.getItemAtPosition(pos).toString()) {
             case "Section 1":
                 section = 1;
                 mStadiumMap.setImageResource(R.drawable.stadium_map_1);
                 break;
             case "Section 2":
                 section = 2;
                 mStadiumMap.setImageResource(R.drawable.stadium_map_2);
                 break;
             case "Section 3":
                 section = 3;
                 mStadiumMap.setImageResource(R.drawable.stadium_map_3);
                 break;
             case "Section 4":
                 section = 4;
                 mStadiumMap.setImageResource(R.drawable.stadium_map_4);
                 break;
         }


    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback

        Log.d("Test", "Nothing Selected");
    }




}
