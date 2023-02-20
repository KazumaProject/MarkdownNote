package com.kazumaproject.markdownnote.drawer.model

import com.kazumaproject.emojipicker.model.Emoji

sealed class DrawerSelectedItem{
    object AllNotes: DrawerSelectedItem()
    object BookmarkedNotes: DrawerSelectedItem()
    object DraftNotes: DrawerSelectedItem()
    object TrashNotes: DrawerSelectedItem()
    data class EmojiCategory(
        val unicode: Int,
        val index: Int
    ): DrawerSelectedItem()
    object GoToSettings: DrawerSelectedItem()
}
