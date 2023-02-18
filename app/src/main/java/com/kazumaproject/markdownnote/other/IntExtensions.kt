package com.kazumaproject.markdownnote.other

import android.content.Context
import android.util.DisplayMetrics


fun Int.convertPxToDp(context: Context): Float {
    val metrics: DisplayMetrics = context.resources.displayMetrics
    return this / metrics.density
}