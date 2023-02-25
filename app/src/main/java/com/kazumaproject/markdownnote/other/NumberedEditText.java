package com.kazumaproject.markdownnote.other;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.kazumaproject.markdownnote.R;

public class NumberedEditText extends TextInputEditText {
    private Rect rect;
    private Paint paint;

    public NumberedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        rect = new Rect();
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(context, R.color.text_color_sub1));
        paint.setTextSize(44);
        paint.setTextAlign(Paint.Align.RIGHT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int baseline = getBaseline();
        for (int i = 0; i < getLineCount(); i++) {
            if (i < 9){
                canvas.drawText("" + (i+1), rect.left + 40, baseline, paint);
            } else if (i < 99){
                canvas.drawText("" + (i+1), rect.left + 60, baseline, paint);
            } else if (i < 999){
                canvas.drawText("" + (i+1), rect.left + 80, baseline, paint);
            } else if (i < 9999){
                canvas.drawText("" + (i+1), rect.left + 100, baseline, paint);
            } else if (i < 99999){
                canvas.drawText("" + (i+1), rect.left + 120, baseline, paint);
            } else  {
                canvas.drawText("" + (i+1), rect.left + 140, baseline, paint);
            }
            baseline += getLineHeight();
        }
        super.onDraw(canvas);
    }
}
