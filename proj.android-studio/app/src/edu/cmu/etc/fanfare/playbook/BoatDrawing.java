package edu.cmu.etc.fanfare.playbook;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by ramya on 4/25/17.
 */
/*
Class to draw lines and dots on the canvas based
on the drawing and order of markers.
This class is customized for the boat drawing
*/
public class BoatDrawing {
    private static View view;
    private static int section;

    BoatDrawing(View view, int section)
    {
        this.view=view;
        this.section=section;
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

            if (section == 0) {
                int[] loc0 = new int[2];
                int[] loc1 = new int[2];
                int[] canvasloc = TreasureHuntFragment.positionView.canvasLocation;

                if (TreasureHuntFragment.gameState.game_on) {
                    ImageView v0 = (ImageView) view.findViewById(R.id.boat_v0);
                    ImageView v1 = (ImageView) view.findViewById(R.id.boat_v1);
                    ImageView v2 = (ImageView) view.findViewById(R.id.boat_v2);

                    int mColor = Color.rgb(255, 255, 255);
                    Paint mPaint = new Paint();
                    mPaint.setColor(mColor);
                    mPaint.setAntiAlias(true);
                    mPaint.setStrokeWidth(10);
                    mPaint.setStyle(Paint.Style.FILL);
                    mPaint.setTextSize(48f);


                    //draw a circle at 0,1,2 vertices
                    v0.getLocationInWindow(loc0);
                    loc0[0] -= canvasloc[0];
                    loc0[1] -= canvasloc[1];
                    canvas.drawCircle(loc0[0], loc0[1], 15, mPaint);
                    canvas.drawText("1",loc0[0]+10,loc0[1]+30, mPaint);

                    v1.getLocationInWindow(loc1);
                    loc1[0] -= canvasloc[0];
                    loc1[1] -= canvasloc[1];
                    canvas.drawCircle(loc1[0], loc1[1], 15, mPaint);
                    canvas.drawText("2",loc1[0]-40,loc1[1]+20, mPaint);

                    //draw a line from 0,1
                    canvas.drawLine(loc0[0], loc0[1], loc1[0], loc1[1], mPaint);

                    v2.getLocationInWindow(loc0);
                    loc0[0] -= canvasloc[0];
                    loc0[1] -= canvasloc[1];
                    canvas.drawCircle(loc0[0], loc0[1], 15, mPaint);
                    canvas.drawText("3",loc0[0]-20,loc0[1]+50, mPaint);

                    //draw a line from 1,2
                    canvas.drawLine(loc1[0], loc1[1], loc0[0], loc0[1], mPaint);
                }


                if (TreasureHuntFragment.gameState.flag1) {
                    ImageView v2 = (ImageView) view.findViewById(R.id.boat_v2);
                    ImageView v3 = (ImageView) view.findViewById(R.id.boat_v3);

                    int mColor = Color.rgb(255, 255, 255);
                    Paint mPaint = new Paint();
                    mPaint.setColor(mColor);
                    mPaint.setAntiAlias(true);
                    mPaint.setStrokeWidth(10);
                    mPaint.setStyle(Paint.Style.FILL);
                    mPaint.setTextSize(48f);

                    v3.getLocationInWindow(loc0);
                    loc0[0] -= canvasloc[0];
                    loc0[1] -= canvasloc[1];
                    canvas.drawCircle(loc0[0], loc0[1], 15, mPaint);
                    canvas.drawText("4",loc0[0]+10,loc0[1]+50, mPaint);
                    v2.getLocationInWindow(loc1);
                    loc1[0] -= canvasloc[0];
                    loc1[1] -= canvasloc[1];
                    canvas.drawLine(loc0[0], loc0[1], loc1[0], loc1[1], mPaint);
                }

                if (TreasureHuntFragment.gameState.flag2) {

                    ImageView v3 = (ImageView) view.findViewById(R.id.boat_v3);
                    ImageView v4 = (ImageView) view.findViewById(R.id.boat_v4);


                    int mColor = Color.rgb(255, 255, 255);
                    Paint mPaint = new Paint();
                    mPaint.setColor(mColor);
                    mPaint.setAntiAlias(true);
                    mPaint.setStrokeWidth(10);
                    mPaint.setStyle(Paint.Style.FILL);
                    mPaint.setTextSize(48f);

                    v4.getLocationInWindow(loc0);
                    loc0[0] -= canvasloc[0];
                    loc0[1] -= canvasloc[1];
                    canvas.drawCircle(loc0[0], loc0[1], 15, mPaint);
                    canvas.drawText("5",loc0[0]+20,loc0[1]+20, mPaint);

                    v3.getLocationInWindow(loc1);
                    loc1[0] -= canvasloc[0];
                    loc1[1] -= canvasloc[1];
                    canvas.drawLine(loc0[0], loc0[1], loc1[0], loc1[1], mPaint);

                }

                if (TreasureHuntFragment.gameState.flag3) {

                    ImageView v4 = (ImageView) view.findViewById(R.id.boat_v4);
                    ImageView v5 = (ImageView) view.findViewById(R.id.boat_v5);
                    ImageView v0 = (ImageView) view.findViewById(R.id.boat_v0);

                    int mColor = Color.rgb(255, 255, 255);
                    Paint mPaint = new Paint();
                    mPaint.setColor(mColor);
                    mPaint.setAntiAlias(true);
                    mPaint.setStrokeWidth(10);
                    mPaint.setStyle(Paint.Style.FILL);
                    mPaint.setTextSize(48f);

                    v5.getLocationInWindow(loc0);
                    loc0[0] -= canvasloc[0];
                    loc0[1] -= canvasloc[1];
                    canvas.drawCircle(loc0[0], loc0[1], 15, mPaint);
                    canvas.drawText("6",loc0[0]+20,loc0[1]-20, mPaint);

                    v4.getLocationInWindow(loc1);
                    loc1[0] -= canvasloc[0];
                    loc1[1] -= canvasloc[1];
                    canvas.drawLine(loc0[0], loc0[1], loc1[0], loc1[1], mPaint);

                    v0.getLocationInWindow(loc1);
                    loc1[0] -= canvasloc[0];
                    loc1[1] -= canvasloc[1];
                    canvas.drawLine(loc0[0], loc0[1], loc1[0], loc1[1], mPaint);
                }
                if (!TreasureHuntFragment.gameState.flag1 || !TreasureHuntFragment.gameState.flag2 || !TreasureHuntFragment.gameState.flag3)
                    invalidate();

            } //section
        }
    }

}
