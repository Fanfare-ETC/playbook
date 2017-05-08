package edu.cmu.etc.fanfare.playbook;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.test.suitebuilder.TestMethod;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Background worker for the leaderboard screen
 * Connects to the server and retrieves info
 * of top 10 players in a separate thread
 * Then display the data to UI thread
 * Created by yqi1 on 3/23/2017.
 */

public class LeaderboardWorker extends AsyncTask<String,Void,String> {

    //current fragment is passed in to get the necessary context
    public LeaderboardFragment activity;
    //constructor
    LeaderboardWorker(LeaderboardFragment activity)
    {
        this.activity = activity;
    }

            //url for rank according to total score
    private final String urlStringLeader = "http://" +
            BuildConfig.PLAYBOOK_SECTION_API_HOST + ":" +
            BuildConfig.PLAYBOOK_SECTION_API_PORT + "/" +
            BuildConfig.PLAYBOOK_LEADER_APP;
            //url for rank according to prediction score
    private final String urlStringLeaderP = "http://" +
            BuildConfig.PLAYBOOK_SECTION_API_HOST + ":" +
            BuildConfig.PLAYBOOK_SECTION_API_PORT + "/" +
            BuildConfig.PLAYBOOK_LEADERP_APP;
            //url for rank according to collection score
    private final String urlStringLeaderC = "http://" +
            BuildConfig.PLAYBOOK_SECTION_API_HOST + ":" +
            BuildConfig.PLAYBOOK_SECTION_API_PORT + "/" +
            BuildConfig.PLAYBOOK_LEADERC_APP;
    //url for rank according to badge
    private final String urlStringLeaderB = "http://" +
            BuildConfig.PLAYBOOK_SECTION_API_HOST + ":" +
            BuildConfig.PLAYBOOK_SECTION_API_PORT + "/" +
            BuildConfig.PLAYBOOK_LEADERB_APP;
            //flag to decide on ranking criteria
            //0 order by total, 1 order by prediction, 2 order by collection, 3 order by badge
    private int flag = 0;

    /*An instance of Leaders captures all the data of one player*/
    public class Leaders {
        String rank;
        String name;
        int predictionScore;
        int collectionScore;
        int totalScore;
        int badge;
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

    /*An instance of LeaderboardAdapter enables populating data to an item in listview*/
    public class LeaderboardAdapter extends ArrayAdapter<Leaders> {
        /*An instance of ViewHolder stores all the views of a listed item in the layout*/
        private class ViewHolder {
            TextView mRank;
            TextView mName;
            TextView mPrediction, mCollection;
            ImageView mBadge;
            Button mArrow, mPredictArrow, mCollectArrow;
            HorizontalScrollView scrollView, scrollView1, scrollView2;
        }
        /*constructor*/
        public LeaderboardAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Leaders> objects) {
            super(context, resource, objects);
        }

        @Override
        /*In this function the info is assigned to each view of the listed item
        * according to the position in the listview
        * so the data is populated through the list
        * and the list containers are reused*/
        public android.view.View getView(int position, View convertView, ViewGroup parent) {
            final Leaders leader = getItem(position);
            final LeaderboardAdapter.ViewHolder viewHolder;
            //find all the views in the layout
            if (convertView == null) {
                viewHolder = new LeaderboardAdapter.ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(activity.getActivity());
                convertView = inflater.inflate(R.layout.leaderboard_list_item, parent, false);
                viewHolder.mRank = (TextView) convertView.findViewById(R.id.rank);
                viewHolder.mName = (TextView) convertView.findViewById(R.id.name);
                viewHolder.mPrediction = (TextView) convertView.findViewById(R.id.predictionContent);
                viewHolder.mCollection = (TextView) convertView.findViewById(R.id.collectionContent);
                viewHolder.mBadge = (ImageView) convertView.findViewById(R.id.total);
                viewHolder.mArrow = (Button) convertView.findViewById(R.id.name_arrow);
                viewHolder.mCollectArrow = (Button) convertView.findViewById(R.id.collect_arrow);
                viewHolder.mPredictArrow = (Button) convertView.findViewById(R.id.predict_arrow);
                viewHolder.scrollView = (HorizontalScrollView) convertView.findViewById(R.id.nameScroll);
                viewHolder.scrollView1 = (HorizontalScrollView) convertView.findViewById(R.id.prediction);
                viewHolder.scrollView2 = (HorizontalScrollView) convertView.findViewById(R.id.collection);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (LeaderboardAdapter.ViewHolder) convertView.getTag();
            }

            // Populate data from leader.
            //font for the leaderboard
            Typeface fontCat = Typeface.createFromAsset(activity.getActivity().getAssets(), "fonts/nova2.ttf");

            /*Log.i("LEAD_LIST", "Name" + leader.name +
                    ", Prediction" + leader.predictionScore +
                    ", Collection" + leader.collectionScore +
                    ", Total" + leader.totalScore +
                    ", Badge" + leader.badge);*/

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

            TextPaint paint = new TextPaint();
            paint.setTextSize(20);
            float width = paint.measureText(leader.name);
            Log.i("Text Width", "For Name " + leader.name);
            Log.i("Text Width", Float.toString(width));

            //activity.getView().findViewById(R.id.nameScroll).measure();
            float nameWidth = viewHolder.mName.getMeasuredWidth();

            Log.i("Text_Width", "Wrap content " + Float.toString(nameWidth));

            float viewWidth = convertView.getMeasuredWidth();

            Log.i("View_Width", "For View? " + Float.toString(viewWidth));


            //final HorizontalScrollView scrollView = (HorizontalScrollView)convertView.findViewById(R.id.nameScroll);
            ViewTreeObserver observer = viewHolder.scrollView.getViewTreeObserver();
            myOnGlobalLayoutListener listener = new myOnGlobalLayoutListener(observer, viewHolder.scrollView, viewHolder.mArrow, leader.name);
            observer.addOnPreDrawListener(listener);

           /* ViewTreeObserver observer = viewHolder.scrollView.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int viewWidth = scrollView.getMeasuredWidth();
                    int contentWidth = scrollView.getChildAt(0).getWidth();
                    Log.i("LEADERBOARD", "view width " + Integer.toString(viewWidth));
                    Log.i("LEADERBOARD", "content width " + Integer.toString(contentWidth));
                    if(viewWidth - contentWidth > 0) {
                        // not scrollable
                        Log.i("SCROLL", leader.name + "seems not scrollable");
                        viewHolder.mArrow.setVisibility(View.INVISIBLE);

                    }
                }
            });*/
            viewHolder.mArrow.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){

                    Log.i("LEADERBOARD", "Button Clicked");
                    //scrollView.scrollTo((int)scrollView.getScrollX() + 20, (int)scrollView.getScrollY());
                    viewHolder.scrollView.fullScroll(View.FOCUS_RIGHT);
                }
            });

            //adjust the font size of the scores for large numbers to fit in
            //might want to do this by using scroll bars in the future
            int scoreSize = 20;
            /*if(leader.predictionScore > 999 | leader.collectionScore > 999 | leader.totalScore > 999){
                if(leader.predictionScore>9999 | leader.collectionScore> 9999 | leader.totalScore > 9999){
                    scoreSize = 12;
                }
                else{
                    scoreSize = 16;
                }

            }
*/
            //need to change the font color into yellow if it is the ranking criteria
            //by default is total scores
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

            /*final HorizontalScrollView scrollView1 = (HorizontalScrollView)convertView.findViewById(R.id.prediction);
            ViewTreeObserver observer1 = scrollView1.getViewTreeObserver();
            observer1.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int viewWidth = scrollView1.getMeasuredWidth();
                    int contentWidth = scrollView1.getChildAt(0).getWidth();
                    Log.i("LEADERBOARD", "Predict view width " + Integer.toString(viewWidth));
                    Log.i("LEADERBOARD", "Predict Content width " + Integer.toString(contentWidth));
                    if(viewWidth - contentWidth >= 0) {
                        // not scrollable
                        Log.i("SCROLL", leader.predictionScore + " seems not scrollable");
                        viewHolder.mPredictArrow.setVisibility(View.INVISIBLE);

                    }
                }
            });
*/
            ViewTreeObserver observer1 = viewHolder.scrollView.getViewTreeObserver();
            myOnGlobalLayoutListener listener1 = new myOnGlobalLayoutListener(observer1, viewHolder.scrollView1, viewHolder.mPredictArrow, Integer.toString(leader.predictionScore));
            observer1.addOnPreDrawListener(listener1);

            viewHolder.mPredictArrow.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){

                    Log.i("LEADERBOARD", "Button 1 Clicked");
                    //scrollView1.scrollTo((int)scrollView1.getScrollX() + 20, (int)scrollView1.getScrollY());
                    viewHolder.scrollView1.fullScroll(View.FOCUS_RIGHT);
                }
            });


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

            /*final HorizontalScrollView scrollView2 = (HorizontalScrollView)convertView.findViewById(R.id.collection);
            ViewTreeObserver observer2 = scrollView2.getViewTreeObserver();
            observer2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int viewWidth = scrollView2.getMeasuredWidth();
                    int contentWidth = scrollView2.getChildAt(0).getWidth();
                    Log.i("LEADERBOARD", "Predict view width " + Integer.toString(viewWidth));
                    Log.i("LEADERBOARD", "Predict Content width " + Integer.toString(contentWidth));
                    if(viewWidth - contentWidth >= 0) {
                        // not scrollable
                        Log.i("SCROLL", leader.collectionScore + " seems not scrollable");
                        viewHolder.mCollectArrow.setVisibility(View.INVISIBLE);

                    }
                }
            });
*/
            ViewTreeObserver observer2 = viewHolder.scrollView2.getViewTreeObserver();
            myOnGlobalLayoutListener listener2 = new myOnGlobalLayoutListener(observer, viewHolder.scrollView2, viewHolder.mCollectArrow, Integer.toString(leader.collectionScore));
            observer2.addOnPreDrawListener(listener2);

            viewHolder.mCollectArrow.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){

                    Log.i("LEADERBOARD", "Button 2 Clicked");
                    //scrollView2.scrollTo(scrollView2.getScrollX() + 20, scrollView2.getScrollY());
                    viewHolder.scrollView2.fullScroll(View.FOCUS_RIGHT);
                }
            });

            //display the badge
            if(leader.badge == 1){
                if(flag == 3){
                    viewHolder.mBadge.setImageResource(R.drawable.badge_yellow);
                }
                else {
                    viewHolder.mBadge.setImageResource(R.drawable.badge_white);
                }

            }

            return convertView;
        }

    }


    @Override
    /*do on background thread to retrieve the player data*/
    protected String doInBackground(String... params) {
        String result = null;

        //default ranking by total score
        if (params[0].equals("0")) {
            try {
                flag = 0;

                URL url = new URL(urlStringLeader);
                Log.i("TEST_URL", "String URL: " + url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("GET");

                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setRequestProperty("Content-type", "application/json");

                int responseCode = httpURLConnection.getResponseCode();
                if(responseCode != 200)
                    return null;
                Log.d("HTTP", "Leaderboard Response code: " + responseCode);
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));

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

        //ranking by prediction score
        else if (params[0].equals("1")) {
            try {
                flag = 1;

                URL url = new URL(urlStringLeaderP);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("GET");

                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setRequestProperty("Content-type", "application/json");

                int responseCode = httpURLConnection.getResponseCode();
                if(responseCode != 200)
                    return null;
                Log.d("HTTP", "LeaderboardP Response code: " + responseCode);
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));

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

        //ranking by collection score
        else if (params[0].equals("2")) {
            try {
                flag = 2;

                URL url = new URL(urlStringLeaderC);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("GET");

                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setRequestProperty("Content-type", "application/json");

                int responseCode = httpURLConnection.getResponseCode();
                Log.d("HTTP", "LeaderboardC Response code: " + responseCode);
                if(responseCode != 200){
                    return null;
                }

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));

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

        //ranking by badge
        else if (params[0].equals("3")) {
            try {
                flag = 3;

                URL url = new URL(urlStringLeaderB);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("GET");

                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setRequestProperty("Content-type", "application/json");

                int responseCode = httpURLConnection.getResponseCode();
                Log.d("HTTP", "LeaderboardB Response code: " + responseCode);
                if(responseCode != 200){
                    return null;
                }

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));

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
        //nothing needs to be done before retrieving the data
    }

    @Override
    protected void onPostExecute(String result) {
        //after retrieving data, save them into an arraylist
        JSONArray jArray = null;
        if(result != null) {
            try {
                jArray = new JSONArray(result);
                List<Leaders> leaders = new ArrayList<Leaders>();
                for (int i = 0; i < jArray.length(); i++) {

                    Log.i("LEAD_TR", "Within the array iteration");
                    JSONObject json_data = null;
                    json_data = jArray.getJSONObject(i);

                    Leaders leader = new Leaders();
                    leader.rank = Integer.toString(i+1);
                    leader.name = json_data.getString("UserName");
                    leader.predictionScore = json_data.getInt("PredictionScore");
                    leader.collectionScore = json_data.getInt("CollectionScore");
                    leader.totalScore = json_data.getInt("Total");
                    leader.badge = json_data.getInt("Badge");

                    leaders.add(leader);
                }
                //create an adapter, and set it to the leaderboard listview
                LeaderboardAdapter leaderboardAdapter = new LeaderboardAdapter(activity.getActivity(), R.layout.leaderboard_list_item, leaders);
                ListView listView = (ListView) activity.getView().findViewById(R.id.leaderboardList);
                listView.setAdapter(leaderboardAdapter);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else{

           /* Typeface externalFont = Typeface.createFromAsset(activity.getActivity().getAssets(), "fonts/nova2.ttf");

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
        //nothing displayed during the background process
        super.onProgressUpdate(values);
    }

}
