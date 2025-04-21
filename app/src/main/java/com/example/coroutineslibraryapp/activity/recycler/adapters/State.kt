package com.example.coroutineslibraryapp.activity.recycler.adapters

import com.example.coroutineslibraryapp.activity.recycler.Item

sealed interface State {
    object Loading : State
    data class Error(val message: String) : State
    data class Content(val items: List<Item>) : State
}