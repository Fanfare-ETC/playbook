package edu.cmu.etc.fanfare.playbook;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import static android.content.Context.MODE_PRIVATE;


public class SeatSelectFragment extends PlaybookFragment implements AdapterView.OnItemSelectedListener {
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
       // ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(),
            //    R.array.sections_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears

        String [] items = new String[5];
        items[0]="Select your section";
        items[1]="Section 1";
        items[2]="Section 2";
        items[3]="Section 3";
        items[4]="Section 4";

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(), R.layout.new_spinner, items) {

            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                Typeface externalFont = Typeface.createFromAsset(this.getContext().getAssets(), "fonts/nova1.ttf");
                ((TextView) v).setTypeface(externalFont);

                return v;
            }


            public View getDropDownView(int position,  View convertView,  ViewGroup parent) {
                View v =super.getDropDownView(position, convertView, parent);

                Typeface externalFont = Typeface.createFromAsset(this.getContext().getAssets(), "fonts/nova1.ttf");
                ((TextView) v).setTypeface(externalFont);
                v.setBackgroundColor(Color.GRAY);

                return v;
            }
        };

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

//get the section selected last time from sharedpreference
        SharedPreferences settings = this.getContext().getSharedPreferences("FANFARE_SHARED", 0);
        section = settings.getInt("section", 0);

        Log.d("SharedPreference", "Section chosen last time: " + section);

        spinner.setSelection(section);

        Button button = (Button) view.findViewById(R.id.proceed);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // BackgroundWorker backgroundWorker = new BackgroundWorker(section);
                //backgroundWorker.execute("section");

                //Show alert if no section is selected
                if(section == 0){
                    Log.i("Section", "Section not selected: " + section);
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setMessage("Please select a section!");
                    builder.setCancelable(false);

                    builder.setPositiveButton(
                            "Go back",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
/*
                builder.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
*/
                    AlertDialog alert = builder.create();
                    alert.show();
                }


                else {
                    SharedPreferences.Editor editor = v.getContext().getSharedPreferences("FANFARE_SHARED", MODE_PRIVATE).edit();
                    editor.putInt("section", section);
                    editor.apply();

                    AppActivity.sectionFlag = true;

                    Toolbar mToolbar = (Toolbar) getActivity().findViewById(R.id.my_toolbar);
                    // setSupportActionBar(mToolbar);
                    mToolbar.setTitle("Guide Your Runner");

                    Fragment treasureHuntFragment = new TreasureHuntFragment();

                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.content_frame, treasureHuntFragment)
                            .commit();
/*
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, treasureHuntFragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack(null);
                ft.commit();
*/
                    //Intent intent = new Intent(v.getContext(), TreasureHuntFragment.class); //change to treasure hunt scene
                    Bundle extras = new Bundle();
                   //save the section selected to shared preference


                    // extras.putString("EXTRA_MESSAGE", Integer.toString(section));
                    //intent.putExtras(extras);
                    //intent.putExtra(EXTRA_MESSAGE, seatNo);
                    //intent.putExtra(EXTRA_COLOR, colorController);
                    //startActivity(intent);
                    //getActivity().finish();
                }


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
             default:
                 section = 0;
                 mStadiumMap.setImageResource(R.drawable.stadium_map);
         }


    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback

        Log.d("Test", "Nothing Selected");
    }




}
