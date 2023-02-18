package com.kazumaproject.markdownnote.other

import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams


fun ViewGroup.setMargins(l: Int, t: Int, r: Int, b: Int) {
    if (this.layoutParams is MarginLayoutParams) {
        val p = this.layoutParams as MarginLayoutParams
        p.setMargins(l, t, r, b)
        this.requestLayout()
    }
}