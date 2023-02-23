package com.kazumaproject.markdownnote.other;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int baseline = getBaseline();
        for (int i = 0; i < getLineCount(); i++) {
            canvas.drawText("" + (i+1), rect.left + 16, baseline, paint);
            baseline += getLineHeight();
        }
        super.onDraw(canvas);
    }
}
