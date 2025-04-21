package com.example.coroutineslibraryapp.activity.recycler

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutineslibraryapp.activity.recycler.LibraryFragment.Companion.BOOK
import com.example.coroutineslibraryapp.activity.recycler.LibraryFragment.Companion.DISK
import com.example.coroutineslibraryapp.activity.recycler.LibraryFragment.Companion.NEWSPAPER
import com.example.coroutineslibraryapp.activity.recycler.adapters.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class MainViewModel : ViewModel() {

    private val _scrollToPosition = MutableSharedFlow<Int?>(replay = 1)
    val scrollToPosition: SharedFlow<Int?> = _scrollToPosition.asSharedFlow()

    fun setScrollPosition(position: Int?) {
        viewModelScope.launch {
            _scrollToPosition.emit(position)
        }
    }

    fun resetScrollPosition() {
        viewModelScope.launch {
            _scrollToPosition.emit(null)
        }
    }

    private val _state = MutableStateFlow<State>(State.Loading)
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _state.value = State.Loading
            try {
                val data = withContext(Dispatchers.Default) {
                    getInitialItems()
                }
                _state.value = State.Content(data)
            } catch (e: Exception) {
                when (e) {
                    is CancellationException -> {}
                    else -> {
                        _state.value = State.Error(e.message ?: "Unknown error")
                    }
                }
            }
        }
    }

    fun createNewItem(type: String, name: String, info: String) {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is State.Content) {
                val newId = currentState.items.filter { it !is Item.Header }.maxOfOrNull {
                        (it as? Item.Book)?.id ?: (it as? Item.Newspaper)?.id
                        ?: (it as? Item.Disk)?.id ?: 0
                    }?.plus(1) ?: 1

                val newItem = when (type) {
                    BOOK -> Item.Book(name, newId, info)
                    NEWSPAPER -> Item.Newspaper(name, newId, info)
                    DISK -> Item.Disk(name, newId, info)
                    else -> return@launch
                }

                addItem(newItem)
            }
        }
    }

    fun addItem(newItem: Item) {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is State.Content) {
                val currentList = currentState.items.toMutableList()

                val sectionHeader = when (newItem) {
                    is Item.Book -> "Books"
                    is Item.Newspaper -> "Newspapers"
                    is Item.Disk -> "Disks"
                    else -> return@launch
                }

                val sectionHeaderIndex = currentList.indexOfFirst {
                    it is Item.Header && it.title == sectionHeader
                }

                if (sectionHeaderIndex != -1) {
                    val sectionEndIndex =
                        currentList.subList(sectionHeaderIndex + 1, currentList.size)
                            .indexOfFirst { it is Item.Header } + sectionHeaderIndex + 1

                    val insertPosition = if (sectionEndIndex > sectionHeaderIndex) {
                        sectionEndIndex
                    } else {
                        sectionHeaderIndex + 1
                    }
                    currentList.add(insertPosition, newItem)
                } else {
                    currentList.add(newItem)
                }

                _state.value = State.Content(currentList)

                val newPosition = currentList.indexOfFirst { it == newItem }
                if (newPosition != -1) setScrollPosition(newPosition)
            }
        }
    }

    fun removeItem(position: Int) {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is State.Content) {
                val currentList = currentState.items.toMutableList()
                if (position in 0 until currentList.size) {
                    currentList.removeAt(position)
                    _state.value = State.Content(currentList)
                }
            }
        }
    }

    private fun getInitialItems(): List<Item> {
        Thread.sleep((100..2000).random().toLong())
        val requestCount = (0..20).random()
        if (requestCount % errorRate == 0) throw Exception("Ошибка загрузки")
        return mutableListOf(
            Item.Header("Books"),
            Item.Book("Маугли", 1, "Автор: Редьярд Киплинг, 250 страниц"),
            Item.Book("Бесы", 2, "Автор: Фёдор Достоевский, 760 страниц"),
            Item.Book("Три товарища", 3, "Автор: Эрих Мария Ремарк, 340 страниц"),

            Item.Header("Newspapers"),
            Item.Newspaper("Правда", 4, "Месяц выпуска: Май, 12 номер выпуска"),
            Item.Newspaper("Тайны вселенной", 5, "Месяц выпуска: Январь, 3 номер выпуска"),
            Item.Newspaper("Новости", 6, "Месяц выпуска: Июль, 20 номер выпуска"),

            Item.Header("Disks"),
            Item.Disk("Веном", 7, "Тип диска: DVD"),
            Item.Disk("Форсаж", 8, "Тип диска: CD"),
            Item.Disk("Марвел", 9, "Тип диска: CD")
        )
    }

    companion object {
        const val errorRate = 3
    }
}

