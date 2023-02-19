package com.kazumaproject.markdownnote.database.note_trash

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "note_trash_table")
data class NoteTrashEntity (
    val body: String,
    val emojiUnicode: Int,
    val bookmark: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString(),
)