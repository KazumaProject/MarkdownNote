package com.kazumaproject.markdownnote.database.note_trash

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteTrashDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrashNote(note: NoteTrashEntity)

    @Query("SELECT * FROM note_trash_table WHERE id = :noteID")
    suspend fun getTrashNoteById(noteID: String): NoteTrashEntity?

    @Query("SELECT * FROM note_trash_table ORDER BY updatedAt DESC")
    fun getAllTrashNotes(): Flow<List<NoteTrashEntity>>

    @Query("DELETE FROM note_trash_table WHERE id = :noteID")
    suspend fun deleteTrashNoteById(noteID: String)

    @Query("DELETE FROM note_trash_table")
    suspend fun deleteAllTrashNotes()
}