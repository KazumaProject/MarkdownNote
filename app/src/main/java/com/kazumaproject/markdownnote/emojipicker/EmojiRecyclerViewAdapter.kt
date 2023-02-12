package com.kazumaproject.markdownnote.emojipicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.emoji.widget.EmojiTextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kazumaproject.markdownnote.R

class EmojiRecyclerViewAdapter : RecyclerView.Adapter<EmojiRecyclerViewAdapter.EmojiListViewHolder>() {

    inner class EmojiListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    private val diffCallback = object : DiffUtil.ItemCallback<Emoji>() {
        override fun areItemsTheSame(oldItem: Emoji, newItem: Emoji): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Emoji, newItem: Emoji): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private var onItemClickListener: ((Emoji, Int) -> Unit)? = null

    fun setOnItemClickListener(onItemClick: (Emoji, Int) -> Unit) {
        this.onItemClickListener = onItemClick
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var emoji_list: List<Emoji>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiListViewHolder {
        return EmojiListViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.emoji_recycler_view_children_layout,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return emoji_list.size
    }

    override fun onBindViewHolder(holder: EmojiListViewHolder, position: Int) {
        val emoji = emoji_list[position]
        holder.itemView.apply {
            findViewById<EmojiTextView>(R.id.emoji_picker_item_text_view).text = emoji.unicode.convertUnicode()
            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(emoji,position)
                }
            }
        }
    }

}