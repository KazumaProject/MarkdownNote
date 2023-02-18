package com.kazumaproject.markdownnote.repositories

import com.kazumaproject.markdownnote.database.note.NoteDao
import com.kazumaproject.markdownnote.database.note.NoteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
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

}