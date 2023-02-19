package com.kazumaproject.markdownnote.database.note_draft

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDraftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraftNote(note: NoteDraftEntity)

    @Query("SELECT * FROM note_draft_table WHERE id = :noteID")
    suspend fun getDraftNoteById(noteID: String): NoteDraftEntity?

    @Query("SELECT * FROM note_draft_table ORDER BY updatedAt DESC")
    fun getAllDraftNotes(): Flow<List<NoteDraftEntity>>

    @Query("DELETE FROM note_draft_table WHERE id = :noteID")
    suspend fun deleteDraftNoteById(noteID: String)

    @Query("DELETE FROM note_draft_table")
    suspend fun deleteAllDraftNotes()
}