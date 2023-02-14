package com.kazumaproject.markdownnote

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.kazumaproject.markdownnote.databinding.ActivityMainBinding
import com.kazumaproject.markdownnote.other.FragmentType
import com.kazumaproject.markdownnote.other.collectLatestLifecycleFlow
import com.kazumaproject.markdownnote.ui.create_edit.CreateEditFragmentDirections
import com.kazumaproject.markdownnote.ui.home.HomeFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val viewModel : MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        collectLatestLifecycleFlow(viewModel.fragmentAndFloatingButtonState){ fragmentAndFloatingButtonState ->
            setAppBottomBarAppearanceByFragmentType(fragmentAndFloatingButtonState.currentFragmentType, fragmentAndFloatingButtonState.floatingButtonState)
            setFloatingButton(fragmentAndFloatingButtonState.currentFragmentType)
        }
        setBottomAppBar()
    }
    private fun setAppBottomBarAppearanceByFragmentType(type: FragmentType, isEnable: Boolean){
        when(type){
            is FragmentType.HomeFragment -> {
                setBottomAppBarMenuItemsVisibility(true)
                binding.addFloatingButton.apply {
                    setImageResource(R.drawable.baseline_add_24)
                    alpha = 1.0f
                    isEnabled = isEnable
                }
            }
            is FragmentType.CreateEditFragment -> {
                setBottomAppBarMenuItemsVisibility(false)
                binding.addFloatingButton.apply {
                    setImageResource(R.drawable.diskette)
                    alpha = 0.5f
                    isEnabled = isEnable
                }
            }
            is FragmentType.DraftFragment -> {
                setBottomAppBarMenuItemsVisibility(false)
                binding.addFloatingButton.apply {
                    setImageResource(R.drawable.draft)
                    alpha = 0.5f
                    isEnabled = isEnable
                }
            }
            is FragmentType.SettingFragment -> {
                setBottomAppBarMenuItemsVisibility(false)
                binding.addFloatingButton.apply {
                    setImageResource(R.drawable.settings)
                    alpha = 0.5f
                    isEnabled = isEnable
                }
            }
        }
    }

    private fun setBottomAppBar() = binding.bottomAppBar.apply {
        setupWithNavController(findNavController(R.id.navHostFragment))
        setOnMenuItemClickListener { item ->
            when(item.itemId){
                R.id.bottom_bar_item_draft -> findNavController(R.id.navHostFragment).navigate(
                    HomeFragmentDirections.actionHomeFragmentToDraftFragment()
                )
                R.id.bottom_bar_item_setting -> findNavController(R.id.navHostFragment).navigate(
                    HomeFragmentDirections.actionHomeFragmentToSettingFragment()
                )
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

    private fun setBottomAppBarMenuItemsVisibility(visibility: Boolean){
        binding.bottomAppBar.menu.apply {
            findItem(R.id.bottom_bar_item_draft).isVisible = visibility
            findItem(R.id.bottom_bar_item_setting).isVisible = visibility
        }
        binding.addFloatingButton.isEnabled = visibility
    }
}