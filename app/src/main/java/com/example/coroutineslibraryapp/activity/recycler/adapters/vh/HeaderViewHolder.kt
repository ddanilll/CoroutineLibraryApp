package com.example.coroutineslibraryapp.activity.recycler.adapters.vh

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coroutineslibraryapp.R
import com.example.coroutineslibraryapp.activity.recycler.Item

class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val titleView: TextView = view.findViewById(R.id.sectionTitle)

    fun bind(header: Item.Header) {
        titleView.text = header.title
    }
}

