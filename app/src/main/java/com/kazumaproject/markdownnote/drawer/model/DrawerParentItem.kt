package com.kazumaproject.markdownnote.drawer.model

data class DrawerParentItem(
    val parentTitle: String,
    val childList: List<DrawerItem>
)
