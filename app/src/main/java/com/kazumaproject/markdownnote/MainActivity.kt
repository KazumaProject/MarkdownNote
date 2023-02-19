package com.kazumaproject.markdownnote

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Switch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.kazumaproject.markdownnote.database.note.NoteEntity
import com.kazumaproject.markdownnote.databinding.ActivityMainBinding
import com.kazumaproject.markdownnote.drawer.model.DrawerItem
import com.kazumaproject.markdownnote.drawer.model.DrawerItemType
import com.kazumaproject.markdownnote.other.FragmentType
import com.kazumaproject.markdownnote.other.KeyboardHelper
import com.kazumaproject.markdownnote.other.collectLatestLifecycleFlow
import com.kazumaproject.markdownnote.ui.create_edit.CreateEditFragmentDirections
import com.kazumaproject.markdownnote.ui.home.HomeFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val viewModel : MainViewModel by viewModels()

    companion object {

        private const val REQUEST_CODE_PERMISSIONS = 17

        private val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            if (!Environment.isExternalStorageManager()){
                try {
                    val uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
                    startActivity(intent)
                }catch (e : Exception){
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    Timber.d("Error: $e")
                }
            }
            if (!allPermissionsGranted()){
                ActivityCompat.requestPermissions(this,
                    REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            }
        }else {
            if (!allPermissionsGranted()){
                ActivityCompat.requestPermissions(this,
                    REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            }
        }

        supportActionBar?.hide()
        setupActionBarWithNavController(findNavController(R.id.navHostFragment))

        collectLatestLifecycleFlow(viewModel.fragmentAndFloatingButtonState){ fragmentAndFloatingButtonState ->
            setAppBottomBarAppearanceByFragmentType(fragmentAndFloatingButtonState.currentFragmentType, fragmentAndFloatingButtonState.floatingButtonState, fragmentAndFloatingButtonState.hasFocus)
            setFloatingButton(fragmentAndFloatingButtonState.currentFragmentType)
            binding.addFloatingButton.alpha = if (fragmentAndFloatingButtonState.floatingButtonState) 1.0f else 0.5f
            setBottomAppBar(
                fragmentAndFloatingButtonState.currentFragmentType,
                fragmentAndFloatingButtonState.hasFocus
            )
        }

        collectLatestLifecycleFlow(viewModel.markdown_switch_state){ state ->
            val markdownSwitch = Switch(this@MainActivity)
            markdownSwitch.isChecked = state
            markdownSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateMarkdownSwitchState(isChecked)
            }
            val bottomAppBarItemPreviewRawChange = binding.bottomAppBar.menu.findItem(R.id.bottom_app_bar_item_preview_raw_change)
            bottomAppBarItemPreviewRawChange.actionView = markdownSwitch
        }

        collectLatestLifecycleFlow(viewModel.dataBaseValues){ value ->
            getEmojiDrawerItems(value.allNotes)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
    private fun setAppBottomBarAppearanceByFragmentType(type: FragmentType, isEnable: Boolean, hasFocus: Boolean){
        when(type){
            is FragmentType.HomeFragment -> {
                setBottomAppBarMenuItemsVisibility(visibility = true, switchVisibility = false, hasFocus)
                binding.addFloatingButton.apply {
                    setImageResource(R.drawable.baseline_add_24)
                    isEnabled = isEnable
                }
            }
            is FragmentType.CreateEditFragment -> {
                setBottomAppBarMenuItemsVisibility(visibility = false, switchVisibility = true, hasFocus)
                binding.addFloatingButton.apply {
                    setImageResource(R.drawable.diskette)
                    isEnabled = isEnable
                }
            }
            is FragmentType.DraftFragment -> {
                setBottomAppBarMenuItemsVisibility(visibility = false, switchVisibility = false, hasFocus)
                binding.addFloatingButton.apply {
                    setImageResource(R.drawable.inbox)
                    isEnabled = isEnable
                }
            }
            is FragmentType.SettingFragment -> {
                setBottomAppBarMenuItemsVisibility(visibility = false, switchVisibility = false, hasFocus)
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
                R.id.bottom_bar_item_back_arrow ->{
                    when(fragmentType){
                        is FragmentType.HomeFragment -> {
                            this@MainActivity.finish()
                        }
                        is FragmentType.CreateEditFragment -> {
                            when{
                                hasFocus -> KeyboardHelper.hideKeyboardAndClearFocus(this@MainActivity)
                                viewModel.markdown_switch_state.value -> viewModel.updateMarkdownSwitchState(false)
                                else ->  findNavController(R.id.navHostFragment).popBackStack()
                            }
                        }
                        is FragmentType.DraftFragment -> {
                            findNavController(R.id.navHostFragment).popBackStack()
                        }
                        is FragmentType.SettingFragment -> {
                            findNavController(R.id.navHostFragment).popBackStack()
                        }
                    }
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun setFloatingButton(fragmentType: FragmentType) = binding.addFloatingButton.apply {
        when(fragmentType){
            is FragmentType.HomeFragment ->{
                setOnClickListener {
                    findNavController(R.id.navHostFragment).navigate(
                        HomeFragmentDirections.actionHomeFragmentToCreateEditFragment()
                    )
                }
            }
            is FragmentType.CreateEditFragment ->{
                setOnClickListener {
                    viewModel.updateSaveClicked(true)
                    findNavController(R.id.navHostFragment).navigate(
                        CreateEditFragmentDirections.actionCreateEditFragmentToHomeFragment()
                    )
                }
            }
            is FragmentType.DraftFragment ->{

            }
            is FragmentType.SettingFragment ->{

            }
        }
    }

    private fun setBottomAppBarMenuItemsVisibility(visibility: Boolean, switchVisibility: Boolean, hasFocus: Boolean){
        binding.bottomAppBar.menu.apply {
            findItem(R.id.bottom_bar_item_draft).isVisible = visibility
            findItem(R.id.bottom_app_bar_item_preview_raw_change).isVisible = switchVisibility && !hasFocus
            if (hasFocus) findItem(R.id.bottom_bar_item_back_arrow).icon =
                ContextCompat.getDrawable(this@MainActivity,R.drawable.arrow_down) else findItem(R.id.bottom_bar_item_back_arrow).icon =
                ContextCompat.getDrawable(this@MainActivity,R.drawable.back)
        }
        binding.addFloatingButton.isEnabled = visibility
    }

    private fun getMainDrawerItems(databaseValues: DatabaseValues): List<DrawerItem> = listOf(
        DrawerItem(
            title = getString(R.string.all_notes),
            count = databaseValues.allNotes.size,
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
        val emojiItems = notes.groupingBy {
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
}