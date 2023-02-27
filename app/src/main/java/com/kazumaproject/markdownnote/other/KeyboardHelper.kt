package com.kazumaproject.markdownnote.other

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.google.android.material.textfield.TextInputEditText
import com.vic797.syntaxhighlight.SyntaxHighlighter

object KeyboardHelper {

    fun hideKeyboardAndClearFocus(activity: Activity){
        val v = activity.currentFocus
        if (v is TextInputEditText || v is SyntaxHighlighter) {
            v.clearFocus()
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken,0)
        }
    }

    fun showKeyboard(activity: Activity, editText: EditText){
        editText.requestFocus()
        if (editText.requestFocus()) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}