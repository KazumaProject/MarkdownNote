package com.kazumaproject.markdownnote.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazumaproject.markdownnote.database.note.NoteEntity
import com.kazumaproject.markdownnote.database.note_bookmark.NoteBookMarkEntity
import com.kazumaproject.markdownnote.database.note_draft.NoteDraftEntity
import com.kazumaproject.markdownnote.database.note_trash.NoteTrashEntity
import com.kazumaproject.markdownnote.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    fun insertNote(note: NoteEntity) = viewModelScope.launch {
        noteRepository.insertNote(note)
    }

    fun deleteNote(id: String) = viewModelScope.launch {
        noteRepository.deleteNote(id)
    }

    fun insertBookmarkedNote(bookmarkedNote: NoteBookMarkEntity) = viewModelScope.launch {
        noteRepository.insertBookmarkedNote(bookmarkedNote)
    }

    fun deleteBookmarkedNote(id: String) = viewModelScope.launch {
        noteRepository.deleteBookmarkedNote(id)
    }

    fun insertDraftedNote(draftNote: NoteDraftEntity) = viewModelScope.launch {
        noteRepository.insertDraftNote(draftNote)
    }

    fun deleteDraftNote(id: String) = viewModelScope.launch {
        noteRepository.deleteDraftNote(id)
    }

    fun insertTrashNote(trashNote: NoteTrashEntity) = viewModelScope.launch {
        noteRepository.insertTrashNote(trashNote)
    }

    fun deleteTrashNote(id: String) = viewModelScope.launch {
        noteRepository.deleteTrashNote(id)
    }
}