package com.kazumaproject.markdownnote.preferences

interface PreferenceInterface {
    fun saveSyntaxInEditText(value: String)
    fun getSyntaxInEditTextType(): String
    fun clearSyntaxInEditTextPreferences()
}