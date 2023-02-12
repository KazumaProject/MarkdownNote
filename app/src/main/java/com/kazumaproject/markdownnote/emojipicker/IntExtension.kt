package com.kazumaproject.markdownnote.emojipicker

fun Int.convertUnicode(): String{
    return String(Character.toChars(this))
}