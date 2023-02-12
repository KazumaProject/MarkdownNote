package com.kazumaproject.emojipicker

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class EmojiPickerDialogFragment : DialogFragment() {
    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.emoji_picker_layout, null)
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setView(view)
        }
        return builder.create()
    }
}