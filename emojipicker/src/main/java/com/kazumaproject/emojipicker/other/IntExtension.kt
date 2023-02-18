package com.kazumaproject.emojipicker.other

fun Int.convertUnicode(): String{
    return String(Character.toChars(this))
}