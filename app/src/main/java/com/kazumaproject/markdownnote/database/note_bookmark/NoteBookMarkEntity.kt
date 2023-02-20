package com.kazumaproject.markdownnote.database.note_bookmark

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "note_bookmark_table")
data class NoteBookMarkEntity(
    val body: String,
    val emojiUnicode: Int,
    val createdAt: Long,
    val updatedAt: Long,
    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString(),
)
