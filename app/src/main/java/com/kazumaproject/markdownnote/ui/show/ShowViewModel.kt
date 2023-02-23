package com.kazumaproject.markdownnote.ui.show

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazumaproject.markdownnote.database.note.NoteEntity
import com.kazumaproject.markdownnote.other.Constants.HOME_TO_SHOW_ARGUMENT
import com.kazumaproject.markdownnote.other.Constants.HOME_TO_SHOW_DRAWER_ITEM
import com.kazumaproject.markdownnote.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class ShowNoteState(
    val currentText: String = "",
    val currentUnicode: Int = 0,
    val switchState: Boolean = false,
    val originalText: String = ""
)
@HiltViewModel
class ShowViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository
): ViewModel() {
    val noteId = savedStateHandle.get<String>(HOME_TO_SHOW_ARGUMENT)
    val drawerSelectedItem = savedStateHandle.get<String>(HOME_TO_SHOW_DRAWER_ITEM)
    suspend fun getNote(noteId: String): NoteEntity? = noteRepository.getNoteById(noteId)

    private val _currentText = MutableStateFlow("")
    private val _currentUnicode = MutableStateFlow(0)
    private val _switchState = MutableStateFlow(false)
    private val _originalNoteText = MutableStateFlow("")

    val showNoteState = combine(
        _currentText,
        _currentUnicode,
        _switchState,
        _originalNoteText
    ){ text, unicode, switch, note ->
        ShowNoteState(
            text,
            unicode,
            switch,
            note
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShowNoteState())

    fun updateCurrentText(value: String){
        _currentText.value = value
    }

    fun updateCurrentUnicode(value: Int){
        _currentUnicode.value = value
    }

    fun updateSwitchState(value: Boolean){
        _switchState.value = value
    }

    fun updateOriginalNoteText(value: String){
        _originalNoteText.value = value
    }

}