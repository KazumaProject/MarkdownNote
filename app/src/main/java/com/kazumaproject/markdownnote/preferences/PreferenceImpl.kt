package com.kazumaproject.markdownnote.preferences

class PreferenceImpl(
    private val sharedPreferences: SharedPreferences
) : PreferenceInterface {
    override fun saveSyntaxInEditText(value: String) {
        sharedPreferences.syntaxInEditText = value
    }

    override fun getSyntaxInEditTextType(): String {
        return sharedPreferences.syntaxInEditText ?: "2"
    }

    override fun clearSyntaxInEditTextPreferences() {
        sharedPreferences.syntaxInEditText = null
    }

}