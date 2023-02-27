package com.kazumaproject.markdownnote.ui.setting

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
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
import timber.log.Timber
import java.io.BufferedReader
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

    private var allNotesInString: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        collectLatestLifecycleFlow(settingViewModel.getAllNotes()){ notes ->
            startBackupPreference?.let { backupPreference ->
                backupPreference.setOnPreferenceClickListener {
                    allNotesInString = gson.toJson(
                        notes.filter { note ->
                            note.id !in activityViewModel.dataBaseValues.value.allTrashNotes.map {
                                it.id
                            }
                        }
                    )
                    val title = "markdown_note_backup_${System.currentTimeMillis()}"
                    createLauncherTxt.launch(title)
                    Snackbar.make(
                        requireView(),
                        "$title is created.",
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

    private val createLauncherTxt = registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        uri ?: return@registerForActivityResult
        requireContext().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        val documentFile = DocumentFile.fromSingleUri(
            requireContext(),
            uri
        )
        allNotesInString?.let { notes ->
            documentFile?.let { file ->
                val out = requireContext().contentResolver.openOutputStream(file.uri)
                out?.apply {
                    write(notes.toByteArray())
                    flush()
                    close()
                }
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preference, rootKey)

        startBackupPreference = findPreference(getString(R.string.backup_save_preference))

        val loadBackupPreference = findPreference<Preference>("load_backup_txt")
        loadBackupPreference?.let { loadBackup ->
            loadBackup.setOnPreferenceClickListener {
                selectFileByUri()
                return@setOnPreferenceClickListener true
            }
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
                val returnCursor: Cursor? = requireContext().contentResolver.query(uri, null, null, null, null)
                val nameIndex: Int? = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                returnCursor?.moveToFirst()
                nameIndex?.let { index ->
                    Timber.d("selected uri: ${returnCursor.getString(index)}")
                    if (returnCursor.getString(index).contains("markdown_note_backup_")) {
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