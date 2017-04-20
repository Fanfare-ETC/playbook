package edu.cmu.etc.fanfare.playbook;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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
        String category;
        String color;
        String playerId1, playerId2, playerId3;
    }

    public class TrophyAdapter extends ArrayAdapter<TrophyCat> {
        private class ViewHolder {
            ImageView mImage1, mImage2, mImage3;
            ImageView mBanner;
            ImageView mLight;
            TextView mBannerName;
            TextView mName1, mName2, mName3;
            TextView mDescription1, mDescription2, mDescription3;
        }

        public TrophyAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<TrophyCat> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TrophyCat trophyCat = getItem(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(activity.getContext());
                convertView = inflater.inflate(R.layout.trophy_list_item, parent, false);
                viewHolder.mImage1 = (ImageView) convertView.findViewById(R.id.trophy1);
                viewHolder.mImage2 = (ImageView) convertView.findViewById(R.id.trophy2);
                viewHolder.mImage3 = (ImageView) convertView.findViewById(R.id.trophy3);
                viewHolder.mBanner = (ImageView) convertView.findViewById(R.id.trophyBanner);
                viewHolder.mBannerName = (TextView) convertView.findViewById(R.id.bannerName);
                viewHolder.mLight = (ImageView) convertView.findViewById(R.id.trophyShadow);
                viewHolder.mName1 = (TextView) convertView.findViewById(R.id.trophyName1);
                viewHolder.mName2 = (TextView) convertView.findViewById(R.id.trophyName2);
                viewHolder.mName3 = (TextView) convertView.findViewById(R.id.trophyName3);
                viewHolder.mDescription1 = (TextView) convertView.findViewById(R.id.trophyDescrp1);
                viewHolder.mDescription2 = (TextView) convertView.findViewById(R.id.trophyDescrp2);
                viewHolder.mDescription3 = (TextView) convertView.findViewById(R.id.trophyDescrp3);
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
                    bannerNameColor = 0x80620000;
                    break;
                case "white":
                    //bannerNameColor = 0x007F0000;
                    bannerNameColor = 0x077D0700;
                    break;
                default:
                    bannerNameColor = 0x80620000;
            }

            Typeface fontCat = Typeface.createFromAsset(activity.getContext().getAssets(), "fonts/rockb.ttf");

            String colorString = "banner_"+trophyCat.color;
            viewHolder.mBanner.setImageResource(getDrawable(activity.getContext(), colorString));
            viewHolder.mBannerName.setTextColor(bannerNameColor);
            viewHolder.mBannerName.setText(trophyCat.category.toUpperCase());
            viewHolder.mBannerName.setTypeface(fontCat);

            boolean light = false;
            String trophyIndex1 = "trophy"+Integer.toString(trophyCat.id1);
            Log.i("TROPHY", "Trophy1 ID is: " + trophyIndex1);
            if (trophyCat.playerId1 == null) {
                Log.i("TROPHY", "Player ID 1 is null");
                viewHolder.mImage1.setImageResource(R.drawable.trophy_black);
            } else {
                light = true;
                Log.i("TROPHY", "Player ID 1 is: " + trophyCat.playerId1);
                viewHolder.mImage1.setImageResource(getDrawable(activity.getContext(), trophyIndex1));
            }

            String trophyIndex2 = "trophy"+Integer.toString(trophyCat.id2);
            Log.i("TROPHY", "Trophy2 ID is: " + trophyIndex2);
            if (trophyCat.playerId2 == null) {
                Log.i("TROPHY", "Player ID 2 is null");
                viewHolder.mImage2.setImageResource(R.drawable.trophy_black);
            } else {
                light = true;
                Log.i("TROPHY", "Player ID 3 is: " + trophyCat.playerId2);
                viewHolder.mImage2.setImageResource(getDrawable(activity.getContext(), trophyIndex2));
            }

            String trophyIndex3 = "trophy"+Integer.toString(trophyCat.id3);
            Log.i("TROPHY", "Trophy3 ID is: " + trophyIndex3);
            if (trophyCat.playerId3 == null) {
                Log.i("TROPHY", "Player ID 3 is null");
                viewHolder.mImage3.setImageResource(R.drawable.trophy_black);
            } else {
                light = true;
                Log.i("TROPHY", "Player ID 3 is: " + trophyCat.playerId3);
                viewHolder.mImage3.setImageResource(getDrawable(activity.getContext(), trophyIndex3));
            }

            if(light == true){
                viewHolder.mLight.setImageResource(R.drawable.light_layer);
            }
            else if(light == false){
                viewHolder.mLight.setImageResource(R.drawable.shadow_layer);
            }

            Typeface nameFont = Typeface.createFromAsset(activity.getContext().getAssets(), "fonts/nova_excblack.otf");
            int nameTextSize = 16;

            viewHolder.mName1.setTextColor(Color.BLACK);
            viewHolder.mName1.setText(trophyCat.name1.toUpperCase());
            viewHolder.mName1.setTypeface(nameFont);
            if(trophyCat.name1.length() >= 17){
                nameTextSize = 12;
            }
            viewHolder.mName1.setTextSize(nameTextSize);

            viewHolder.mName2.setTextColor(Color.BLACK);
            viewHolder.mName2.setText(trophyCat.name2.toUpperCase());
            viewHolder.mName2.setTypeface(nameFont);
            if(trophyCat.name2.length() >= 17){
                nameTextSize = 12;
            }
            viewHolder.mName2.setTextSize(nameTextSize);

            viewHolder.mName3.setTextColor(Color.BLACK);
            viewHolder.mName3.setText(trophyCat.name3.toUpperCase());
            viewHolder.mName3.setTypeface(nameFont);
            if(trophyCat.name3.length() >= 17){
                nameTextSize = 12;
            }
            viewHolder.mName3.setTextSize(nameTextSize);

            Typeface descripFont = Typeface.createFromAsset(activity.getContext().getAssets(), "fonts/nova_excthin.otf");

            viewHolder.mDescription1.setTextColor(Color.BLACK);
            viewHolder.mDescription1.setText(trophyCat.description1);
            viewHolder.mDescription1.setTypeface(descripFont);
            viewHolder.mDescription1.setTextSize(11);
            viewHolder.mDescription1.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);

            viewHolder.mDescription2.setTextColor(Color.BLACK);
            viewHolder.mDescription2.setText(trophyCat.description2);
            viewHolder.mDescription2.setTypeface(descripFont);
            viewHolder.mDescription2.setTextSize(11);
            viewHolder.mDescription2.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);

            viewHolder.mDescription3.setTextColor(Color.BLACK);
            viewHolder.mDescription3.setText(trophyCat.description3);
            viewHolder.mDescription3.setTypeface(descripFont);
            viewHolder.mDescription3.setTextSize(11);
            viewHolder.mDescription3.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);

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
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setRequestProperty("Content-type", "application/json");
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                JSONObject object = new JSONObject();
                //object.put("id", LoginActivity.acct.getId().toString());
                object.put("id", "1"); //for test purpose only
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
                    trophyCat.category = item1.getString("category");
                    trophyCat.color = item1.getString("color");
                    trophyCat.playerId1 = item1.isNull("playerId") ? null : item1.getString("playerId");
                    trophyCat.playerId2 = item2.isNull("playerId") ? null : item2.getString("playerId");
                    trophyCat.playerId3 = item3.isNull("playerId") ? null : item3.getString("playerId");
                    trophies.add(trophyCat);
                }

                TrophyAdapter trophyAdapter = new TrophyAdapter(activity.getContext(), R.layout.trophy_list_item, trophies);
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
