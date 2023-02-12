package com.kazumaproject.markdownnote.ui.create_edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.emoji.text.EmojiCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.kazumaproject.markdownnote.emojipicker.Constants.EMOJI_LIST_SMILEYS_PEOPLE
import com.kazumaproject.markdownnote.emojipicker.EmojiPickerDialogFragment
import com.kazumaproject.markdownnote.emojipicker.convertUnicode
import com.kazumaproject.markdownnote.MainViewModel
import com.kazumaproject.markdownnote.databinding.FragmentCreateEditBinding
import com.kazumaproject.markdownnote.other.FragmentType
import com.kazumaproject.markdownnote.other.collectLatestLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CreateEditFragment : Fragment(){
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
        setChooseEmojiView()
        collectLatestLifecycleFlow(createEditViewModel.currentEmoji){ current_emoji ->
            Timber.d("current emoji: ${current_emoji.unicode.convertUnicode()}\nname: ${current_emoji.emoji_short_name}\nunicode: ${current_emoji.unicode}")
            binding.chosenEmojiTextView.apply {
                try {
                    val textStr = EmojiCompat.get().process(current_emoji.unicode.convertUnicode())
                    Timber.d("emoji: $text")
                    text = textStr
                }catch (e: Exception){
                    //** Noting to do **//
                }
            }
        }
        collectLatestLifecycleFlow(activityViewModel.note_save_request){
            if (it){
                Timber.d("current selected emoji: ${binding.chosenEmojiTextView.text}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setChooseEmojiView() = binding.changeEmojiParentView.apply {
        setOnClickListener {
            val dialog = EmojiPickerDialogFragment(createEditViewModel)
            dialog.show(requireActivity().supportFragmentManager,"emoji picker dialog")
        }
    }

}