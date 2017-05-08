package edu.cmu.etc.fanfare.playbook;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yqi1 on 4/4/2017.
 */

public class trophyWorker extends AsyncTask<String,Void,String> {

    public TrophyFragment activity;

    private final String urlStringTrophy = "http://" +
            BuildConfig.PLAYBOOK_SECTION_API_HOST + ":" +
            BuildConfig.PLAYBOOK_SECTION_API_PORT + "/" +
            BuildConfig.PLAYBOOK_TROPHY_APP;

    public class TrophyCat {
        int id1, id2, id3;
        String name1, name2, name3;
        String description1, description2, description3;
        String date1, date2, date3;
        String category;
        String color;
        String playerId1, playerId2, playerId3;
    }

    public class myOnGlobalLayoutListener implements ViewTreeObserver.OnPreDrawListener {
        HorizontalScrollView scrollView;
        String scrollString;
        Button button;
        ViewTreeObserver observer;
        public myOnGlobalLayoutListener(ViewTreeObserver observer, HorizontalScrollView scrollView, Button button, String scrollString){
            this.scrollView = scrollView;
            this.scrollString = scrollString;
            this.button = button;
            this.observer = observer;
        }
        @Override
        public boolean onPreDraw() {
            int viewWidth = scrollView.getMeasuredWidth();
            int contentWidth = scrollView.getChildAt(0).getWidth();
            //Log.i("TROPHY", "view width " + Integer.toString(viewWidth));
            //Log.i("TROPHY", "content width " + Integer.toString(contentWidth));
            if(viewWidth - contentWidth >= 0) {
                // not scrollable
                //Log.i("SCROLL", scrollString + " seems not scrollable");
                //viewHolder.mNameButton3.setVisibility(View.INVISIBLE);
                button.setVisibility(View.INVISIBLE);

            }
            else {
                //Log.i("SCROLL", scrollString + " seems scrollable");
                button.setVisibility(View.VISIBLE);

            }

            //observer.removeOnGlobalLayoutListener(this);
            return true;
        }
    }

    public class TrophyAdapter extends ArrayAdapter<TrophyCat> {
        private class ViewHolder {
            ImageView mImage1, mImage2, mImage3;
            ImageView mBanner;
            ImageView mLight;
            TextView mBannerName;
            TextView mName1, mName2, mName3;
            TextView mDescription1, mDescription2, mDescription3;
            Button mButton1, mButton2, mButton3;
            Button mNameButton1, mDescripButton1, mNameButton2, mDescripButton2, mNameButton3, mDescripButton3;
            HorizontalScrollView scrollView1, scrollView2, scrollView3, scrollViewName1, scrollViewName2, scrollViewName3;
        }

        public TrophyAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<TrophyCat> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final TrophyCat trophyCat = getItem(position);
            final ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(activity.getActivity());
                convertView = inflater.inflate(R.layout.trophy_list_item, parent, false);
                viewHolder.mImage1 = (ImageView) convertView.findViewById(R.id.trophy1);
                viewHolder.mImage2 = (ImageView) convertView.findViewById(R.id.trophy2);
                viewHolder.mImage3 = (ImageView) convertView.findViewById(R.id.trophy3);
                viewHolder.mBanner = (ImageView) convertView.findViewById(R.id.trophyBanner);
                viewHolder.mBannerName = (TextView) convertView.findViewById(R.id.bannerName);
                viewHolder.mLight = (ImageView) convertView.findViewById(R.id.trophyShadow);
                viewHolder.mName1 = (TextView) convertView.findViewById(R.id.trophyName1Content);
                viewHolder.mName2 = (TextView) convertView.findViewById(R.id.trophyName2Content);
                viewHolder.mName3 = (TextView) convertView.findViewById(R.id.trophyName3Content);
                viewHolder.mDescription1 = (TextView) convertView.findViewById(R.id.trophyDescrp1Content);
                viewHolder.mDescription2 = (TextView) convertView.findViewById(R.id.trophyDescrp2Content);
                viewHolder.mDescription3 = (TextView) convertView.findViewById(R.id.trophyDescrp3Content);
                viewHolder.mButton1 = (Button) convertView.findViewById(R.id.trophyButton1);
                viewHolder.mButton2 = (Button) convertView.findViewById(R.id.trophyButton2);
                viewHolder.mButton3 = (Button) convertView.findViewById(R.id.trophyButton3);
                viewHolder.mNameButton1 = (Button) convertView.findViewById(R.id.name_arrow1);
                viewHolder.mNameButton2 = (Button) convertView.findViewById(R.id.name_arrow2);
                viewHolder.mNameButton3 = (Button) convertView.findViewById(R.id.name_arrow3);
                viewHolder.mDescripButton1 = (Button) convertView.findViewById(R.id.descrip_arrow1);
                viewHolder.mDescripButton2 = (Button) convertView.findViewById(R.id.descrip_arrow2);
                viewHolder.mDescripButton3 = (Button) convertView.findViewById(R.id.descrip_arrow3);
                viewHolder.scrollView1 = (HorizontalScrollView) convertView.findViewById(R.id.trophyDescrp1);
                viewHolder.scrollView2 = (HorizontalScrollView) convertView.findViewById(R.id.trophyDescrp2);
                viewHolder.scrollView3 = (HorizontalScrollView) convertView.findViewById(R.id.trophyDescrp3);
                viewHolder.scrollViewName1 = (HorizontalScrollView) convertView.findViewById(R.id.trophyName1);
                viewHolder.scrollViewName2 = (HorizontalScrollView) convertView.findViewById(R.id.trophyName2);
                viewHolder.scrollViewName3 = (HorizontalScrollView) convertView.findViewById(R.id.trophyName3);


                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // Populate data from trophyCat.


            Integer bannerNameColor;
            switch (trophyCat.color){
                case "green":
                case "gold":
                case "blue":
                case "red":
                    bannerNameColor = Color.WHITE;
                    break;
                case "yellow":
                    bannerNameColor = 0xFF806200;
                    break;
                case "white":
                    //bannerNameColor = 0x007F0000;
                    bannerNameColor = 0xFF007F00;
                    break;
                default:
                    bannerNameColor = Color.BLACK;
            }

            Typeface fontCat = Typeface.createFromAsset(activity.getActivity().getAssets(), "rockb.ttf");

            String colorString = "banner_"+trophyCat.color;
            viewHolder.mBanner.setImageResource(getDrawable(activity.getActivity(), colorString));
            viewHolder.mBannerName.setTextColor(bannerNameColor);
            viewHolder.mBannerName.setText(trophyCat.category.toUpperCase());
            viewHolder.mBannerName.setTypeface(fontCat);

            //int descripTextSize = 11;

            boolean light = false;
            final String trophyIndex1 = "trophy"+Integer.toString(trophyCat.id1);
            Log.i("TROPHY", "Trophy1 ID is: " + trophyIndex1);
            if (trophyCat.playerId1 == null) {
                Log.i("TROPHY", "Player ID 1 is null");

                if(trophyCat.id1 <= 15) {
                    viewHolder.mImage1.setImageResource(R.drawable.trophy_black);
                }
                else if(trophyCat.id1 <= 21){
                    viewHolder.mImage1.setImageResource(R.drawable.trophy_black_prediction);
                }
                else{
                    viewHolder.mImage1.setImageResource(R.drawable.trophy_black_dots);
                }

                viewHolder.mDescription1.setText(trophyCat.description1);
                if(trophyCat.description1.length() >= 63){
                    viewHolder.mDescription1.setTextSize(20);
                }
                viewHolder.mDescription1.setTextSize(20);
            } else {
                light = true;
                Log.i("TROPHY", "Player ID 1 is: " + trophyCat.playerId1);
                viewHolder.mImage1.setImageResource(getDrawable(activity.getActivity(), trophyIndex1));
                viewHolder.mDescription1.setText(trophyCat.date1);
                viewHolder.mDescription1.setTextSize(20);
            }

            //HorizontalScrollView scrollView = (HorizontalScrollView)convertView.findViewById(R.id.trophyDescrp1);
          /*  ViewTreeObserver observer = viewHolder.scrollView.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int viewWidth = viewHolder.scrollView.getMeasuredWidth();
                    int contentWidth = viewHolder.scrollView.getChildAt(0).getWidth();
                    Log.i("TROPHY", "view width " + Integer.toString(viewWidth));
                    Log.i("TROPHY", "content width " + Integer.toString(contentWidth));
                    if(viewWidth - contentWidth > 0) {
                        // not scrollable
                        Log.i("SCROLL", trophyCat.description1 + " seems not scrollable");
                       // viewHolder.mDescripButton1.setVisibility(View.INVISIBLE);

                    }
                    else {
                        Log.i("SCROLL", trophyCat.description1 + " seems scrollable");
                        viewHolder.mDescripButton1.setVisibility(View.VISIBLE);

                    }
                }
            });*/
            ViewTreeObserver observer1 = viewHolder.scrollView1.getViewTreeObserver();
            myOnGlobalLayoutListener listenerDescrip1 = new myOnGlobalLayoutListener(observer1, viewHolder.scrollView1, viewHolder.mDescripButton1, trophyCat.description1);
            observer1.addOnPreDrawListener(listenerDescrip1);

            viewHolder.mDescripButton1.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){

                    Log.i("LEADERBOARD", "Button Clicked");
                    //scrollView.scrollTo((int)scrollView.getScrollX() + 20, (int)scrollView.getScrollY());
                    viewHolder.scrollView1.fullScroll(View.FOCUS_RIGHT);
                }
            });


            final String trophyIndex2 = "trophy"+Integer.toString(trophyCat.id2);
            Log.i("TROPHY", "Trophy2 ID is: " + trophyIndex2);
            if (trophyCat.playerId2 == null) {

                Log.i("TROPHY", "Player ID 2 is null");
                viewHolder.mDescription2.setText(trophyCat.description2);

                if(trophyCat.id2 <= 15) {
                    viewHolder.mImage2.setImageResource(R.drawable.trophy_black);
                }
                else if(trophyCat.id2 <= 21){
                    viewHolder.mImage2.setImageResource(R.drawable.trophy_black_prediction);
                }
                else{
                    viewHolder.mImage2.setImageResource(R.drawable.trophy_black_dots);
                }

                if(trophyCat.description2.length() >= 63){
                    viewHolder.mDescription2.setTextSize(20);
                }
                viewHolder.mDescription2.setTextSize(20);
            } else {
                light = true;
                Log.i("TROPHY", "Player ID 3 is: " + trophyCat.playerId2);
                viewHolder.mImage2.setImageResource(getDrawable(activity.getActivity(), trophyIndex2));
                viewHolder.mDescription2.setText(trophyCat.date2);
                viewHolder.mDescription2.setTextSize(20);
            }

            //final HorizontalScrollView scrollView1 = (HorizontalScrollView)convertView.findViewById(R.id.trophyDescrp2);
         /*   ViewTreeObserver observer1 = viewHolder.scrollView1.getViewTreeObserver();
            observer1.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int viewWidth = viewHolder.scrollView1.getMeasuredWidth();
                    int contentWidth = viewHolder.scrollView1.getChildAt(0).getWidth();
                    Log.i("TROPHY", "view width " + Integer.toString(viewWidth));
                    Log.i("TROPHY", "content width " + Integer.toString(contentWidth));
                    if(viewWidth - contentWidth > 0) {
                        // not scrollable
                        Log.i("SCROLL", trophyCat.description2 + " seems not scrollable");
                        //viewHolder.mDescripButton2.setVisibility(View.INVISIBLE);

                    }
                    else {
                        Log.i("SCROLL", trophyCat.description2 + " seems scrollable");
                        viewHolder.mDescripButton2.setVisibility(View.VISIBLE);

                    }

                }
            });*/

            ViewTreeObserver observer2 = viewHolder.scrollView2.getViewTreeObserver();
            myOnGlobalLayoutListener listenerDescrip2 = new myOnGlobalLayoutListener(observer2, viewHolder.scrollView2, viewHolder.mDescripButton2, trophyCat.description2);
            observer2.addOnPreDrawListener(listenerDescrip2);
            viewHolder.mDescripButton2.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){

                    Log.i("TROPHY", "Button Clicked");
                    //scrollView.scrollTo((int)scrollView.getScrollX() + 20, (int)scrollView.getScrollY());
                    viewHolder.scrollView2.fullScroll(View.FOCUS_RIGHT);
                }
            });


            final String trophyIndex3 = "trophy"+Integer.toString(trophyCat.id3);
            Log.i("TROPHY", "Trophy3 ID is: " + trophyIndex3);
            if (trophyCat.playerId3 == null) {

                Log.i("TROPHY", "Player ID 3 is null");
                viewHolder.mDescription3.setText(trophyCat.description3);

                if(trophyCat.id3 <= 15) {
                    viewHolder.mImage3.setImageResource(R.drawable.trophy_black);
                }
                else if(trophyCat.id3 <= 21){
                    viewHolder.mImage3.setImageResource(R.drawable.trophy_black_prediction);
                }
                else{
                    viewHolder.mImage3.setImageResource(R.drawable.trophy_black_dots);
                }

                if(trophyCat.description3.length() >= 63){
                    viewHolder.mDescription3.setTextSize(20);
                }
                viewHolder.mDescription3.setTextSize(20);
            } else {
                light = true;
                Log.i("TROPHY", "Player ID 3 is: " + trophyCat.playerId3);
                viewHolder.mImage3.setImageResource(getDrawable(activity.getActivity(), trophyIndex3));
                viewHolder.mDescription3.setText(trophyCat.date3);
                viewHolder.mDescription3.setTextSize(20);
            }

           // final HorizontalScrollView scrollView2 = (HorizontalScrollView)convertView.findViewById(R.id.trophyDescrp3);
           /* ViewTreeObserver observer2 = viewHolder.scrollView2.getViewTreeObserver();
            observer2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int viewWidth = viewHolder.scrollView2.getMeasuredWidth();
                    int contentWidth = viewHolder.scrollView2.getChildAt(0).getWidth();
                    Log.i("TROPHY", "view width " + Integer.toString(viewWidth));
                    Log.i("TROPHY", "content width " + Integer.toString(contentWidth));
                    if(viewWidth - contentWidth > 0) {
                        // not scrollable
                        Log.i("SCROLL", trophyCat.description3 + " seems not scrollable");
                        //viewHolder.mDescripButton3.setVisibility(View.INVISIBLE);

                    }
                    else {
                        Log.i("SCROLL", trophyCat.description3 + " seems scrollable");
                        viewHolder.mDescripButton3.setVisibility(View.VISIBLE);

                    }
                }
            });*/
            ViewTreeObserver observer3 = viewHolder.scrollView3.getViewTreeObserver();
            myOnGlobalLayoutListener listenerDescrip3 = new myOnGlobalLayoutListener(observer3, viewHolder.scrollView3, viewHolder.mDescripButton3, trophyCat.description3);
            observer3.addOnPreDrawListener(listenerDescrip3);

            viewHolder.mDescripButton3.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){

                    Log.i("TROPHY", "Button Clicked");
                    //scrollView.scrollTo((int)scrollView.getScrollX() + 20, (int)scrollView.getScrollY());
                    viewHolder.scrollView3.fullScroll(View.FOCUS_RIGHT);
                }
            });



            if(light == true){
                viewHolder.mLight.setImageResource(R.drawable.light_layer);
            }
            else if(light == false){
                viewHolder.mLight.setImageResource(R.drawable.shadow_layer);
            }

            final Typeface nameFont = Typeface.createFromAsset(activity.getActivity().getAssets(), "nova_excblack.otf");
            int nameTextSize = 16;

            viewHolder.mName1.setTextColor(Color.BLACK);
            viewHolder.mName1.setText(trophyCat.name1.toUpperCase());
            viewHolder.mName1.setTypeface(nameFont);

            viewHolder.mName1.setTextSize(nameTextSize);

            //final HorizontalScrollView scrollViewName = (HorizontalScrollView)convertView.findViewById(R.id.trophyName1);
            ViewTreeObserver observerName1 = viewHolder.scrollViewName1.getViewTreeObserver();
            myOnGlobalLayoutListener listener1 = new myOnGlobalLayoutListener(observerName1, viewHolder.scrollViewName1, viewHolder.mNameButton1, trophyCat.name1);
            observerName1.addOnPreDrawListener(listener1);
            viewHolder.mNameButton1.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){

                    Log.i("TROPHY", "Button Clicked");
                    //scrollView.scrollTo((int)scrollView.getScrollX() + 20, (int)scrollView.getScrollY());
                    viewHolder.scrollViewName1.fullScroll(View.FOCUS_RIGHT);
                }
            });


            viewHolder.mName2.setTextColor(Color.BLACK);
            viewHolder.mName2.setText(trophyCat.name2.toUpperCase());
            viewHolder.mName2.setTypeface(nameFont);

            viewHolder.mName2.setTextSize(nameTextSize);

            //final HorizontalScrollView scrollViewName1 = (HorizontalScrollView)convertView.findViewById(R.id.trophyName2);
            ViewTreeObserver observerName2 = viewHolder.scrollViewName2.getViewTreeObserver();
            myOnGlobalLayoutListener listener2 = new myOnGlobalLayoutListener(observerName2, viewHolder.scrollViewName2, viewHolder.mNameButton2, trophyCat.name2);
            observerName2.addOnPreDrawListener(listener2);
            viewHolder.mNameButton2.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){

                    Log.i("TROPHY", "Button Clicked");
                    //scrollView.scrollTo((int)scrollView.getScrollX() + 20, (int)scrollView.getScrollY());
                    viewHolder.scrollViewName2.fullScroll(View.FOCUS_RIGHT);
                }
            });

            viewHolder.mName3.setTextColor(Color.BLACK);
            viewHolder.mName3.setText(trophyCat.name3.toUpperCase());
            viewHolder.mName3.setTypeface(nameFont);

            viewHolder.mName3.setTextSize(nameTextSize);

            //final HorizontalScrollView scrollViewName2 = (HorizontalScrollView)convertView.findViewById(R.id.trophyName3);
            ViewTreeObserver observerName3 = viewHolder.scrollViewName3.getViewTreeObserver();
            myOnGlobalLayoutListener listener3 = new myOnGlobalLayoutListener(observerName3, viewHolder.scrollViewName3, viewHolder.mNameButton3, trophyCat.name3);
            observerName3.addOnPreDrawListener(listener3);
            viewHolder.mNameButton3.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){

                    Log.i("TROPHY", "Button Clicked");
                    //scrollView.scrollTo((int)scrollView.getScrollX() + 20, (int)scrollView.getScrollY());
                    viewHolder.scrollViewName3.fullScroll(View.FOCUS_RIGHT);
                }
            });

            Typeface descripFont = Typeface.createFromAsset(activity.getActivity().getAssets(), "nova_excthin.otf");
            //descripTextSize = 11;
            viewHolder.mDescription1.setTextColor(Color.BLACK);
            //viewHolder.mDescription1.setText(trophyCat.description1);
            viewHolder.mDescription1.setTypeface(descripFont);
            //viewHolder.mDescription1.setGravity(Gravity.TOP | Gravity.RIGHT);

            viewHolder.mDescription2.setTextColor(Color.BLACK);
            //viewHolder.mDescription2.setText(trophyCat.description2);
            viewHolder.mDescription2.setTypeface(descripFont);

           // viewHolder.mDescription2.setGravity(Gravity.TOP | Gravity.RIGHT);

            viewHolder.mDescription3.setTextColor(Color.BLACK);
            //viewHolder.mDescription3.setText(trophyCat.description3);
            viewHolder.mDescription3.setTypeface(descripFont);

            //viewHolder.mDescription3.setGravity(Gravity.TOP | Gravity.RIGHT);

            viewHolder.mButton1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("Trophy", "Item " + trophyCat.id1 + "selected");
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                    ImageView trophy1 = new ImageView(v.getContext());
                    trophy1.setImageResource(getDrawable(activity.getActivity(), trophyIndex1));

                    TextView title1 = new TextView(v.getContext());
                    title1.setText(("Trophy " + trophyCat.name1).toUpperCase());
                    title1.setTextSize(36);
                    title1.setTypeface(nameFont);
                    title1.setGravity(Gravity.CENTER);

                    builder.setView(trophy1);
                    if(trophyCat.playerId1 != null){
                        builder.setMessage("Description: " + trophyCat.description1 + "\n" + "You got it on " + trophyCat.date1 + "!");
                    }
                    else {
                        builder.setMessage("Description: " + trophyCat.description1);
                    }

                    builder.setCustomTitle(title1);
                    builder.setCancelable(false);

                    builder.setPositiveButton(
                            "Go back",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert = builder.create();

                    alert.show();
                }
            });

            viewHolder.mButton2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("Trophy", "Item " + trophyCat.id2 + "selected");
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                    ImageView trophy2 = new ImageView(v.getContext());
                    trophy2.setImageResource(getDrawable(activity.getActivity(), trophyIndex2));

                    TextView title2 = new TextView(v.getContext());
                    title2.setText(("Trophy " + trophyCat.name2).toUpperCase());
                    title2.setTextSize(36);
                    title2.setTypeface(nameFont);
                    title2.setGravity(Gravity.CENTER);

                    builder.setView(trophy2);
                    if(trophyCat.playerId2 != null){
                        builder.setMessage("Description: " + trophyCat.description2 + "\n" + "You got it on " + trophyCat.date2 + "!");
                    }
                    else {
                        builder.setMessage("Description: " + trophyCat.description2);
                    }
                    builder.setCustomTitle(title2);
                    builder.setCancelable(false);

                    builder.setPositiveButton(
                            "Go back",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert = builder.create();

                    alert.show();
                }
            });

            viewHolder.mButton3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("Trophy", "Item " + trophyCat.id3 + "selected");
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                    ImageView trophy3 = new ImageView(v.getContext());
                    trophy3.setImageResource(getDrawable(activity.getActivity(), trophyIndex3));

                    TextView title3 = new TextView(v.getContext());
                    title3.setText(("Trophy " + trophyCat.name3).toUpperCase());
                    title3.setTextSize(36);
                    title3.setTypeface(nameFont);
                    title3.setGravity(Gravity.CENTER);

                    builder.setView(trophy3);
                    if(trophyCat.playerId3 != null){
                        builder.setMessage("Description: " + trophyCat.description3 + "\n" + "You got it on " + trophyCat.date3 + "!");
                    }
                    else {
                        builder.setMessage("Description: " + trophyCat.description3);
                    }
                    builder.setCustomTitle(title3);
                    builder.setCancelable(false);

                    builder.setPositiveButton(
                            "Go back",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert = builder.create();

                    alert.show();
                }
            });


            return convertView;
        }
    }

    trophyWorker(TrophyFragment activity)
    {
        this.activity = activity;
    }
    @Override

    protected String doInBackground(String... params) {
        String result = null;

        if (params[0].equals("trophy")) {
            try {
                URL url = new URL(urlStringTrophy);
                Log.i("Trophy URL", "String URL: " + url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setRequestProperty("Content-type", "application/json");
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                JSONObject object = new JSONObject();
                object.put("id", LoginActivity.acct.getId().toString());
                //object.put("id", "1"); //for test purpose only
                Log.d("acct_no", object.toString());
                bufferedWriter.write(object.toString());
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                int responseCode = httpURLConnection.getResponseCode();
                if(responseCode != 200)
                    return null;
                Log.d("HTTP", "Trophy Response code: " + responseCode);
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));

                String line = null;
                StringBuilder sb = new StringBuilder();
                while((line = bufferedReader.readLine())!= null) {
                    Log.i("TROPHY_Line", line.toString());
                    sb.append(line);
                }
                result = sb.toString();
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected void onPostExecute(String result) {
        JSONArray jArray = null;
        if(result != null) {
            try {
                jArray = new JSONArray(result);
                List<TrophyCat> trophies = new ArrayList<TrophyCat>();
                for (int i = 0; i < jArray.length() / 3; i++) {
                    JSONObject item1 = jArray.getJSONObject(i*3);
                    JSONObject item2 = jArray.getJSONObject(i*3+1);
                    JSONObject item3 = jArray.getJSONObject(i*3+2);
                    TrophyCat trophyCat = new TrophyCat();
                    trophyCat.id1 = item1.getInt("trophyId");
                    trophyCat.id2 = item2.getInt("trophyId");
                    trophyCat.id3 = item3.getInt("trophyId");
                    trophyCat.name1 = item1.getString("trophyName");
                    trophyCat.name2 = item2.getString("trophyName");
                    trophyCat.name3 = item3.getString("trophyName");
                    trophyCat.description1 = item1.getString("description");
                    trophyCat.description2 = item2.getString("description");
                    trophyCat.description3 = item3.getString("description");
                    trophyCat.date1 = item1.getString("Date");
                    trophyCat.date2 = item2.getString("Date");
                    trophyCat.date3 = item3.getString("Date");
                    trophyCat.category = item1.getString("category");
                    trophyCat.color = item1.getString("color");
                    trophyCat.playerId1 = item1.isNull("playerId") ? null : item1.getString("playerId");
                    trophyCat.playerId2 = item2.isNull("playerId") ? null : item2.getString("playerId");
                    trophyCat.playerId3 = item3.isNull("playerId") ? null : item3.getString("playerId");
                    trophies.add(trophyCat);
                }

                TrophyAdapter trophyAdapter = new TrophyAdapter(activity.getActivity(), R.layout.trophy_list_item, trophies);
                ListView listView = (ListView) activity.getView().findViewById(R.id.trophyList);
                listView.setAdapter(trophyAdapter);
                //listView.invalidate();

                /*
                TableLayout tv = (TableLayout) activity.getView().findViewById(R.id.trophyList);
                tv.removeAllViewsInLayout();

                DisplayMetrics displayMetrics = new DisplayMetrics();
                ((Activity)activity.getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height = displayMetrics.heightPixels;
                int width = displayMetrics.widthPixels;
                int maxH = height / 8;
                Log.i("TROPHY", "Max height: " + maxH);


                for(int i = 0; i < jArray.length(); i++){


                    JSONObject jo = null;
                    jo = jArray.getJSONObject(i);

                    Log.i("TROPHY_TR", "Within post execute function for loop");

                    TableRow tr = new TableRow(activity.getContext());

                    TableRow.LayoutParams params1 = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            maxH, 1);
                    TableRow.LayoutParams params2 = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            maxH, 1);


                    Log.i("TROPHY_TR", "Player ID: " + jo.getString("playerId") +
                            ", Trophy ID: " + jo.getInt("trophyId") +
                            ", Trophy Description: " + jo.getString("description"));

                    String trophy_index = "trophy"+Integer.toString(i+1);

                    ImageView b1 = new ImageView(activity.getContext());
                    if(jo.getString("playerId").equals("null")){
                        Log.i("TROPHY", "Player ID is null");
                        b1.setImageResource(R.drawable.trophy_black);
                    }
                    else{
                        Log.i("TROPHY", "Player ID is: " + jo.getString("playerId"));
                        b1.setImageResource(getDrawable(activity.getContext(), trophy_index));
                    }

                    b1.setPadding(10, 10, 0, 10);
                    b1.setAdjustViewBounds(true);
                    //b1.setMaxHeight(148);

                    Typeface externalFont = Typeface.createFromAsset(activity.getContext().getAssets(), "fonts/nova2.ttf");

                    TextView b2 = new TextView(activity.getContext());
                    String description = String.valueOf(jo.getString("description"));
                    b2.setText(description);
                    */
                    /*if(jo.getString("playerId").equals("null")){
                        Log.i("TROPHY", "Player ID is null");
                        b2.setTextColor(Color.BLUE);
                    }
                    else{
                        Log.i("TROPHY", "Player ID is: " + jo.getString("playerId"));
                        b2.setTextColor(Color.parseColor("#FFB84D"));
                    }*/
                    /*
                    b2.setTextColor(Color.BLACK);
                    b2.setTypeface(externalFont);
                    //b2.setPadding(35, 10, 10, 0);
                    b2.setPadding(35, 10, 10, 0);
                    b2.setTextSize(16);
                    b2.setGravity(Gravity.CENTER);

                    b1.setLayoutParams(params1);
                    b2.setLayoutParams(params2);

                    tr.addView(b1);
                    tr.addView(b2);
                    //tr.setBackgroundResource(R.drawable.shelf1);

                    tv.addView(tr);
                    */

                    /*if(i == 3){
                        ImageView divider = new ImageView(activity.getContext());
                        divider.setImageResource(R.drawable.divider);
                        tv.addView(divider);
                    }*/

                //}


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else{
            Toast.makeText(activity.getActivity(), "Unable to retrieve trophy list due to a server error.", Toast.LENGTH_SHORT).show();
            Log.i("trophyList", "Server error. Object is null");
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    //For dynamically setting the images in the drawables
    public static int getDrawable(Context context, String name){
        Assert.assertNotNull(context);
        Assert.assertNotNull(name);

        return context.getResources().getIdentifier(name,
                "drawable", context.getPackageName());
    }


}
