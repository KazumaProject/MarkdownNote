package com.kazumaproject.markdownnote.database.note_bookmark

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [NoteBookMarkEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NoteBookMarkDatabase: RoomDatabase() {
    abstract fun noteBookmarkDao(): NoteBookMarkDao
}