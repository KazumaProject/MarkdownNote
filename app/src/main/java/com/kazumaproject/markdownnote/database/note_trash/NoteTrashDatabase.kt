package com.kazumaproject.markdownnote.database.note_trash

import androidx.room.Database

@Database(
    entities = [NoteTrashEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NoteTrashDatabase {
    abstract fun noteTrashDao(): NoteTrashDao
}