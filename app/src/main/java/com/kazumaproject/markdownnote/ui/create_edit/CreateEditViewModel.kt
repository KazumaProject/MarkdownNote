package com.kazumaproject.markdownnote.ui.create_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazumaproject.emojipicker.model.Emoji
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class CreateEditState(
    val currentText: String = "",
    val editTextHasFocus: Boolean = false,
    val emoji: Emoji = Emoji(
        539,
        "dog",
        0x1f415
    )
)
@HiltViewModel
class CreateEditViewModel @Inject constructor() : ViewModel() {

    private val _currentText = MutableStateFlow("")
    private val _editTextHasFocus = MutableStateFlow(false)
    private var _currentEmoji = MutableStateFlow(
        Emoji(
            539,
            "dog",
            0x1f415
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

}