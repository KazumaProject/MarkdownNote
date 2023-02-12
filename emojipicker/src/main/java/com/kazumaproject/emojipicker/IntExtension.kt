package com.kazumaproject.emojipicker

fun Int.convertUnicode(): String{
    return String(Character.toChars(this))
}