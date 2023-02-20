package com.kazumaproject.emojipicker.model

data class ParentEmojiItem(
    val parentTitle: String,
    val childItemList: List<Emoji>
)
