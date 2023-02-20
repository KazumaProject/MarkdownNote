package com.kazumaproject.markdownnote.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kazumaproject.markdownnote.R
import com.kazumaproject.markdownnote.drawer.model.DrawerItem
import com.kazumaproject.markdownnote.drawer.model.DrawerParentItem

class DrawerParentRecyclerViewAdapter: RecyclerView.Adapter<DrawerParentRecyclerViewAdapter.ParentDrawerViewHolder>() {

    inner class ParentDrawerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    private val viewPool = RecyclerView.RecycledViewPool()

    private val diffCallback = object : DiffUtil.ItemCallback<DrawerParentItem>() {
        override fun areItemsTheSame(oldItem: DrawerParentItem, newItem: DrawerParentItem): Boolean {
            return oldItem.parentTitle == newItem.parentTitle
        }

        override fun areContentsTheSame(oldItem: DrawerParentItem, newItem: DrawerParentItem): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private var onItemClickListener: ((DrawerItem, Int) -> Unit)? = null

    fun setOnItemClickListener(onItemClick: (DrawerItem, Int) -> Unit) {
        this.onItemClickListener = onItemClick
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var parent_drawer_item_list: List<DrawerParentItem>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentDrawerViewHolder {
        return ParentDrawerViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.drawer_parent_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return parent_drawer_item_list.size
    }

    override fun onBindViewHolder(holder: ParentDrawerViewHolder, position: Int) {
        val parentItem = parent_drawer_item_list[position]
        val childRecyclerView = holder.itemView.findViewById<RecyclerView>(R.id.drawer_child_recyclerview)
        holder.itemView.apply {
            findViewById<TextView>(R.id.drawer_parent_item_title).text = parentItem.parentTitle
        }
        val childLayoutManager = LinearLayoutManager(childRecyclerView.context)
        childLayoutManager.initialPrefetchItemCount = parentItem.childList.size

        val childAdapter = DrawerChildRecyclerViewAdapter()
        childAdapter.drawer_item_list = parentItem.childList
        childRecyclerView.apply {
            adapter = childAdapter
            setRecycledViewPool(viewPool)
            layoutManager = childLayoutManager
        }
        childAdapter.setOnItemClickListener { item, i ->
            onItemClickListener?.let { click ->
                click(item, i)
            }
        }
    }
}