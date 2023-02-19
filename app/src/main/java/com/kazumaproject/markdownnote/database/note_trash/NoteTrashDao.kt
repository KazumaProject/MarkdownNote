package com.kazumaproject.markdownnote.database.note_trash

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kazumaproject.markdownnote.database.note.NoteEntity
import kotlinx.coroutines.flow.Flow

interface NoteTrashDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("SELECT * FROM note_trash_table WHERE id = :noteID")
    suspend fun getNoteById(noteID: String): NoteEntity?

    @Query("SELECT * FROM note_trash_table ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("DELETE FROM note_trash_table WHERE id = :noteID")
    suspend fun deleteNoteById(noteID: String)

    @Query("DELETE FROM note_trash_table")
    suspend fun deleteAllNotes()
}