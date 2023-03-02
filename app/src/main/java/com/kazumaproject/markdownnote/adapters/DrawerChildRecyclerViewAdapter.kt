package com.kazumaproject.markdownnote.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.kazumaproject.emojipicker.other.convertUnicode
import com.kazumaproject.markdownnote.R
import com.kazumaproject.markdownnote.drawer.model.DrawerItem
import com.kazumaproject.markdownnote.drawer.model.DrawerItemType

class DrawerChildRecyclerViewAdapter : RecyclerView.Adapter<DrawerChildRecyclerViewAdapter.DrawerChildLViewHolder>() {

    inner class DrawerChildLViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    private val diffCallback = object : DiffUtil.ItemCallback<DrawerItem>() {
        override fun areItemsTheSame(oldItem: DrawerItem, newItem: DrawerItem): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: DrawerItem, newItem: DrawerItem): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private var onItemClickListener: ((DrawerItem, Int) -> Unit)? = null

    fun setOnItemClickListener(onItemClick: (DrawerItem, Int) -> Unit) {
        this.onItemClickListener = onItemClick
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var drawer_item_list: List<DrawerItem>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawerChildLViewHolder {
        return DrawerChildLViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.drawer_child_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return drawer_item_list.size
    }

    override fun onBindViewHolder(holder: DrawerChildLViewHolder, position: Int) {
        val drawerItem = drawer_item_list[position]
        holder.itemView.apply {
            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(drawerItem, position)
                }
            }
            val drawerIcon = findViewById<ShapeableImageView>(R.id.drawer_child_icon)
            val drawerTitle = findViewById<MaterialTextView>(R.id.drawer_child_item_title)
            val drawerCount = findViewById<MaterialTextView>(R.id.drawer_child_item_count_text)

            when(drawerItem.type){
                is DrawerItemType.FilterNotes -> {
                    drawerItem.resID?.let { id ->
                        drawerIcon.background = ContextCompat.getDrawable(context,id)
                        drawerIcon.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context,R.color.text_color_main))
                    }
                    drawerTitle.text = drawerItem.title
                    drawerCount.text = if (drawerItem.count >= 100) "99+" else drawerItem.count.toString()
                }
                is DrawerItemType.CategoryEmoji -> {
                    drawerItem.emojiUnicode?.let { unicode ->
                        drawerTitle.text = unicode.convertUnicode()
                    }
                    drawerIcon.background = null
                    drawerCount.text = if (drawerItem.count >= 100) "99+" else drawerItem.count.toString()
                }
                is DrawerItemType.Navigation -> {
                    drawerItem.resID?.let { id ->
                        drawerIcon.background = ContextCompat.getDrawable(context,id)
                        drawerIcon.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context,R.color.text_color_main))
                    }
                    drawerTitle.text = drawerItem.title
                    drawerCount.text = ""
                }
            }
        }
    }
}