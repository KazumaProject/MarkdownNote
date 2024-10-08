package com.kazumaproject.markdownnote

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Switch
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.kazumaproject.markdownnote.adapters.DrawerParentRecyclerViewAdapter
import com.kazumaproject.markdownnote.database.note.NoteEntity
import com.kazumaproject.markdownnote.databinding.ActivityMainBinding
import com.kazumaproject.markdownnote.drawer.model.DrawerItem
import com.kazumaproject.markdownnote.drawer.model.DrawerItemType
import com.kazumaproject.markdownnote.drawer.model.DrawerParentItem
import com.kazumaproject.markdownnote.drawer.model.DrawerSelectedItem
import com.kazumaproject.markdownnote.other.*
import com.kazumaproject.markdownnote.ui.create_edit.CreateEditFragmentDirections
import com.kazumaproject.markdownnote.ui.home.HomeFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.UUID

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val viewModel : MainViewModel by viewModels()
    private var drawerParentRecyclerViewAdapter: DrawerParentRecyclerViewAdapter? = null

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        drawerParentRecyclerViewAdapter = DrawerParentRecyclerViewAdapter()

        setupActionBarWithNavController(findNavController(R.id.navHostFragment))

        collectLatestLifecycleFlow(viewModel.fragmentAndFloatingButtonState){ fragmentAndFloatingButtonState ->
            setAppBottomBarAppearanceByFragmentType(
                fragmentAndFloatingButtonState.currentFragmentType,
                fragmentAndFloatingButtonState.floatingButtonState,
                fragmentAndFloatingButtonState.hasFocus,
                fragmentAndFloatingButtonState.drawerItemInShow
            )
            setFloatingButton(fragmentAndFloatingButtonState.currentFragmentType, fragmentAndFloatingButtonState.drawerItemInShow)
            binding.addFloatingButton.alpha = if (fragmentAndFloatingButtonState.floatingButtonState) 1.0f else 0.5f
            setBottomAppBar(
                fragmentAndFloatingButtonState.currentFragmentType,
                fragmentAndFloatingButtonState.hasFocus
            )
        }

        collectLatestLifecycleFlow(viewModel.markdown_switch_state){ state ->
            val markdownSwitch = Switch(this@MainActivity)
            markdownSwitch.isChecked = state
            if (state) KeyboardHelper.hideKeyboardAndClearFocus(this)
            markdownSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateMarkdownSwitchState(isChecked)
            }
            val bottomAppBarItemPreviewRawChange = binding.bottomAppBar.menu.findItem(R.id.bottom_app_bar_item_preview_raw_change)
            bottomAppBarItemPreviewRawChange.actionView = markdownSwitch
        }

        collectLatestLifecycleFlow(viewModel.dataBaseValues){ value ->
            drawerParentRecyclerViewAdapter?.let { drawerAdapter ->
                val drawer_parent_items = mutableListOf<DrawerParentItem>()
                val mainDrawerItems = DrawerParentItem(
                    parentTitle = getString(R.string.app_name),
                    childList = getMainDrawerItems(value)
                )
                drawer_parent_items.add(mainDrawerItems)
                val emojiDrawerItems = DrawerParentItem(
                    parentTitle = getString(R.string.emoji_string),
                    childList = getEmojiDrawerItems(value.allNotes)
                )
                drawer_parent_items.add(emojiDrawerItems)
                val navigationDrawerItems = DrawerParentItem(
                    parentTitle = getString(R.string.general_string),
                    childList = getNavigationDrawerItem()
                )
                drawer_parent_items.add(navigationDrawerItems)
                drawerAdapter.parent_drawer_item_list = drawer_parent_items.toList()
                binding.drawerRecyclerView.apply {
                    this.adapter = drawerAdapter
                    this.layoutManager = LinearLayoutManager(this@MainActivity)
                }
                drawerAdapter.setOnItemClickListener { drawerItem, i ->
                    Timber.d("clicked drawer item: $drawerItem\nindex: $i")
                    binding.bottomAppBar.performShow()
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    when(drawerItem.type){
                        is DrawerItemType.FilterNotes -> {
                            when(i){
                                0 -> viewModel.updateCurrentSelectedDrawerItem(DrawerSelectedItem.AllNotes)
                                1 -> viewModel.updateCurrentSelectedDrawerItem(DrawerSelectedItem.BookmarkedNotes)
                                2 -> viewModel.updateCurrentSelectedDrawerItem(DrawerSelectedItem.DraftNotes)
                                3 -> viewModel.updateCurrentSelectedDrawerItem(DrawerSelectedItem.TrashNotes)
                            }
                        }
                        is DrawerItemType.CategoryEmoji -> {
                            drawerItem.emojiUnicode?.let { unicode ->
                                viewModel.updateCurrentSelectedDrawerItem(DrawerSelectedItem.EmojiCategory(
                                    unicode = unicode,
                                    index = i
                                ))
                            }
                        }
                        is DrawerItemType.Navigation -> {
                            when(i){
                                0 ->{
                                    viewModel.updateCurrentSelectedDrawerItem(
                                        DrawerSelectedItem.ReadFile
                                    )
                                    selectFileByUri()
                                }
                                1 ->{
                                    viewModel.updateCurrentSelectedDrawerItem(
                                        DrawerSelectedItem.ReadApplicationFile
                                    )
                                    selectJsonFileByUri()
                                }
                                2 ->{
                                    viewModel.updateCurrentSelectedDrawerItem(
                                        DrawerSelectedItem.GoToSettings
                                    )
                                    findNavController(R.id.navHostFragment).navigate(
                                        HomeFragmentDirections.actionHomeFragmentToSettingFragment()
                                    )
                                }
                            }

                        }
                    }
                }
            }
            getEmojiDrawerItems(value.allNotes)

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        changeIconColorByTheme()
    }

    override fun onDestroy() {
        super.onDestroy()
        drawerParentRecyclerViewAdapter = null
    }

    private fun selectFileByUri(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"
        }
        resultLauncher.launch(intent)
    }

    private fun selectJsonFileByUri(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        resultLauncher.launch(intent)
    }


    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.also { uri ->
                val returnCursor: Cursor? = contentResolver.query(uri, null, null, null, null)
                val nameIndex: Int? = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                returnCursor?.moveToFirst()
                nameIndex?.let { index ->
                    val content: String = readTextFile(uri)
                    try {
                        content.let { contents ->
                            Timber.d("selected contents: $contents")
                            findNavController(R.id.navHostFragment).navigate(
                                HomeFragmentDirections.actionHomeFragmentToDraftFragment(
                                    noteId = contents,
                                    drawerSelectedItem = DrawerSelectedItemInShow.READ_FILE.name,
                                    noteType = NoteType.READ_FILE.name
                                )
                            )
                        }
                    }catch (e: Exception){
                        Snackbar.make(
                            binding.root,
                            "Saved text is not valid.",
                            Snackbar.LENGTH_LONG
                        ).show()
                        e.printStackTrace()
                    }
                }

            }
        }
    }

    private fun readTextFile(uri: Uri): String {
        var reader: BufferedReader? = null
        val builder = StringBuilder()
        try {
            reader = BufferedReader(InputStreamReader(contentResolver?.openInputStream(uri)))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                builder.append(line + "\n")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return builder.toString()
    }

    private fun setAppBottomBarAppearanceByFragmentType(
        type: FragmentType,
        isEnable: Boolean,
        hasFocus: Boolean,
        drawerSelectedItemInShow: String
    ){
        when(type){
            is FragmentType.HomeFragment -> {
                setBottomAppBarMenuItemsVisibility(
                    visibility = true,
                    switchVisibility = false,
                    hasFocus,
                    unicodeVisibility = false,
                    switchVisibilityInShow = false,
                    drawerSelectedItemInShow = drawerSelectedItemInShow,
                    deleteNoteVisibility = false,
                    restoreNoteVisibility = false,
                    type
                )
                binding.addFloatingButton.apply {
                    setImageResource(R.drawable.baseline_add_24)
                    isEnabled = isEnable
                }
            }
            is FragmentType.CreateEditFragment -> {
                setBottomAppBarMenuItemsVisibility(
                    visibility = false,
                    switchVisibility = true,
                    hasFocus,
                    unicodeVisibility = false,
                    switchVisibilityInShow = false,
                    drawerSelectedItemInShow = drawerSelectedItemInShow,
                    deleteNoteVisibility = false,
                    restoreNoteVisibility = false,
                    type
                )
                binding.addFloatingButton.apply {
                    setImageResource(R.drawable.diskette)
                    isEnabled = isEnable
                }
            }
            is FragmentType.DraftFragment -> {
                setBottomAppBarMenuItemsVisibility(
                    visibility = false,
                    switchVisibility = false,
                    hasFocus,
                    unicodeVisibility = true,
                    switchVisibilityInShow = true,
                    drawerSelectedItemInShow = drawerSelectedItemInShow,
                    deleteNoteVisibility = true,
                    restoreNoteVisibility = false,
                    type
                )
                binding.addFloatingButton.apply {
                    setImageResource(R.drawable.diskette)
                    isEnabled = isEnable
                }
            }
            is FragmentType.SettingFragment -> {
                setBottomAppBarMenuItemsVisibility(
                    visibility = false,
                    switchVisibility = false,
                    hasFocus,
                    unicodeVisibility = false,
                    switchVisibilityInShow = false,
                    drawerSelectedItemInShow = drawerSelectedItemInShow,
                    deleteNoteVisibility = false,
                    restoreNoteVisibility = false,
                    type
                )
                binding.addFloatingButton.apply {
                    setImageResource(R.drawable.settings)
                    isEnabled = isEnable
                }
            }
        }
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun setBottomAppBar(fragmentType: FragmentType, hasFocus: Boolean) = binding.bottomAppBar.apply {

        setOnMenuItemClickListener { item ->
            when(item.itemId){
                R.id.bottom_bar_item_draft -> {
                    binding.drawerLayout.openDrawer(GravityCompat.START)
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun setFloatingButton(
        fragmentType: FragmentType,
        selectedItemInShow: String
    ) = binding.addFloatingButton.apply {
        when(fragmentType){
            is FragmentType.HomeFragment ->{
                isVisible = true
                setOnClickListener {
                    binding.bottomAppBar.performShow()
                    findNavController(R.id.navHostFragment).navigate(
                        HomeFragmentDirections.actionHomeFragmentToCreateEditFragment()
                    )
                }
            }
            is FragmentType.CreateEditFragment ->{
                isVisible = true
                setOnClickListener {
                    binding.bottomAppBar.performShow()
                    viewModel.updateSaveClicked(true)
                    findNavController(R.id.navHostFragment).navigate(
                        CreateEditFragmentDirections.actionCreateEditFragmentToHomeFragment()
                    )
                }
            }
            is FragmentType.DraftFragment ->{
                when(selectedItemInShow){
                    DrawerSelectedItemInShow.ALL_NOTE.name ->{
                        isVisible = false
                        setOnClickListener {
                            binding.bottomAppBar.performShow()
                            viewModel.updateSaveClickedInShow(true)
                        }
                    }
                    DrawerSelectedItemInShow.BOOKMARKED.name ->{
                        isVisible = false
                        setOnClickListener {
                            binding.bottomAppBar.performShow()
                            viewModel.updateSaveClickedInShow(true)
                        }
                    }
                    DrawerSelectedItemInShow.DRAFTS.name ->{
                        isVisible = true
                        setOnClickListener {
                            binding.bottomAppBar.performShow()
                            viewModel.updateSaveClickedInShow(true)
                        }
                    }
                    DrawerSelectedItemInShow.TRASH.name ->{
                        isVisible = false
                        setOnClickListener {

                        }
                    }
                    DrawerSelectedItemInShow.EMOJI.name ->{
                        isVisible = false
                        setOnClickListener {
                            binding.bottomAppBar.performShow()
                            viewModel.updateSaveClickedInShow(true)
                        }
                    }
                    DrawerSelectedItemInShow.READ_FILE.name ->{
                        isVisible = true
                        setOnClickListener {
                            binding.bottomAppBar.performShow()
                            viewModel.updateSaveClickedInShow(true)
                        }
                    }
                }
            }
            is FragmentType.SettingFragment ->{
                isVisible = false
            }
        }
    }

    private fun setBottomAppBarMenuItemsVisibility(
        visibility: Boolean,
        switchVisibility: Boolean,
        hasFocus: Boolean,
        unicodeVisibility: Boolean,
        switchVisibilityInShow: Boolean,
        drawerSelectedItemInShow: String,
        deleteNoteVisibility: Boolean,
        restoreNoteVisibility: Boolean,
        fragmentType: FragmentType
    ){
        binding.bottomAppBar.menu.apply {
            findItem(R.id.bottom_bar_item_draft).isVisible = visibility
            findItem(R.id.bottom_app_bar_item_preview_raw_change).isVisible = switchVisibility && !hasFocus
            when(fragmentType){
                is FragmentType.DraftFragment ->{
                    when(drawerSelectedItemInShow){
                        DrawerSelectedItemInShow.TRASH.name -> {
                            findItem(R.id.bottom_app_bar_item_emoji_unicode_text).isVisible = false
                            findItem(R.id.bottom_app_bar_item_export_note).isVisible = false
                            findItem(R.id.bottom_app_bar_item_restore_note).isVisible = true
                            findItem(R.id.bottom_app_bar_item_delete_note).isVisible = true
                        }
                        DrawerSelectedItemInShow.DRAFTS.name ->{
                            findItem(R.id.bottom_app_bar_item_emoji_unicode_text).isVisible = true
                            findItem(R.id.bottom_app_bar_item_export_note).isVisible = false
                            findItem(R.id.bottom_app_bar_item_restore_note).isVisible = false
                            findItem(R.id.bottom_app_bar_item_delete_note).isVisible = true
                        }
                        else -> {
                            findItem(R.id.bottom_app_bar_item_emoji_unicode_text).isVisible = unicodeVisibility
                            findItem(R.id.bottom_app_bar_item_export_note).isVisible = unicodeVisibility
                            findItem(R.id.bottom_app_bar_item_restore_note).isVisible = restoreNoteVisibility
                            findItem(R.id.bottom_app_bar_item_delete_note).isVisible = deleteNoteVisibility
                        }
                    }
                }
                else -> {
                    findItem(R.id.bottom_app_bar_item_emoji_unicode_text).isVisible = unicodeVisibility
                    findItem(R.id.bottom_app_bar_item_export_note).isVisible = unicodeVisibility
                    findItem(R.id.bottom_app_bar_item_restore_note).isVisible = restoreNoteVisibility
                    findItem(R.id.bottom_app_bar_item_delete_note).isVisible = deleteNoteVisibility
                }
            }
            findItem(R.id.bottom_app_bar_item_preview_raw_change_in_show_fragment).isVisible = switchVisibilityInShow && drawerSelectedItemInShow != DrawerSelectedItemInShow.TRASH.name
        }
        binding.addFloatingButton.isEnabled = visibility
    }

    private fun getMainDrawerItems(databaseValues: DatabaseValues): List<DrawerItem> = listOf(
        DrawerItem(
            title = getString(R.string.all_notes),
            count = databaseValues.allNotes.filter { note ->
                !viewModel.dataBaseValues.value.allTrashNotes.contains(note.convertNoteTrashEntity())
            }.size,
            type = DrawerItemType.FilterNotes,
            resID = R.drawable.inbox,
            emojiUnicode = null
        ),
        DrawerItem(
            title = getString(R.string.bookmarked_notes),
            count = databaseValues.allBookmarkNotes.size,
            type = DrawerItemType.FilterNotes,
            resID = R.drawable.love,
            emojiUnicode = null
        ),
        DrawerItem(
            title = getString(R.string.draft_notes),
            count = databaseValues.allDraftNotes.size,
            type = DrawerItemType.FilterNotes,
            resID = R.drawable.draft,
            emojiUnicode = null
        ),
        DrawerItem(
            title = getString(R.string.trash_notes),
            count = databaseValues.allTrashNotes.size,
            type = DrawerItemType.FilterNotes,
            resID = R.drawable.trash,
            emojiUnicode = null
        )
    )
    private fun getEmojiDrawerItems(notes: List<NoteEntity>): List<DrawerItem>{
        val emojiItems = notes.filter { note ->
            !viewModel.dataBaseValues.value.allTrashNotes.contains(note.convertNoteTrashEntity())
        }.groupingBy {
            it.emojiUnicode
        }.eachCount()
        val emojiDrawerList: MutableList<DrawerItem> = mutableListOf()
        emojiItems.forEach { (unicode, count) ->
            emojiDrawerList.add(
                DrawerItem(
                    title = unicode.toString(),
                    count= count,
                    type = DrawerItemType.CategoryEmoji,
                    resID = null,
                    emojiUnicode = unicode
                )
            )
        }
        Timber.d("current all notes in main activity: \nemoji drawer list: $emojiDrawerList\nemoji drawer list size: ${emojiDrawerList.size}")
        return emojiDrawerList.toList()
    }

    private fun getNavigationDrawerItem(): List<DrawerItem> = listOf(
        DrawerItem(
            title = getString(R.string.read_md),
            count = 0,
            type = DrawerItemType.Navigation,
            resID = R.drawable.edit,
            emojiUnicode = null
        ),
        DrawerItem(
            title = getString(R.string.read_json),
            count = 0,
            type = DrawerItemType.Navigation,
            resID = R.drawable.json_file,
            emojiUnicode = null
        ),
        DrawerItem(
            title = getString(R.string.main_menu_setting),
            count = 0,
            type = DrawerItemType.Navigation,
            resID = R.drawable.settings,
            emojiUnicode = null
        )
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun changeIconColorByTheme(){
        binding.bottomAppBar.menu.apply {
            findItem(R.id.bottom_bar_item_draft).iconTintList = ColorStateList.valueOf(getColor(R.color.text_color_main))
            findItem(R.id.bottom_app_bar_item_preview_raw_change).iconTintList = ColorStateList.valueOf(getColor(R.color.text_color_main))
            findItem(R.id.bottom_app_bar_item_emoji_unicode_text).iconTintList = ColorStateList.valueOf(getColor(R.color.text_color_main))
            findItem(R.id.bottom_app_bar_item_export_note).iconTintList = ColorStateList.valueOf(getColor(R.color.text_color_main))
            findItem(R.id.bottom_app_bar_item_restore_note).iconTintList = ColorStateList.valueOf(getColor(R.color.text_color_main))
            findItem(R.id.bottom_app_bar_item_delete_note).iconTintList = ColorStateList.valueOf(getColor(R.color.text_color_main))
            findItem(R.id.bottom_app_bar_item_preview_raw_change_in_show_fragment).iconTintList = ColorStateList.valueOf(getColor(R.color.text_color_main))
        }
    }

}