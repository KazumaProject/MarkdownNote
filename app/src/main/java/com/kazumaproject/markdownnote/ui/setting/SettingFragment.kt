package com.kazumaproject.markdownnote.ui.setting

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kazumaproject.markdownnote.MainViewModel
import com.kazumaproject.markdownnote.R
import com.kazumaproject.markdownnote.database.note.NoteEntity
import com.kazumaproject.markdownnote.other.FragmentType
import com.kazumaproject.markdownnote.other.collectLatestLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment : PreferenceFragmentCompat() {

    private val settingViewModel: SettingViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    private var onBackPressedCallback: OnBackPressedCallback? = null

    @Inject
    lateinit var gson: Gson

    private var startBackupPreference: Preference? = null

    companion object {
        private const val READ_REQUEST_CODE: Int = 77
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        collectLatestLifecycleFlow(settingViewModel.getAllNotes()){ notes ->
            startBackupPreference?.let { backupPreference ->
                backupPreference.setOnPreferenceClickListener {
                    val jsonObject: String = gson.toJson(
                        notes.filter { note ->
                            note.id !in activityViewModel.dataBaseValues.value.allTrashNotes.map {
                                it.id
                            }
                        }
                    )
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
        val loadBackupPreference = findPreference<Preference>("load_backup_txt")
        loadBackupPreference?.let { loadBackup ->
            loadBackup.setOnPreferenceClickListener {
                selectFileByUri()
                return@setOnPreferenceClickListener true
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

    private fun selectFileByUri(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
        }
        resultLauncher.launch(intent)
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.also { uri ->
                if (uri.path?.contains("markdown_note_backup_") == true) {
                    val content: String = readTextFile(uri)
                    try {
                        content.let { contents ->
                            val myType = object : TypeToken<List<NoteEntity>>() {}.type
                            val notesFromTxt = gson.fromJson<List<NoteEntity>>(contents, myType)
                            settingViewModel.insertAllNotes(notesFromTxt)
                        }
                    }catch (e: Exception){
                        Snackbar.make(
                            requireView(),
                            "Saved text is not valid.",
                            Snackbar.LENGTH_LONG
                        ).show()
                        e.printStackTrace()
                    }
                } else {
                    Snackbar.make(
                        requireView(),
                        "The selected file is not valid.",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun readTextFile(uri: Uri): String {
        var reader: BufferedReader? = null
        val builder = StringBuilder()
        try {
            reader = BufferedReader(InputStreamReader(context?.contentResolver?.openInputStream(uri)))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                builder.append(line)
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

}