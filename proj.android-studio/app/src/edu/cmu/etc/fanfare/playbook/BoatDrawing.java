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

            if (section == 1) {
                int[] loc0 = new int[2];
                int[] loc1 = new int[2];
                int[] canvasloc = TreasureHuntFragment.LinesView.canvasLocation;

                if (TreasureHuntFragment.gameState.game_on) {
                    ImageView v0 = (ImageView) view.findViewById(R.id.boat_v0);
                    ImageView v3 = (ImageView) view.findViewById(R.id.boat_v3);
                    ImageView v5 = (ImageView) view.findViewById(R.id.boat_v5);

                    int mColor = Color.rgb(255, 255, 255);
                    Paint mPaint = new Paint();
                    mPaint.setColor(mColor);
                    mPaint.setAntiAlias(true);
                    mPaint.setStrokeWidth(10);
                    mPaint.setStyle(Paint.Style.FILL);

                    //draw a circle at 0,3,5 vertices
                    v0.getLocationInWindow(loc0);
                    loc0[0] -= canvasloc[0];
                    loc0[1] -= canvasloc[1];
                    canvas.drawCircle(loc0[0], loc0[1], 15, mPaint);
                    v5.getLocationInWindow(loc1);
                    loc1[0] -= canvasloc[0];
                    loc1[1] -= canvasloc[1];
                    canvas.drawCircle(loc1[0], loc1[1], 15, mPaint);

                    //draw a line from 0,5
                    canvas.drawLine(loc0[0], loc0[1], loc1[0], loc1[1], mPaint);

                    v3.getLocationInWindow(loc0);
                    loc0[0] -= canvasloc[0];
                    loc0[1] -= canvasloc[1];
                    canvas.drawCircle(loc0[0], loc0[1], 15, mPaint);
                }


                if (TreasureHuntFragment.gameState.flag1) {
                    ImageView v1 = (ImageView) view.findViewById(R.id.boat_v1);
                    ImageView v5 = (ImageView) view.findViewById(R.id.boat_v5);

                    int mColor = Color.rgb(255, 255, 255);
                    Paint mPaint = new Paint();
                    mPaint.setColor(mColor);
                    mPaint.setAntiAlias(true);
                    mPaint.setStrokeWidth(10);
                    mPaint.setStyle(Paint.Style.FILL);

                    v1.getLocationInWindow(loc0);
                    loc0[0] -= canvasloc[0];
                    loc0[1] -= canvasloc[1];
                    canvas.drawCircle(loc0[0], loc0[1], 15, mPaint);
                    v5.getLocationInWindow(loc1);
                    loc1[0] -= canvasloc[0];
                    loc1[1] -= canvasloc[1];
                    canvas.drawLine(loc0[0], loc0[1], loc1[0], loc1[1], mPaint);
                }

                if (TreasureHuntFragment.gameState.flag2) {

                    ImageView v0 = (ImageView) view.findViewById(R.id.boat_v0);
                    ImageView v3 = (ImageView) view.findViewById(R.id.boat_v3);
                    ImageView v4 = (ImageView) view.findViewById(R.id.boat_v4);


                    int mColor = Color.rgb(255, 255, 255);
                    Paint mPaint = new Paint();
                    mPaint.setColor(mColor);
                    mPaint.setAntiAlias(true);
                    mPaint.setStrokeWidth(10);
                    mPaint.setStyle(Paint.Style.FILL);

                    v4.getLocationInWindow(loc0);
                    loc0[0] -= canvasloc[0];
                    loc0[1] -= canvasloc[1];
                    canvas.drawCircle(loc0[0], loc0[1], 15, mPaint);

                    v0.getLocationInWindow(loc1);
                    loc1[0] -= canvasloc[0];
                    loc1[1] -= canvasloc[1];
                    canvas.drawLine(loc0[0], loc0[1], loc1[0], loc1[1], mPaint);

                    v3.getLocationInWindow(loc1);
                    loc1[0] -= canvasloc[0];
                    loc1[1] -= canvasloc[1];
                    canvas.drawLine(loc0[0], loc0[1], loc1[0], loc1[1], mPaint);

                }

                if (TreasureHuntFragment.gameState.flag3) {

                    ImageView v1 = (ImageView) view.findViewById(R.id.boat_v1);
                    ImageView v2 = (ImageView) view.findViewById(R.id.boat_v2);
                    ImageView v3 = (ImageView) view.findViewById(R.id.boat_v3);

                    int mColor = Color.rgb(255, 255, 255);
                    Paint mPaint = new Paint();
                    mPaint.setColor(mColor);
                    mPaint.setAntiAlias(true);
                    mPaint.setStrokeWidth(10);
                    mPaint.setStyle(Paint.Style.FILL);
                    v2.getLocationInWindow(loc0);
                    loc0[0] -= canvasloc[0];
                    loc0[1] -= canvasloc[1];
                    canvas.drawCircle(loc0[0], loc0[1], 15, mPaint);

                    v1.getLocationInWindow(loc1);
                    loc1[0] -= canvasloc[0];
                    loc1[1] -= canvasloc[1];
                    canvas.drawLine(loc0[0], loc0[1], loc1[0], loc1[1], mPaint);

                    v3.getLocationInWindow(loc1);
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
