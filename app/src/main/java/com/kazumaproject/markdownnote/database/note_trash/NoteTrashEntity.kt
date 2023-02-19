package com.kazumaproject.markdownnote.database.note_trash

import androidx.room.PrimaryKey
import java.util.*

data class NoteTrashEntity (
    val body: String,
    val emojiUnicode: Int,
    val bookmark: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString(),
)