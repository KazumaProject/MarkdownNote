package com.kazumaproject.markdownnote.other

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import com.google.android.material.textfield.TextInputEditText

object KeyboardHelper {

    fun hideKeyboardAndClearFocus(activity: Activity){
        val v = activity.currentFocus
        if (v is TextInputEditText) {
            v.clearFocus()
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken,0)
        }
    }
}