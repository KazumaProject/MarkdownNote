package com.kazumaproject.markdownnote

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.kazumaproject.markdownnote.databinding.ActivityMainBinding
import com.kazumaproject.markdownnote.other.FragmentType
import com.kazumaproject.markdownnote.other.collectLatestLifecycleFlow
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

        collectLatestLifecycleFlow(viewModel.current_fragment_type){
            Timber.d("current fragment: $it")
            setAppBottomBarAppearanceByFragmentType(it)
        }

        setBottomAppBar()
        setFloatingButton()
    }

    private fun setAppBottomBarAppearanceByFragmentType(type: FragmentType){
        when(type){
            is FragmentType.HomeFragment -> {
                setBottomAppBarMenuItemsVisibility(true)
                binding.addFloatingButton.apply {
                    setImageResource(R.drawable.baseline_add_24)
                    alpha = 1.0f
                }
            }
            is FragmentType.CreateEditFragment -> {
                setBottomAppBarMenuItemsVisibility(false)
                binding.addFloatingButton.apply {
                    setImageResource(R.drawable.diskette)
                    alpha = 0.5f
                }
            }
            is FragmentType.DraftFragment -> {
                setBottomAppBarMenuItemsVisibility(false)
                binding.addFloatingButton.apply {
                    setImageResource(R.drawable.draft)
                    alpha = 0.5f
                }
            }
            is FragmentType.SettingFragment -> {
                setBottomAppBarMenuItemsVisibility(false)
                binding.addFloatingButton.apply {
                    setImageResource(R.drawable.settings)
                    alpha = 0.5f
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

    private fun setFloatingButton() = binding.addFloatingButton.apply {
        setOnClickListener {
            findNavController(R.id.navHostFragment).navigate(
                HomeFragmentDirections.actionHomeFragmentToCreateEditFragment()
            )
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