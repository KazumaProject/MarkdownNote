package com.kazumaproject.markdownnote.other

fun String.getTitleFromNote(): String {
    val title = this.split("\r?\n|\r".toRegex()).toTypedArray()
    return if (title.isEmpty()) this else title[0]
}

fun String.convertInFileTitle(): String{
    val regex = "[^a-zA-Z0-9]".toRegex()
    return this.replace(regex,"").lowercase()
}