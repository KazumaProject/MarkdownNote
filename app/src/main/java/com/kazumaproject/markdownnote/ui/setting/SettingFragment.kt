package com.kazumaproject.markdownnote.ui.setting

import android.icu.text.CaseMap.Title
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.findNavController
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.kazumaproject.markdownnote.MainViewModel
import com.kazumaproject.markdownnote.R
import com.kazumaproject.markdownnote.other.FragmentType
import com.kazumaproject.markdownnote.other.collectLatestLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment : PreferenceFragmentCompat() {

    private val settingViewModel: SettingViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    private var onBackPressedCallback: OnBackPressedCallback? = null

    @Inject
    lateinit var gson: Gson

    private var startBackupPreference: Preference? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        collectLatestLifecycleFlow(settingViewModel.getAllNotes()){ notes ->
            startBackupPreference?.let { backupPreference ->
                backupPreference.setOnPreferenceClickListener {
                    val jsonObject: String = gson.toJson(notes)
                    val title = "markdown_note_backup_${System.currentTimeMillis()}"
                    saveAllNotes(jsonObject, title)
                    Snackbar.make(
                        requireView(),
                        "$title is created at \n${
                            settingViewModel.getFilePathEditText()
                        }",
                        Snackbar.LENGTH_LONG
                    ).show()
                    return@setOnPreferenceClickListener true
                }
            }
        }

        activityViewModel.updateCurrentFragmentType(FragmentType.SettingFragment)
        activityViewModel.updateFloatingButtonEnableState(false)
        onBackPressedCallback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                requireActivity().findNavController(R.id.navHostFragment).popBackStack()
            }
        }
        onBackPressedCallback?.let { backPressed ->
            requireActivity().onBackPressedDispatcher.addCallback(backPressed)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback = null
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preference, rootKey)
        val filePathEditText = findPreference<EditTextPreference>(getString(R.string.file_path_edit_text_preference_key))
        startBackupPreference = findPreference(getString(R.string.backup_save_preference))
        filePathEditText?.let { editText ->
            editText.apply {
                summary = settingViewModel.getFilePathEditText()
                text = settingViewModel.getFilePathEditText()
                setOnPreferenceChangeListener { _, newValue ->
                    summary = newValue.toString()
                    return@setOnPreferenceChangeListener true
                }
            }
        }
    }


    private fun saveAllNotes(string: String, title: String){
        File(
            settingViewModel.getFilePathEditText(),
            "$title.txt"
        ).writer().use {
            it.write(string)
        }
    }

}