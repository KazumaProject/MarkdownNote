package com.kazumaproject.markdownnote.database.note_draft

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "note_draft_table")
data class NoteDraftEntity (
    val body: String,
    val emojiUnicode: Int,
    val bookmark: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString(),
)