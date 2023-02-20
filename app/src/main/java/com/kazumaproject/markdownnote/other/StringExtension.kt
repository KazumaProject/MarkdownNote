package com.kazumaproject.markdownnote.other

fun String.getTitleFromNote(): String?{
    val title = this.split("[\r\n]+")
    return if (title.isEmpty()) this else title[0]
}