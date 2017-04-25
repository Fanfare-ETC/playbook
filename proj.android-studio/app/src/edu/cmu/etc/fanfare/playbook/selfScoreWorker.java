package edu.cmu.etc.fanfare.playbook;

/**
 * Created by yqi1 on 3/27/2017.
 */

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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

public class selfScoreWorker extends AsyncTask<String,Void,String> {

    public LeaderboardFragment activity;

    private final String urlStringLeader = "http://" +
            BuildConfig.PLAYBOOK_SECTION_API_HOST + ":" +
            BuildConfig.PLAYBOOK_SECTION_API_PORT + "/" +
            BuildConfig.PLAYBOOK_SELF_APP;

    selfScoreWorker(LeaderboardFragment activity)
    {
        this.activity = activity;
    }
    @Override

    protected String doInBackground(String... params) {
        String result = null;

        if (params[0].equals("self")) {
            try {
                // String sort=Integer.toString(section);
                //Log.v("sec",Integer.toString(section));
                //String move = "2";
                //URL url = new URL("http://10.0.2.2:9000/leaderboard");
                URL url = new URL(urlStringLeader);
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
                object.put("id", "1");
                Log.d("acct_no", object.toString());
                // String post_data = URLEncoder.encode("sectionNo","UTF-8")+"="+URLEncoder.encode(sec,"UTF-8")+"&"
                //    + URLEncoder.encode("Move","UTF-8")+"="+URLEncoder.encode(move,"UTF-8");
                bufferedWriter.write(object.toString());
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                int responseCode = httpURLConnection.getResponseCode();
                if(responseCode != 200)
                    return null;
                Log.d("HTTP", "Self Score Response code: " + responseCode);
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
                /*StringBuilder sb = new StringBuilder();
                String row = null;
                while((row = bufferedReader.readLine()) != null )
                {
                    sb.append(row + "\n");
                }*/
                //String result="";
                String line = null;
                StringBuilder sb = new StringBuilder();
                while((line = bufferedReader.readLine())!= null) {
                    Log.i("SELF_Line", line.toString());
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
        JSONObject jo = null;
        if(result != null) {
            try {
                jo = new JSONObject(result);
                Log.i("SELF_TR", "Within post execute function");
                TableLayout tv = (TableLayout) activity.getView().findViewById(R.id.selfboard);
                tv.removeAllViewsInLayout();

                TableRow tr = new TableRow(activity.getActivity());
                    /*tr.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.MATCH_PARENT));*/
                TableRow.LayoutParams params1 = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT, 0.5f);
                TableRow.LayoutParams params2 = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT, 1 / 3f);
                /*
                    JSONObject json_data = null;
                    json_data = jArray.getJSONObject(i);
    */
                Log.i("SELF_TR", "Prediction" + jo.getInt("PredictionScore") +
                        ", Collection" + jo.getString("CollectionScore") +
                        ", Total" + jo.getString("Total"));

                Typeface externalFont = Typeface.createFromAsset(activity.getActivity().getAssets(), "SCOREBOARD.ttf");

                int textSize = 40;
                if((jo.getInt("PredictionScore") > 999) | (jo.getInt("CollectionScore") > 999) | (jo.getInt("Total") > 999)){
                    if((jo.getInt("PredictionScore") > 9999) | (jo.getInt("CollectionScore") > 9999) | (jo.getInt("Total") > 9999)){
                        textSize = 20;
                    }
                    else{
                        textSize = 30;
                    }

                }
                TextView b2 = new TextView(activity.getActivity());
                String prediction = String.valueOf(jo.getInt("PredictionScore"));
                if(jo.getInt("PredictionScore") < 10){
                    b2.setText("0" + prediction);
                }
                else{
                    b2.setText(prediction);
                }
                b2.setTextColor(Color.WHITE);
                b2.setTypeface(externalFont);
                b2.setPadding(15, 10, 0, 0);

                    b2.setTextSize(textSize);

                b2.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
                b2.setLayoutParams(params2);
                tr.addView(b2);

                TextView b3 = new TextView(activity.getActivity());
                String collection = String.valueOf(jo.getInt("CollectionScore"));
                if(jo.getInt("CollectionScore") < 10){
                    b3.setText("0" + collection);
                }
                else{
                    b3.setText(prediction);
                }
                b3.setTextColor(Color.WHITE);
                b3.setTypeface(externalFont);
                b3.setPadding(10, 10, 0, 0);

                    b3.setTextSize(textSize);

                b3.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
                b3.setLayoutParams(params2);
                tr.addView(b3);

                TextView b4 = new TextView(activity.getActivity());
                String total = String.valueOf(jo.getInt("Total"));
                if(jo.getInt("Total") < 10){
                    b4.setText("0" + total);
                }
                else{
                    b4.setText(total);
                }
                b4.setPadding(10, 10, 10, 0);
                b4.setTypeface(externalFont);

                    b4.setTextSize(textSize);

                b4.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
                b4.setTextColor(Color.WHITE);
                b4.setLayoutParams(params2);
                tr.addView(b4);

                tv.addView(tr);
                // final View vline = new View(activity);
                //vline.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 2));
                //vline.setBackgroundColor(Color.BLUE);
                //tv.addView(vline);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else{
            /*Typeface externalFont = Typeface.createFromAsset(activity.getContext().getAssets(), "fonts/nova2.ttf");

            TextView b0 = new TextView(activity.getContext());
            b0.setTypeface(externalFont);
            b0.setText("Server error. Please inform Project Fanfare for tech support");
            b0.setPadding(20, 50, 12, 0);
            b0.setTextSize(18);*/
            Log.i("selfboard", "Server error. Object is null");
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

}
