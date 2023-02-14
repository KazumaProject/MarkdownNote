package com.kazumaproject.emojipicker

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kazumaproject.emojipicker.adapter.ParentEmojiRecyclerViewAdapter
import com.kazumaproject.emojipicker.model.Emoji
import com.kazumaproject.emojipicker.model.ParentEmojiItem
import com.kazumaproject.emojipicker.other.Constants.EMOJI_LIST_ANIMALS_NATURE
import com.kazumaproject.emojipicker.other.Constants.EMOJI_LIST_SMILEYS_PEOPLE

class EmojiPickerDialogFragment (
    private val mEmojiItemClickListener: EmojiItemClickListener
        ): DialogFragment(){

    private var adapter: ParentEmojiRecyclerViewAdapter? = null

    interface EmojiItemClickListener{
        fun onEmojiClicked(emoji: Emoji)
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val parentView = layoutInflater.inflate(R.layout.emoji_picker_layout, null)
        val builder = AlertDialog.Builder(context).create()
        builder.apply {
            setView(parentView)
        }
        adapter = ParentEmojiRecyclerViewAdapter(builder, mEmojiItemClickListener)
        setParentRecyclerView(parentView.findViewById(R.id.emoji_recycler_view), adapter)
        return builder
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }

    private fun setParentRecyclerView(recyclerView: RecyclerView, parentEmojiRecyclerViewAdapter: ParentEmojiRecyclerViewAdapter?) = recyclerView.apply {
        parentEmojiRecyclerViewAdapter?.let { parentAdapter ->
            val emoji_itemList = ArrayList<ParentEmojiItem>()
            val emoji_item_smiley_and_people = ParentEmojiItem(
                "Smileys & People",
                EMOJI_LIST_SMILEYS_PEOPLE
            )
            emoji_itemList.add(emoji_item_smiley_and_people)
            val emoji_item_animals_and_nature = ParentEmojiItem(
                "Animals & Nature",
                EMOJI_LIST_ANIMALS_NATURE
            )
            emoji_itemList.add(emoji_item_animals_and_nature)
            parentAdapter.parent_emoji_list = emoji_itemList
            this.adapter = parentAdapter
        }
        this.layoutManager = LinearLayoutManager(requireContext())
    }

}