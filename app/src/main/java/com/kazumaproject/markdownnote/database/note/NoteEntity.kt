package com.kazumaproject.markdownnote.database.note

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "note_table")
data class NoteEntity(
    val body: String,
    val emojiUnicode: Int,
    val createdAt: Long,
    val updatedAt: Long,
    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString(),
)
