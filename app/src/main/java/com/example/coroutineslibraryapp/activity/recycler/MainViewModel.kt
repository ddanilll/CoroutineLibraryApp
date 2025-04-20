package com.example.coroutineslibraryapp.activity.recycler

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {

    private val _scrollToPosition = MutableLiveData<Int?>()
    val scrollToPosition: LiveData<Int?> = _scrollToPosition

    fun resetScrollPosition() {
        _scrollToPosition.value = null
    }

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val errorRate = 3
    private var requestCount = 0

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                delay((100..2000).random().toLong())

                requestCount++
                if (requestCount % errorRate == 0) throw Exception("Ошибка загрузки")

                val data = withContext(Dispatchers.Default) {
                    getInitialItems()
                }
                _items.value = data
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addItem(newItem: Item) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                delay((100..2000).random().toLong())
            } catch (e: Exception) {
                _error.value = "Ошибка добавления: ${e.message}"
            }
        }
        val currentList = _items.value.toMutableList()
        val sectionHeader = when (newItem) {
            is Item.Book -> "Books"
            is Item.Newspaper -> "Newspapers"
            is Item.Disk -> "Disks"
            else -> return
        }

        val sectionHeaderIndex = currentList.indexOfFirst {
            it is Item.Header && it.title == sectionHeader
        }

        if (sectionHeaderIndex != -1) {
            val sectionEndIndex = currentList.subList(sectionHeaderIndex + 1, currentList.size)
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

        _items.value = currentList

        val newPosition = currentList.indexOfFirst { it == newItem }
        if (newPosition != -1) _scrollToPosition.postValue(newPosition)
    }

    fun removeItem(position: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val currentList = _items.value.toMutableList()
                currentList.removeAt(position)
                _items.value = currentList
            } catch (e: Exception) {
                _error.value = "Ошибка удаления: ${e.message}"
            }
        }
    }

    private fun getInitialItems(): List<Item> {
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
}

