package edu.cmu.etc.fanfare.playbook;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.StringTokenizer;

public class TreasureHuntFragment extends Fragment implements View.OnClickListener{

    public static int section;
    private View view;
    private boolean iswarm=false,iscold=false,isplant=false;
    private final String mEndpoint = "ws://" +
            BuildConfig.PLAYBOOK_TREASUREHUNT_API_HOST + ":" +
            BuildConfig.PLAYBOOK_TREASUREHUNT_API_PORT;
    final Handler timerHandler = new Handler();

    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            final ImageView runner = (ImageView)view.findViewById(R.id.runner);
            AsyncHttpClient.getDefaultInstance().websocket(mEndpoint, "my-protocol", new AsyncHttpClient.WebSocketConnectCallback() {
                @Override
                public void onCompleted(Exception ex, WebSocket webSocket) {
                    if (ex != null) {
                        ex.printStackTrace();
                        return;
                    }
                    final JSONObject obj= new JSONObject();
                    try {
                        obj.put("section",section);
                        obj.put("selection",3);
                        obj.put("method","get");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }

                    webSocket.send(obj.toString());
                    webSocket.setStringCallback(new WebSocket.StringCallback() {
                        public void onStringAvailable(String s) {
                           if(s!=null)
                           {
                               int x=0,y=0,z=0;
                               StringTokenizer st = new StringTokenizer(s);
                               while (st.hasMoreTokens()) {
                                    x = Integer.valueOf(st.nextToken());
                                    y = Integer.valueOf(st.nextToken());
                                    z = Integer.valueOf(st.nextToken());
                               }
                               Log.d("aggregate",Integer.toString(x)+ " "+Integer.toString(y)+" "+Integer.toString(z));
                               if(x >=y && x>=z)
                               {
                                   Log.d("max","warm");
                                   iswarm=true;
                               }
                               else if(y >z && y>z)
                               {
                                   Log.d("max","cold");
                                   iscold=true;
                               }
                               else
                               {
                                   Log.d("max","plant");
                                   isplant=true;
                               }

                           }
                        }
                    });

                }
            });
            if(iswarm)
            {
                runner.setImageResource(R.drawable.runnerwarm);
                view.invalidate();
                iswarm=false;
            }
            else if(iscold)
            {
                runner.setImageResource(R.drawable.runnercold);
                view.invalidate();
                iscold=false;
            }
            else
            {
                runner.setImageResource(R.drawable.runnerplant);
                view.invalidate();
                isplant=false;
            }
            timerHandler.postDelayed(this, 1000);
        }
    };
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view=inflater.inflate(R.layout.treasurehunt_fragment, container, false);

        //create a list box to enter section

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add("1");
        arrayAdapter.add("2");
        arrayAdapter.add("3");
        arrayAdapter.add("4");

        // 2. Chain together various setter methods to set the dialog characteristics
        //builder.setMessage(R.string.dialog_message1)
        builder.setTitle("Select Section");
        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                section = which;

                ImageView image= (ImageView)view.findViewById(R.id.map);
                switch(section)
                {
                    case 1:
                        image.setImageResource(R.drawable.map2);

                        break;
                    case 2:
                        image.setImageResource(R.drawable.map2);
                        break;
                    case 3:
                        image.setImageResource(R.drawable.map2);
                        break;
                    case 4:
                        image.setImageResource(R.drawable.map2);
                        break;
                }

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();


        ImageView button_w= (ImageView)view.findViewById(R.id.warmer);
        button_w.setOnClickListener(this);
        ImageView button_c= (ImageView)view.findViewById(R.id.colder);
        button_c.setOnClickListener(this);
        ImageView button_p= (ImageView)view.findViewById(R.id.plant);
        button_p.setOnClickListener(this);

        timerHandler.postDelayed(timerRunnable,0);
        return view;

    }
    public View onResumeView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        timerHandler.postDelayed(timerRunnable,0);
        return view;
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.warmer:
                try
                {
                    final JSONObject obj= new JSONObject();
                    obj.put("section",section);
                    obj.put("selection",0);
                    obj.put("method","post");
                    AsyncHttpClient.getDefaultInstance().websocket(mEndpoint, "my-protocol", new AsyncHttpClient.WebSocketConnectCallback() {
                        @Override
                        public void onCompleted(Exception ex, WebSocket webSocket) {
                            if (ex != null) {
                                ex.printStackTrace();
                                return;
                            }
                            webSocket.send(obj.toString());

                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.colder:
                try
                {
                    final JSONObject obj= new JSONObject();
                    obj.put("section",section);
                    obj.put("selection",1);
                    obj.put("method","post");
                    AsyncHttpClient.getDefaultInstance().websocket(mEndpoint, "my-protocol", new AsyncHttpClient.WebSocketConnectCallback() {
                        @Override
                        public void onCompleted(Exception ex, WebSocket webSocket) {
                            if (ex != null) {
                                ex.printStackTrace();
                                return;
                            }
                            webSocket.send(obj.toString());

                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.plant:
                try
                {
                    final JSONObject obj= new JSONObject();
                    obj.put("section",section);
                    obj.put("selection",2);
                    obj.put("method","post");
                    AsyncHttpClient.getDefaultInstance().websocket(mEndpoint, "my-protocol", new AsyncHttpClient.WebSocketConnectCallback() {
                        @Override
                        public void onCompleted(Exception ex, WebSocket webSocket) {
                            if (ex != null) {
                                ex.printStackTrace();
                                return;
                            }
                            webSocket.send(obj.toString());

                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
        }

    }

}
