package com.kazumaproject.markdownnote.ui.home

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.snackbar.Snackbar
import com.kazumaproject.markdownnote.MainViewModel
import com.kazumaproject.markdownnote.R
import com.kazumaproject.markdownnote.adapters.HomeNotesRecyclerViewAdapter
import com.kazumaproject.markdownnote.database.note.NoteEntity
import com.kazumaproject.markdownnote.databinding.FragmentHomeBinding
import com.kazumaproject.markdownnote.drawer.model.DrawerSelectedItem
import com.kazumaproject.markdownnote.other.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import timber.log.Timber


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()
    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var homeNotesRecyclerViewAdapter: HomeNotesRecyclerViewAdapter? = null
    private var initialStart = true
    private var requestSwipeItem = false
    private var onBackPressedCallback: OnBackPressedCallback? = null

    var homeRecylcerViewState: Parcelable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        collectLatestLifecycleFlow(activityViewModel.filteredNotesValue){ filtered_notes ->
            if (initialStart || requestSwipeItem) binding.progressBarHomeFragment.isVisible = true
            binding.homeNotesRecyclerView.isEnabled = false
            delay(1)
            binding.homeNotesRecyclerView.isEnabled = true
            binding.progressBarHomeFragment.isVisible = false
            initialStart = false
            requestSwipeItem = false
            Timber.d("all trash notes: ${activityViewModel.dataBaseValues.value.allTrashNotes}\ncount: ${activityViewModel.dataBaseValues.value.allTrashNotes.size}")

            onBackPressedCallback = object: OnBackPressedCallback(true){
                override fun handleOnBackPressed() {
                    when(filtered_notes.currentDrawerSelectedItem){
                        is DrawerSelectedItem.AllNotes -> {
                            requireActivity().finish()
                        }
                        is DrawerSelectedItem.BookmarkedNotes -> {
                            activityViewModel.updateCurrentSelectedDrawerItem(DrawerSelectedItem.AllNotes)
                        }
                        is DrawerSelectedItem.DraftNotes -> {
                            activityViewModel.updateCurrentSelectedDrawerItem(DrawerSelectedItem.AllNotes)
                        }
                        is DrawerSelectedItem.TrashNotes -> {
                            activityViewModel.updateCurrentSelectedDrawerItem(DrawerSelectedItem.AllNotes)
                        }
                        is DrawerSelectedItem.EmojiCategory ->{
                            activityViewModel.updateCurrentSelectedDrawerItem(DrawerSelectedItem.AllNotes)
                        }
                        is DrawerSelectedItem.ReadFile ->{
                            requireActivity().finish()
                        }
                        is DrawerSelectedItem.ReadApplicationFile ->{
                            requireActivity().finish()
                        }
                        is DrawerSelectedItem.GoToSettings -> {
                            requireActivity().finish()
                        }
                    }
                }
            }

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
                is DrawerSelectedItem.ReadFile -> {
                    binding.currentSelectedItemTitle.text = getString(R.string.all_notes)
                }
                is DrawerSelectedItem.ReadApplicationFile ->{
                    binding.currentSelectedItemTitle.text = getString(R.string.all_notes)
                }
                is DrawerSelectedItem.GoToSettings -> {
                    binding.currentSelectedItemTitle.text = getString(R.string.all_notes)
                }
            }
            val filteredNotes: List<NoteEntity> = when(filtered_notes.currentDrawerSelectedItem){
                is DrawerSelectedItem.AllNotes -> filtered_notes.allNotes.filter { note ->
                    note.id !in activityViewModel.dataBaseValues.value.allTrashNotes.map {
                        it.id
                    }
                }
                is DrawerSelectedItem.BookmarkedNotes -> filtered_notes.allNotes.filter { note ->
                    activityViewModel.dataBaseValues.value.allBookmarkNotes.contains(note.convertNoteBookMarkEntity())
                }
                is DrawerSelectedItem.DraftNotes -> activityViewModel.dataBaseValues.value.allDraftNotes.map {
                    it.convertNoteEntity()
                }
                is DrawerSelectedItem.TrashNotes -> filtered_notes.allNotes.filter { note ->
                    activityViewModel.dataBaseValues.value.allTrashNotes.contains(note.convertNoteTrashEntity())
                }
                is DrawerSelectedItem.EmojiCategory -> filtered_notes.allNotes.filter {
                    it.emojiUnicode == filtered_notes.currentDrawerSelectedItem.unicode
                }.filter { note ->
                    !activityViewModel.dataBaseValues.value.allTrashNotes.contains(note.convertNoteTrashEntity())
                }
                is DrawerSelectedItem.ReadFile -> filtered_notes.allNotes.filter { note ->
                    !activityViewModel.dataBaseValues.value.allTrashNotes.contains(note.convertNoteTrashEntity())
                }
                is DrawerSelectedItem.ReadApplicationFile -> filtered_notes.allNotes.filter { note ->
                    !activityViewModel.dataBaseValues.value.allTrashNotes.contains(note.convertNoteTrashEntity())
                }
                is DrawerSelectedItem.GoToSettings -> filtered_notes.allNotes.filter { note ->
                    !activityViewModel.dataBaseValues.value.allTrashNotes.contains(note.convertNoteTrashEntity())
                }
            }
            homeNotesRecyclerViewAdapter = HomeNotesRecyclerViewAdapter(filtered_notes.allBookmarkNotes, filtered_notes.currentDrawerSelectedItem)
            setRecyclerView(filteredNotes, homeNotesRecyclerViewAdapter, filtered_notes.currentDrawerSelectedItem)
            binding.homeNotesRecyclerView.layoutManager?.onRestoreInstanceState(homeRecylcerViewState)
            setSwipeRefreshLayout(filteredNotes, homeNotesRecyclerViewAdapter)
            setSearchView(filteredNotes, homeNotesRecyclerViewAdapter)
            onBackPressedCallback?.let { backPressed ->
                requireActivity().onBackPressedDispatcher.addCallback(backPressed)
            }
            Timber.d("current filtered notes: $filteredNotes\ncounts: ${filteredNotes.size}")
        }

        binding.homeNotesRecyclerView.apply {
            val itemTouchHelper = ItemTouchHelper( object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    homeNotesRecyclerViewAdapter?.let { noteAdapter ->
                        homeRecylcerViewState = binding.homeNotesRecyclerView.layoutManager?.onSaveInstanceState()
                        when(activityViewModel.filteredNotesValue.value.currentDrawerSelectedItem){
                            is DrawerSelectedItem.AllNotes, is DrawerSelectedItem.GoToSettings, is DrawerSelectedItem.ReadFile, is DrawerSelectedItem.ReadApplicationFile, -> {
                                val note = noteAdapter.filtered_notes[viewHolder.layoutPosition]
                                homeViewModel.insertTrashNote(note.convertNoteTrashEntity())
                                val bookmarksList = activityViewModel.dataBaseValues.value.allBookmarkNotes.map {
                                    it.id
                                }
                                if (bookmarksList.contains(note.id)) {
                                    homeViewModel.deleteBookmarkedNote(note.id)
                                    Snackbar.make(
                                        requireView(),
                                        "${note.body.getTitleFromNote()} ${getString(R.string.deleted_message)}",
                                        Snackbar.LENGTH_LONG
                                    ).apply {
                                        setAction(getString(R.string.undo_message)){
                                            homeViewModel.deleteTrashNote(note.id)
                                            homeViewModel.insertBookmarkedNote(note.convertNoteBookMarkEntity())
                                        }
                                    }.show()
                                } else {
                                    Snackbar.make(
                                        requireView(),
                                        "${note.body.getTitleFromNote()} ${getString(R.string.deleted_message)}",
                                        Snackbar.LENGTH_LONG
                                    ).apply {
                                        setAction(getString(R.string.undo_message)){
                                            homeViewModel.deleteTrashNote(note.id)
                                        }
                                    }.show()
                                }
                            }
                            is DrawerSelectedItem.TrashNotes -> {
                                val note = noteAdapter.filtered_notes[viewHolder.layoutPosition]
                                homeViewModel.deleteTrashNote(note.id)
                                homeViewModel.deleteNote(note.id)
                            }
                            is DrawerSelectedItem.EmojiCategory -> {
                                val note = noteAdapter.filtered_notes[viewHolder.layoutPosition]
                                val bookmarksList = activityViewModel.dataBaseValues.value.allBookmarkNotes.map {
                                    it.id
                                }
                                homeViewModel.insertTrashNote(note.convertNoteTrashEntity())
                                if (bookmarksList.contains(note.id)) {
                                    homeViewModel.deleteBookmarkedNote(note.id)
                                    Snackbar.make(
                                        requireView(),
                                        "${note.body.getTitleFromNote()} ${getString(R.string.deleted_message)}",
                                        Snackbar.LENGTH_LONG
                                    ).apply {
                                        setAction(getString(R.string.undo_message)){
                                            homeViewModel.deleteTrashNote(note.id)
                                            homeViewModel.insertBookmarkedNote(note.convertNoteBookMarkEntity())
                                        }
                                    }.show()
                                } else {
                                    Snackbar.make(
                                        requireView(),
                                        "${note.body.getTitleFromNote()} ${getString(R.string.deleted_message)}",
                                        Snackbar.LENGTH_LONG
                                    ).apply {
                                        setAction(getString(R.string.undo_message)){
                                            homeViewModel.deleteTrashNote(note.id)
                                        }
                                    }.show()
                                }
                            }
                            is DrawerSelectedItem.DraftNotes -> {
                                val note = noteAdapter.filtered_notes[viewHolder.layoutPosition]
                                homeViewModel.deleteDraftNote(note.id)
                            }
                            is DrawerSelectedItem.BookmarkedNotes -> {
                                val note = noteAdapter.filtered_notes[viewHolder.layoutPosition]
                                homeViewModel.deleteBookmarkedNote(note.id)
                                homeViewModel.insertTrashNote(note.convertNoteTrashEntity())
                                Snackbar.make(
                                    requireView(),
                                    "${note.body.getTitleFromNote()} ${getString(R.string.deleted_message)}",
                                    Snackbar.LENGTH_LONG
                                ).apply {
                                    setAction(getString(R.string.undo_message)){
                                        homeViewModel.insertBookmarkedNote(note.convertNoteBookMarkEntity())
                                        homeViewModel.deleteTrashNote(note.id)
                                    }
                                }.show()
                            }
                        }
                    }
                    requestSwipeItem = true
                }
            })
            itemTouchHelper.attachToRecyclerView(this)
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
        initialStart = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        homeNotesRecyclerViewAdapter = null
        _binding = null
        onBackPressedCallback = null
    }

    private fun setRecyclerView(
        notes: List<NoteEntity>,
        homeNotesAdapter: HomeNotesRecyclerViewAdapter?,
        drawerSelectedItem: DrawerSelectedItem
    ) = binding.homeNotesRecyclerView.apply {
        homeNotesAdapter?.let { noteAdapter ->
            noteAdapter.filtered_notes = notes
            this@apply.adapter = noteAdapter
            noteAdapter.setOnItemClickListener { noteEntity, i ->
                requireActivity().findViewById<BottomAppBar>(R.id.bottom_app_bar).performShow()
                when(drawerSelectedItem){
                    is DrawerSelectedItem.AllNotes -> requireActivity().findNavController(R.id.navHostFragment).navigate(
                        HomeFragmentDirections.actionHomeFragmentToDraftFragment(
                            noteEntity.id,
                            DrawerSelectedItemInShow.ALL_NOTE.name,
                            NoteType.NORMAL.name
                        )
                    )
                    is DrawerSelectedItem.BookmarkedNotes -> requireActivity().findNavController(R.id.navHostFragment).navigate(
                        HomeFragmentDirections.actionHomeFragmentToDraftFragment(
                            noteEntity.id,
                            DrawerSelectedItemInShow.BOOKMARKED.name,
                            NoteType.NORMAL.name
                        )
                    )
                    is DrawerSelectedItem.DraftNotes -> requireActivity().findNavController(R.id.navHostFragment).navigate(
                        HomeFragmentDirections.actionHomeFragmentToDraftFragment(
                            noteEntity.id,
                            DrawerSelectedItemInShow.DRAFTS.name,
                            NoteType.DRAFT.name
                        )
                    )
                    is DrawerSelectedItem.TrashNotes -> requireActivity().findNavController(R.id.navHostFragment).navigate(
                        HomeFragmentDirections.actionHomeFragmentToDraftFragment(
                            noteEntity.id,
                            DrawerSelectedItemInShow.TRASH.name,
                            NoteType.NORMAL.name
                        )
                    )
                    is DrawerSelectedItem.EmojiCategory -> requireActivity().findNavController(R.id.navHostFragment).navigate(
                        HomeFragmentDirections.actionHomeFragmentToDraftFragment(
                            noteEntity.id,
                            DrawerSelectedItemInShow.EMOJI.name,
                            NoteType.NORMAL.name
                        )
                    )
                    is DrawerSelectedItem.ReadFile, is DrawerSelectedItem.ReadApplicationFile, is DrawerSelectedItem.GoToSettings -> requireActivity().findNavController(R.id.navHostFragment).navigate(
                        HomeFragmentDirections.actionHomeFragmentToDraftFragment(
                            noteEntity.id,
                            DrawerSelectedItemInShow.ALL_NOTE.name,
                            NoteType.NORMAL.name
                        )
                    )
                }
            }
            noteAdapter.setOnItemLikedClickListener { noteEntity, i, isSelected ->
                Timber.d("clicked note: $noteEntity\nindex: $i\nselected: $isSelected")
                requireActivity().findViewById<BottomAppBar>(R.id.bottom_app_bar).performShow()
                if (!isSelected) homeViewModel.insertBookmarkedNote(noteEntity.convertNoteBookMarkEntity()) else homeViewModel.deleteBookmarkedNote(noteEntity.id)
                homeRecylcerViewState = binding.homeNotesRecyclerView.layoutManager?.onSaveInstanceState()
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