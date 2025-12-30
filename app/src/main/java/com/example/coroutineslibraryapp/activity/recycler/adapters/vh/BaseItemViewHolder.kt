package com.example.coroutineslibraryapp.activity.recycler.adapters.vh

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.coroutineslibraryapp.R
import com.example.coroutineslibraryapp.activity.recycler.Item
import com.example.coroutineslibraryapp.activity.recycler.adapters.LibraryAdapter

abstract class BaseItemViewHolder(
    view: View,
    private val onItemClickListener: LibraryAdapter.OnItemClickListener?
) : RecyclerView.ViewHolder(view) {
    protected val nameView: TextView = view.findViewById(R.id.itemName)
    protected val idView: TextView = view.findViewById(R.id.itemId)
    protected val iconView: ImageView = view.findViewById(R.id.itemIcon)
    protected val cardView: CardView = view.findViewById(R.id.itemCard)

    init {
        cardView.setOnClickListener {
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                onItemClickListener?.onItemClick(bindingAdapterPosition, item)
            }
        }
    }

    protected lateinit var item: Item
    abstract fun bind(item: Item)
}
