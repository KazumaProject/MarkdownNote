package com.kazumaproject.markdownnote.ui.create_edit

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.emoji.text.EmojiCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.kazumaproject.emojipicker.EmojiPickerDialogFragment
import com.kazumaproject.emojipicker.model.Emoji
import com.kazumaproject.emojipicker.other.convertUnicode
import com.kazumaproject.markdownnote.MainViewModel
import com.kazumaproject.markdownnote.database.note.NoteEntity
import com.kazumaproject.markdownnote.database.note_draft.NoteDraftEntity
import com.kazumaproject.markdownnote.databinding.FragmentCreateEditBinding
import com.kazumaproject.markdownnote.other.FragmentType
import com.kazumaproject.markdownnote.other.KeyboardHelper
import com.kazumaproject.markdownnote.other.collectLatestLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.*
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CreateEditFragment : Fragment(), EmojiPickerDialogFragment.EmojiItemClickListener{
    private val createEditViewModel: CreateEditViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()
    private var _binding : FragmentCreateEditBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var markwon: Markwon

    private var onBackPressedCallback: OnBackPressedCallback? =null

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

    @SuppressLint("ClickableViewAccessibility")
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
            onBackPressedCallback = getOnBackPressCallback(state.editTextHasFocus)
            onBackPressedCallback?.let { backPressed ->
                requireActivity().onBackPressedDispatcher.addCallback(backPressed)
            }
            markwon.setMarkdown(binding.markdownPreviewText,state.currentText)
        }
        collectLatestLifecycleFlow(activityViewModel.markdown_switch_state){ state ->
            binding.markdownRawEditText.isVisible = !state
            binding.markdownPreviewText.isVisible = state
            binding.createEditFragmentRootView.setOnTouchListener(null)
            if (!state){
                binding.createEditFragmentRootView.setOnTouchListener { _, _ ->
                    KeyboardHelper.hideKeyboardAndClearFocus(requireActivity())
                    return@setOnTouchListener true
                }
            }
        }
        collectLatestLifecycleFlow(activityViewModel.saveClicked){ clicked ->
            if (clicked && createEditViewModel.createEditState.value.currentText.isNotBlank()){
                createEditViewModel.insertNote(NoteEntity(
                    createEditViewModel.createEditState.value.currentText,
                    createEditViewModel.createEditState.value.emoji.unicode,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                ))
            }
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
                activityViewModel.updateHasFocusInEditText(hasFocus)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        activityViewModel.updateMarkdownSwitchState(false)
    }

    override fun onDestroyView() {

        if (!requireActivity().isChangingConfigurations &&
            !binding.markdownRawEditText.text.isNullOrBlank() &&
            !activityViewModel.saveClicked.value
        ){
            createEditViewModel.insertDraftNote(
                NoteDraftEntity(
                    binding.markdownRawEditText.text.toString(),
                    createEditViewModel.createEditState.value.emoji.unicode,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        super.onDestroyView()
        _binding = null
        onBackPressedCallback = null
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

    private fun getOnBackPressCallback(hasFocus: Boolean): OnBackPressedCallback {
        return object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                when{
                    hasFocus -> binding.markdownRawEditText.clearFocus()
                    activityViewModel.markdown_switch_state.value -> activityViewModel.updateMarkdownSwitchState(false)
                    else -> {
                        isEnabled = false
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

}