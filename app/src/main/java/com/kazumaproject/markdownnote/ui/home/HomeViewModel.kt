package com.kazumaproject.markdownnote.ui.home

import androidx.lifecycle.ViewModel
import com.kazumaproject.markdownnote.database.note.NoteEntity
import com.kazumaproject.markdownnote.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private var _allNote = MutableStateFlow<List<NoteEntity>>(
        emptyList()
    )

    val allNotes = _allNote.asStateFlow()

    fun getAllNotes(): Flow<List<NoteEntity>>{
        return noteRepository.getAllNotes()
    }

}