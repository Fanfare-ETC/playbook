package edu.cmu.etc.fanfare.playbook;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.test.suitebuilder.TestMethod;
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

import static android.R.attr.id;
import static android.R.attr.text;
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
    private final String urlStringLeaderP = "http://" +
            BuildConfig.PLAYBOOK_SECTION_API_HOST + ":" +
            BuildConfig.PLAYBOOK_SECTION_API_PORT + "/" +
            BuildConfig.PLAYBOOK_LEADERP_APP;
    private final String urlStringLeaderC = "http://" +
            BuildConfig.PLAYBOOK_SECTION_API_HOST + ":" +
            BuildConfig.PLAYBOOK_SECTION_API_PORT + "/" +
            BuildConfig.PLAYBOOK_LEADERC_APP;
    private int flag = 0; //0 order by total, 1 order by prediction, 2 order by collection

    public class Leaders {
        String rank;
        String name;
        int predictionScore;
        int collectionScore;
        int totalScore;
    }

    public class LeaderboardAdapter extends ArrayAdapter<Leaders> {
        private class ViewHolder {
            TextView mRank;
            TextView mName;
            TextView mPrediction, mCollection, mTotal;
        }

        public LeaderboardAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Leaders> objects) {
            super(context, resource, objects);
        }

        @Override
        public android.view.View getView(int position, View convertView, ViewGroup parent) {
            Leaders leader = getItem(position);
            LeaderboardAdapter.ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new LeaderboardAdapter.ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(activity.getActivity());
                convertView = inflater.inflate(R.layout.leaderboard_list_item, parent, false);
                viewHolder.mRank = (TextView) convertView.findViewById(R.id.rank);
                viewHolder.mName = (TextView) convertView.findViewById(R.id.name);
                viewHolder.mPrediction = (TextView) convertView.findViewById(R.id.prediction);
                viewHolder.mCollection = (TextView) convertView.findViewById(R.id.collection);
                viewHolder.mTotal = (TextView) convertView.findViewById(R.id.total);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (LeaderboardAdapter.ViewHolder) convertView.getTag();
            }

            // Populate data from leader.

            Typeface fontCat = Typeface.createFromAsset(activity.getActivity().getAssets(), "fonts/nova2.ttf");

            Log.i("LEAD_LIST", "Name" + leader.name +
                    ", Prediction" + leader.predictionScore +
                    ", Collection" + leader.collectionScore +
                    ", Total" + leader.totalScore);

            viewHolder.mRank.setText("#" + leader.rank);
            if(Integer.parseInt(leader.rank) == 1){
                viewHolder.mRank.setTextSize(26);
            }
            else {
                viewHolder.mRank.setTextSize(20);
            }
            viewHolder.mRank.setTextColor(Color.WHITE);
            viewHolder.mRank.setGravity(Gravity.LEFT);
            viewHolder.mRank.setTypeface(null, fontCat.ITALIC);

            viewHolder.mName.setText(leader.name);
            viewHolder.mName.setTextColor(Color.WHITE);
            viewHolder.mName.setGravity(Gravity.LEFT | Gravity.CENTER);
            viewHolder.mName.setTypeface(fontCat);
            viewHolder.mName.setTextSize(20);

            int scoreSize = 20;
            if(leader.predictionScore > 999 | leader.collectionScore > 999 | leader.totalScore > 999){
                if(leader.predictionScore>9999 | leader.collectionScore> 9999 | leader.totalScore > 9999){
                    scoreSize = 12;
                }
                else{
                    scoreSize = 16;
                }

            }

            viewHolder.mPrediction.setText(String.valueOf(leader.predictionScore));
            if (flag == 1) {
                viewHolder.mPrediction.setTextColor(Color.parseColor("#FFC300"));
            }
            else{
                viewHolder.mPrediction.setTextColor(Color.WHITE);
            }
            viewHolder.mPrediction.setTextSize(scoreSize);
            viewHolder.mPrediction.setGravity(Gravity.RIGHT | Gravity.CENTER);
            viewHolder.mPrediction.setTypeface(fontCat);

            viewHolder.mCollection.setText(String.valueOf(leader.collectionScore));
            if (flag == 2) {
                viewHolder.mCollection.setTextColor(Color.parseColor("#FFC300"));
            }
            else{
                viewHolder.mCollection.setTextColor(Color.WHITE);
            }
            viewHolder.mCollection.setTextSize(scoreSize);
            viewHolder.mCollection.setGravity(Gravity.RIGHT | Gravity.CENTER);
            viewHolder.mCollection.setTypeface(fontCat);

            viewHolder.mTotal.setText(String.valueOf(leader.totalScore));
            if (flag == 0) {
                viewHolder.mTotal.setTextColor(Color.parseColor("#FFC300"));
            }
            else{
                viewHolder.mTotal.setTextColor(Color.WHITE);
            }
            viewHolder.mTotal.setTextSize(scoreSize);
            viewHolder.mTotal.setGravity(Gravity.RIGHT | Gravity.CENTER);
            viewHolder.mTotal.setTypeface(fontCat);

            return convertView;
        }
    }


    LeaderboardWorker(LeaderboardFragment activity)
    {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(String... params) {
        String result = null;

        if (params[0].equals("0")) {
            try {
                flag = 0;
                // String sort=Integer.toString(section);
                //Log.v("sec",Integer.toString(section));
                //String move = "2";
                //URL url = new URL("http://10.0.2.2:9000/leaderboard");

                URL url = new URL(urlStringLeader);
                Log.i("TEST_URL", "String URL: " + url);
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
                int responseCode = httpURLConnection.getResponseCode();
                if(responseCode != 200)
                    return null;
                Log.d("HTTP", "Leaderboard Response code: " + responseCode);
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

        else if (params[0].equals("1")) {
            try {
                flag = 1;
                // String sort=Integer.toString(section);
                //Log.v("sec",Integer.toString(section));
                //String move = "2";
                //URL url = new URL("http://10.0.2.2:9000/leaderboard");
                URL url = new URL(urlStringLeaderP);
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
                int responseCode = httpURLConnection.getResponseCode();
                if(responseCode != 200)
                    return null;
                Log.d("HTTP", "LeaderboardP Response code: " + responseCode);
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

        else if (params[0].equals("2")) {
            try {
                flag = 2;
                // String sort=Integer.toString(section);
                //Log.v("sec",Integer.toString(section));
                //String move = "2";
                //URL url = new URL("http://10.0.2.2:9000/leaderboard");
                URL url = new URL(urlStringLeaderC);
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
                int responseCode = httpURLConnection.getResponseCode();
                Log.d("HTTP", "LeaderboardC Response code: " + responseCode);
                if(responseCode != 200)
                    return null;
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
        if(result != null) {
            try {
                jArray = new JSONArray(result);
                List<Leaders> leaders = new ArrayList<Leaders>();
                for (int i = 0; i < jArray.length(); i++) {

                    Log.i("LEAD_TR", "Within the array iteration");
                    JSONObject json_data = null;
                    json_data = jArray.getJSONObject(i);

                    Log.i("LEAD_TR", "Name" + json_data.getString("UserName") +
                            ", Prediction" + json_data.getInt("PredictionScore") +
                            ", Collection" + json_data.getString("CollectionScore") +
                            ", Total" + json_data.getString("Total"));
                    
                    Leaders leader = new Leaders();
                    leader.rank = Integer.toString(i+1);
                    leader.name = json_data.getString("UserName");
                    leader.predictionScore = json_data.getInt("PredictionScore");
                    leader.collectionScore = json_data.getInt("CollectionScore");
                    leader.totalScore = json_data.getInt("Total");

                    leaders.add(leader);
                }

                LeaderboardAdapter leaderboardAdapter = new LeaderboardAdapter(activity.getActivity(), R.layout.leaderboard_list_item, leaders);
                ListView listView = (ListView) activity.getView().findViewById(R.id.leaderboardList);
                listView.setAdapter(leaderboardAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else{

            /*Typeface externalFont = Typeface.createFromAsset(activity.getActivity().getAssets(), "fonts/nova2.ttf");

            TextView b0 = new TextView(activity.getActivity());
            b0.setTypeface(externalFont);
            b0.setText("Server error. Please inform Project Fanfare for tech support");
            b0.setPadding(20, 50, 12, 0);
            b0.setTextSize(24);*/
            Log.i("Leaderboard", "Server error. Object is null");
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

}
