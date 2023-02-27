package com.kazumaproject.markdownnote.ui.create_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazumaproject.emojipicker.model.Emoji
import com.kazumaproject.emojipicker.other.Constants
import com.kazumaproject.markdownnote.database.note.NoteEntity
import com.kazumaproject.markdownnote.database.note_draft.NoteDraftEntity
import com.kazumaproject.markdownnote.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class CreateEditState(
    val currentText: String = "",
    val editTextHasFocus: Boolean = false,
    val emoji: Emoji = Emoji(
        0,
        "initial_emoji",
        Constants.EMOJI_LIST_ANIMALS_NATURE[(0 until Constants.EMOJI_LIST_ANIMALS_NATURE.size).random()].unicode
    )
)
@HiltViewModel
class CreateEditViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _currentText = MutableStateFlow("")
    private val _editTextHasFocus = MutableStateFlow(false)
    private var _currentEmoji = MutableStateFlow(
        Emoji(
            0,
            "initial_emoji",
            Constants.EMOJI_LIST_ANIMALS_NATURE[(0 until Constants.EMOJI_LIST_ANIMALS_NATURE.size).random()].unicode
        )
    )

    val createEditState = combine(_currentText, _editTextHasFocus, _currentEmoji) {text, focus, emoji ->
        CreateEditState(
            currentText = text,
            editTextHasFocus = focus,
            emoji = emoji
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CreateEditState())

    fun updateCurrentEmoji(emoji: Emoji){
        _currentEmoji.value = emoji
    }

    fun updateCurrentText(text: String){
        _currentText.value = text
    }

    fun updateEditTextHasFocus(value: Boolean){
        _editTextHasFocus.value = value
    }

    /**************************
      Database methods start
     **************************/

    fun insertNote(note: NoteEntity) = viewModelScope.launch {
        Timber.d("insert note called")
        noteRepository.insertNote(note)
    }

    fun insertDraftNote(noteDraftEntity: NoteDraftEntity) = viewModelScope.launch {
        noteRepository.insertDraftNote(noteDraftEntity)
    }

}