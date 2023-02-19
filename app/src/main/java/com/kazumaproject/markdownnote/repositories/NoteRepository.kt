package com.kazumaproject.markdownnote.repositories

import com.kazumaproject.markdownnote.database.note.NoteDao
import com.kazumaproject.markdownnote.database.note.NoteEntity
import com.kazumaproject.markdownnote.database.note_bookmark.NoteBookMarkDao
import com.kazumaproject.markdownnote.database.note_bookmark.NoteBookMarkEntity
import com.kazumaproject.markdownnote.database.note_draft.NoteDraftDao
import com.kazumaproject.markdownnote.database.note_draft.NoteDraftEntity
import com.kazumaproject.markdownnote.database.note_trash.NoteTrashDao
import com.kazumaproject.markdownnote.database.note_trash.NoteTrashEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val noteDraftDao: NoteDraftDao,
    private val noteTrashDao: NoteTrashDao,
    private val noteBookMarkDao: NoteBookMarkDao
) {
    suspend fun insertNote(note: NoteEntity){
        noteDao.insertNote(note = note)
    }

    suspend fun insertNotes(notes: List<NoteEntity>){
        notes.forEach { insertNote(it)}
    }

    suspend fun deleteNote(noteID: String){
        noteDao.deleteNoteById(noteID = noteID)
    }

    suspend fun getNoteById(noteID: String) = noteDao.getNoteById(noteID = noteID)

    fun getAllNotes(): Flow<List<NoteEntity>> {
        return noteDao.getAllNotes()
    }
    fun getAllDraftNotes(): Flow<List<NoteDraftEntity>> {
        return noteDraftDao.getAllDraftNotes()
    }

    fun getAllTrashNotes(): Flow<List<NoteTrashEntity>> {
        return noteTrashDao.getAllTrashNotes()
    }

    fun getAllBookmarkNotes(): Flow<List<NoteBookMarkEntity>> {
        return noteBookMarkDao.getAllBookmarkNotes()
    }


}