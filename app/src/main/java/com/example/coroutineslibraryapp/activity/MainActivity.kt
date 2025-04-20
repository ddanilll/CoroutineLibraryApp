package com.example.coroutineslibraryapp.actitvity

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.coroutineslibraryapp.R
import com.example.coroutineslibraryapp.actitvity.recycler.InfoFragment
import com.example.coroutineslibraryapp.actitvity.recycler.InfoFragment.Companion.BOOK
import com.example.coroutineslibraryapp.actitvity.recycler.InfoFragment.Companion.DISK
import com.example.coroutineslibraryapp.actitvity.recycler.InfoFragment.Companion.NEWSPAPER
import com.example.coroutineslibraryapp.activity.recycler.Item
import com.example.coroutineslibraryapp.activity.recycler.LibraryFragment
import com.example.coroutineslibraryapp.activity.recycler.MainViewModel
import com.example.coroutineslibraryapp.activity.recycler.adapters.LibraryAdapter

class MainActivity : AppCompatActivity(), InfoFragment.OnItemCreatedListener,
    LibraryAdapter.OnItemClickListener {

    private fun Context.isLandscape(): Boolean {
        val orientation = resources.configuration.orientation
        return orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    private var currentSelectedItem: Item? = null
    private var isDetailsShown = false
    private var isCreateMode = false
    private var createType: String = BOOK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState != null) {
            currentSelectedItem = savedInstanceState.getParcelable(SELECTED_ITEM)
            isDetailsShown = savedInstanceState.getBoolean(DETAILS_SHOWN, false)
            isCreateMode = savedInstanceState.getBoolean(CREATE_MODE, false)
            createType = savedInstanceState.getString(CREATE_TYPE, BOOK)
        } else {
            createType = intent.getStringExtra(CREATE_TYPE) ?: BOOK
        }

        setupFragments()
        setupBackPressHandler()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currentSelectedItem?.let {
            outState.putParcelable(SELECTED_ITEM, it)
        }
        outState.putBoolean(DETAILS_SHOWN, isDetailsShown)
        outState.putBoolean(CREATE_MODE, isCreateMode)
        outState.putString(CREATE_TYPE, createType)
    }

    private fun setupFragments() {
        val existingLibraryFragment =
            supportFragmentManager.findFragmentByTag(LIBRARY_FRAGMENT_TAG) as? LibraryFragment
        val libraryFragment = existingLibraryFragment ?: LibraryFragment().apply {
            setOnItemClickListener(this@MainActivity)
        }

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            val selectedItem = currentSelectedItem

            if (isLandscape()) {
                replace(R.id.list_container, libraryFragment, LIBRARY_FRAGMENT_TAG)


                when {
                    isDetailsShown && selectedItem != null -> {
                        replace(R.id.details_container, InfoFragment.newViewInstance(selectedItem))
                    }

                    isCreateMode -> {
                        replace(R.id.details_container, InfoFragment.newCreateInstance(createType))
                    }

                    else -> {
                        replace(R.id.details_container, Fragment())
                    }
                }
            } else {
                if (isDetailsShown && selectedItem != null) {
                    replace(
                        R.id.list_container,
                        InfoFragment.newViewInstance(selectedItem)
                    ).addToBackStack(null)
                } else if (isCreateMode) {
                    replace(
                        R.id.list_container,
                        InfoFragment.newCreateInstance(createType)
                    ).addToBackStack(null)
                } else {
                    if (supportFragmentManager.findFragmentById(R.id.list_container) !is LibraryFragment) {
                        replace(R.id.list_container, libraryFragment, LIBRARY_FRAGMENT_TAG)
                    }
                }
            }
        }
    }


    fun clearRightPane() {
        isDetailsShown = false
        isCreateMode = false
        supportFragmentManager.beginTransaction().replace(R.id.details_container, Fragment())
            .commit()
    }

    override fun onItemClick(position: Int, item: Item) {
        currentSelectedItem = item
        isDetailsShown = true
        isCreateMode = false
        if (isLandscape()) {
            showDetailsLandscape(item)
        } else {
            showDetailsPortrait(item)
        }
    }

    private fun showDetailsLandscape(item: Item) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.details_container, InfoFragment.newViewInstance(item)).commit()
    }

    private fun showDetailsPortrait(item: Item) {
        isDetailsShown = true
        isCreateMode = false
        supportFragmentManager.beginTransaction()
            .replace(R.id.list_container, InfoFragment.newViewInstance(item)).addToBackStack(null)
            .commit()
    }

    private fun showCreateLandscape(type: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.details_container, InfoFragment.newCreateInstance(type)).commit()
    }

    private fun showCreatePortrait(type: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.list_container, InfoFragment.newCreateInstance(type)).addToBackStack(null)
            .commit()
    }

    override fun onItemCreated(type: String, name: String, info: String) {
        if (name.isBlank() || info.isBlank()) return

        val viewModel: MainViewModel by viewModels()
        val newId = viewModel.items.value.let { items ->
            items.filter { it !is Item.Header }.maxOfOrNull {
                (it as? Item.Book)?.id ?: (it as? Item.Newspaper)?.id ?: (it as? Item.Disk)?.id ?: 0
            }?.plus(1) ?: 1
        }

        val newItem = when (type) {
            BOOK -> Item.Book(name, newId ?: 1, info)
            NEWSPAPER -> Item.Newspaper(name, newId ?: 1, info)
            DISK -> Item.Disk(name, newId ?: 1, info)
            else -> return
        }

        viewModel.addItem(newItem)

        isDetailsShown = false
        isCreateMode = false

        if (!isLandscape()) {
            supportFragmentManager.popBackStack()
        } else {
            clearRightPane()
        }
    }

    fun createNewItem(type: String) {
        isDetailsShown = false
        isCreateMode = true
        createType = type
        if (isLandscape()) {
            showCreateLandscape(type)
        } else {
            showCreatePortrait(type)
        }
    }

    private fun setupBackPressHandler() {
        val viewModel: MainViewModel by viewModels()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isLandscape()) {
                    if (isDetailsShown || isCreateMode) {
                        viewModel.resetScrollPosition()
                        clearRightPane()
                        isDetailsShown = false
                        isCreateMode = false
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                } else {
                    if (supportFragmentManager.backStackEntryCount > 0) {
                        isDetailsShown = false
                        viewModel.resetScrollPosition()
                        isCreateMode = false
                        supportFragmentManager.popBackStack()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        setupFragments()
    }

    companion object {
        const val SELECTED_ITEM = "SELECTED_ITEM"
        const val DETAILS_SHOWN = "DETAILS_SHOWN"
        const val CREATE_MODE = "CREATE_MODE"
        const val CREATE_TYPE = "CREATE_TYPE"
        const val LIBRARY_FRAGMENT_TAG = "LibraryFragmentTag"
    }
}