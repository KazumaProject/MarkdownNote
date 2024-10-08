package com.kazumaproject.markdownnote.drawer.model

sealed class DrawerSelectedItem{
    object AllNotes: DrawerSelectedItem()
    object BookmarkedNotes: DrawerSelectedItem()
    object DraftNotes: DrawerSelectedItem()
    object TrashNotes: DrawerSelectedItem()
    data class EmojiCategory(
        val unicode: Int,
        val index: Int
    ): DrawerSelectedItem()

    object ReadFile: DrawerSelectedItem()

    object ReadApplicationFile: DrawerSelectedItem()

    object GoToSettings: DrawerSelectedItem()
}
