package com.kazumaproject.markdownnote.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazumaproject.markdownnote.database.note_bookmark.NoteBookMarkEntity
import com.kazumaproject.markdownnote.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {
    fun insertBookmarkedNote(bookmarkedNote: NoteBookMarkEntity) = viewModelScope.launch {
        noteRepository.insertBookmarkedNote(bookmarkedNote)
    }

    fun deleteBookmarkedNote(id: String) = viewModelScope.launch {
        noteRepository.deleteBookmarkedNote(id)
    }
}