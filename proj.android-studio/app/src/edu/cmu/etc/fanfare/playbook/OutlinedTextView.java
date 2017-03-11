package edu.cmu.etc.fanfare.playbook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class OutlinedTextView extends AppCompatTextView {
    public static final String TAG = OutlinedTextView.class.getSimpleName();
    private Paint mPaint = new Paint();

    private int mInnerStrokeWidth = 4;
    private int mInnerStrokeColor;

    private int mOuterStrokeWidth = 8;
    private int mOuterStrokeColor;

    public OutlinedTextView(Context context) {
        super(context);
    }

    public OutlinedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OutlinedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setInnerStrokeWidth(int width) {
        mInnerStrokeWidth = width;
    }

    public void setInnerStrokeColor(int color) {
        mInnerStrokeColor = color;
    }

    public void setOuterStrokeWidth(int width) {
        mOuterStrokeWidth = width;
    }

    public void setOuterStrokeColor(int color) {
        mOuterStrokeColor = color;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth() + mOuterStrokeWidth * 2, getMeasuredHeight());
    }

    @Override
    public void onDraw(Canvas canvas) {
        final int w = getMeasuredWidth();
        final int h = getMeasuredHeight();
        final CharSequence text = getText();
        final String transformedText = getTransformationMethod().getTransformation(text, this).toString();
        final Rect textBounds = new Rect();
        final Paint textPaint = getPaint();
        final int textWidth = (int) textPaint.measureText(transformedText);
        textPaint.getTextBounds("X", 0, 1, textBounds);

        final int left = w - getPaddingRight() - textWidth - mOuterStrokeWidth;
        final int bottom = (h + textBounds.height()) / 2;

        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setTextSize(getTextSize());
        mPaint.setTypeface(getTypeface());
        mPaint.setStrokeWidth(mOuterStrokeWidth);
        mPaint.setColor(mOuterStrokeColor);
        canvas.drawText(transformedText, left, bottom, mPaint);

        mPaint.setStrokeWidth(mInnerStrokeWidth);
        mPaint.setColor(mInnerStrokeColor);
        canvas.drawText(transformedText, left, bottom, mPaint);

        Bitmap bitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas parentCanvas = new Canvas(bitmap);
        super.onDraw(parentCanvas);

        canvas.drawBitmap(bitmap, mOuterStrokeWidth, 0, new Paint());
    }
}
