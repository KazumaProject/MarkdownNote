package com.kazumaproject.emojipicker.model

import com.kazumaproject.emojipicker.model.Emoji

data class ParentEmojiItem(
    val patentTitle: String,
    val childItemList: List<Emoji>
)
