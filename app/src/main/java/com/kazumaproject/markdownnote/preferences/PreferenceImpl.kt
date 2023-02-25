package com.kazumaproject.markdownnote.preferences

import android.os.Environment

class PreferenceImpl(
    private val sharedPreferences: SharedPreferences
) : PreferenceInterface {
    override fun saveBackupFilePath(file_path: String) {
        sharedPreferences.filePath = file_path
    }

    override fun getBackupFilePath(): String {
        return sharedPreferences.filePath ?: "${
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS)}"
    }

    override fun clearFilePathPreference() {
        sharedPreferences.filePath = null
    }
}