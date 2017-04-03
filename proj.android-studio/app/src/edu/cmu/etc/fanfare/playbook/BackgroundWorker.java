package edu.cmu.etc.fanfare.playbook;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;


import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.WebSocket;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Created by ramya on 3/7/17.
 */

public class  BackgroundWorker extends AsyncTask<String,Void,String> {
    private static final String TAG = BackgroundWorker.class.getSimpleName();
    private int param;
    //public LeaderboardActivity activity;
    private final String urlStringSection = "http://" +
            BuildConfig.PLAYBOOK_SECTION_API_HOST + ":" +
            BuildConfig.PLAYBOOK_SECTION_API_PORT + "/" +
            BuildConfig.PLAYBOOK_SECTION_APP;

    BackgroundWorker(int section)
{
    this.param = section;
}
    @Override

    protected String doInBackground(String... params) {
        String result="";
       if (params[0].equals("section")) {
            try {
                String sec=Integer.toString(param);
                Log.v("sec",Integer.toString(param));
                //String move = "2";
                //URL url = new URL("http://10.0.2.2:8080/events");
                URL url = new URL(urlStringSection);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                JSONObject object = new JSONObject();
                object.put("id", param);
                object.put("userId", LoginActivity.acct.getId());
                object.put("userName", LoginActivity.acct.getGivenName() + " " + LoginActivity.acct.getFamilyName());
                Log.d("test", object.toString());
               // String post_data = URLEncoder.encode("sectionNo","UTF-8")+"="+URLEncoder.encode(sec,"UTF-8")+"&"
                    //    + URLEncoder.encode("Move","UTF-8")+"="+URLEncoder.encode(move,"UTF-8");
                bufferedWriter.write(object.toString());
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                Log.d(TAG, "Response code: " + httpURLConnection.getResponseCode());

                try {
                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"utf-8"));
                    // String result="";
                    String line="";
                    while((line = bufferedReader.readLine())!= null) {
                        result += line;
                    }
                    Log.d(TAG, "Result: " + result);
                    bufferedReader.close();
                    inputStream.close();
                } catch (IOException e) {
                    InputStream errorStream = httpURLConnection.getErrorStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
                    String errorResult = "";
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        errorResult += line;
                    }
                    Log.d(TAG, "Server returned error: " + errorResult);
                    reader.close();
                    errorStream.close();
                }

                httpURLConnection.disconnect();
            } catch (IOException | JSONException e) {
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
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

}

