package com.kazumaproject.markdownnote.ui.create_edit

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.emoji.text.EmojiCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.kazumaproject.emojipicker.Constants.EMOJI_LIST_SMILEYS_PEOPLE
import com.kazumaproject.emojipicker.EmojiPickerDialogFragment
import com.kazumaproject.emojipicker.convertUnicode
import com.kazumaproject.markdownnote.MainViewModel
import com.kazumaproject.markdownnote.databinding.FragmentCreateEditBinding
import com.kazumaproject.markdownnote.other.FragmentType
import timber.log.Timber

class CreateEditFragment : Fragment() {
    private val createEditViewModel: CreateEditViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()
    private var _binding : FragmentCreateEditBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityViewModel.updateCurrentFragmentType(FragmentType.CreateEditFragment)
        setEmojiTextView()
        setChooseEmojiView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setChooseEmojiView() = binding.changeEmojiParentView.apply {
        setOnClickListener {
            val dialog = EmojiPickerDialogFragment(binding.chosenEmojiTextView)
            dialog.show(requireActivity().supportFragmentManager,"emoji picker dialog")
        }
    }

    private fun setEmojiTextView() = binding.chosenEmojiTextView.apply {
        try {
            val textStr = EmojiCompat.get().process(EMOJI_LIST_SMILEYS_PEOPLE[10].unicode.convertUnicode())
            Timber.d("emoji: $text")
            text = textStr
        }catch (e: Exception){
            //** Noting to do **//
        }
    }

}