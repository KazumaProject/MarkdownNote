package com.kazumaproject.markdownnote.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.kazumaproject.markdownnote.MainViewModel
import com.kazumaproject.markdownnote.database.note.NoteEntity
import com.kazumaproject.markdownnote.databinding.FragmentHomeBinding
import com.kazumaproject.markdownnote.drawer.model.DrawerSelectedItem
import com.kazumaproject.markdownnote.other.FragmentType
import com.kazumaproject.markdownnote.other.collectLatestLifecycleFlow
import com.kazumaproject.markdownnote.other.convertNoteEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()
    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback {
            requireActivity().finish()
        }

        collectLatestLifecycleFlow(activityViewModel.current_selected_drawer_item){ selected_drawer_item ->
            val filteredNotes: List<NoteEntity> = when(selected_drawer_item){
                is DrawerSelectedItem.AllNotes -> {
                    delay(150)
                    activityViewModel.dataBaseValues.value.allNotes.map {
                        it
                    }
                }
                is DrawerSelectedItem.BookmarkedNotes -> {
                    activityViewModel.dataBaseValues.value.allBookmarkNotes.map {
                        it.convertNoteEntity()
                    }
                }
                is DrawerSelectedItem.DraftNotes -> {
                    activityViewModel.dataBaseValues.value.allDraftNotes.map {
                        it.convertNoteEntity()
                    }
                }
                is DrawerSelectedItem.TrashNotes -> {
                    activityViewModel.dataBaseValues.value.allTrashNotes.map {
                        it.convertNoteEntity()
                    }
                }
                is DrawerSelectedItem.EmojiCategory ->{
                    activityViewModel.dataBaseValues.value.allNotes.filter {
                        it.emojiUnicode == selected_drawer_item.unicode
                    }
                }
                is DrawerSelectedItem.GoToSettings -> {
                    activityViewModel.dataBaseValues.value.allNotes
                }
            }
            Timber.d("current filtered notes: $filteredNotes\ncounts: ${filteredNotes.size}")
        }

        activityViewModel.updateCurrentFragmentType(FragmentType.HomeFragment)
        activityViewModel.updateFloatingButtonEnableState(true)
        activityViewModel.updateSaveClicked(false)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}