package com.kazumaproject.markdownnote.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.kazumaproject.emojipicker.other.convertUnicode
import com.kazumaproject.markdownnote.R
import com.kazumaproject.markdownnote.database.note.NoteEntity
import com.kazumaproject.markdownnote.other.DateAgoCalculator
import com.kazumaproject.markdownnote.other.getTitleFromNote
import xyz.hanks.library.bang.SmallBangView
import java.util.*

class HomeNotesRecyclerViewAdapter: RecyclerView.Adapter<HomeNotesRecyclerViewAdapter.HomeNotesViewHolder>() {

    inner class HomeNotesViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    private val diffCallback = object : DiffUtil.ItemCallback<NoteEntity>() {
        override fun areItemsTheSame(oldItem: NoteEntity, newItem: NoteEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NoteEntity, newItem: NoteEntity): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private var onItemClickListener: ((NoteEntity, Int) -> Unit)? = null

    fun setOnItemClickListener(onItemClick: (NoteEntity, Int) -> Unit) {
        this.onItemClickListener = onItemClick
    }

    private var onItemLikedClickListener: ((NoteEntity, Int, Boolean) -> Unit)? = null

    fun setOnItemLikedClickListener(onItemLikeClick: (NoteEntity, Int, Boolean) -> Unit) {
        this.onItemLikedClickListener = onItemLikeClick
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var filtered_notes: List<NoteEntity>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeNotesViewHolder {
        return HomeNotesViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.note_item_layout,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return filtered_notes.size
    }

    override fun onBindViewHolder(holder: HomeNotesViewHolder, position: Int) {
        val note = filtered_notes[position]
        holder.itemView.apply {
            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(note, position)
                }
            }
            val emojiText = findViewById<MaterialTextView>(R.id.note_item_emoji_view)
            val noteTitleText = findViewById<MaterialTextView>(R.id.note_item_title)
            val noteTimeText = findViewById<MaterialTextView>(R.id.note_item_time_text)
            val likedButton = findViewById<SmallBangView>(R.id.note_item_like_heart_parent)
            emojiText.text = note.emojiUnicode.convertUnicode()
            noteTitleText.text = note.body.getTitleFromNote()
            noteTimeText.text = DateAgoCalculator.getLabel(
                Date(System.currentTimeMillis()),
                Date(note.updatedAt),
                context
            )
            likedButton.apply {
                setOnClickListener {
                    onItemLikedClickListener?.let { likedClick ->
                        isSelected = !this.isSelected
                        likeAnimation()
                        likedClick(note, position,isSelected)
                    }
                }
            }
        }
    }
}