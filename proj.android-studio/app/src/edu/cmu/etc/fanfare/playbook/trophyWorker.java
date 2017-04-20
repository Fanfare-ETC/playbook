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

    public class Trophy {
        int id;
        String description;
        String playerId;
    }

    public class TrophyAdapter extends ArrayAdapter<Trophy> {
        private class ViewHolder {
            ImageView mImage;
            TextView mDescription;
        }

        public TrophyAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Trophy> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Trophy trophy = getItem(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(activity.getActivity());
                convertView = inflater.inflate(R.layout.trophy_list_item, parent, false);
                viewHolder.mImage = (ImageView) convertView.findViewById(R.id.trophyImage);
                viewHolder.mDescription = (TextView) convertView.findViewById(R.id.trophyDescription);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // Populate data from trophy.
            String trophyIndex = "trophy"+Integer.toString(trophy.id);
            Log.i("TROPHY", "Trophy ID is: " + trophyIndex);
            if (trophy.playerId == null) {
                Log.i("TROPHY", "Player ID is null");
                viewHolder.mImage.setImageResource(R.drawable.trophy_black);
            } else {
                Log.i("TROPHY", "Player ID is: " + trophy.playerId);
                viewHolder.mImage.setImageResource(getDrawable(activity.getActivity(), trophyIndex));
            }

            Typeface externalFont = Typeface.createFromAsset(activity.getActivity().getAssets(), "fonts/nova1.ttf");

            viewHolder.mDescription.setTextColor(Color.BLACK);
            viewHolder.mDescription.setText(trophy.description);
            viewHolder.mDescription.setTypeface(externalFont);
            viewHolder.mDescription.setTextSize(11);


            if(trophy.id <= 6){
                convertView.setBackgroundColor(Color.argb(255, 0, 127, 0));
                //convertView.setBackgroundResource(R.drawable.trophy_top);
            }
            else{
                //convertView.setBackgroundColor(Color.argb(0, 0, 0, 0));
                convertView.setBackground(null);
            }
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
                List<Trophy> trophies = new ArrayList<Trophy>();
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject item = jArray.getJSONObject(i);
                    Trophy trophy = new Trophy();
                    trophy.id = item.getInt("trophyId");
                    trophy.description = item.getString("description");
                    trophy.playerId = item.isNull("playerId") ? null : item.getString("playerId");
                    trophies.add(trophy);
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
