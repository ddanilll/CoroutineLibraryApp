package com.example.coroutineslibraryapp.actitvity.recycler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.coroutineslibraryapp.R
import com.example.coroutineslibraryapp.actitvity.MainActivity
import com.example.coroutineslibraryapp.activity.recycler.Item

class InfoFragment : Fragment() {
    private lateinit var nameEditText: EditText
    private lateinit var infoEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var iconImageView: ImageView
    private var type: String? = null
    private var item: Item? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.item_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameEditText = view.findViewById(R.id.name)
        infoEditText = view.findViewById(R.id.information)
        saveButton = view.findViewById(R.id.saveButton)
        iconImageView = view.findViewById(R.id.icon)

        arguments?.let { args ->
            if (args.containsKey(EXTRA_ITEM)) {
                item = args.getParcelable(EXTRA_ITEM)
                setupViewMode()
            } else {
                type = args.getString(SUBJECT_TYPE)
                setupCreateMode()
            }
        }
    }

    private fun setupViewMode() {
        nameEditText.isEnabled = false
        infoEditText.isEnabled = false
        saveButton.visibility = View.GONE

        when (item) {
            is Item.Book -> setupBook(item as Item.Book)
            is Item.Newspaper -> setupNewspaper(item as Item.Newspaper)
            is Item.Disk -> setupDisk(item as Item.Disk)
            else -> (activity as? MainActivity)?.clearRightPane()
        }
    }

    private fun setupCreateMode() {
        nameEditText.isEnabled = true
        infoEditText.isEnabled = true
        saveButton.visibility = View.VISIBLE

        when (type) {
            BOOK -> {
                iconImageView.setImageResource(R.drawable.ic_bookimage)
                nameEditText.hint = "Название книги"
                infoEditText.hint = "Информация о книге: "
            }

            NEWSPAPER -> {
                iconImageView.setImageResource(R.drawable.ic_newspaperimage)
                nameEditText.hint = "Название газеты"
                infoEditText.hint = "Информация о газете: "
            }

            DISK -> {
                iconImageView.setImageResource(R.drawable.ic_diskimage)
                nameEditText.hint = "Название диска"
                infoEditText.hint = "Информация о диске "
            }
        }

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val info = infoEditText.text.toString().trim()

            if (name.isBlank() || info.isBlank()) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            (requireActivity() as? OnItemCreatedListener)?.onItemCreated(
                type ?: return@setOnClickListener, name, info
            )
        }
    }

    private fun setupBook(book: Item.Book) {
        nameEditText.setText(book.name)
        infoEditText.setText(book.info)
        iconImageView.setImageResource(R.drawable.ic_bookimage)
    }

    private fun setupNewspaper(newspaper: Item.Newspaper) {
        nameEditText.setText(newspaper.name)
        infoEditText.setText(newspaper.info)
        iconImageView.setImageResource(R.drawable.ic_newspaperimage)
    }

    private fun setupDisk(disk: Item.Disk) {
        nameEditText.setText(disk.name)
        infoEditText.setText(disk.info)
        iconImageView.setImageResource(R.drawable.ic_diskimage)
    }

    interface OnItemCreatedListener {
        fun onItemCreated(type: String, name: String, info: String)
    }

    companion object {
        const val SUBJECT_TYPE = "subjectType"
        const val BOOK = "BOOK"
        const val NEWSPAPER = "NEWSPAPER"
        const val DISK = "DISK"
        const val EXTRA_ITEM = "extra_item"

        fun newCreateInstance(type: String): InfoFragment {
            return InfoFragment().apply {
                arguments = Bundle().apply {
                    putString(SUBJECT_TYPE, type)
                }
            }
        }

        fun newViewInstance(item: Item): InfoFragment {
            return InfoFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(EXTRA_ITEM, item)
                }
            }
        }
    }
}