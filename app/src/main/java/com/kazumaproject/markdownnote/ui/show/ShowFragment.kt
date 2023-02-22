package com.kazumaproject.markdownnote.ui.show

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Switch
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.textview.MaterialTextView
import com.kazumaproject.emojipicker.other.convertUnicode
import com.kazumaproject.markdownnote.MainViewModel
import com.kazumaproject.markdownnote.R
import com.kazumaproject.markdownnote.databinding.FragmentDraftBinding
import com.kazumaproject.markdownnote.other.FragmentType
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ShowFragment : Fragment() {

    private val showViewModel: ShowViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()
    private var _binding : FragmentDraftBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var markwon: Markwon

    private var onBackPressedCallback: OnBackPressedCallback? =null

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
                    markwon.setMarkdown(binding.showFragmentMarkwonText, it.body)
                }
            }
        }
        onBackPressedCallback?.let { backPressed ->
            requireActivity().onBackPressedDispatcher.addCallback(backPressed)
        }
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(),R.color.markdown_bg_color)

        createMenuItemsInBottomAppBarInMainActivity(false,128021)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback = null
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(),R.color.window_bg_color)
        _binding = null
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun createMenuItemsInBottomAppBarInMainActivity(state: Boolean, unicode: Int){
        val markdownSwitch = Switch(requireContext())
        markdownSwitch.isChecked = state
        markdownSwitch.setOnCheckedChangeListener { _, isChecked ->

        }
        val emojiText = MaterialTextView(requireContext())
        emojiText.apply {
            textSize = 24f
            text = unicode.convertUnicode()
        }

        requireActivity().findViewById<BottomAppBar>(R.id.bottom_app_bar).apply {
            menu.findItem(R.id.bottom_app_bar_item_emoji_unicode_text).actionView = emojiText
            menu.findItem(R.id.bottom_app_bar_item_preview_raw_change_in_show_fragment).actionView =
                markdownSwitch
        }
    }

}