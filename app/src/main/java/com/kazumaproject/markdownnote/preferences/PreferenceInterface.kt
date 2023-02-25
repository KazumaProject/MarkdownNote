package com.kazumaproject.markdownnote.preferences

interface PreferenceInterface {
    fun saveBackupFilePath(file_path: String)
    fun getBackupFilePath(): String
    fun clearFilePathPreference()
}