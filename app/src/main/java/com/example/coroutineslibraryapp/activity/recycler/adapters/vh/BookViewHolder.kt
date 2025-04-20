package com.example.coroutineslibraryapp.activity.recycler.adapters.vh

import android.view.View
import com.example.coroutineslibraryapp.R
import com.example.coroutineslibraryapp.activity.recycler.Item
import com.example.coroutineslibraryapp.activity.recycler.adapters.LibraryAdapter

class BookViewHolder(
    view: View,
    onItemClickListener: LibraryAdapter.OnItemClickListener?
) : BaseItemViewHolder(view, onItemClickListener) {

    override fun bind(item: Item) {
        this.item = item
        if (item !is Item.Book) return

        nameView.text = item.name
        idView.text = itemView.context.getString(R.string.item_id_format, item.id)
        iconView.setImageResource(R.drawable.ic_bookimage)


    }
}
