package com.kazumaproject.markdownnote.drawer.model

sealed class DrawerItemType{
    object FilterNotes: DrawerItemType()
    object CategoryEmoji: DrawerItemType()
    object Navigation: DrawerItemType()
}
