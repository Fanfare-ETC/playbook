package edu.cmu.etc.fanfare.playbook;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class TreasureHuntFragment extends PlaybookFragment implements View.OnClickListener,View.OnTouchListener{


    private final JSONObject obj = new JSONObject();
    private final Exception callbackException[] = {null};
    private final WebSocket callbackWs[] = {null};
    private websocketHandler wsh = new websocketHandler();

    private static int section;
    private static View view;
    private ConstraintLayout layout;
    private int plusoneId,plustenWarmerId,plustenColderId,plustenMarkerId;
    private int warmerSectionId,colderSectionId,markerSectionId;
    private ImageView warmerView,colderView,markerView;
    private boolean flag=false;

    private  String mEndpoint = "ws://" +
            BuildConfig.PLAYBOOK_TREASUREHUNT_API_HOST + ":" +
            BuildConfig.PLAYBOOK_TREASUREHUNT_API_PORT;

    public static class gameState
    {
        public static boolean game_on=false;
        public static boolean flag1=false;
        public static boolean flag2=false;
        public static boolean flag3=false;
        public static boolean game_off=true;
    }

    public static class connectDots extends View {

        public connectDots(Context context) {
            super(context);
        }

        public connectDots(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        public void onDraw(Canvas canvas) {

            super.onDraw(canvas);

            int[] loc0 = new int[2];
            int[] loc1 = new int[2];
            int [] canvasloc = LinesView.canvasLocation;

            if(gameState.game_on) {
                ImageView v2 =(ImageView)view.findViewById(R.id.v2);
                ImageView v4 =(ImageView)view.findViewById(R.id.v4);
                ImageView v5 =(ImageView)view.findViewById(R.id.v5);

                int mColor = Color.rgb(255, 255, 255);
                Paint mPaint= new Paint();
                mPaint.setColor(mColor);
                mPaint.setAntiAlias(true);
                mPaint.setStrokeWidth(10);
                mPaint.setStyle(Paint.Style.FILL);

                //draw a circle at 2,4,5 vertices
                v2.getLocationInWindow(loc0);
                loc0[0] -= canvasloc[0];
                loc0[1] -= canvasloc[1];
                canvas.drawCircle(loc0[0], loc0[1], 15, mPaint);
                v4.getLocationInWindow(loc0);
                loc0[0] -= canvasloc[0];
                loc0[1] -= canvasloc[1];
                canvas.drawCircle(loc0[0], loc0[1], 15, mPaint);
                v5.getLocationInWindow(loc0);
                loc0[0] -= canvasloc[0];
                loc0[1] -= canvasloc[1];
                canvas.drawCircle(loc0[0], loc0[1], 15, mPaint);
            }


            if (gameState.flag1)
            {
                ImageView v0 =(ImageView)view.findViewById(R.id.v0);
                ImageView v5 =(ImageView)view.findViewById(R.id.v5);

                int mColor = Color.rgb(255, 255, 255);
                Paint mPaint= new Paint();
                mPaint.setColor(mColor);
                mPaint.setAntiAlias(true);
                mPaint.setStrokeWidth(10);
                mPaint.setStyle(Paint.Style.FILL);

                v0.getLocationInWindow(loc0);
                loc0[0] -= canvasloc[0];
                loc0[1] -= canvasloc[1];
                canvas.drawCircle(loc0[0], loc0[1], 15, mPaint);
                v5.getLocationInWindow(loc1);
                loc1[0] -= canvasloc[0];
                loc1[1] -= canvasloc[1];
                canvas.drawLine(loc0[0], loc0[1], loc1[0], loc1[1], mPaint);
            }

            if(gameState.flag2)
            {

                ImageView v2 =(ImageView)view.findViewById(R.id.v2);
                ImageView v3 =(ImageView)view.findViewById(R.id.v3);
                ImageView v4 =(ImageView)view.findViewById(R.id.v4);
                ImageView v5 =(ImageView)view.findViewById(R.id.v5);

                int mColor = Color.rgb(255, 255, 255);
                Paint mPaint= new Paint();
                mPaint.setColor(mColor);
                mPaint.setAntiAlias(true);
                mPaint.setStrokeWidth(10);
                mPaint.setStyle(Paint.Style.FILL);

                    v3.getLocationInWindow(loc0);
                    loc0[0] -= canvasloc[0];
                    loc0[1] -= canvasloc[1];
                    canvas.drawCircle(loc0[0],loc0[1],15,mPaint);

                    v2.getLocationInWindow(loc1);
                    loc1[0] -= canvasloc[0];
                    loc1[1] -= canvasloc[1];
                    canvas.drawLine(loc0[0], loc0[1], loc1[0], loc1[1], mPaint);

                    v4.getLocationInWindow(loc1);
                    loc1[0] -= canvasloc[0];
                    loc1[1] -= canvasloc[1];
                    canvas.drawLine(loc0[0], loc0[1], loc1[0], loc1[1], mPaint);

                    v5.getLocationInWindow(loc0);
                    loc0[0] -= canvasloc[0];
                    loc0[1] -= canvasloc[1];
                    canvas.drawLine(loc0[0], loc0[1], loc1[0], loc1[1], mPaint);
            }

            if(gameState.flag3) {

                ImageView v0 =(ImageView)view.findViewById(R.id.v0);
                ImageView v1 =(ImageView)view.findViewById(R.id.v1);
                ImageView v2 =(ImageView)view.findViewById(R.id.v2);

                int mColor = Color.rgb(255, 255, 255);
                Paint mPaint= new Paint();
                mPaint.setColor(mColor);
                mPaint.setAntiAlias(true);
                mPaint.setStrokeWidth(10);
                mPaint.setStyle(Paint.Style.FILL);
                        v1.getLocationInWindow(loc0);
                        loc0[0] -= canvasloc[0];
                        loc0[1] -= canvasloc[1];
                        canvas.drawCircle(loc0[0],loc0[1],15,mPaint);

                        v2.getLocationInWindow(loc1);
                        loc1[0] -= canvasloc[0];
                        loc1[1] -= canvasloc[1];
                        canvas.drawLine(loc0[0], loc0[1], loc1[0], loc1[1], mPaint);

                        v0.getLocationInWindow(loc1);
                        loc1[0] -= canvasloc[0];
                        loc1[1] -= canvasloc[1];
                        canvas.drawLine(loc0[0], loc0[1], loc1[0], loc1[1], mPaint);
            }
                    if(!gameState.flag1|| !gameState.flag2|| !gameState.flag3)
                    invalidate();

            }
    }
    public static class LinesView extends View {

        private View mWarmerView;
        private View mColderView;
        private View mMarkerView;

        public static int[] warmerLocation = new int[2];
        public static int[] colderLocation = new int[2];
        public static int[] markerLocation = new int[2];
        public static int[] canvasLocation = new int[2];

        public LinesView(Context context) {
            super(context);
        }

        public LinesView(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        public void setWarmerView(View warmerView) {
            mWarmerView = warmerView;
        }

        public void setColderView(View colderView) {
            mColderView = colderView;
        }

        public void setMarkerView(View markerView) {
            mMarkerView = markerView;
        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // Draw lines only when everything is set up.
            if ( mWarmerView == null || mColderView == null ||
                    mMarkerView == null) {
                return;
            }

            mWarmerView.getLocationInWindow(warmerLocation);
            mColderView.getLocationInWindow(colderLocation);
            mMarkerView.getLocationInWindow(markerLocation);
            this.getLocationInWindow(canvasLocation);

            // Compute the midpoints of these locations.
            warmerLocation[0] = warmerLocation[0] + mWarmerView.getWidth() / 2 - canvasLocation[0];
            warmerLocation[1] = warmerLocation[1] + mWarmerView.getHeight() / 2 - canvasLocation[1];
            colderLocation[0] = colderLocation[0] + mColderView.getWidth() / 2 - canvasLocation[0];
            colderLocation[1] = colderLocation[1] + mColderView.getHeight() / 2 - canvasLocation[1];
            markerLocation[0] = markerLocation[0] + mMarkerView.getWidth() / 2 - canvasLocation[0];
            markerLocation[1] = markerLocation[1] + mMarkerView.getHeight() / 2 - canvasLocation[1];
            /*
            for(int i =0;i<5;i++)
            {
                vertex.get(i).getLocationInWindow(loc0);
                vertex.get(i+1).getLocationInWindow(loc1);
                loc0[0] -= canvasLocation[0];
                loc0[1] -= canvasLocation[1];
                loc1[0] -= canvasLocation[0];
                loc1[1] -= canvasLocation[1];
                canvas.drawLine(loc0[0], loc0[1], loc1[0], loc1[1], mPaint);
            }
            */
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view=inflater.inflate(R.layout.treasurehunt_fragment, container, false);
        ImageView translucent = (ImageView) view.findViewById(R.id.translucentlayer);
        translucent.setVisibility(View.VISIBLE);

            SharedPreferences settings = this.getActivity().getSharedPreferences("FANFARE_SHARED", 0);
            section = settings.getInt("section", 0) - 1;

            layout = (ConstraintLayout) view.findViewById(R.id.treasurehunt_layout);
            plusoneId = R.drawable.plusone;
            plustenWarmerId = R.drawable.plusten_w;
            plustenColderId = R.drawable.plusten_c;
            plustenMarkerId = R.drawable.plusten_m;
            warmerSectionId = R.id.warmer;
            colderSectionId = R.id.colder;
            markerSectionId = R.id.marker;

            TextView warmer_text = (TextView) view.findViewById(R.id.warmer_text);
            Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/nova2.ttf");
            warmer_text.setTypeface(tf);
            warmer_text.setTextSize(24);
            warmer_text.setText("Getting\nWarmer!");

            TextView colder_text = (TextView) view.findViewById(R.id.colder_text);
            colder_text.setTypeface(tf);
            colder_text.setTextSize(24);
            colder_text.setText("Getting\nColder!");

            TextView plant_text = (TextView) view.findViewById(R.id.marker_text);
            plant_text.setTypeface(tf);
            plant_text.setTextSize(24);
            plant_text.setText("Drop the\nMarker!");

            warmerView = (ImageView) view.findViewById(warmerSectionId);
            warmerView.setOnClickListener(this);
            colderView = (ImageView) view.findViewById(colderSectionId);
            colderView.setOnClickListener(this);
            markerView = (ImageView) view.findViewById(markerSectionId);
            markerView.setOnClickListener(this);

            ImageView MapView = (ImageView) view.findViewById(R.id.map);
            MapView.setOnClickListener(this);
            ImageView agg = (ImageView) view.findViewById(R.id.aggregate);
            agg.setOnTouchListener(this);


            view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    LinesView linesView = (LinesView) view.findViewById(R.id.linesView);
                    linesView.setWarmerView(warmerView);
                    linesView.setColderView(colderView);
                    linesView.setMarkerView(markerView);
                    linesView.invalidate();
                }
            });

        mEndpoint="ws://128.2.238.137:9000";
        Future<WebSocket> webSocket= AsyncHttpClient.getDefaultInstance().websocket(mEndpoint, null, wsh);

        return view;

    }
    public View onResumeView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        return view;
    }
    public void processState(JSONObject jsonObject)
    {
        try {
            gameState.game_on=jsonObject.getBoolean("game_on");
            gameState.flag1=jsonObject.getBoolean("flag1");
            gameState.flag2=jsonObject.getBoolean("flag2");
            gameState.flag3=jsonObject.getBoolean("flag3");
            gameState.game_off=jsonObject.getBoolean("game_off");
            Log.d("tstate", Boolean.toString(gameState.game_on)+" 1: "+Boolean.toString(gameState.flag1)+" 2: "+Boolean.toString(gameState.flag2)+" 3: "+Boolean.toString(gameState.flag3)+" "+Boolean.toString(gameState.game_off));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(gameState.game_on)
                    startGame();
                if(gameState.flag1)
                    updateMarker_1();
                if(gameState.flag2)
                    updateMarker_2();
                if(gameState.flag3)
                    updateMarker_3();
                if(gameState.game_off)
                    stopGame();
                connectDots cd = (connectDots) view.findViewById(R.id.connectDots);
                cd.invalidate();
            }
        });
    }
    public void  updateMarker_1()
    {
        //move marker to vertex 3
        ImageView v3 = ((ImageView) view.findViewById(R.id.v3));
        int[] loc0 = new int[2];
        int[] canvasLocation = new int[2];
        view.getLocationInWindow(canvasLocation);
        ImageView ex = (ImageView) view.findViewById(R.id.ex);
        v3.getLocationInWindow(loc0);
        loc0[0] -= canvasLocation[0] + 50;
        loc0[1] -= canvasLocation[1] + 50;
        ex.setX(loc0[0]);
        ex.setY(loc0[1]);

    }
    public void  updateMarker_2()
    {
        //move marker to vertex 1
        ImageView v1 = ((ImageView) view.findViewById(R.id.v1));
        int[] loc0 = new int[2];
        int[] canvasLocation = new int[2];
        view.getLocationInWindow(canvasLocation);
        ImageView ex = (ImageView) view.findViewById(R.id.ex);
        v1.getLocationInWindow(loc0);
        loc0[0] -= canvasLocation[0] + 50;
        loc0[1] -= canvasLocation[1] + 50;
        ex.setX(loc0[0]);
        ex.setY(loc0[1]);

    }
    public void  updateMarker_3()
    {
        ImageView ex = (ImageView) view.findViewById(R.id.ex);
        ex.setVisibility(View.INVISIBLE);
    }
    public void startGame()
    {
        ImageView translucent = (ImageView) view.findViewById(R.id.translucentlayer);
        translucent.setVisibility(View.INVISIBLE);
        ImageView drawing= (ImageView)view.findViewById(R.id.drawing);
        drawing.setVisibility(View.INVISIBLE);
        //move marker to vertex 0
        ImageView v0 = ((ImageView) view.findViewById(R.id.v0));
        int[] loc0 = new int[2];
        int[] canvasLocation = new int[2];
        view.getLocationInWindow(canvasLocation);
        ImageView ex = (ImageView) view.findViewById(R.id.ex);
        ex.setVisibility(View.VISIBLE);
        v0.getLocationInWindow(loc0);
        loc0[0] -= canvasLocation[0] + 50;
        loc0[1] -= canvasLocation[1] + 50;
        ex.setX(loc0[0]);
        ex.setY(loc0[1]);

        /*
        //create a alert box with tutorial screen
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Tutorial");
        //alertDialog.setContentView(view.findViewById(R.id.treasurehunt_tutorial));
        //alertDialog.setView(view.findViewById(R.id.treasurehunt_tutorial));
        if(gameState.game_live) {
            Log.d("here","game live");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Continue!",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            flag=true;
                            dialog.cancel();
                            ImageView translucent = (ImageView) view.findViewById(R.id.translucentlayer);
                            translucent.setVisibility(View.INVISIBLE);
                            final TextView text = (TextView) view.findViewById(R.id.timer);
                            int mTimerColor = Color.rgb(0, 0, 0);
                            text.setTextColor(mTimerColor);
                            new CountDownTimer(5000, 1000) {

                                public void onTick(long millisUntilFinished) {
                                    long time = millisUntilFinished / 1000;
                                    if (time == 1)
                                        text.setText("GO!");
                                    else
                                        text.setText(" " + Long.toString(time - 1));
                                }

                                public void onFinish() {
                                    text.setText("");
                                }
                            }.start();
                        }
                    });
            if (!flag)alertDialog.show();
        }
        else {
            alertDialog.show();
        }*/

    }
    public void stopGame()
    {
        /*ImageView MapView= (ImageView)view.findViewById(R.id.map);
        ObjectAnimator flip_map = ObjectAnimator.ofFloat(MapView, "rotationY", 0, 90);
        flip_map.setDuration(1000);
        flip_map.start();*/
        ImageView ex = (ImageView) view.findViewById(R.id.ex);
        ex.setVisibility(View.INVISIBLE);
        ImageView drawing= (ImageView)view.findViewById(R.id.drawing);
        drawing.setVisibility(View.VISIBLE);
        ImageView translucent = (ImageView) view.findViewById(R.id.translucentlayer);
        translucent.setVisibility(View.VISIBLE);

    }
    public void plustenaimation(Vector<ImageView> plustens,Vector<ObjectAnimator> anim_plustens,int plustenId,int sectionId)
    {

        int[] location = new int[2];
        ImageView new_plusten= new ImageView(getActivity());
        new_plusten.setImageResource(plustenId);
        new_plusten.setVisibility(View.VISIBLE);
        ImageView section = (ImageView) view.findViewById(sectionId);
        section.getLocationInWindow(location);

        Random generator = new Random();
        int x = generator.nextInt(100)-70;
        int y = generator.nextInt(100)-70 ;

        new_plusten.setRotation(x*y);
        new_plusten.setX(location[0]+x);
        new_plusten.setY(location[1]+y);

        if(plustens.size()==5)
        {
            plustens.clear();
        }
        else {
            for(int j=0;j<plustens.size();j++) {
                layout.removeView(plustens.get(j));
            }
            plustens.add(new_plusten);
        }

        ObjectAnimator blink_plusten = ObjectAnimator.ofFloat(new_plusten, "alpha", 1.0f, 0.0f);
        blink_plusten.setDuration(1000);

        if(anim_plustens.size()==5)
        {
            anim_plustens.clear();
        }
        else {
            anim_plustens.add(blink_plusten);
        }

        //add animation to plusten
        for(int j=0;j<plustens.size();j++) {
            layout.addView(plustens.get(j));
            anim_plustens.get(j).start();
        }
    }
    public void plusoneanimation(int[] position,int[] offset)
    {
        final ImageView newone = new ImageView(getActivity());
        newone.setImageResource(plusoneId);
        newone.setVisibility(View.VISIBLE);
        newone.setX(position[0]-offset[0]);
        newone.setY(position[1]-offset[1]);
        layout.addView(newone);

        final ObjectAnimator anim_plusone_w = ObjectAnimator.ofFloat(newone, "y", position[1]-offset[1], position[1]-offset[1] - 500);
        anim_plusone_w.setDuration(500);
        anim_plusone_w.start();
        final ObjectAnimator blink_plusone_w = ObjectAnimator.ofFloat(newone, "alpha", 1.0f, 0.0f);
        blink_plusone_w.setDuration(500);
        blink_plusone_w.start();
        final Handler handler = new Handler();
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                anim_plusone_w.cancel();
                blink_plusone_w.cancel();
                newone.setAnimation(null);
                ((ViewGroup) newone.getParent()).removeView(newone);
                }
            };
        handler.postDelayed(task,500);

    }
    public void onClick(View v) {

        final JSONObject obj = new JSONObject();

        switch (v.getId()) {
            case R.id.warmer:
                try {
                    obj.put("selection", 0);
                    obj.put("method", "post");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                plusoneanimation(LinesView.warmerLocation,LinesView.canvasLocation);

                break;
            case R.id.colder:

                try {
                    obj.put("selection", 1);
                    obj.put("method", "post");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                plusoneanimation(LinesView.colderLocation,LinesView.canvasLocation);
                break;
            case R.id.marker:

                try {
                    obj.put("selection", 2);
                    obj.put("method", "post");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                plusoneanimation(LinesView.markerLocation,LinesView.canvasLocation);
                break;

        }
        callbackWs[0].send(obj.toString());

    }
public boolean onTouch(View v, MotionEvent event)
{
        switch (v.getId()) {
            case R.id.aggregate:
                if(event.getAction() == MotionEvent.ACTION_DOWN) {

                    float width = v.getWidth();
                    float tx = event.getX();
                    float ty = event.getY();

                    int[] values = new int[2];
                    v.getLocationInWindow(values);


                    if (tx <= width / 3) {

                        try {
                            obj.put("selection", 0);
                            obj.put("method", "post");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else if (tx >= width / 3 && tx <= width * 2 / 3) {

                        try {
                            obj.put("selection", 2);
                            obj.put("method", "post");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (tx >= 2 * width / 3 && tx <= width) {
                        try {
                            obj.put("selection", 1);
                            obj.put("method", "post");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    callbackWs[0].send(obj.toString());

                    int[] clickpos = new int[2];
                    clickpos[0]=(int)tx ;//- values[0];
                    clickpos[1]=(int)ty ;//- values[1];

                    plusoneanimation(clickpos,values);

                }
                break;
        }
    return true;
}

    public class websocketHandler implements AsyncHttpClient.WebSocketConnectCallback, WebSocket.StringCallback {

        public void onCompleted(Exception e, WebSocket webSocket) {
            if (e != null) {
                e.printStackTrace();
                return;
            }
            callbackException[0] = e;
            callbackWs[0] = webSocket;

            JSONObject o = new JSONObject();
            try {
                o.put("method", "getstate");
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            Log.d("tstate", "Asking server for state");
            webSocket.send(o.toString());
            webSocket.setStringCallback(this);
        }

        public void onStringAvailable(String s) {

            if (s != null) {
                if (s.contains("state")) {
                    Log.d("tstate", "Recieved state from server");
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        processState(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                else if(s.equals("plus10warmer"))
                {
                    final Vector<ImageView> plustens = new Vector<ImageView>(5);
                    final Vector<ObjectAnimator> anim_plustens= new Vector<ObjectAnimator>(5);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            plustenaimation(plustens,anim_plustens,plustenWarmerId,R.id.warmerSection);

                        }
                    });
                }
                else if(s.equals("plus10colder"))
                {

                    final Vector<ImageView> plustens = new Vector<ImageView>(5);
                    final Vector<ObjectAnimator> anim_plustens= new Vector<ObjectAnimator>(5);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            plustenaimation(plustens,anim_plustens,plustenColderId,R.id.colderSection);
                        }
                    });

                }
                else if(s.equals("plus10marker"))
                {

                    final Vector<ImageView> plustens = new Vector<ImageView>(5);
                    final Vector<ObjectAnimator> anim_plustens= new Vector<ObjectAnimator>(5);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            plustenaimation(plustens,anim_plustens,plustenMarkerId,R.id.markerSection);
                        }
                    });

                }

                else if (s.equals("flag1wrong")||s.equals("flag2wrong")||s.equals("flag3wrong"))
                {
                    Log.d("wrong","wrong");
                }
                else {

                    //section aggregate text display logic

                    if (gameState.game_on) {

                        final int w, m, c;
                        final List<String> num = Arrays.asList(s.split(","));
                        if(num.size()!=3) {w=0;m=0;c=0;}
                        else {
                            w = Integer.valueOf(num.get(0));
                            m = Integer.valueOf(num.get(1));
                            c = Integer.valueOf(num.get(2));
                        }
                        //Log.d("waggregate", Integer.toString(w) + " " + Integer.toString(m) + " " + Integer.toString(c));
                        if(getActivity()!=null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TextView text = (TextView) view.findViewById(R.id.aggregate_text);

                                    Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/nova2.ttf");
                                    text.setTypeface(tf);
                                    text.setTextSize(30);
                                    if (w > m && w > c) {
                                        text.setTextColor(Color.WHITE);
                                        text.setBackgroundColor(getResources().getColor(R.color.tertiary_dark));
                                        text.setText("   Your Section Says : WARMER!");
                                    } else if (m > w && m > c) {
                                        text.setBackgroundColor(getResources().getColor(R.color.primary));
                                        text.setText("   Your Section Says : DROP!");
                                    } else if (c > w && c > m) {
                                        text.setTextColor(Color.WHITE);
                                        text.setBackgroundColor(getResources().getColor(R.color.secondary));
                                        text.setText("   Your Section Says : COLDER!");
                                    } else
                                        text.setText("   Your Section Says :");
                                }
                            });
                        }
                    }
                } // else
            } //s!=null
        }//onstringavilable
    }
}
