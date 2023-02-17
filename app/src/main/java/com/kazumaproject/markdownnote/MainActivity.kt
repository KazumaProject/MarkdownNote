package com.kazumaproject.markdownnote

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Switch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.kazumaproject.markdownnote.databinding.ActivityMainBinding
import com.kazumaproject.markdownnote.other.FragmentType
import com.kazumaproject.markdownnote.other.KeyboardHelper
import com.kazumaproject.markdownnote.other.collectLatestLifecycleFlow
import com.kazumaproject.markdownnote.ui.create_edit.CreateEditFragmentDirections
import com.kazumaproject.markdownnote.ui.home.HomeFragmentDirections
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val viewModel : MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        setupActionBarWithNavController(findNavController(R.id.navHostFragment))

        collectLatestLifecycleFlow(viewModel.fragmentAndFloatingButtonState){ fragmentAndFloatingButtonState ->
            setAppBottomBarAppearanceByFragmentType(fragmentAndFloatingButtonState.currentFragmentType, fragmentAndFloatingButtonState.floatingButtonState, fragmentAndFloatingButtonState.hasFocus)
            setFloatingButton(fragmentAndFloatingButtonState.currentFragmentType)
            binding.addFloatingButton.alpha = if (fragmentAndFloatingButtonState.floatingButtonState) 1.0f else 0.5f
            setBottomAppBar(fragmentAndFloatingButtonState.currentFragmentType, fragmentAndFloatingButtonState.hasFocus)
        }
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
                    setImageResource(R.drawable.draft)
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
        val markdownSwitch = Switch(this@MainActivity)
        markdownSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateMarkdownSwitchState(isChecked)
        }
        val bottomAppBarItemPreviewRawChange = menu.findItem(R.id.bottom_app_bar_item_preview_raw_change)
        bottomAppBarItemPreviewRawChange.actionView = markdownSwitch
        setOnMenuItemClickListener { item ->
            when(item.itemId){
                R.id.bottom_bar_item_draft -> findNavController(R.id.navHostFragment).navigate(
                    HomeFragmentDirections.actionHomeFragmentToDraftFragment()
                )
                R.id.bottom_bar_item_setting -> findNavController(R.id.navHostFragment).navigate(
                    HomeFragmentDirections.actionHomeFragmentToSettingFragment()
                )
                R.id.bottom_bar_item_back_arrow ->{
                    when(fragmentType){
                        is FragmentType.HomeFragment -> {
                            this@MainActivity.finish()
                        }
                        is FragmentType.CreateEditFragment -> {
                            if (hasFocus) KeyboardHelper.hideKeyboardAndClearFocus(this@MainActivity) else findNavController(R.id.navHostFragment).popBackStack()
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
            findItem(R.id.bottom_bar_item_setting).isVisible = visibility
            findItem(R.id.bottom_bar_item_back_arrow).isVisible = !visibility
            findItem(R.id.bottom_app_bar_item_preview_raw_change).isVisible = switchVisibility && !hasFocus
        }
        binding.addFloatingButton.isEnabled = visibility
    }
}