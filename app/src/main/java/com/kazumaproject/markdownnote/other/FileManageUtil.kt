package com.kazumaproject.markdownnote.other

import android.content.Context
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.MarginLayoutParams
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.view.updateLayoutParams
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.kazumaproject.markdownnote.R
import com.kazumaproject.markdownnote.ui.show.ShowViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class FileManageUtil {

    fun showAlertDialogForExportNote(
        context: Context,
        showViewModel: ShowViewModel,
        view: View
    ){
        val editText = EditText(context)
        editText.apply {
            setText(System.currentTimeMillis().toString())
            requestFocus()
            selectAll()
        }
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setView(editText)
        alertDialog.apply {
            setTitle(context.getString(R.string.save_note))
            setMessage("Path: ${ Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}/")
            setCancelable(true)
            setPositiveButton("txt") { dialog, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    val note = showViewModel.getNote(showViewModel.noteDataBaseData.value.noteId)
                    note?.let { noteTmp ->
                        saveFile(noteTmp.body, editText.text.toString())
                        delay(500)
                        launch(Dispatchers.Main) {
                            Snackbar.make(view,
                                "markdown_note_${editText.text}.txt is created.\\n${
                                    Environment.getExternalStoragePublicDirectory(
                                        Environment.DIRECTORY_DOCUMENTS
                                    )
                                }/",
                                Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
                dialog.dismiss()
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            setNeutralButton("md"){ dialog, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    val note = showViewModel.getNote(showViewModel.noteDataBaseData.value.noteId)
                    note?.let { noteTmp ->
                        saveFileInMD(noteTmp.body, editText.text.toString())
                        delay(500)
                        launch(Dispatchers.Main) {
                            Snackbar.make(view,
                                "markdown_note_${editText.text}.md is created.\\n${
                                    Environment.getExternalStoragePublicDirectory(
                                        Environment.DIRECTORY_DOCUMENTS
                                    )
                                }/",
                                Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
                dialog.dismiss()
            }
        }
        alertDialog.show()
    }

    private fun saveFile(string: String, title: String){
        File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "markdown_note_$title.txt"
        ).writer().use {
            it.write(string)
        }
    }

    private fun saveFileInMD(string: String, title: String){
        File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "markdown_note_$title.md"
        ).writer().use {
            it.write(string)
        }
    }

}