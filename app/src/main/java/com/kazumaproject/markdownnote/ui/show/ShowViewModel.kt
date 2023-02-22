package com.kazumaproject.markdownnote.ui.show

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kazumaproject.markdownnote.database.note.NoteEntity
import com.kazumaproject.markdownnote.other.Constants.HOME_TO_SHOW_ARGUMENT
import com.kazumaproject.markdownnote.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class ShowViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository
): ViewModel() {
    val noteId = savedStateHandle.get<String>(HOME_TO_SHOW_ARGUMENT)
    suspend fun getNote(noteId: String): NoteEntity? = noteRepository.getNoteById(noteId)
}