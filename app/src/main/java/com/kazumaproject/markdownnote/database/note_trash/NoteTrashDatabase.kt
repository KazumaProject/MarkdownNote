package com.kazumaproject.markdownnote.database.note_trash

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [NoteTrashEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NoteTrashDatabase: RoomDatabase() {
    abstract fun noteTrashDao(): NoteTrashDao
}