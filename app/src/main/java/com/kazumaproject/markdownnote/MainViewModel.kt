package com.kazumaproject.markdownnote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazumaproject.markdownnote.database.note.NoteEntity
import com.kazumaproject.markdownnote.database.note_bookmark.NoteBookMarkEntity
import com.kazumaproject.markdownnote.database.note_draft.NoteDraftEntity
import com.kazumaproject.markdownnote.database.note_trash.NoteTrashEntity
import com.kazumaproject.markdownnote.drawer.model.DrawerSelectedItem
import com.kazumaproject.markdownnote.other.FragmentType
import com.kazumaproject.markdownnote.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class FragmentAndFloatingButtonState(
    val currentFragmentType: FragmentType = FragmentType.HomeFragment,
    val floatingButtonState: Boolean = true,
    val hasFocus: Boolean = false
)

data class DatabaseValues(
    val allNotes: List<NoteEntity> = emptyList(),
    val allDraftNotes: List<NoteDraftEntity> = emptyList(),
    val allTrashNotes: List<NoteTrashEntity> = emptyList(),
    val allBookmarkNotes: List<NoteBookMarkEntity> = emptyList()
)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {
    private val _current_fragment_type = MutableStateFlow<FragmentType>(FragmentType.HomeFragment)
    private val _floating_button_enable_state = MutableStateFlow(true)
    private val _hasFocus = MutableStateFlow(false)

    val fragmentAndFloatingButtonState = combine(_current_fragment_type, _floating_button_enable_state, _hasFocus) { fragmentType, floatingButtonState, editTextHasFocus ->
        FragmentAndFloatingButtonState(
            currentFragmentType = fragmentType,
            floatingButtonState = floatingButtonState,
            hasFocus = editTextHasFocus
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FragmentAndFloatingButtonState())

    fun updateCurrentFragmentType(type: FragmentType){
        _current_fragment_type.value = type
    }
    fun updateFloatingButtonEnableState(value: Boolean){
        _floating_button_enable_state.value = value
    }
    fun updateHasFocusInEditText(value: Boolean){
        _hasFocus.value = value
    }

    private val _markdown_switch_state = MutableStateFlow(false)
    val markdown_switch_state = _markdown_switch_state.asStateFlow()

    fun updateMarkdownSwitchState(value: Boolean){
        _markdown_switch_state.value = value
    }

    private var _saveClicked = MutableStateFlow(false)
    val saveClicked = _saveClicked.asStateFlow()

    fun updateSaveClicked(value: Boolean){
        _saveClicked.value = value
    }

    private fun getAllNotes(): Flow<List<NoteEntity>>{
        return noteRepository.getAllNotes()
    }

    private fun getAllDraftNotes(): Flow<List<NoteDraftEntity>>{
        return noteRepository.getAllDraftNotes()
    }

    private fun getAllTrashNotes(): Flow<List<NoteTrashEntity>>{
        return noteRepository.getAllTrashNotes()
    }

    private fun getAllBookmarkNotes(): Flow<List<NoteBookMarkEntity>>{
        return noteRepository.getAllBookmarkNotes()
    }

    val dataBaseValues = combine(
        getAllNotes(),
        getAllDraftNotes(),
        getAllTrashNotes(),
        getAllBookmarkNotes()
    ){ notes, drafts, trash, bookmarks ->
        DatabaseValues(
            allNotes = notes,
            allDraftNotes = drafts,
            allTrashNotes = trash,
            allBookmarkNotes = bookmarks
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DatabaseValues())

    private var _current_selected_drawer_item = MutableStateFlow<DrawerSelectedItem>(DrawerSelectedItem.AllNotes)
    val current_selected_drawer_item = _current_selected_drawer_item.asStateFlow()
    fun updateCurrentSelectedDrawerItem(value: DrawerSelectedItem){
        _current_selected_drawer_item.value = value
    }
}