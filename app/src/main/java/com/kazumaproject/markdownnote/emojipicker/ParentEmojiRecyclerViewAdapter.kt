package com.kazumaproject.markdownnote.emojipicker

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.emoji.widget.EmojiTextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kazumaproject.markdownnote.R
import com.kazumaproject.markdownnote.ui.create_edit.CreateEditViewModel

class ParentEmojiRecyclerViewAdapter (
    private val alertDialog: AlertDialog,
    private val createEditViewModel: CreateEditViewModel
    ) : RecyclerView.Adapter<ParentEmojiRecyclerViewAdapter.ParentEmojiListViewHolder>() {
    inner class ParentEmojiListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    private val viewPool = RecyclerView.RecycledViewPool()

    private val diffCallback = object : DiffUtil.ItemCallback<ParentEmojiItem>() {
        override fun areItemsTheSame(oldItem: ParentEmojiItem, newItem: ParentEmojiItem): Boolean {
            return oldItem.patentTitle == newItem.patentTitle
        }

        override fun areContentsTheSame(oldItem: ParentEmojiItem, newItem: ParentEmojiItem): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var parent_emoji_list: List<ParentEmojiItem>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentEmojiListViewHolder {
        return ParentEmojiListViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.emoji_recycler_view_item_layout,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return parent_emoji_list.size
    }

    override fun onBindViewHolder(holder: ParentEmojiListViewHolder, position: Int) {
        val parentItem = parent_emoji_list[position]
        val childRecyclerView = holder.itemView.findViewById<RecyclerView>(R.id.child_recyclerview)
        holder.itemView.apply {
            findViewById<TextView>(R.id.parent_item_title).text = parentItem.patentTitle
        }
        val childLayoutManager = GridLayoutManager(
            childRecyclerView.context,
            6
        )
        childLayoutManager.initialPrefetchItemCount = parentItem.childItemList.size

        val childAdapter = EmojiRecyclerViewAdapter()
        childAdapter.emoji_list = parentItem.childItemList
        childRecyclerView.apply {
            adapter = childAdapter
            setRecycledViewPool(viewPool)
            layoutManager = childLayoutManager
        }
        childAdapter.setOnItemClickListener { emoji, i ->
            createEditViewModel.updateCurrentEmoji(emoji)
            alertDialog.dismiss()
        }
    }
}