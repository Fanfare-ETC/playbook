package edu.cmu.etc.fanfare.playbook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
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
        final Layout textLayout = getLayout();
        final int textWidth = (int) textPaint.measureText(transformedText);
        textPaint.getTextBounds("X", 0, 1, textBounds);

        final int left = getPaddingLeft();
        final int bottom = (h + textBounds.height()) / 2;

        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setTextSize(getTextSize());
        mPaint.setTypeface(getTypeface());
        mPaint.setStrokeJoin(Paint.Join.MITER);
        mPaint.setStrokeMiter(2.0f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPaint.setLetterSpacing(getLetterSpacing());
        }

        // When drawing, we need to check if the text is ellipsized.
        int lines = textLayout.getLineCount();
        int ellipsisCount = textLayout.getEllipsisCount(lines - 1);
        int ellipsisStart;
        if (ellipsisCount > 0) {
            ellipsisStart = textLayout.getEllipsisStart(lines - 1);
        } else {
            ellipsisStart = text.length();
        }

        mPaint.setStrokeWidth(mOuterStrokeWidth);
        mPaint.setColor(mOuterStrokeColor);
        canvas.drawText(transformedText, 0, ellipsisStart, left, bottom, mPaint);

        mPaint.setStrokeWidth(mInnerStrokeWidth);
        mPaint.setColor(mInnerStrokeColor);
        canvas.drawText(transformedText, 0, ellipsisStart, left, bottom, mPaint);

        Bitmap bitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas parentCanvas = new Canvas(bitmap);
        super.onDraw(parentCanvas);

        canvas.drawBitmap(bitmap, 0, 0, null);
    }
}
