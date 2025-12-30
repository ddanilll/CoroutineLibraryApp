package com.example.coroutineslibraryapp.activity.recycler

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
sealed class Item : Parcelable {

    abstract fun isSameItem(other: Item): Boolean

    @Parcelize
    data class Header(val title: String) : Item() {
        override fun isSameItem(other: Item): Boolean = other is Header && title == other.title
    }

    @Parcelize
    data class Book(
        val name: String, val id: Int, val info: String
    ) : Item() {
        override fun isSameItem(other: Item): Boolean = other is Book && id == other.id
    }

    @Parcelize
    data class Newspaper(
        val name: String, val id: Int, val info: String
    ) : Item() {
        override fun isSameItem(other: Item): Boolean = other is Newspaper && id == other.id
    }

    @Parcelize
    data class Disk(
        val name: String, val id: Int, val info: String
    ) : Item() {
        override fun isSameItem(other: Item): Boolean = other is Disk && id == other.id
    }
}
