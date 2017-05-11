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
This class is customized for the bird drawing
*/
public class BirdDrawing {

    private static View view;
    private static int section;

    BirdDrawing(View view, int section)
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
                int[] canvasloc = TreasureHuntFragment.positionView.canvasLocation;

                if (TreasureHuntFragment.gameState.game_on) {
                    ImageView v2 = (ImageView) view.findViewById(R.id.bird_v2);
                    ImageView v4 = (ImageView) view.findViewById(R.id.bird_v4);
                    ImageView v5 = (ImageView) view.findViewById(R.id.bird_v5);

                    int mColor = Color.rgb(255, 255, 255);
                    Paint mPaint = new Paint();
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


                if (TreasureHuntFragment.gameState.flag1) {
                    ImageView v0 = (ImageView) view.findViewById(R.id.bird_v0);
                    ImageView v5 = (ImageView) view.findViewById(R.id.bird_v5);

                    int mColor = Color.rgb(255, 255, 255);
                    Paint mPaint = new Paint();
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

                if (TreasureHuntFragment.gameState.flag2) {

                    ImageView v2 = (ImageView) view.findViewById(R.id.bird_v2);
                    ImageView v3 = (ImageView) view.findViewById(R.id.bird_v3);
                    ImageView v4 = (ImageView) view.findViewById(R.id.bird_v4);
                    ImageView v5 = (ImageView) view.findViewById(R.id.bird_v5);

                    int mColor = Color.rgb(255, 255, 255);
                    Paint mPaint = new Paint();
                    mPaint.setColor(mColor);
                    mPaint.setAntiAlias(true);
                    mPaint.setStrokeWidth(10);
                    mPaint.setStyle(Paint.Style.FILL);

                    v3.getLocationInWindow(loc0);
                    loc0[0] -= canvasloc[0];
                    loc0[1] -= canvasloc[1];
                    canvas.drawCircle(loc0[0], loc0[1], 15, mPaint);

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

                if (TreasureHuntFragment.gameState.flag3) {

                    ImageView v0 = (ImageView) view.findViewById(R.id.bird_v0);
                    ImageView v1 = (ImageView) view.findViewById(R.id.bird_v1);
                    ImageView v2 = (ImageView) view.findViewById(R.id.bird_v2);

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

                    v2.getLocationInWindow(loc1);
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

            } // section
        }
    }

}
