package com.kazumaproject.markdownnote.preferences

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import androidx.preference.PreferenceManager

object SharedPreferences {
    private lateinit var preferences: SharedPreferences

    private val FILE_PATH = Pair(
        "file_path_edit_preference",
        "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}"
    )

    fun init(context: Context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var filePath: String?
        get() = preferences.getString(FILE_PATH.first, FILE_PATH.second)

        set(value) = preferences.edit {
            it.putString(FILE_PATH.first, value ?: "")
        }

}