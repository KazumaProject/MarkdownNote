package com.kazumaproject.markdownnote.drawer.model

data class DrawerItem(
    val title: String,
    val count: Int,
    val type: DrawerItemType,
    val resID: Int?,
    val emojiUnicode: Int?
)
