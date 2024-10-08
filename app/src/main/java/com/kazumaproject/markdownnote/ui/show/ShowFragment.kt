package com.kazumaproject.markdownnote.ui.show

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
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
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import com.kazumaproject.emojipicker.EmojiPickerDialogFragment
import com.kazumaproject.emojipicker.model.Emoji
import com.kazumaproject.emojipicker.other.convertUnicode
import com.kazumaproject.markdownnote.MainViewModel
import com.kazumaproject.markdownnote.R
import com.kazumaproject.markdownnote.database.note.NoteEntity
import com.kazumaproject.markdownnote.databinding.FragmentDraftBinding
import com.kazumaproject.markdownnote.other.DrawerSelectedItemInShow
import com.kazumaproject.markdownnote.other.FileManageUtil
import com.kazumaproject.markdownnote.other.FragmentType
import com.kazumaproject.markdownnote.other.KeyboardHelper
import com.kazumaproject.markdownnote.other.NoteType
import com.kazumaproject.markdownnote.other.collectLatestLifecycleFlow
import com.kazumaproject.markdownnote.other.convertNoteBookMarkEntity
import com.kazumaproject.markdownnote.other.convertNoteTrashEntity
import com.neo.highlight.util.listener.HighlightTextWatcher
import com.neo.highlight.util.scheme.ColorScheme
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class ShowFragment : Fragment(), EmojiPickerDialogFragment.EmojiItemClickListener {

    private val showViewModel: ShowViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentDraftBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var markwon: Markwon

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var fileManageUtil: FileManageUtil

    private var onBackPressedCallback: OnBackPressedCallback? = null

    private var emojiText: MaterialTextView? = null

    private var emojiDialog: EmojiPickerDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityViewModel.updateCurrentFragmentType(FragmentType.DraftFragment)
        activityViewModel.updateFloatingButtonEnableState(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDraftBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setHighLightHtmlInEditText() {
        val highLight = HighlightTextWatcher()

        when (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                highLight.addScheme(
                    ColorScheme(
                        Pattern.compile("<!--[\\s\\S]*?-->"),
                        resources.getColor(android.R.color.holo_green_dark, null)
                    ).setClearOldSpan(true), ColorScheme(
                        Pattern.compile(
                            "<[\\da-zA-Z]+([ \\t\\n\\f\\r]+[^ \\x00-\\x1F\\x7F\"'>/=]+([ \\t\\n\\f\\r]*=[ \\t\\n\\f\\r]*([^ \\t\\n\\f\\r\"'=><`]+|'[^\\x00-\\x08\\x0B\\x0E-\\x1F\\x7F']*'|\"[^\\x00-\\x08\\x0B\\x0E-\\x1F\\x7F\"]*\"))?)*[ \\t\\n\\f\\r]*/?>"
                        ), resources.getColor(android.R.color.holo_blue_dark, null)
                    ).setClearOldSpan(true), ColorScheme(
                        Pattern.compile(
                            "</[\\da-zA-Z]+[ \\t\\n\\f\\r]*>"
                        ), resources.getColor(android.R.color.holo_blue_dark, null)
                    ), ColorScheme(
                        Pattern.compile(
                            "<x([ \\t\\n\\f\\r]+[^ \\x00-\\x1F\\x7F\"'>/=]+([ \\t\\n\\f\\r]*=[ \\t\\n\\f\\r]*([^ \\t\\n\\f\\r\"'=><`]+|'[^\\x00-\\x08\\x0B\\x0E-\\x1F\\x7F']*'|\"[^\\x00-\\x08\\x0B\\x0E-\\x1F\\x7F\"]*\"))?)*[ \\t\\n\\f\\r]*/?>"
                        ), resources.getColor(android.R.color.holo_orange_dark, null)
                    ), ColorScheme(
                        Pattern.compile(
                            "</x[ \\t\\n\\f\\r]*>"
                        ), resources.getColor(android.R.color.holo_orange_dark, null)
                    )
                )
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                highLight.addScheme(
                    ColorScheme(
                        Pattern.compile("<!--[\\s\\S]*?-->"),
                        resources.getColor(android.R.color.holo_green_light, null)
                    ).setClearOldSpan(true), ColorScheme(
                        Pattern.compile(
                            "<[\\da-zA-Z]+([ \\t\\n\\f\\r]+[^ \\x00-\\x1F\\x7F\"'>/=]+([ \\t\\n\\f\\r]*=[ \\t\\n\\f\\r]*([^ \\t\\n\\f\\r\"'=><`]+|'[^\\x00-\\x08\\x0B\\x0E-\\x1F\\x7F']*'|\"[^\\x00-\\x08\\x0B\\x0E-\\x1F\\x7F\"]*\"))?)*[ \\t\\n\\f\\r]*/?>"
                        ), resources.getColor(android.R.color.holo_blue_light, null)
                    ).setClearOldSpan(true), ColorScheme(
                        Pattern.compile(
                            "</[\\da-zA-Z]+[ \\t\\n\\f\\r]*>"
                        ), resources.getColor(android.R.color.holo_blue_light, null)
                    ), ColorScheme(
                        Pattern.compile(
                            "<x([ \\t\\n\\f\\r]+[^ \\x00-\\x1F\\x7F\"'>/=]+([ \\t\\n\\f\\r]*=[ \\t\\n\\f\\r]*([^ \\t\\n\\f\\r\"'=><`]+|'[^\\x00-\\x08\\x0B\\x0E-\\x1F\\x7F']*'|\"[^\\x00-\\x08\\x0B\\x0E-\\x1F\\x7F\"]*\"))?)*[ \\t\\n\\f\\r]*/?>"
                        ), resources.getColor(android.R.color.holo_orange_light, null)
                    ), ColorScheme(
                        Pattern.compile(
                            "</x[ \\t\\n\\f\\r]*>"
                        ), resources.getColor(android.R.color.holo_orange_light, null)
                    )
                )
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                highLight.addScheme(
                    ColorScheme(
                        Pattern.compile("<!--[\\s\\S]*?-->"),
                        resources.getColor(android.R.color.holo_green_light, null)
                    ).setClearOldSpan(true), ColorScheme(
                        Pattern.compile(
                            "<[\\da-zA-Z]+([ \\t\\n\\f\\r]+[^ \\x00-\\x1F\\x7F\"'>/=]+([ \\t\\n\\f\\r]*=[ \\t\\n\\f\\r]*([^ \\t\\n\\f\\r\"'=><`]+|'[^\\x00-\\x08\\x0B\\x0E-\\x1F\\x7F']*'|\"[^\\x00-\\x08\\x0B\\x0E-\\x1F\\x7F\"]*\"))?)*[ \\t\\n\\f\\r]*/?>"
                        ), resources.getColor(android.R.color.holo_blue_light, null)
                    ).setClearOldSpan(true), ColorScheme(
                        Pattern.compile(
                            "</[\\da-zA-Z]+[ \\t\\n\\f\\r]*>"
                        ), resources.getColor(android.R.color.holo_blue_light, null)
                    ), ColorScheme(
                        Pattern.compile(
                            "<x([ \\t\\n\\f\\r]+[^ \\x00-\\x1F\\x7F\"'>/=]+([ \\t\\n\\f\\r]*=[ \\t\\n\\f\\r]*([^ \\t\\n\\f\\r\"'=><`]+|'[^\\x00-\\x08\\x0B\\x0E-\\x1F\\x7F']*'|\"[^\\x00-\\x08\\x0B\\x0E-\\x1F\\x7F\"]*\"))?)*[ \\t\\n\\f\\r]*/?>"
                        ), resources.getColor(android.R.color.holo_orange_light, null)
                    ), ColorScheme(
                        Pattern.compile(
                            "</x[ \\t\\n\\f\\r]*>"
                        ), resources.getColor(android.R.color.holo_orange_light, null)
                    )
                )
            }
        }
        binding.editText.addTextChangedListener(highLight)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (activityViewModel.fragmentAndFloatingButtonState.value.hasFocus) {
                    KeyboardHelper.hideKeyboardAndClearFocus(requireActivity())
                    requireActivity().findViewById<BottomAppBar>(R.id.bottom_app_bar).performShow()
                    requireActivity().findViewById<FloatingActionButton>(R.id.add_floating_button)
                        .show()
                } else {
                    requireActivity().findViewById<FloatingActionButton>(R.id.add_floating_button)
                        .hide()
                    requireActivity().findNavController(
                        R.id.navHostFragment
                    ).popBackStack()
                }
            }
        }
        emojiDialog = EmojiPickerDialogFragment(this)
        binding.lineLayout.attachEditText(binding.editText)
        when (showViewModel.getSyntaxType()) {
            "1" -> {

            }

            "2" -> {
                setHighLightHtmlInEditText()
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            showViewModel.drawerSelectedItem?.let { drawerItem ->
                Timber.d("drawer item: $drawerItem")
                activityViewModel.updateCurrentDrawerSelectedItemInShow(drawerItem)
                createMenuItemsInBottomAppBarInMainActivity(drawerItem)
            }
            showViewModel.noteType?.let { type ->
                when (type) {
                    NoteType.NORMAL.name -> {
                        showViewModel.updateCurrentNoteType(type)
                        showViewModel.noteId?.let { id ->
                            val note = showViewModel.getNote(id)
                            note?.let {
                                Timber.d("note in show fragment: ${it.id}")
                                showViewModel.updateCurrentText(it.body)
                                showViewModel.updateOriginalNoteText(it.body)
                                showViewModel.updateCurrentUnicode(it.emojiUnicode)
                                showViewModel.updateNoteId(id)
                                showViewModel.updateNoteCreatedAt(it.createdAt)
                                showViewModel.updateOriginUnicode(it.emojiUnicode)
                            }
                        }
                    }

                    NoteType.DRAFT.name -> {
                        showViewModel.updateCurrentNoteType(type)
                        showViewModel.noteId?.let { id ->
                            val note = showViewModel.getDraftNote(id)
                            note?.let {
                                showViewModel.updateCurrentText(it.body)
                                showViewModel.updateOriginalNoteText(it.body)
                                showViewModel.updateCurrentUnicode(it.emojiUnicode)
                                showViewModel.updateNoteId(id)
                                showViewModel.updateNoteCreatedAt(it.createdAt)
                                showViewModel.updateOriginUnicode(it.emojiUnicode)
                            }
                        }
                    }

                    NoteType.READ_FILE.name -> {
                        showViewModel.updateCurrentNoteType(type)
                        showViewModel.noteId?.let { id ->
                            val unicode =
                                com.kazumaproject.emojipicker.other.Constants.EMOJI_LIST_ANIMALS_NATURE[(0 until com.kazumaproject.emojipicker.other.Constants.EMOJI_LIST_ANIMALS_NATURE.size).random()].unicode
                            showViewModel.updateCurrentText(id)
                            showViewModel.updateOriginalNoteText("")
                            showViewModel.updateCurrentUnicode(unicode)
                            showViewModel.updateNoteId(UUID.randomUUID().toString())
                            showViewModel.updateNoteCreatedAt(System.currentTimeMillis())
                            showViewModel.updateOriginUnicode(unicode)
                        }
                    }
                }
            }
        }

        binding.editText.apply {
            val initialText = showViewModel.showNoteState.value.originalText
            Timber.d("edit text: $initialText ${this.text.toString()} ${showViewModel.showNoteState.value.currentText}")
            addTextChangedListener { text: Editable? ->
                showViewModel.updateCurrentText(text.toString())
            }
        }

        onBackPressedCallback?.let { backPressed ->
            requireActivity().onBackPressedDispatcher.addCallback(backPressed)
        }
        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.markdown_bg_color)


        collectLatestLifecycleFlow(showViewModel.switchState) { switch_on ->
            binding.lineLayout.isVisible = switch_on
            binding.showFragmentMarkwonTextParent.isVisible = !switch_on
        }

        collectLatestLifecycleFlow(showViewModel.showNoteState) { showNoteState ->

            Timber.d("showNoteState: current: ${showNoteState.currentText}\noriginal: ${showNoteState.originalText}\n${showNoteState.currentText != showNoteState.originalText}\n$")
            if (showNoteState.currentText != showNoteState.originalText) {
                CoroutineScope(Dispatchers.Main).launch {
                    requireActivity().findViewById<FloatingActionButton>(R.id.add_floating_button)
                        .show()
                }
            }else {
                requireActivity().findViewById<FloatingActionButton>(R.id.add_floating_button)
                    .hide()
            }

            Timber.d("current unicode: ${showNoteState.currentUnicode.convertUnicode()}\noriginal unicode: ${showNoteState.originalUnicode.convertUnicode()}")

            markwon.setMarkdown(binding.showFragmentMarkwonText, showNoteState.currentText)


            activityViewModel.updateFloatingButtonEnableState(showNoteState.currentText != showNoteState.originalText || showNoteState.currentUnicode != showNoteState.originalUnicode || showNoteState.currentNoteType == NoteType.DRAFT.name || showNoteState.currentNoteType == NoteType.READ_FILE.name)

            if (showNoteState.currentText != showNoteState.originalText || showNoteState.currentUnicode != showNoteState.originalUnicode || showNoteState.currentNoteType == NoteType.DRAFT.name) {
                requireActivity().findViewById<BottomAppBar>(R.id.bottom_app_bar).apply {
                    fabAnchorMode = BottomAppBar.FAB_ANCHOR_MODE_CRADLE
                }
            } else {
                requireActivity().findViewById<BottomAppBar>(R.id.bottom_app_bar).apply {
                    fabAnchorMode = BottomAppBar.FAB_ANCHOR_MODE_EMBED
                }
            }
        }

        collectLatestLifecycleFlow(activityViewModel.save_clicked_in_show) { editSaveClick ->
            if (editSaveClick) {
                showViewModel.noteType?.let { type ->
                    Timber.d("note type: $type")
                    when (type) {
                        NoteType.NORMAL.name -> {
                            val bookmarkedNote =
                                showViewModel.getBookmarkNote(showViewModel.noteDataBaseData.value.noteId)
                            val note = NoteEntity(
                                body = showViewModel.showNoteState.value.currentText,
                                emojiUnicode = showViewModel.showNoteState.value.currentUnicode,
                                createdAt = showViewModel.noteDataBaseData.value.createdAt,
                                updatedAt = System.currentTimeMillis(),
                                id = showViewModel.noteDataBaseData.value.noteId
                            )
                            Timber.d("note in show fragment: ${note.id} ${note.body}")
                            if (bookmarkedNote == null) {
                                showViewModel.insertNote(note)
                            } else {
                                showViewModel.insertNote(note)
                                showViewModel.insertBookmarkNote(
                                    note.convertNoteBookMarkEntity()
                                )
                            }
                            Timber.d("save note: $note")
                            requireActivity().findNavController(R.id.navHostFragment).navigate(
                                ShowFragmentDirections.actionDraftFragmentToHomeFragment()
                            )
                        }

                        NoteType.DRAFT.name -> {
                            val note = NoteEntity(
                                body = showViewModel.showNoteState.value.currentText,
                                emojiUnicode = showViewModel.showNoteState.value.currentUnicode,
                                createdAt = showViewModel.noteDataBaseData.value.createdAt,
                                updatedAt = System.currentTimeMillis(),
                                id = showViewModel.noteDataBaseData.value.noteId
                            )
                            showViewModel.insertNote(note)
                            //showViewModel.deleteDraftNote(noteId = note.id)
                            Timber.d("save note: $note")
                            requireActivity().findNavController(R.id.navHostFragment).navigate(
                                ShowFragmentDirections.actionDraftFragmentToHomeFragment()
                            )
                        }

                        NoteType.READ_FILE.name -> {
                            val note = NoteEntity(
                                body = showViewModel.showNoteState.value.currentText,
                                emojiUnicode = showViewModel.showNoteState.value.currentUnicode,
                                createdAt = showViewModel.noteDataBaseData.value.createdAt,
                                updatedAt = System.currentTimeMillis(),
                                id = showViewModel.noteDataBaseData.value.noteId
                            )
                            showViewModel.insertNote(note)
                            requireActivity().findNavController(R.id.navHostFragment).navigate(
                                ShowFragmentDirections.actionDraftFragmentToHomeFragment()
                            )
                        }
                    }
                }
            }
        }

        collectLatestLifecycleFlow(showViewModel.noteDataBaseData){

        }
    }

    override fun onPause() {
        super.onPause()
        if (emojiDialog?.fragmentManager != null) {
            try {
                emojiDialog?.dismiss()
            } catch (e: Exception) {

            }
        }
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
        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.window_bg_color)
        _binding = null
        onBackPressedCallback = null
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode", "SetTextI18n")
    private fun createMenuItemsInBottomAppBarInMainActivity(drawerItem: String) {
        val markdownSwitch = Switch(requireContext())
        markdownSwitch.isChecked = false
        markdownSwitch.setOnCheckedChangeListener { _, isChecked ->
            showViewModel.updateSwitchState(isChecked)
            binding.editText.setText(showViewModel.showNoteState.value.currentText)
        }

        emojiText = MaterialTextView(requireContext())
        emojiText?.apply {
            text = showViewModel.showNoteState.value.currentUnicode.convertUnicode()
            setOnClickListener {
                emojiDialog?.show(
                    requireActivity().supportFragmentManager,
                    "emoji picker dialog from show fragment"
                )
            }
        }

        requireActivity().findViewById<BottomAppBar>(R.id.bottom_app_bar).apply {
            menu.findItem(R.id.bottom_app_bar_item_emoji_unicode_text).apply {
                actionView = emojiText
                setOnMenuItemClickListener {
                    emojiDialog?.show(
                        requireActivity().supportFragmentManager,
                        "emoji picker dialog from show fragment"
                    )
                    return@setOnMenuItemClickListener true
                }
                when (drawerItem) {
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
                when (drawerItem) {
                    DrawerSelectedItemInShow.ALL_NOTE.name -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            val note =
                                showViewModel.getNote(showViewModel.noteDataBaseData.value.noteId)
                            val bookMarkNote =
                                showViewModel.getBookmarkNote(showViewModel.noteDataBaseData.value.noteId)
                            note?.let { trash ->
                                showViewModel.insertTrashNote(trash.convertNoteTrashEntity())
                                bookMarkNote?.let { bookmark ->
                                    showViewModel.deleteBookmarkedNote(bookmark.id)
                                }
                                withContext(Dispatchers.Main) {
                                    requireActivity().findNavController(R.id.navHostFragment)
                                        .navigate(
                                            ShowFragmentDirections.actionDraftFragmentToHomeFragment()
                                        )
                                }
                            }
                        }
                    }

                    DrawerSelectedItemInShow.BOOKMARKED.name -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            val note =
                                showViewModel.getNote(showViewModel.noteDataBaseData.value.noteId)
                            note?.let { bookmarked ->
                                showViewModel.deleteBookmarkedNote(showViewModel.noteDataBaseData.value.noteId)
                                showViewModel.insertTrashNote(bookmarked.convertNoteTrashEntity())
                                withContext(Dispatchers.Main) {
                                    requireActivity().findNavController(R.id.navHostFragment)
                                        .navigate(
                                            ShowFragmentDirections.actionDraftFragmentToHomeFragment()
                                        )
                                }
                            }
                        }
                    }

                    DrawerSelectedItemInShow.DRAFTS.name -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            showViewModel.deleteDraftNote(showViewModel.noteDataBaseData.value.noteId)
                            withContext(Dispatchers.Main) {
                                requireActivity().findNavController(R.id.navHostFragment).navigate(
                                    ShowFragmentDirections.actionDraftFragmentToHomeFragment()
                                )
                            }
                        }
                    }

                    DrawerSelectedItemInShow.TRASH.name -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            showViewModel.deleteNote(showViewModel.noteDataBaseData.value.noteId)
                            showViewModel.deleteTrashNote(showViewModel.noteDataBaseData.value.noteId)
                            withContext(Dispatchers.Main) {
                                requireActivity().findNavController(R.id.navHostFragment).navigate(
                                    ShowFragmentDirections.actionDraftFragmentToHomeFragment()
                                )
                            }
                        }
                    }

                    DrawerSelectedItemInShow.EMOJI.name -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            val note =
                                showViewModel.getNote(showViewModel.noteDataBaseData.value.noteId)
                            note?.let { trash ->
                                showViewModel.insertTrashNote(trash.convertNoteTrashEntity())
                                withContext(Dispatchers.Main) {
                                    requireActivity().findNavController(R.id.navHostFragment)
                                        .navigate(
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
                    withContext(Dispatchers.Main) {
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

                val listView = ListView(requireContext())
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    arrayListOf("txt", "md", "json", "html")
                )
                listView.adapter = arrayAdapter
                listView.onItemClickListener = OnItemClickListener { parent, view, position, id ->
                    when (position) {
                        0 -> {
                            createLauncherTxt.launch("markdown_note_${System.currentTimeMillis()}")
                        }

                        1 -> {
                            createLauncherMD.launch("markdown_note_${System.currentTimeMillis()}")
                        }

                        2 -> {
                            createLauncherJson.launch("markdown_note_${System.currentTimeMillis()}")
                        }

                        3 -> {
                            createLauncherHTML.launch("markdown_note_${System.currentTimeMillis()}")
                        }

                        4 -> {
                            createLauncherHTML.launch("markdown_note_${System.currentTimeMillis()}")
                        }
                    }
                }

                val alertDialog = AlertDialog.Builder(context)
                alertDialog.setView(listView)
                alertDialog.apply {
                    setTitle(context.getString(R.string.save_note))
                    setCancelable(true)
                    setNegativeButton("Cancel") { dialog, _ ->
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
        if (emoji.unicode != showViewModel.showNoteState.value.originalUnicode) {
            requireActivity().findViewById<FloatingActionButton>(R.id.add_floating_button).isVisible =
                true
        }
    }

    private val createLauncherTxt =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
            uri ?: return@registerForActivityResult
            requireContext().contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            val documentFile = DocumentFile.fromSingleUri(
                requireContext(), uri
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

    private val createLauncherMD =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/markdown")) { uri ->
            uri ?: return@registerForActivityResult
            requireContext().contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            val documentFile = DocumentFile.fromSingleUri(
                requireContext(), uri
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

    private val createLauncherCSS =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/css")) { uri ->
            uri ?: return@registerForActivityResult
            requireContext().contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            val documentFile = DocumentFile.fromSingleUri(
                requireContext(), uri
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

    private val createLauncherCSV =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
            uri ?: return@registerForActivityResult
            requireContext().contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            val documentFile = DocumentFile.fromSingleUri(
                requireContext(), uri
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

    private val createLauncherHTML =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/html")) { uri ->
            uri ?: return@registerForActivityResult
            requireContext().contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            val documentFile = DocumentFile.fromSingleUri(
                requireContext(), uri
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

    private val createLauncherJson =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            uri ?: return@registerForActivityResult
            requireContext().contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            val documentFile = DocumentFile.fromSingleUri(
                requireContext(), uri
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

    private val createLauncherJavascript =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/javascript")) { uri ->
            uri ?: return@registerForActivityResult
            requireContext().contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            val documentFile = DocumentFile.fromSingleUri(
                requireContext(), uri
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

    private val createLauncherKotlin =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
            uri ?: return@registerForActivityResult
            requireContext().contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            val documentFile = DocumentFile.fromSingleUri(
                requireContext(), uri
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