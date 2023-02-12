package com.kazumaproject.markdownnote

import androidx.lifecycle.ViewModel
import com.kazumaproject.markdownnote.other.FragmentType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val _current_fragment_type = MutableStateFlow<FragmentType>(FragmentType.HomeFragment)
    val current_fragment_type = _current_fragment_type.asStateFlow()
    fun updateCurrentFragmentType(type: FragmentType){
        _current_fragment_type.value = type
    }

    private val _note_save_request = MutableStateFlow(false)
    val note_save_request = _note_save_request.asStateFlow()
    fun updateNoteSaveRequest(value: Boolean){
        _note_save_request.value = value
    }
}