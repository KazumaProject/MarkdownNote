package com.kazumaproject.markdownnote.ui.create_edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.emoji.text.EmojiCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.kazumaproject.emojipicker.EmojiPickerDialogFragment
import com.kazumaproject.emojipicker.model.Emoji
import com.kazumaproject.emojipicker.other.convertUnicode
import com.kazumaproject.markdownnote.MainViewModel
import com.kazumaproject.markdownnote.databinding.FragmentCreateEditBinding
import com.kazumaproject.markdownnote.other.FragmentType
import com.kazumaproject.markdownnote.other.collectLatestLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CreateEditFragment : Fragment(), EmojiPickerDialogFragment.EmojiItemClickListener{
    private val createEditViewModel: CreateEditViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()
    private var _binding : FragmentCreateEditBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityViewModel.updateFloatingButtonEnableState(false)
    }
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
        collectLatestLifecycleFlow(createEditViewModel.createEditState){ state ->
            binding.chosenEmojiTextView.apply {
                try {
                    val textStr = EmojiCompat.get().process(state.emoji.unicode.convertUnicode())
                    Timber.d("emoji: $text")
                    text = textStr
                }catch (e: Exception){
                    //** Noting to do **//
                }
            }
            activityViewModel.updateFloatingButtonEnableState(state.currentText.isNotBlank())
        }
        binding.markdownRawEditText.apply {
            addTextChangedListener { editable ->
                editable?.let { input ->
                    createEditViewModel.updateCurrentText(input.toString())
                }
            }
            setOnFocusChangeListener { v, hasFocus ->
                isCursorVisible = hasFocus
                createEditViewModel.updateEditTextHasFocus(hasFocus)
                activityViewModel.updateEditTextInCreateEditHasFocus(hasFocus)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setChooseEmojiView() = binding.changeEmojiParentView.apply {
        setOnClickListener {
            val dialog = EmojiPickerDialogFragment(
                this@CreateEditFragment
            )
            dialog.show(requireActivity().supportFragmentManager,"emoji picker dialog")
        }
    }

    override fun onEmojiClicked(emoji: Emoji) {
        createEditViewModel.updateCurrentEmoji(emoji)
    }

}