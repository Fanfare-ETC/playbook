package edu.cmu.etc.fanfare.playbook;

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

import static android.R.attr.id;
import static edu.cmu.etc.fanfare.playbook.R.styleable.View;

/**
 * Created by yqi1 on 3/23/2017.
 */

public class LeaderboardWorker extends AsyncTask<String,Void,String> {

    public LeaderboardFragment activity;

    private final String urlStringLeader = "http://" +
            BuildConfig.PLAYBOOK_SECTION_API_HOST + ":" +
            BuildConfig.PLAYBOOK_SECTION_API_PORT + "/" +
            BuildConfig.PLAYBOOK_LEADER_APP;

    LeaderboardWorker(LeaderboardFragment activity)
    {
        this.activity = activity;
    }
    @Override

    protected String doInBackground(String... params) {
        String result = null;

        if (params[0].equals("0")) {
            try {
                // String sort=Integer.toString(section);
                //Log.v("sec",Integer.toString(section));
                //String move = "2";
                URL url = new URL("http://10.0.2.2:9000/leaderboard");
                //URL url = new URL(urlStringLeader);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                //httpURLConnection.setDoOutput(true);
                //httpURLConnection.setDoInput(false);
                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setRequestProperty("Content-type", "application/json");
                //OutputStream outputStream = httpURLConnection.getOutputStream();
               // BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
               // JSONObject object = new JSONObject();
               /* object.put("id", section);
                Log.d("test", object.toString());*/
                // String post_data = URLEncoder.encode("sectionNo","UTF-8")+"="+URLEncoder.encode(sec,"UTF-8")+"&"
                //    + URLEncoder.encode("Move","UTF-8")+"="+URLEncoder.encode(move,"UTF-8");
                //bufferedWriter.write(object.toString());
               // bufferedWriter.flush();
               // bufferedWriter.close();
               // outputStream.close();
                Log.d("HTTP", "Leaderboard Response code: " + httpURLConnection.getResponseCode());
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
                    Log.i("LEAD_TR", "Count rows of result object");
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
        try {
            jArray = new JSONArray(result);
            Log.i("LEAD_TR", "Within post execute function");
            TableLayout tv = (TableLayout) activity.getView().findViewById(R.id.leaderboard);
            tv.removeAllViewsInLayout();

            for (int i = 0; i < jArray.length(); i++) {
                TableRow tr = new TableRow(activity.getContext());
                /*tr.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT));*/
                TableRow.LayoutParams params1 = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT, 0.5f);
                TableRow.LayoutParams params2 = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT, 1/6f);

                Log.i("LEAD_TR", "Within the array iteration");
                JSONObject json_data = null;
                json_data = jArray.getJSONObject(i);

                    Log.i("LEAD_TR", "Name" + json_data.getString("UserName") +
                            ", Prediction" + json_data.getInt("PredictionScore") +
                            ", Collection" + json_data.getString("CollectionScore") +
                            ", Total" + json_data.getString("Total"));

                Typeface externalFont = Typeface.createFromAsset(activity.getContext().getAssets(), "fonts/nova2.ttf");

                TextView b1 = new TextView(activity.getContext());
                String name = json_data.getString("UserName");
                b1.setText("#" + (i+1) + "  " + name);
                b1.setPadding(20, 10, 10, 0);
                b1.setTextColor(Color.WHITE);
                b1.setTypeface(externalFont);
                b1.setTextSize(20);
                b1.setGravity(Gravity.LEFT);
                b1.setLayoutParams(params1);
                tr.addView(b1);

                TextView b2 = new TextView(activity.getContext());
                String prediction = String.valueOf(json_data.getInt("PredictionScore"));
                b2.setText(prediction);
                b2.setTextColor(Color.WHITE);
                b2.setTypeface(externalFont);
                b2.setPadding(35, 10, 10, 0);
                b2.setTextSize(20);
                b2.setGravity(Gravity.RIGHT|Gravity.CENTER);
                b2.setLayoutParams(params2);
                tr.addView(b2);

                TextView b3 = new TextView(activity.getContext());
                String collection = String.valueOf(json_data.getInt("CollectionScore"));
                b3.setText(collection);
                b3.setTextColor(Color.WHITE);
                b3.setTypeface(externalFont);
                b3.setPadding(35, 10, 10, 0);
                b3.setTextSize(20);
                b3.setGravity(Gravity.RIGHT|Gravity.CENTER);
                b3.setLayoutParams(params2);
                tr.addView(b3);

                TextView b4 = new TextView(activity.getContext());
                String total = String.valueOf(json_data.getInt("Total"));
                b4.setText(total);
                b4.setPadding(35, 10, 10, 0);
                b4.setTypeface(externalFont);
                b4.setTextSize(20);
                b4.setGravity(Gravity.RIGHT|Gravity.CENTER);
                b4.setTextColor(Color.WHITE);
                b4.setLayoutParams(params2);
                tr.addView(b4);

                tv.addView(tr);
               // final View vline = new View(activity);
                //vline.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 2));
                //vline.setBackgroundColor(Color.BLUE);
                //tv.addView(vline);


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

}
