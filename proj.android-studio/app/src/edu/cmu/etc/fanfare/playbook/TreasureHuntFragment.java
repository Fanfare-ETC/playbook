package edu.cmu.etc.fanfare.playbook;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.StringTokenizer;

public class TreasureHuntFragment extends Fragment implements View.OnClickListener{

    public static int section;
    public static View view;
    private boolean treasurehunt_live=false;

    private  String mEndpoint = "ws://" +
            BuildConfig.PLAYBOOK_TREASUREHUNT_API_HOST + ":" +
            BuildConfig.PLAYBOOK_TREASUREHUNT_API_PORT;
    //final Handler timerHandler = new Handler();

    /**Runnable timerRunnable = new Runnable() {

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
                        final JSONObject obj = new JSONObject();
                        try {
                            obj.put("section", section);
                            obj.put("selection", 3);
                            obj.put("method", "get");
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                        webSocket.send(obj.toString());
                        webSocket.setStringCallback(new WebSocket.StringCallback() {
                            public void onStringAvailable(String s) {
                                if (s != null) {
                                    int x = 0, y = 0, z = 0;
                                    StringTokenizer st = new StringTokenizer(s);
                                    while (st.hasMoreTokens()) {
                                        x = Integer.valueOf(st.nextToken());
                                        y = Integer.valueOf(st.nextToken());
                                        z = Integer.valueOf(st.nextToken());
                                    }
                                    Log.d("aggregate", Integer.toString(x) + " " + Integer.toString(y) + " " + Integer.toString(z));
                                    if (x >= y && x >= z) {
                                        Log.d("max", "warm");
                                        iswarm = true;
                                    } else if (y > z && y > z) {
                                        Log.d("max", "cold");
                                        iscold = true;
                                    } else {
                                        Log.d("max", "plant");
                                        isplant = true;
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
            else if(isplant)
            {
                runner.setImageResource(R.drawable.runnerplant);
                view.invalidate();
                isplant=false;
            }
            else{}
            timerHandler.postDelayed(this, 1000);
        }
    };
  **/
    /**
     * A custom view to draw lines between the runner and the individual
     * buttons on screen.
     */
    public static class LinesView extends View {
        private final int mWarmerColor = Color.rgb(192, 55, 41);
        private final int mColderColor = Color.rgb(30, 48, 98);
        private final int mPlantColor = Color.rgb(255, 195, 13);

        private final Paint mWarmerPaint = new Paint();
        private final Paint mColderPaint = new Paint();
        private final Paint mPlantPaint = new Paint();

        private View mRunnerView;
        private View mWarmerView;
        private View mColderView;
        private View mPlantView;


        public static int[] runnerLocation = new int[2];
        public static int[] warmerLocation = new int[2];
        public static int[] colderLocation = new int[2];
        public static int[] plantLocation = new int[2];
        public static int[] canvasLocation = new int[2];

        public LinesView(Context context) {
            super(context);
            initPaints();
        }

        public LinesView(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            initPaints();
        }

        private void initPaints() {
            mWarmerPaint.setColor(mWarmerColor);
            mColderPaint.setColor(mColderColor);
            mPlantPaint.setColor(mPlantColor);

            // Make use of display metrics to scale the stroke width appropriately.
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            float strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6.0f, metrics);
            mWarmerPaint.setStrokeWidth(strokeWidth);
            mColderPaint.setStrokeWidth(strokeWidth);
            mPlantPaint.setStrokeWidth(strokeWidth);

            mWarmerPaint.setAntiAlias(true);
            mColderPaint.setAntiAlias(true);
            mPlantPaint.setAntiAlias(true);
        }

        public void setRunnerView(View runnerView) {
            mRunnerView = runnerView;
        }

        public void setWarmerView(View warmerView) {
            mWarmerView = warmerView;
        }

        public void setColderView(View colderView) {
            mColderView = colderView;
        }

        public void setPlantView(View plantView) {
            mPlantView = plantView;
        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // Draw lines only when everything is set up.
            if (mRunnerView == null || mWarmerView == null || mColderView == null ||
                    mPlantView == null) {
                return;
            }


            mRunnerView.getLocationInWindow(runnerLocation);
            mWarmerView.getLocationInWindow(warmerLocation);
            mColderView.getLocationInWindow(colderLocation);
            mPlantView.getLocationInWindow(plantLocation);
            this.getLocationInWindow(canvasLocation);

            // Compute the midpoints of these locations.
            runnerLocation[0] = runnerLocation[0] + mRunnerView.getWidth() / 2 - canvasLocation[0];
            runnerLocation[1] = runnerLocation[1] + mRunnerView.getHeight() / 2 - canvasLocation[1];
            warmerLocation[0] = warmerLocation[0] + mWarmerView.getWidth() / 2 - canvasLocation[0];
            warmerLocation[1] = warmerLocation[1] + mWarmerView.getHeight() / 2 - canvasLocation[1];
            colderLocation[0] = colderLocation[0] + mColderView.getWidth() / 2 - canvasLocation[0];
            colderLocation[1] = colderLocation[1] + mColderView.getHeight() / 2 - canvasLocation[1];
            plantLocation[0] = plantLocation[0] + mPlantView.getWidth() / 2 - canvasLocation[0];
            plantLocation[1] = plantLocation[1] + mPlantView.getHeight() / 2 - canvasLocation[1];

            // Set up the paints and draw the lines.
            canvas.drawLine(colderLocation[0], colderLocation[1], colderLocation[0], colderLocation[1]-400, mColderPaint);
            canvas.drawLine(warmerLocation[0], warmerLocation[1],warmerLocation[0], warmerLocation[1]-400, mWarmerPaint);
            canvas.drawLine(plantLocation[0], plantLocation[1], runnerLocation[0], runnerLocation[1], mPlantPaint);

            mWarmerPaint.setStrokeWidth(10);
            mWarmerPaint.setStyle(Paint.Style.STROKE);
            // draw a red bucket
            canvas.drawLine(warmerLocation[0]-150, warmerLocation[1]-400, warmerLocation[0]+ 150, warmerLocation[1]-400, mWarmerPaint); //bottom horizontal
            canvas.drawLine(warmerLocation[0]-150, warmerLocation[1]-400, warmerLocation[0]-150, warmerLocation[1]-600, mWarmerPaint); //left vertical
            canvas.drawLine(warmerLocation[0]-150, warmerLocation[1]-600, warmerLocation[0]+150, warmerLocation[1]-600, mWarmerPaint); //top horizontal
            canvas.drawLine( warmerLocation[0]+150, warmerLocation[1]-600, warmerLocation[0]+ 150, warmerLocation[1]-400, mWarmerPaint); //right horizontal

            mColderPaint.setStrokeWidth(10);
            mColderPaint.setStyle(Paint.Style.STROKE);
            // draw a blue bucket
            canvas.drawLine(colderLocation[0]-150, colderLocation[1]-400, colderLocation[0]+ 150, colderLocation[1]-400,mColderPaint); //bottom horizontal
            canvas.drawLine(colderLocation[0]-150, colderLocation[1]-400, colderLocation[0]-150, colderLocation[1]-600, mColderPaint); //left vertical
            canvas.drawLine(colderLocation[0]-150, colderLocation[1]-600, colderLocation[0]+150, colderLocation[1]-600, mColderPaint); //top horizontal
            canvas.drawLine(colderLocation[0]+150, colderLocation[1]-600, colderLocation[0]+ 150, colderLocation[1]-400, mColderPaint); //right horizontal

            mPlantPaint.setStrokeWidth(10);
            mPlantPaint.setStyle(Paint.Style.STROKE);
            // draw a yellow bucket
            runnerLocation[1] = runnerLocation[1] + 400;
            canvas.drawLine(runnerLocation[0]-150, runnerLocation[1]-400, runnerLocation[0]+ 150, runnerLocation[1]-400,mPlantPaint); //bottom horizontal
            canvas.drawLine(runnerLocation[0]-150, runnerLocation[1]-400, runnerLocation[0]-150, runnerLocation[1]-600, mPlantPaint); //left vertical
            canvas.drawLine(runnerLocation[0]-150, runnerLocation[1]-600, runnerLocation[0]+150, runnerLocation[1]-600, mPlantPaint); //top horizontal
            canvas.drawLine(runnerLocation[0]+150, runnerLocation[1]-600, runnerLocation[0]+ 150, runnerLocation[1]-400, mPlantPaint); //right horizontal

        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view=inflater.inflate(R.layout.treasurehunt_fragment, container, false);

        SharedPreferences settings = this.getContext().getSharedPreferences("FANFARE_SHARED", 0);
        section = settings.getInt("section", 0)-1;
        ImageView marker0= (ImageView)view.findViewById(R.id.marker0);
        ImageView marker1= (ImageView)view.findViewById(R.id.marker1);
        if(section==0)
        {
            marker0.setVisibility(View.VISIBLE);
            marker1.setVisibility(View.INVISIBLE);
        }
        else if(section==1)
        {
            marker0.setVisibility(View.INVISIBLE);
            marker1.setVisibility(View.VISIBLE);
        }

        else{}

        ImageView button_w= (ImageView)view.findViewById(R.id.warmer);
        button_w.setOnClickListener(this);
        ImageView button_c= (ImageView)view.findViewById(R.id.colder);
        button_c.setOnClickListener(this);
        ImageView button_p= (ImageView)view.findViewById(R.id.plant);
        button_p.setOnClickListener(this);

       //timerHandler.postDelayed(timerRunnable,0);

        // Dynamically draw lines.
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // We only want to know that layout happened for the first time.
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Create a new view for the lines and draw them.
                LinesView linesView = (LinesView) view.findViewById(R.id.linesView);
                linesView.setRunnerView(view.findViewById(R.id.runner));
                linesView.setWarmerView(view.findViewById(R.id.warmer));
                linesView.setColderView(view.findViewById(R.id.colder));
                linesView.setPlantView(view.findViewById(R.id.plant));
                linesView.invalidate();
            }
        });
        mEndpoint="ws://128.2.238.137:9000";
        Log.d("url",mEndpoint);

                AsyncHttpClient.getDefaultInstance().websocket(mEndpoint, "my-protocol", new AsyncHttpClient.WebSocketConnectCallback() {
                    @Override
                    public void onCompleted(Exception ex, WebSocket webSocket) {
                        if (ex != null) {
                            ex.printStackTrace();
                            return;
                        }
                        webSocket.setStringCallback(new WebSocket.StringCallback() {
                            public void onStringAvailable(String s) {
                                if (s != null) {
                                    Log.d("signal",s);
                                    if(s.equals("start"))
                                    {
                                        treasurehunt_live=true;
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ImageView translucent = (ImageView) view.findViewById(R.id.translucentlayer);
                                                translucent.setVisibility(View.INVISIBLE);
                                                ImageView tutorial = (ImageView) view.findViewById(R.id.treasurehunt_tutorial);
                                                ObjectAnimator blink_tut = ObjectAnimator.ofFloat(tutorial, "alpha", 0.5f, 0.0f);
                                                blink_tut.setDuration(500);
                                                blink_tut.start();
                                            }
                                        });
                                    }
                                    else if(s.equals("stop"))
                                    {
                                        treasurehunt_live=false;
                                    }
                                    else if(s.equals("plus10warmer"))
                                    {

                                    }
                                    else if(s.equals("plus10colder"))
                                    {

                                    }
                                    else if(s.equals("plus10plant"))
                                    {

                                    }
                                    else{}

                                }
                            }
                        });
                    }
                });


        return view;

    }
    public View onResumeView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        //timerHandler.postDelayed(timerRunnable,0);
        return view;
    }
    public void onClick(View v) {

        ImageView translucent = (ImageView) view.findViewById(R.id.translucentlayer);
        translucent.setVisibility(View.INVISIBLE);

        final JSONObject obj= new JSONObject();

        ImageView ball_w = (ImageView)TreasureHuntFragment.view.findViewById(R.id.ball_w);

        ObjectAnimator blink_w = ObjectAnimator.ofFloat(ball_w, "alpha", 1.0f, 0.0f);
        blink_w.setDuration(500);

        ObjectAnimator animX0 = ObjectAnimator.ofFloat(ball_w, "x", LinesView.warmerLocation[0], LinesView.runnerLocation[0]);
        ObjectAnimator animY0 = ObjectAnimator.ofFloat(ball_w, "y", LinesView.warmerLocation[1], LinesView.runnerLocation[1]);
        AnimatorSet animSetXY0 = new AnimatorSet();
        animSetXY0.setDuration(500);

        ImageView ball_c = (ImageView)TreasureHuntFragment.view.findViewById(R.id.ball_c);

        ObjectAnimator blink_c = ObjectAnimator.ofFloat(ball_c, "alpha", 1.0f, 0.0f);
        blink_c.setDuration(500);

        ObjectAnimator animX1 = ObjectAnimator.ofFloat(ball_c, "x", LinesView.colderLocation[0], LinesView.runnerLocation[0]);
        ObjectAnimator animY1 = ObjectAnimator.ofFloat(ball_c, "y", LinesView.colderLocation[1], LinesView.runnerLocation[1]);
        AnimatorSet animSetXY1 = new AnimatorSet();
        animSetXY1.setDuration(500);


        ImageView ball_p = (ImageView)TreasureHuntFragment.view.findViewById(R.id.ball_p);

        ObjectAnimator blink_p = ObjectAnimator.ofFloat(ball_p, "alpha", 1.0f, 0.0f);
        blink_p.setDuration(500);

        ObjectAnimator animX2 = ObjectAnimator.ofFloat(ball_p, "x", LinesView.plantLocation[0], LinesView.runnerLocation[0]);
        ObjectAnimator animY2 = ObjectAnimator.ofFloat(ball_p, "y", LinesView.plantLocation[1], LinesView.runnerLocation[1]);
        AnimatorSet animSetXY2 = new AnimatorSet();
        animSetXY2.setDuration(500);

        switch (v.getId()) {

            case R.id.warmer:
                    try {
                        obj.put("section", section);
                        obj.put("selection", 0);
                        obj.put("method", "post");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //animate ball from warmer to runner
                    ball_w.setVisibility(View.VISIBLE);
                    animSetXY0.play(animX0).with(animY0).with(blink_w);
                    animSetXY0.start();

                    if(!animSetXY0.isRunning()) ball_w.setVisibility(View.INVISIBLE);

                break;
            case R.id.colder:

                    try {
                        obj.put("section", section);
                        obj.put("selection", 1);
                        obj.put("method", "post");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //animate ball from colder to runner
                    ball_c.setVisibility(View.VISIBLE);
                    animSetXY1.play(animX1).with(animY1).with(blink_c);
                    animSetXY1.start();

                    if(!animSetXY1.isRunning()) ball_c.setVisibility(View.INVISIBLE);

                break;
            case R.id.plant:

                    try {
                        obj.put("section", section);
                        obj.put("selection", 2);
                        obj.put("method", "post");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //animate ball from plant to runner
                    ball_p.setVisibility(View.VISIBLE);
                    animSetXY2.play(animX2).with(animY2).with(blink_p);
                    animSetXY2.start();

                    if(!animSetXY2.isRunning()) ball_p.setVisibility(View.INVISIBLE);

                break;
        }
        if(treasurehunt_live) {
            Log.d("sending",obj.toString());
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
        }

    }

}
