package com.kazumaproject.markdownnote.ui.show

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Switch
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import com.kazumaproject.emojipicker.EmojiPickerDialogFragment
import com.kazumaproject.emojipicker.model.Emoji
import com.kazumaproject.emojipicker.other.convertUnicode
import com.kazumaproject.markdownnote.MainViewModel
import com.kazumaproject.markdownnote.R
import com.kazumaproject.markdownnote.database.note.NoteEntity
import com.kazumaproject.markdownnote.databinding.FragmentDraftBinding
import com.kazumaproject.markdownnote.other.*
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ShowFragment : Fragment(), EmojiPickerDialogFragment.EmojiItemClickListener {

    private val showViewModel: ShowViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()
    private var _binding : FragmentDraftBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var markwon: Markwon

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var fileManageUtil: FileManageUtil

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
                if (activityViewModel.fragmentAndFloatingButtonState.value.hasFocus){
                    KeyboardHelper.hideKeyboardAndClearFocus(requireActivity())
                } else {
                    requireActivity().findNavController(
                        R.id.navHostFragment
                    ).popBackStack()
                }
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            showViewModel.drawerSelectedItem?.let { drawerItem ->
                Timber.d("drawer item: $drawerItem")
                activityViewModel.updateCurrentDrawerSelectedItemInShow(drawerItem)
                createMenuItemsInBottomAppBarInMainActivity(drawerItem)
            }
            showViewModel.noteType?.let { type ->
                when(type){
                    NoteType.NORMAL.name ->{
                        showViewModel.updateCurrentNoteType(type)
                        showViewModel.noteId?.let { id ->
                            val note = showViewModel.getNote(id)
                            note?.let {
                                showViewModel.updateCurrentText(note.body)
                                showViewModel.updateOriginalNoteText(note.body)
                                showViewModel.updateCurrentUnicode(note.emojiUnicode)
                                showViewModel.updateNoteId(note.id)
                                showViewModel.updateNoteCreatedAt(note.createdAt)
                                showViewModel.updateOriginUnicode(note.emojiUnicode)
                            }
                        }
                    }
                    NoteType.DRAFT.name ->{
                        showViewModel.updateCurrentNoteType(type)
                        showViewModel.noteId?.let { id ->
                            val note = showViewModel.getDraftNote(id)
                            note?.let {
                                showViewModel.updateCurrentText(note.body)
                                showViewModel.updateOriginalNoteText(note.body)
                                showViewModel.updateCurrentUnicode(note.emojiUnicode)
                                showViewModel.updateNoteId(note.id)
                                showViewModel.updateNoteCreatedAt(note.createdAt)
                                showViewModel.updateOriginUnicode(note.emojiUnicode)
                            }
                        }
                    }
                }
            }
        }

        binding.showFragmentEditText.apply {
            addTextChangedListener { text: Editable? ->
                showViewModel.updateCurrentText(text.toString())
            }
            setOnFocusChangeListener { _, hasFocus ->
                activityViewModel.updateHasFocusInEditText(hasFocus)
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
                isVisible = showNoteState.currentText != showNoteState.originalText || showNoteState.currentUnicode != showNoteState.originalUnicode || showNoteState.currentNoteType == NoteType.DRAFT.name
            }

            activityViewModel.updateFloatingButtonEnableState(showNoteState.currentText != showNoteState.originalText || showNoteState.currentUnicode != showNoteState.originalUnicode || showNoteState.currentNoteType == NoteType.DRAFT.name )

            if (showNoteState.currentText != showNoteState.originalText || showNoteState.currentUnicode != showNoteState.originalUnicode || showNoteState.currentNoteType == NoteType.DRAFT.name){
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
                showViewModel.noteType?.let { type ->
                    when(type){
                        NoteType.NORMAL.name ->{
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
                        NoteType.DRAFT.name ->{
                            val note = NoteEntity(
                                body = showViewModel.showNoteState.value.currentText,
                                emojiUnicode = showViewModel.showNoteState.value.currentUnicode,
                                createdAt = showViewModel.noteDataBaseData.value.createdAt,
                                updatedAt = System.currentTimeMillis(),
                                id = showViewModel.noteDataBaseData.value.noteId
                            )
                            delay(1)
                            showViewModel.insertNote(note)
                            showViewModel.deleteDraftNote(noteId = note.id)
                            Timber.d("save note: $note")
                            delay(1)
                            requireActivity().findNavController(R.id.navHostFragment).navigate(
                                ShowFragmentDirections.actionDraftFragmentToHomeFragment()
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        activityViewModel.updateSaveClickedInShow(false)
    }

    override fun onStop() {
        super.onStop()
        requireActivity().findViewById<BottomAppBar>(R.id.bottom_app_bar).apply {
            fabAnchorMode = BottomAppBar.FAB_ANCHOR_MODE_CRADLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(),R.color.window_bg_color)
        _binding = null
        onBackPressedCallback = null
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode", "SetTextI18n")
    private fun createMenuItemsInBottomAppBarInMainActivity(drawerItem: String){
        val markdownSwitch = Switch(requireContext())
        markdownSwitch.isChecked = false
        markdownSwitch.setOnCheckedChangeListener { _, isChecked ->
            showViewModel.updateSwitchState(isChecked)
            binding.showFragmentEditText.setText(showViewModel.showNoteState.value.currentText)
        }

        requireActivity().findViewById<BottomAppBar>(R.id.bottom_app_bar).apply {
            menu.findItem(R.id.bottom_app_bar_item_emoji_unicode_text).apply {
                setOnMenuItemClickListener {
                    EmojiPickerDialogFragment(this@ShowFragment).show(requireActivity().supportFragmentManager,"emoji picker dialog from show fragment")
                    return@setOnMenuItemClickListener true
                }
                when(drawerItem){
                    DrawerSelectedItemInShow.ALL_NOTE.name -> setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                    DrawerSelectedItemInShow.BOOKMARKED.name -> setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                    DrawerSelectedItemInShow.TRASH.name -> setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
                    DrawerSelectedItemInShow.DRAFTS.name -> setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                    DrawerSelectedItemInShow.EMOJI.name -> setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                }
            }
            menu.findItem(R.id.bottom_app_bar_item_preview_raw_change_in_show_fragment).actionView =
                markdownSwitch
            menu.findItem(R.id.bottom_app_bar_item_delete_note).setOnMenuItemClickListener {
                when(drawerItem){
                    DrawerSelectedItemInShow.ALL_NOTE.name ->{
                        CoroutineScope(Dispatchers.IO).launch {
                            val note = showViewModel.getNote(showViewModel.noteDataBaseData.value.noteId)
                            val bookMarkNote = showViewModel.getBookmarkNote(showViewModel.noteDataBaseData.value.noteId)
                            note?.let { trash ->
                                showViewModel.insertTrashNote(trash.convertNoteTrashEntity())
                                bookMarkNote?.let { bookmark ->
                                    showViewModel.deleteBookmarkedNote(bookmark.id)
                                }
                                withContext(Dispatchers.Main){
                                    requireActivity().findNavController(R.id.navHostFragment).navigate(
                                        ShowFragmentDirections.actionDraftFragmentToHomeFragment()
                                    )
                                }
                            }
                        }
                    }
                    DrawerSelectedItemInShow.BOOKMARKED.name ->{
                        CoroutineScope(Dispatchers.IO).launch {
                            val note = showViewModel.getNote(showViewModel.noteDataBaseData.value.noteId)
                            note?.let { bookmarked ->
                                showViewModel.deleteBookmarkedNote(showViewModel.noteDataBaseData.value.noteId)
                                showViewModel.insertTrashNote(bookmarked.convertNoteTrashEntity())
                                withContext(Dispatchers.Main){
                                    requireActivity().findNavController(R.id.navHostFragment).navigate(
                                        ShowFragmentDirections.actionDraftFragmentToHomeFragment()
                                    )
                                }
                            }
                        }
                    }
                    DrawerSelectedItemInShow.DRAFTS.name ->{
                        CoroutineScope(Dispatchers.IO).launch {
                            showViewModel.deleteDraftNote(showViewModel.noteDataBaseData.value.noteId)
                            withContext(Dispatchers.Main){
                                requireActivity().findNavController(R.id.navHostFragment).navigate(
                                    ShowFragmentDirections.actionDraftFragmentToHomeFragment()
                                )
                            }
                        }
                    }
                    DrawerSelectedItemInShow.TRASH.name ->{
                        CoroutineScope(Dispatchers.IO).launch {
                            showViewModel.deleteNote(showViewModel.noteDataBaseData.value.noteId)
                            showViewModel.deleteTrashNote(showViewModel.noteDataBaseData.value.noteId)
                            withContext(Dispatchers.Main){
                                requireActivity().findNavController(R.id.navHostFragment).navigate(
                                    ShowFragmentDirections.actionDraftFragmentToHomeFragment()
                                )
                            }
                        }
                    }
                    DrawerSelectedItemInShow.EMOJI.name ->{
                        CoroutineScope(Dispatchers.IO).launch {
                            val note = showViewModel.getNote(showViewModel.noteDataBaseData.value.noteId)
                            note?.let { trash ->
                                showViewModel.insertTrashNote(trash.convertNoteTrashEntity())
                                withContext(Dispatchers.Main){
                                    requireActivity().findNavController(R.id.navHostFragment).navigate(
                                        ShowFragmentDirections.actionDraftFragmentToHomeFragment()
                                    )
                                }
                            }
                        }
                    }
                }
                return@setOnMenuItemClickListener true
            }
            menu.findItem(R.id.bottom_app_bar_item_restore_note).setOnMenuItemClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    showViewModel.deleteTrashNote(showViewModel.noteDataBaseData.value.noteId)
                    withContext(Dispatchers.Main){
                        requireActivity().findNavController(R.id.navHostFragment).navigate(
                            ShowFragmentDirections.actionDraftFragmentToHomeFragment()
                        )
                    }

                }
                return@setOnMenuItemClickListener true
            }
            menu.findItem(R.id.bottom_app_bar_item_export_note).setOnMenuItemClickListener {

                val editText = EditText(context)
                editText.apply {
                    setSingleLine()
                    setText("markdown_note_${System.currentTimeMillis()}")
                    requestFocus()
                    selectAll()
                }
                val alertDialog = AlertDialog.Builder(context)
                alertDialog.setView(editText)
                alertDialog.apply {
                    setTitle(context.getString(R.string.save_note))
                    setCancelable(true)
                    setPositiveButton("txt") { dialog, _ ->
                        createLauncherTxt.launch(editText.text.toString())
                        dialog.dismiss()
                    }
                    setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    setNeutralButton("md"){ dialog, _ ->
                        createLauncherMD.launch(editText.text.toString())
                        dialog.dismiss()
                    }
                }
                alertDialog.show()
                return@setOnMenuItemClickListener true
            }
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

    private val createLauncherTxt = registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        uri ?: return@registerForActivityResult
        requireContext().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        val documentFile = DocumentFile.fromSingleUri(
            requireContext(),
            uri
        )
        documentFile?.let { file ->
            val out = requireContext().contentResolver.openOutputStream(file.uri)
            out?.apply {
                write(showViewModel.showNoteState.value.currentText.toByteArray())
                flush()
                close()
            }
        }
    }

    private val createLauncherMD = registerForActivityResult(ActivityResultContracts.CreateDocument("text/markdown")) { uri ->
        uri ?: return@registerForActivityResult
        requireContext().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        val documentFile = DocumentFile.fromSingleUri(
            requireContext(),
            uri
        )
        documentFile?.let { file ->
            val out = requireContext().contentResolver.openOutputStream(file.uri)
            out?.apply {
                write(showViewModel.showNoteState.value.currentText.toByteArray())
                flush()
                close()
            }
        }
    }

}