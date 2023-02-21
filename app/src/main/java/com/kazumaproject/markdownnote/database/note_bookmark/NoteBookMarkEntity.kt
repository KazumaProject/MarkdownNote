package com.kazumaproject.markdownnote.database.note_bookmark

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_bookmark_table")
data class NoteBookMarkEntity(
    val createdAt: Long,
    val updatedAt: Long,
    @PrimaryKey(autoGenerate = false)
    val id: String
)
