package com.kazumaproject.markdownnote.database.note

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("SELECT * FROM note_table WHERE id = :noteID")
    suspend fun getNoteById(noteID: String): NoteEntity?

    @Query("SELECT * FROM note_table ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("DELETE FROM note_table WHERE id = :noteID")
    suspend fun deleteNoteById(noteID: String)

    @Query("DELETE FROM note_table")
    suspend fun deleteAllNotes()
}