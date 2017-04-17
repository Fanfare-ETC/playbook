package edu.cmu.etc.fanfare.playbook;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class TreasureHuntFragment extends PlaybookFragment implements View.OnClickListener,View.OnTouchListener{

    public static int section;
    public static View view;
    private static boolean treasurehunt_live=false;
    private ConstraintLayout layout;
    private int id;

    private  String mEndpoint = "ws://" +
            BuildConfig.PLAYBOOK_TREASUREHUNT_API_HOST + ":" +
            BuildConfig.PLAYBOOK_TREASUREHUNT_API_PORT;

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

        private View mWarmerView;
        private View mColderView;
        private View mPlantView;

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
            if ( mWarmerView == null || mColderView == null ||
                    mPlantView == null) {
                return;
            }


           // mRunnerView.getLocationInWindow(runnerLocation);
            mWarmerView.getLocationInWindow(warmerLocation);
            mColderView.getLocationInWindow(colderLocation);
            mPlantView.getLocationInWindow(plantLocation);
            this.getLocationInWindow(canvasLocation);

            // Compute the midpoints of these locations.
            warmerLocation[0] = warmerLocation[0] + mWarmerView.getWidth() / 2 - canvasLocation[0];
            warmerLocation[1] = warmerLocation[1] + mWarmerView.getHeight() / 2 - canvasLocation[1];
            colderLocation[0] = colderLocation[0] + mColderView.getWidth() / 2 - canvasLocation[0];
            colderLocation[1] = colderLocation[1] + mColderView.getHeight() / 2 - canvasLocation[1];
            plantLocation[0] = plantLocation[0] + mPlantView.getWidth() / 2 - canvasLocation[0];
            plantLocation[1] = plantLocation[1] + mPlantView.getHeight() / 2 - canvasLocation[1];

            /**

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
          **/
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view=inflater.inflate(R.layout.treasurehunt_fragment, container, false);

        if(!treasurehunt_live)
        {
            ImageView translucent = (ImageView) view.findViewById(R.id.translucentlayer);
            translucent.setVisibility(View.VISIBLE);
            ImageView tutorial = (ImageView) view.findViewById(R.id.treasurehunt_tutorial);
            tutorial.setVisibility(View.VISIBLE);
           // treasurehunt_live=true;
        }

        SharedPreferences settings = this.getContext().getSharedPreferences("FANFARE_SHARED", 0);
        section = settings.getInt("section", 0)-1;
       /* ImageView marker0= (ImageView)view.findViewById(R.id.marker0);
        ImageView marker1= (ImageView)view.findViewById(R.id.marker1);
        ImageView marker2= (ImageView)view.findViewById(R.id.marker2);
        ImageView marker3= (ImageView)view.findViewById(R.id.marker3);
        switch(section) {
            case 0:
                marker0.setVisibility(View.VISIBLE);
                marker1.setVisibility(View.INVISIBLE);
                marker2.setVisibility(View.INVISIBLE);
                marker3.setVisibility(View.INVISIBLE);
                break;
            case 1:
                marker0.setVisibility(View.INVISIBLE);
                marker1.setVisibility(View.VISIBLE);
                marker2.setVisibility(View.INVISIBLE);
                marker3.setVisibility(View.INVISIBLE);
            case 2:
                marker0.setVisibility(View.INVISIBLE);
                marker1.setVisibility(View.INVISIBLE);
                marker2.setVisibility(View.VISIBLE);
                marker3.setVisibility(View.INVISIBLE);
                break;
            case 3:
                marker0.setVisibility(View.INVISIBLE);
                marker1.setVisibility(View.INVISIBLE);
                marker2.setVisibility(View.INVISIBLE);
                marker3.setVisibility(View.VISIBLE);
                break;
        }*/
        TextView warmer_text = (TextView) view.findViewById(R.id.warmer_text);
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/nova2.ttf");
        warmer_text.setTypeface(tf);
        warmer_text.setTextSize(24);
        warmer_text.setText("Getting\nWarmer!");

        TextView colder_text = (TextView) view.findViewById(R.id.colder_text);
        colder_text .setTypeface(tf);
        colder_text.setTextSize(24);
        colder_text .setText("Getting\nColder!");

        TextView plant_text = (TextView) view.findViewById(R.id.marker_text);
        plant_text .setTypeface(tf);
        plant_text.setTextSize(24);
        plant_text .setText("Drop the\nMarker!");

        //TextView aggregate_text=(TextView) view.findViewById(R.id.aggregate_text);
       // aggregate_text.setBackgroundColor(getResources().getColor(R.color.primary));

        ImageView WarmerView= (ImageView)view.findViewById(R.id.warmer);
        WarmerView.setOnClickListener(this);
        ImageView ColderView= (ImageView)view.findViewById(R.id.colder);
        ColderView.setOnClickListener(this);
        ImageView PlantView= (ImageView)view.findViewById(R.id.marker);
        PlantView.setOnClickListener(this);
        ImageView MapView= (ImageView)view.findViewById(R.id.map);
        MapView.setOnClickListener(this);
        ImageView agg= (ImageView)view.findViewById(R.id.aggregate);
        agg.setOnTouchListener(this);

       //timerHandler.postDelayed(timerRunnable,0);

        // Dynamically draw lines.
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
         // We only want to know that layout happened for the first time.
         view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Create a new view for the lines and draw them.
                LinesView linesView = (LinesView) view.findViewById(R.id.linesView);
                linesView.setWarmerView(view.findViewById(R.id.warmer));
                linesView.setColderView(view.findViewById(R.id.colder));
                linesView.setPlantView(view.findViewById(R.id.marker));
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
                                        if(!treasurehunt_live) {
                                            treasurehunt_live = true;
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    ImageView tutorial = (ImageView) view.findViewById(R.id.treasurehunt_tutorial);
                                                    ObjectAnimator blink_tut = ObjectAnimator.ofFloat(tutorial, "alpha", 0.5f, 0.0f);
                                                    blink_tut.setDuration(2000);
                                                    blink_tut.start();
                                                    final TextView text = (TextView) view.findViewById(R.id.timer);
                                                    int mTimerColor = Color.rgb(0, 0, 0);
                                                    text.setTextColor(mTimerColor);
                                                    //text.setFontFeatureSettings(R.);
                                                    //Handler timerHandler = new Handler();
                                                    //timerHandler.postDelayed(timerRunnable,0);
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

                                                    //((ViewGroup) namebar.getParent()).removeView(namebar);
                                                    ImageView translucent = (ImageView) view.findViewById(R.id.translucentlayer);
                                                    translucent.setVisibility(View.INVISIBLE);
                                                    ObjectAnimator blink_trans = ObjectAnimator.ofFloat(tutorial, "alpha", 0.5f, 0.0f);
                                                    blink_trans.setDuration(3000);
                                                    blink_trans.start();
                                                }
                                            });
                                        }
                                    }
                                    else if(s.equals("stop"))
                                    {
                                        if(treasurehunt_live)
                                         treasurehunt_live=false;
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ImageView translucent = (ImageView) view.findViewById(R.id.translucentlayer);
                                                translucent.setVisibility(View.VISIBLE);
                                            }
                                            });
                                    }
                                    else if(s.equals("plus10warmer"))
                                    {
                                        final Vector<ImageView> plustens = new Vector<ImageView>(10);
                                        final Vector<ObjectAnimator> anim_plustens= new Vector<ObjectAnimator>(10);

                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                //create a new plusten
                                                ConstraintLayout layout=(ConstraintLayout)view.findViewById(R.id.treasurehunt_layout);
                                                int[] location = new int[2];
                                                int w_id = R.drawable.plusten_w;

                                                ImageView new_plusten= new ImageView(getContext());
                                                new_plusten.setImageResource(w_id);
                                                new_plusten.setVisibility(View.VISIBLE);
                                                ImageView section = (ImageView) view.findViewById(R.id.warmer_section);
                                                section.getLocationInWindow(location);

                                                Random generator = new Random();
                                                int x = generator.nextInt(100)-80;
                                                int y = generator.nextInt(100)-80 ;

                                                new_plusten.setRotation(x*y);
                                                new_plusten.setX(location[0]+x);
                                                new_plusten.setY(location[1]+y);

                                                if(plustens.size()==10)
                                                {
                                                    plustens.clear();
                                                }
                                                else {
                                                    plustens.add(new_plusten);
                                                }

                                                ObjectAnimator blink_plusten = ObjectAnimator.ofFloat(new_plusten, "alpha", 1.0f, 0.0f);
                                                blink_plusten.setDuration(1000);

                                                if(anim_plustens.size()==10)
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
                                                /*
                                                TextView text = (TextView) view.findViewById(R.id.aggregate_text);
                                                text.setBackgroundColor(getResources().getColor(R.color.primary));
                                                Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/nova2.ttf");
                                                text.setTypeface(tf);
                                                text.setTextSize(30);
                                                text.setText("   Your Section Says : WARMER");
                                                */
                                            }
                                        });
                                    }
                                    else if(s.equals("plus10colder"))
                                    {

                                        final Vector<ImageView> plustens = new Vector<ImageView>(10);
                                        final Vector<ObjectAnimator> anim_plustens= new Vector<ObjectAnimator>(10);

                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                //create a new plusten
                                                ConstraintLayout layout=(ConstraintLayout)view.findViewById(R.id.treasurehunt_layout);
                                                int[] location = new int[2];
                                                int w_id = R.drawable.plusten_c;

                                                ImageView new_plusten= new ImageView(getContext());
                                                new_plusten.setImageResource(w_id);
                                                new_plusten.setVisibility(View.VISIBLE);
                                                ImageView section = (ImageView) view.findViewById(R.id.colder_section);
                                                section.getLocationOnScreen(location);

                                                Random generator = new Random();
                                                int x = generator.nextInt(100)-80;
                                                int y = generator.nextInt(100)-80 ;

                                                new_plusten.setRotation(x*y);
                                                new_plusten.setX(location[0]+x);
                                                new_plusten.setY(location[1]+y);

                                                if(plustens.size()==10)
                                                {
                                                    plustens.clear();
                                                }
                                                else {
                                                    plustens.add(new_plusten);
                                                }

                                                ObjectAnimator blink_plusten = ObjectAnimator.ofFloat(new_plusten, "alpha", 1.0f, 0.0f);
                                                blink_plusten.setDuration(1000);

                                                if(anim_plustens.size()==10)
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
                                                /*
                                                TextView text = (TextView) view.findViewById(R.id.aggregate_text);
                                                text.setBackgroundColor(getResources().getColor(R.color.primary));
                                                Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/nova2.ttf");
                                                text.setTypeface(tf);
                                                text.setTextSize(30);
                                                text.setText("   Your Section Says : COLDER");
                                                */
                                            }
                                        });

                                    }
                                    else if(s.equals("plus10plant"))
                                    {

                                        final Vector<ImageView> plustens = new Vector<ImageView>(10);
                                        final Vector<ObjectAnimator> anim_plustens= new Vector<ObjectAnimator>(10);

                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                //create a new plusten
                                                ConstraintLayout layout=(ConstraintLayout)view.findViewById(R.id.treasurehunt_layout);
                                                int[] location = new int[2];
                                                int w_id = R.drawable.plusten_p;

                                                ImageView new_plusten= new ImageView(getContext());
                                                new_plusten.setImageResource(w_id);
                                                new_plusten.setVisibility(View.VISIBLE);
                                                ImageView section = (ImageView) view.findViewById(R.id.plant_section);
                                                section.getLocationInWindow(location);

                                                Random generator = new Random();
                                                int x = generator.nextInt(100)-80;
                                                int y = generator.nextInt(100)-80 ;

                                                new_plusten.setRotation(x*y);
                                                new_plusten.setX(location[0]+x);
                                                new_plusten.setY(location[1]+y);

                                                if(plustens.size()==10)
                                                {
                                                    plustens.clear();
                                                }
                                                else {
                                                    plustens.add(new_plusten);
                                                }

                                                ObjectAnimator blink_plusten = ObjectAnimator.ofFloat(new_plusten, "alpha", 1.0f, 0.0f);
                                                blink_plusten.setDuration(1000);

                                                if(anim_plustens.size()==10)
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
                                        });

                                    }
                                    else if (s.contains("winner"))
                                    {
                                        //display winner
                                    }
                                    else {
                                        //moving the section aggregate text display logic to here
                                        if (treasurehunt_live) {
                                            // tokenise wanderer side aggregate
                                            final int w, m, c;
                                            final List<String> num = Arrays.asList(s.split(","));
                                            //if(num.size()!=12) {w=0;m=0;c=0;}
                                            w = Integer.valueOf(num.get(section));
                                            m = Integer.valueOf(num.get(section + 4));
                                            c = Integer.valueOf(num.get(section + 8));
                                            Log.d("wagg", Integer.toString(w) + " " + Integer.toString(m) + " " + Integer.toString(c));
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    TextView text = (TextView) view.findViewById(R.id.aggregate_text);
                                                    text.setBackgroundColor(getResources().getColor(R.color.primary));
                                                    Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/nova2.ttf");
                                                    text.setTypeface(tf);
                                                    text.setTextSize(30);
                                                    if (w > m && w > c)
                                                        text.setText("   Your Section Says : WARMER!");
                                                    else if (m > w && m > c)
                                                        text.setText("   Your Section Says : DROP!");
                                                    else if (c > w && c > m)
                                                        text.setText("   Your Section Says : COLDER!");
                                                    else text.setText("   Your Section Says :");
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        });
                    }
                });

        //define all common animations

         layout=(ConstraintLayout)view.findViewById(R.id.treasurehunt_layout);
         id = R.drawable.plusone;

        return view;

    }
    public View onResumeView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        //timerHandler.postDelayed(timerRunnable,0);
        return view;
    }
    public void onClick(View v) {

        final JSONObject obj = new JSONObject();
        if(treasurehunt_live) {
        switch (v.getId()) {
            case R.id.warmer:
                try {
                    obj.put("section", section);
                    obj.put("selection", 0);
                    obj.put("method", "post");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //create a new plusone
                ImageView new_w = new ImageView(getContext());
                new_w.setImageResource(id);
                new_w.setVisibility(View.VISIBLE);
                new_w.setX(LinesView.warmerLocation[0]);
                new_w.setY(LinesView.warmerLocation[1]);
                layout.addView(new_w);

                ObjectAnimator anim_plusone_w = ObjectAnimator.ofFloat(new_w, "y", LinesView.warmerLocation[1], LinesView.warmerLocation[1] - 500);
                anim_plusone_w.setDuration(500);
                anim_plusone_w.start();
                ObjectAnimator blink_plusone_w = ObjectAnimator.ofFloat(new_w, "alpha", 1.0f, 0.0f);
                blink_plusone_w.setDuration(500);
                blink_plusone_w.start();
                break;
            case R.id.colder:

                try {
                    obj.put("section", section);
                    obj.put("selection", 1);
                    obj.put("method", "post");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //create a new plusone
                ImageView new_c = new ImageView(getContext());
                new_c.setImageResource(id);
                new_c.setVisibility(View.VISIBLE);
                new_c.setX(LinesView.colderLocation[0]);
                new_c.setY(LinesView.colderLocation[1]);
                layout.addView(new_c);

                ObjectAnimator anim_plusone_c = ObjectAnimator.ofFloat(new_c, "y", LinesView.colderLocation[1], LinesView.colderLocation[1] - 500);
                anim_plusone_c.setDuration(500);
                anim_plusone_c.start();
                ObjectAnimator blink_plusone_c = ObjectAnimator.ofFloat(new_c, "alpha", 1.0f, 0.0f);
                blink_plusone_c.setDuration(500);
                blink_plusone_c.start();
                break;
            case R.id.marker:

                try {
                    obj.put("section", section);
                    obj.put("selection", 2);
                    obj.put("method", "post");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //create a new plusone
                ImageView new_p = new ImageView(getContext());
                new_p.setImageResource(id);
                new_p.setVisibility(View.VISIBLE);
                new_p.setX(LinesView.plantLocation[0]);
                new_p.setY(LinesView.plantLocation[1]);
                layout.addView(new_p);

                ObjectAnimator anim_plusone_p = ObjectAnimator.ofFloat(new_p, "y", LinesView.plantLocation[1], LinesView.plantLocation[1] - 500);
                anim_plusone_p.setDuration(500);
                anim_plusone_p.start();
                ObjectAnimator blink_plusone_p = ObjectAnimator.ofFloat(new_p, "alpha", 1.0f, 0.0f);
                blink_plusone_p.setDuration(500);
                blink_plusone_p.start();
                break;
            case R.id.map:
                ImageView MapView= (ImageView)view.findViewById(R.id.map);
                ObjectAnimator flip_map = ObjectAnimator.ofFloat(MapView, "rotationY", 0, 90);
                flip_map.setDuration(500);
                flip_map.start();
                break;
        }
    }
        if(treasurehunt_live) {
            //Log.d("sending",obj.toString());
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
public boolean onTouch(View v, MotionEvent event)
{
    final JSONObject obj = new JSONObject();
    if(treasurehunt_live) {
        switch (v.getId()) {
            case R.id.aggregate:
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    float width = v.getWidth();
                    float tx = event.getX();//-values[0];
                    float ty = event.getY();//-values[1];
                    if (tx <= width / 3) {

                        try {
                            obj.put("section", section);
                            obj.put("selection", 0);
                            obj.put("method", "post");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (tx >= width / 3 && tx <= width * 2 / 3) {

                        try {
                            obj.put("section", section);
                            obj.put("selection", 2);
                            obj.put("method", "post");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (tx >= 2 * width / 3 && tx <= width) {
                        try {
                            obj.put("section", section);
                            obj.put("selection", 1);
                            obj.put("method", "post");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

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
                break;
        }
    }
    if(treasurehunt_live) {

    }
    return true;
}
}
