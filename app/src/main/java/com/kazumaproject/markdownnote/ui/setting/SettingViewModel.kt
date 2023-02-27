package com.kazumaproject.markdownnote.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazumaproject.markdownnote.database.note.NoteEntity
import com.kazumaproject.markdownnote.preferences.PreferenceImpl
import com.kazumaproject.markdownnote.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val preferenceImpl: PreferenceImpl,
    private val noteRepository: NoteRepository
) : ViewModel() {
    fun getAllNotes() = noteRepository.getAllNotes()

    fun insertAllNotes(notes: List<NoteEntity>) = viewModelScope.launch {
        noteRepository.insertNotes(notes)
    }
}