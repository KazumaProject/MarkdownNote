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
}