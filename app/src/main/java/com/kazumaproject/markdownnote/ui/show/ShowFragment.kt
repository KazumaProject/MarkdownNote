package com.kazumaproject.markdownnote.ui.show

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textview.MaterialTextView
import com.kazumaproject.emojipicker.EmojiPickerDialogFragment
import com.kazumaproject.emojipicker.model.Emoji
import com.kazumaproject.emojipicker.other.convertUnicode
import com.kazumaproject.markdownnote.MainViewModel
import com.kazumaproject.markdownnote.R
import com.kazumaproject.markdownnote.database.note.NoteEntity
import com.kazumaproject.markdownnote.databinding.FragmentDraftBinding
import com.kazumaproject.markdownnote.other.DrawerSelectedItemInShow
import com.kazumaproject.markdownnote.other.FragmentType
import com.kazumaproject.markdownnote.other.collectLatestLifecycleFlow
import com.kazumaproject.markdownnote.other.convertNoteBookMarkEntity
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ShowFragment : Fragment(), EmojiPickerDialogFragment.EmojiItemClickListener {

    private val showViewModel: ShowViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()
    private var _binding : FragmentDraftBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var markwon: Markwon

    private var onBackPressedCallback: OnBackPressedCallback? =null

    private var emojiText: MaterialTextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityViewModel.updateCurrentFragmentType(FragmentType.DraftFragment)
        activityViewModel.updateFloatingButtonEnableState(false)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDraftBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onBackPressedCallback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                requireActivity().findNavController(
                    R.id.navHostFragment
                ).popBackStack()
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            showViewModel.noteId?.let { id ->
                val note = showViewModel.getNote(id)
                note?.let {
                    showViewModel.updateCurrentText(note.body)
                    showViewModel.updateOriginalNoteText(note.body)
                    showViewModel.updateCurrentUnicode(note.emojiUnicode)
                    showViewModel.updateNoteId(note.id)
                    showViewModel.updateNoteCreatedAt(note.createdAt)
                    showViewModel.updateOriginUnicode(note.emojiUnicode)

                    showViewModel.drawerSelectedItem?.let { drawerItem ->
                        Timber.d("drawer item: $drawerItem")
                        activityViewModel.updateCurrentDrawerSelectedItemInShow(drawerItem)
                        createMenuItemsInBottomAppBarInMainActivity(note.emojiUnicode, drawerItem)
                    }

                }
            }
        }

        binding.showFragmentEditText.apply {
            addTextChangedListener { text: Editable? ->
                showViewModel.updateCurrentText(text.toString())
            }
        }

        onBackPressedCallback?.let { backPressed ->
            requireActivity().onBackPressedDispatcher.addCallback(backPressed)
        }
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(),R.color.markdown_bg_color)


        collectLatestLifecycleFlow(showViewModel.noteDataBaseData){ data ->

        }


        collectLatestLifecycleFlow(showViewModel.switchState){ switch_on ->
            binding.showFragmentEditText.isVisible = switch_on
            binding.showFragmentMarkwonText.isVisible = !switch_on
        }

        collectLatestLifecycleFlow(showViewModel.showNoteState){ showNoteState ->

            Timber.d("current unicode: ${showNoteState.currentUnicode.convertUnicode()}\noriginal unicode: ${showNoteState.originalUnicode.convertUnicode()}")

            markwon.setMarkdown(binding.showFragmentMarkwonText, showNoteState.currentText)


            requireActivity().findViewById<FloatingActionButton>(R.id.add_floating_button).apply {
                isVisible = showNoteState.currentText != showNoteState.originalText || showNoteState.currentUnicode != showNoteState.originalUnicode
            }

            activityViewModel.updateFloatingButtonEnableState(showNoteState.currentText != showNoteState.originalText || showNoteState.currentUnicode != showNoteState.originalUnicode)

            if (showNoteState.currentText != showNoteState.originalText || showNoteState.currentUnicode != showNoteState.originalUnicode){
                requireActivity().findViewById<BottomAppBar>(R.id.bottom_app_bar).apply {
                    fabAnchorMode = BottomAppBar.FAB_ANCHOR_MODE_CRADLE
                }
            } else {
                requireActivity().findViewById<BottomAppBar>(R.id.bottom_app_bar).apply {
                    fabAnchorMode = BottomAppBar.FAB_ANCHOR_MODE_EMBED
                }
            }
        }

        collectLatestLifecycleFlow(activityViewModel.save_clicked_in_show){ editSaveClick ->
            if (editSaveClick){
                val bookmarkedNote = showViewModel.getBookmarkNote(showViewModel.noteDataBaseData.value.noteId)
                val note = NoteEntity(
                    body = showViewModel.showNoteState.value.currentText,
                    emojiUnicode = showViewModel.showNoteState.value.currentUnicode,
                    createdAt = showViewModel.noteDataBaseData.value.createdAt,
                    updatedAt = System.currentTimeMillis(),
                    id = showViewModel.noteDataBaseData.value.noteId
                )
                delay(1)
                if (bookmarkedNote == null){
                    showViewModel.insertNote(note)
                } else {
                    showViewModel.insertNote(note)
                    showViewModel.insertBookmarkNote(
                        note.convertNoteBookMarkEntity()
                    )
                }
                Timber.d("save note: $note")
                delay(1)
                requireActivity().findNavController(R.id.navHostFragment).navigate(
                    ShowFragmentDirections.actionDraftFragmentToHomeFragment()
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        activityViewModel.updateSaveClickedInShow(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<BottomAppBar>(R.id.bottom_app_bar).apply {
            fabAnchorMode = BottomAppBar.FAB_ANCHOR_MODE_CRADLE
        }
        onBackPressedCallback = null
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(),R.color.window_bg_color)
        _binding = null
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun createMenuItemsInBottomAppBarInMainActivity(unicode: Int, drawerItem: String){
        val markdownSwitch = Switch(requireContext())
        markdownSwitch.isChecked = false
        markdownSwitch.setOnCheckedChangeListener { _, isChecked ->
            showViewModel.updateSwitchState(isChecked)
            binding.showFragmentEditText.setText(showViewModel.showNoteState.value.currentText)
        }
        emojiText = MaterialTextView(requireContext())
        emojiText?.apply {
            textSize = 24f
            text = unicode.convertUnicode()
            setOnClickListener {
                EmojiPickerDialogFragment(this@ShowFragment).show(requireActivity().supportFragmentManager,"emoji picker dialog from show fragment")
            }
        }

        requireActivity().findViewById<BottomAppBar>(R.id.bottom_app_bar).apply {
            menu.findItem(R.id.bottom_app_bar_item_emoji_unicode_text).actionView = emojiText
            menu.findItem(R.id.bottom_app_bar_item_preview_raw_change_in_show_fragment).actionView =
                markdownSwitch
        }
    }

    override fun onEmojiClicked(emoji: Emoji) {
        emojiText?.apply {
            this.text = emoji.unicode.convertUnicode()
        }
        showViewModel.updateCurrentUnicode(emoji.unicode)
        if (emoji.unicode != showViewModel.showNoteState.value.originalUnicode){
            requireActivity().findViewById<FloatingActionButton>(R.id.add_floating_button).isVisible = true
        }

    }

}