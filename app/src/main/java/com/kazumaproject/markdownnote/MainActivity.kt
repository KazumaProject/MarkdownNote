package com.kazumaproject.markdownnote

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.kazumaproject.markdownnote.databinding.ActivityMainBinding
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
        }

        binding.bottomAppBar.apply {
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
    }
}