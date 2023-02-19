package com.kazumaproject.markdownnote.database.note_bookmark

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kazumaproject.markdownnote.database.note_draft.NoteDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteBookMarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmarkNote(note: NoteBookMarkEntity)

    @Query("SELECT * FROM note_bookmark_table WHERE id = :noteID")
    suspend fun getBookmarkNoteById(noteID: String): NoteBookMarkEntity?

    @Query("SELECT * FROM note_bookmark_table ORDER BY updatedAt DESC")
    fun getAllBookmarkNotes(): Flow<List<NoteBookMarkEntity>>

    @Query("DELETE FROM note_bookmark_table WHERE id = :noteID")
    suspend fun deleteBookmarkNoteById(noteID: String)

    @Query("DELETE FROM note_bookmark_table")
    suspend fun deleteAllBookmarkNotes()
}