package com.kazumaproject.markdownnote.ui.show

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazumaproject.markdownnote.database.note.NoteEntity
import com.kazumaproject.markdownnote.database.note_bookmark.NoteBookMarkEntity
import com.kazumaproject.markdownnote.database.note_draft.NoteDraftEntity
import com.kazumaproject.markdownnote.database.note_trash.NoteTrashEntity
import com.kazumaproject.markdownnote.other.Constants.HOME_TO_SHOW_ARGUMENT
import com.kazumaproject.markdownnote.other.Constants.HOME_TO_SHOW_DRAWER_ITEM
import com.kazumaproject.markdownnote.other.Constants.HOME_TO_SHOW_NOTE_TYPE
import com.kazumaproject.markdownnote.preferences.PreferenceImpl
import com.kazumaproject.markdownnote.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShowNoteState(
    val currentText: String = "",
    val currentUnicode: Int = 0,
    val originalText: String = "",
    val originalUnicode: Int = 0,
    val currentNoteType: String = ""
)

data class NoteDataBaseData(
    val noteId: String = "",
    val createdAt: Long = 0L,
)
@HiltViewModel
class ShowViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
    private val preferenceImpl: PreferenceImpl
): ViewModel() {
    val noteId = savedStateHandle.get<String>(HOME_TO_SHOW_ARGUMENT)
    val drawerSelectedItem = savedStateHandle.get<String>(HOME_TO_SHOW_DRAWER_ITEM)
    val noteType = savedStateHandle.get<String>(HOME_TO_SHOW_NOTE_TYPE)
    suspend fun getNote(noteId: String): NoteEntity? = noteRepository.getNoteById(noteId)
    suspend fun getDraftNote(noteId: String): NoteDraftEntity? = noteRepository.getDraftNoteById(noteId)

    private val _currentText = MutableStateFlow("")
    private val _currentUnicode = MutableStateFlow(0)
    private val _originalNoteText = MutableStateFlow("")
    private val _originalNoteUnicode = MutableStateFlow(0)

    private val _current_note_id = MutableStateFlow("")
    private val _note_create_at = MutableStateFlow(0L)

    private val _currentNoteType = MutableStateFlow("")

    val showNoteState = combine(
        _currentText,
        _currentUnicode,
        _originalNoteText,
        _originalNoteUnicode,
        _currentNoteType,
    ){ text, unicode, note, original_unicode, type ->
        ShowNoteState(
            text,
            unicode,
            note,
            original_unicode,
            type
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShowNoteState())


    val noteDataBaseData = combine(
        _current_note_id,
        _note_create_at,
    ){ noteId, createAt ->
        NoteDataBaseData(
            noteId = noteId,
            createdAt = createAt,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NoteDataBaseData())

    private val _switchState = MutableStateFlow(false)
    val switchState = _switchState.asStateFlow()

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

    fun updateNoteId(value: String){
        _current_note_id.value = value
    }

    fun updateNoteCreatedAt(value: Long){
        _note_create_at.value = value
    }

    fun updateOriginUnicode(value: Int){
        _originalNoteUnicode.value = value
    }

    fun updateCurrentNoteType(value: String){
        _currentNoteType.value = value
    }

    fun insertNote(noteEntity: NoteEntity) = viewModelScope.launch {
        noteRepository.insertNote(noteEntity)
    }

    fun insertDraftNote(noteDraftEntity: NoteDraftEntity) = viewModelScope.launch {
        noteRepository.insertDraftNote(noteDraftEntity)
    }

    fun insertBookmarkNote(noteNookMarkEntity: NoteBookMarkEntity) = viewModelScope.launch {
        noteRepository.insertBookmarkedNote(noteNookMarkEntity)
    }
    suspend fun getBookmarkNote(noteId: String): NoteBookMarkEntity?{
        return noteRepository.getBookmarkedNoteById(noteId)
    }

    fun insertTrashNote(noteTrashEntity: NoteTrashEntity) = viewModelScope.launch {
        noteRepository.insertTrashNote(noteTrashEntity)
    }

    fun deleteNote(noteId: String) = viewModelScope.launch {
        noteRepository.deleteNote(noteId)
    }

    fun deleteBookmarkedNote(noteId: String) = viewModelScope.launch {
        noteRepository.deleteBookmarkedNote(noteId)
    }

    fun deleteTrashNote(noteId: String) = viewModelScope.launch {
        noteRepository.deleteTrashNote(noteId)
    }

    fun deleteDraftNote(noteId: String) = viewModelScope.launch {
        noteRepository.deleteDraftNote(noteId)
    }

    fun getSyntaxType():String{
        return preferenceImpl.getSyntaxInEditTextType()
    }
}