package com.kazumaproject.markdownnote.ui.home

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.google.android.material.bottomappbar.BottomAppBar
import com.kazumaproject.markdownnote.MainViewModel
import com.kazumaproject.markdownnote.R
import com.kazumaproject.markdownnote.adapters.HomeNotesRecyclerViewAdapter
import com.kazumaproject.markdownnote.database.note.NoteEntity
import com.kazumaproject.markdownnote.databinding.FragmentHomeBinding
import com.kazumaproject.markdownnote.drawer.model.DrawerSelectedItem
import com.kazumaproject.markdownnote.other.FragmentType
import com.kazumaproject.markdownnote.other.collectLatestLifecycleFlow
import com.kazumaproject.markdownnote.other.convertNoteBookMarkEntity
import com.kazumaproject.markdownnote.other.convertNoteEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()
    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var homeNotesRecyclerViewAdapter: HomeNotesRecyclerViewAdapter? = null


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

        collectLatestLifecycleFlow(activityViewModel.filteredNotesValue){ filtered_notes ->
            delay(800)
            val recyclerViewState: Parcelable? = binding.homeNotesRecyclerView.layoutManager?.onSaveInstanceState()
            binding.homeNotesRecyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
            when(filtered_notes.currentDrawerSelectedItem){
                is DrawerSelectedItem.AllNotes -> {
                   binding.currentSelectedItemTitle.text = getString(R.string.all_notes)
                }
                is DrawerSelectedItem.BookmarkedNotes -> {
                    binding.currentSelectedItemTitle.text = getString(R.string.bookmarked_notes)
                }
                is DrawerSelectedItem.DraftNotes -> {
                    binding.currentSelectedItemTitle.text = getString(R.string.draft_notes)
                }
                is DrawerSelectedItem.TrashNotes -> {
                    binding.currentSelectedItemTitle.text = getString(R.string.trash_notes)
                }
                is DrawerSelectedItem.EmojiCategory ->{
                    binding.currentSelectedItemTitle.text = getString(R.string.emoji_string)
                }
                is DrawerSelectedItem.GoToSettings -> {
                    binding.currentSelectedItemTitle.text = getString(R.string.all_notes)
                }
            }
            val filteredNotes: List<NoteEntity> = when(filtered_notes.currentDrawerSelectedItem){
                is DrawerSelectedItem.AllNotes -> filtered_notes.allNotes
                is DrawerSelectedItem.BookmarkedNotes -> activityViewModel.dataBaseValues.value.allBookmarkNotes.map {
                    it.convertNoteEntity()
                }
                is DrawerSelectedItem.DraftNotes -> activityViewModel.dataBaseValues.value.allDraftNotes.map {
                    it.convertNoteEntity()
                }
                is DrawerSelectedItem.TrashNotes -> activityViewModel.dataBaseValues.value.allTrashNotes.map {
                    it.convertNoteEntity()
                }
                is DrawerSelectedItem.EmojiCategory -> filtered_notes.allNotes.filter {
                    it.emojiUnicode == filtered_notes.currentDrawerSelectedItem.unicode
                }
                is DrawerSelectedItem.GoToSettings -> filtered_notes.allNotes
            }
            homeNotesRecyclerViewAdapter = HomeNotesRecyclerViewAdapter(filtered_notes.allBookmarkNotes)
            setRecyclerView(filteredNotes, homeNotesRecyclerViewAdapter)
            setSwipeRefreshLayout(filteredNotes, homeNotesRecyclerViewAdapter)
            setSearchView(filteredNotes, homeNotesRecyclerViewAdapter)
            Timber.d("current filtered notes: $filteredNotes\ncounts: ${filteredNotes.size}")
        }

        activityViewModel.updateCurrentFragmentType(FragmentType.HomeFragment)
        activityViewModel.updateFloatingButtonEnableState(true)
        activityViewModel.updateSaveClicked(false)
    }

    override fun onPause() {
        super.onPause()
        binding.homeSearchView.apply {
            setQuery("",false)
            clearFocus()
            isIconified = true
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        homeNotesRecyclerViewAdapter = null
        _binding = null
    }

    private fun setRecyclerView(
        notes: List<NoteEntity>,
        homeNotesAdapter: HomeNotesRecyclerViewAdapter?
    ) = binding.homeNotesRecyclerView.apply {
        homeNotesAdapter?.let { noteAdapter ->
            noteAdapter.filtered_notes = notes
            this@apply.adapter = noteAdapter
            noteAdapter.setOnItemClickListener { noteEntity, i ->
                requireActivity().findViewById<BottomAppBar>(R.id.bottom_app_bar).performShow()
                requireActivity().findNavController(R.id.navHostFragment).navigate(
                    HomeFragmentDirections.actionHomeFragmentToDraftFragment()
                )
            }
            noteAdapter.setOnItemLikedClickListener { noteEntity, i, isSelected ->
                Timber.d("clicked note: $noteEntity\nindex: $i\nselected: $isSelected")
                if (!isSelected) homeViewModel.insertBookmarkedNote(noteEntity.convertNoteBookMarkEntity()) else homeViewModel.deleteBookmarkedNote(noteEntity.id)
                CoroutineScope(Dispatchers.Main).launch {
                    val state: Parcelable? = layoutManager?.onSaveInstanceState()
                    delay(804)
                    layoutManager?.onRestoreInstanceState(state)
                }
            }

        }
        this.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setSwipeRefreshLayout(notes: List<NoteEntity>, adapter: HomeNotesRecyclerViewAdapter?){
        binding.homeFragmentSwipeRefreshLayout.apply {
            setOnRefreshListener {
                adapter?.let { noteAdapter ->
                    noteAdapter.filtered_notes = notes
                    binding.homeNotesRecyclerView.adapter = noteAdapter
                }
                isRefreshing = false
            }
        }
    }

    private fun setSearchView(notes: List<NoteEntity>, homeNotesAdapter: HomeNotesRecyclerViewAdapter?) = binding.homeSearchView.apply {
        setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { text ->
                    homeNotesAdapter?.let { noteAdapter ->
                        noteAdapter.filtered_notes = notes.filter {
                            it.body.contains(text)
                        }
                        binding.homeNotesRecyclerView.adapter = noteAdapter
                    }
                }
                return false
            }
        })
        setOnQueryTextFocusChangeListener { _, hasFocus ->
            requireActivity().findViewById<BottomAppBar>(R.id.bottom_app_bar).menu.findItem(R.id.bottom_bar_item_back_arrow).isVisible = !hasFocus
        }
    }

}