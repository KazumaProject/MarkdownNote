package com.kazumaproject.markdownnote.database.note_trash

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_trash_table")
data class NoteTrashEntity (
    val createdAt: Long,
    val updatedAt: Long,
    @PrimaryKey(autoGenerate = false)
    val id: String
)