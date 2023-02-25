package com.kazumaproject.markdownnote.ui.setting

import android.icu.text.CaseMap.Title
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.kazumaproject.markdownnote.MainViewModel
import com.kazumaproject.markdownnote.R
import com.kazumaproject.markdownnote.other.FragmentType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment : PreferenceFragmentCompat() {

    private val settingViewModel: SettingViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    private var onBackPressedCallback: OnBackPressedCallback? = null

    @Inject
    lateinit var gson: Gson

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityViewModel.updateCurrentFragmentType(FragmentType.SettingFragment)
        activityViewModel.updateFloatingButtonEnableState(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        val startBackupPreference = findPreference<Preference>(getString(R.string.backup_save_preference))
        filePathEditText?.let { editText ->
            editText.apply {
                summary = settingViewModel.getFilePathEditText()
                text = settingViewModel.getFilePathEditText()
                setOnPreferenceChangeListener { _, newValue ->
                    summary = newValue.toString()
                    return@setOnPreferenceChangeListener true
                }
            }
            startBackupPreference?.let { backupPreference ->
                backupPreference.setOnPreferenceClickListener {
                    CoroutineScope(Dispatchers.IO).launch{
                        val jsonObject: String = gson.toJson(settingViewModel.getAllNotes())
                        val title = "markdown_note_backup${System.currentTimeMillis()}"
                        saveAllNotes(jsonObject, title)
                        delay(500)
                        withContext(Dispatchers.Main){
                            Snackbar.make(requireView(),
                                "$title.txt is created.\n${
                                    settingViewModel.getFilePathEditText()
                                }/",
                                Snackbar.LENGTH_LONG).show()
                        }
                    }
                    return@setOnPreferenceClickListener true
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