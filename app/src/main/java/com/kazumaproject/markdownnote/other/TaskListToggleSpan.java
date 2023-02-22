package com.kazumaproject.markdownnote.other;

import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import io.noties.markwon.ext.tasklist.TaskListSpan;
import timber.log.Timber;

public class TaskListToggleSpan extends ClickableSpan {
    private final TaskListSpan span;

    public TaskListToggleSpan(@NonNull TaskListSpan span) {
        this.span = span;
    }

    @Override
    public void onClick(@NonNull View widget) {
        // toggle span (this is a mere visual change)
        span.setDone(!span.isDone());
        // request visual update
        widget.invalidate();

        // it must be a TextView
        final TextView textView = (TextView) widget;
        // it must be spanned
        final Spanned spanned = (Spanned) textView.getText();

        // actual text of the span (this can be used along with the  `span`)
        final CharSequence task = spanned.subSequence(
                spanned.getSpanStart(this),
                spanned.getSpanEnd(this)
        );
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        // no op, so text is not rendered as a link
    }
}
