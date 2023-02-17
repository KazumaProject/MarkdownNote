package com.kazumaproject.markdownnote

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.textfield.TextInputEditText
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
            binding.addFloatingButton.alpha = if (fragmentAndFloatingButtonState.floatingButtonState) 1.0f else 0.5f
        }
        setBottomAppBar()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let { event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val v = currentFocus
                if (v is TextInputEditText) {
                    val outRect = Rect()
                    v.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())){
                        v.clearFocus()
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(v.windowToken,0)
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun setAppBottomBarAppearanceByFragmentType(type: FragmentType, isEnable: Boolean){
        when(type){
            is FragmentType.HomeFragment -> {
                setBottomAppBarMenuItemsVisibility(true)
                binding.addFloatingButton.apply {
                    setImageResource(R.drawable.baseline_add_24)
                    isEnabled = isEnable
                }
            }
            is FragmentType.CreateEditFragment -> {
                setBottomAppBarMenuItemsVisibility(false)
                binding.addFloatingButton.apply {
                    setImageResource(R.drawable.diskette)
                    isEnabled = isEnable
                }
            }
            is FragmentType.DraftFragment -> {
                setBottomAppBarMenuItemsVisibility(false)
                binding.addFloatingButton.apply {
                    setImageResource(R.drawable.draft)
                    isEnabled = isEnable
                }
            }
            is FragmentType.SettingFragment -> {
                setBottomAppBarMenuItemsVisibility(false)
                binding.addFloatingButton.apply {
                    setImageResource(R.drawable.settings)
                    isEnabled = isEnable
                }
            }
        }
    }

    private fun setBottomAppBar() = binding.bottomAppBar.apply {
        setupActionBarWithNavController(findNavController(R.id.navHostFragment))
        setOnMenuItemClickListener { item ->
            when(item.itemId){
                R.id.bottom_bar_item_draft -> findNavController(R.id.navHostFragment).navigate(
                    HomeFragmentDirections.actionHomeFragmentToDraftFragment()
                )
                R.id.bottom_bar_item_setting -> findNavController(R.id.navHostFragment).navigate(
                    HomeFragmentDirections.actionHomeFragmentToSettingFragment()
                )
                R.id.bottom_bar_item_back_arrow -> findNavController(R.id.navHostFragment).popBackStack()
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
            findItem(R.id.bottom_bar_item_back_arrow).isVisible = !visibility
        }
        binding.addFloatingButton.isEnabled = visibility
    }
}