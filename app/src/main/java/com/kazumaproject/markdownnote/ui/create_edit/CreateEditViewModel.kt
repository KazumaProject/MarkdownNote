package com.kazumaproject.markdownnote.ui.create_edit

import androidx.lifecycle.ViewModel
import com.kazumaproject.emojipicker.model.Emoji
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CreateEditViewModel @Inject constructor() : ViewModel() {

    private var _currentEmoji = MutableStateFlow(
        Emoji(
            539,
            "dog",
            0x1f415
        )
    )

    val currentEmoji = _currentEmoji.asStateFlow()

    fun updateCurrentEmoji(emoji: Emoji){
        _currentEmoji.value = emoji
    }

}