package com.kazumaproject.markdownnote.preferences

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import androidx.preference.PreferenceManager

object SharedPreferences {
    private lateinit var preferences: SharedPreferences

    private val SYNTAX = Pair("edit_text_syntax_mode_preference","")
    fun init(context: Context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var syntaxInEditText: String?
        get() = preferences.getString(SYNTAX.first, SYNTAX.second)

        set(value) = preferences.edit {
            it.putString(SYNTAX.first, value ?: "")
        }

}