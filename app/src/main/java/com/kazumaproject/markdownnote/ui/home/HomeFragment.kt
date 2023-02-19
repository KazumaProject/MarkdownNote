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
import com.kazumaproject.markdownnote.databinding.FragmentHomeBinding
import com.kazumaproject.markdownnote.other.FragmentType
import com.kazumaproject.markdownnote.other.collectLatestLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
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

        collectLatestLifecycleFlow(homeViewModel.getAllNotes()){ notes ->
            val filteredNotes = notes.filter { note ->
                !note.draft && !note.trash
            }
            Timber.d("current all notes: $notes\ncounts: ${notes.size}\nfiltered notes: $filteredNotes")
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