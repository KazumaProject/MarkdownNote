package com.kazumaproject.markdownnote.drawer.model

import com.kazumaproject.emojipicker.model.Emoji

sealed class DrawerSelectedItem{
    object AllNotes: DrawerSelectedItem()
    object BookmarkedNotes: DrawerSelectedItem()
    object DraftNotes: DrawerSelectedItem()
    object TrashNotes: DrawerSelectedItem()
    data class EmojiCategory(
        val emoji: Emoji
    ): DrawerSelectedItem()
    object GoToSettings: DrawerSelectedItem()
}
