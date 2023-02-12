package com.kazumaproject.markdownnote.emojipicker

import com.kazumaproject.markdownnote.emojipicker.Emoji

data class ParentEmojiItem(
    val patentTitle: String,
    val childItemList: List<Emoji>
)
